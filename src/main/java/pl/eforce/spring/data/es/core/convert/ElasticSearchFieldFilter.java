package pl.eforce.spring.data.es.core.convert;

import org.springframework.data.annotation.Id;

import pl.eforce.es.orm.mapping.ESField;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;

public class ElasticSearchFieldFilter implements BeanPropertyFilter {

	@Override
	public void depositSchemaProperty(BeanPropertyWriter writer, JsonObjectFormatVisitor objectVisitor, SerializerProvider provider)
			throws JsonMappingException {
		if ((writer.getAnnotation(ESField.class) != null) || (writer.getAnnotation(Id.class) != null)) {
			writer.depositSchemaProperty(objectVisitor);
		}

	}

	@Override
	public void depositSchemaProperty(BeanPropertyWriter writer, ObjectNode propertiesNode, SerializerProvider provider) throws JsonMappingException {
		if ((writer.getAnnotation(ESField.class) != null) || (writer.getAnnotation(Id.class) != null)) {
			writer.depositSchemaProperty(propertiesNode, provider);
		}
	}

	@Override
	public void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider prov, BeanPropertyWriter writer) throws Exception {
		if ((writer.getAnnotation(ESField.class) != null) || (writer.getAnnotation(Id.class) != null)) {
			writer.serializeAsField(bean, jgen, prov);
		}
	}

}
