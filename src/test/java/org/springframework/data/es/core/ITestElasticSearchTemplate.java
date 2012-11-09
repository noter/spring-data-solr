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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.es.AbstractITestWithEmbeddedElasticSearch;
import org.springframework.data.es.ExampleElasticSearchBean;
import org.springframework.data.es.core.query.FacetOptions;
import org.springframework.data.es.core.query.FacetQuery;
import org.springframework.data.es.core.query.MatchAllCriteria;
import org.springframework.data.es.core.query.Query;
import org.springframework.data.es.core.query.SimpleFacetQuery;
import org.springframework.data.es.core.query.SimpleField;
import org.springframework.data.es.core.query.SimpleQuery;
import org.springframework.data.es.core.query.result.FacetEntry;
import org.springframework.data.es.core.query.result.FacetPage;
import org.xml.sax.SAXException;

/**
 * @author Patryk Wasik
 */
public class ITestElasticSearchTemplate extends AbstractITestWithEmbeddedElasticSearch {

	private ElasticSearchTemplate elasticSearchTemplate;

	@Before
	public void setUp() throws IOException, ParserConfigurationException, SAXException {
		elasticSearchTemplate = new ElasticSearchTemplate(client, null);
	}

	@After
	public void tearDown() {
		elasticSearchTemplate.delete(new SimpleQuery(new MatchAllCriteria()), ExampleElasticSearchBean.class);
	}

	@Test
	public void testBeanLifecycle() {
		ExampleElasticSearchBean toInsert = createDefaultExampleBean();

		elasticSearchTemplate.save(toInsert);
		ExampleElasticSearchBean recalled = elasticSearchTemplate.findById(toInsert.getId(), ExampleElasticSearchBean.class);

		assertNotNull(recalled);
		assertEquals(toInsert.getId(), recalled.getId());

		elasticSearchTemplate.deleteById(toInsert.getId(), ExampleElasticSearchBean.class);
		recalled = elasticSearchTemplate.findById(toInsert.getId(), ExampleElasticSearchBean.class);
		assertNull(recalled);
	}

	@Test
	public void testFacetQuery() {
		List<ExampleElasticSearchBean> values = new ArrayList<ExampleElasticSearchBean>();
		for (int i = 0; i < 10; i++) {
			values.add(createExampleBeanWithId(Integer.toString(i)));
		}
		elasticSearchTemplate.save(values);
		elasticSearchTemplate.refresh(ExampleElasticSearchBean.class);

		FacetQuery q = new SimpleFacetQuery(new MatchAllCriteria()).setFacetOptions(new FacetOptions().addFacetOnField("name").addFacetOnField("id")
				.setFacetLimit(5));

		FacetPage<ExampleElasticSearchBean> page = elasticSearchTemplate.findAll(q, ExampleElasticSearchBean.class);

		for (Page<FacetEntry> facetResultPage : page.getFacetResultPages()) {
			Assert.assertEquals(5, facetResultPage.getNumberOfElements());
		}

		Page<FacetEntry> facetPage = page.getFacetResultPage(new SimpleField("name"));
		for (FacetEntry entry : facetPage) {
			Assert.assertNotNull(entry.getValue());
			Assert.assertEquals("name", entry.getField().getName());
			Assert.assertEquals(1l, entry.getValueCount());
		}

		facetPage = page.getFacetResultPage(new SimpleField("id"));
		for (FacetEntry entry : facetPage) {
			Assert.assertNotNull(entry.getValue());
			Assert.assertEquals("id", entry.getField().getName());
			Assert.assertEquals(1l, entry.getValueCount());
		}
	}

	@Test
	public void testQueryWithMultiSort() {
		List<ExampleElasticSearchBean> values = new ArrayList<ExampleElasticSearchBean>();
		for (int i = 0; i < 10; i++) {
			ExampleElasticSearchBean bean = createExampleBeanWithId(Integer.toString(i));
			bean.setInStock((i % 2) == 0);
			values.add(bean);
		}
		elasticSearchTemplate.save(values);
		elasticSearchTemplate.refresh(ExampleElasticSearchBean.class);

		Query query = new SimpleQuery(new MatchAllCriteria()).addSort(new Sort(Sort.Direction.DESC, "inStock")).addSort(
				new Sort(Sort.Direction.ASC, "name"));
		Page<ExampleElasticSearchBean> page = elasticSearchTemplate.findAll(query, ExampleElasticSearchBean.class);

		ExampleElasticSearchBean prev = page.getContent().get(0);
		for (int i = 1; i < 5; i++) {
			ExampleElasticSearchBean cur = page.getContent().get(i);
			Assert.assertTrue(cur.isInStock());
			Assert.assertTrue(Long.valueOf(cur.getId()) > Long.valueOf(prev.getId()));
			prev = cur;
		}

		prev = page.getContent().get(5);
		for (int i = 6; i < page.getContent().size(); i++) {
			ExampleElasticSearchBean cur = page.getContent().get(i);
			Assert.assertFalse(cur.isInStock());
			Assert.assertTrue(Long.valueOf(cur.getId()) > Long.valueOf(prev.getId()));
			prev = cur;
		}

	}

	@Test
	public void testQueryWithSort() {
		List<ExampleElasticSearchBean> values = new ArrayList<ExampleElasticSearchBean>();
		for (int i = 0; i < 10; i++) {
			values.add(createExampleBeanWithId(Integer.toString(i)));
		}
		elasticSearchTemplate.save(values);
		elasticSearchTemplate.refresh(ExampleElasticSearchBean.class);

		Query query = new SimpleQuery(new MatchAllCriteria()).addSort(new Sort(Sort.Direction.DESC, "name"));
		Page<ExampleElasticSearchBean> page = elasticSearchTemplate.findAll(query, ExampleElasticSearchBean.class);

		ExampleElasticSearchBean prev = page.getContent().get(0);
		for (int i = 1; i < page.getContent().size(); i++) {
			ExampleElasticSearchBean cur = page.getContent().get(i);
			Assert.assertTrue(Long.valueOf(cur.getId()) < Long.valueOf(prev.getId()));
			prev = cur;
		}
	}
}
