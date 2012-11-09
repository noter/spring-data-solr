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

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.es.AbstractITestWithEmbeddedElasticSearch;
import org.springframework.data.es.ExampleElasticSearchBean;
import org.springframework.data.es.core.SolrTemplate;
import org.springframework.data.es.core.geo.Distance;
import org.springframework.data.es.core.geo.GeoLocation;
import org.springframework.data.es.core.query.Criteria;
import org.springframework.data.es.core.query.SimpleQuery;
import org.xml.sax.SAXException;

/**
 * @author Christoph Strobl
 */
public class ITestCriteriaExecution extends AbstractITestWithEmbeddedElasticSearch {

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
	public void testNegativeNumberCriteria() {
		ExampleElasticSearchBean positivePopularity = createExampleBeanWithId("1");
		positivePopularity.setPopularity(100);

		ExampleElasticSearchBean negativePopularity = createExampleBeanWithId("2");
		negativePopularity.setPopularity(-200);

		solrTemplate.executeAddBeans(Arrays.asList(positivePopularity, negativePopularity));
		solrTemplate.executeCommit();

		Page<ExampleElasticSearchBean> result = solrTemplate.executeListQuery(new SimpleQuery(new Criteria("popularity").is(-200)),
				ExampleElasticSearchBean.class);
		Assert.assertEquals(1, result.getContent().size());
		Assert.assertEquals(negativePopularity.getId(), result.getContent().get(0).getId());
	}

	@Test
	public void testNegativeNumberInRange() {
		ExampleElasticSearchBean negative100 = createExampleBeanWithId("1");
		negative100.setPopularity(-100);

		ExampleElasticSearchBean negative200 = createExampleBeanWithId("2");
		negative200.setPopularity(-200);

		solrTemplate.executeAddBeans(Arrays.asList(negative100, negative200));
		solrTemplate.executeCommit();

		Page<ExampleElasticSearchBean> result = solrTemplate.executeListQuery(
				new SimpleQuery(new Criteria("popularity").between(-150, -50)), ExampleElasticSearchBean.class);
		Assert.assertEquals(1, result.getContent().size());
		Assert.assertEquals(negative100.getId(), result.getContent().get(0).getId());
	}

	@Test
	public void testDateValue() {
		ExampleElasticSearchBean searchableBean = createExampleBeanWithId("1");
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.set(2012, 7, 23, 6, 10, 0);
		searchableBean.setLastModified(calendar.getTime());

		solrTemplate.executeAddBean(searchableBean);
		solrTemplate.executeCommit();

		Page<ExampleElasticSearchBean> result = solrTemplate.executeListQuery(
				new SimpleQuery(new Criteria("last_modified").is(calendar.getTime())), ExampleElasticSearchBean.class);
		Assert.assertEquals(1, result.getContent().size());
	}

	@Test
	public void testDateValueInRangeQuery() {
		ExampleElasticSearchBean searchableBeanIn2012 = createExampleBeanWithId("1");
		Calendar calendar2012 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar2012.set(2012, 7, 23, 6, 10, 0);
		searchableBeanIn2012.setLastModified(calendar2012.getTime());

		ExampleElasticSearchBean searchableBeanIn2011 = createExampleBeanWithId("2");
		Calendar calendar2011 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar2011.set(2011, 7, 23, 6, 10, 0);
		searchableBeanIn2011.setLastModified(calendar2011.getTime());

		solrTemplate.executeAddBeans(Arrays.asList(searchableBeanIn2012, searchableBeanIn2011));
		solrTemplate.executeCommit();

		Page<ExampleElasticSearchBean> result = solrTemplate.executeListQuery(
				new SimpleQuery(new Criteria("last_modified").between(new DateTime(2012, 1, 1, 0, 0, 0, DateTimeZone.UTC),
						new DateTime(2012, 12, 31, 23, 59, 59, DateTimeZone.UTC))), ExampleElasticSearchBean.class);
		Assert.assertEquals(1, result.getContent().size());

	}

	@Test
	public void testGeoLocation() {
		ExampleElasticSearchBean searchableBeanInBuffalow = createExampleBeanWithId("1");
		searchableBeanInBuffalow.setStore("45.17614,-93.87341");

		ExampleElasticSearchBean searchableBeanInNYC = createExampleBeanWithId("2");
		searchableBeanInNYC.setStore("40.7143,-74.006");

		solrTemplate.executeAddBeans(Arrays.asList(searchableBeanInBuffalow, searchableBeanInNYC));
		solrTemplate.executeCommit();

		Page<ExampleElasticSearchBean> result = solrTemplate.executeListQuery(
				new SimpleQuery(new Criteria("store").near(new GeoLocation(45.15, -93.85), new Distance(5))),
				ExampleElasticSearchBean.class);

		Assert.assertEquals(1, result.getContent().size());
	}
}
