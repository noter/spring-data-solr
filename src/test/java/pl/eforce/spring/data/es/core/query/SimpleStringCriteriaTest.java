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
package pl.eforce.spring.data.es.core.query;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import pl.eforce.spring.data.es.core.query.Criteria;
import pl.eforce.spring.data.es.core.query.SimpleStringCriteria;

/**
 * @author Patryk Wasik
 */
public class SimpleStringCriteriaTest {

	@Test
	public void testStringCriteria() {
		Criteria criteria = new SimpleStringCriteria("field_1:value_1 AND field_2:value_2");
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.query_string.query", is("field_1:value_1 AND field_2:value_2"));
	}

	@Test
	public void testStringCriteriaWithMoreFragments() {
		Criteria criteria = new SimpleStringCriteria("field_1:value_1 AND field_2:value_2");
		criteria = criteria.and("field_3").is("value_3");
		System.out.println(criteria.getQueryBuilder().toString());

	}
}
