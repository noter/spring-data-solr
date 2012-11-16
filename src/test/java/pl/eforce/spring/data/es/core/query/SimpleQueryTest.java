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

import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import pl.eforce.spring.data.es.core.query.Criteria;
import pl.eforce.spring.data.es.core.query.FacetOptions;
import pl.eforce.spring.data.es.core.query.FacetQuery;
import pl.eforce.spring.data.es.core.query.Field;
import pl.eforce.spring.data.es.core.query.Query;
import pl.eforce.spring.data.es.core.query.SimpleFacetQuery;
import pl.eforce.spring.data.es.core.query.SimpleField;
import pl.eforce.spring.data.es.core.query.SimpleQuery;
import pl.eforce.spring.data.es.core.query.SimpleStringCriteria;

/**
 * @author Patryk Wasik
 */
public class SimpleQueryTest {

	@Test(expected = IllegalArgumentException.class)
	public void testAddCriteriaWithEmptyFieldname() {
		new SimpleQuery().addCriteria(new Criteria(new SimpleField("")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddCriteriaWithNullField() {
		new SimpleQuery().addCriteria(new Criteria());
	}

	@Test
	public void testAddFacetOptions() {
		FacetOptions facetOptions = new FacetOptions("field_1", "field_2");
		FacetQuery query = new SimpleFacetQuery().setFacetOptions(facetOptions);
		Assert.assertEquals(facetOptions, query.getFacetOptions());
	}

	@Test
	public void testAddFacetOptionsWithNullValue() {
		FacetQuery query = new SimpleFacetQuery().setFacetOptions(null);
		Assert.assertNull(query.getFacetOptions());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddFacetOptionsWithoutFacetFields() {
		new SimpleFacetQuery().setFacetOptions(new FacetOptions());
	}

	@SuppressWarnings("rawtypes")
	@Test(expected = UnsupportedOperationException.class)
	public void testAddGroupBy() {
		Query query = new SimpleQuery().addGroupByField(new SimpleField("field_1")).addGroupByField(new SimpleField("field_2"));
		Assert.assertEquals(2, ((List) query.getGroupByFields()).size());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testAddGroupByNullField() {
		new SimpleQuery().addGroupByField((Field) null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testAddGroupByNullFieldName() {
		new SimpleQuery().addGroupByField(new SimpleField(StringUtils.EMPTY));
	}

	@Test
	public void testAddMultipleSort() {
		Sort sort1 = new Sort("field_2", "field_3");
		Sort sort2 = new Sort("field_1");
		Query query = new SimpleQuery(new Criteria("field_1").is("value_1"));
		query.addSort(sort1);
		query.addSort(sort2);

		Assert.assertNotNull(query.getSort());
		Assert.assertNotNull(query.getSort().getOrderFor("field_1"));
		Assert.assertNotNull(query.getSort().getOrderFor("field_2"));
		Assert.assertNotNull(query.getSort().getOrderFor("field_3"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddNullCriteria() {
		new SimpleQuery().addCriteria(null);
	}

	@Test
	public void testAddNullSort() {
		Query query = new SimpleQuery(new Criteria("field_1").is("value_1"));
		query.addSort(null);

		Assert.assertNull(query.getSort());
	}

	@Test
	public void testAddNullToExistingSort() {
		Sort sort = new Sort("field_2", "field_3");
		Query query = new SimpleQuery(new Criteria("field_1").is("value_1"));
		query.addSort(sort);
		query.addSort(null);

		Assert.assertNotNull(query.getSort());
		Assert.assertEquals(sort, query.getSort());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testAddProjection() {
		Query query = new SimpleQuery().addProjectionOnField(new SimpleField("field_1")).addProjectionOnField(new SimpleField("field_2"));
		Assert.assertEquals(2, ((List) query.getProjectionOnFields()).size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddProjectionNullField() {
		new SimpleQuery().addProjectionOnField((Field) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddProjectionNullFieldName() {
		new SimpleQuery().addProjectionOnField(new SimpleField(StringUtils.EMPTY));
	}

	@Test
	public void testAddSort() {
		Sort sort = new Sort("field_2", "field_3");
		Query query = new SimpleQuery(new Criteria("field_1").is("value_1"));
		query.addSort(sort);

		Assert.assertNotNull(query.getSort());
	}

	@Test
	public void testCloneNullQuery() {
		Assert.assertNull(SimpleQuery.fromQuery(null));
	}

	@Test
	public void testCloneQuery() {
		Query query = new SimpleQuery();
		Assert.assertNotSame(query, SimpleQuery.fromQuery(query));
	}

	@Test
	public void testCloneQueryWithCriteria() {
		Query source = new SimpleQuery(new Criteria("field_1").is("value_1"));
		Query destination = SimpleQuery.fromQuery(source);
		Assert.assertNotSame(source, destination);
		Assert.assertEquals("field_1", destination.getCriteria().getField().getName());
		with(destination.getCriteria().getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.field.field_1", is("value_1"));
	}

	@Test
	public void testCloneQueryWithFilterQuery() {
		Query source = new SimpleQuery(new Criteria("field_1").is("value_1"));
		source.addFilterQuery(new SimpleQuery(new Criteria("field_2").startsWith("value_2")));

		Query destination = SimpleQuery.fromQuery(source);
		with(destination.getCriteria().getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.field.field_1", is("value_1"));
		Assert.assertEquals(1, destination.getFilterQueries().size());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testCloneQueryWithGroupBy() {
		Query source = new SimpleQuery(new Criteria("field_1").is("value_1"));
		source.addGroupByField(new SimpleField("field_2"));

		Query destination = SimpleQuery.fromQuery(source);
		Assert.assertEquals(1, destination.getGroupByFields().size());
	}

	@Test
	public void testCloneQueryWithProjection() {
		Query source = new SimpleQuery(new Criteria("field_1").is("value_1"));
		source.addProjectionOnField(new SimpleField("field_2"));

		Query destination = SimpleQuery.fromQuery(source);
		Assert.assertEquals(1, destination.getProjectionOnFields().size());
	}

	@Test
	public void testCloneQueryWithSort() {
		Query source = new SimpleQuery(new Criteria("field_1").is("value_1"));
		source.addSort(new Sort(Sort.Direction.DESC, "field_3"));

		Query destination = SimpleQuery.fromQuery(source);
		Assert.assertEquals(source.getSort(), destination.getSort());
	}

	@Test
	public void testCreateQueryWithSortedPageRequest() {
		SimpleQuery query = new SimpleQuery(new SimpleStringCriteria("*:*"), new PageRequest(0, 20, Sort.Direction.DESC, "value_1", "value_2"));
		Assert.assertNotNull(query.getPageRequest());
		Assert.assertNotNull(query.getSort());

		int i = 0;
		for (Order order : query.getSort()) {
			Assert.assertEquals(Sort.Direction.DESC, order.getDirection());
			Assert.assertEquals("value_" + (++i), order.getProperty());
		}

	}

	@Test
	public void testSetPageRequest() {
		SimpleQuery query = new SimpleQuery();
		Assert.assertEquals(SimpleQuery.DEFAULT_PAGE, query.getPageRequest());

		Pageable alteredPage = new PageRequest(0, 20);

		query.setPageRequest(alteredPage);
		Assert.assertEquals(alteredPage, query.getPageRequest());
		Assert.assertNull(query.getSort());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetPageRequestWithNullValue() {
		new SimpleQuery().setPageRequest(null);
	}

	@Test
	public void testSetPageRequestWithSort() {
		SimpleQuery query = new SimpleQuery();
		Assert.assertEquals(SimpleQuery.DEFAULT_PAGE, query.getPageRequest());

		Pageable alteredPage = new PageRequest(0, 20, Sort.Direction.DESC, "value_1", "value_2");

		query.setPageRequest(alteredPage);
		Assert.assertEquals(alteredPage, query.getPageRequest());
		Assert.assertNotNull(query.getSort());

		int i = 0;
		for (Order order : query.getSort()) {
			Assert.assertEquals(Sort.Direction.DESC, order.getDirection());
			Assert.assertEquals("value_" + (++i), order.getProperty());
		}
	}

}