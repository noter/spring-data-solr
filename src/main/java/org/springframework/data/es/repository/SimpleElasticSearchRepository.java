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
package org.springframework.data.es.repository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.es.core.ElasticSearchOperations;
import org.springframework.data.es.core.query.Criteria;
import org.springframework.data.es.core.query.MatchAllCriteria;
import org.springframework.data.es.core.query.SimpleFilterQuery;
import org.springframework.data.es.core.query.SimpleQuery;
import org.springframework.data.es.repository.query.ElasticSearchEntityInformation;
import org.springframework.util.Assert;

/**
 * Solr specific repository implementation. Likely to be used as target within
 * {@link SolrRepositoryFactory}
 * 
 * @param <T>
 * 
 * @author Patryk Wasik
 */
public class SimpleElasticSearchRepository<T> implements ElasticSearchCrudRepository<T, String> {

	private static final String DEFAULT_ID_FIELD = "id";

	private ElasticSearchOperations elasticSearchOperations;
	private Class<T> entityClass;
	private ElasticSearchEntityInformation<T, String> entityInformation;
	private String idFieldName = DEFAULT_ID_FIELD;

	public SimpleElasticSearchRepository() {

	}

	public SimpleElasticSearchRepository(ElasticSearchEntityInformation<T, String> metadata, ElasticSearchOperations elasticSearchOperations) {
		this(elasticSearchOperations);
		Assert.notNull(metadata);

		this.entityInformation = metadata;
		setIdFieldName(this.entityInformation.getIdAttribute());
		setEntityClass(this.entityInformation.getJavaType());
	}

	public SimpleElasticSearchRepository(ElasticSearchOperations solrOperations) {
		Assert.notNull(solrOperations);

		this.setElasticSearchOperations(solrOperations);
	}

	public SimpleElasticSearchRepository(ElasticSearchOperations solrOperations, Class<T> entityClass) {
		this(solrOperations);

		this.setEntityClass(entityClass);
	}

	@Override
	public long count() {
		return elasticSearchOperations.count(new SimpleQuery(new MatchAllCriteria()), entityClass);
	}

	@Override
	public void delete(Iterable<? extends T> entities) {
		Assert.notNull(entities, "Cannot delete 'null' list.");

		ArrayList<String> idsToDelete = new ArrayList<String>();
		for (T entity : entities) {
			idsToDelete.add(extractIdFromBean(entity));
		}
		this.elasticSearchOperations.executeDeleteById(idsToDelete);
		this.elasticSearchOperations.executeCommit();
	}

	@Override
	public void delete(String id) {
		Assert.notNull(id, "Cannot delete entity with id 'null'.");

		this.elasticSearchOperations.executeDeleteById(id);
		this.elasticSearchOperations.executeCommit();
	}

	@Override
	public void delete(T entity) {
		Assert.notNull(entity, "Cannot delete 'null' entity.");

		delete(Arrays.asList(entity));
	}

	@Override
	public void deleteAll() {
		this.elasticSearchOperations.executeDelete(new SimpleFilterQuery(new Criteria(Criteria.WILDCARD).expression(Criteria.WILDCARD)));
		this.elasticSearchOperations.executeCommit();
	}

	@Override
	public boolean exists(String id) {
		return findOne(id) != null;
	}

	@Override
	public Iterable<T> findAll() {
		int itemCount = (int) this.count();
		if (itemCount == 0) {
			return new PageImpl<T>(Collections.<T> emptyList());
		}
		return this.findAll(new PageRequest(0, Math.max(1, itemCount)));
	}

	@Override
	public Iterable<T> findAll(Iterable<String> ids) {
		org.springframework.data.es.core.query.Query query = new SimpleQuery(new Criteria(this.idFieldName).in(ids));
		query.setPageRequest(new PageRequest(0, Math.max(1, (int) count(query))));

		return getSolrOperations().executeListQuery(query, getEntityClass());
	}

	@Override
	public Page<T> findAll(Pageable pageable) {
		return getSolrOperations().executeListQuery(
				new SimpleQuery(new Criteria(Criteria.WILDCARD).expression(Criteria.WILDCARD)).setPageRequest(pageable), getEntityClass());
	}

