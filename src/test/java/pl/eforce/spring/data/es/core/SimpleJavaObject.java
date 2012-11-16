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
package pl.eforce.spring.data.es.core;

import pl.eforce.es.orm.mapping.ESField;

/**
 * @author Patryk Wasik
 */
public class SimpleJavaObject {

	@ESField
	private String id;

	@ESField
	private Long value;

	public SimpleJavaObject() {
	}

	public SimpleJavaObject(String id, Long value) {
		super();
		this.id = id;
		this.value = value;
	}

	public String getId() {
		return id;
	}

	public Long getValue() {
		return value;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setValue(Long value) {
		this.value = value;
	}

}
