package gov.nasa.arc.dert.scene.tool;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.QuadTree;
import gov.nasa.arc.dert.scenegraph.BillboardMarker;
import gov.nasa.arc.dert.scenegraph.HiddenLine;
import gov.nasa.arc.dert.scenegraph.Marker;
import gov.nasa.arc.dert.scenegraph.MotionListener;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.state.PlaneState;
import gov.nasa.arc.dert.util.ImageUtil;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.SpatialUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.util.UIUtil;
import gov.nasa.arc.dert.view.mapelement.PlanePanel;
import gov.nasa.arc.dert.viewpoint.BasicCamera;
import gov.nasa.arc.dert.viewpoint.ViewDependent;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.Properties;

import javax.swing.Icon;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.image.Texture;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.scenegraph.hint.TextureCombineMode;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Provides a map element that is a plane. This plane is configured with three
 * points that can be positioned on the terrain.
 *
 */
public class Plane extends Node implements Tool, ViewDependent {

	// Representative icon
	public static final Icon icon = Icons.getImageIcon("plane.png");

	// Default map element properties
	public static float defaultSize = 100;
	public static Color defaultColor = Color.cyan;
	public static boolean defaultLabelVisible = true;
	public static boolean defaultTriangleVisible = true;
	public static String defaultColorMap = "default0";
	public static boolean strikeAsCompassBearing = true;

	// Point texture
	protected static Texture texture, highlightTexture;

	// Scene graph element to draw triangle
	private HiddenLine triangleLine;

	// Scene graph element to draw strike/dip line
	private Line[] strikeLine, dipLine;

	// Scene graph element to draw polygon (area) body
	private Mesh poly;

	// Scene graph element group that manages and renders points
	private BillboardMarker[] point;

	// map element properties for rendering
	private ColorRGBA colorRGBA;
	private Color color;
	private double size;
	private boolean labelVisible, pinned, triangleVisible;
	private double lengthScale = 1, widthScale = 1;

	// The map element state object
	protected PlaneState state;

	// For slope, aspect and viewpoint
	private volatile double strike, dip;
	private Vector3 workVec;
	private double[] planeEq = new double[4];

	// Helper fields
	private Vector3 p0Loc, p1Loc, p2Loc, p3Loc, normal, centroid, strikeDir, dipDir;
	private Vector3 lowerBound, upperBound;
	private Vector3 tmp0, tmp1;
	private Matrix3 rotMat;
	
	// For display
	private PlanePanel planePanel;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public Plane(PlaneState pState) {
		super(pState.name);
		this.state = pState;
		state.setMapElement(this);

		this.size = state.size;
		this.labelVisible = state.labelVisible;
		this.triangleVisible = state.triangleVisible;
		this.pinned = state.pinned;
		this.color = state.color;
		this.lengthScale = state.lengthScale;
		this.widthScale = state.widthScale;
		p0Loc = new Vector3(state.p0);
		p1Loc = new Vector3(state.p1);
		p1Loc.setX(p1Loc.getX() + 0.0001);
		p2Loc = new Vector3(state.p2);
		p2Loc.setY(p2Loc.getY() + 0.0001);
		p3Loc = new Vector3();
		normal = new Vector3();
		centroid = new Vector3();
		lowerBound = new Vector3();
		upperBound = new Vector3();
		strikeDir = new Vector3();
		dipDir = new Vector3();
		tmp0 = new Vector3();
		tmp1 = new Vector3();
		rotMat = new Matrix3();
		workVec = new Vector3();

		// Create points
		point = new BillboardMarker[3];
		point[0] = createMarker(state.name + "_0", p0Loc, state.zOff0, color);
		point[0].addMotionListener(new MotionListener() {
			@Override
			public void move(Movable mo, ReadOnlyVector3 pos) {
				updatePlane();
				updateTriangleLine();
				updateStrikeDipLines();
				updatePolygon();
				updateGeometricState(0);
			}
		});
		attachChild(point[0]);

		point[1] = createMarker(state.name + "_1", p1Loc, state.zOff1, color);
		point[1].addMotionListener(new MotionListener() {
			@Override
			public void move(Movable mo, ReadOnlyVector3 pos) {
				updatePlane();
				updateTriangleLine();
				updateStrikeDipLines();
				updatePolygon();
				updateGeometricState(0);
			}
		});
		attachChild(point[1]);

		point[2] = createMarker(state.name + "_2", p2Loc, state.zOff2, color);
		point[2].addMotionListener(new MotionListener() {
			@Override
			public void move(Movable mo, ReadOnlyVector3 pos) {
				updatePlane();
				updateTriangleLine();
				updateStrikeDipLines();
				updatePolygon();
				updateGeometricState(0);
			}
		});
		attachChild(point[2]);
		updateGeometricState(0);

		updatePlane();
		createTriangleLine();
		setTriangleVisible(triangleVisible);
		attachChild(triangleLine);
		createStrikeDipLines();
		attachChild(strikeLine[0]);
		attachChild(strikeLine[1]);
		attachChild(dipLine[0]);
		attachChild(dipLine[1]);
		createPolygon();
		attachChild(poly);
		setColor(state.color);
		
		setVisible(state.visible);
	}
	
