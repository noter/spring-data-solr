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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.util.TypeInformation;

import pl.eforce.es.orm.mapping.ESDocument;
import pl.eforce.spring.data.es.ElasticSearchClientFactory;
import pl.eforce.spring.data.es.core.mapping.SimpleElasticSearchPersistentEntity;
import pl.eforce.spring.data.es.core.mapping.SimpleElasticSearchPersistentProperty;

/**
 * @author Patryk Wasik
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleElasticSearchPersistentEntityTest {

	@ESDocument
	static class SearchableBeanWithEmptyESDocumentAnnotation {
	}

	@ESDocument(value = "intexTypeName", indexName = "indexName")
	static class SearchableBeanWithESDocumentTypeAndIndexAnnotation {
	}

	@ESDocument("indexTypeName")
	static class SearchableBeanWithESDocumentTypeAnnotation {
	}

	static class SearchableBeanWithoutESDocumentAnnotation {
	}

	@Mock
	ElasticSearchClientFactory elasticSearchClientFactory;

	@SuppressWarnings("rawtypes")
	@Mock
	TypeInformation typeInfo;

	@Test
	public void testBeanWithoutAnnotation() {
		Mockito.when(typeInfo.getType()).thenReturn(SearchableBeanWithoutESDocumentAnnotation.class);
		Mockito.when(elasticSearchClientFactory.getDefaultIndexName()).thenReturn(null);

		SimpleElasticSearchPersistentEntity<SimpleElasticSearchPersistentProperty> elasticSearchPersistentEntity = new SimpleElasticSearchPersistentEntity<SimpleElasticSearchPersistentProperty>(
				typeInfo, elasticSearchClientFactory);

		assertNotNull(elasticSearchPersistentEntity.getTypeName());
		assertNotNull(elasticSearchPersistentEntity.getIndexName());
		assertThat(elasticSearchPersistentEntity.getTypeName(), is("searchablebeanwithoutesdocumentannotation"));
		assertThat(elasticSearchPersistentEntity.getIndexName(), is("searchablebeanwithoutesdocumentannotation"));

	}

	@Test
	public void testBeanWithoutAnnotationAndDefaultIndexSet() {
		Mockito.when(typeInfo.getType()).thenReturn(SearchableBeanWithoutESDocumentAnnotation.class);
		Mockito.when(elasticSearchClientFactory.getDefaultIndexName()).thenReturn("defaultIndexName");

		SimpleElasticSearchPersistentEntity<SimpleElasticSearchPersistentProperty> elasticSearchPersistentEntity = new SimpleElasticSearchPersistentEntity<SimpleElasticSearchPersistentProperty>(
				typeInfo, elasticSearchClientFactory);

		assertNotNull(elasticSearchPersistentEntity.getTypeName());
		assertNotNull(elasticSearchPersistentEntity.getIndexName());
		assertThat(elasticSearchPersistentEntity.getTypeName(), is("searchablebeanwithoutesdocumentannotation"));
		assertThat(elasticSearchPersistentEntity.getIndexName(), is("defaultIndexName"));
	}

	@Test
	public void testEmptyESDocumentAnnotation() {
		Mockito.when(typeInfo.getType()).thenReturn(SearchableBeanWithEmptyESDocumentAnnotation.class);
		Mockito.when(elasticSearchClientFactory.getDefaultIndexName()).thenReturn(null);

		SimpleElasticSearchPersistentEntity<SimpleElasticSearchPersistentProperty> elasticSearchPersistentEntity = new SimpleElasticSearchPersistentEntity<SimpleElasticSearchPersistentProperty>(
				typeInfo, elasticSearchClientFactory);

		assertNotNull(elasticSearchPersistentEntity.getTypeName());
		assertNotNull(elasticSearchPersistentEntity.getIndexName());
		assertThat(elasticSearchPersistentEntity.getTypeName(), is("searchablebeanwithemptyesdocumentannotation"));
		assertThat(elasticSearchPersistentEntity.getIndexName(), is("searchablebeanwithemptyesdocumentannotation"));

	}

	@Test
	public void testEmptyESDocumentAnnotationWithDefaultIndexSet() {
		Mockito.when(typeInfo.getType()).thenReturn(SearchableBeanWithEmptyESDocumentAnnotation.class);
		Mockito.when(elasticSearchClientFactory.getDefaultIndexName()).thenReturn("defaultIndexName");

		SimpleElasticSearchPersistentEntity<SimpleElasticSearchPersistentProperty> elasticSearchPersistentEntity = new SimpleElasticSearchPersistentEntity<SimpleElasticSearchPersistentProperty>(
				typeInfo, elasticSearchClientFactory);

		assertNotNull(elasticSearchPersistentEntity.getTypeName());
		assertNotNull(elasticSearchPersistentEntity.getIndexName());
		assertThat(elasticSearchPersistentEntity.getTypeName(), is("searchablebeanwithemptyesdocumentannotation"));
		assertThat(elasticSearchPersistentEntity.getIndexName(), is("defaultIndexName"));
	}

	@Test
	public void testESDocumentAnnotationWithType() {
		Mockito.when(typeInfo.getType()).thenReturn(SearchableBeanWithESDocumentTypeAnnotation.class);
		Mockito.when(elasticSearchClientFactory.getDefaultIndexName()).thenReturn(null);

		SimpleElasticSearchPersistentEntity<SimpleElasticSearchPersistentProperty> elasticSearchPersistentEntity = new SimpleElasticSearchPersistentEntity<SimpleElasticSearchPersistentProperty>(
				typeInfo, elasticSearchClientFactory);

		assertNotNull(elasticSearchPersistentEntity.getTypeName());
		assertNotNull(elasticSearchPersistentEntity.getIndexName());
		assertThat(elasticSearchPersistentEntity.getTypeName(), is("indexTypeName"));
		assertThat(elasticSearchPersistentEntity.getIndexName(), is("indexTypeName"));

	}

	@Test
	public void testESDocumentAnnotationWithTypeAndDefaultIndexSet() {
		Mockito.when(typeInfo.getType()).thenReturn(SearchableBeanWithESDocumentTypeAnnotation.class);
		Mockito.when(elasticSearchClientFactory.getDefaultIndexName()).thenReturn("defaultIndexName");

		SimpleElasticSearchPersistentEntity<SimpleElasticSearchPersistentProperty> elasticSearchPersistentEntity = new SimpleElasticSearchPersistentEntity<SimpleElasticSearchPersistentProperty>(
				typeInfo, elasticSearchClientFactory);

		assertNotNull(elasticSearchPersistentEntity.getTypeName());
		assertNotNull(elasticSearchPersistentEntity.getIndexName());
		assertThat(elasticSearchPersistentEntity.getTypeName(), is("indexTypeName"));
		assertThat(elasticSearchPersistentEntity.getIndexName(), is("defaultIndexName"));
	}

	@Test
	public void testESDocumentAnnotationWithTypeAndIndex() {
		Mockito.when(typeInfo.getType()).thenReturn(SearchableBeanWithESDocumentTypeAndIndexAnnotation.class);
		Mockito.when(elasticSearchClientFactory.getDefaultIndexName()).thenReturn(null);

		SimpleElasticSearchPersistentEntity<SimpleElasticSearchPersistentProperty> elasticSearchPersistentEntity = new SimpleElasticSearchPersistentEntity<SimpleElasticSearchPersistentProperty>(
				typeInfo, elasticSearchClientFactory);

		assertNotNull(elasticSearchPersistentEntity.getTypeName());
		assertNotNull(elasticSearchPersistentEntity.getIndexName());
		assertThat(elasticSearchPersistentEntity.getTypeName(), is("intexTypeName"));
		assertThat(elasticSearchPersistentEntity.getIndexName(), is("indexName"));

	}

	@Test
	public void testESDocumentAnnotationWithTypeAndIndexAndDefaultIndexSet() {
		Mockito.when(typeInfo.getType()).thenReturn(SearchableBeanWithESDocumentTypeAndIndexAnnotation.class);
		Mockito.when(elasticSearchClientFactory.getDefaultIndexName()).thenReturn("defaultIndexName");

		SimpleElasticSearchPersistentEntity<SimpleElasticSearchPersistentProperty> elasticSearchPersistentEntity = new SimpleElasticSearchPersistentEntity<SimpleElasticSearchPersistentProperty>(
				typeInfo, elasticSearchClientFactory);

		assertNotNull(elasticSearchPersistentEntity.getTypeName());
		assertNotNull(elasticSearchPersistentEntity.getIndexName());
		assertThat(elasticSearchPersistentEntity.getTypeName(), is("intexTypeName"));
		assertThat(elasticSearchPersistentEntity.getIndexName(), is("indexName"));
	}

}
