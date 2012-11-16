package pl.eforce.spring.data.es;

import java.math.BigInteger;
import java.util.Map;

import org.springframework.data.annotation.Id;

import pl.eforce.es.orm.mapping.ESField;

public class Test {

	@Id
	private final BigInteger id;

	@ESField
	private Map<String, Test2> map;

	@ESField
	private String name;

	public Test(BigInteger id) {
		super();
		this.id = id;
	}

	public BigInteger getId() {
		return id;
	}

	public Map<String, Test2> getMap() {
		return map;
	}

	public String getName() {
		return name;
	}

	public void setMap(Map<String, Test2> map) {
		this.map = map;
	}

	public void setName(String name) {
		this.name = name;
	}

}
