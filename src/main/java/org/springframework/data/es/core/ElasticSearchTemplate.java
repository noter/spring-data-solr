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
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.convert.EntityInstantiators;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.es.ESClientFactory;
import org.springframework.data.es.TransportClientESClientFactory;
import org.springframework.data.es.UncategorizedESException;
import org.springframework.data.es.core.convert.ElasticSearchConverter;
import org.springframework.data.es.core.convert.MappingElasticSearchConverter;
import org.springframework.data.es.core.mapping.ElasticSearchPersistentEntity;
import org.springframework.data.es.core.mapping.SimpleESMappingContext;
import org.springframework.data.es.core.query.ESDataQuery;
import org.springframework.data.es.core.query.FacetQuery;
import org.springframework.data.es.core.query.Query;
import org.springframework.data.es.core.query.result.FacetPage;
import org.springframework.data.mapping.model.BeanWrapper;
import org.springframework.util.Assert;

/**
 * Implementation of SolrOperations
 * 
 * @author Christoph Strobl
 */
public class ElasticSearchTemplate implements ElasticSearchOperations, InitializingBean, ApplicationContextAware {

	private static final QueryParser DEFAULT_QUERY_PARSER = new QueryParser();
	private static final PersistenceExceptionTranslator exceptionTranslator = new ESExceptionTranslator();

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
		MappingElasticSearchConverter converter = new MappingElasticSearchConverter(new SimpleESMappingContext());
		converter.afterPropertiesSet(); // have to call this one to initialize
										// default converters
		return converter;
	}

	private final ElasticSearchConverter elasticSearchConverter;

	private final EntityInstantiators entityInstantiator = new EntityInstantiators();

	private final ESClientFactory esClientFactory;

	private QueryParser queryParser = DEFAULT_QUERY_PARSER;

	public ElasticSearchTemplate(Client client) {
		this(client, null);
	}

	public ElasticSearchTemplate(Client client, String defaultIndexName) {
		this(new TransportClientESClientFactory(client, defaultIndexName));
	}

	public ElasticSearchTemplate(ESClientFactory esClientFactory) {
		this(esClientFactory, null);
	}

	public ElasticSearchTemplate(ESClientFactory esClientFactory, ElasticSearchConverter elasticSearchConverter) {
		Assert.notNull(esClientFactory, "ESClientFactory must not be 'null'.");
		Assert.notNull(esClientFactory.getElasticSearchClient(), "ESClientFactory has to return a Client.");

		this.esClientFactory = esClientFactory;
		elasticSearchConverter = elasticSearchConverter == null ? getDefaultESConverter() : elasticSearchConverter;
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
	public <T> T convertESJsonToBean(String source, Class<T> clazz) {
		return getConverter().read(clazz, new StringBuilder(source));
	}

	public <T> T execute(ElasticSearchCallback<T> action) {
		Assert.notNull(action);
		try {
			return action.doInES(getElasticSearchClient());
		} catch (Exception e) {
			DataAccessException resolved = getExceptionTranslator().translateExceptionIfPossible(new RuntimeException(e.getMessage(), e));
			throw resolved == null ? new UncategorizedESException(e.getMessage(), e) : resolved;
		}
	}

	@Override
	public IndexResponse executeAddBean(final Object objectToAdd) {
		assertNoCollection(objectToAdd);
		return execute(new ElasticSearchCallback<IndexResponse>() {

			@Override
			public IndexResponse doInES(Client client) throws ElasticSearchException, IOException {
				return prepareIndex(client, objectToAdd).setSource(convertBeanToESJsonSource(objectToAdd)).execute().actionGet();
			}

		});
	}

	@Override
	public BulkResponse executeAddBeans(final Collection<?> beansToAdd) {
		return execute(new ElasticSearchCallback<BulkResponse>() {
			@Override
			public BulkResponse doInES(Client client) throws ElasticSearchException, IOException {
				BulkRequestBuilder prepareBulk = client.prepareBulk();
				for (Object object : beansToAdd) {
					prepareBulk.add(prepareIndex(client, object).setSource(convertBeanToESJsonSource(object)));
				}
				return prepareBulk.execute().actionGet();
			}

		});
	}

	@Override
	public long executeCount(final ESDataQuery query) {
		Assert.notNull(query, "Query must not be 'null'.");

		return execute(new ElasticSearchCallback<Long>() {

			@Override
			public Long doInES(Client client) throws ElasticSearchException, IOException {
				return queryParser.constructESSearchQuery(query, client).setFrom(0).setSize(0).execute().actionGet().getHits().getTotalHits();
			}

		});
	}

	@Override
	public DeleteByQueryResponse executeDelete(final ESDataQuery query) {
		Assert.notNull(query, "Query must not be 'null'.");

		return execute(new ElasticSearchCallback<DeleteByQueryResponse>() {

			@Override
			public DeleteByQueryResponse doInES(Client client) throws ElasticSearchException, IOException {
				return client.prepareDeleteByQuery().setQuery(queryParser.getESQuery(query)).execute().actionGet();
			}
		});
	}

	@Override
	public List<DeleteResponse> executeDeleteById(final Collection<String> ids) {
		Assert.notNull(ids, "Cannot delete 'null' collection.");

		return execute(new ElasticSearchCallback<List<DeleteResponse>>() {

			@Override
			public List<DeleteResponse> doInES(Client client) throws ElasticSearchException, IOException {
				List<DeleteResponse> deleteResponses = new ArrayList<DeleteResponse>();
				for (String id : ids) {
					deleteResponses.add(executeDeleteById(id));
				}
				return deleteResponses;
			}
		});
	}

	@Override
	public DeleteResponse executeDeleteById(final String id) {
		Assert.notNull(id, "Cannot delete 'null' id.");

		return execute(new ElasticSearchCallback<DeleteResponse>() {

			@Override
			public DeleteResponse doInES(Client client) throws ElasticSearchException, IOException {
				return client.prepareDelete().setId(id).execute().actionGet();
			}
		});

	}

	@Override
	public <T> FacetPage<T> executeFacetQuery(final FacetQuery query, final Class<T> clazz) {
		Assert.notNull(query, "Query must not be 'null'.");
		Assert.notNull(clazz, "Target class must not be 'null'.");

		return execute(new ElasticSearchCallback<FacetPage<T>>() {

			@Override
			public FacetPage<T> doInES(Client client) throws ElasticSearchException, IOException {
				SearchRequestBuilder builder = queryParser.constructESSearchQuery(query, client);
				ElasticSearchPersistentEntity<?> persistentEntity = getConverter().getMappingContext().getPersistentEntity(clazz);

				builder.setIndices(persistentEntity.getIndexName()).setTypes(persistentEntity.getTypeName());

				SearchResponse actionGet = builder.execute().actionGet();
				FacetPage<T> facetPage = new FacetPage<T>(convertSearchResponse(actionGet, clazz), query.getPageRequest(), actionGet.getHits()
						.getTotalHits());

				return facetPage;
			}
		});

		QueryResponse response = executeQuery(query);

		FacetPage<T> page = new FacetPage<T>(response.getBeans(clazz), query.getPageRequest(), response.getResults().getNumFound());
		page.addAllFacetResultPages(ResultHelper.convertFacetQueryResponseToFacetPageMap(query, response));

		return page;
	}

	@Override
	public <T> Page<T> executeListQuery(Query query, Class<T> clazz) {
		Assert.notNull(query, "Query must not be 'null'.");
		Assert.notNull(clazz, "Target class must not be 'null'.");

		QueryResponse response = executeQuery(query);
		return new PageImpl<T>(response.getBeans(clazz), query.getPageRequest(), response.getResults().getNumFound());
	}

	@Override
	public <T> T executeObjectQuery(Query query, Class<T> clazz) {
		Assert.notNull(query, "Query must not be 'null'.");
		Assert.notNull(clazz, "Target class must not be 'null'.");

		query.setPageRequest(new PageRequest(0, 1));
		QueryResponse response = executeQuery(query);

		if (response.getResults().size() > 0) {
			if (response.getResults().size() > 1) {
				LOGGER.warn("More than 1 result found for singe result query ('{}'), returning first entry in list");
			}
			return response.getBeans(clazz).get(0);
		}
		return null;
	}

	@Override
	public final QueryResponse executeQuery(SolrDataQuery query) {
		Assert.notNull(query, "Query must not be 'null'");

		SolrQuery solrQuery = queryParser.constructSolrQuery(query);
		LOGGER.debug("Executing query '" + solrQuery + "' against solr.");

		return executeSolrQuery(solrQuery);
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

	final QueryResponse executeSolrQuery(final SolrQuery solrQuery) {
		return execute(new SolrCallback<QueryResponse>() {
			@Override
			public QueryResponse doInSolr(SolrServer solrServer) throws SolrServerException, IOException {
				return solrServer.query(solrQuery);
			}
		});
	}

	private <T> List<T> convertSearchResponse(SearchResponse searchResponse, Class<T> clazz) {
		List<T> list = new ArrayList<T>();
		for (SearchHit searchHit : searchResponse.getHits()) {
			list.add(convertESJsonToBean(searchHit.getSourceAsString(), clazz));
		}
		return list;
	}

	private IndexRequestBuilder prepareIndex(Client client, Object object) {
		ElasticSearchPersistentEntity<?> elasticSearchPersistentEntity = getConverter().getMappingContext().getPersistentEntity(object.getClass());
		BeanWrapper<ElasticSearchPersistentEntity<Object>, Object> beanWrapper = BeanWrapper.create(object, getConverter().getConversionService());
		return client.prepareIndex(elasticSearchPersistentEntity.getIndexName(), elasticSearchPersistentEntity.getTypeName(), getConverter()
				.getConversionService().convert(beanWrapper.getProperty(elasticSearchPersistentEntity.getIdProperty()), String.class));
	}
}
