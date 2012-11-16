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
package pl.eforce.spring.data.es.repository.query;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.parser.PartTree;

import pl.eforce.spring.data.es.core.ElasticSearchOperations;
import pl.eforce.spring.data.es.core.mapping.ElasticSearchPersistentProperty;
import pl.eforce.spring.data.es.core.query.Query;

/**
 * ElasticSearch specific implementation of a derived query.
 * 
 * @author Patryk Wasik
 */
public class PartTreeElasticSearchQuery extends AbstractElasticSearchQuery {

	private final MappingContext<?, ElasticSearchPersistentProperty> mappingContext;
	private final PartTree tree;

	public PartTreeElasticSearchQuery(ElasticSearchQueryMethod method, ElasticSearchOperations solrOperations) {
		super(solrOperations, method);
		tree = new PartTree(method.getName(), method.getEntityInformation().getJavaType());
		mappingContext = solrOperations.getConverter().getMappingContext();
	}

	public PartTree getTree() {
		return tree;
	}

	@Override
	protected Query createQuery(ElasticSearchParameterAccessor parameterAccessor) {
		return new ElasticSearchQueryCreator(tree, parameterAccessor, mappingContext).createQuery();
	}

}