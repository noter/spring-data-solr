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
package pl.eforce.spring.data.es.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.rest.RestStatus;
import org.junit.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.PermissionDeniedDataAccessException;

import pl.eforce.spring.data.es.core.ElasticSearchExceptionTranslator;

/**
 * @author Patryk Wasik
 * 
 */
public class ElasticSearchExceptionTranslatorTest {

	private final ElasticSearchExceptionTranslator exceptionTranslator = new ElasticSearchExceptionTranslator();

	@Test
	public void badRequest() {
		assertThat(exceptionTranslator.translateExceptionIfPossible(exceptionFor(RestStatus.BAD_REQUEST)),
				instanceOf(InvalidDataAccessApiUsageException.class));
	}

	@Test
	public void forbidden() {
		assertThat(exceptionTranslator.translateExceptionIfPossible(exceptionFor(RestStatus.FORBIDDEN)),
				instanceOf(PermissionDeniedDataAccessException.class));
	}

	@Test
	public void internalServerError() {
		assertThat(exceptionTranslator.translateExceptionIfPossible(exceptionFor(RestStatus.INTERNAL_SERVER_ERROR)),
				instanceOf(DataAccessResourceFailureException.class));
	}

	@Test
	public void notFound() {
		assertThat(exceptionTranslator.translateExceptionIfPossible(exceptionFor(RestStatus.NOT_FOUND)),
				instanceOf(DataAccessResourceFailureException.class));
	}

	@Test
	public void serviceUnavailable() {
		assertThat(exceptionTranslator.translateExceptionIfPossible(exceptionFor(RestStatus.SERVICE_UNAVAILABLE)),
				instanceOf(DataAccessResourceFailureException.class));
	}

	@Test
	public void unauthorized() {
		assertThat(exceptionTranslator.translateExceptionIfPossible(exceptionFor(RestStatus.UNAUTHORIZED)),
				instanceOf(PermissionDeniedDataAccessException.class));
	}

	private RuntimeException exceptionFor(final RestStatus restStatus) {
		return new RuntimeException(new ElasticSearchException("Exception") {
			@Override
			public RestStatus status() {
				return restStatus;
			}
		});
	}

}
