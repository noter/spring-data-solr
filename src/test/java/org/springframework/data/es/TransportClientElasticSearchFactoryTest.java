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
package org.springframework.data.es;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.MalformedURLException;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Patryk Wasik
 */
public class TransportClientElasticSearchFactoryTest {

	private Client client;
	private Node node;

	@Before
	public void setUp() throws MalformedURLException {
		node = NodeBuilder.nodeBuilder().local(true).node();
		client = node.client();
	}

	@After
	public void tearDown() {
		client.close();
		node.close();
	}

	@Test
	public void testInitFactory() {
		TransportClientElasticSearchFactory factory = new TransportClientElasticSearchFactory(client);

		assertNotNull(factory.getElasticSearchClient());
		assertEquals(client, factory.getElasticSearchClient());
		assertNull(factory.getDefaultIndexName());
	}

	@Test
	public void testInitFactoryWithDefaultIndex() {
		TransportClientElasticSearchFactory factory = new TransportClientElasticSearchFactory(client, "defaultIndex");

		assertNotNull(factory.getElasticSearchClient());
		assertEquals(client, factory.getElasticSearchClient());
		assertEquals("defaultIndex", factory.getDefaultIndexName());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testInitFactoryWithNullServer() {
		new TransportClientElasticSearchFactory(null);
	}

}
