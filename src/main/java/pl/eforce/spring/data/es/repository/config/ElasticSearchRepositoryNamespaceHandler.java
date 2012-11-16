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
package pl.eforce.spring.data.es.repository.config;

import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.data.repository.config.RepositoryBeanDefinitionParser;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import pl.eforce.spring.data.es.config.TransportClientElasticSearchBeanDefinitionParser;
import pl.eforce.spring.data.es.embedded.config.EmbeddedElasticSearchBeanDefinitionParser;

/**
 * {@link NamespaceHandler} implementation to register parser for
 * {@code <es:repositories />},
 * {@code <es:embedded-es-node defaultIndexName="indexName" />} elements.
 * 
 * @author Oliver Gierke
 * @author Christoph Strobl
 * @author Patryk Wąsik
 */
class ElasticSearchRepositoryNamespaceHandler extends NamespaceHandlerSupport {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
	 */
	@Override
	public void init() {

		RepositoryConfigurationExtension extension = new ElasticSearchRepositoryConfigExtension();
		RepositoryBeanDefinitionParser parser = new RepositoryBeanDefinitionParser(extension);

		registerBeanDefinitionParser("repositories", parser);
		registerBeanDefinitionParser("embedded-es-node", new EmbeddedElasticSearchBeanDefinitionParser());
		registerBeanDefinitionParser("es-client", new TransportClientElasticSearchBeanDefinitionParser());
	}
}