package gov.nasa.arc.dert.io.geojson;

import gov.nasa.arc.dert.io.geojson.json.JsonObject;

/**
 * Provides a base class for GeoJSON Geometry objects.
 *
 */
public abstract class Geometry extends GeoJsonObject {

	public static enum GeojsonType {
		Point, MultiPoint, LineString, MultiLineString, Polygon, MultiPolygon, GeometryCollection
	}

	/**
	 * Constructor
	 * 
	 * @param jsonObject
	 */
	public Geometry(JsonObject jsonObject) {
		super(jsonObject);
	}

	/**
	 * Create a Geometry object from a JSON object.
	 * 
	 * @param jObj
	 * @return
	 */
	public static Geometry createGeometry(JsonObject jObj, CoordinateReferenceSystem crs) {
		String typeStr = jObj.getString("type");
		GeojsonType type = GeojsonType.valueOf(typeStr);
		Geometry geometry = null;
		switch (type) {
		case Point:
			geometry = new Point(jObj, crs);
			break;
		case MultiPoint:
			geometry = new MultiPoint(jObj, crs);
			break;
		case LineString:
			geometry = new LineString(jObj, crs);
			break;
		case MultiLineString:
			geometry = new MultiLineString(jObj, crs);
			break;
		case Polygon:
			geometry = new Polygon(jObj, crs);
			break;
		case MultiPolygon:
			geometry = new MultiPolygon(jObj, crs);
			break;
		case GeometryCollection:
			geometry = new GeometryCollection(jObj, crs);
			break;
		}

		return (geometry);
	}

}
