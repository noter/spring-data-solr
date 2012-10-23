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
package org.springframework.data.es.core.query;

import static org.elasticsearch.index.query.FilterBuilders.boolFilter;
import static org.elasticsearch.index.query.FilterBuilders.geoDistanceFilter;
import static org.elasticsearch.index.query.FilterBuilders.queryFilter;
import static org.elasticsearch.index.query.FilterBuilders.rangeFilter;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.fieldQuery;
import static org.elasticsearch.index.query.QueryBuilders.filteredQuery;
import static org.elasticsearch.index.query.QueryBuilders.fuzzyQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryString;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeFilterBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.es.core.geo.Distance;
import org.springframework.data.es.core.geo.GeoLocation;
import org.springframework.util.Assert;

/**
 * Criteria is the central class when constructing queries. It follows more or
 * less a fluent API style, which allows to easily chain together multiple
 * criteria.
 * 
 * @author Patryk Wasik
 */
public class Criteria implements QueryHolder {

	static class CriteriaEntry {

		private final OperationKey key;
		private final Object value;

		public CriteriaEntry(OperationKey key, Object value) {
			this.key = key;
			this.value = value;
		}

		public OperationKey getKey() {
			return key;
		}

		public Object getValue() {
			return value;
		}
	}

	static class FuzzyCriteriaEntry extends CriteriaEntry {

		private final String minSimilarity;

		public FuzzyCriteriaEntry(OperationKey key, Object value, String minSimilarity) {
			super(key, value);
			this.minSimilarity = minSimilarity;
		}

		public String getMinSimilarity() {
			return minSimilarity;
		}

	}

	enum OperationKey {
		BETWEEN, CONTAINS, ENDS_WITH, EQUALS, EXPRESSION, FUZZY, NEAR, STARTS_WITH;
	}

	static class OrCriteria extends Criteria {

		public OrCriteria() {
			super();
		}

		public OrCriteria(Field field) {
			super(field);
		}

		public OrCriteria(List<Criteria> criteriaChain, Field field) {
			super(criteriaChain, field);
		}

		public OrCriteria(List<Criteria> criteriaChain, String fieldname) {
			super(criteriaChain, fieldname);
		}

		public OrCriteria(String fieldname) {
			super(fieldname);
		}

		@Override
		public String getConjunctionOperator() {
			return OR_OPERATOR;
		}

	}

	public static final String CRITERIA_VALUE_SEPERATOR = " ";
	public static final String WILDCARD = "*";
	private static final String AND_OPERATOR = " AND ";
	private static final String DOUBLEQUOTE = "\"";
	private static final String OR_OPERATOR = " OR ";

	private static final String[] RESERVED_CHARS = { DOUBLEQUOTE, "+", "-", "&&", "||", "!", "(", ")", "{", "}", "[", "]", "^", "~", "*", "?", ":",
			"\\" };
	private static final String[] RESERVED_CHARS_REPLACEMENT = { "\\" + DOUBLEQUOTE, "\\+", "\\-", "\\&\\&", "\\|\\|", "\\!", "\\(", "\\)", "\\{",
			"\\}", "\\[", "\\]", "\\^", "\\~", "\\*", "\\?", "\\:", "\\\\" };

	/**
	 * Static factory method to create a new Criteria for provided field
	 * 
	 * @param field
	 * @return
	 */
	public static Criteria where(Field field) {
		return new Criteria(field);
	}

	/**
	 * Static factory method to create a new Criteria for field with given name
	 * 
	 * @param field
	 * @return
	 */
	public static Criteria where(String field) {
		return where(new SimpleField(field));
	}

	private float boost = Float.NaN;

	private final Set<CriteriaEntry> criteria = new LinkedHashSet<CriteriaEntry>();

	private final List<Criteria> criteriaChain = new ArrayList<Criteria>(1);

	private Field field;

	private boolean negating = false;

	public Criteria() {
	}

