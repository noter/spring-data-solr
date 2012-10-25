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
package org.springframework.data.es.repository.support;

import static org.springframework.data.querydsl.QueryDslUtils.QUERY_DSL_PRESENT;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.data.es.core.ElasticSearchOperations;
import org.springframework.data.es.repository.ElasticSearchRepository;
import org.springframework.data.es.repository.SimpleElasticSearchRepository;
import org.springframework.data.es.repository.query.ElasticSearchEntityInformation;
import org.springframework.data.es.repository.query.ElasticSearchEntityInformationCreator;
import org.springframework.data.es.repository.query.ElasticSearchQueryMethod;
import org.springframework.data.es.repository.query.PartTreeElasticSearchQuery;
import org.springframework.data.es.repository.query.StringBasedElasticSearchQuery;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.Assert;

/**
 * Factory to create {@link ElasticSearchRepository}
 * 
 * @author Patryk Wasik
 */
public class ElasticSearchRepositoryFactory extends RepositoryFactorySupport {

	private class ElasticSearchQueryLookupStrategy implements QueryLookupStrategy {

		@Override
		public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, NamedQueries namedQueries) {

			ElasticSearchQueryMethod queryMethod = new ElasticSearchQueryMethod(method, metadata, entityInformationCreator);
			String namedQueryName = queryMethod.getNamedQueryName();

			if (namedQueries.hasQuery(namedQueryName)) {
				String namedQuery = namedQueries.getQuery(namedQueryName);
				return new StringBasedElasticSearchQuery(namedQuery, queryMethod, elasticSearchOperations);
			} else if (queryMethod.hasAnnotatedQuery()) {
				return new StringBasedElasticSearchQuery(queryMethod, elasticSearchOperations);
			} else {
				return new PartTreeElasticSearchQuery(queryMethod, elasticSearchOperations);
			}
		}

	}

	private static boolean isQueryDslRepository(Class<?> repositoryInterface) {
		return QUERY_DSL_PRESENT && QueryDslPredicateExecutor.class.isAssignableFrom(repositoryInterface);
	}

	private final ElasticSearchOperations elasticSearchOperations;

	private final ElasticSearchEntityInformationCreator entityInformationCreator;

	public ElasticSearchRepositoryFactory(ElasticSearchOperations elasticSearchOperations) {
		Assert.notNull(elasticSearchOperations);
		this.elasticSearchOperations = elasticSearchOperations;
		entityInformationCreator = new ElasticSearchEntityInformationCreatorImpl(elasticSearchOperations.getConverter().getMappingContext());
	}

	@Override
	public <T, ID extends Serializable> ElasticSearchEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
		return entityInformationCreator.getEntityInformation(domainClass);
	}

	@Override
	protected QueryLookupStrategy getQueryLookupStrategy(Key key) {
		return new ElasticSearchQueryLookupStrategy();
	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		if (isQueryDslRepository(metadata.getRepositoryInterface())) {
			throw new IllegalArgumentException("QueryDsl Support has not been implemented yet.");
		}
		return SimpleElasticSearchRepository.class;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object getTargetRepository(RepositoryMetadata metadata) {
		SimpleElasticSearchRepository repository = new SimpleElasticSearchRepository(getEntityInformation(metadata.getDomainType()),
				elasticSearchOperations);
		repository.setEntityClass(metadata.getDomainType());
		return repository;
	}

}
