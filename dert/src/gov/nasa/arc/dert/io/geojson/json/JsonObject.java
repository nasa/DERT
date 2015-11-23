package gov.nasa.arc.dert.io.geojson.json;

import java.util.HashMap;

public class JsonObject {
	
	private HashMap<String,Object> map;
	
	public JsonObject() {
		map = new HashMap<String, Object>();
	}
	
	public synchronized void add(String key, Object value) {
		map.put(key, value);
	}
	
	public synchronized String getString(String key) {
		return((String)map.get(key));
	}
	
	public synchronized Object[] getArray(String key) {
		return((Object[])map.get(key));
	}
	
	public synchronized JsonObject getJsonObject(String key) {
		return((JsonObject)map.get(key));
	}
	
	public synchronized String[] getKeys() {
		String[] key = new String[map.size()];
		map.keySet().toArray(key);
		return(key);
	}
	
	public synchronized Object get(String key) {
		return(map.get(key));
	}

}
