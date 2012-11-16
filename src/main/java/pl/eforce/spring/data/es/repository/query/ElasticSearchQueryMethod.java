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
package pl.eforce.spring.data.es.repository.query;

import java.lang.reflect.Method;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.StringUtils;

import pl.eforce.spring.data.es.repository.Query;

/**
 * ElasticSearch specific implementation of {@link QueryMethod} taking care of
 * {@link Query}
 * 
 * @author Patryk Wasik
 */
public class ElasticSearchQueryMethod extends QueryMethod {

	private final ElasticSearchEntityInformation<?, ?> entityInformation;
	private final Method method;

	public ElasticSearchQueryMethod(Method method, RepositoryMetadata metadata,
			ElasticSearchEntityInformationCreator elasticSearchEntityInformationCreator) {
		super(method, metadata);
		this.method = method;
		entityInformation = elasticSearchEntityInformationCreator.getEntityInformation(metadata.getReturnedDomainClass(method));
	}

	@Override
	public ElasticSearchEntityInformation<?, ?> getEntityInformation() {
		return entityInformation;
	}

	@Override
	public String getNamedQueryName() {
		if (!hasAnnotatedNamedQueryName()) {
			return super.getNamedQueryName();
		}
		return getAnnotatedNamedQueryName();
	}

	public boolean hasAnnotatedNamedQueryName() {
		return getAnnotatedNamedQueryName() != null;
	}

	public boolean hasAnnotatedQuery() {
		return getAnnotatedQuery() != null;
	}

	String getAnnotatedNamedQueryName() {
		String namedQueryName = (String) AnnotationUtils.getValue(getQueryAnnotation(), "name");
		return StringUtils.hasText(namedQueryName) ? namedQueryName : null;
	}

	String getAnnotatedQuery() {
		String query = (String) AnnotationUtils.getValue(getQueryAnnotation(), "value");
		return StringUtils.hasText(query) ? query : null;
	}

	TypeInformation<?> getReturnType() {
		return ClassTypeInformation.fromReturnTypeOf(method);
	}

	private Query getQueryAnnotation() {
		return method.getAnnotation(Query.class);
	}

}
