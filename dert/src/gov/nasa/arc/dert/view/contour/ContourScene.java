package gov.nasa.arc.dert.view.contour;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.render.BasicScene;
import gov.nasa.arc.dert.render.ColorTableEffects;
import gov.nasa.arc.dert.scene.tool.Plane;
import gov.nasa.arc.dert.scenegraph.GroupNode;
import gov.nasa.arc.dert.state.PlaneState;
import gov.nasa.arc.dert.util.ColorMap;
import gov.nasa.arc.dert.util.ColorMapListener;
import gov.nasa.arc.dert.util.ImageUtil;
import gov.nasa.arc.dert.viewpoint.BasicCamera;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.image.Image;
import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.image.PixelDataType;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture1D;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.intersection.IntersectionRecord;
import com.ardor3d.intersection.PickData;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera.ProjectionMode;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.util.TextureKey;

/**
 * Ardor3D scene for contour display.
 *
 */
public class ContourScene extends BasicScene implements ColorMapListener {

	// Plane generating the difference map
	private Plane plane;

	// Camera for this scene
	private BasicCamera camera;

	// Buffers
	private ByteBuffer byteBuffer, table;
	private int imageSize, lutSize = 256;

	// Color map used for the contour
	private ColorMap colorMap;

	// Location of difference map pixels in the texture
	private int offX, offY, rows, columns;

	// Range
	private float[] minMaxElev = { 0, 100 };

	// Mesh to hold texture
	private Quad quad;

	// Texture to display contours
	private Texture texture;

	// Texture to act as a lookup table
	private Texture colorTable;

	// Texture
	private TextureState textureState;
	private Image textureImage, colorTableImage;
	private float[][] diff;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public ContourScene(PlaneState state) {
		setRootNode(new GroupNode(state.toString()));
		plane = (Plane) state.getMapElement();
		camera = new BasicCamera(1, 1);
		camera.setProjectionMode(ProjectionMode.Parallel);
		colorMap = new ColorMap(state.colorMapName, plane.getName(), minMaxElev[0], minMaxElev[1], state.minimum,
			state.maximum, state.gradient);
		colorMap.addListener(this);
	}

	/**
	 * Initialize this scene with a canvas renderer
	 */
	@Override
	public void init(CanvasRenderer canvasRenderer) {
		canvasRenderer.getRenderer().setBackgroundColor(ColorRGBA.BLACK);

		imageSize = ImageUtil.getMaxTextureRendererSize() / 2;

		// Allocate memory
		byteBuffer = ByteBuffer.allocateDirect(imageSize * imageSize);
		table = ByteBuffer.allocateDirect(lutSize * 4);
		table.order(ByteOrder.nativeOrder());

		// Create the mesh to hold the display texture
		quad = new Quad("quad", imageSize, imageSize);
		quad.setModelBound(new BoundingBox());
		quad.updateModelBound();
		quad.setTranslation(0, 0, -1);
		quad.setDefaultColor(ColorRGBA.BLACK);

		// Create the display texture
		texture = new Texture2D();
		texture.setApply(Texture.ApplyMode.Modulate);
		texture.setWrap(Texture.WrapMode.EdgeClamp);
		texture.setMinificationFilter(Texture.MinificationFilter.NearestNeighborNoMipMaps);
		texture.setMagnificationFilter(Texture.MagnificationFilter.NearestNeighbor);
		texture.setTextureStoreFormat(TextureStoreFormat.Luminance8);
		texture.setTextureKey(TextureKey.getRTTKey(Texture.MinificationFilter.NearestNeighborNoMipMaps));
		ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>(1);
		list.add(byteBuffer);
		textureImage = new Image(ImageDataFormat.Luminance, PixelDataType.UnsignedByte, imageSize, imageSize, list,
			null);
		texture.setImage(textureImage);

		// Create the color look up table texture
		colorTable = new Texture1D();
		colorTable.setWrap(Texture.WrapMode.EdgeClamp);
		colorTable.setTextureStoreFormat(TextureStoreFormat.RGBA8);
		colorTable.setMinificationFilter(Texture.MinificationFilter.NearestNeighborNoMipMaps);
		colorTable.setMagnificationFilter(Texture.MagnificationFilter.NearestNeighbor);
		colorTable.setTextureKey(TextureKey.getRTTKey(Texture.MinificationFilter.NearestNeighborNoMipMaps));
		list = new ArrayList<ByteBuffer>(1);
		list.add(table);
		colorTableImage = new Image(ImageDataFormat.RGBA, PixelDataType.UnsignedByte, lutSize, 1, list, null);
		colorTable.setImage(colorTableImage);

		// Setup the textures
		textureState = new TextureState();
		textureState.setTexture(texture, 0);
		textureState.setTexture(colorTable, 1);
		textureState.setEnabled(true);
		quad.setRenderState(textureState);
		ColorTableEffects colorTableEffects = new ColorTableEffects(0, 1);
		colorTableEffects.setEnabled(true);
		quad.setRenderState(colorTableEffects);
		rootNode.attachChild(quad);
		rootNode.updateGeometricState(0);
	}

	/**
	 * Get the color map used with this contour map
	 * 
	 * @return
	 */
	public ColorMap getColorMap() {
		return (colorMap);
	}

	/**
	 * Get the plane used with this contour map
	 * 
	 * @return
	 */
	public Plane getPlane() {
		return (plane);
	}