	/**
	 * Creates a new Criteria for the given field
	 * 
	 * @param field
	 */
	public Criteria(Field field) {
		Assert.notNull(field, "Field for criteria must not be null");
		Assert.hasText(field.getName(), "Field.name for criteria must not be null/empty");

		criteriaChain.add(this);
		this.field = field;
	}

	/**
	 * Creates a new Criteria for the Filed with provided name
	 * 
	 * @param fieldname
	 */
	public Criteria(String fieldname) {
		this(new SimpleField(fieldname));
	}

	protected Criteria(List<Criteria> criteriaChain, Field field) {
		Assert.notNull(criteriaChain, "CriteriaChain must not be null");
		Assert.notNull(field, "Field for criteria must not be null");
		Assert.hasText(field.getName(), "Field.name for criteria must not be null/empty");

		this.criteriaChain.addAll(criteriaChain);
		this.criteriaChain.add(this);
		this.field = field;
	}

	protected Criteria(List<Criteria> criteriaChain, String fieldname) {
		this(criteriaChain, new SimpleField(fieldname));
	}

	/**
	 * Chain using {@code AND}
	 * 
	 * @param field
	 * @return
	 */
	public Criteria and(Criteria criteria) {
		criteriaChain.add(criteria);
		return this;
	}

	/**
	 * Chain using {@code AND}
	 * 
	 * @param field
	 * @return
	 */
	public Criteria and(Criteria... criterias) {
		criteriaChain.addAll(Arrays.asList(criterias));
		return this;
	}

	/**
	 * Chain using {@code AND}
	 * 
	 * @param field
	 * @return
	 */
	public Criteria and(Field field) {
		return new Criteria(criteriaChain, field);
	}

	/**
	 * Chain using {@code AND}
	 * 
	 * @param field
	 * @return
	 */
	public Criteria and(String fieldname) {
		return new Criteria(criteriaChain, fieldname);
	}

	/**
	 * Crates new CriteriaEntry for {@code RANGE [lowerBound TO upperBound]}
	 * 
	 * @param lowerBound
	 * @param upperBound
	 * @return
	 */
	public Criteria between(Object lowerBound, Object upperBound) {
		if ((lowerBound == null) && (upperBound == null)) {
			throw new InvalidDataAccessApiUsageException("Range [* TO *] is not allowed");
		}

		criteria.add(new CriteriaEntry(OperationKey.BETWEEN, new Object[] { lowerBound, upperBound }));
		return this;
	}

	/**
	 * Boost positive hit with given factor. eg. ^2.3
	 * 
	 * @param boost
	 * @return
	 */
	public Criteria boost(float boost) {
		if (boost < 0) {
			throw new InvalidDataAccessApiUsageException("Boost must not be negative.");
		}
		this.boost = boost;
		return this;
	}

	/**
	 * Crates new CriteriaEntry with leading and trailing wildcards <br/>
	 * <strong>NOTE: </strong> mind your schema as leading wildcards may not be
	 * supported and/or execution might be slow.
	 * 
	 * @param o
	 * @return
	 */
	public Criteria contains(String s) {
		assertNoBlankInWildcardedQuery(s, true, true);
		criteria.add(new CriteriaEntry(OperationKey.CONTAINS, s));
		return this;
	}

	/**
	 * Crates new CriteriaEntry with leading wildcard <br />
	 * <strong>NOTE: </strong> mind your schema and execution times as leading
	 * wildcards may not be supported.
	 * 
	 * @param o
	 * @return
	 */
	public Criteria endsWith(String s) {
		assertNoBlankInWildcardedQuery(s, false, true);
		criteria.add(new CriteriaEntry(OperationKey.ENDS_WITH, s));
		return this;
	}

	/**
	 * Crates new CriteriaEntry allowing native es expressions
	 * 
	 * @param o
	 * @return
	 */
	public Criteria expression(String s) {
		criteria.add(new CriteriaEntry(OperationKey.EXPRESSION, s));
		return this;
	}

