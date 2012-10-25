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
import org.springframework.data.mapping.model.BeanWrapper;
import org.springframework.data.repository.core.support.AbstractEntityInformation;

/**
 * ElasticSearch specific implementation of {@link AbstractEntityInformation}
 * 
 * @param <T>
 * @param <ID>
 * @author Patryk Wasik
 */
public class MappingESEntityInformation<T, ID extends Serializable> extends AbstractEntityInformation<T, ID> implements
		ElasticSearchEntityInformation<T, ID> {

	private final ElasticSearchPersistentEntity<T> entityMetadata;
	private final String indexTypeName;

	public MappingESEntityInformation(ElasticSearchPersistentEntity<T> entity) {
		this(entity, null);
	}

	public MappingESEntityInformation(ElasticSearchPersistentEntity<T> entity, String indexTypeName) {
		super(entity.getType());
		this.entityMetadata = entity;
		this.indexTypeName = indexTypeName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ID getId(T entity) {
		ElasticSearchPersistentProperty id = entityMetadata.getIdProperty();
		try {
			return (ID) BeanWrapper.create(entity, null).getProperty(id);
		} catch (Exception e) {
			throw new IllegalStateException("ID could not be resolved", e);
		}
	}

	@Override
	public String getIdAttribute() {
		return entityMetadata.getIdProperty().getIndexName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<ID> getIdType() {
		return (Class<ID>) String.class;
	}

	@Override
	public String getIndexTypeName() {
		return indexTypeName != null ? indexTypeName : entityMetadata.getTypeName();
	}

}
