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
package org.springframework.data.es.core.mapping;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

/**
 * @author Patryk Wasik
 * 
 */
public class SimpleElasticSearchMappingContext extends AbstractMappingContext<SimpleElasticSearchPersistentEntity<?>, ElasticSearchPersistentProperty> {

	@Override
	protected <T> SimpleElasticSearchPersistentEntity<?> createPersistentEntity(TypeInformation<T> typeInformation) {
		return new SimpleElasticSearchPersistentEntity<T>(typeInformation);
	}

	@Override
	protected ElasticSearchPersistentProperty createPersistentProperty(Field field, PropertyDescriptor descriptor, SimpleElasticSearchPersistentEntity<?> owner,
			SimpleTypeHolder simpleTypeHolder) {
		return new SimpleElasticSearchPersistentProperty(field, descriptor, owner, simpleTypeHolder);
	}

}
