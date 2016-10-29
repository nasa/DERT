package gov.nasa.arc.dert.landscape.factory;

import gov.nasa.arc.dert.landscape.LayerInfo.LayerType;
import gov.nasa.arc.dert.raster.ProjectionInfo;
import gov.nasa.arc.dert.raster.SpatialReferenceSystem;
import gov.nasa.arc.dert.render.JoglRendererDouble;
import gov.nasa.arc.dert.scene.featureset.FeatureSet;
import gov.nasa.arc.dert.scenegraph.GroupNode;
import gov.nasa.arc.dert.state.FeatureSetState;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

import javax.swing.JTextField;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;

/**
 * Convert a vector layer file in GeoJSON format to a multi-resolution tiled
 * pyramid. Vectors are drawn into an off screen renderer for each tile at each
 * resolution level.
 *
 */
public class VectorPyramidLayerFactory extends PyramidLayerFactory {

	// Spatial reference system for referencing vectors to landscape
	protected SpatialReferenceSystem srs;

	// Ardor3D root of vector scenegraph
	protected GroupNode root;

	// Dimensions
	protected int tileWidth, tileLength, tileWidth1, tileLength1, bytesPerPixel;

	// Tile renderer
	protected OffscreenRenderer offscreenRenderer;
	
	// Clipping planes
	protected double near, far, cameraZ;

	/**
	 * Constructor
	 * 
	 * @param filePath
	 *            path to source file
	 */
	public VectorPyramidLayerFactory(String filePath) {
		super(filePath);
		minimumSampleValue = new double[] { 0 };
		maximumSampleValue = new double[] { 255 };
	}

	/**
	 * Build the pyramid layer.
	 * 
	 * @param landPath
	 *            path to the destination landscape
	 * @param layerName
	 *            name of this layer
	 * @param baseProperties
	 *            properties from landscape elevation layer
	 * @param color
	 *            default color for vectors
	 * @param isContour
	 *            true = this is a contour map
	 * @param messageText
	 *            UI text field for messages to the user
	 * @throws IOException
	 */
	public void buildPyramid(String landPath, String layerName, Color color, String elevAttrName, JTextField messageText)
		throws IOException {
		// Get the elevation properties
		File elevDir = new File(landPath, "elevation");
		File propFile = new File(elevDir, "layer.properties");
		String propertiesPath = propFile.getAbsolutePath();
		Properties baseProperties = new Properties();
		baseProperties.load(new FileInputStream(propertiesPath));

		long t = System.currentTimeMillis();

		projInfo = new ProjectionInfo();
		projInfo.loadFromProperties(baseProperties);
		int numLevels = StringUtil.getIntegerValue(baseProperties, "NumberOfLevels", true, 0, true);
		int maxLevel = numLevels - 1;
		tileWidth = StringUtil.getIntegerValue(baseProperties, "TileWidth", true, 0, true);
		tileLength = StringUtil.getIntegerValue(baseProperties, "TileLength", true, 0, true);
		int numberOfTiles = StringUtil.getIntegerValue(baseProperties, "NumberOfTiles", true, 0, true);
		bytesPerPixel = 4;
		layerType = LayerType.colorimage;
		
		minimumSampleValue[0] = StringUtil.getDoubleValue(baseProperties, "MinimumValue", false, 0, true);
		maximumSampleValue[0] = StringUtil.getDoubleValue(baseProperties, "MaximumValue", false, 0, true);
		near = (maximumSampleValue[0]-minimumSampleValue[0])*0.0001;
		far = 2*(maximumSampleValue[0]-minimumSampleValue[0]);
		cameraZ = maximumSampleValue[0]+2*near;

		double xScale = projInfo.scale[0];
		double yScale = projInfo.scale[1];
		double leftEdge = -projInfo.rasterWidth * xScale / 2;
		double topEdge = projInfo.rasterLength * yScale / 2;

		tileWidth *= 4;
		tileWidth1 = tileWidth + 1;
		tileLength *= 4;
		tileLength1 = tileLength + 1;
		xScale /= 4;
		yScale /= 4;

		srs = new SpatialReferenceSystem(projInfo);

		offscreenRenderer = new OffscreenRenderer(new DisplaySettings(tileWidth1, tileLength1, bytesPerPixel * 8, 0,
			false), new JoglRendererDouble(), ColorRGBA.BLACK_NO_ALPHA);

		// Load the vector file into an Ardor3D object.
		FeatureSetState state = new FeatureSetState(layerName, sourceFilePath, color, true, null);
		root = new FeatureSet(state, elevAttrName, srs);
		root.updateGeometricState(0);

		// Create a sub-directory for the layer
		String dirPath = new File(landPath, layerName).getAbsolutePath();

		// Write tiles for each level starting at highest resolution
		int numTiles = numberOfTiles;
		doIt = true;
		for (int level = maxLevel; level >= 0; level--) {
			if (!doIt) {
				break;
			}
			double columnStep = tileWidth * xScale;
			double rowStep = tileLength * yScale;
			int rcnt = 0;
			if (messageText == null)
				System.out.println("Writing "+numTiles+" rows for level "+(level+1)+" of "+(maxLevel+1));
			for (int r = 0; r < numTiles; ++r) {
				if (messageText == null) {
					if (rcnt%10 == 0)
						System.out.print(rcnt);
					else 
						System.out.print(".");
					rcnt ++;
				}
				for (int c = 0; c < numTiles; ++c) {
					if (!doIt) {
						break;
					}
					if (messageText != null) {
						messageText.setText("Writing " + layerName + " level " + (level + 1) + " of " + (maxLevel + 1)
							+ ", tile row " + (r + 1) + " of " + numTiles + " . . .");
						Thread.yield();
					}
					String tilePath = getTileFilePath(c, r, numTiles, level, dirPath);
					double x = leftEdge + columnStep * c + columnStep / 2;
					double y = topEdge - rowStep * r - rowStep / 2;
					ByteBuffer raster = createRaster(x, y, xScale, yScale);
					writeTile(raster, offscreenRenderer.getWidth(), offscreenRenderer.getHeight(), tilePath);
				}
			}
			if (messageText == null)
				System.out.println();
			numTiles /= 2;
			xScale *= 2;
			yScale *= 2;
		}

		projInfo.rasterWidth = tileWidth * numberOfTiles;
		projInfo.rasterLength = tileLength * numberOfTiles;

		if (doIt) {
			writeProperties(new File(dirPath, "layer.properties").getAbsolutePath(), maxLevel + 1, tileWidth,
				tileLength, numberOfTiles);
		}
		System.out.println("Number of levels for " + layerName + " = " + (maxLevel + 1) + " with " + numberOfTiles
			+ " tiles per side at the highest resolution level.");
		int nt = 0;
		int n = 1;
		for (int i = 0; i <= maxLevel; ++i) {
			nt += n;
			n *= 4;
		}

		offscreenRenderer.cleanup();

		// report
		System.out.println("Total number of tiles for " + layerName + " = " + nt + " using "
			+ (((double)nt*tileWidth1*tileLength1*bytesPerPixel) / 1073741824.0) + " GB.");
		System.out.println("Total time for building " + layerName + " = "
			+ (float) ((System.currentTimeMillis() - t) / 60000.0) + " minutes.");
	}

