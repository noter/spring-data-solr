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
package pl.eforce.spring.data.es.repository.support;

import org.apache.solr.client.solrj.beans.Field;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.ClassTypeInformation;

import pl.eforce.spring.data.es.core.mapping.SimpleSolrPersistentProperty;
import pl.eforce.spring.data.es.core.mapping.SolrPersistentEntity;
import pl.eforce.spring.data.es.repository.ProductBean;
import pl.eforce.spring.data.es.repository.query.SolrEntityInformation;
import pl.eforce.spring.data.es.repository.support.MappingSolrEntityInformation;

/**
 * @author Christoph Strobl
 */
@RunWith(MockitoJUnitRunner.class)
public class MappingSolrEntityInformationTest {

	private static final String PRODUCT_BEAN_SIMPLE_NAME = "productbean";

	@Mock
	private SolrPersistentEntity<ProductBean> persistentEntity;

	@Mock
	private SolrPersistentEntity<ProductBeanWithAlternateFieldNameForId> persistentEntityWihtAlternateFieldNameForId;

	@Before
	public void setUp() {
		Mockito.when(persistentEntity.getType()).thenReturn(ProductBean.class);
		Mockito.when(persistentEntity.getSolrCoreName()).thenReturn(PRODUCT_BEAN_SIMPLE_NAME);

		Mockito.when(persistentEntityWihtAlternateFieldNameForId.getType()).thenReturn(
				ProductBeanWithAlternateFieldNameForId.class);
		Mockito.when(persistentEntityWihtAlternateFieldNameForId.getSolrCoreName()).thenReturn(PRODUCT_BEAN_SIMPLE_NAME);
	}

	@Test
	public void testSolrCoreRetrievalWhenNotExplicitlySet() {
		SolrEntityInformation<ProductBean, String> entityInformation = new MappingSolrEntityInformation<ProductBean, String>(
				persistentEntity);
		Assert.assertEquals(PRODUCT_BEAN_SIMPLE_NAME, entityInformation.getSolrCoreName());
	}

	@Test
	public void testSolrCoreRetrievalWhenSet() {
		final String coreName = "core1";
		SolrEntityInformation<ProductBean, String> entityInformation = new MappingSolrEntityInformation<ProductBean, String>(
				persistentEntity, coreName);
		Assert.assertEquals(coreName, entityInformation.getSolrCoreName());
	}

	@Test
	public void testIdType() {
		SolrEntityInformation<ProductBean, String> entityInformation = new MappingSolrEntityInformation<ProductBean, String>(
				persistentEntity);
		Assert.assertEquals(String.class, entityInformation.getIdType());
	}

	@Test
	public void testGetIdAttribute() throws NoSuchFieldException, SecurityException {
		Mockito.when(persistentEntity.getTypeInformation()).thenReturn(ClassTypeInformation.from(ProductBean.class));
		SimpleSolrPersistentProperty property = new SimpleSolrPersistentProperty(ProductBean.class.getDeclaredField("id"),
				null, persistentEntity, new SimpleTypeHolder());
		Mockito.when(persistentEntity.getIdProperty()).thenReturn(property);

		SolrEntityInformation<ProductBean, String> entityInformation = new MappingSolrEntityInformation<ProductBean, String>(
				persistentEntity);
		Assert.assertEquals("id", entityInformation.getIdAttribute());
	}

	@Test
	public void testGetIdAttributeForAlternateFieldName() throws NoSuchFieldException, SecurityException {
		Mockito.when(persistentEntityWihtAlternateFieldNameForId.getTypeInformation()).thenReturn(
				ClassTypeInformation.from(ProductBeanWithAlternateFieldNameForId.class));

		SimpleSolrPersistentProperty property = new SimpleSolrPersistentProperty(
				ProductBeanWithAlternateFieldNameForId.class.getDeclaredField("productId"), null,
				persistentEntityWihtAlternateFieldNameForId, new SimpleTypeHolder());
		Mockito.when(persistentEntityWihtAlternateFieldNameForId.getIdProperty()).thenReturn(property);

		SolrEntityInformation<ProductBeanWithAlternateFieldNameForId, String> entityInformation = new MappingSolrEntityInformation<ProductBeanWithAlternateFieldNameForId, String>(
				persistentEntityWihtAlternateFieldNameForId);
		Assert.assertEquals("product_id", entityInformation.getIdAttribute());
	}

	class ProductBeanWithAlternateFieldNameForId {

		@Id
		@Field("product_id")
		private String productId;

		public String getProductId() {
			return productId;
		}

		public void setProductId(String productId) {
			this.productId = productId;
		}

	}
}
