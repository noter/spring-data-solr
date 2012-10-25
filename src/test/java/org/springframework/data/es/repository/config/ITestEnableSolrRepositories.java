/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.es.repository.config;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.es.AbstractITestWithEmbeddedSolrServer;
import org.springframework.data.es.core.SolrOperations;
import org.springframework.data.es.core.SolrTemplate;
import org.springframework.data.es.repository.config.EnableElasticSearchRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration test for {@link EnableElasticSearchRepositories}.
 * 
 * @author Oliver Gierke
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ITestEnableSolrRepositories extends AbstractITestWithEmbeddedSolrServer {

	@Configuration
	@EnableElasticSearchRepositories
	static class Config {

		@Bean
		public SolrOperations solrTemplate() {
			return new SolrTemplate(solrServer);
		}
	}

	@Autowired
	PersonRepository repository;

	@Test
	public void bootstrapsRepository() {
		assertThat(repository, is(notNullValue()));
	}
}
