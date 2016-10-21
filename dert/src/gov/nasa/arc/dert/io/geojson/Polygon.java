package gov.nasa.arc.dert.io.geojson;

import gov.nasa.arc.dert.io.geojson.json.JsonObject;

/**
 * Provides a GeoJSON Polygon object.
 *
 */
public class Polygon extends Geometry {

	private double[][][] coordinate;

	/**
	 * Constructor
	 * 
	 * @param jsonObject
	 */
	public Polygon(JsonObject jsonObject, CoordinateReferenceSystem crs) {
		super(jsonObject, GeojsonType.Polygon);
		Object[] arrayN = jsonObject.getArray("coordinates");
		int n = arrayN.length;
		Object[] arrayM = (Object[])arrayN[0];
		int m = arrayM.length;
		Object[] pos = (Object[])arrayM[0];
		int posLength = pos.length;
		coordinate = new double[n][m][posLength];
		for (int i = 0; i < n; ++i) {
			arrayM = (Object[])arrayN[i];
			for (int j = 0; j < m; ++j) {
				pos = (Object[])arrayM[j];
				for (int p = 0; p < posLength; ++p) {
					coordinate[i][j][p] = ((Double)pos[p]).doubleValue();
				}
				crs.translate(coordinate[i][j]);
			}
		}
	}

	/**
	 * Get coordinates.
	 * 
	 * @return
	 */
	public double[][][] getCoordinates() {
		return (coordinate);
	}

}
