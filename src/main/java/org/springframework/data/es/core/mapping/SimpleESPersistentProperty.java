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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * ElasticSearch specific {@link PersistentProperty} implementation processing
 * taking {@link ESField} into account
 * 
 * @author Patryk Wasik
 * 
 */
public class SimpleESPersistentProperty extends AnnotationBasedPersistentProperty<ElasticSearchPersistentProperty> implements
		ElasticSearchPersistentProperty {

	private static final Set<String> SUPPORTED_ID_PROPERTY_NAMES = new HashSet<String>();

	static {
		SUPPORTED_ID_PROPERTY_NAMES.add("id");
	}

	private String indexName;

	private MappingInfo[] mappingInfos;

	public SimpleESPersistentProperty(Field field, PropertyDescriptor propertyDescriptor, PersistentEntity<?, ElasticSearchPersistentProperty> owner,
			SimpleTypeHolder simpleTypeHolder) {
		super(field, propertyDescriptor, owner, simpleTypeHolder);
		derivateESFieldInfoFromField(field);
	}

	@Override
	public String getIndexName() {
		return indexName;
	}

	@Override
	public MappingInfo getMappingInfo() {
		return mappingInfos[0];
	}

	@Override
	public MappingInfo[] getMappingInfos() {
		return mappingInfos;
	}

	@Override
	public boolean isIdProperty() {
		if (super.isIdProperty()) {
			return true;
		}

		return SUPPORTED_ID_PROPERTY_NAMES.contains(getMappingInfo().getIndexName());
	}

	@Override
	public boolean isMultiField() {
		return mappingInfos.length > 0;
	}

	@Override
	protected Association<ElasticSearchPersistentProperty> createAssociation() {
		return null;
	}

	private void derivateESFieldInfoFromField(Field field) {
		String fieldName = field.getName();
		if (field.isAnnotationPresent(ESMultiField.class)) {
			ESMultiField esMultiField = field.getAnnotation(ESMultiField.class);

			Assert.notEmpty(esMultiField.value());

			indexName = StringUtils.hasText(esMultiField.name()) ? esMultiField.name() : fieldName;

			mappingInfos = new MappingInfo[esMultiField.value().length];
			for (int i = 0; i < esMultiField.value().length; i++) {
				ESField esField = esMultiField.value()[i];
				MappingInfo mappingInfo = new MappingInfo();
				mappingInfo.setIndexName(fieldName);
				readMappingInfo(esField, mappingInfo);
				mappingInfos[i] = mappingInfo;
			}
		} else if (field.isAnnotationPresent(ESField.class)) {
			ESField esField = field.getAnnotation(ESField.class);
			mappingInfos = new MappingInfo[1];
			MappingInfo mappingInfo = new MappingInfo();
			mappingInfo.setIndexName(fieldName);
			readMappingInfo(esField, mappingInfo);

			indexName = mappingInfo.getIndexName();

			mappingInfos[0] = mappingInfo;
		} else {
			mappingInfos = new MappingInfo[1];
			MappingInfo mappingInfo = new MappingInfo();
			mappingInfo.setIndexName(fieldName);

			indexName = mappingInfo.getIndexName();

			mappingInfos[0] = mappingInfo;
		}
	}

	private void readMappingInfo(ESField esField, MappingInfo mappingInfo) {
		if (StringUtils.hasText(esField.indexName())) {
			mappingInfo.setIndexName(esField.indexName());
		}
		if (esField.boost() != 1.0) {
			mappingInfo.setBoost(esField.boost());
		}
		if (!esField.includeInAll()) {
			mappingInfo.setIncludeInAll(esField.includeInAll());
		}
		if (esField.store()) {
			mappingInfo.setStore(true);
		}
		if (StringUtils.hasText(esField.index())) {
			mappingInfo.setIndex(esField.index());
		}
		if (StringUtils.hasText(esField.nullValue())) {
			mappingInfo.setNullValue(esField.nullValue());
		}
		if (getType().equals(String.class)) {
			if (StringUtils.hasText(esField.analyzer())) {
				mappingInfo.setAnalyzer(esField.analyzer());
			}
			if (StringUtils.hasText(esField.indexAnalyzer())) {
				mappingInfo.setIndexAnalyzer(esField.indexAnalyzer());
			}
			if (StringUtils.hasText(esField.searchAnalyzer())) {
				mappingInfo.setSearchAnalyzer(esField.searchAnalyzer());
			}
			if (esField.omitNorms()) {
				mappingInfo.setOmitNorms(esField.omitNorms());
			}
			if (esField.omitTermFreqAndPositions()) {
				mappingInfo.setOmitTermFreqAndPositions(esField.omitTermFreqAndPositions());
			}
			if (StringUtils.hasText(esField.indexOptions())) {
				mappingInfo.setIndexOptions(esField.indexOptions());
			}
			if (esField.ignoreAbove() > 0) {
				mappingInfo.setIgnoreAbove(esField.ignoreAbove());
			}
			if (StringUtils.hasText(esField.termVector())) {
				mappingInfo.setTermVector(esField.termVector());
			}
		}
		if (Number.class.isAssignableFrom(getType())) {
			if (esField.precisionStep() != 4) {
				mappingInfo.setPrecisionStep(esField.precisionStep());
			}
			if (esField.ignoreMalformed()) {
				mappingInfo.setIgnoreMalformed(esField.ignoreMalformed());
			}
		}
		if (getType().equals(Date.class)) {
			if (esField.precisionStep() != 4) {
				mappingInfo.setPrecisionStep(esField.precisionStep());
			}
			if (StringUtils.hasText(esField.format())) {
				mappingInfo.setFormat(esField.format());
			}
			if (esField.ignoreMalformed()) {
				mappingInfo.setIgnoreMalformed(esField.ignoreMalformed());
			}
		}
	}
}
