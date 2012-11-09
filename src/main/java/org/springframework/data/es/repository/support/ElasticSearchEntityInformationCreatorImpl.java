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
package org.springframework.data.es.repository.support;

import java.io.Serializable;

import org.springframework.data.es.core.mapping.ElasticSearchPersistentEntity;
import org.springframework.data.es.core.mapping.ElasticSearchPersistentProperty;
import org.springframework.data.es.repository.query.ElasticSearchEntityInformation;
import org.springframework.data.es.repository.query.ElasticSearchEntityInformationCreator;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.util.Assert;

/**
 * @author Patryk Wasik
 */
public class ElasticSearchEntityInformationCreatorImpl implements ElasticSearchEntityInformationCreator {

	private final MappingContext<? extends ElasticSearchPersistentEntity<?>, ElasticSearchPersistentProperty> mappingContext;

	public ElasticSearchEntityInformationCreatorImpl(MappingContext<? extends ElasticSearchPersistentEntity<?>, ElasticSearchPersistentProperty> mappingContext) {
		Assert.notNull(mappingContext);
		this.mappingContext = mappingContext;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, ID extends Serializable> ElasticSearchEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
		ElasticSearchPersistentEntity<T> persistentEntity = (ElasticSearchPersistentEntity<T>) mappingContext.getPersistentEntity(domainClass);

		return new MappingElasticSearchEntityInformation<T, ID>(persistentEntity);
	}

}
