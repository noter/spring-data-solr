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
package pl.eforce.spring.data.es.repository.support;

import java.io.Serializable;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.Assert;

import pl.eforce.spring.data.es.core.ElasticSearchOperations;

/**
 * Spring {@link FactoryBean} implementation to ease container based
 * configuration for XML namespace and JavaConfig.
 * 
 * @author Oliver Gierke
 * @author Patryk Wasik
 */
public class ElasticSearchRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable> extends
		RepositoryFactoryBeanSupport<T, S, ID> {

	private ElasticSearchOperations operations;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport
	 * #afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {

		super.afterPropertiesSet();
		Assert.notNull(operations, "ElasticSearchOperations must be configured!");
	}

	/**
	 * Configures the {@link ElasticSearchOperations} to be used to create
	 * ElasticSearch repositories.
	 * 
	 * @param operations
	 *            the operations to set
	 */
	public void setElasticSearchOperations(ElasticSearchOperations operations) {
		Assert.notNull(operations);
		this.operations = operations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport
	 * #createRepositoryFactory()
	 */
	@Override
	protected RepositoryFactorySupport createRepositoryFactory() {
		return new ElasticSearchRepositoryFactory(operations);
	}
}