	public void setHiddenDashed(boolean hiddenDashed) {
		triangleLine.enableDash(hiddenDashed);
	}

	/**
	 * Get lower bound of plane
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getLowerBound() {
		return (lowerBound);
	}

	/**
	 * Get the upper bound of the plane
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getUpperBound() {
		return (upperBound);
	}

	private BillboardMarker createMarker(String name, ReadOnlyVector3 point, double zOff, Color color) {
		if (texture == null) {
			texture = ImageUtil.createTexture(Icons.getIconURL("paddle.png"), true);
			highlightTexture = ImageUtil.createTexture(Icons.getIconURL("paddle-highlight.png"), true);
		}
		BillboardMarker bm = new BillboardMarker(name, point, 1, zOff, color, labelVisible, pinned);
		bm.setTexture(texture, highlightTexture);
		return (bm);
	}

	private void createTriangleLine() {
		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(9);
		vertexBuffer.limit(0);
		triangleLine = new HiddenLine("_polyline", IndexMode.LineLoop);
		triangleLine.setVertexBuffer(vertexBuffer);
		triangleLine.getSceneHints().setCastsShadows(false);
		triangleLine.getSceneHints().setTextureCombineMode(TextureCombineMode.Off);
		triangleLine.setModelBound(new BoundingBox());
		triangleLine.updateModelBound();
		triangleLine.getSceneHints().setAllPickingHints(false);
		triangleLine.setLineWidth(2);
		updateTriangleLine();
	}

	private void createPolygon() {
		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(12);
		vertexBuffer.limit(0);
		FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(12);
		normalBuffer.limit(0);
		poly = new Mesh("_polygon");
		poly.getMeshData().setIndexMode(IndexMode.Quads);
		poly.getMeshData().setVertexBuffer(vertexBuffer);
		poly.getMeshData().setNormalBuffer(normalBuffer);
		poly.setModelBound(new BoundingBox());
		poly.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
//		SpatialUtil.setPickHost(poly, this);
		poly.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		poly.getSceneHints().setCastsShadows(false);
		updatePolygon();
	}

	private void createStrikeDipLines() {
		MaterialState lineMS = new MaterialState();
		lineMS.setDiffuse(new ColorRGBA(0, 0, 0, 1));
		lineMS.setAmbient(new ColorRGBA(0, 0, 0, 1));
		lineMS.setEmissive(MaterialState.MaterialFace.FrontAndBack, ColorRGBA.WHITE);
		strikeLine = new Line[2];
		dipLine = new Line[2];
		for (int i = 0; i < 2; ++i) {
			FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(6);
			vertexBuffer.limit(0);
			strikeLine[i] = new Line("_strike" + i);
			strikeLine[i].getMeshData().setIndexMode(IndexMode.Lines);
			strikeLine[i].getMeshData().setVertexBuffer(vertexBuffer);
			strikeLine[i].getSceneHints().setCastsShadows(false);
			strikeLine[i].getSceneHints().setTextureCombineMode(TextureCombineMode.Off);
			strikeLine[i].setModelBound(new BoundingBox());
			strikeLine[i].updateModelBound();
			strikeLine[i].getSceneHints().setAllPickingHints(false);
			strikeLine[i].setLineWidth(i * 4 + 1);

			vertexBuffer = BufferUtils.createFloatBuffer(6);
			vertexBuffer.limit(0);
			dipLine[i] = new Line("_dip" + i);
			dipLine[i].getMeshData().setIndexMode(IndexMode.Lines);
			dipLine[i].getMeshData().setVertexBuffer(vertexBuffer);
			dipLine[i].getSceneHints().setCastsShadows(false);
			dipLine[i].getSceneHints().setTextureCombineMode(TextureCombineMode.Off);
			dipLine[i].setModelBound(new BoundingBox());
			dipLine[i].updateModelBound();
			dipLine[i].getSceneHints().setAllPickingHints(false);
			dipLine[i].setStipplePattern((short) 0xff00);
			dipLine[i].setLineWidth(i * 4 + 1);

			strikeLine[i].setRenderState(lineMS);
			dipLine[i].setRenderState(lineMS);
		}
		updateStrikeDipLines();
	}

	private void updateTriangleLine() {

		FloatBuffer vertexBuffer = triangleLine.getVertexBuffer();
		vertexBuffer.clear();
		for (int i = 0; i < point.length; ++i) {
			ReadOnlyVector3 trans = point[i].getWorldTranslation();
			vertexBuffer.put(trans.getXf()).put(trans.getYf()).put(trans.getZf());
		}
		vertexBuffer.rewind();
		triangleLine.setVertexBuffer(vertexBuffer);
		triangleLine.updateModelBound();
	}

	private void updatePolygon() {
		p0Loc.set(centroid);
		p1Loc.set(centroid);
		p2Loc.set(centroid);
		p3Loc.set(centroid);

		p1Loc.addLocal(strikeDir);
		p2Loc.addLocal(strikeDir);
		p0Loc.subtractLocal(strikeDir);
		p3Loc.subtractLocal(strikeDir);

		p2Loc.addLocal(dipDir);
		p3Loc.addLocal(dipDir);
		p0Loc.subtractLocal(dipDir);
		p1Loc.subtractLocal(dipDir);

		FloatBuffer vertexBuffer = poly.getMeshData().getVertexBuffer();
		vertexBuffer.clear();
		vertexBuffer.put(p0Loc.getXf()).put(p0Loc.getYf()).put(p0Loc.getZf());
		vertexBuffer.put(p1Loc.getXf()).put(p1Loc.getYf()).put(p1Loc.getZf());
		vertexBuffer.put(p2Loc.getXf()).put(p2Loc.getYf()).put(p2Loc.getZf());
		vertexBuffer.put(p3Loc.getXf()).put(p3Loc.getYf()).put(p3Loc.getZf());
		vertexBuffer.rewind();
		FloatBuffer normalBuffer = poly.getMeshData().getNormalBuffer();
		normalBuffer.clear();
		normalBuffer.put(normal.getXf()).put(normal.getYf()).put(normal.getZf());
		normalBuffer.put(normal.getXf()).put(normal.getYf()).put(normal.getZf());
		normalBuffer.put(normal.getXf()).put(normal.getYf()).put(normal.getZf());
		normalBuffer.put(normal.getXf()).put(normal.getYf()).put(normal.getZf());
		normalBuffer.rewind();
		poly.getMeshData().setVertexBuffer(vertexBuffer);
		poly.getMeshData().setNormalBuffer(normalBuffer);
		poly.markDirty(DirtyType.Bounding);
		poly.updateModelBound();
		poly.updateGeometricState(0);
	}

	private void updateStrikeDipLines() {

		tmp0.set(centroid);
		tmp0.subtractLocal(dipDir);

		tmp1.set(centroid);

		FloatBuffer vertexBuffer = dipLine[0].getMeshData().getVertexBuffer();
		vertexBuffer.clear();
		vertexBuffer.put(tmp0.getXf()).put(tmp0.getYf()).put(tmp0.getZf());
		vertexBuffer.put(tmp1.getXf()).put(tmp1.getYf()).put(tmp1.getZf());
		vertexBuffer.rewind();
		dipLine[0].getMeshData().setVertexBuffer(vertexBuffer);
		dipLine[0].updateModelBound();
		dipLine[0].updateGeometricState(0);

		tmp0.set(centroid);

		tmp1.set(centroid);
		tmp1.addLocal(dipDir);

		vertexBuffer = dipLine[1].getMeshData().getVertexBuffer();
		vertexBuffer.clear();
		vertexBuffer.put(tmp0.getXf()).put(tmp0.getYf()).put(tmp0.getZf());
		vertexBuffer.put(tmp1.getXf()).put(tmp1.getYf()).put(tmp1.getZf());
		vertexBuffer.rewind();
		dipLine[1].getMeshData().setVertexBuffer(vertexBuffer);
		dipLine[1].updateModelBound();
		dipLine[1].updateGeometricState(0);

		tmp0.set(centroid);
		tmp0.subtractLocal(strikeDir);
		tmp1.set(centroid);

		vertexBuffer = strikeLine[0].getMeshData().getVertexBuffer();
		vertexBuffer.clear();
		vertexBuffer.put(tmp0.getXf()).put(tmp0.getYf()).put(tmp0.getZf());
		vertexBuffer.put(tmp1.getXf()).put(tmp1.getYf()).put(tmp1.getZf());
		vertexBuffer.rewind();
		strikeLine[0].getMeshData().setVertexBuffer(vertexBuffer);
		strikeLine[0].updateModelBound();
		strikeLine[0].updateGeometricState(0);

		tmp0.set(centroid);
		tmp1.set(centroid);
		tmp1.addLocal(strikeDir);

		vertexBuffer = strikeLine[1].getMeshData().getVertexBuffer();
		vertexBuffer.clear();
		vertexBuffer.put(tmp0.getXf()).put(tmp0.getYf()).put(tmp0.getZf());
		vertexBuffer.put(tmp1.getXf()).put(tmp1.getYf()).put(tmp1.getZf());
		vertexBuffer.rewind();
		strikeLine[1].getMeshData().setVertexBuffer(vertexBuffer);
		strikeLine[1].updateModelBound();
		strikeLine[1].updateGeometricState(0);
	}

	/**
	 * Get the map element state for this Path.
	 */
	@Override
	public MapElementState getState() {
		return (state);
	}
	
