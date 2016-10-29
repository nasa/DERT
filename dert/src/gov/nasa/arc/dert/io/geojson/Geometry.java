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
	
	public GeojsonType type;

	/**
	 * Constructor
	 * 
	 * @param jsonObject
	 */
	public Geometry(JsonObject jsonObject, GeojsonType type) {
		super(jsonObject);
		this.type = type;
	}

	/**
	 * Create a Geometry object from a JSON object.
	 * 
	 * @param jObj
	 * @return
	 */
	public static Geometry createGeometry(JsonObject jObj) {
		String typeStr = jObj.getString("type");
		GeojsonType type = GeojsonType.valueOf(typeStr);
		Geometry geometry = null;
		switch (type) {
		case Point:
			geometry = new Point(jObj);
			break;
		case MultiPoint:
			geometry = new MultiPoint(jObj);
			break;
		case LineString:
			geometry = new LineString(jObj);
			break;
		case MultiLineString:
			geometry = new MultiLineString(jObj);
			break;
		case Polygon:
			geometry = new Polygon(jObj);
			break;
		case MultiPolygon:
			geometry = new MultiPolygon(jObj);
			break;
		case GeometryCollection:
			geometry = new GeometryCollection(jObj);
			break;
		}

		return (geometry);
	}

}
