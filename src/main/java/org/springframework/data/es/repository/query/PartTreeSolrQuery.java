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

import org.springframework.data.es.core.SolrOperations;
import org.springframework.data.es.core.mapping.SolrPersistentProperty;
import org.springframework.data.es.core.query.Query;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.parser.PartTree;

/**
 * Solr specific implementation of a derived query.
 * 
 * @author Christoph Strobl
 */
public class PartTreeSolrQuery extends AbstractSolrQuery {

	private final PartTree tree;
	private final MappingContext<?, SolrPersistentProperty> mappingContext;

	public PartTreeSolrQuery(SolrQueryMethod method, SolrOperations solrOperations) {
		super(solrOperations, method);
		this.tree = new PartTree(method.getName(), method.getEntityInformation().getJavaType());
		this.mappingContext = solrOperations.getConverter().getMappingContext();
	}

	public PartTree getTree() {
		return tree;
	}

	@Override
	protected Query createQuery(SolrParameterAccessor parameterAccessor) {
		return new SolrQueryCreator(tree, parameterAccessor, mappingContext).createQuery();
	}

}