	public Marker getMarker(int index) {
		return(point[index]);
	}

	/**
	 * Set the Path name.
	 */
	@Override
	public void setName(String name) {
		super.setName(name);
		for (int i = 0; i < point.length; ++i) {
			point[i].setName(name + "_" + i);
		}
	}

	/**
	 * Get the location of the point at the given index
	 * 
	 * @param i
	 * @return
	 */
	public ReadOnlyVector3 getPoint(int i) {
		return (point[i].getLocation());
	}

	/**
	 * Set the location of the point at the given index
	 * 
	 * @param i
	 * @param p
	 */
	public void setPoint(int i, double x, double y, double z) {
		point[i].setLocation(x, y, z, true);
	}

	/**
	 * Set the scale along the length of the plane
	 * 
	 * @param lengthScale
	 */
	public void setLengthScale(double lengthScale) {
		this.lengthScale = lengthScale;
		updatePlane();
		updateStrikeDipLines();
		updatePolygon();
	}

	/**
	 * Get the scale along the length of the plane
	 * 
	 * @return
	 */
	public double getLengthScale() {
		return (lengthScale);
	}

	/**
	 * Set the scale along the width of the plane
	 * 
	 * @param widthScale
	 */
	public void setWidthScale(double widthScale) {
		this.widthScale = widthScale;
		updatePlane();
		updateStrikeDipLines();
		updatePolygon();
	}

