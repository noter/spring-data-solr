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
package pl.eforce.spring.data.es.core.convert;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import pl.eforce.es.orm.mapping.ESField;
import pl.eforce.spring.data.es.ElasticSearchClientFactory;
import pl.eforce.spring.data.es.JsonTestUtils;
import pl.eforce.spring.data.es.core.convert.MappingElasticSearchConverter;
import pl.eforce.spring.data.es.core.mapping.SimpleElasticSearchMappingContext;

/**
 * @author Patryk Wasik
 */
@RunWith(MockitoJUnitRunner.class)
public class MappingSolrConverterTest {

	public static class ConvertableBean {

		@ESField
		Integer intProperty;

		@ESField("indexStringProperty")
		String stringProperty;

		public ConvertableBean() {
		}

		public ConvertableBean(String stringProperty, Integer intProperty) {
			super();
			this.stringProperty = stringProperty;
			this.intProperty = intProperty;
		}

		public Integer getIntProperty() {
			return intProperty;
		}

		public String getStringProperty() {
			return stringProperty;
		}

		public void setIntProperty(Integer intProperty) {
			this.intProperty = intProperty;
		}

		public void setStringProperty(String stringProperty) {
			this.stringProperty = stringProperty;
		}

	}

	@Mock
	ElasticSearchClientFactory elasticSearchClientFactory;

	private MappingElasticSearchConverter converter;

	@Test
	public void read() {
		ConvertableBean bean = converter.read(ConvertableBean.class, new StringBuilder("{\"intProperty\":321,\"indexStringProperty\":\"Test123\"}"));

		assertThat(bean.intProperty, is(321));
		assertThat(bean.stringProperty, is("Test123"));

	}

	@Before
	public void setUp() {
		converter = new MappingElasticSearchConverter(new SimpleElasticSearchMappingContext(elasticSearchClientFactory));
	}

	@Test
	public void write() {
		ConvertableBean bean = new ConvertableBean("Test123", 321);
		StringBuilder builder = new StringBuilder();
		converter.write(bean, builder);

		assertThat(JsonTestUtils.toSingleLineString(builder.toString()), is("{intProperty:321,indexStringProperty:Test123}"));

	}

}
