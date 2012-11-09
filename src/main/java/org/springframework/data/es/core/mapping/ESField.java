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
package org.springframework.data.es.core.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.data.annotation.Persistent;

/**
 * @author Patryk Wąsik
 * 
 */
@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface ESField {

	/**
	 * String field
	 * 
	 * @return
	 */
	String analyzer() default "";

	double boost() default 1.0;

	/**
	 * Date field
	 * 
	 * @return
	 */
	String format() default "";

	/**
	 * String field
	 * 
	 * @return
	 */
	int ignoreAbove() default -1;

	/**
	 * Number field
	 * 
	 * @return
	 */
	boolean ignoreMalformed() default false;

	boolean includeInAll() default true;

	String index() default "";

	/**
	 * String field
	 * 
	 * @return
	 */
	String indexAnalyzer() default "";

	/**
	 * String field
	 * 
	 * @return
	 */
	String indexOptions() default "";

	String nullValue() default "";

	/**
	 * String field
	 * 
	 * @return
	 */
	boolean omitNorms() default false;

	/**
	 * String field
	 * 
	 * @return
	 */
	boolean omitTermFreqAndPositions() default false;

	/**
	 * Number and date field
	 * 
	 * @return
	 */
	int precisionStep() default 4;

	/**
	 * String field
	 * 
	 * @return
	 */
	String searchAnalyzer() default "";

	boolean store() default false;

	/**
	 * String field
	 * 
	 * @return
	 */
	String termVector() default "";

	String value() default "";

}
