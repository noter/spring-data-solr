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

/**
 * @author Patryk Wasik
 */
public interface FacetQuery extends Query {

	/**
	 * @return
	 */
	FacetOptions getFacetOptions();

	/**
	 * @return true if options set
	 */
	boolean hasFacetOptions();

	/**
	 * Faceting options to apply when exectuing query
	 * 
	 * @param facetOptions
	 * @return
	 */
	<T extends ElasticSearchDataQuery> T setFacetOptions(FacetOptions facetOptions);

}
