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
package org.springframework.data.es.repository.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.es.core.ElasticSearchOperations;
import org.springframework.data.es.core.query.Query;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.Assert;

/**
 * Base implementation of a ElasticSearch specific {@link RepositoryQuery}
 * 
 * @author Patryk Wasik
 */
public abstract class AbstractElasticSearchQuery implements RepositoryQuery {

	abstract class AbstractQueryExecution implements QueryExecution {

		protected Page<?> executeFind(Query query) {
			ElasticSearchEntityInformation<?, ?> metadata = elasticSearchQueryMethod.getEntityInformation();
			return elasticSearchOperations.findAll(query, metadata.getJavaType());
		}

	}

	class CollectionExecution extends AbstractQueryExecution {
		private final Pageable pageable;

		public CollectionExecution(Pageable pageable) {
			this.pageable = pageable;
		}

		@Override
		public Object execute(Query query) {
			query.setPageRequest(pageable != null ? pageable : new PageRequest(0, Math.max(1, (int) count(query))));
			return executeFind(query).getContent();
		}

		private long count(Query query) {
			return elasticSearchOperations.count(query, elasticSearchQueryMethod.getEntityInformation().getJavaType());
		}

	}

	class PagedExecution extends AbstractQueryExecution {
		private final Pageable pageable;

		public PagedExecution(Pageable pageable) {
			Assert.notNull(pageable);
			this.pageable = pageable;
		}

		@Override
		public Object execute(Query query) {
			query.setPageRequest(pageable);
			return executeFind(query);
		}
	}

	class SingleEntityExecution implements QueryExecution {

		@Override
		public Object execute(Query query) {
			ElasticSearchEntityInformation<?, ?> metadata = elasticSearchQueryMethod.getEntityInformation();
			return elasticSearchOperations.findOne(query, metadata.getJavaType());
		}
	}

	private interface QueryExecution {
		Object execute(Query query);
	}

	private final ElasticSearchOperations elasticSearchOperations;

	private final ElasticSearchQueryMethod elasticSearchQueryMethod;

	public AbstractElasticSearchQuery(ElasticSearchOperations elasticSearchOperations, ElasticSearchQueryMethod elasticSearchQueryMethod) {
		Assert.notNull(elasticSearchOperations);
		Assert.notNull(elasticSearchQueryMethod);
		this.elasticSearchOperations = elasticSearchOperations;
		this.elasticSearchQueryMethod = elasticSearchQueryMethod;
	}

	@Override
	public Object execute(Object[] parameters) {
		ElasticSearchParameterAccessor accessor = new ElasticSearchParametersParameterAccessor(elasticSearchQueryMethod, parameters);

		Query query = createQuery(accessor);

		if (elasticSearchQueryMethod.isPageQuery()) {
			return new PagedExecution(accessor.getPageable()).execute(query);
		} else if (elasticSearchQueryMethod.isCollectionQuery()) {
			return new CollectionExecution(accessor.getPageable()).execute(query);
		}

		return new SingleEntityExecution().execute(query);
	}

	@Override
	public ElasticSearchQueryMethod getQueryMethod() {
		return elasticSearchQueryMethod;
	}

	protected abstract Query createQuery(ElasticSearchParameterAccessor parameterAccessor);

}
