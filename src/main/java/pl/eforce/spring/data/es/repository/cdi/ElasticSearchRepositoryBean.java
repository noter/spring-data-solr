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
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.springframework.data.repository.cdi.CdiRepositoryBean;
import org.springframework.util.Assert;

import pl.eforce.spring.data.es.core.ElasticSearchOperations;
import pl.eforce.spring.data.es.repository.ElasticSearchRepository;
import pl.eforce.spring.data.es.repository.support.ElasticSearchRepositoryFactory;

/**
 * Uses CdiRepositoryBean to create {@link ElasticSearchRepository} instances.
 * 
 * @author Patryk Wasik
 */
public class ElasticSearchRepositoryBean<T> extends CdiRepositoryBean<T> {

	private final Bean<ElasticSearchOperations> esOperationsBean;

	public ElasticSearchRepositoryBean(Bean<ElasticSearchOperations> operations, Set<Annotation> qualifiers, Class<T> repositoryType,
			BeanManager beanManager) {
		super(qualifiers, repositoryType, beanManager);

		Assert.notNull(operations, "Cannot create repository with 'null' for ElasticSearchOperations.");
		esOperationsBean = operations;
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return esOperationsBean.getScope();
	}

	@Override
	protected T create(CreationalContext<T> creationalContext, Class<T> repositoryType) {
		ElasticSearchOperations esOperations = getDependencyInstance(esOperationsBean, ElasticSearchOperations.class);
		return new ElasticSearchRepositoryFactory(esOperations).getRepository(repositoryType);
	}

}
