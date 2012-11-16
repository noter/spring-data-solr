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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.elasticsearch.search.facet.terms.TermsFacet.ComparatorType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;

/**
 * Set of options that can be set on a {@link FacetQuery}
 * 
 * @author Christoph Strobl
 * @author Patryk Wasik
 */
public class FacetOptions {

	public static final int DEFAULT_FACET_LIMIT = 10;
	public static final int DEFAULT_FACET_MIN_COUNT = 1;
	public static final ComparatorType DEFAULT_FACET_SORT = ComparatorType.COUNT;

	private Integer facetLimit = DEFAULT_FACET_LIMIT;
	private final int facetMinCount = DEFAULT_FACET_MIN_COUNT;
	private final List<Field> facetOnFields = new ArrayList<Field>(1);
	private ComparatorType facetSort = DEFAULT_FACET_SORT;
	private Pageable pageable;

	public FacetOptions() {
	}

	/**
	 * Creates new instance faceting on given fields
	 * 
	 * @param fieldnames
	 */
	public FacetOptions(Field... fields) {
		Assert.notNull(fields, "Fields must not be null.");
		Assert.noNullElements(fields, "Cannot facet on null field.");

		for (Field field : fields) {
			addFacetOnField(field);
		}
	}

	/**
	 * Creates new instance faceting on fields with given name
	 * 
	 * @param fieldnames
	 */
	public FacetOptions(String... fieldnames) {
		Assert.notNull(fieldnames, "Fields must not be null.");
		Assert.noNullElements(fieldnames, "Cannot facet on null fieldname.");

		for (String fieldname : fieldnames) {
			addFacetOnField(fieldname);
		}
	}

	/**
	 * Append additional field for faceting
	 * 
	 * @param field
	 * @return
	 */
	public final FacetOptions addFacetOnField(Field field) {
		Assert.notNull(field, "Cannot facet on null field.");
		Assert.hasText(field.getName(), "Cannot facet on field with null/empty fieldname.");

		facetOnFields.add(field);
		return this;
	}

	/**
	 * Append additional field with given name for faceting
	 * 
	 * @param fieldname
	 * @return
	 */
	public final FacetOptions addFacetOnField(String fieldname) {
		addFacetOnField(new SimpleField(fieldname));
		return this;
	}

	/**
	 * Get the max number of results per facet field
	 * 
	 * @return
	 */
	public int getFacetLimit() {
		return facetLimit;
	}

	/**
	 * get the min number of hits a result has to have to get listed in result.
	 * Default is 1. Zero is not recommended.
	 * 
	 * @return
	 */
	public int getFacetMinCount() {
		return facetMinCount;
	}

	/**
	 * Get the list of Fields to facet on
	 * 
	 * @return
	 */
	public final List<Field> getFacetOnFields() {
		return Collections.unmodifiableList(facetOnFields);
	}

	/**
	 * Get sorting of facet results. Default is COUNT
	 * 
	 * @return
	 */
	public ComparatorType getFacetSort() {
		return facetSort;
	}

	public Pageable getPageable() {
		return pageable != null ? pageable : new PageRequest(0, facetLimit);
	}

	/**
	 * true if at least one facet field set
	 * 
	 * @return
	 */
	public boolean hasFields() {
		return !facetOnFields.isEmpty();
	}

	/**
	 * Set limit on nr results returned
	 * 
	 * @param rowsToReturn
	 *            Default is 10
	 * @return
	 */
	public FacetOptions setFacetLimit(int rowsToReturn) {
		facetLimit = java.lang.Math.max(1, rowsToReturn);
		return this;
	}

	/**
	 * Set minimum number of hits for result to be included in response
	 * 
	 * @param minCount
	 *            Default is 1
	 * @return
	 */
	public FacetOptions setFacetMinCount(int minCount) {
		throw new UnsupportedOperationException();
		/*
		 * facetMinCount = java.lang.Math.max(0, minCount); return this;
		 */
	}

	/**
	 * Set sorting (INDEX or COUNT)
	 * 
	 * @param facetSort
	 *            Default is COUNT
	 * @return
	 */
	public FacetOptions setFacetSort(ComparatorType facetSort) {
		Assert.notNull(facetSort, "FacetSort must not be null.");

		this.facetSort = facetSort;
		return this;
	}

	public FacetOptions setPageable(Pageable pageable) {
		throw new UnsupportedOperationException();
		/*
		 * this.pageable = pageable; return this;
		 */
	}
}
