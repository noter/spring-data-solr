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
package pl.eforce.spring.data.es.core.query;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryString;

import org.elasticsearch.index.query.BoolQueryBuilder;

/**
 * The most basic criteria holding an already formatted query string that can be executed 'as is' against the ElasticSearch client
 * 
 * @author Patryk Wasik
 */
public class SimpleStringCriteria extends Criteria {

	private final String queryString;

	public SimpleStringCriteria(String queryString) {
		this.queryString = queryString;
		getCriteriaChain().put(this, ConjunctionOperator.FIRST);
	}

	@Override
	protected BoolQueryBuilder constructFieldQuery() {
		BoolQueryBuilder boolQueryBuilder = boolQuery();
		return boolQueryBuilder.must(queryString(queryString));
	}

}
