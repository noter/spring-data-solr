package org.springframework.data.es.core.query;

import static org.elasticsearch.index.query.FilterBuilders.matchAllFilter;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;

public class MatchAllCriteria extends Criteria {

	@Override
	public FilterBuilder getFilterBuilder() {
		return matchAllFilter();
	}

	@Override
	public QueryBuilder getQueryBuilder() {
		return matchAllQuery();
	}

}
