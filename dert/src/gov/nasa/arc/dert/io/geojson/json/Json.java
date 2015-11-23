package gov.nasa.arc.dert.io.geojson.json;

import java.io.InputStream;
import java.io.InputStreamReader;

public class Json {
	
	public static JsonReader createReader(InputStream is) {
		JsonReader reader = new JsonReader(new InputStreamReader(is));
		return(reader);
	}

}
