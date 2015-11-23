package gov.nasa.arc.dert.io.geojson;

import gov.nasa.arc.dert.io.geojson.json.JsonObject;

import java.util.ArrayList;

/**
 * Provides a FeatureCollection for GeoJSON
 *
 */
public class GeoJsonFeatureCollection extends GeoJsonObject {

	// List of GeoJSON Features
	private ArrayList<GeoJsonFeature> featureList;

	public GeoJsonFeatureCollection(JsonObject jsonObject) {
		super(jsonObject);
		Object[] array = jsonObject.getArray("features");

		// load the GeoJSON features from the collection
		featureList = new ArrayList<GeoJsonFeature>();
		for (int i = 0; i < array.length; ++i) {
			JsonObject jObj = (JsonObject)array[i];
			String type = jObj.getString("type");
			if (type.equals("Feature")) {
				featureList.add(new GeoJsonFeature(jObj));
			}
		}
	}

	/**
	 * Get the list of GeoJSON Features in this collection.
	 */
	public ArrayList<GeoJsonFeature> getFeatureList() {
		return (featureList);
	}

}