	/**
	 * Write out a tile.
	 * 
	 * @param raster
	 *            the raster array from which the tile comes
	 * @param tilePath
	 *            the file path for the tile
	 * @throws IOException
	 */
	protected void writeTile(ByteBuffer raster, int width, int height, String tilePath) throws IOException {

		// zero the file if it is only missing values
		// if (isEmpty(raster))
		// raster.limit(0);

		// allocate buffer for writing the file
		byte[] bbArray = new byte[raster.limit()];
		raster.get(bbArray);

		writeTile(tilePath, bbArray, width, height, layerType);
	}

	/**
	 * Create the tile by rendering to an offscreen buffer.
	 * 
	 * @param x
	 *            camera X location
	 * @param y
	 *            camera Y location
	 * @param camWidth
	 *            fov width
	 * @param camHeight
	 *            fov height
	 * @return
	 */
	protected ByteBuffer createRaster(double x, double y, double xScale, double yScale) {

		try {
			// setup the off screen renderer camera
			Camera tCam = offscreenRenderer.getCamera();
//			tCam.setProjectionMode(ProjectionMode.Parallel);
			Vector3 camLocation = new Vector3(x, y, cameraZ);
			tCam.setFrame(camLocation, Vector3.NEG_UNIT_X, Vector3.UNIT_Y, Vector3.NEG_UNIT_Z);
			double right = (tileWidth * xScale) / 2 + xScale;
			double top = (tileLength * yScale) / 2 + yScale;
			tCam.setFrustum(near, far, -right, right, top, -top);
			tCam.update();

			// // set the background to transparent
			// offscreenRenderer.setBackgroundColor(ColorRGBA.BLACK_NO_ALPHA);

			// render the tile
			offscreenRenderer.render(root, Renderer.BUFFER_COLOR_AND_DEPTH);

			// return the image
			return (offscreenRenderer.getRGBABuffer());
		} catch (Exception e) {
			System.out.println("Unable to render tile.");
			e.printStackTrace();
			return (null);
		}
	}

}
