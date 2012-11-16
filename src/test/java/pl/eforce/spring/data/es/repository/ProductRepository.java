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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import pl.eforce.spring.data.es.core.geo.Distance;
import pl.eforce.spring.data.es.core.geo.GeoLocation;
import pl.eforce.spring.data.es.repository.ElasticSearchCrudRepository;
import pl.eforce.spring.data.es.repository.Query;

/**
 * @author Christoph Strobl
 */
public interface ProductRepository extends ElasticSearchCrudRepository<ProductBean, String> {

	List<ProductBean> findByAvailableFalse();

	List<ProductBean> findByAvailableTrue();

	List<ProductBean> findByAvailableTrueOrderByNameDesc();

	@Query("inStock:?0")
	List<ProductBean> findByAvailableUsingQueryAnnotation(boolean available);

	ProductBean findById(String id);

	List<ProductBean> findByLastModifiedAfter(Date date);

	List<ProductBean> findByLastModifiedBefore(Date date);

	List<ProductBean> findByLocationNear(GeoLocation location, Distance distance);

	List<ProductBean> findByName(String name);

	ProductBean findByNameAndAvailableTrue(String name);

	List<ProductBean> findByNamedQuery(Integer popularity);

	List<ProductBean> findByNameLike(String name);

	List<ProductBean> findByNameNot(String name);

	List<ProductBean> findByNameRegex(String name);

	Page<ProductBean> findByNameStartingWith(String name, Pageable page);

	List<ProductBean> findByNameStartsWith(String name);

	List<ProductBean> findByPopularityAndAvailableTrue(Integer popularity);

	List<ProductBean> findByPopularityBetween(Integer low, Integer up);

	List<ProductBean> findByPopularityGreaterThan(Integer low);

	List<ProductBean> findByPopularityIn(Collection<Integer> popularities);

	List<ProductBean> findByPopularityLessThan(Integer up);

	List<ProductBean> findByPopularityNotIn(Collection<Integer> popularities);

	List<ProductBean> findByPopularityOrAvailableFalse(Integer popularity);

}