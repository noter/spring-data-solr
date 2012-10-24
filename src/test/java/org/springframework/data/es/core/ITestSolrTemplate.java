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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.es.AbstractITestWithEmbeddedSolrServer;
import org.springframework.data.es.ExampleSolrBean;
import org.springframework.data.es.core.SolrTemplate;
import org.springframework.data.es.core.query.Criteria;
import org.springframework.data.es.core.query.FacetOptions;
import org.springframework.data.es.core.query.FacetQuery;
import org.springframework.data.es.core.query.Query;
import org.springframework.data.es.core.query.SimpleFacetQuery;
import org.springframework.data.es.core.query.SimpleField;
import org.springframework.data.es.core.query.SimpleQuery;
import org.springframework.data.es.core.query.SimpleStringCriteria;
import org.springframework.data.es.core.query.result.FacetEntry;
import org.springframework.data.es.core.query.result.FacetPage;
import org.xml.sax.SAXException;

/**
 * @author Christoph Strobl
 */
public class ITestSolrTemplate extends AbstractITestWithEmbeddedSolrServer {

	private SolrTemplate solrTemplate;

	@Before
	public void setUp() throws IOException, ParserConfigurationException, SAXException {
		solrTemplate = new SolrTemplate(solrServer, null);
	}

	@After
	public void tearDown() {
		solrTemplate.executeDelete(new SimpleQuery(new Criteria(Criteria.WILDCARD).expression(Criteria.WILDCARD)));
		solrTemplate.executeCommit();
	}

	@Test
	public void testBeanLifecycle() {
		ExampleSolrBean toInsert = createDefaultExampleBean();

		solrTemplate.executeAddBean(toInsert);
		ExampleSolrBean recalled = solrTemplate.executeObjectQuery(new SimpleQuery(new Criteria("id").is("1")),
				ExampleSolrBean.class);
		Assert.assertNull(recalled);
		solrTemplate.executeCommit();

		recalled = solrTemplate.executeObjectQuery(new SimpleQuery(new Criteria("id").is("1")), ExampleSolrBean.class);
		Assert.assertEquals(toInsert.getId(), recalled.getId());

		solrTemplate.executeDeleteById(toInsert.getId());
		recalled = solrTemplate.executeObjectQuery(new SimpleQuery(new Criteria("id").is("1")), ExampleSolrBean.class);
		Assert.assertEquals(toInsert.getId(), recalled.getId());

		solrTemplate.executeCommit();
		recalled = solrTemplate.executeObjectQuery(new SimpleQuery(new Criteria("id").is("1")), ExampleSolrBean.class);
		Assert.assertNull(recalled);
	}

	@Test
	public void testPing() throws SolrServerException, IOException {
		solrTemplate.executePing();
	}

	@Test
	public void testRollback() {
		ExampleSolrBean toInsert = createDefaultExampleBean();
		solrTemplate.executeAddBean(toInsert);
		ExampleSolrBean recalled = solrTemplate.executeObjectQuery(new SimpleQuery(new Criteria("id").is("1")),
				ExampleSolrBean.class);
		Assert.assertNull(recalled);

		solrTemplate.executeRollback();
		recalled = solrTemplate.executeObjectQuery(new SimpleQuery(new Criteria("id").is("1")), ExampleSolrBean.class);
		Assert.assertNull(recalled);
	}

	@Test
	public void testFacetQuery() {
		List<ExampleSolrBean> values = new ArrayList<ExampleSolrBean>();
		for (int i = 0; i < 10; i++) {
			values.add(createExampleBeanWithId(Integer.toString(i)));
		}
		solrTemplate.executeAddBeans(values);
		solrTemplate.executeCommit();

		FacetQuery q = new SimpleFacetQuery(new Criteria(Criteria.WILDCARD).expression(Criteria.WILDCARD))
				.setFacetOptions(new FacetOptions().addFacetOnField("name").addFacetOnField("id").setFacetLimit(5));

		FacetPage<ExampleSolrBean> page = solrTemplate.executeFacetQuery(q, ExampleSolrBean.class);

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
	public void testQueryWithSort() {
		List<ExampleSolrBean> values = new ArrayList<ExampleSolrBean>();
		for (int i = 0; i < 10; i++) {
			values.add(createExampleBeanWithId(Integer.toString(i)));
		}
		solrTemplate.executeAddBeans(values);
		solrTemplate.executeCommit();

		Query query = new SimpleQuery(new SimpleStringCriteria("*:*")).addSort(new Sort(Sort.Direction.DESC, "name"));
		Page<ExampleSolrBean> page = solrTemplate.executeListQuery(query, ExampleSolrBean.class);

		ExampleSolrBean prev = page.getContent().get(0);
		for (int i = 1; i < page.getContent().size(); i++) {
			ExampleSolrBean cur = page.getContent().get(i);
			Assert.assertTrue(Long.valueOf(cur.getId()) < Long.valueOf(prev.getId()));
			prev = cur;
		}
	}

	@Test
	public void testQueryWithMultiSort() {
		List<ExampleSolrBean> values = new ArrayList<ExampleSolrBean>();
		for (int i = 0; i < 10; i++) {
			ExampleSolrBean bean = createExampleBeanWithId(Integer.toString(i));
			bean.setInStock(i % 2 == 0);
			values.add(bean);
		}
		solrTemplate.executeAddBeans(values);
		solrTemplate.executeCommit();

		Query query = new SimpleQuery(new SimpleStringCriteria("*:*")).addSort(new Sort(Sort.Direction.DESC, "inStock"))
				.addSort(new Sort(Sort.Direction.ASC, "name"));
		Page<ExampleSolrBean> page = solrTemplate.executeListQuery(query, ExampleSolrBean.class);

		ExampleSolrBean prev = page.getContent().get(0);
		for (int i = 1; i < 5; i++) {
			ExampleSolrBean cur = page.getContent().get(i);
			Assert.assertTrue(cur.isInStock());
			Assert.assertTrue(Long.valueOf(cur.getId()) > Long.valueOf(prev.getId()));
			prev = cur;
		}

		prev = page.getContent().get(5);
		for (int i = 6; i < page.getContent().size(); i++) {
			ExampleSolrBean cur = page.getContent().get(i);
			Assert.assertFalse(cur.isInStock());
			Assert.assertTrue(Long.valueOf(cur.getId()) > Long.valueOf(prev.getId()));
			prev = cur;
		}

	}
}
