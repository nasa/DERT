package gov.nasa.arc.dert.io.geojson;

import gov.nasa.arc.dert.io.geojson.json.JsonObject;

import java.util.HashMap;

/**
 * Provides a Feature object for GeoJSON.
 *
 */
public class GeoJsonFeature extends GeoJsonObject {

	private HashMap<String, Object> properties;
	private Geometry geometry;
	protected String id;

	/**
	 * Constructor
	 * 
	 * @param jsonObject
	 */
	public GeoJsonFeature(JsonObject jsonObject) {
		super(jsonObject);
		id = jsonObject.getString("id");

		// convert JSON properties to hash map
		JsonObject jObj = jsonObject.getJsonObject("properties");
		createProperties(jObj);

		// create the geometry object for this feature
		jObj = jsonObject.getJsonObject("geometry");
		geometry = Geometry.createGeometry(jObj);
	}

	private void createProperties(JsonObject jObj) {
		if (jObj == null) {
			return;
		}
		String[] key = jObj.getKeys();
		properties = new HashMap<String, Object>();
		for (int i = 0; i < key.length; ++i) {
			Object val = jObj.get(key[i]);
			if (!(val instanceof Object[]))
				properties.put(key[i], val);
		}
	}

	/**
	 * Get the Feature properties
	 * 
	 * @return
	 */
	public HashMap<String, Object> getProperties() {
		return (properties);
	}

	/**
	 * Get the Feature geometry
	 * 
	 * @return
	 */
	public Geometry getGeometry() {
		return (geometry);
	}
	
	/**
	 * Get the Feature id
	 * 
	 * @return id
	 */
	public String getId() {
		return(id);
	}
}
