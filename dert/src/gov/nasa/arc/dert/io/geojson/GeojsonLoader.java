package gov.nasa.arc.dert.io.geojson;

import gov.nasa.arc.dert.io.geojson.json.GeoJsonFeature;
import gov.nasa.arc.dert.io.geojson.json.GeoJsonFeatureCollection;
import gov.nasa.arc.dert.io.geojson.json.GeoJsonObject;
import gov.nasa.arc.dert.io.geojson.json.Geometry;
import gov.nasa.arc.dert.io.geojson.json.GeometryCollection;
import gov.nasa.arc.dert.io.geojson.json.Json;
import gov.nasa.arc.dert.io.geojson.json.JsonObject;
import gov.nasa.arc.dert.io.geojson.json.JsonReader;
import gov.nasa.arc.dert.io.geojson.json.LineString;
import gov.nasa.arc.dert.io.geojson.json.MultiLineString;
import gov.nasa.arc.dert.io.geojson.json.MultiPoint;
import gov.nasa.arc.dert.io.geojson.json.MultiPolygon;
import gov.nasa.arc.dert.io.geojson.json.Point;
import gov.nasa.arc.dert.io.geojson.json.Polygon;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.srs.Projection;
import gov.nasa.arc.dert.landscape.srs.SpatialReferenceSystem;
import gov.nasa.arc.dert.scene.featureset.Feature;
import gov.nasa.arc.dert.scene.featureset.FeatureSet;
import gov.nasa.arc.dert.scenegraph.ContourLine;
import gov.nasa.arc.dert.scenegraph.FigureMarker;
import gov.nasa.arc.dert.scenegraph.GroupNode;
import gov.nasa.arc.dert.scenegraph.LineStrip;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.state.FeatureSetState;
import gov.nasa.arc.dert.state.FeatureState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.view.Console;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Provides a file loader for GeoJSON format.
 *
 */
public class GeojsonLoader {

	private String filePath;
	private double minZ, maxZ;
	private Vector3 coord = new Vector3();
	private double[] dcoord = new double[3];
	private SpatialReferenceSystem srs;
	private double landscapeMinZ;
	private String elevAttrName;
	private boolean ground;
	private float size, lineWidth;

	/**
	 * Constructor
	 * 
	 * @param srs
	 *            the spatial reference system to be used for coordinates
	 */
	public GeojsonLoader(SpatialReferenceSystem srs, String elevAttrName, boolean ground, float size, float lineWidth) {
		this.srs = srs;
		this.elevAttrName = elevAttrName;
		this.ground = ground;
		this.size = size;
		this.lineWidth = lineWidth;
	}

	/**
	 * Load a GeoJSON file
	 * 
	 * @param filePath
	 *            path to the file
	 * @return a GeoJSON object
	 */
	public GeoJsonObject load(String filePath) {
		this.filePath = filePath;
		File file = null;
		try {
			file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			JsonReader jsonReader = Json.createReader(fis);
			JsonObject root = jsonReader.readObject();
			jsonReader.close();
			fis.close();
			String type = root.getString("type");
			GeoJsonObject groot = null;
			if (type.equals("FeatureCollection")) {
				groot = new GeoJsonFeatureCollection(root);
			} else if (type.equals("Feature")) {
				groot = new GeoJsonFeature(root);
			}
			return (groot);
		} catch (Exception e) {
			Console.println("Unable to load GeoJSON file " + filePath + ", see log.");
			e.printStackTrace();
		}
		return (null);
	}

