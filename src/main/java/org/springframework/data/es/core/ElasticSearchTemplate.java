/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.es.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.es.ElasticSearchClientFactory;
import org.springframework.data.es.TransportClientElasticSearchFactory;
import org.springframework.data.es.UncategorizedElasticSearchException;
import org.springframework.data.es.core.convert.ElasticSearchConverter;
import org.springframework.data.es.core.convert.MappingElasticSearchConverter;
import org.springframework.data.es.core.mapping.ElasticSearchPersistentEntity;
import org.springframework.data.es.core.mapping.ElasticSearchPersistentProperty;
import org.springframework.data.es.core.mapping.SimpleElasticSearchMappingContext;
import org.springframework.data.es.core.query.ElasticSearchDataQuery;
import org.springframework.data.es.core.query.FacetQuery;
import org.springframework.data.es.core.query.Query;
import org.springframework.data.es.core.query.result.FacetPage;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.BeanWrapper;
import org.springframework.util.Assert;

/**
 * Implementation of ElasticSearchOperations
 * 
 * @author Patryk Wąsik
 */
public class ElasticSearchTemplate implements ElasticSearchOperations, InitializingBean, ApplicationContextAware {

	private static final QueryParser DEFAULT_QUERY_PARSER = new QueryParser();
	private static final PersistenceExceptionTranslator exceptionTranslator = new ElasticSearchExceptionTranslator();

	@SuppressWarnings("serial")
	private static final List<String> ITERABLE_CLASSES = new ArrayList<String>() {
		{
			add(List.class.getName());
			add(Collection.class.getName());
			add(Iterator.class.getName());
		}
	};

