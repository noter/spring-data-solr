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
package pl.eforce.spring.data.es.core.mapping;

import org.springframework.data.mapping.PersistentEntity;

/**
 * @param <T>
 * @author Patryk Wasik
 */
public interface ElasticSearchPersistentEntity<T> extends PersistentEntity<T, ElasticSearchPersistentProperty> {

	Boolean getDateDetection();

	String getDynamicDateFormats();

	String getIndexAnalyzer();

	String getIndexName();

	Boolean getNumericDetection();

	String getSearchAnalyzer();

	String getTypeName();

}