	/**
	 * Crates new CriteriaEntry with trailing ~
	 * 
	 * @param s
	 * @return
	 */
	public Criteria fuzzy(String s) {
		return fuzzy(s, "");
	}

	/**
	 * Crates new CriteriaEntry with trailing ~ followed by levensteinDistance
	 * 
	 * @param s
	 * @param levenshteinDistance
	 * @return
	 */
	public Criteria fuzzy(String s, String minSimilarity) {
		criteria.add(new FuzzyCriteriaEntry(OperationKey.FUZZY, s, minSimilarity));
		return this;
	}

	/**
	 * Conjunction to be used with this criteria (AND | OR)
	 * 
	 * @return
	 */
	public String getConjunctionOperator() {
		return AND_OPERATOR;
	}

	/**
	 * Field targeted by this Criteria
	 * 
	 * @return
	 */
	public Field getField() {
		return field;
	}

	@Override
	public FilterBuilder getFilterBuilder() {
		BoolFilterBuilder builder = boolFilter();
		FilterBuilder constructFieldQuery = constructFieldFilter(field.getName(), criteria);
		if (negating) {
			builder.mustNot(constructFieldQuery);
		} else {
			builder.must(constructFieldQuery);
		}
		for (Criteria criteria : criteriaChain) {
			if (criteria.getConjunctionOperator().equals(AND_OPERATOR)) {
				if (negating) {
					builder.mustNot(criteria.getFilterBuilder());
				} else {
					builder.must(criteria.getFilterBuilder());
				}
			} else {
				if (negating) {
					builder.should(criteria.getFilterBuilder());
				} else {
					builder.should(criteria.getFilterBuilder());
				}
			}

		}
		return builder;
	}

	@Override
	public QueryBuilder getQueryBuilder() {
		BoolQueryBuilder builder = boolQuery();
		QueryBuilder constructFieldQuery = constructFieldQuery(field.getName(), criteria);
		if (!Float.isNaN(boost)) {
			builder.boost(boost);
		}
		if (negating) {
			builder.mustNot(constructFieldQuery);
		} else {
			builder.must(constructFieldQuery);
		}
		for (Criteria criteria : criteriaChain) {
			if (criteria.getConjunctionOperator().equals(AND_OPERATOR)) {
				if (negating) {
					builder.mustNot(criteria.getQueryBuilder());
				} else {
					builder.must(criteria.getQueryBuilder());
				}
			} else {
				if (negating) {
					builder.should(criteria.getQueryBuilder());
				} else {
					builder.should(criteria.getQueryBuilder());
				}
			}

		}
		return builder;
	}

	/**
	 * Crates new CriteriaEntry for {@code RANGE [lowerBound TO *]}
	 * 
	 * @param lowerBound
	 * @return
	 */
	public Criteria greaterThanEqual(Object lowerBound) {
		between(lowerBound, null);
		return this;
	}

	/**
	 * Crates new CriteriaEntry for multiple values {@code (arg0 arg1 arg2 ...)}
	 * 
	 * @param c
	 *            the collection containing the values to match against
	 * @return
	 */
	public Criteria in(Iterable<?> values) {
		Assert.notNull(values, "Collection of 'in' values must not be null");
		for (Object value : values) {
			if (value instanceof Collection) {
				in((Collection<?>) value);
			} else {
				is(value);
			}
		}
		return this;
	}

	/**
	 * Crates new CriteriaEntry for multiple values {@code (arg0 arg1 arg2 ...)}
	 * 
	 * @param lowerBound
	 * @return
	 */
	public Criteria in(Object... values) {
		if ((values.length == 0) || ((values.length > 1) && (values[1] instanceof Collection))) {
			throw new InvalidDataAccessApiUsageException("At least one element "
					+ (values.length > 0 ? ("of argument of type " + values[1].getClass().getName()) : "") + " has to be present.");
		}
		return in(Arrays.asList(values));
	}

