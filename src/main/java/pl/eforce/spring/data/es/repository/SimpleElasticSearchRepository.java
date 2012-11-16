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
package pl.eforce.spring.data.es.repository;

import java.io.Serializable;
import java.util.Collections;

import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;

import pl.eforce.spring.data.es.core.ElasticSearchOperations;
import pl.eforce.spring.data.es.core.query.Criteria;
import pl.eforce.spring.data.es.core.query.MatchAllCriteria;
import pl.eforce.spring.data.es.core.query.SimpleQuery;
import pl.eforce.spring.data.es.repository.query.ElasticSearchEntityInformation;
import pl.eforce.spring.data.es.repository.support.ElasticSearchRepositoryFactory;

import com.google.common.collect.Lists;

/**
 * ElasticSearch specific repository implementation. Likely to be used as target within {@link ElasticSearchRepositoryFactory}
 * 
 * @param <T>
 * 
 * @author Patryk Wasik
 */
public class SimpleElasticSearchRepository<T, ID extends Serializable> implements ElasticSearchCrudRepository<T, ID> {

	private final ConversionService conversionService;
	private final ElasticSearchOperations elasticSearchOperations;
	private final Class<T> entityClass;
	private final ElasticSearchEntityInformation<T, String> entityInformation;

	private final String idFieldName;

	public SimpleElasticSearchRepository(ElasticSearchEntityInformation<T, String> metadata, ElasticSearchOperations elasticSearchOperations) {
		Assert.notNull(metadata);
		Assert.notNull(elasticSearchOperations);

		this.elasticSearchOperations = elasticSearchOperations;
		conversionService = elasticSearchOperations.getConverter().getConversionService();

		this.entityInformation = metadata;
		idFieldName = this.entityInformation.getIdAttribute();
		entityClass = this.entityInformation.getJavaType();
	}

	@Override
	public long count() {
		return elasticSearchOperations.count(new SimpleQuery(new MatchAllCriteria()), entityClass);
	}

	@Override
	public void delete(ID id) {
		Assert.notNull(id, "Cannot delete entity with id 'null'.");

		getElasticSearchOperations().deleteById(conversionService.convert(id, String.class), entityClass);
	}

	@Override
	public void delete(Iterable<? extends T> entities) {
		Assert.notNull(entities, "Cannot delete 'null' list.");

		getElasticSearchOperations().delete(Lists.newArrayList(entities));
	}

	@Override
	public void delete(T entity) {
		Assert.notNull(entity, "Cannot delete 'null' entity.");

		getElasticSearchOperations().delete(entity);
	}

	@Override
	public void deleteAll() {
		getElasticSearchOperations().delete(new SimpleQuery(new MatchAllCriteria()), entityClass);
	}

	@Override
	public boolean exists(ID id) {
		return count(new SimpleQuery(new Criteria(this.idFieldName).is(id))) == 1;
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
	public Iterable<T> findAll(Iterable<ID> ids) {
		pl.eforce.spring.data.es.core.query.Query query = new SimpleQuery(new Criteria(this.idFieldName).in(ids));
		query.setPageRequest(new PageRequest(0, Math.max(1, (int) count(query))));

		return getElasticSearchOperations().findAll(query, getEntityClass());
	}

	@Override
	public Page<T> findAll(Pageable pageable) {
		return getElasticSearchOperations().findAll(new SimpleQuery(new MatchAllCriteria()).setPageRequest(pageable), getEntityClass());
	}

	@Override
	public Iterable<T> findAll(Sort sort) {
		int itemCount = (int) this.count();
		if (itemCount == 0) {
			return new PageImpl<T>(Collections.<T> emptyList());
		}
		return getElasticSearchOperations().findAll(
				new SimpleQuery(new MatchAllCriteria()).setPageRequest(new PageRequest(0, Math.max(1, itemCount))).addSort(sort), getEntityClass());
	}

	@Override
	public T findOne(ID id) {
		return getElasticSearchOperations().findOne(new SimpleQuery(new Criteria(this.idFieldName).is(id)), getEntityClass());
	}

	public final ElasticSearchOperations getElasticSearchOperations() {
		return elasticSearchOperations;
	}

	public Class<T> getEntityClass() {
		return entityClass;
	}

	public final String getIdFieldName() {
		return idFieldName;
	}

	@Override
	public <S extends T> Iterable<S> save(Iterable<S> entities) {
		Assert.notNull(entities, "Cannot insert 'null' as a List.");

		getElasticSearchOperations().save(Lists.newArrayList(entities));
		return entities;
	}

	@Override
	public <S extends T> S save(S entity) {
		Assert.notNull(entity, "Cannot save 'null' entity.");

		getElasticSearchOperations().save(entity);
		return entity;
	}

	protected long count(pl.eforce.spring.data.es.core.query.Query query) {
		pl.eforce.spring.data.es.core.query.Query countQuery = SimpleQuery.fromQuery(query);
		return getElasticSearchOperations().count(countQuery, entityClass);
	}

}