	/**
	 * Load a GeoJSON file
	 * 
	 * @param filePath
	 *            path to the file
	 * @return a GeoJSON object
	 */
	public GeoJsonObject load(InputStream inputStream) {
		this.filePath = null;
		try {
			JsonReader jsonReader = Json.createReader(inputStream);
			JsonObject root = jsonReader.readObject();
			jsonReader.close();
			inputStream.close();
			String type = root.getString("type");
			GeoJsonObject groot = null;
			if (type.equals("FeatureCollection")) {
				groot = new GeoJsonFeatureCollection(root);
			} else if (type.equals("Feature")) {
				groot = new GeoJsonFeature(root);
			}
			return (groot);
		} catch (Exception e) {
			Console.println("Unable to load GeoJSON input stream, see log.");
			e.printStackTrace();
		}
		return (null);
	}

	/**
	 * Convert a GeoJsonObject to a FeatureSet
	 * 
	 * @param gjRoot
	 *            the GeoJsonObject
	 * @param root
	 *            the FeatureSet
	 * @param pointColor
	 *            color for Points (points and multipoints are not currently
	 *            supported)
	 * @param lineColor
	 *            color for Lines
	 * @param elevAttrName
	 *            the elevation attribute name (from gdaldem)
	 * @return the FeatureSet
	 */
	public Node geoJsonToArdor3D(GeoJsonObject gjRoot, FeatureSet root, String labelProp) {
		Color color = root.getColor();
		if (labelProp == null)
			labelProp = ((FeatureSetState)root.getState()).labelProp;
					
		// Minimum landscape elevation
		landscapeMinZ = 0;
		if ((elevAttrName == null) && ground)
			landscapeMinZ = Landscape.getInstance().getMinimumElevation();

		int count = 0;
		Node result = null;
		if (gjRoot instanceof GeoJsonFeature) {
			GeoJsonFeature gjFeature = (GeoJsonFeature) gjRoot;
			Feature feature = geojsonFeatureToArdor3D(gjFeature, color, labelProp, count);
			if (feature != null) {
				root.attachChild(feature);
				count++;
			}
			result = feature;
		} else if (gjRoot instanceof GeoJsonFeatureCollection) {
			GeoJsonFeatureCollection collection = (GeoJsonFeatureCollection) gjRoot;
			ArrayList<GeoJsonFeature> featureList = collection.getFeatureList();
			for (int i = 0; i < featureList.size(); ++i) {
				GeoJsonFeature gjFeature = featureList.get(i);
				Feature feature = geojsonFeatureToArdor3D(gjFeature, color, labelProp, count);
				if (feature != null) {
					root.attachChild(feature);
					count++;
				}
			}
			result = root;
		}
		Collections.sort(root.getChildren(), new Comparator<Spatial>() {
			public int compare(Spatial spat1, Spatial spat2) {
				return(spat1.getName().compareTo(spat2.getName()));
			}
			public boolean equals(Object obj) {
				return(this == obj);
			}
		});
		root.setLabelVisible(true);
		if (Console.getInstance() != null)
			Console.println("Found " + count + " features for GeoJSON file " + filePath + ".");
		else
			System.out.println("Found " + count + " features for GeoJSON file " + filePath + ".");
		return (result);
	}

	private Feature geojsonFeatureToArdor3D(GeoJsonFeature gjFeature, Color color, String labelProp, int count) {
		minZ = Double.MAX_VALUE;
		maxZ = -Double.MAX_VALUE;
		Geometry geometry = gjFeature.getGeometry();
		if (geometry == null)
			return (null);
		String name = null;
		if (labelProp != null) {
			Object obj = gjFeature.getProperties().get(labelProp);
			if (obj != null)
				name = obj.toString();
		}
		if ((name == null) || name.isEmpty())
			name = gjFeature.getId();
		if (name == null)
			name = "Feature"+count;
		FeatureState fState = new FeatureState(count, name, Type.Feature, "Feature", color);
		Feature feature = new Feature(fState, gjFeature.getProperties());
		if (geojsonGeometryToArdor3D(feature, geometry, color, count, feature.getProperties())) {
			return (feature);
		}
		return(null);
	}