	/**
	 * Crates new CriteriaEntry without any wildcards
	 * 
	 * @param o
	 * @return
	 */
	public Criteria is(Object o) {
		criteria.add(new CriteriaEntry(OperationKey.EQUALS, o));
		return this;
	}

	/**
	 * Crates new CriteriaEntry for {@code RANGE [* TO upperBound]}
	 * 
	 * @param upperBound
	 * @return
	 */
	public Criteria lessThanEqual(Object upperBound) {
		between(null, upperBound);
		return this;
	}

	/**
	 * Creates new CriteriaEntry for {@code !geodist}
	 * 
	 * @param location
	 *            Geolocation in degrees
	 * @param distance
	 * @return
	 */
	public Criteria near(GeoLocation location, Distance distance) {
		Assert.notNull(location);
		if (distance != null) {
			if (distance.getValue() < 0) {
				throw new InvalidDataAccessApiUsageException("distance must not be negative.");
			}
		}
		criteria.add(new CriteriaEntry(OperationKey.NEAR, new Object[] { location, distance != null ? distance : new Distance(0) }));
		return this;
	}

	/**
	 * Crates new CriteriaEntry with trailing -
	 * 
	 * @param s
	 * @return
	 */
	public Criteria not() {
		negating = true;
		return this;
	}

	/**
	 * Chain using {@code OR}
	 * 
	 * @param field
	 * @return
	 */
	public Criteria or(Criteria criteria) {
		Assert.notNull(criteria, "Cannot chain 'null' criteria.");

		Criteria orConnectedCritiera = new OrCriteria(criteriaChain, criteria.getField());
		orConnectedCritiera.criteria.addAll(criteria.criteria);
		return orConnectedCritiera;
	}

	/**
	 * Chain using {@code OR}
	 * 
	 * @param field
	 * @return
	 */
	public Criteria or(Field field) {
		return new OrCriteria(criteriaChain, field);
	}

	/**
	 * Chain using {@code OR}
	 * 
	 * @param field
	 * @return
	 */
	public Criteria or(String fieldname) {
		return or(new SimpleField(fieldname));
	}

	/**
	 * Crates new CriteriaEntry with trailing wildcard
	 * 
	 * @param o
	 * @return
	 */
	public Criteria startsWith(String s) {
		assertNoBlankInWildcardedQuery(s, true, false);
		criteria.add(new CriteriaEntry(OperationKey.STARTS_WITH, s));
		return this;
	}

	List<Criteria> getCriteriaChain() {
		return criteriaChain;
	}

	private void assertNoBlankInWildcardedQuery(String searchString, boolean leadingWildcard, boolean trailingWildcard) {
		if (StringUtils.contains(searchString, CRITERIA_VALUE_SEPERATOR)) {
			throw new InvalidDataAccessApiUsageException("Cannot constructQuery '" + (leadingWildcard ? "*" : "") + "\"" + searchString + "\""
					+ (trailingWildcard ? "*" : "") + "'. Use epxression or mulitple clauses instead.");
		}
	}

