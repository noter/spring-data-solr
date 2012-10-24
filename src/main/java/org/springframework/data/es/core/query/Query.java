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
package org.springframework.data.es.core.query;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * A Query that can be translated into a es understandable Query.
 * 
 * @author Paryk Wasik
 */
public interface Query extends ESDataQuery {

	int DEFAULT_PAGE_SIZE = 10;

	/**
	 * add query to filter results Corresponds to 'fq' in es
	 * 
	 * @param query
	 * @return
	 */
	<T extends Query> T addFilterQuery(FilterQuery query);

	/**
	 * add the given field to those used for grouping result Corresponds to ''
	 * in es
	 * 
	 * @param field
	 * @return
	 */
	<T extends Query> T addGroupByField(Field field);

	/**
	 * add given Field to those included in result. Corresponds to the 'fl'
	 * parameter in es.
	 * 
	 * @param field
	 * @return
	 */
	<T extends Query> T addProjectionOnField(Field field);

	/**
	 * Add {@link Sort} to query
	 * 
	 * @param sort
	 * @return
	 */
	<T extends Query> T addSort(Sort sort);

	/**
	 * Get filter queries if defined
	 * 
	 * @return
	 */
	List<FilterQuery> getFilterQueries();

	/**
	 * Get group by fields if defined
	 * 
	 * @return
	 */
	List<Field> getGroupByFields();

	/**
	 * Get page settings if defined
	 * 
	 * @return
	 */
	Pageable getPageRequest();

	/**
	 * Get projection fields if defined
	 * 
	 * @return
	 */
	List<Field> getProjectionOnFields();

	/**
	 * @return null if not set
	 */
	Sort getSort();

	/**
	 * restrict result to entries on given page. Corresponds to the 'start' and
	 * 'rows' parameter in es
	 * 
	 * @param pageable
	 * @return
	 */
	<T extends Query> T setPageRequest(Pageable pageable);

}