	private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchTemplate.class);

	public static PersistenceExceptionTranslator getExceptionTranslator() {
		return exceptionTranslator;
	}

	private static final ElasticSearchConverter getDefaultESConverter() {
		MappingElasticSearchConverter converter = new MappingElasticSearchConverter(new SimpleElasticSearchMappingContext());
		converter.afterPropertiesSet(); // have to call this one to initialize
										// default converters
		return converter;
	}

	private final ElasticSearchConverter elasticSearchConverter;

	private final ElasticSearchClientFactory esClientFactory;

	private QueryParser queryParser = DEFAULT_QUERY_PARSER;

	public ElasticSearchTemplate(Client client) {
		this(client, null);
	}

	public ElasticSearchTemplate(Client client, String defaultIndexName) {
		this(new TransportClientElasticSearchFactory(client, defaultIndexName));
	}

	public ElasticSearchTemplate(ElasticSearchClientFactory esClientFactory) {
		this(esClientFactory, null);
	}

	public ElasticSearchTemplate(ElasticSearchClientFactory esClientFactory, ElasticSearchConverter elasticSearchConverter) {
		Assert.notNull(esClientFactory, "ESClientFactory must not be 'null'.");
		Assert.notNull(esClientFactory.getElasticSearchClient(), "ESClientFactory has to return a Client.");

		this.esClientFactory = esClientFactory;
		this.elasticSearchConverter = elasticSearchConverter == null ? getDefaultESConverter() : elasticSearchConverter;
	}

	@Override
	public void afterPropertiesSet() {
		if (queryParser == null) {
			LOGGER.warn("QueryParser not set, using default one.");
			queryParser = DEFAULT_QUERY_PARSER;
		}
	}

	@Override
	public String convertBeanToESJsonSource(Object bean) {
		StringBuilder builder = new StringBuilder();
		getConverter().write(bean, builder);
		return builder.toString();
	}

	@Override
	public <T> T convertESJsonSourceToBean(String source, Class<T> clazz) {
		return getConverter().read(clazz, new StringBuilder(source));
	}

	@Override
	public long count(final ElasticSearchDataQuery query, Class<?>... types) {
		Assert.notNull(query, "Query must not be 'null'.");

		return countInternal(query, indicateNames(types), types);
	}

	@Override
	public long count(ElasticSearchDataQuery query, String... indices) {
		return countInternal(query, indices, new Class[0]);
	}

	@Override
	public long count(ElasticSearchDataQuery query, String[] indices, Class<?>... types) {
		return countInternal(query, indices, types);
	}

	@Override
	public DeleteByQueryResponse delete(final ElasticSearchDataQuery query, final Class<?>... type) {
		Assert.notNull(query, "Query must not be 'null'.");
		return deleteInternal(query, new String[0], type);

	}

	@Override
	public DeleteByQueryResponse delete(ElasticSearchDataQuery query, String... indices) {
		return deleteInternal(query, indices, new Class[0]);
	}

	@Override
	public DeleteByQueryResponse delete(ElasticSearchDataQuery query, String[] indices, Class<?>... types) {
		return deleteInternal(query, indices, types);
	}

	@Override
	public List<DeleteResponse> deleteById(final Collection<String> ids, final Class<?> type) {
		Assert.notNull(ids, "Cannot delete 'null' collection.");
		Assert.notNull(type, "Type can't be null");

		return execute(new ElasticSearchCallback<List<DeleteResponse>>() {

			@Override
			public List<DeleteResponse> doInES(Client client) throws ElasticSearchException, IOException {
				List<DeleteResponse> deleteResponses = new ArrayList<DeleteResponse>();
				for (String id : ids) {
					deleteResponses.add(deleteById(id, type));
				}
				return deleteResponses;
			}
		});
	}

	@Override
	public DeleteResponse deleteById(final String id, final Class<?> type) {
		Assert.notNull(id, "Cannot delete 'null' id.");
		Assert.notNull(type, "Type can't be null");

		return execute(new ElasticSearchCallback<DeleteResponse>() {
			@Override
			public DeleteResponse doInES(Client client) throws ElasticSearchException, IOException {
				return client.prepareDelete().setIndex(indicateName(type)).setType(typeName(type)).setId(id).execute().actionGet();
			}
		});

	}

	public <T> T execute(ElasticSearchCallback<T> action) {
		Assert.notNull(action);
		try {
			return action.doInES(getElasticSearchClient());
		} catch (Exception e) {
			DataAccessException resolved = getExceptionTranslator().translateExceptionIfPossible(new RuntimeException(e.getMessage(), e));
			throw resolved == null ? new UncategorizedElasticSearchException(e.getMessage(), e) : resolved;
		}
	}

	@Override
	public <T> FacetPage<T> findAll(final FacetQuery query, final Class<T> clazz) {
		Assert.notNull(query, "Query must not be 'null'.");
		Assert.notNull(clazz, "Target class must not be 'null'.");

		return execute(new ElasticSearchCallback<FacetPage<T>>() {

			@Override
			public FacetPage<T> doInES(Client client) throws ElasticSearchException, IOException {

				SearchResponse searchResponse = execute(client, query, clazz);

				FacetPage<T> facetPage = new FacetPage<T>(convertSearchResponse(searchResponse, clazz), query.getPageRequest(), searchResponse
						.getHits().getTotalHits());
				facetPage.addAllFacetResultPages(ResultHelper.convertFacetQueryResponseToFacetPageMap(query, searchResponse.facets()));
				return facetPage;
			}
		});
	}

	@Override
	public <T> Page<T> findAll(final Query query, final Class<T> clazz) {
		Assert.notNull(query, "Query must not be 'null'.");
		Assert.notNull(clazz, "Target class must not be 'null'.");

		return execute(new ElasticSearchCallback<Page<T>>() {

			@Override
			public Page<T> doInES(Client client) throws ElasticSearchException, IOException {

				SearchRequestBuilder builder = queryParser.constructESSearchQuery(query, client);
				ElasticSearchPersistentEntity<?> persistentEntity = getConverter().getMappingContext().getPersistentEntity(clazz);

				builder.setIndices(persistentEntity.getIndexName()).setTypes(persistentEntity.getTypeName());

				SearchResponse actionGet = builder.execute().actionGet();
				PageImpl<T> page = new PageImpl<T>(convertSearchResponse(actionGet, clazz), query.getPageRequest(), actionGet.getHits()
						.getTotalHits());
				return page;
			}
		});
	}

	@Override
	public <T> T findById(final String id, final Class<T> clazz) {
		return execute(new ElasticSearchCallback<T>() {

			@Override
			public T doInES(Client client) throws ElasticSearchException, IOException {
				GetResponse getResponse = client.prepareGet(indicateName(clazz), typeName(clazz), id).execute().actionGet();
				if (getResponse.exists()) {
					return convertESJsonSourceToBean(getResponse.getSourceAsString(), clazz);
				}
				return null;
			}
		});
	}

	@Override
	public <T> T findOne(final Query query, final Class<T> clazz) {
		Assert.notNull(query, "Query must not be 'null'.");
		Assert.notNull(clazz, "Target class must not be 'null'.");

		return execute(new ElasticSearchCallback<T>() {

			@Override
			public T doInES(Client client) throws ElasticSearchException, IOException {
				SearchResponse response = client.prepareSearch(indicateName(clazz)).setTypes(typeName(clazz)).setQuery(queryParser.getESQuery(query))
						.setFrom(0).setSize(1).execute().actionGet();
				if (response.getHits().getTotalHits() > 0) {
					if (response.getHits().getTotalHits() > 1) {
						LOGGER.warn("More than 1 result found for singe result query ('{}'), returning first entry in list");
					}
					return convertESJsonSourceToBean(response.getHits().getAt(0).getSourceAsString(), clazz);
				}
				return null;
			}
		});
	}

	@Override
	public ElasticSearchConverter getConverter() {
		return elasticSearchConverter;
	}

	@Override
	public Client getElasticSearchClient() {
		return esClientFactory.getElasticSearchClient();
	}

	@Override
	public final SearchResponse query(final ElasticSearchDataQuery query) {
		Assert.notNull(query, "Query must not be 'null'");

		return execute(new ElasticSearchCallback<SearchResponse>() {

			@Override
			public SearchResponse doInES(Client client) throws ElasticSearchException, IOException {
				return queryParser.constructESSearchQuery(query, client).execute().actionGet();
			}
		});
	}

	@Override
	public void refresh(final Class<?>... types) {
		refresh(indicateNames(types));
	}

	@Override
	public void refresh(final String... indices) {
		execute(new ElasticSearchCallback<Void>() {

			@Override
			public Void doInES(Client client) throws ElasticSearchException, IOException {
				client.admin().indices().refresh(new RefreshRequest(indices)).actionGet();
				return null;
			}
		});

	}

	@Override
	public BulkResponse save(final Collection<?> beansToAdd) {
		return execute(new ElasticSearchCallback<BulkResponse>() {
			@Override
			public BulkResponse doInES(Client client) throws ElasticSearchException, IOException {
				BulkRequestBuilder prepareBulk = client.prepareBulk();
				for (Object object : beansToAdd) {
					prepareBulk.add(prepareIndex(client, object).setId(beanToId(object)).setSource(convertBeanToESJsonSource(object)));
				}
				return prepareBulk.execute().actionGet();
			}

		});
	}

	@Override
	public IndexResponse save(final Object objectToAdd) {
		assertNoCollection(objectToAdd);
		return execute(new ElasticSearchCallback<IndexResponse>() {

			@Override
			public IndexResponse doInES(Client client) throws ElasticSearchException, IOException {
				return prepareIndex(client, objectToAdd).setId(beanToId(objectToAdd)).setSource(convertBeanToESJsonSource(objectToAdd)).execute()
						.actionGet();
			}

		});
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		// future use
	}

	protected void assertNoCollection(Object o) {
		if (null != o) {
			if (o.getClass().isArray() || ITERABLE_CLASSES.contains(o.getClass().getName())) {
				throw new IllegalArgumentException("Collections are not supported for this operation");
			}
		}
	}

	DeleteByQueryResponse deleteInternal(final ElasticSearchDataQuery query, String[] indices, final Class<?>[] types) {
		return execute(new ElasticSearchCallback<DeleteByQueryResponse>() {

			@Override
			public DeleteByQueryResponse doInES(Client client) throws ElasticSearchException, IOException {

				return client.prepareDeleteByQuery().setIndices(indicateNames(types)).setTypes(typeNames(types))
						.setQuery(queryParser.getESQuery(query)).execute().actionGet();
			}
		});
	}

	private String beanToId(Object bean) {
		return (String) BeanWrapper.create(bean, null).getProperty(getMappingContext().getPersistentEntity(bean.getClass()).getIdProperty());
	}

	private <T> List<T> convertSearchResponse(SearchResponse searchResponse, Class<T> clazz) {
		List<T> list = new ArrayList<T>();
		for (SearchHit searchHit : searchResponse.getHits()) {
			list.add(convertESJsonSourceToBean(searchHit.getSourceAsString(), clazz));
		}
		return list;
	}

	private long countInternal(final ElasticSearchDataQuery query, final String[] indices, final Class<?>[] types) {
		return execute(new ElasticSearchCallback<Long>() {

			@Override
			public Long doInES(Client client) throws ElasticSearchException, IOException {

				QueryBuilder queryBuilder = queryParser.getESQuery(query);
				return client.prepareCount(indices).setTypes(typeNames(types)).setQuery(queryBuilder).execute().actionGet().count();
			}
		});
	}

	private SearchResponse execute(Client client, Query query, Class<?> clazz) {
		SearchRequestBuilder builder = queryParser.constructESSearchQuery(query, client);
		ElasticSearchPersistentEntity<?> persistentEntity = getConverter().getMappingContext().getPersistentEntity(clazz);

		builder.setIndices(persistentEntity.getIndexName()).setTypes(persistentEntity.getTypeName());

		return builder.execute().actionGet();
	}

	private MappingContext<? extends ElasticSearchPersistentEntity<?>, ElasticSearchPersistentProperty> getMappingContext() {
		Assert.notNull(getConverter());
		return getConverter().getMappingContext();
	}

	private String indicateName(Class<?> clazz) {
		ElasticSearchPersistentEntity<?> persistentEntity = getMappingContext().getPersistentEntity(clazz);

		Assert.notNull(persistentEntity);

		return persistentEntity.getIndexName();
	}

	private String[] indicateNames(Class<?>[] classes) {
		List<String> indicateNames = new ArrayList<String>();
		for (Class<?> clazz : classes) {
			indicateNames.add(indicateName(clazz));
		}
		return indicateNames.toArray(new String[0]);
	}

	private IndexRequestBuilder prepareIndex(Client client, Object object) {
		ElasticSearchPersistentEntity<?> elasticSearchPersistentEntity = getConverter().getMappingContext().getPersistentEntity(object.getClass());
		BeanWrapper<ElasticSearchPersistentEntity<Object>, Object> beanWrapper = BeanWrapper.create(object, getConverter().getConversionService());
		return client.prepareIndex(elasticSearchPersistentEntity.getIndexName(), elasticSearchPersistentEntity.getTypeName(), getConverter()
				.getConversionService().convert(beanWrapper.getProperty(elasticSearchPersistentEntity.getIdProperty()), String.class));
	}

	private String typeName(Class<?> clazz) {
		ElasticSearchPersistentEntity<?> persistentEntity = getMappingContext().getPersistentEntity(clazz);

		Assert.notNull(persistentEntity);

		return persistentEntity.getTypeName();
	}

	private String[] typeNames(Class<?>[] classes) {
		List<String> typeNames = new ArrayList<String>();
		for (Class<?> clazz : classes) {
			typeNames.add(typeName(clazz));
		}
		return typeNames.toArray(new String[0]);
	}
}
