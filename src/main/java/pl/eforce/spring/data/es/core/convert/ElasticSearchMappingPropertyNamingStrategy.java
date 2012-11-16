package pl.eforce.spring.data.es.core.convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.common.collect.Sets;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.util.Assert;

import pl.eforce.spring.data.es.core.mapping.ElasticSearchPersistentEntity;
import pl.eforce.spring.data.es.core.mapping.ElasticSearchPersistentProperty;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;

public class ElasticSearchMappingPropertyNamingStrategy extends PropertyNamingStrategy {
	private final Map<Class<?>, Map<String, String>> cache = new HashMap<Class<?>, Map<String, String>>();
	private final MappingContext<? extends ElasticSearchPersistentEntity<?>, ElasticSearchPersistentProperty> mappingContext;

	@SuppressWarnings("unchecked")
	private final SimpleTypeHolder simpleTypeHolder = new SimpleTypeHolder(Sets.newHashSet(BigInteger.class, BigDecimal.class), true);

	public ElasticSearchMappingPropertyNamingStrategy(
			MappingContext<? extends ElasticSearchPersistentEntity<?>, ElasticSearchPersistentProperty> mappingContext) {
		this.mappingContext = mappingContext;
	}

	@Override
	public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
		if (simpleTypeHolder.isSimpleType(field.getDeclaringClass())) {
			return super.nameForField(config, field, defaultName);
		}
		return getPropertyName(field.getDeclaringClass(), defaultName);
	}

	@Override
	public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
		if (simpleTypeHolder.isSimpleType(method.getDeclaringClass())) {
			return super.nameForGetterMethod(config, method, defaultName);
		}
		return getPropertyName(method.getDeclaringClass(), defaultName);
	}

	@Override
	public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
		if (simpleTypeHolder.isSimpleType(method.getDeclaringClass())) {
			return super.nameForSetterMethod(config, method, defaultName);
		}
		return getPropertyName(method.getDeclaringClass(), defaultName);
	}

	private void checkCache(Class<?> clazz) {
		if (!cache.containsKey(clazz)) {
			final ElasticSearchPersistentEntity<?> persistentEntity = mappingContext.getPersistentEntity(clazz);
			final HashMap<String, String> fieldsMap = new HashMap<String, String>();
			cache.put(clazz, fieldsMap);

			persistentEntity.doWithProperties(new PropertyHandler<ElasticSearchPersistentProperty>() {

				@Override
				public void doWithPersistentProperty(ElasticSearchPersistentProperty persistentProperty) {
					fieldsMap.put(persistentProperty.getName(), persistentProperty.getIndexName());
				}
			});
		}
	}

	private String getPropertyName(Class<?> clazz, String fieldName) {
		checkCache(clazz);

		String toReturn = cache.get(clazz).get(fieldName);

		Assert.notNull(toReturn, "Property name " + fieldName);

		return toReturn;
	}
}
