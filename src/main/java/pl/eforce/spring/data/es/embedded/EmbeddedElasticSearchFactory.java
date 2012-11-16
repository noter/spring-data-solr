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
package pl.eforce.spring.data.es.embedded;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import pl.eforce.spring.data.es.ElasticSearchClientFactory;

/**
 * The EmbeddedElasticSearchServerFactory allows hosting of an ElasticSearch
 * instance in embedded mode.
 * 
 * @author Patryk Wasik
 */
public class EmbeddedElasticSearchFactory implements ElasticSearchClientFactory {

	private Client client;
	private String defaultIndexName = "defaultIndex";

	private Node node;

	@Override
	public String getDefaultIndexName() {
		return defaultIndexName;
	}

	@Override
	public Client getElasticSearchClient() {
		return client;
	}

	public void initElasticSearchNode() {
		node = NodeBuilder.nodeBuilder().local(true).node();
		client = node.client();
	}

	public void setDefaultIndexName(String defaultIndexName) {
		this.defaultIndexName = defaultIndexName;
	}

	public void shutdown() {
		client.close();
		node.close();
	}

}