	/**
	 * Get the scale along the width of the plane
	 * 
	 * @return
	 */
	public double getWidthScale() {
		return (widthScale);
	}

	/**
	 * Make the way points visible or not.
	 * 
	 * @param show
	 *            true = visible
	 */
	public void setTriangleVisible(boolean show) {
		triangleVisible = show;
		CullHint hint = show ? CullHint.Inherit : CullHint.Always;
		for (int i = 0; i < point.length; ++i) {
			point[i].getSceneHints().setCullHint(hint);
		}
		triangleLine.getSceneHints().setCullHint(hint);
		markDirty(DirtyType.RenderState);
	}

	/**
	 * Check if way points are visible.
	 * 
	 * @return
	 */
	public boolean isTriangleVisible() {
		return (triangleVisible);
	}

	/**
	 * Set the color.
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;

		for (int i = 0; i < point.length; ++i) {
			point[i].setColor(color);
		}

		// set the color of the body
		colorRGBA = UIUtil.colorToColorRGBA(color);

		MaterialState lineMS = new MaterialState();
		lineMS.setDiffuse(ColorRGBA.BLACK);
		lineMS.setAmbient(ColorRGBA.BLACK);
		lineMS.setEmissive(MaterialState.MaterialFace.FrontAndBack, colorRGBA);
		triangleLine.setRenderState(lineMS);

		colorRGBA.setAlpha(colorRGBA.getAlpha()*0.4f);
		MaterialState polyMS = new MaterialState();
		polyMS.setDiffuse(MaterialState.MaterialFace.FrontAndBack, colorRGBA);
		polyMS.setAmbient(MaterialState.MaterialFace.FrontAndBack, ColorRGBA.BLACK);
		polyMS.setEmissive(MaterialState.MaterialFace.FrontAndBack, colorRGBA);
		polyMS.setEnabled(true);
		poly.setRenderState(polyMS);
	}

	/**
	 * Get the color.
	 */
	@Override
	public Color getColor() {
		return (color);
	}

