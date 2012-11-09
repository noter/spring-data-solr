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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.facet.terms.TermsFacet.ComparatorType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.es.AbstractITestWithEmbeddedElasticSearch;
import org.springframework.data.es.core.query.Criteria;
import org.springframework.data.es.core.query.FacetOptions;
import org.springframework.data.es.core.query.FacetQuery;
import org.springframework.data.es.core.query.Query;
import org.springframework.data.es.core.query.SimpleFacetQuery;
import org.springframework.data.es.core.query.SimpleField;
import org.springframework.data.es.core.query.SimpleFilterQuery;
import org.springframework.data.es.core.query.SimpleQuery;
import org.springframework.data.es.core.query.SimpleStringCriteria;

/**
 * @author Patryk Wasik
 */
public class QueryParserTest extends AbstractITestWithEmbeddedElasticSearch {

	private QueryParser queryParser;

	@Before
	public void setUp() {
		queryParser = new QueryParser();
	}

	@Test
	public void testConstructESSearchQueryWithFacetSort() {
		FacetQuery query = new SimpleFacetQuery(new Criteria("field_1").is("value_1")).setFacetOptions(new FacetOptions("facet_1")
				.setFacetSort(ComparatorType.COUNT));
		SearchRequestBuilder searchRequestBuilder = queryParser.constructESSearchQuery(query, client);

		assertThat(toSingleLineString(searchRequestBuilder),
				is("{query:{bool:{must:{bool:{must:{field:{field_1:value_1}}}}}},facets:{facet_1:{terms:{field:facet_1,size:10,order:count}}}}"));

		query.getFacetOptions().setFacetSort(ComparatorType.TERM);
		query.getFacetOptions().setFacetLimit(5);
		searchRequestBuilder = queryParser.constructESSearchQuery(query, client);

		assertThat(toSingleLineString(searchRequestBuilder),
				is("{query:{bool:{must:{bool:{must:{field:{field_1:value_1}}}}}},facets:{facet_1:{terms:{field:facet_1,size:5,order:term}}}}"));

	}

	@Test
	public void testConstructESSearchQueryWithMultipleFacet() {
		FacetQuery query = new SimpleFacetQuery(new Criteria("field_1").is("value_1")).setFacetOptions(new FacetOptions("facet_1", "facet_2"));
		SearchRequestBuilder searchRequestBuilder = queryParser.constructESSearchQuery(query, client);
		assertThat(
				toSingleLineString(searchRequestBuilder),
				is("{query:{bool:{must:{bool:{must:{field:{field_1:value_1}}}}}},facets:{facet_1:{terms:{field:facet_1,size:10,order:count}},facet_2:{terms:{field:facet_2,size:10,order:count}}}}"));
	}

	@Test
	public void testConstructESSearchQueryWithPagination() {
		int page = 1;
		int pageSize = 100;
		Query query = new SimpleQuery(new Criteria("field_1").is("value_1")).setPageRequest(new PageRequest(page, pageSize));
		SearchRequestBuilder searchRequestBuilder = queryParser.constructESSearchQuery(query, client);
		assertThat(toSingleLineString(searchRequestBuilder), is("{from:100,size:100,query:{bool:{must:{bool:{must:{field:{field_1:value_1}}}}}}}"));
	}

	@Test
	public void testConstructESSearchQueryWithSingleFacet() {
		Query query = new SimpleFacetQuery(new Criteria("field_1").is("value_1")).setFacetOptions(new FacetOptions("facet_1"));
		SearchRequestBuilder searchRequestBuilder = queryParser.constructESSearchQuery(query, client);
		assertThat(toSingleLineString(searchRequestBuilder),
				is("{query:{bool:{must:{bool:{must:{field:{field_1:value_1}}}}}},facets:{facet_1:{terms:{field:facet_1,size:10,order:count}}}}"));
	}

	@Test
	public void testConstructSimpleElasticSearchQuery() {
		Query query = new SimpleQuery(new Criteria("field_1").is("value_1"));
		SearchRequestBuilder searchRequestBuilder = queryParser.constructESSearchQuery(query, client);
		assertThat(toSingleLineString(searchRequestBuilder), is("{query:{bool:{must:{bool:{must:{field:{field_1:value_1}}}}}}}"));
	}

	@Test
	public void testConstructSimpleElasticSearchQueryWithProjection() {
		Query query = new SimpleQuery(new Criteria("field_1").is("value_1")).addProjectionOnField("projection_1").addProjectionOnField(
				new SimpleField("projection_2"));
		SearchRequestBuilder searchRequestBuilder = queryParser.constructESSearchQuery(query, client);
		assertThat(
				toSingleLineString(searchRequestBuilder),
				is("{query:{bool:{must:{bool:{must:{field:{field_1:value_1}}}}}},facets:{projection_1:{terms:{field:projection_1,size:10}},projection_2:{terms:{field:projection_2,size:10}}}}"));

	}