	private boolean geojsonGeometryToArdor3D(Node parent, Geometry geometry, Color color, int count, HashMap<String, Object> properties) {
		// this is a contour map, we have an elevation attribute from gdaldem
		boolean isContour = (elevAttrName != null);
		LineStrip lineStrip = null;
		ReadOnlyVector3 pos = null;
		switch (geometry.type) {
		case Point:
			Point point = (Point) geometry;
			double[] pCoord = point.getCoordinates();
			if (pCoord == null)
				return (false);
			if (pCoord.length == 0)
				return (false);
			pos = toWorld(pCoord, ground);
			if (pos != null) {
				FigureMarker fm = new FigureMarker(parent.getName(), pos, size, 0, color, false, true, true);
				fm.setShape(ShapeType.crystal);
				fm.setAutoShowLabel(true);
				parent.attachChild(fm);
				minZ = pos.getZ();
				maxZ = pos.getZ();
			}
			break;
			
		case MultiPoint:
			MultiPoint mPoint = (MultiPoint) geometry;
			double[][] mpCoord = mPoint.getCoordinates();
			if (mpCoord == null) {
				return (false);
			}
			if (mpCoord.length == 0) {
				return (false);
			}
			for (int i = 0; i < mpCoord.length; ++i) {
				if (mpCoord[i].length == 0) {
					continue;
				}
				pos = toWorld(mpCoord[i], ground);
				if (coord != null) {
					FigureMarker fm = new FigureMarker(parent.getName()+i, pos, size, 0, color, false, true, true);
					fm.setShape(ShapeType.crystal);
					fm.setAutoShowLabel(true);
					parent.attachChild(fm);
					minZ = Math.min(minZ, pos.getZ());
					maxZ = Math.max(maxZ, pos.getZ());
				}
			}
			break;
			
		case LineString:
			LineString lineString = (LineString) geometry;
			double[][] lsCoord = lineString.getCoordinates();
			if (lsCoord == null)
				return (false);
			if (lsCoord.length == 0)
				return (false);
			lineStrip = createLineStrip("_geom", lsCoord, color);
			if (lineStrip == null)
				return(false);

			// if this is a contour map put the line strip in a Contour object
			if (isContour) {
				Object elevation = properties.get(elevAttrName);
				if (elevation != null) {
					double el = ((Number)elevation).doubleValue();
					parent.attachChild(new ContourLine(lineStrip, el, color));
				} else {
					parent.attachChild(lineStrip);
				}
			} else {
				parent.attachChild(lineStrip);
//				System.err.println("GeojsonLoader.geojsonFeatureToArdor3D "+coordinate.length+" "+minZ+" "+maxZ+" "+line.getModelBound());
			}
			break;
			
		case MultiLineString:
			MultiLineString multilineString = (MultiLineString) geometry;
			double[][][] mlsCoord = multilineString.getCoordinates();
			if (mlsCoord == null) {
				return (false);
			}
			if (mlsCoord.length == 0) {
				return (false);
			}
			for (int i = 0; i < mlsCoord.length; ++i) {
				if (mlsCoord[i].length == 0) {
					continue;
				}
				lineStrip = createLineStrip("_geom"+i, mlsCoord[i], color);
				if (lineStrip == null)
					continue;
				if (isContour) {
					Object elevation = properties.get(elevAttrName);
					if (elevation != null) {
						double el = ((Number)elevation).doubleValue();
						parent.attachChild(new ContourLine(lineStrip, el, color));
					} else {
						parent.attachChild(lineStrip);
					}
				} else {
					parent.attachChild(lineStrip);
				}
			}
			break;
			
		case Polygon:
			Polygon polygon = (Polygon) geometry;
			double[][][] plyCoord = polygon.getCoordinates();
			if (plyCoord == null) {
				return (false);
			}
			if (plyCoord.length == 0) {
				return (false);
			}
			for (int i = 0; i < plyCoord.length; ++i) {
				if (plyCoord[i].length == 0) {
					continue;
				}
				lineStrip = createLineStrip("_geom"+i, plyCoord[i], color);
				if (lineStrip != null)
					parent.attachChild(lineStrip);
			}
			break;
			
		case MultiPolygon:
			MultiPolygon multiPolygon = (MultiPolygon) geometry;
			double[][][][] mplyCoord = multiPolygon.getCoordinates();
			if (mplyCoord == null) {
				return (false);
			}
			if (mplyCoord.length == 0) {
				return (false);
			}
			for (int i = 0; i < mplyCoord.length; ++i) {
				if (mplyCoord[i].length == 0) {
					continue;
				}
				for (int j=0; j<mplyCoord[i].length; ++j) {
					if (mplyCoord[i][j].length == 0)
						continue;
					lineStrip = createLineStrip("_geom"+i+"."+j, mplyCoord[i][j], color);
					if (lineStrip != null)
						parent.attachChild(lineStrip);
				}
			}
			break;
			
		case GeometryCollection:
			GeometryCollection geometryCollection = (GeometryCollection) geometry;
			ArrayList<Geometry> geometryList = geometryCollection.getGeometryList();
			if (geometryList.size() == 0)
				return(false);
			GroupNode group = new GroupNode("_geom");
			for (int i=0; i<geometryList.size(); ++i) {
				Geometry geom = geometryList.get(i);
				geojsonGeometryToArdor3D(group, geom, color, count, properties);
			}
			parent.attachChild(group);
			break;
			
		}
		return(true);
		
	}

