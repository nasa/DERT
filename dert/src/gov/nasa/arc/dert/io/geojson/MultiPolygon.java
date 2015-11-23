package gov.nasa.arc.dert.io.geojson;

import gov.nasa.arc.dert.io.geojson.json.JsonObject;

/**
 * Provides a GeoJSON MultiPolygon object.
 *
 */
public class MultiPolygon extends Geometry {

	private double[][][][] coordinate;

	/**
	 * Constructor
	 * 
	 * @param jsonObject
	 */
	public MultiPolygon(JsonObject jsonObject) {
		super(jsonObject);
		Object[] arrayN = jsonObject.getArray("coordinates");
		int n = arrayN.length;
		Object[] arrayM = (Object[])arrayN[0];
		int m = arrayM.length;
		Object[] arrayL = (Object[])arrayM[0];
		int l = arrayL.length;
		Object[] pos = (Object[])arrayL[0];
		int posLength = pos.length;
		coordinate = new double[n][m][l][posLength];
		for (int i = 0; i < n; ++i) {
			arrayM = (Object[])arrayN[i];
			for (int j = 0; j < m; ++j) {
				arrayL = (Object[])arrayM[j];
				for (int k = 0; k < l; ++k) {
					pos = (Object[])arrayL[k];
					for (int p = 0; p < posLength; ++p) {
						coordinate[i][j][k][p] = ((Double)pos[p]).doubleValue();
					}
				}
			}
		}
	}

	/**
	 * Get coordinates.
	 * 
	 * @return
	 */
	public double[][][][] getCoordinates() {
		return (coordinate);
	}

}
