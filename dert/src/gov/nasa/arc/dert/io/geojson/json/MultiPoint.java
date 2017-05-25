package gov.nasa.arc.dert.io.geojson.json;



/**
 * Provides a GeoJSON MultiPoint object.
 *
 */
public class MultiPoint extends Geometry {

	private double[][] coordinate;

	/**
	 * Constructor
	 * 
	 * @param jsonObject
	 */
	public MultiPoint(JsonObject jsonObject) {
		super(jsonObject, GeojsonType.MultiPoint);
		Object[] arrayN = jsonObject.getArray("coordinates");
		int n = arrayN.length;
		Object[] pos = (Object[])arrayN[0];
		int posLength = pos.length;
		coordinate = new double[n][posLength];
		for (int i = 0; i < n; ++i) {
			pos = (Object[])arrayN[i];
			for (int p = 0; p < posLength; ++p) {
				coordinate[i][p] = ((Double)pos[p]).doubleValue();
			}
		}
	}
	
	public MultiPoint(double[][] coordinate) {
		super(GeojsonType.MultiPoint);
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