	/**
	 * Is the map element visible.
	 */
	@Override
	public boolean isVisible() {
		return (SpatialUtil.isDisplayed(this));
	}

	/**
	 * Set the map element visibility.
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			getSceneHints().setCullHint(CullHint.Dynamic);
		} else {
			getSceneHints().setCullHint(CullHint.Always);
		}
		markDirty(DirtyType.RenderState);
	}

	/**
	 * Get the point icon size.
	 */
	@Override
	public double getSize() {
		return (size);
	}

	/**
	 * A quad tree has merged or split. Update the elevation of each way point
	 * if it is inside the quad tree.
	 * 
	 * @param quadTree
	 *            the Quad Tree.
	 */
	@Override
	public boolean updateElevation(QuadTree quadTree) {
		for (int i = 0; i < point.length; ++i) {
			point[i].updateElevation(quadTree);
		}
		return (false);
	}

	/**
	 * Get the map element type.
	 */
	@Override
	public Type getType() {
		return (Type.Plane);
	}

	/**
	 * Get the point to seek to.
	 */
	@Override
	public double getSeekPointAndDistance(Vector3 p) {
		p.set(centroid);
		return (point[0].getWorldBound().getRadius() * 1.5);
	}

	/**
	 * Get the icon that represents this map element.
	 */
	@Override
	public Icon getIcon() {
		return (icon);
	}

	/**
	 * Is this map element pinned?
	 */
	@Override
	public boolean isPinned() {
		return (pinned);
	}

	/**
	 * Pin down this map element so it cannot be moved.
	 */
	@Override
	public void setPinned(boolean pinned) {
		this.pinned = pinned;
		for (int i = 0; i < point.length; ++i) {
			point[i].setPinned(pinned);
		}
	}