	@Override
	public Iterable<T> findAll(Sort sort) {
		int itemCount = (int) this.count();
		if (itemCount == 0) {
			return new PageImpl<T>(Collections.<T> emptyList());
		}
		return getSolrOperations().executeListQuery(
				new SimpleQuery(new Criteria(Criteria.WILDCARD).expression(Criteria.WILDCARD)).setPageRequest(
						new PageRequest(0, Math.max(1, itemCount))).addSort(sort), getEntityClass());
	}

	@Override
	public T findOne(String id) {
		return (T) getSolrOperations().executeObjectQuery(new SimpleQuery(new Criteria(this.idFieldName).is(id)), getEntityClass());
	}

	public final ElasticSearchOperations getElasticSearchOperations() {
		return elasticSearchOperations;
	}

	public Class<T> getEntityClass() {
		if (!isEntityClassSet()) {
			try {
				this.entityClass = resolveReturnedClassFromGernericType();
			} catch (Exception e) {
				throw new InvalidDataAccessApiUsageException("Unable to resolve EntityClass. Please use according setter!", e);
			}
		}
		return entityClass;
	}

	public final String getIdFieldName() {
		return idFieldName;
	}

	@Override
	public <S extends T> Iterable<S> save(Iterable<S> entities) {
		Assert.notNull(entities, "Cannot insert 'null' as a List.");

		if (!(entities instanceof Collection<?>)) {
			throw new InvalidDataAccessApiUsageException("Entities have to be inside a collection");
		}

		this.elasticSearchOperations.executeAddBeans((Collection<? extends T>) entities);
		this.elasticSearchOperations.executeCommit();
		return entities;
	}

	@Override
	public <S extends T> S save(S entity) {
		Assert.notNull(entity, "Cannot save 'null' entity.");

		this.elasticSearchOperations.executeAddBean(entity);
		this.elasticSearchOperations.executeCommit();
		return entity;
	}

	public final void setElasticSearchOperations(ElasticSearchOperations elasticSearchOperations) {
		Assert.notNull(elasticSearchOperations, "SolrOperations must not be null.");

		this.elasticSearchOperations = elasticSearchOperations;
	}

	public final void setEntityClass(Class<T> entityClass) {
		Assert.notNull(entityClass, "EntityClass must not be null.");

		this.entityClass = entityClass;
	}

	public final void setIdFieldName(String idFieldName) {
		Assert.notNull(idFieldName, "ID Field cannot be null.");

		this.idFieldName = idFieldName;
	}

	protected long count(org.springframework.data.es.core.query.Query query) {
		org.springframework.data.es.core.query.Query countQuery = SimpleQuery.fromQuery(query);
		return getSolrOperations().executeCount(countQuery);
	}

	private String extractIdFromBean(T entity) {
		if (entityInformation != null) {
			return entityInformation.getId(entity);
		}

		SolrInputDocument solrInputDocument = this.elasticSearchOperations.convertBeanToSolrInputDocument(entity);
		return extractIdFromSolrInputDocument(solrInputDocument);
	}

	private String extractIdFromSolrInputDocument(SolrInputDocument solrInputDocument) {
		Assert.notNull(solrInputDocument.getField(idFieldName), "Unable to find field '" + idFieldName + "' in SolrDocument.");
		Assert.notNull(solrInputDocument.getField(idFieldName).getValue(), "ID must not be 'null'.");

		return solrInputDocument.getField(idFieldName).getValue().toString();
	}

	private boolean isEntityClassSet() {
		return entityClass != null;
	}

	@SuppressWarnings("unchecked")
	private Class<T> resolveReturnedClassFromGernericType() {
		ParameterizedType parameterizedType = resolveReturnedClassFromGernericType(getClass());
		return (Class<T>) parameterizedType.getActualTypeArguments()[0];
	}

	private ParameterizedType resolveReturnedClassFromGernericType(Class<?> clazz) {
		Object genericSuperclass = clazz.getGenericSuperclass();
		if (genericSuperclass instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
			Type rawtype = parameterizedType.getRawType();
			if (SimpleElasticSearchRepository.class.equals(rawtype)) {
				return parameterizedType;
			}
		}
		return resolveReturnedClassFromGernericType(clazz.getSuperclass());
	}

}
