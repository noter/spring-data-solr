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

import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.es.core.query.ESDataQuery;
import org.springframework.data.es.core.query.FacetOptions;
import org.springframework.data.es.core.query.FacetQuery;
import org.springframework.data.es.core.query.Field;
import org.springframework.data.es.core.query.FilterQuery;
import org.springframework.data.es.core.query.Query;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * The QueryParser takes a spring-data-es Query and returns a
 * {@link SearchRequestBuilder}. All Query parameters are translated into the
 * according SearchRequestBuilder fields.
 * 
 * @author Christoph Strobl
 */
public class QueryParser {

	/**
	 * Convert given Query into a SolrQuery executable via {@link SolrServer}
	 * 
	 * @param query
	 * @return
	 */
	public final SearchRequestBuilder constructESSearchQuery(ESDataQuery query, Client client) {
		Assert.notNull(query, "Cannot construct solrQuery from null value.");
		Assert.notNull(query.getCriteria(), "Query has to have a criteria.");

		SearchRequestBuilder esSearchRequestBuilder = client.prepareSearch();

		QueryBuilder builder = getESQuery(query);
		if (builder != null) {
			esSearchRequestBuilder.setQuery(builder);
		}

		if (query instanceof Query) {
			processQueryOptions(esSearchRequestBuilder, (Query) query);
		}
		if (query instanceof FacetQuery) {
			processFacetOptions(esSearchRequestBuilder, (FacetQuery) query);
		}
		return esSearchRequestBuilder;
	}

	/**
	 * Get ElstaicSearch {@link QueryBuilder} from given ESDataQuery
	 * 
	 * @param query
	 * @return
	 */
	public QueryBuilder getESQuery(ESDataQuery query) {
		if (query.getCriteria() == null) {
			return null;
		}
		return query.getCriteria().getQueryBuilder();
	}

	private void appendFacetingOnFields(SearchRequestBuilder searchRequestBuilder, FacetQuery query) {
		FacetOptions facetOptions = query.getFacetOptions();
		if ((facetOptions == null) || !facetOptions.hasFields()) {
			return;
		}
		for (Field field : facetOptions.getFacetOnFields()) {
			searchRequestBuilder.addFacet(FacetBuilders.termsFacet(field.getName()).size(facetOptions.getFacetLimit())
					.order(facetOptions.getFacetSort()));
		}
	}

	private void appendFilterQuery(SearchRequestBuilder searchRequestBuilder, List<FilterQuery> filterQueries) {
		if (CollectionUtils.isEmpty(filterQueries)) {
			return;
		}
		for (FilterQuery filterQuery : filterQueries) {
			searchRequestBuilder.setFilter(filterQuery.getCriteria().getFilterBuilder());
		}
	}

	private void appendPagination(SearchRequestBuilder query, Pageable pageable) {
		if (pageable == null) {
			return;
		}
		query.setFrom(pageable.getOffset());
		query.setSize(pageable.getPageSize());
	}

	private void appendProjectionOnFields(SearchRequestBuilder searchRequestBuilder, List<Field> fields) {
		if (CollectionUtils.isEmpty(fields)) {
			return;
		}
		for (Field field : fields) {
			searchRequestBuilder.addFacet(FacetBuilders.termsFacet(field.getName()));
		}
	}

	private void appendSort(SearchRequestBuilder searchRequestBuilder, Sort sort) {
		if (sort == null) {
			return;
		}
		for (Order order : sort) {
			searchRequestBuilder.addSort(order.getProperty(), order.getDirection().equals(Direction.ASC) ? SortOrder.ASC : SortOrder.DESC);
		}
	}

	private void processFacetOptions(SearchRequestBuilder searchRequestBuilder, FacetQuery query) {
		appendFacetingOnFields(searchRequestBuilder, query);
	}

	private void processQueryOptions(SearchRequestBuilder searchRequestBuilder, Query query) {
		appendPagination(searchRequestBuilder, query.getPageRequest());
		appendProjectionOnFields(searchRequestBuilder, query.getProjectionOnFields());
		appendFilterQuery(searchRequestBuilder, query.getFilterQueries());
		appendSort(searchRequestBuilder, query.getSort());
	}
}
