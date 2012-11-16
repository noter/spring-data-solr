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

import org.elasticsearch.client.Client;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.Assert;

/**
 * @author Patryk Wąsik
 */
public class TransportClientElasticSearchFactory implements ElasticSearchClientFactory, DisposableBean {

	private Client client;
	private String defaultIndexName;

	public TransportClientElasticSearchFactory(Client client) {
		Assert.notNull(client);
		this.client = client;
	}

	public TransportClientElasticSearchFactory(Client client, String defaultIndexName) {
		this.client = client;
		this.defaultIndexName = defaultIndexName;
	}

	protected TransportClientElasticSearchFactory() {

	}

	@Override
	public void destroy() throws Exception {
		if (client != null) {
			client.close();
		}
	}

	@Override
	public String getDefaultIndexName() {
		return defaultIndexName;
	}

	@Override
	public Client getElasticSearchClient() {
		return client;
	}

	public void setDefaultIndexName(String defaultIndexName) {
		this.defaultIndexName = defaultIndexName;
	}

	public void setElasticSearchClient(Client client) {
		this.client = client;
	}

}
