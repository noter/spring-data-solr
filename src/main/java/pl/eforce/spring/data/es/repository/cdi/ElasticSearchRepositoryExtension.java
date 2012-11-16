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
package pl.eforce.spring.data.es.repository.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ProcessBean;

import org.springframework.data.repository.cdi.CdiRepositoryExtensionSupport;

import pl.eforce.spring.data.es.core.ElasticSearchOperations;

/**
 * @author Patryk Wasik
 */
public class ElasticSearchRepositoryExtension extends CdiRepositoryExtensionSupport {

	private final Map<String, Bean<ElasticSearchOperations>> esOperationsMap = new HashMap<String, Bean<ElasticSearchOperations>>();

	void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
		for (Entry<Class<?>, Set<Annotation>> entry : getRepositoryTypes()) {

			Class<?> repositoryType = entry.getKey();
			Set<Annotation> qualifiers = entry.getValue();

			Bean<?> repositoryBean = createRepositoryBean(repositoryType, qualifiers, beanManager);
			afterBeanDiscovery.addBean(repositoryBean);
		}
	}

	@SuppressWarnings("unchecked")
	<T> void processBean(@Observes ProcessBean<T> processBean) {
		Bean<T> bean = processBean.getBean();
		for (Type type : bean.getTypes()) {
			if ((type instanceof Class<?>) && ElasticSearchOperations.class.isAssignableFrom((Class<?>) type)) {
				esOperationsMap.put(bean.getQualifiers().toString(), ((Bean<ElasticSearchOperations>) bean));
			}
		}
	}

	private <T> Bean<T> createRepositoryBean(Class<T> repositoryType, Set<Annotation> qualifiers, BeanManager beanManager) {
		Bean<ElasticSearchOperations> esOperationBeans = esOperationsMap.get(qualifiers.toString());

		if (esOperationBeans == null) {
			throw new UnsatisfiedResolutionException(String.format("Unable to resolve a bean for '%s' with qualifiers %s.",
					ElasticSearchOperations.class.getName(), qualifiers));
		}

		return new ElasticSearchRepositoryBean<T>(esOperationBeans, qualifiers, repositoryType, beanManager);
	}

}
