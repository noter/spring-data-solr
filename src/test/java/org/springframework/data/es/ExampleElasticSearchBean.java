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
package org.springframework.data.es;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.springframework.data.es.core.mapping.ESField;

/**
 * @author Patryk Wasik
 */
public class ExampleElasticSearchBean {

	@ESField("cat")
	private List<String> category;

	@ESField
	private String id;

	@ESField
	private boolean inStock;

	@ESField("last_modified")
	private Date lastModified;

	@ESField
	private String name;

	@ESField
	private Integer popularity;

	@ESField
	private float price;

	@ESField
	private String store;

	public ExampleElasticSearchBean() {
		category = new ArrayList<String>();
	}

	public ExampleElasticSearchBean(String id, String name, String category) {
		this();
		this.id = id;
		this.name = name;
		this.category.add(category);
	}

	public ExampleElasticSearchBean(String id, String name, String category, float price, boolean inStock) {
		this(id, name, category);
		this.price = price;
		this.inStock = inStock;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public List<String> getCategory() {
		return category;
	}

	public String getId() {
		return id;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public String getName() {
		return name;
	}

	public Integer getPopularity() {
		return popularity;
	}

	public float getPrice() {
		return price;
	}

	public String getStore() {
		return store;
	}

	public boolean isInStock() {
		return inStock;
	}

	public void setCategory(List<String> category) {
		this.category = category;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setInStock(boolean inStock) {
		this.inStock = inStock;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPopularity(Integer popularity) {
		this.popularity = popularity;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public void setStore(String store) {
		this.store = store;
	}

}
