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
package pl.eforce.spring.data.es.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;

import pl.eforce.es.orm.mapping.ESField;

/**
 * @author Christoph Strobl
 */
public class ProductBean {

	@ESField("inStock")
	private boolean available;

	@ESField
	private List<String> categories;

	@Id
	private String id;

	@ESField("last_modified")
	private Date lastModified;

	@ESField("store")
	private String location;

	@ESField("name")
	private String name;

	@ESField
	private Integer popularity;

	@ESField
	private Float price;

	@ESField("textGeneral")
	private String text;

	@ESField("title")
	private String title;

	@ESField
	private Float weight;

	public List<String> getCategories() {
		return categories;
	}

	public String getId() {
		return id;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public String getLocation() {
		return location;
	}

	public String getName() {
		return name;
	}

	public Integer getPopularity() {
		return popularity;
	}

	public Float getPrice() {
		return price;
	}

	public String getText() {
		return text;
	}

	public String getTitle() {
		return title;
	}

	public Float getWeight() {
		return weight;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPopularity(Integer popularity) {
		this.popularity = popularity;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setWeight(Float weight) {
		this.weight = weight;
	}

}
