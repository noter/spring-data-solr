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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import pl.eforce.spring.data.es.core.geo.Distance;
import pl.eforce.spring.data.es.core.geo.GeoLocation;
import pl.eforce.spring.data.es.core.query.Criteria;
import pl.eforce.spring.data.es.core.query.Field;
import pl.eforce.spring.data.es.core.query.SimpleField;

import com.jayway.jsonpath.InvalidPathException;

/**
 * @author Patryk Wasik
 */
public class CriteriaTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testAnd() {
		Criteria criteria = new Criteria("field_1").startsWith("start").endsWith("end").and("field_2").startsWith("2start").endsWith("2end");
		Assert.assertEquals("field_2", criteria.getField().getName());
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must[0].bool.must[0].field.field_1.query", is("start*"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must[0].bool.must[0].field.field_1.analyze_wildcard", is(true));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must[0].bool.must[1].field.field_1.query", is("*end"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must[0].bool.must[1].field.field_1.analyze_wildcard", is(true));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must[1].bool.must[0].field.field_2.query", is("2start*"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must[1].bool.must[0].field.field_2.analyze_wildcard", is(true));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must[1].bool.must[1].field.field_2.query", is("*2end"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must[1].bool.must[1].field.field_2.analyze_wildcard", is(true));
	}

	@Test
	public void testBetween() {
		Criteria criteria = new Criteria("field_1").between(100, 200);
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.range.field_1.from", is(100));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.range.field_1.to", is(200));
	}

	@Test(expected = InvalidDataAccessApiUsageException.class)
	public void testBetweenWithoutLowerAndUpperBound() {
		new Criteria("field_1").between(null, null);
	}

	@Test
	public void testBetweenWithoutLowerBound() {
		Criteria criteria = new Criteria("field_1").between(null, 200);
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.range.field_1.to", is(200));
	}

	@Test
	public void testBetweenWithoutUpperBound() {
		Criteria criteria = new Criteria("field_1").between(100, null);
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.range.field_1.from", is(100));
	}

	@Test
	public void testBoost() {
		Criteria criteria = new Criteria("field_1").is("value_1").boost(2f);
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.field.field_1", is("value_1"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.boost", is(2.0));
	}

	@Test
	public void testBoostMultipleCriteriasValues() {
		Criteria criteria = new Criteria("field_1").is("value_1").is("value_2").boost(2f).and("field_3").is("value_3");
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must[0].bool.must[0].field.field_1", is("value_1"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must[0].bool.must[1].field.field_1", is("value_2"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must[0].bool.boost", is(2.0));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must[1].bool.must.field.field_3", is("value_3"));
		exception.expect(InvalidPathException.class);
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must[1].bool.boost", nullValue());
	}

	@Test
	public void testBoostMultipleValues() {
		Criteria criteria = new Criteria("field_1").is("value_1").is("value_2").boost(2f);
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must[0].field.field_1", is("value_1"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must[1].field.field_1", is("value_2"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.boost", is(2.0));
	}

	@Test
	public void testContains() {
		Criteria criteria = new Criteria("field_1").contains("contains");
		Assert.assertEquals("field_1", criteria.getField().getName());
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.field.field_1.query", is("*contains*"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.field.field_1.analyze_wildcard", is(true));
	}

	@Test(expected = InvalidDataAccessApiUsageException.class)
	public void testContainsWithBlank() {
		new Criteria("field_1").contains("no blank");
	}

	@Test
	public void testCriteriaChain() {
		Criteria criteria = new Criteria("field_1").startsWith("start").endsWith("end").contains("contains").is("is");
		Assert.assertEquals("field_1", criteria.getField().getName());
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must[0].field.field_1.query", is("start*"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must[1].field.field_1.query", is("*end"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must[2].field.field_1.query", is("*contains*"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must[3].field.field_1", is("is"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCriteriaForNullField() {
		new Criteria((Field) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCriteriaForNullFieldName() {
		new Criteria(new SimpleField(StringUtils.EMPTY));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCriteriaForNullString() {
		new Criteria((String) null);
	}

	@Test
	public void testCriteriaWithDoubleQuotes() {
		Criteria criteria = new Criteria("field_1").is("with \"quote");
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.field.field_1", is("with \"quote"));
	}

	@Test
	public void testCriteriaWithWhiteSpace() {
		Criteria criteria = new Criteria("field_1").is("white space");
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.field.field_1", is("white space"));
	}

	@Test
	public void testEndsWith() {
		Criteria criteria = new Criteria("field_1").endsWith("end");

		Assert.assertEquals("field_1", criteria.getField().getName());
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.field.field_1.query", is("*end"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.field.field_1.analyze_wildcard", is(true));
	}

	@Test(expected = InvalidDataAccessApiUsageException.class)
	public void testEndsWithBlank() {
		new Criteria("field_1").endsWith("no blank");
	}

	@Test
	public void testExpression() {
		Criteria criteria = new Criteria("field_1").expression("(have fun using +solr && expressions*)");
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.query_string.query",
				is("(have fun using +solr && expressions*)"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.query_string.default_field", is("field_1"));
	}

	@Test
	public void testFuzzy() {
		Criteria criteria = new Criteria("field_1").fuzzy("value_1");
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.fuzzy.field_1", is("value_1"));
	}

	@Test
	public void testFuzzyWithDistance() {
		Criteria criteria = new Criteria("field_1").fuzzy("value_1", "0.5");
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.fuzzy.field_1.value", is("value_1"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.fuzzy.field_1.min_similarity", is("0.5"));
	}

	@Test
	public void testGreaterEqualThan() {
		Criteria criteria = new Criteria("field_1").greaterThanEqual(100);
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.range.field_1.from", is(100));
	}

	@Test(expected = InvalidDataAccessApiUsageException.class)
	public void testGreaterThanEqualNull() {
		new Criteria("field_1").greaterThanEqual(null);
	}

	@Test
	public void testIn() {
		Criteria criteria = new Criteria("field_1").in(1, 2, 3, 5, 8, 13, 21);
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.terms.field_1", contains(1, 2, 3, 5, 8, 13, 21));
	}

	@Test
	public void testInWithNestedCollection() {
		List<List<String>> enclosingList = new ArrayList<List<String>>();
		enclosingList.add(Arrays.asList("spring", "data"));
		enclosingList.add(Arrays.asList("solr"));
		Criteria criteria = new Criteria("field_1").in(enclosingList);
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.terms.field_1", contains("spring", "data", "solr"));
	}

	@Test(expected = InvalidDataAccessApiUsageException.class)
	public void testInWithNoValues() {
		new Criteria("field_1").in();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInWithNull() {
		new Criteria("field_1").in((Collection<?>) null);
	}

	@Test
	public void testIs() {
		Criteria criteria = new Criteria("field_1").is("is");
		Assert.assertEquals("field_1", criteria.getField().getName());
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.field.field_1", is("is"));
	}

	@Test
	public void testIsNot() {
		Criteria criteria = new Criteria("field_1").is("value_1").not();
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must_not.bool.must.field.field_1", is("value_1"));
	}

	@Test
	public void testIsWithJavaDateValue() {
		DateTime dateTime = new DateTime(2012, 8, 21, 6, 35, 0, DateTimeZone.UTC);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
		calendar.setTimeInMillis(dateTime.getMillis());

		Criteria criteria = new Criteria("dateField").is(calendar.getTime());
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.field.dateField", is("2012-08-21T06:35:00.000Z"));
	}

	@Test
	public void testIsWithJodaDateTime() {
		DateTime dateTime = new DateTime(2012, 8, 21, 6, 35, 0, DateTimeZone.UTC);

		Criteria criteria = new Criteria("dateField").is(dateTime);
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.field.dateField", is("2012-08-21T06:35:00.000Z"));
	}

	@Test
	public void testIsWithJodaLocalDateTime() {
		LocalDateTime dateTime = new LocalDateTime(new DateTime(2012, 8, 21, 6, 35, 0, DateTimeZone.UTC).getMillis(), DateTimeZone.UTC);

		Criteria criteria = new Criteria("dateField").is(dateTime);
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.field.dateField", is("2012-08-21T06:35:00.000"));
	}

	@Test
	public void testIsWithNegativeNumner() {
		Criteria criteria = new Criteria("field_1").is(-100);
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.field.field_1", is(-100));
	}

	@Test
	public void testLessThanEqual() {
		Criteria criteria = new Criteria("field_1").lessThanEqual(200);
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.range.field_1.to", is(200));
	}

	@Test(expected = InvalidDataAccessApiUsageException.class)
	public void testLessThanEqualNull() {
		new Criteria("field_1").lessThanEqual(null);
	}

	@Test
	public void testMultipleIs() {
		Criteria criteria = new Criteria("field_1").is("is").is("another is");
		Assert.assertEquals("field_1", criteria.getField().getName());
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must[0].field.field_1", is("is"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must[1].field.field_1", is("another is"));
	}

	@Test
	public void testNear() {
		Criteria criteria = new Criteria("field_1").near(new GeoLocation(48.303056, 14.290556), new Distance(5));
		System.out.println(criteria.getQueryBuilder().toString());
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.filtered.query.match_all", notNullValue());
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.filtered.filter.geo_distance.field_1",
				contains(14.290556, 48.303056));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.filtered.filter.geo_distance.distance", is("5.0km"));
	}

	@Test(expected = InvalidDataAccessApiUsageException.class)
	public void testNearWithNegativeDistance() {
		new Criteria("field_1").near(new GeoLocation(48.303056, 14.290556), new Distance(-1));
	}

	@Test
	public void testNearWithNullDistance() {
		Criteria criteria = new Criteria("field_1").near(new GeoLocation(48.303056, 14.290556), null);
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.filtered.query.match_all", notNullValue());
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.filtered.filter.geo_distance.field_1",
				contains(14.290556, 48.303056));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNearWithNullLocation() {
		Criteria criteria = new Criteria("field_1").near(null, new Distance(5));
		criteria.getQueryBuilder().toString();
	}

	@Test
	public void testOr() {
		Criteria criteria = new Criteria("field_1").startsWith("start").or("field_2").endsWith("end").startsWith("start2");
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.should[0].bool.must.field.field_1.query", is("start*"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.should[1].bool.must[0].field.field_2.query", is("*end"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.should[1].bool.must[1].field.field_2.query", is("start2*"));
	}

	@Test
	public void testOrWithCriteria() {
		Criteria criteria = new Criteria("field_1").startsWith("start");
		Criteria orCriteria = new Criteria("field_2").endsWith("end").startsWith("start2");
		criteria = criteria.or(orCriteria);
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.should[0].bool.must.field.field_1.query", is("start*"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.should[1].bool.must.bool.must[0].field.field_2.query", is("*end"));
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.should[1].bool.must.bool.must[1].field.field_2.query", is("start2*"));
	}

	@Test
	public void testStartsWith() {
		Criteria criteria = new Criteria("field_1").startsWith("start");

		Assert.assertEquals("field_1", criteria.getField().getName());
		with(criteria.getQueryBuilder().toString()).assertThat("$.bool.must.bool.must.field.field_1.query", is("start*"));
	}

	@Test(expected = InvalidDataAccessApiUsageException.class)
	public void testStartsWithBlank() {
		new Criteria("field_1").startsWith("no blank");
	}

}
