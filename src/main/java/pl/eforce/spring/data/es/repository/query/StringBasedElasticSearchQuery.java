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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.repository.query.RepositoryQuery;

import pl.eforce.spring.data.es.core.ElasticSearchOperations;
import pl.eforce.spring.data.es.core.convert.GeoConverters;
import pl.eforce.spring.data.es.core.geo.Distance;
import pl.eforce.spring.data.es.core.geo.GeoLocation;
import pl.eforce.spring.data.es.core.query.Query;
import pl.eforce.spring.data.es.core.query.SimpleQuery;
import pl.eforce.spring.data.es.core.query.SimpleStringCriteria;

/**
 * ElasticSearch specific implementation of {@link RepositoryQuery} that can
 * handle string based queries
 * 
 * @author Patryk Wasik
 */
public class StringBasedElasticSearchQuery extends AbstractElasticSearchQuery {

	private static final Pattern PARAMETER_PLACEHOLDER = Pattern.compile("\\?(\\d+)");

	private final GenericConversionService conversionService = new GenericConversionService();
	private final String rawQueryString;

	{
		if (!conversionService.canConvert(GeoLocation.class, String.class)) {
			conversionService.addConverter(GeoConverters.GeoLocationToStringConverter.INSTANCE);
		}
		if (!conversionService.canConvert(Distance.class, String.class)) {
			conversionService.addConverter(GeoConverters.DistanceToStringConverter.INSTANCE);
		}
	}

	public StringBasedElasticSearchQuery(ElasticSearchQueryMethod method, ElasticSearchOperations solrOperations) {
		this(method.getAnnotatedQuery(), method, solrOperations);
	}

	public StringBasedElasticSearchQuery(String query, ElasticSearchQueryMethod queryMethod, ElasticSearchOperations solrOperations) {
		super(solrOperations, queryMethod);
		rawQueryString = query;
	}

	@Override
	protected Query createQuery(ElasticSearchParameterAccessor parameterAccessor) {
		String queryString = replacePlaceholders(rawQueryString, parameterAccessor);

		return new SimpleQuery(new SimpleStringCriteria(queryString));
	}

	private String getParameterWithIndex(ElasticSearchParameterAccessor accessor, int index) {

		Object parameter = accessor.getBindableValue(index);

		if (parameter == null) {
			return "null";
		}

		if (conversionService.canConvert(parameter.getClass(), String.class)) {
			return conversionService.convert(parameter, String.class);
		}

		return parameter.toString();
	}

	private String replacePlaceholders(String input, ElasticSearchParameterAccessor accessor) {

		Matcher matcher = PARAMETER_PLACEHOLDER.matcher(input);
		String result = input;

		while (matcher.find()) {
			String group = matcher.group();
			int index = Integer.parseInt(matcher.group(1));
			result = result.replace(group, getParameterWithIndex(accessor, index));
		}
		return result;
	}
}
