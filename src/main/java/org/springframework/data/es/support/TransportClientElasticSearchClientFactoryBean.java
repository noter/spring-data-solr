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
package org.springframework.data.es.support;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.es.TransportClientElasticSearchFactory;
import org.springframework.util.Assert;

/**
 * @author Patryk Wasik
 */
public class TransportClientElasticSearchClientFactoryBean extends TransportClientElasticSearchFactory implements FactoryBean<Client>, InitializingBean,
		DisposableBean {

	private InetSocketTransportAddress[] addresses;
	private Boolean ignoreClusterName;
	private Integer nodeSamplerInterval;
	private Integer pingTimeout;

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notEmpty(addresses);
		initESClient();
	}

	@Override
	public Client getObject() throws Exception {
		return getElasticSearchClient();
	}

	@Override
	public Class<?> getObjectType() {
		return Client.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setAddresses(InetSocketTransportAddress[] addresses) {
		this.addresses = addresses;
	}

	public void setIgnoreClusterName(Boolean ignoreClusterName) {
		this.ignoreClusterName = ignoreClusterName;
	}

	public void setNodeSamplerInterval(Integer nodeSamplerInterval) {
		this.nodeSamplerInterval = nodeSamplerInterval;
	}

	public void setPingTimeout(Integer pingTimeout) {
		this.pingTimeout = pingTimeout;
	}

	private void initESClient() {
		Builder settingsBuilder = ImmutableSettings.settingsBuilder();
		if (ignoreClusterName != null) {
			settingsBuilder.put("client.transport.ignore_cluster_name", ignoreClusterName);
		}
		if (pingTimeout != null) {
			settingsBuilder.put("client.transport.ping_timeout", pingTimeout);
		}
		if (nodeSamplerInterval != null) {
			settingsBuilder.put("client.transport.nodes_sampler_interval", nodeSamplerInterval);
		}

		setElasticSearchClient(new TransportClient(settingsBuilder).addTransportAddresses(addresses));
	}

}
