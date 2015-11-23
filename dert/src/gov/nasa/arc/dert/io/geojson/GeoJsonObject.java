package gov.nasa.arc.dert.io.geojson;

import gov.nasa.arc.dert.io.geojson.json.JsonObject;

/**
 * Provides a GeoJSON Object.
 *
 */
public class GeoJsonObject {

	// Bounding box
	protected double[] bbox;

	// Coordinate reference system
	protected CoordinateReferenceSystem crs;

	/**
	 * Constructor
	 * 
	 * @param jsonObject
	 */
	public GeoJsonObject(JsonObject jsonObject) {
		Object[] array = jsonObject.getArray("bbox");
		createBBox(array);
		JsonObject jObj = jsonObject.getJsonObject("crs");
		createCRS(jObj);
	}

	private void createBBox(Object[] array) {
		if (array == null) {
			return;
		}
		bbox = new double[array.length];
		for (int i = 0; i < array.length; ++i) {
			bbox[i] = ((Double)array[i]).doubleValue();
		}
	}

	private void createCRS(JsonObject jsonObject) {
		if (jsonObject == null) {
			return;
		}
		crs = new CoordinateReferenceSystem();
	}

}
