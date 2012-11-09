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
package org.springframework.data.es.core.convert;

import java.util.List;
import java.util.Map;

import org.elasticsearch.common.mvel2.ConversionException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.es.core.mapping.ElasticSearchPersistentEntity;
import org.springframework.data.es.core.mapping.ElasticSearchPersistentProperty;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Patryk Wąsik
 */
public class MappingElasticSearchConverter implements ElasticSearchConverter, ApplicationContextAware, InitializingBean {

	interface IsEmptyMixIn {

		@JsonIgnore
		boolean isEmpty();
	}

	@SuppressWarnings("unused")
	private ApplicationContext applicationContext;
	private final GenericConversionService conversionService;
	private final MappingContext<? extends ElasticSearchPersistentEntity<?>, ElasticSearchPersistentProperty> mappingContext;

	private final ObjectMapper objectMapper;

	public MappingElasticSearchConverter(MappingContext<? extends ElasticSearchPersistentEntity<?>, ElasticSearchPersistentProperty> mappingContext) {
		Assert.notNull(mappingContext);

		this.mappingContext = mappingContext;
		conversionService = new DefaultConversionService();
		objectMapper = new ObjectMapper();
		objectMapper.addMixInAnnotations(List.class, IsEmptyMixIn.class);
		objectMapper.addMixInAnnotations(Map.class, IsEmptyMixIn.class);
		objectMapper.setPropertyNamingStrategy(new ElasticSearchMappingPropertyNamingStrategy(mappingContext));
	}

	@Override
	public void afterPropertiesSet() {
	}

	@Override
	public ConversionService getConversionService() {
		return conversionService;
	}

	@Override
	public MappingContext<? extends ElasticSearchPersistentEntity<?>, ElasticSearchPersistentProperty> getMappingContext() {
		return mappingContext;
	}

	@Override
	public <R> R read(Class<R> type, StringBuilder source) {
		return read(ClassTypeInformation.from(type), source);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void write(Object source, StringBuilder target) {
		if (source == null) {
			return;
		}

		try {
			target.append(objectMapper.writeValueAsString(source));
		} catch (JsonProcessingException e) {
			throw new ConversionException(String.format("Can't convert class '%s' to json", source.getClass().getName()), e);
		}
	}

	protected <S extends Object> S read(TypeInformation<S> targetTypeInformation, StringBuilder source) {
		try {
			return objectMapper.readValue(source.toString(), targetTypeInformation.getType());
		} catch (Exception e) {
			throw new ConversionException(String.format("Can't convert json to class '%s'", targetTypeInformation.getType().getName()), e);
		}
	}

}