	private FilterBuilder constructFieldFilter(String field, Set<CriteriaEntry> criteriaEntries) {
		AndFilterBuilder filterBuilder = FilterBuilders.andFilter();
		for (CriteriaEntry criteriaEntry : criteriaEntries) {
			switch (criteriaEntry.key) {
			case EQUALS:
				filterBuilder.add(queryFilter(fieldQuery(field, criteriaEntry.value)));
				break;

			case BETWEEN:
				Object[] objects = (Object[]) criteriaEntry.value;
				RangeFilterBuilder rangeQuery2 = rangeFilter(field);
				if (objects[0] != null) {
					rangeQuery2.from(objects[0]);
				}
				if (objects[1] != null) {
					rangeQuery2.to(objects[1]);
				}
				filterBuilder.add(rangeQuery2);
				break;

			case CONTAINS:
				filterBuilder.add(queryFilter(fieldQuery(field, WILDCARD + escapeCriteriaValue(criteriaEntry.value.toString()) + WILDCARD)
						.analyzeWildcard(true)));
				break;
			case ENDS_WITH:
				filterBuilder
						.add(queryFilter(fieldQuery(field, WILDCARD + escapeCriteriaValue(criteriaEntry.value.toString())).analyzeWildcard(true)));
				break;
			case STARTS_WITH:
				filterBuilder
						.add(queryFilter(fieldQuery(field, escapeCriteriaValue(criteriaEntry.value.toString()) + WILDCARD).analyzeWildcard(true)));
				break;
			case FUZZY:
				filterBuilder.add(queryFilter(fuzzyQuery(field, escapeCriteriaValue(criteriaEntry.value.toString())).minSimilarity(
						((FuzzyCriteriaEntry) criteriaEntry).getMinSimilarity())));
				break;
			case EXPRESSION:
				filterBuilder.add(queryFilter(queryString(criteriaEntry.value.toString()).defaultField(field)));
				break;
			case NEAR:
				Object[] objects2 = (Object[]) criteriaEntry.value;
				GeoLocation location = (GeoLocation) objects2[0];
				Distance distance = (Distance) objects2[1];
				filterBuilder.add(geoDistanceFilter(field).lat(location.getLatitude()).lon(location.getLongitude())
						.distance(distance.getValue(), DistanceUnit.KILOMETERS));
				break;

			default:
				break;
			}

		}
		return filterBuilder;
	}

	private QueryBuilder constructFieldQuery(String field, Set<CriteriaEntry> criteriaEntries) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		for (CriteriaEntry criteriaEntry : criteriaEntries) {
			switch (criteriaEntry.key) {
			case EQUALS:
				boolQueryBuilder.must(fieldQuery(field, criteriaEntry.value));
				break;

			case BETWEEN:
				Object[] objects = (Object[]) criteriaEntry.value;
				RangeQueryBuilder rangeQuery2 = rangeQuery(field);
				if (objects[0] != null) {
					rangeQuery2.from(objects[0]);
				}
				if (objects[1] != null) {
					rangeQuery2.to(objects[1]);
				}
				boolQueryBuilder.must(rangeQuery2);
				break;

			case CONTAINS:
				boolQueryBuilder.must(fieldQuery(field, WILDCARD + escapeCriteriaValue(criteriaEntry.value.toString()) + WILDCARD).analyzeWildcard(
						true));
				break;
			case ENDS_WITH:
				boolQueryBuilder.must(fieldQuery(field, WILDCARD + escapeCriteriaValue(criteriaEntry.value.toString())).analyzeWildcard(true));
				break;
			case STARTS_WITH:
				boolQueryBuilder.must(fieldQuery(field, escapeCriteriaValue(criteriaEntry.value.toString()) + WILDCARD).analyzeWildcard(true));
				break;
			case FUZZY:
				boolQueryBuilder.must(fuzzyQuery(field, escapeCriteriaValue(criteriaEntry.value.toString())).minSimilarity(
						((FuzzyCriteriaEntry) criteriaEntry).getMinSimilarity()));
				break;
			case EXPRESSION:
				boolQueryBuilder.must(queryString(criteriaEntry.value.toString()).defaultField(field));
				break;
			case NEAR:
				Object[] objects2 = (Object[]) criteriaEntry.value;
				GeoLocation location = (GeoLocation) objects2[0];
				Distance distance = (Distance) objects2[1];
				boolQueryBuilder.must(filteredQuery(
						matchAllQuery(),
						FilterBuilders.geoDistanceFilter(field).lat(location.getLatitude()).lon(location.getLongitude())
								.distance(distance.getValue(), DistanceUnit.KILOMETERS)));
				break;

			default:
				break;
			}

		}
		return boolQueryBuilder;
	}

	private String escapeCriteriaValue(String criteriaValue) {
		return StringUtils.replaceEach(criteriaValue, RESERVED_CHARS, RESERVED_CHARS_REPLACEMENT);
	}

}