	/**
	 * Get the elevation difference data and update the contours
	 */
	public void updateContour() {
		diff = new float[imageSize][imageSize];
		int[] dim = plane.getElevationDifference(imageSize, diff, minMaxElev);
		rows = dim[0];
		columns = dim[1];
		offX = (imageSize - columns) / 2;
		offY = (imageSize - rows) / 2;
		double min = Math.floor(minMaxElev[0]);
		double max = Math.ceil(minMaxElev[1]);
		byteBuffer.clear();
		for (int i = 0; i < offY; ++i) {
			for (int j = 0; j < imageSize; ++j) {
				byteBuffer.put((byte) 0);
			}
		}
		for (int i = 0; i < rows; ++i) {
			for (int j = 0; j < offX; ++j) {
				byteBuffer.put((byte) 0);
			}
			for (int j = 0; j < columns; ++j) {
				if (Float.isNaN(diff[i][j])) {
					byteBuffer.put((byte) 0);
				} else {
					int b = (int) (1 + (lutSize - 2) * (diff[i][j] - min) / (max - min));
					if ((b < 1) || (b > 255)) {
						System.err.println("ContourScene.updateContour " + b + " " + i + " " + j + " " + diff[i][j]
							+ " " + minMaxElev[0] + " " + minMaxElev[1]);
					}
					byteBuffer.put((byte) b);
				}
			}
			for (int j = columns + offX; j < imageSize; ++j) {
				byteBuffer.put((byte) 0);
			}
		}
		for (int i = rows + offY; i < imageSize; ++i) {
			for (int j = 0; j < imageSize; ++j) {
				byteBuffer.put((byte) 0);
			}
		}
		byteBuffer.rewind();
		texture.setImage(textureImage);
		colorMap.setBaseMinimum(min);
		colorMap.setBaseMaximum(max);
		colorMap.setRange(min, max);
	}

	@Override
	public void render(Renderer renderer) {
//		camera.update();
//		camera.apply(renderer);
//		renderer.clearBuffers(Renderer.BUFFER_COLOR_AND_DEPTH);
		renderer.draw(rootNode);
	}

	/**
	 * The color map colors changed
	 */
	@Override
	public void mapChanged(ColorMap cMap) {
		cMap.getColorTable(lutSize, table);
		colorTable.setImage(colorTableImage);
		sceneChanged.set(true);
	}

	/**
	 * The color map range changed
	 */
	@Override
	public void rangeChanged(ColorMap cMap) {
		cMap.getColorTable(lutSize, table);
		colorTable.setImage(colorTableImage);
		sceneChanged.set(true);
	}

	/**
	 * The view was resized
	 */
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		camera.resize(width, height);
	}

	/**
	 * Get the camera
	 */
	@Override
	public BasicCamera getCamera() {
		return (camera);
	}

	/**
	 * Single-click in the scene. Get landscape coordinates at picked point.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Vector3 getPickCoords(double x, double y) {
		Vector2 mousePos = new Vector2(x, y);
		Ray3 pickRay = new Ray3();
		camera.getPickRay(mousePos, false, pickRay);
		PickResults pickResults = doPick(pickRay);
		if (pickResults == null) {
			return (null);
		}
		if (pickResults.getNumber() > 0) {
			double dist = Double.MAX_VALUE;
			int index = 0;
			IntersectionRecord record = null;
			for (int j = 0; j < pickResults.getNumber(); j++) {
				PickData pd = pickResults.getPickData(j);
				IntersectionRecord ir = pd.getIntersectionRecord();
				int closestIndex = ir.getClosestIntersection();
				double d = ir.getIntersectionDistance(closestIndex);
				if (d < dist) {
					dist = d;
					index = closestIndex;
					record = ir;
				}
			}
			if (record == null) {
				return (null);
			}
			ReadOnlyVector3 pos = record.getIntersectionPoint(index);
			double pX = pos.getX() + (imageSize / 2) - offX;
			double pY = pos.getY() + (imageSize / 2) - offY;
			if ((pX < 0) || (pX > columns)) {
				return (null);
			}
			pX /= columns;
			if ((pY < 0) || (pY > rows)) {
				return (null);
			}
			pY /= rows;
			ReadOnlyVector3 lowerBound = plane.getLowerBound();
			ReadOnlyVector3 upperBound = plane.getUpperBound();

			pX = lowerBound.getX() + (upperBound.getX() - lowerBound.getX()) * pX;
			pY = lowerBound.getY() + (upperBound.getY() - lowerBound.getY()) * pY;

			double pZ = Landscape.getInstance().getZ(pX, pY);

			return (new Vector3(pX, pY, pZ));
		} else {
			return (null);
		}
	}

	/**
	 * Cursor moved, get coordinates under cursor.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Vector3 getCoords(int x, int y) {
		Vector2 mousePos = new Vector2(x, y);
		ReadOnlyVector3 pos = camera.getWorldCoordinates(mousePos, 0);
		System.err.println("ContourScene.getCoords "+x+" "+y+" "+pos+" "+offX+" "+offY+" "+imageSize);
		double pX = pos.getX() + (imageSize / 2) - offX;
		double pY = pos.getY() + (imageSize / 2) - offY;
		if ((pX < 0) || (pX > columns)) {
			return (null);
		}
		if ((pY < 0) || (pY > rows)) {
			return (null);
		}
		double pZ = diff[(int) pY][(int) pX];
		pX /= columns;
		pY /= rows;
		ReadOnlyVector3 lowerBound = plane.getLowerBound();
		ReadOnlyVector3 upperBound = plane.getUpperBound();

		pX = lowerBound.getX() + (upperBound.getX() - lowerBound.getX()) * pX;
		pY = lowerBound.getY() + (upperBound.getY() - lowerBound.getY()) * pY;

		return (new Vector3(pX, pY, pZ));
	}

}