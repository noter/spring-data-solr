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
package pl.eforce.spring.data.es.core.mapping;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mapping.PersistentProperty;

/**
 * @author Patryk Wąsik
 */
public interface ElasticSearchPersistentProperty extends PersistentProperty<ElasticSearchPersistentProperty> {

	public static class MappingInfo {

		private String analyzer;

		private Double boost;

		private String format;

		private Integer ignoreAbove;

		private Boolean ignoreMalformed;

		private Boolean includeInAll;

		private String index;

		private String indexAnalyzer;

		private String indexName;

		private String indexOptions;

		private String nullValue;

		private Boolean omitNorms;

		private Boolean omitTermFreqAndPositions;

		private Integer precisionStep;

		private String searchAnalyzer;

		private Boolean store;

		private String termVector;

		public String getAnalyzer() {
			return analyzer;
		}

		public Double getBoost() {
			return boost;
		}

		public String getFormat() {
			return format;
		}

		public Integer getIgnoreAbove() {
			return ignoreAbove;
		}

		public Boolean getIgnoreMalformed() {
			return ignoreMalformed;
		}

		public Boolean getIncludeInAll() {
			return includeInAll;
		}

		public String getIndex() {
			return index;
		}

		public String getIndexAnalyzer() {
			return indexAnalyzer;
		}

		public String getIndexName() {
			return indexName;
		}

		public String getIndexOptions() {
			return indexOptions;
		}

		public String getNullValue() {
			return nullValue;
		}

		public Boolean getOmitNorms() {
			return omitNorms;
		}

		public Boolean getOmitTermFreqAndPositions() {
			return omitTermFreqAndPositions;
		}

		public Integer getPrecisionStep() {
			return precisionStep;
		}

		public String getSearchAnalyzer() {
			return searchAnalyzer;
		}

		public Boolean getStore() {
			return store;
		}

		public String getTermVector() {
			return termVector;
		}

		public void setAnalyzer(String analyzer) {
			this.analyzer = analyzer;
		}

		public void setBoost(Double boost) {
			this.boost = boost;
		}

		public void setFormat(String format) {
			this.format = format;
		}

		public void setIgnoreAbove(Integer ignoreAbove) {
			this.ignoreAbove = ignoreAbove;
		}

		public void setIgnoreMalformed(Boolean ignoreMalformed) {
			this.ignoreMalformed = ignoreMalformed;
		}

		public void setIncludeInAll(Boolean includeInAll) {
			this.includeInAll = includeInAll;
		}

		public void setIndex(String index) {
			this.index = index;
		}

		public void setIndexAnalyzer(String indexAnalyzer) {
			this.indexAnalyzer = indexAnalyzer;
		}

		public void setIndexName(String indexName) {
			this.indexName = indexName;
		}

		public void setIndexOptions(String indexOptions) {
			this.indexOptions = indexOptions;
		}

		public void setNullValue(String nullValue) {
			this.nullValue = nullValue;
		}

		public void setOmitNorms(Boolean omitNorms) {
			this.omitNorms = omitNorms;
		}

		public void setOmitTermFreqAndPositions(Boolean omitTermFreqAndPositions) {
			this.omitTermFreqAndPositions = omitTermFreqAndPositions;
		}

		public void setPrecisionStep(Integer precisionStep) {
			this.precisionStep = precisionStep;
		}

		public void setSearchAnalyzer(String searchAnalyzer) {
			this.searchAnalyzer = searchAnalyzer;
		}

		public void setStore(Boolean store) {
			this.store = store;
		}

		public void setTermVector(String termVector) {
			this.termVector = termVector;
		}

	}

	public enum PropertyToFieldNameConverter implements Converter<ElasticSearchPersistentProperty, String> {

		INSTANCE;

		@Override
		public String convert(ElasticSearchPersistentProperty source) {
			return source.getIndexName();
		}
	}

	String getIndexName();

	MappingInfo getMappingInfo();

	MappingInfo[] getMappingInfos();

	boolean isMultiField();

}
