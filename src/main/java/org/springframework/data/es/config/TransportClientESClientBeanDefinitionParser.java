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
package org.springframework.data.es.config;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.data.es.support.TransportClientESClientFactoryBean;
import org.w3c.dom.Element;

/**
 * @author Patryk Wasik
 */
public class TransportClientESClientBeanDefinitionParser extends AbstractBeanDefinitionParser {

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(TransportClientESClientFactoryBean.class);
		setESConf(element, builder);
		return getSourcedBeanDefinition(builder, element, parserContext);
	}

	private AbstractBeanDefinition getSourcedBeanDefinition(BeanDefinitionBuilder builder, Element source, ParserContext context) {

		AbstractBeanDefinition definition = builder.getBeanDefinition();
		definition.setSource(context.extractSource(source));
		return definition;
	}

	private void setESConf(Element element, BeanDefinitionBuilder builder) {
		builder.addPropertyValue("addresses", element.getAttribute("addresses"));
		builder.addPropertyValue("ignoreClusterName", element.getAttribute("ignoreClusterName"));
		builder.addPropertyValue("pingTimeout", element.getAttribute("pingTimeout"));
		builder.addPropertyValue("nodeSamplerInterval", element.getAttribute("nodeSamplerInterval"));
		builder.addPropertyValue("defaultIndexName", element.getAttribute("defaultIndexName"));
		;
	}
}