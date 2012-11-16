package pl.eforce.spring.data.es;

public class JsonTestUtils {

	public static String toSingleLineString(String string) {
		return string.replaceAll("\n", "").replaceAll("\\s*", "").replaceAll("\"", "").trim();
	}

}
