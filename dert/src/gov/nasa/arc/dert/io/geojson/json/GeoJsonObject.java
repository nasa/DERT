package gov.nasa.arc.dert.io.geojson.json;


/**
 * Provides a GeoJSON Object.
 *
 */
public class GeoJsonObject {

	// Bounding box
	protected double[] bbox;

	/**
	 * Constructor
	 * 
	 * @param jsonObject
	 */
	public GeoJsonObject(JsonObject jsonObject) {
		Object[] array = jsonObject.getArray("bbox");
		createBBox(array);
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

}