	@Test
	public void testWithEmptyFilterQuery() {
		Query query = new SimpleQuery(new Criteria("field_1").is("value_1")).addFilterQuery(new SimpleQuery());
		SearchRequestBuilder searchRequestBuilder = queryParser.constructESSearchQuery(query, client);

		assertThat(toSingleLineString(searchRequestBuilder), is("{query:{bool:{must:{bool:{must:{field:{field_1:value_1}}}}}}}"));
	}

	@Test
	public void testWithFilterQuery() {
		Query query = new SimpleQuery(new Criteria("field_1").is("value_1")).addFilterQuery(new SimpleFilterQuery(new Criteria("filter_field")
				.is("filter_value")));
		SearchRequestBuilder searchRequestBuilder = queryParser.constructESSearchQuery(query, client);

		assertThat(
				toSingleLineString(searchRequestBuilder),
				is("{query:{bool:{must:{bool:{must:{field:{field_1:value_1}}}}}},filter:{bool:{must:{and:{filters:[{query:{field:{filter_field:filter_value}}}]}}}}}"));
	}

	@Test
	public void testWithNullSort() {
		SimpleStringCriteria criteria = new SimpleStringCriteria("field_1:value_1");
		Query query = new SimpleQuery(criteria);
		query.addSort(null); // do this explicitly

		SearchRequestBuilder searchRequestBuilder = queryParser.constructESSearchQuery(query, client);
		assertThat(toSingleLineString(searchRequestBuilder), is("{query:{query_string:{query:field_1:value_1}}}"));
	}

	@Test
	public void testWithSimpleStringCriteria() {
		SimpleStringCriteria criteria = new SimpleStringCriteria("field_1:value_1");
		Query query = new SimpleQuery(criteria);
		SearchRequestBuilder searchRequestBuilder = queryParser.constructESSearchQuery(query, client);
		assertThat(toSingleLineString(searchRequestBuilder), is("{query:{query_string:{query:field_1:value_1}}}"));
	}

	@Test
	public void testWithSortAscMultipleFields() {
		SimpleStringCriteria criteria = new SimpleStringCriteria("field_1:value_1");
		Query query = new SimpleQuery(criteria);
		query.addSort(new Sort("field_2", "field_3"));
		SearchRequestBuilder searchRequestBuilder = queryParser.constructESSearchQuery(query, client);
		assertThat(toSingleLineString(searchRequestBuilder),
				is("{query:{query_string:{query:field_1:value_1}},sort:[{field_2:{order:asc}},{field_3:{order:asc}}]}"));
	}

	@Test
	public void testWithSortAscOnSingleField() {
		SimpleStringCriteria criteria = new SimpleStringCriteria("field_1:value_1");
		Query query = new SimpleQuery(criteria);
		query.addSort(new Sort("field_2"));
		SearchRequestBuilder searchRequestBuilder = queryParser.constructESSearchQuery(query, client);
		assertThat(toSingleLineString(searchRequestBuilder), is("{query:{query_string:{query:field_1:value_1}},sort:[{field_2:{order:asc}}]}"));
	}

	@Test
	public void testWithSortDescMultipleFields() {
		SimpleStringCriteria criteria = new SimpleStringCriteria("field_1:value_1");
		Query query = new SimpleQuery(criteria);
		query.addSort(new Sort(Sort.Direction.DESC, "field_2", "field_3"));
		SearchRequestBuilder searchRequestBuilder = queryParser.constructESSearchQuery(query, client);
		assertThat(toSingleLineString(searchRequestBuilder),
				is("{query:{query_string:{query:field_1:value_1}},sort:[{field_2:{order:desc}},{field_3:{order:desc}}]}"));

	}

	@Test
	public void testWithSortDescOnSingleField() {
		SimpleStringCriteria criteria = new SimpleStringCriteria("field_1:value_1");
		Query query = new SimpleQuery(criteria);
		query.addSort(new Sort(Sort.Direction.DESC, "field_2"));
		SearchRequestBuilder searchRequestBuilder = queryParser.constructESSearchQuery(query, client);
		assertThat(toSingleLineString(searchRequestBuilder), is("{query:{query_string:{query:field_1:value_1}},sort:[{field_2:{order:desc}}]}"));
	}

	@Test
	public void testWithSortMixedDirections() {
		SimpleStringCriteria criteria = new SimpleStringCriteria("field_1:value_1");
		Query query = new SimpleQuery(criteria);
		query.addSort(new Sort("field_1"));
		query.addSort(new Sort(Sort.Direction.DESC, "field_2", "field_3"));
		SearchRequestBuilder searchRequestBuilder = queryParser.constructESSearchQuery(query, client);
		assertThat(toSingleLineString(searchRequestBuilder),
				is("{query:{query_string:{query:field_1:value_1}},sort:[{field_1:{order:asc}},{field_2:{order:desc}},{field_3:{order:desc}}]}"));
	}

	private String toSingleLineString(SearchRequestBuilder searchRequestBuilder) {
		return searchRequestBuilder.toString().replaceAll("\n", "").replaceAll("\\s*", "").replaceAll("\"", "").trim();
	}
}