	private ReadOnlyVector3 toWorld(double[] coordinate, boolean getZ) {
		if (coordinate.length == 3) {
			translate(coordinate);
			coord.set(coordinate[0], coordinate[1], coordinate[2]);
			srs.getProjection().worldToLocal(coord);
			if (getZ)
				coord.setZ(Landscape.getInstance().getZ(coord.getX(), coord.getY()));
			else
				coord.setZ(coord.getZ() - landscapeMinZ);
		} else if (coordinate.length == 2) {
			translate(coordinate);
			coord.set(coordinate[0], coordinate[1], 0);
			srs.getProjection().worldToLocal(coord);
			if (getZ)
				coord.setZ(Landscape.getInstance().getZ(coord.getX(), coord.getY()));
		} else {
			throw new IllegalArgumentException("GeoJSON Position has < 2 elements.");
		}
		if (Double.isNaN(coord.getZ())) {
			return (null);
		} else {
			return (coord);
		}
	}
	
	private LineStrip createLineStrip(String name, double[][] coord, Color color) {
		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(3 * coord.length);
		for (int i = 0; i < coord.length; ++i) {
			ReadOnlyVector3 pos = toWorld(coord[i], ground);
			if (pos != null) {
				vertexBuffer.put(pos.getXf()).put(pos.getYf()).put(pos.getZf());
				minZ = Math.min(minZ, pos.getZ());
				maxZ = Math.max(maxZ, pos.getZ());
			}
		}
		vertexBuffer.flip();
		if (vertexBuffer.limit() > 0) {
			LineStrip lineStrip = new LineStrip(name, vertexBuffer, null, null, null);
			lineStrip.setLineWidth(lineWidth);
			lineStrip.setModelBound(new BoundingBox());
			lineStrip.updateModelBound();
			lineStrip.setColor(color);
			lineStrip.getSceneHints().setLightCombineMode(LightCombineMode.Off);
			return(lineStrip);
		}
		return(null);
	}
	
	public void translate(double[] coordinate) {
		Projection projection = srs.getProjection();
		if (coordinate.length == 3)
			projection.sphericalToWorld(coordinate);
		else {
			dcoord[0] = coordinate[0];
			dcoord[1] = coordinate[1];
			dcoord[2] = 0;
			projection.sphericalToWorld(dcoord);
			coordinate[0] = dcoord[0];
			coordinate[1] = dcoord[1];
		}
	}				

}
