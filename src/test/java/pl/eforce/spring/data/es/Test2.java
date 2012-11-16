package pl.eforce.spring.data.es;

import java.math.BigInteger;

import org.springframework.data.annotation.Id;

import pl.eforce.es.orm.mapping.ESField;

public class Test2 {

	@Id
	private final BigInteger id;

	@ESField
	private String name;

	public Test2(BigInteger id) {
		super();
		this.id = id;
	}

	public BigInteger getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