	/**
	 * Is this map element's label visible?
	 */
	@Override
	public boolean isLabelVisible() {
		return (labelVisible);
	}

	/**
	 * Set the label on this map element visible.
	 */
	@Override
	public void setLabelVisible(boolean labelVisible) {
		this.labelVisible = labelVisible;
		for (int i = 0; i < point.length; ++i) {
			point[i].setLabelVisible(labelVisible);
		}
	}

	/**
	 * Change the vertical exaggeration for this map element.
	 */
	@Override
	public void setVerticalExaggeration(double vertExag, double oldVertExag, double minZ) {
		for (int i = 0; i < point.length; ++i) {
			point[i].setVerticalExaggeration(vertExag, oldVertExag, minZ);
		}
		updateTriangleLine();
		updateStrikeDipLines();
		updatePolygon();
	}

	/**
	 * Update the map element icons according to the camera position.
	 */
	@Override
	public void update(BasicCamera camera) {
		for (int i = 0; i < point.length; ++i) {
			point[i].update(camera);
		}
	}

	/**
	 * Dispose of this map element's resources.
	 */
	public void dispose() {
		// nothing here
	}

	@Override
	public String toString() {
		return (getName());
	}

	/**
	 * Get the difference between the plane and the terrain elevation.
	 * 
	 * @param size
	 * @param diff
	 * @param minMaxElev
	 * @return
	 */
	public int[] getElevationDifference(int size, float[][] diff, float[] minMaxElev) {
		updatePlane();
		updatePolygon();

		// get plane vertices
		Vector3[] vertex = new Vector3[5];
		vertex[0] = new Vector3(p0Loc);
		vertex[1] = new Vector3(p1Loc);
		vertex[2] = new Vector3(p2Loc);
		vertex[3] = new Vector3(p3Loc);
		vertex[4] = new Vector3(p0Loc);

		// compute bounds
		lowerBound.set(vertex[0]);
		upperBound.set(vertex[0]);
		for (int i = 1; i < vertex.length; ++i) {
			lowerBound.setX(Math.min(lowerBound.getX(), vertex[i].getX()));
			upperBound.setX(Math.max(upperBound.getX(), vertex[i].getX()));
			lowerBound.setY(Math.min(lowerBound.getY(), vertex[i].getY()));
			upperBound.setY(Math.max(upperBound.getY(), vertex[i].getY()));
			lowerBound.setZ(Math.min(lowerBound.getZ(), vertex[i].getZ()));
			upperBound.setZ(Math.max(upperBound.getZ(), vertex[i].getZ()));
		}

		// determine sample size
		double sampleWidth = (upperBound.getX() - lowerBound.getX()) / size;
		double sampleLength = (upperBound.getY() - lowerBound.getY()) / size;
		sampleWidth = Math.max(sampleWidth, Landscape.getInstance().getPixelWidth());
		sampleLength = Math.max(sampleLength, Landscape.getInstance().getPixelLength());
		double sampleSize = Math.max(sampleWidth, sampleLength);

		// get elevation differences
		double[] planeEqCopy = new double[planeEq.length];
		System.arraycopy(planeEq, 0, planeEqCopy, 0, planeEq.length);
		int[] result = Landscape.getInstance().getSampledDifferenceOfRegion(vertex, lowerBound, upperBound, planeEqCopy, sampleSize, diff, minMaxElev);
		return (result);
	}

	/**
	 * Get the plane strike
	 * 
	 * @return
	 */
	public double getStrike() {
		return (strike);
	}

	/**
	 * Get the plane dip
	 * 
	 * @return
	 */
	public double getDip() {
		return (dip);
	}
	
	/**
	 * Set the PlanePanel for strike and dip updates.
	 * @param planePanel
	 */
	public void setPlanePanel(PlanePanel planePanel) {
		this.planePanel = planePanel;
	}

