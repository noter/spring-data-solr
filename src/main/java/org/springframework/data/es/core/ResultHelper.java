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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.facet.terms.TermsFacet.Entry;
import org.springframework.data.domain.Page;
import org.springframework.data.es.core.query.FacetQuery;
import org.springframework.data.es.core.query.Field;
import org.springframework.data.es.core.query.result.FacetEntry;
import org.springframework.data.es.core.query.result.FacetPage;
import org.springframework.data.es.core.query.result.SimpleFacetEntry;
import org.springframework.util.Assert;

/**
 * Use Result Helper to extract various parameters from the {@link Facets} and
 * convert it into a proper Format taking care of non existent and null elements
 * with the response.
 * 
 * @author Patryk Wasik
 */
final class ResultHelper {

	static Map<Field, Page<FacetEntry>> convertFacetQueryResponseToFacetPageMap(FacetQuery query, Facets facets) {
		Assert.notNull(query, "Cannot convert response for 'null', query");

		if (!query.hasFacetOptions() || (facets == null)) {
			return Collections.emptyMap();
		}
		Map<Field, Page<FacetEntry>> facetResult = new HashMap<Field, Page<FacetEntry>>();
		for (Field field : query.getFacetOptions().getFacetOnFields()) {
			TermsFacet termsFacet = facets.facet(TermsFacet.class, field.getName());
			if (termsFacet != null) {
				List<FacetEntry> facetEntries = new ArrayList<FacetEntry>();
				for (Entry entry : termsFacet) {
					facetEntries.add(new SimpleFacetEntry(field, entry.term(), entry.count()));
				}
				facetResult.put(field, new FacetPage<FacetEntry>(facetEntries));
			} else {
				facetResult.put(field, new FacetPage<FacetEntry>(new ArrayList<FacetEntry>()));
			}
		}
		return facetResult;
	}

	private ResultHelper() {
	}

}
