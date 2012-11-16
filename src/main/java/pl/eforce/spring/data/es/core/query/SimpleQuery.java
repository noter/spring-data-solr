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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;

/**
 * Full implementation of {@link Query} that allows multiple options like pagination, grouping,...
 * 
 * @author Patryk Wasik
 */
public class SimpleQuery extends AbstractQuery implements Query, FilterQuery {

	public static final Pageable DEFAULT_PAGE = new PageRequest(0, DEFAULT_PAGE_SIZE);

	public static final Query fromQuery(Query source) {
		if (source == null) {
			return null;
		}

		SimpleQuery query = new SimpleQuery();
		if (source.getCriteria() != null) {
			query.addCriteria(source.getCriteria());
		}
		if (!source.getFilterQueries().isEmpty()) {
			query.filterQueries.addAll(source.getFilterQueries());
		}
		if (!source.getProjectionOnFields().isEmpty()) {
			query.projectionOnFields.addAll(source.getProjectionOnFields());
		}
		if (!source.getGroupByFields().isEmpty()) {
			query.groupByFields.addAll(source.getGroupByFields());
		}
		if (source.getSort() != null) {
			query.addSort(source.getSort());
		}
		return query;
	}

	private final List<FilterQuery> filterQueries = new ArrayList<FilterQuery>(0);
	private final List<Field> groupByFields = new ArrayList<Field>(0);;
	private Pageable pageable = DEFAULT_PAGE;
	private final List<Field> projectionOnFields = new ArrayList<Field>(0);

	private Sort sort;

	public SimpleQuery() {
	}

	public SimpleQuery(Criteria criteria) {
		this(criteria, null);
	}

	public SimpleQuery(Criteria criteria, Pageable pageable) {
		super(criteria);
		this.pageable = pageable;
		if (pageable != null) {
			this.addSort(pageable.getSort());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Query> T addFilterQuery(FilterQuery filterQuery) {
		filterQueries.add(filterQuery);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <T extends Query> T addGroupByField(Field field) {
		throw new UnsupportedOperationException("Not implemented yet");
		/*
		 * Assert.notNull(field, "Field for grouping must not be null.");
		 * Assert.hasText(field.getName(),
		 * "Field.name for grouping must not be null/empty.");
		 * 
		 * groupByFields.add(field); return (T) this;
		 */
	}

	public final <T extends Query> T addGroupByField(String fieldname) {
		return addGroupByField(new SimpleField(fieldname));
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <T extends Query> T addProjectionOnField(Field field) {
		Assert.notNull(field, "Field for projection must not be null.");
		Assert.hasText(field.getName(), "Field.name for projection must not be null/empty.");

		projectionOnFields.add(field);
		return (T) this;
	}

	public final <T extends Query> T addProjectionOnField(String fieldname) {
		return this.addProjectionOnField(new SimpleField(fieldname));
	}

	@SuppressWarnings("unchecked")
	public final <T extends Query> T addProjectionOnFields(Field... fields) {
		Assert.notEmpty(fields, "Cannot add projection on null/empty field list.");
		for (Field field : fields) {
			addProjectionOnField(field);
		}
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public final <T extends Query> T addProjectionOnFields(String... fieldnames) {
		Assert.notEmpty(fieldnames, "Cannot add projection on null/empty field list.");
		for (String fieldname : fieldnames) {
			addProjectionOnField(fieldname);
		}
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <T extends Query> T addSort(Sort sort) {
		if (sort == null) {
			return (T) this;
		}

		if (this.sort == null) {
			this.sort = sort;
		} else {
			this.sort = this.sort.and(sort);
		}

		return (T) this;
	}

	@Override
	public List<FilterQuery> getFilterQueries() {
		return Collections.unmodifiableList(filterQueries);
	}

	@Override
	public List<Field> getGroupByFields() {
		return Collections.unmodifiableList(groupByFields);
	}

	@Override
	public Pageable getPageRequest() {
		return pageable;
	}

	@Override
	public List<Field> getProjectionOnFields() {
		return Collections.unmodifiableList(projectionOnFields);
	}

	@Override
	public Sort getSort() {
		return sort;
	}

	@Override
	public final <T extends Query> T setPageRequest(Pageable pageable) {
		Assert.notNull(pageable);

		this.pageable = pageable;
		return this.addSort(pageable.getSort());
	}

}
