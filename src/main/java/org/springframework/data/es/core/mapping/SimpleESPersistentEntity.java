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

import java.util.Locale;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.BeanFactoryAccessor;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.data.es.ESClientFactory;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

/**
 * ElasticSearch specific {@link PersistentEntity} implementation
 * 
 * @param <T>
 * @author Patryk Wąsik
 */
public class SimpleESPersistentEntity<T> extends BasicPersistentEntity<T, ElasticSearchPersistentProperty> implements
		ElasticSearchPersistentEntity<T>, ApplicationContextAware {

	private final StandardEvaluationContext context;
	private boolean dateDetection;
	private String defaultIndexName;
	private String dynamicDateFormats;
	private String indexAnalyzer;
	private String indexName;
	private boolean numericDetection;
	private String searchAnalyzer;
	private String typeName;

	public SimpleESPersistentEntity(TypeInformation<T> typeInformation) {
		super(typeInformation);
		this.context = new StandardEvaluationContext();
		derivateESDocumentInfoFromClass(typeInformation.getType());
	}

	@Override
	public Boolean getDateDetection() {
		return dateDetection;
	}

	@Override
	public String getDynamicDateFormats() {
		return dynamicDateFormats;
	}

	@Override
	public String getIndexAnalyzer() {
		return indexAnalyzer;
	}

	@Override
	public String getIndexName() {
		return indexName;
	}

	@Override
	public Boolean getNumericDetection() {
		return numericDetection;
	}

	@Override
	public String getSearchAnalyzer() {
		return searchAnalyzer;
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context.addPropertyAccessor(new BeanFactoryAccessor());
		context.setBeanResolver(new BeanFactoryResolver(applicationContext));
		context.setRootObject(applicationContext);
		defaultIndexName = applicationContext.getBean(ESClientFactory.class).getDefaultIndexName();
	}

	private void derivateESDocumentInfoFromClass(Class<?> clazz) {
		typeName = clazz.getSimpleName().toLowerCase(Locale.ENGLISH);
		indexName = defaultIndexName != null ? defaultIndexName : typeName;
		if (clazz.isAnnotationPresent(ESDocument.class)) {
			ESDocument esDocument = clazz.getAnnotation(ESDocument.class);
			if (StringUtils.hasText(esDocument.value())) {
				typeName = esDocument.value();
			}
			if (StringUtils.hasText(esDocument.indexName())) {
				indexName = esDocument.indexName();
			}
			if (StringUtils.hasText(esDocument.indexAnalyzer())) {
				indexAnalyzer = esDocument.indexAnalyzer();
			}
			if (StringUtils.hasText(esDocument.searchAnalyzer())) {
				searchAnalyzer = esDocument.searchAnalyzer();
			}
			if (StringUtils.hasText(esDocument.dynamicDateFormats())) {
				dynamicDateFormats = esDocument.dynamicDateFormats();
			}
			dateDetection = esDocument.dateDetection();
			numericDetection = esDocument.numericDetection();
		}
	}

}
