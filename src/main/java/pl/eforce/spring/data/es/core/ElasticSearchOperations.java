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
package pl.eforce.spring.data.es.core;

import java.util.Collection;
import java.util.List;

import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.springframework.data.domain.Page;

import pl.eforce.spring.data.es.core.convert.ElasticSearchConverter;
import pl.eforce.spring.data.es.core.query.ElasticSearchDataQuery;
import pl.eforce.spring.data.es.core.query.FacetQuery;
import pl.eforce.spring.data.es.core.query.Query;
import pl.eforce.spring.data.es.core.query.result.FacetPage;

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

	/**
	 * Convert given JSON source to a bean of type clazz
	 * 
	 * @param source
	 * @param clazz
	 * @return
	 */
	<T extends Object> T convertESJsonSourceToBean(String source, Class<T> clazz);

	/**
	 * return number of elements found by for given query for given type
	 * 
	 * @param query
	 * @return
	 */
	long count(ElasticSearchDataQuery query, Class<?>... types);

	/**
	 * return number of elements found by for given query in given inidcates
	 * 
	 * @param query
	 * @return
	 */
	long count(ElasticSearchDataQuery query, String... indices);

	/**
	 * return number of elements found by for given query in given inidcates and types
	 * 
	 * @param query
	 * @return
	 */
	long count(ElasticSearchDataQuery query, String[] indices, Class<?>... types);

	/**
	 * Delete objects
	 * 
	 * @param entities
	 * @return
	 */
	List<DeleteResponse> delete(Collection<?> objects);

	/**
	 * Find and delete all objects matching the provided Query and types
	 * 
	 * @param query
	 * @return
	 */
	DeleteByQueryResponse delete(ElasticSearchDataQuery query, Class<?>... types);

	/**
	 * Find and delete all objects matching the provided Query in given indices
	 * 
	 * @param query
	 * @return
	 */

	DeleteByQueryResponse delete(ElasticSearchDataQuery query, String... indices);

	/**
	 * Find and delete all objects matching the provided Query and types in given indices
	 * 
	 * @param query
	 * @return
	 */
	DeleteByQueryResponse delete(ElasticSearchDataQuery query, String[] indices, Class<?>... types);

	/**
	 * Detele the one object
	 * 
	 * @param entity
	 * @return
	 */
	DeleteResponse delete(Object object);

	/**
	 * Detele object by id
	 * 
	 * @param id
	 * @return
	 */
	DeleteResponse deleteById(String id, Class<?> type);

	/**
	 * Execute a facet query against ElasticSearch facet result will be returned along with query result within the FacetPage
	 * 
	 * @param query
	 * @param clazz
	 * @return
	 */
	<T> FacetPage<T> findAll(FacetQuery query, Class<T> clazz);

	/**
	 * Execute the query against ElasticSarcg and retrun result as {@link Page}
	 * 
	 * @param query
	 * @param clazz
	 * @return
	 */
	<T> Page<T> findAll(Query query, Class<T> clazz);

	/**
	 * Get bean by id
	 * 
	 * @param id
	 * @param clazz
	 * @return
	 */
	<T> T findById(String id, Class<T> clazz);

	/**
	 * Execute the query against ElasticSearch and return the first returned object
	 * 
	 * @param query
	 * @param clazz
	 * @return the first matching object
	 */
	<T> T findOne(Query query, Class<T> clazz);

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

	/**
	 * Execute query against Solr
	 * 
	 * @param query
	 * @return
	 */
	SearchResponse query(ElasticSearchDataQuery query);

	/**
	 * Refresh indexes for given types to perform near realtime operation
	 * 
	 * @param types
	 */
	void refresh(Class<?>... types);

	/**
	 * Refresh indexes to perform near realtime operation
	 * 
	 * @param indices
	 */
	void refresh(String... indices);

	/**
	 * Add a collection of beans to es
	 * 
	 * @param beans
	 * @return
	 */
	BulkResponse save(Collection<?> beans);

	/**
	 * Execute add operation against es
	 * 
	 * @param obj
	 * @return
	 */
	IndexResponse save(Object obj);

}
