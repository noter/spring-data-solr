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
package pl.eforce.spring.data.es;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.NodeBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.xml.sax.SAXException;

/**
 * @author Patryk Wasik
 */
public abstract class AbstractITestWithEmbeddedElasticSearch {

	protected static Client client;
	protected static String DEFAULT_BEAN_ID = "1";

	@AfterClass
	public static void cleanDataInElasticSearch() {
		client.admin().indices().prepareDelete().execute().actionGet();
	}

	@BeforeClass
	public static void initSolrServer() throws IOException, ParserConfigurationException, SAXException {
		client = NodeBuilder.nodeBuilder().local(true).node().client();
	}

	public ExampleElasticSearchBean createDefaultExampleBean() {
		return createExampleBeanWithId(DEFAULT_BEAN_ID);
	}

	public ExampleElasticSearchBean createExampleBeanWithId(String id) {
		return new ExampleElasticSearchBean(id, "bean_" + id, "category_" + id);
	}

}
