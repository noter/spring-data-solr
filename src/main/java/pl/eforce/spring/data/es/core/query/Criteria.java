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

import static org.elasticsearch.index.query.FilterBuilders.boolFilter;
import static org.elasticsearch.index.query.FilterBuilders.inFilter;
import static org.elasticsearch.index.query.FilterBuilders.notFilter;
import static org.elasticsearch.index.query.FilterBuilders.queryFilter;
import static org.elasticsearch.index.query.FilterBuilders.rangeFilter;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.fieldQuery;
import static org.elasticsearch.index.query.QueryBuilders.filteredQuery;
import static org.elasticsearch.index.query.QueryBuilders.fuzzyQuery;
import static org.elasticsearch.index.query.QueryBuilders.inQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryString;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FuzzyQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.RangeFilterBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.util.Assert;

import pl.eforce.spring.data.es.core.geo.Distance;
import pl.eforce.spring.data.es.core.geo.GeoLocation;

/**
 * Criteria is the central class when constructing queries. It follows more or less a fluent API style, which allows to easily chain together multiple
 * criteria.
 * 
 * @author Patryk Wasik
 */
public class Criteria implements QueryHolder {

	public enum ConjunctionOperator {
		AND, AND_SUBCRITERIA, FIRST, OR, OR_SUBCRITERIA
	}

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
		BETWEEN, CONTAINS, ENDS_WITH, EQUALS, EXPRESSION, FUZZY, IN, NEAR, STARTS_WITH;
	}

	public static final String CRITERIA_VALUE_SEPERATOR = " ";

	public static final String WILDCARD = "*";
	private static final String DOUBLEQUOTE = "\"";

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

	final Set<CriteriaEntry> criteriaEntries = new LinkedHashSet<CriteriaEntry>();

	private float boost = Float.NaN;

	private LinkedHashMap<Criteria, ConjunctionOperator> criteriaChain = new LinkedHashMap<Criteria, ConjunctionOperator>();

	private Field field;

	private boolean negating = false;

	public Criteria() {
		criteriaChain.put(this, ConjunctionOperator.FIRST);
	}

	/**
	 * Creates a new Criteria for the given field
	 * 
	 * @param field
	 */
	public Criteria(Field field) {
		this();
		Assert.notNull(field, "Field for criteria must not be null");
		Assert.hasText(field.getName(), "Field.name for criteria must not be null/empty");

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

	protected Criteria(LinkedHashMap<Criteria, ConjunctionOperator> criteriaChain, Field field) {
		Assert.notNull(criteriaChain, "CriteriaChain must not be null");
		Assert.notNull(field, "Field for criteria must not be null");
		Assert.hasText(field.getName(), "Field.name for criteria must not be null/empty");

		this.criteriaChain = criteriaChain;
		this.field = field;
	}

	/**
	 * Chain using {@code AND}
	 * 
	 * @param field
	 * @return
	 */
	public Criteria and(Criteria criteria) {
		criteriaChain.put(criteria, ConjunctionOperator.AND_SUBCRITERIA);
		return this;
	}

	/**
	 * Chain using {@code AND}
	 * 
	 * @param field
	 * @return
	 */
	public Criteria and(Criteria... criterias) {
		for (Criteria criteria : criterias) {
			criteriaChain.put(criteria, ConjunctionOperator.AND_SUBCRITERIA);
		}
		return this;
	}

	/**
	 * Chain using {@code AND}
	 * 
	 * @param field
	 * @return
	 */
	public Criteria and(Field field) {
		Criteria criteria = new Criteria(criteriaChain, field);
		criteriaChain.put(criteria, ConjunctionOperator.AND);
		return criteria;
	}

	/**
	 * Chain using {@code AND}
	 * 
	 * @param field
	 * @return
	 */
	public Criteria and(String fieldname) {
		return and(new SimpleField(fieldname));
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

		criteriaEntries.add(new CriteriaEntry(OperationKey.BETWEEN, new Object[] { lowerBound, upperBound }));
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
	 * <strong>NOTE: </strong> mind your schema as leading wildcards may not be supported and/or execution might be slow.
	 * 
	 * @param o
	 * @return
	 */
	public Criteria contains(String s) {
		assertNoBlankInWildcardedQuery(s, true, true);
		criteriaEntries.add(new CriteriaEntry(OperationKey.CONTAINS, s));
		return this;
	}

	/**
	 * Crates new CriteriaEntry with leading wildcard <br />
	 * <strong>NOTE: </strong> mind your schema and execution times as leading wildcards may not be supported.
	 * 
	 * @param o
	 * @return
	 */
	public Criteria endsWith(String s) {
		assertNoBlankInWildcardedQuery(s, false, true);
		criteriaEntries.add(new CriteriaEntry(OperationKey.ENDS_WITH, s));
		return this;
	}

	/**
	 * Crates new CriteriaEntry allowing native es expressions
	 * 
	 * @param o
	 * @return
	 */
	public Criteria expression(String s) {
		criteriaEntries.add(new CriteriaEntry(OperationKey.EXPRESSION, s));
		return this;
	}

	/**
	 * Crates new CriteriaEntry with trailing ~
	 * 
	 * @param s
	 * @return
	 */
	public Criteria fuzzy(String s) {
		return fuzzy(s, null);
	}

	/**
	 * Crates new CriteriaEntry with trailing ~ followed by levensteinDistance
	 * 
	 * @param s
	 * @param levenshteinDistance
	 * @return
	 */
	public Criteria fuzzy(String s, String minSimilarity) {
		criteriaEntries.add(new FuzzyCriteriaEntry(OperationKey.FUZZY, s, minSimilarity));
		return this;
	}

	public LinkedHashMap<Criteria, ConjunctionOperator> getCriteriaChain() {
		return criteriaChain;
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
		BoolFilterBuilder boolFilterBuilder = boolFilter();
		FilterBuilder first = null;
		for (Entry<Criteria, ConjunctionOperator> entry : criteriaChain.entrySet()) {
			if (ConjunctionOperator.FIRST.equals(entry.getValue())) {
				first = entry.getKey().constructFilterBuilder();
			} else if (ConjunctionOperator.AND.equals(entry.getValue())) {
				if (first != null) {
					boolFilterBuilder.must(first);
					first = null;
				}
				boolFilterBuilder.must(entry.getKey().constructFilterBuilder());
			} else if (ConjunctionOperator.OR.equals(entry.getValue())) {
				if (first != null) {
					boolFilterBuilder.should(first);
					first = null;
				}
				boolFilterBuilder.should(entry.getKey().constructFilterBuilder());
			} else if (ConjunctionOperator.AND_SUBCRITERIA.equals(entry.getValue())) {
				if (first != null) {
					boolFilterBuilder.must(first);
					first = null;
				}
				boolFilterBuilder.must(entry.getKey().getFilterBuilder());
			} else if (ConjunctionOperator.OR_SUBCRITERIA.equals(entry.getValue())) {
				if (first != null) {
					boolFilterBuilder.should(first);
					first = null;
				}
				boolFilterBuilder.should(entry.getKey().getFilterBuilder());
			}
		}
		if (first != null) {
			boolFilterBuilder.must(first);
		}
		return boolFilterBuilder;
	}

	@Override
	public QueryBuilder getQueryBuilder() {
		BoolQueryBuilder boolQueryBuilder = boolQuery();
		QueryBuilder first = null;
		for (Entry<Criteria, ConjunctionOperator> entry : criteriaChain.entrySet()) {
			if (ConjunctionOperator.FIRST.equals(entry.getValue())) {
				first = entry.getKey().constructQueryBuilder();
			} else if (ConjunctionOperator.AND.equals(entry.getValue())) {
				if (first != null) {
					boolQueryBuilder.must(first);
					first = null;
				}
				boolQueryBuilder.must(entry.getKey().constructQueryBuilder());
			} else if (ConjunctionOperator.OR.equals(entry.getValue())) {
				if (first != null) {
					boolQueryBuilder.should(first);
					first = null;
				}
				boolQueryBuilder.should(entry.getKey().constructQueryBuilder());
			} else if (ConjunctionOperator.AND_SUBCRITERIA.equals(entry.getValue())) {
				if (first != null) {
					boolQueryBuilder.must(first);
					first = null;
				}
				boolQueryBuilder.must(entry.getKey().getQueryBuilder());
			} else if (ConjunctionOperator.OR_SUBCRITERIA.equals(entry.getValue())) {
				if (first != null) {
					boolQueryBuilder.should(first);
					first = null;
				}
				boolQueryBuilder.should(entry.getKey().getQueryBuilder());
			}
		}
		// only one element
		if (first != null) {
			boolQueryBuilder.must(first);
		}
		return boolQueryBuilder;
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
		ArrayList<Object> arrayList = new ArrayList<Object>();
		for (Object value : values) {
			if (value instanceof Collection) {
				arrayList.addAll((Collection<? extends Object>) value);
			} else {
				arrayList.add(value);
			}
		}
		criteriaEntries.add(new CriteriaEntry(OperationKey.IN, arrayList));
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
		criteriaEntries.add(new CriteriaEntry(OperationKey.EQUALS, o));
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
		criteriaEntries.add(new CriteriaEntry(OperationKey.NEAR, new Object[] { location, distance != null ? distance : new Distance(0) }));
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
		criteriaChain.put(criteria, ConjunctionOperator.OR_SUBCRITERIA);
		return this;
	}

	/**
	 * Chain using {@code OR}
	 * 
	 * @param field
	 * @return
	 */
	public Criteria or(Field field) {
		Criteria criteria = new Criteria(criteriaChain, field);
		criteriaChain.put(criteria, ConjunctionOperator.OR);
		return criteria;
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
		criteriaEntries.add(new CriteriaEntry(OperationKey.STARTS_WITH, s));
		return this;
	}

	protected FilterBuilder constructFieldFilter() {
		AndFilterBuilder filterBuilder = FilterBuilders.andFilter();
		String field = this.field.getName();
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
				FuzzyQueryBuilder fuzzyQuery2 = fuzzyQuery(field, escapeCriteriaValue(criteriaEntry.value.toString()));
				FuzzyCriteriaEntry fuzzyCriteriaEntry = (FuzzyCriteriaEntry) criteriaEntry;
				if (fuzzyCriteriaEntry.minSimilarity != null) {
					fuzzyQuery2.minSimilarity(fuzzyCriteriaEntry.minSimilarity);
				}

				filterBuilder.add(queryFilter(fuzzyQuery2));
				break;
			case EXPRESSION:
				filterBuilder.add(queryFilter(queryString(criteriaEntry.value.toString()).defaultField(field)));
				break;
			case NEAR:
				Object[] objects2 = (Object[]) criteriaEntry.value;
				GeoLocation location = (GeoLocation) objects2[0];
				Distance distance = (Distance) objects2[1];
				GeoDistanceFilterBuilder geoDistanceFilterBuilder = FilterBuilders.geoDistanceFilter(field).lat(location.getLatitude())
						.lon(location.getLongitude());
				if (distance != null) {
					geoDistanceFilterBuilder.distance(distance.getValue(), DistanceUnit.KILOMETERS);
				}
				filterBuilder.add(geoDistanceFilterBuilder);
				break;
			case IN:
				filterBuilder.add(inFilter(field, ((List<Object>) criteriaEntry.value).toArray()));
				break;
			default:
				break;
			}

		}
		return filterBuilder;
	}

	protected BoolQueryBuilder constructFieldQuery() {
		BoolQueryBuilder boolQueryBuilder = boolQuery();
		String field = this.field.getName();
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
				FuzzyQueryBuilder fuzzyQuery2 = fuzzyQuery(field, escapeCriteriaValue(criteriaEntry.value.toString()));
				FuzzyCriteriaEntry fuzzyCriteriaEntry = (FuzzyCriteriaEntry) criteriaEntry;
				if (fuzzyCriteriaEntry.minSimilarity != null) {
					fuzzyQuery2.minSimilarity(fuzzyCriteriaEntry.minSimilarity);
				}
				boolQueryBuilder.must(fuzzyQuery2);
				break;
			case EXPRESSION:
				boolQueryBuilder.must(queryString(criteriaEntry.value.toString()).defaultField(field));
				break;
			case NEAR:
				Object[] objects2 = (Object[]) criteriaEntry.value;
				GeoLocation location = (GeoLocation) objects2[0];
				Distance distance = (Distance) objects2[1];
				GeoDistanceFilterBuilder geoDistanceFilterBuilder = FilterBuilders.geoDistanceFilter(field).lat(location.getLatitude())
						.lon(location.getLongitude());
				if (distance != null) {
					geoDistanceFilterBuilder.distance(distance.getValue(), DistanceUnit.KILOMETERS);
				}

				boolQueryBuilder.must(filteredQuery(matchAllQuery(), geoDistanceFilterBuilder));
				break;
			case IN:
				boolQueryBuilder.must(inQuery(field, ((List<Object>) criteriaEntry.value).toArray()));
				break;
			default:
				break;
			}
		}
		return boolQueryBuilder;
	}

	private void assertNoBlankInWildcardedQuery(String searchString, boolean leadingWildcard, boolean trailingWildcard) {
		if (StringUtils.contains(searchString, CRITERIA_VALUE_SEPERATOR)) {
			throw new InvalidDataAccessApiUsageException("Cannot constructQuery '" + (leadingWildcard ? "*" : "") + "\"" + searchString + "\""
					+ (trailingWildcard ? "*" : "") + "'. Use epxression or mulitple clauses instead.");
		}
	}

	private FilterBuilder constructFilterBuilder() {
		FilterBuilder filterBuilder = constructFieldFilter();
		if (negating) {
			filterBuilder = notFilter(filterBuilder);
		}

		return filterBuilder;
	}

	private QueryBuilder constructQueryBuilder() {
		BoolQueryBuilder queryBuilder = constructFieldQuery();
		if (!Float.isNaN(boost)) {
			queryBuilder.boost(boost);
		}
		if (negating) {
			queryBuilder = boolQuery().mustNot(queryBuilder);
		}

		return queryBuilder;
	}

	private String escapeCriteriaValue(String criteriaValue) {
		return StringUtils.replaceEach(criteriaValue, RESERVED_CHARS, RESERVED_CHARS_REPLACEMENT);
	}

}
