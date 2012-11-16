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
package pl.eforce.spring.data.es.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pl.eforce.spring.data.es.AbstractITestWithEmbeddedElasticSearch;
import pl.eforce.spring.data.es.ExampleElasticSearchBean;
import pl.eforce.spring.data.es.core.ElasticSearchTemplate;
import pl.eforce.spring.data.es.repository.query.ElasticSearchEntityInformation;
import pl.eforce.spring.data.es.repository.query.ElasticSearchEntityInformationCreator;
import pl.eforce.spring.data.es.repository.support.ElasticSearchEntityInformationCreatorImpl;

import com.google.common.collect.Lists;

/**
 * @author Patryk Wasik
 */
public class ITestSimpleSolrRepository extends AbstractITestWithEmbeddedElasticSearch {

	private ExampleElasticSearchBeanRepository repository;

	@Before
	public void setUp() {
		ElasticSearchTemplate elasticSearchTemplate = new ElasticSearchTemplate(client);
		ElasticSearchEntityInformationCreator elasticSearchRepositoryFactory = new ElasticSearchEntityInformationCreatorImpl(elasticSearchTemplate
				.getConverter().getMappingContext());
		ElasticSearchEntityInformation<ExampleElasticSearchBean, String> entityInformation = elasticSearchRepositoryFactory
				.getEntityInformation(ExampleElasticSearchBean.class);
		repository = new ExampleElasticSearchBeanRepository(entityInformation, elasticSearchTemplate);
	}

	@Test
	public void testBeanLifecyle() {
		ExampleElasticSearchBean toInsert = createDefaultExampleBean();
		ExampleElasticSearchBean savedBean = repository.save(toInsert);

		Assert.assertSame(toInsert, savedBean);

		Assert.assertTrue(repository.exists(savedBean.getId()));

		ExampleElasticSearchBean retrieved = repository.findOne(savedBean.getId());
		Assert.assertNotNull(retrieved);
		Assert.assertEquals(savedBean, retrieved);

		Assert.assertEquals(1, repository.count());

		Assert.assertTrue(repository.exists(savedBean.getId()));

		repository.delete(savedBean);

		Assert.assertEquals(0, repository.count());
		retrieved = repository.findOne(savedBean.getId());
		Assert.assertNull(retrieved);
	}

	@Test
	public void testListFunctions() {
		int objectCount = 100;
		List<ExampleElasticSearchBean> toInsert = new ArrayList<ExampleElasticSearchBean>(objectCount);
		for (int i = 0; i < 100; i++) {
			toInsert.add(createExampleBeanWithId(Integer.toString(i)));
		}

		repository.save(toInsert);

		Assert.assertEquals(objectCount, repository.count());

		List<ExampleElasticSearchBean> retrivedBeans = Lists.newArrayList(repository.findAll());
		assertThat(retrivedBeans, hasSize(toInsert.size()));
		assertThat(retrivedBeans, containsInAnyOrder(toInsert.toArray(new ExampleElasticSearchBean[0])));

		repository.delete(toInsert.get(0));
		Assert.assertEquals(99, repository.count());

		repository.deleteAll();

		Assert.assertEquals(0, repository.count());
	}

}
