package gov.nasa.arc.dert.io.geojson;

import gov.nasa.arc.dert.io.geojson.json.Json;
import gov.nasa.arc.dert.io.geojson.json.JsonObject;
import gov.nasa.arc.dert.io.geojson.json.JsonReader;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.raster.SpatialReferenceSystem;
import gov.nasa.arc.dert.scene.LineSet;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scenegraph.GroupNode;
import gov.nasa.arc.dert.scenegraph.LineStrip;
import gov.nasa.arc.dert.view.Console;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
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
	private SpatialReferenceSystem srs;
	private double landscapeMinZ;

	/**
	 * Constructor
	 * 
	 * @param srs
	 *            the spatial reference system to be used for coordinates
	 */
	public GeojsonLoader(SpatialReferenceSystem srs) {
		this.srs = srs;
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
			Console.getInstance().println("Unable to load GeoJSON file " + filePath + ", see log.");
			e.printStackTrace();
		}
		return (null);
	}

	/**
	 * Convert a GeoJsonObject to a LineSet
	 * 
	 * @param gjRoot
	 *            the GeoJsonObject
	 * @param root
	 *            the LineSet
	 * @param pointColor
	 *            color for Points (points and multipoints are not currently
	 *            supported)
	 * @param lineColor
	 *            color for Lines
	 * @param elevAttrName
	 *            the elevation attribute name (from gdaldem)
	 * @return the LineSet
	 */
	public LineSet geoJsonToArdor3D(GeoJsonObject gjRoot, LineSet root, Color pointColor, Color lineColor,
		String elevAttrName) {
		// no elevation (Z) values so this will be 2D, make Z the minimum
		// landscape elevation
		landscapeMinZ = 0;
		if (elevAttrName == null) {
			World world = World.getInstance();
			if (world != null)
				landscapeMinZ = Landscape.getInstance().getMinimumElevation();
		}
		root.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		int count = 0;
		if (gjRoot instanceof GeoJsonFeature) {
			GeoJsonFeature feature = (GeoJsonFeature) gjRoot;
			Spatial spatial = geojsonFeatureToArdor3D(feature, pointColor, lineColor, elevAttrName, count);
			if (spatial != null) {
				root.attachChild(spatial);
				count++;
			}
		} else if (gjRoot instanceof GeoJsonFeatureCollection) {
			GeoJsonFeatureCollection collection = (GeoJsonFeatureCollection) gjRoot;
			ArrayList<GeoJsonFeature> featureList = collection.getFeatureList();
			for (int i = 0; i < featureList.size(); ++i) {
				Spatial spatial = geojsonFeatureToArdor3D(featureList.get(i), pointColor, lineColor, elevAttrName,
					count);
				if (spatial != null) {
					root.attachChild(spatial);
					count++;
				}
			}
		}
		Console console = Console.getInstance();
		if (console != null)
			console.println("Found " + count + " features for GeoJSON file " + filePath + ".");
		else
			System.out.println("Found " + count + " features for GeoJSON file " + filePath + ".");
		return (root);
	}

	private Spatial geojsonFeatureToArdor3D(GeoJsonFeature feature, Color pointColor, Color lineColor,
		String elevAttrName, int count) {
		ColorRGBA lineColorRGBA = new ColorRGBA(lineColor.getRed() / 255.0f, lineColor.getGreen() / 255.0f,
			lineColor.getBlue() / 255.0f, lineColor.getAlpha() / 255.0f);
		Spatial spatial = null;
		// this is a contour map, we have an elevation attribute from gdaldem
		boolean isContour = (elevAttrName != null);
		minZ = Double.MAX_VALUE;
		maxZ = -Double.MAX_VALUE;
		Geometry geometry = feature.getGeometry();
		if (geometry == null) {
			return (null);
		} else if (geometry instanceof LineString) {
			LineString lineString = (LineString) geometry;
			double[][] coordinate = lineString.getCoordinates();
			if (coordinate == null) {
				return (null);
			}
			if (coordinate.length == 0) {
				return (null);
			}
			FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(3 * coordinate.length);
			for (int i = 0; i < coordinate.length; ++i) {
				ReadOnlyVector3 coord = toWorld(coordinate[i], !isContour);
				if (coord != null) {
					vertexBuffer.put((float) coord.getX()).put((float) coord.getY()).put((float) coord.getZ());
					minZ = Math.min(minZ, coord.getZ());
					maxZ = Math.max(maxZ, coord.getZ());
				}
			}
			vertexBuffer.flip();
			if (vertexBuffer.limit() == 0) {
				return (null);
			}
			LineStrip line = new LineStrip("linestring" + count, vertexBuffer, null, null, null);
			line.setLineWidth(2);
			line.setModelBound(new BoundingBox());
			line.updateModelBound();
			line.setDefaultColor(lineColorRGBA);
			line.getSceneHints().setLightCombineMode(LightCombineMode.Off);

			// if this is a contour map put the line strip in a Contour object
			if (isContour) {
				HashMap<String, Object> properties = feature.getProperties();
				Object elevation = properties.get(elevAttrName);
				if (elevation != null) {
					spatial = new ContourLine(line, (Double) elevation, lineColorRGBA);
				} else {
					spatial = line;
				}
			} else {
				spatial = line;
//				System.err.println("GeojsonLoader.geojsonFeatureToArdor3D "+coordinate.length+" "+minZ+" "+maxZ+" "+line.getModelBound());
			}
		}
		// GeoJSON MultiLineString
		else if (geometry instanceof MultiLineString) {
			MultiLineString lineString = (MultiLineString) geometry;
			double[][][] coordinate = lineString.getCoordinates();
			if (coordinate == null) {
				return (null);
			}
			if (coordinate.length == 0) {
				return (null);
			}
			GroupNode group = new GroupNode("group" + count);
			for (int i = 0; i < coordinate.length; ++i) {
				if (coordinate[i].length == 0) {
					continue;
				}
				FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(3 * coordinate[i].length);
				for (int j = 0; j < coordinate[i].length; ++j) {
					ReadOnlyVector3 coord = toWorld(coordinate[i][j], !isContour);
					if (coord != null) {
						vertexBuffer.put((float) coord.getX()).put((float) coord.getY()).put((float) coord.getZ());
						minZ = Math.min(minZ, coord.getZ());
						maxZ = Math.max(maxZ, coord.getZ());
					}
				}
				vertexBuffer.flip();
				if (vertexBuffer.limit() == 0) {
					continue;
				}
				LineStrip line = new LineStrip("linestring" + i, vertexBuffer, null, null, null);
				line.setLineWidth(2);
				line.setModelBound(new BoundingBox());
				line.updateModelBound();
				line.setDefaultColor(lineColorRGBA);
				line.getSceneHints().setLightCombineMode(LightCombineMode.Off);
				if (isContour) {
					HashMap<String, Object> properties = feature.getProperties();
					Object elevation = properties.get(elevAttrName);
					if (elevation != null) {
						group.attachChild(new ContourLine(line, (Double) elevation, lineColorRGBA));
					} else {
						group.attachChild(line);
					}
				} else {
					group.attachChild(line);
				}
			}
			spatial = group;
		}
		return (spatial);
	}

	private ReadOnlyVector3 toWorld(double[] coordinate, boolean getZ) {
		coord.set(coordinate[0], coordinate[1], coordinate[2]);
		if (coordinate.length == 3) {
			srs.getProjection().worldToLocal(coord);
			coord.setZ(coord.getZ() - landscapeMinZ);
			return (coord);
		} else if (coordinate.length == 2) {
			coord.setZ(0);
			srs.getProjection().worldToLocal(coord);
			if (getZ) {
				coord.setZ(Landscape.getInstance().getZ(coord.getX(), coord.getY()));
			}
			if (Double.isNaN(coord.getZ())) {
				return (null);
			} else {
				return (coord);
			}
		} else {
			throw new IllegalArgumentException("GeoJSON Position has < 2 elements.");
		}
	}

}
