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

import java.util.Collection;
import java.util.List;

import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.es.core.convert.ElasticSearchConverter;
import org.springframework.data.es.core.query.ESDataQuery;
import org.springframework.data.es.core.query.FacetQuery;
import org.springframework.data.es.core.query.Query;
import org.springframework.data.es.core.query.result.FacetPage;

/**
 * Interface that specifies a basic set of ElasticSearch operations.
 * 
 * @author Patryk Wasik
 */
public interface ElasticSearchOperations {

	/**
	 * Convert given bean into a ES source
	 * 
	 * @param bean
	 * @return
	 */
	String convertBeanToESJsonSource(Object bean);

	<T extends Object> T convertESJsonToBean(String source, Class<T> clazz);

	/**
	 * Execute add operation against es
	 * 
	 * @param obj
	 * @return
	 */
	IndexResponse executeAddBean(Object obj);

	/**
	 * Add a collection of beans to es
	 * 
	 * @param beans
	 * @return
	 */
	BulkResponse executeAddBeans(Collection<?> beans);

	/**
	 * return number of elements found by for given query
	 * 
	 * @param query
	 * @return
	 */
	long executeCount(ESDataQuery query);

	/**
	 * Find and delete all objects matching the provided Query
	 * 
	 * @param query
	 * @return
	 */
	DeleteByQueryResponse executeDelete(ESDataQuery query);

	/**
	 * Delete objects with given ids
	 * 
	 * @param id
	 * @return
	 */
	List<DeleteResponse> executeDeleteById(Collection<String> id);

	/**
	 * Detele the one object with provided id
	 * 
	 * @param id
	 * @return
	 */
	DeleteResponse executeDeleteById(String id);

	/**
	 * Execute a facet query against solr facet result will be returned along
	 * with query result within the FacetPage
	 * 
	 * @param query
	 * @param clazz
	 * @return
	 */
	<T> FacetPage<T> executeFacetQuery(FacetQuery query, Class<T> clazz);

	/**
	 * Execute the query against solr and retrun result as {@link Page}
	 * 
	 * @param query
	 * @param clazz
	 * @return
	 */
	<T> Page<T> executeListQuery(Query query, Class<T> clazz);

	/**
	 * Execute the query against solr and return the first returned object
	 * 
	 * @param query
	 * @param clazz
	 * @return the first matching object
	 */
	<T> T executeObjectQuery(Query query, Class<T> clazz);

	/**
	 * Execute query against Solr
	 * 
	 * @param query
	 * @return
	 */
	SearchResponse executeQuery(ESDataQuery query);

	/**
	 * @return Converter in use
	 */
	ElasticSearchConverter getConverter();

	/**
	 * Get the underlying ElasticSearch Client instance
	 * 
	 * @return
	 */
	Client getElasticSearchClient();

}
