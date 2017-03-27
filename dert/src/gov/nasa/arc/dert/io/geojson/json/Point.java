package gov.nasa.arc.dert.io.geojson.json;


/**
 * Provides a GeoJSON Point object.
 *
 */
public class Point extends Geometry {

	private double[] coordinate;

	/**
	 * Constructor
	 * 
	 * @param jsonObject
	 */
	public Point(JsonObject jsonObject) {
		super(jsonObject, GeojsonType.Point);
		Object[] coordArray = jsonObject.getArray("coordinates");
		coordinate = new double[coordArray.length];
		for (int i = 0; i < coordArray.length; ++i) {
			coordinate[i] = ((Double)coordArray[i]).doubleValue();
		}
	}

	/**
	 * Get coordinates.
	 * 
	 * @return
	 */
	public double[] getCoordinates() {
		return (coordinate);
	}
}
