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
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.es.core.query.Criteria;
import org.springframework.data.es.core.query.FacetOptions;
import org.springframework.data.es.core.query.FacetQuery;
import org.springframework.data.es.core.query.Field;
import org.springframework.data.es.core.query.SimpleFacetQuery;
import org.springframework.data.es.core.query.result.FacetEntry;

/**
 * @author Patryk Wasik
 */
@RunWith(MockitoJUnitRunner.class)
public class ResultHelperTest {

	@Mock
	private Facets facets;

	@Test(expected = IllegalArgumentException.class)
	public void testConvertFacetQueryResponseForNullQuery() {
		ResultHelper.convertFacetQueryResponseToFacetPageMap(null, null);
	}

	@Test
	public void testConvertFacetQueryResponseForNullQueryResponse() {
		Map<Field, Page<FacetEntry>> result = ResultHelper.convertFacetQueryResponseToFacetPageMap(createFacetQuery("field_1"), null);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void testConvertFacetQueryResponseForQueryResultWithEmptyFacetFields() {
		Mockito.when(facets.facet(TermsFacet.class, "field_1")).thenReturn(null);
		Map<Field, Page<FacetEntry>> result = ResultHelper.convertFacetQueryResponseToFacetPageMap(createFacetQuery("field_1"), facets);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isEmpty());
	}

	@Test
	public void testConvertFacetQueryResponseForQueryResultWithSingeFacetField() {

		TermsFacet createFacetField = createFacetField("field_1", 1, 2);
		Mockito.when(facets.facet(TermsFacet.class, "field_1")).thenReturn(createFacetField);

		Map<Field, Page<FacetEntry>> result = ResultHelper.convertFacetQueryResponseToFacetPageMap(createFacetQuery("field_1"), facets);
		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Entry<Field, Page<FacetEntry>> resultEntry = result.entrySet().iterator().next();

		Assert.assertEquals("field_1", resultEntry.getKey().getName());
		Assert.assertEquals(2, resultEntry.getValue().getContent().size());
	}

	@Test
	public void testConvertFacetQueryResponseForQueryResultWithSingleFacetFieldWithoutValues() {

		TermsFacet createFacetField = createFacetField("field_1");
		Mockito.when(facets.facet(TermsFacet.class, "field_1")).thenReturn(createFacetField);

		Map<Field, Page<FacetEntry>> result = ResultHelper.convertFacetQueryResponseToFacetPageMap(createFacetQuery("field_1"), facets);
		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Entry<Field, Page<FacetEntry>> resultEntry = result.entrySet().iterator().next();

		Assert.assertEquals("field_1", resultEntry.getKey().getName());
		Assert.assertTrue(resultEntry.getValue().getContent().isEmpty());
	}

	@Test
	public void testConvertFacetQueryResponseForQueryWithoutFacetOptions() {
		Map<Field, Page<FacetEntry>> result = ResultHelper.convertFacetQueryResponseToFacetPageMap(new SimpleFacetQuery(new Criteria("field_1")),
				null);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isEmpty());
	}

	private TermsFacet createFacetField(String fieldName, long... values) {
		TermsFacet mock = Mockito.mock(TermsFacet.class);
		ArrayList<org.elasticsearch.search.facet.terms.TermsFacet.Entry> entries = new ArrayList<TermsFacet.Entry>();
		for (long l : values) {
			org.elasticsearch.search.facet.terms.TermsFacet.Entry entry = Mockito.mock(org.elasticsearch.search.facet.terms.TermsFacet.Entry.class);
			Mockito.when(entry.term()).thenReturn("value_" + l);
			Mockito.when(entry.count()).thenReturn((int) l);
			entries.add(entry);
		}
		Mockito.when(mock.iterator()).thenReturn(entries.iterator());
		return mock;
	}

	private FacetQuery createFacetQuery(String... facetFields) {
		FacetQuery fq = new SimpleFacetQuery(new Criteria(facetFields[0]));
		fq.setFacetOptions(new FacetOptions(facetFields));
		return fq;
	}

}
