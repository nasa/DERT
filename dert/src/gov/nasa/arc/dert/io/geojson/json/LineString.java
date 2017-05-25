package gov.nasa.arc.dert.io.geojson.json;



/**
 * Provides a GeoJSON LineString Geometry object.
 *
 */
public class LineString extends Geometry {

	private double[][] coordinate;

	/**
	 * Constructor
	 * 
	 * @param jsonObject
	 */
	public LineString(JsonObject jsonObject) {
		super(jsonObject, GeojsonType.LineString);
		Object[] arrayN = jsonObject.getArray("coordinates");
		int n = arrayN.length;
		if (n < 2) {
			throw new IllegalArgumentException("LineString has < 2 positions.");
		}
		Object[] pos = (Object[])arrayN[0];
		int posLength = pos.length;
		coordinate = new double[n][posLength];
		for (int i = 0; i < n; ++i) {
			pos = (Object[])arrayN[i];
			for (int p = 0; p < posLength; ++p)
				coordinate[i][p] = ((Double)pos[p]).doubleValue();
		}
	}
	
	public LineString(double[][] coordinate) {
		super(GeojsonType.LineString);
		this.coordinate = coordinate;
	}

	/**
	 * Get coordinates.
	 * 
	 * @return
	 */
	public double[][] getCoordinates() {
		return (coordinate);
	}

}
