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
package org.springframework.data.es.core;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.rest.RestStatus;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;

/**
 * @author Patryk Wasik
 * 
 */
public class ESExceptionTranslator implements PersistenceExceptionTranslator {

	@Override
	public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
		if (ex.getCause() instanceof ElasticSearchException) {
			ElasticSearchException esServerException = (ElasticSearchException) ex.getCause();

			RestStatus status = esServerException.status();

			switch (status) {
			case NOT_FOUND:
			case SERVICE_UNAVAILABLE:
			case INTERNAL_SERVER_ERROR:
				return new DataAccessResourceFailureException(esServerException.getMessage(), esServerException);
			case FORBIDDEN:
			case UNAUTHORIZED:
				return new PermissionDeniedDataAccessException(esServerException.getMessage(), esServerException);
			case BAD_REQUEST:
				return new InvalidDataAccessApiUsageException(esServerException.getMessage(), esServerException);
			default:
				break;
			}
		}
		return null;
	}
}