	private void updatePlane() {

		// find triangle centroid
		centroid.set(point[0].getWorldTranslation());
		centroid.addLocal(point[1].getWorldTranslation());
		centroid.addLocal(point[2].getWorldTranslation());
		centroid.multiplyLocal(0.33333333333);

		// find the surface normal of the triangle
		MathUtil.createNormal(normal, point[0].getWorldTranslation(), point[1].getWorldTranslation(),
			point[2].getWorldTranslation());
		// be sure the normal is pointing up
		if (normal.getZ() < 0) {
			normal.negateLocal();
		}

		// get the plane equation from a point and the normal
		MathUtil.getPlaneFromPointAndNormal(point[0].getWorldTranslation(), normal, planeEq);

		// get the dip azimuth from the normal
		double aspect = MathUtil.getAspectFromNormal(normal);
		// strike azimuth is 90 degrees off of dip azimuth
		strike = (aspect + 90) % 360;
		// get the dip in degrees from a horizontal plane
		dip = MathUtil.getSlopeFromNormal(normal);

		// get the dip direction vector from the normal
		dipDir.set(normal.getX(), normal.getY(), 0);
		dipDir.setZ(MathUtil.getPlaneZ(centroid.getX() + normal.getX(), centroid.getY() + normal.getY(), planeEq)
			- centroid.getZ());
		dipDir.normalizeLocal();
		rotMat.fromAngleNormalAxis(-Math.PI / 2, normal);
		rotMat.applyPost(dipDir, strikeDir);
		strikeDir.normalizeLocal();
		strikeDir.multiplyLocal(widthScale);
		dipDir.multiplyLocal(lengthScale);
		
		if (planePanel != null)
			planePanel.updateStrikeAndDip(strike, dip);
	}

	/**
	 * Get the location in world coordinates
	 */
	public ReadOnlyVector3 getLocationInWorld() {
		workVec.set(centroid);
		Landscape.getInstance().localToWorldCoordinate(workVec);
		return (workVec);
	}
	
	public void ground() {
		for (int i=0; i<point.length; ++i)
			point[i].ground();
	}
	
	public void setZOffset(double zOff, boolean doTrans) {
		// do nothing
	}
	
	public double getZOffset() {
		return(0);
	}

	/**
	 * Set the defaults
	 * 
	 * @param properties
	 */
	public static void setDefaultsFromProperties(Properties properties) {
		defaultColor = StringUtil.getColorValue(properties, "MapElement.Plane.defaultColor", defaultColor, false);
		defaultSize = (float) StringUtil.getDoubleValue(properties, "MapElement.Plane.defaultSize", true, defaultSize,
			false);
		defaultLabelVisible = StringUtil.getBooleanValue(properties, "MapElement.Plane.defaultLabelVisible",
			defaultLabelVisible, false);
		defaultTriangleVisible = StringUtil.getBooleanValue(properties, "MapElement.Plane.defaultTriangleVisible",
			defaultTriangleVisible, false);
		strikeAsCompassBearing = StringUtil.getBooleanValue(properties, "MapElement.Plane.strikeAsCompassBearing",
			strikeAsCompassBearing, false);
		defaultColorMap = StringUtil.getStringValue(properties, "MapElement.Plane.defaultColorMap", defaultColorMap,
			false);
	}

	/**
	 * Save the defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		properties.setProperty("MapElement.Plane.defaultColor", StringUtil.colorToString(defaultColor));
		properties.setProperty("MapElement.Plane.defaultSize", Float.toString(defaultSize));
		properties.setProperty("MapElement.Plane.defaultTriangleVisible", Boolean.toString(defaultTriangleVisible));
		properties.setProperty("MapElement.Plane.strikeAsCompassBearing", Boolean.toString(strikeAsCompassBearing));
		properties.setProperty("MapElement.Plane.defaultLabelVisible", Boolean.toString(defaultLabelVisible));
		properties.setProperty("MapElement.Plane.defaultColorMap", defaultColorMap);
	}
}
