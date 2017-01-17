package gov.nasa.arc.dert.io.geojson;

import gov.nasa.arc.dert.io.geojson.json.Json;
import gov.nasa.arc.dert.io.geojson.json.JsonObject;
import gov.nasa.arc.dert.io.geojson.json.JsonReader;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.raster.SpatialReferenceSystem;
import gov.nasa.arc.dert.scene.featureset.Feature;
import gov.nasa.arc.dert.scene.featureset.FeatureSet;
import gov.nasa.arc.dert.scenegraph.FigureMarker;
import gov.nasa.arc.dert.scenegraph.GroupNode;
import gov.nasa.arc.dert.scenegraph.LineStrip;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.view.Console;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
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
	private String labelProp;
	private double minZ, maxZ;
	private Vector3 coord = new Vector3();
	private CoordinateReferenceSystem crs;
	private SpatialReferenceSystem srs;
	private double landscapeMinZ;
	private String elevAttrName;
	private boolean ground;
	private float size, lineWidth;
	
//	private Texture texture;

	/**
	 * Constructor
	 * 
	 * @param srs
	 *            the spatial reference system to be used for coordinates
	 */
	public GeojsonLoader(SpatialReferenceSystem srs, String elevAttrName, String labelProp, boolean ground, float size, float lineWidth) {
		this.srs = srs;
		this.elevAttrName = elevAttrName;
		this.ground = ground;
		this.labelProp = labelProp;
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
//		texture = ImageUtil.createTexture(ImageBoard.defaultImagePath, true);
//		texture.setApply(ApplyMode.Modulate);
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
	public FeatureSet geoJsonToArdor3D(GeoJsonObject gjRoot, FeatureSet root, Color color, boolean isProjected) {
		crs = gjRoot.crs;
		if (!isProjected && (crs == null))
			crs = new CoordinateReferenceSystem(Landscape.getInstance().getSpatialReferenceSystem().getProjection());
		// Minimum landscape elevation
		landscapeMinZ = 0;
		if ((elevAttrName == null) && ground)
			landscapeMinZ = Landscape.getInstance().getMinimumElevation();

		int count = 0;
		if (gjRoot instanceof GeoJsonFeature) {
			GeoJsonFeature gjFeature = (GeoJsonFeature) gjRoot;
			Feature feature = geojsonFeatureToArdor3D(gjFeature, color, count);
			if (feature != null) {
				root.attachChild(feature);
				count++;
			}
		} else if (gjRoot instanceof GeoJsonFeatureCollection) {
			GeoJsonFeatureCollection collection = (GeoJsonFeatureCollection) gjRoot;
			ArrayList<GeoJsonFeature> featureList = collection.getFeatureList();
			for (int i = 0; i < featureList.size(); ++i) {
				GeoJsonFeature gjFeature = featureList.get(i);
				Feature feature = geojsonFeatureToArdor3D(gjFeature, color, count);
				if (feature != null) {
					root.attachChild(feature);
					count++;
				}
			}
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
		return (root);
	}

	private Feature geojsonFeatureToArdor3D(GeoJsonFeature gjFeature, Color color, int count) {
		minZ = Double.MAX_VALUE;
		maxZ = -Double.MAX_VALUE;
		Geometry geometry = gjFeature.getGeometry();
		if (geometry == null)
			return (null);
		String name = null;
		if (labelProp != null)
			name = gjFeature.getProperties().get(labelProp).toString();
		if ((name == null) || name.isEmpty())
			name = gjFeature.getId();
		if (name == null)
			name = "Feature"+count;
		Feature feature = new Feature(name, color, gjFeature.getProperties());
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
//				fm.setAutoShowLabel(false);
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
//					fm.setAutoShowLabel(false);
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

//	private ReadOnlyVector3 toWorld(double[] coordinate, boolean getZ) {
//		if (coordinate.length == 3) {
//			coord.set(coordinate[0], coordinate[1], coordinate[2]);
//			srs.getProjection().worldToLocal(coord);
//			coord.setZ(coord.getZ() - landscapeMinZ);
//			return (coord);
//		} else if (coordinate.length == 2) {
//			coord.set(coordinate[0], coordinate[1], 0);
//			srs.getProjection().worldToLocal(coord);
//			if (getZ) {
//				coord.setZ(Landscape.getInstance().getZ(coord.getX(), coord.getY()));
//			}
//			if (Double.isNaN(coord.getZ())) {
//				return (null);
//			} else {
//				return (coord);
//			}
//		} else {
//			throw new IllegalArgumentException("GeoJSON Position has < 2 elements.");
//		}
//	}

	private ReadOnlyVector3 toWorld(double[] coordinate, boolean getZ) {
		if (coordinate.length == 3) {
			coord.set(coordinate[0], coordinate[1], coordinate[2]);
			if (crs != null)
				crs.translate(coordinate, coord);
			srs.getProjection().worldToLocal(coord);
			if (getZ)
				coord.setZ(Landscape.getInstance().getZ(coord.getX(), coord.getY()));
			else
				coord.setZ(coord.getZ() - landscapeMinZ);
		} else if (coordinate.length == 2) {
			coord.set(coordinate[0], coordinate[1], 0);
			if (crs != null)
				crs.translate(coordinate, coord);
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

}
