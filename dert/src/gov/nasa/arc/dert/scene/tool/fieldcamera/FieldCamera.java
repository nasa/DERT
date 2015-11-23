package gov.nasa.arc.dert.scene.tool.fieldcamera;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.QuadTree;
import gov.nasa.arc.dert.render.Viewshed;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.Tool;
import gov.nasa.arc.dert.scenegraph.Billboard;
import gov.nasa.arc.dert.scenegraph.Text.AlignType;
import gov.nasa.arc.dert.scenegraph.LineSegment;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.scenegraph.RasterText;
import gov.nasa.arc.dert.state.FieldCameraState;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.util.ImageUtil;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.SpatialUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.viewpoint.BasicCamera;
import gov.nasa.arc.dert.viewpoint.ViewDependent;

import java.awt.Color;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Icon;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.image.Texture;
import com.ardor3d.intersection.PickData;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.MaterialState.MaterialFace;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.extension.CameraNode;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.scenegraph.shape.Sphere;

/**
 * A camera map element that provides a view from the terrain level. It can be
 * placed anywhere in the scene and has adjustable height, pan, and tilt.
 *
 */
public class FieldCamera extends Movable implements Tool, ViewDependent {

	public static final Icon icon = Icons.getImageIcon("fieldcamera.png");
	protected static float AMBIENT_FACTOR = 0.75f;

	// Defaults
	public static Color defaultColor = Color.orange;
	public static boolean defaultLabelVisible = true;
	public static boolean defaultFovVisible = false;
	public static boolean defaultLineVisible = false;
	public static boolean defaultPinned = false;
	public static double defaultAzimuth = 0;
	public static double defaultTilt = 0;
	public static double defaultHeight = 1;
	public static String defaultDefinition = "CameraDef1";

	// Icon textures
	protected static volatile Texture nominalTexture;
	protected static volatile Texture highlightTexture;

	public AtomicBoolean changed = new AtomicBoolean();

	// Camera Definition
	private FieldCameraInfo fieldCameraInfo;
	private String fieldCameraDef;

	// Field Camera Box
	private Mesh box;
	private CameraNode cameraNode;
	private Node mountingNode, geomNode, viewDependentNode;
	private Billboard billboard;

	// Camera
	private BasicCamera basicCamera;
	private BoundingSphere sceneBounds = new BoundingSphere(10, new Vector3());
	private Vector3 oldTranslation = new Vector3();
	private Matrix3 oldRotation = new Matrix3();
	private double fovX;
	private double aspect;

	// Pan and tilt
	private double height, panValue, tiltValue;
	private Node pan, tilt;
	private Matrix3 rotMat = new Matrix3();

	// Camera stand
	private Vector3 p0 = new Vector3(), p1 = new Vector3();
	private LineSegment lineSegment;

	// Frustum and LookAtLine
	private FrustumPyramid frustum;
	private LineSegment lookAtLine;
	private double fovLength, lineLength;
	private boolean fovVisible;

	// Attributes
	private ColorRGBA colorRGBA;
	private ColorRGBA highlightColorRGBA;
	private MaterialState materialState;
	private Color color;

	// scale factor for viewpoint resizing
	private double scale = 1, oldScale = 1;
	private RasterText label;

	// Center of scene
	private int centerX, centerY;

	// Math helpers
	private Vector3 workVec = new Vector3();
	private Matrix3 workMat = new Matrix3();

	// Map element state
	private FieldCameraState state;

	public FieldCamera(FieldCameraState state) {
		super(state.name);
		this.state = state;
		if (state.position != null) {
			super.setTranslation(state.position);
		}

		// camera stand
		Sphere base = new Sphere("_base", 50, 50, 0.25f);
		base.setModelBound(new BoundingBox());
		attachChild(base);
		lineSegment = new LineSegment("_post", p0, p1);
		lineSegment.setModelBound(new BoundingBox());
		attachChild(lineSegment);

		// mounting node to set orientation relative to OpenGL default axes
		mountingNode = new Node("_mounting");
		Matrix3 rotX = new Matrix3().fromAngleNormalAxis(Math.PI / 2, Vector3.UNIT_X);
		mountingNode.setRotation(rotX);

		// camera box
		geomNode = new Node("_geometry");
		box = new Box("_box", new Vector3(), 0.25, 0.25, 0.25);
		box.setModelBound(new BoundingBox());
		geomNode.attachChild(box);

		fovLength = World.getInstance().getLandscape().getWorldBound().getRadius();
		fovVisible = state.fovVisible;

		setFieldCameraDefinition(state.fieldCameraDef);

		// create lookat line
		lookAtLine = new LineSegment("_line", Vector3.ZERO, new Vector3(0, 0, -1));
		lookAtLine.setModelBound(new BoundingBox());
		setLookAtLineLength(2 * fovLength);
		setLookAtLineVisible(state.lineVisible);
		geomNode.attachChild(lookAtLine);

		// special billboard for spotting fieldCamera from far away
		viewDependentNode = new Node("_viewDependent");
		billboard = new Billboard("_billboard", getIconTexture());
		SpatialUtil.setPickHost(billboard, this);
		viewDependentNode.attachChild(billboard);
		label = new RasterText("_label", state.name, AlignType.Center);
		label.setScaleFactor(0.75f);
		label.setColor(ColorRGBA.WHITE);
		label.setTranslation(0, 1.5, 0);
		label.setVisible(state.labelVisible);
		viewDependentNode.attachChild(label);
		viewDependentNode.setTranslation(0.0, 0.25, 0.0);
		geomNode.attachChild(viewDependentNode);

		// the camera for this fieldCamera
		cameraNode = new CameraNode() {
			@Override
			public void updateWorldTransform(boolean recurse) {
				super.updateWorldTransform(recurse);
				double farPlane = MathUtil.distanceToSphere(sceneBounds, basicCamera.getLocation(),
					basicCamera.getDirection());
				basicCamera.setFrustumFar(farPlane);
				changed.set(!(oldTranslation.equals(getWorldTranslation()) && oldRotation.equals(getWorldRotation())));
				oldTranslation.set(getWorldTranslation());
				oldRotation.set(getWorldRotation());
				setLookAtLineLength(0.8 * farPlane);
				setFovLength(0.8 * farPlane);
			}
		};
		basicCamera = new BasicCamera(1, 1, fovX, aspect, height);
		cameraNode.setCamera(basicCamera);

		mountingNode.attachChild(cameraNode);
		mountingNode.attachChild(geomNode);

		// tilt and pan
		tilt = new Node("_tilt");
		tilt.attachChild(mountingNode);
		pan = new Node("_pan");
		pan.setTranslation(0, 0, height);
		pan.attachChild(tilt);
		attachChild(pan);

		// initialize other fields
		setColor(state.color);
		changed.set(true);
		setAzimuth(state.azimuth);
		setElevation(state.tilt);
		setHeight(state.height);

		state.setMapElement(this);

		getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
	}

	/**
	 * Get the map element state
	 */
	@Override
	public MapElementState getState() {
		return (state);
	}

	/**
	 * Get the field camera info object
	 * 
	 * @return
	 */
	public FieldCameraInfo getFieldCameraInfo() {
		return (fieldCameraInfo);
	}

	/**
	 * Set the field camera definition
	 * 
	 * @param fieldCameraDef
	 * @return
	 */
	public boolean setFieldCameraDefinition(String fieldCameraDef) {
		if ((this.fieldCameraDef != null) && this.fieldCameraDef.equals(fieldCameraDef)) {
			return (false);
		}
		this.fieldCameraDef = fieldCameraDef;
		fieldCameraInfo = FieldCameraInfoManager.getInstance().getFieldCameraInfo(fieldCameraDef);
		fovX = fieldCameraInfo.fovX;
		aspect = fieldCameraInfo.fovX / fieldCameraInfo.fovY;
		height = fieldCameraInfo.tripodHeight;
		if (height == 0) {
			height = 0.0001;
		}
		panValue = fieldCameraInfo.tripodPan;
		tiltValue = fieldCameraInfo.tripodTilt;
		p1.set(0, 0, height);

		if (frustum != null) {
			geomNode.detachChild(frustum);
		}
		float fovHgt = (float) Math.tan(Math.toRadians(fieldCameraInfo.fovY));
		frustum = new FrustumPyramid("_frustum", fovHgt * aspect, fovHgt, 1f, fieldCameraInfo.color);
		frustum.getSceneHints().setAllPickingHints(false);
		setFovLength(fovLength);
		setFovVisible(fovVisible);
		geomNode.attachChild(frustum);

		mountingNode.setTranslation(fieldCameraInfo.mountingOffset);
		return (true);
	}

	public void resetCamera() {
		basicCamera = new BasicCamera(1, 1, fovX, aspect, height);
		cameraNode.setCamera(basicCamera);
		// changed.set(true);
	}

	/**
	 * Get the field camera definition name
	 * 
	 * @return
	 */
	public String getFieldCameraDefinition() {
		return (fieldCameraDef);
	}

	public ReadOnlyColorRGBA getColorRGBA() {
		return (colorRGBA);
	}

	public BasicCamera getCamera() {
		return (basicCamera);
	}

	private void setFovLength(double length) {
		if (fovLength == length) {
			return;
		}
		fovLength = length;
		frustum.setScale(fovLength, fovLength, fovLength);
	}

	private void setLookAtLineLength(double length) {
		if (lineLength == length) {
			return;
		}
		lineLength = length;
		lookAtLine.setScale(1, 1, length);
	}

	@Override
	public ReadOnlyVector3 getLocation() {
		return (getWorldTranslation());
	}

	/**
	 * Get FOV visibility
	 * 
	 * @return
	 */
	public boolean isFovVisible() {
		return (frustum.getSceneHints().getCullHint() != CullHint.Always);
	}

	/**
	 * Set FOV visibility
	 * 
	 * @param enable
	 */
	public void setFovVisible(boolean enable) {
		frustum.getSceneHints().setCullHint(enable ? CullHint.Inherit : CullHint.Always);
		frustum.markDirty(DirtyType.RenderState);
		fovVisible = enable;
	}

	/**
	 * Set lookAt line visibility
	 * 
	 * @param enable
	 */
	public void setLookAtLineVisible(boolean enable) {
		lookAtLine.getSceneHints().setCullHint(enable ? CullHint.Inherit : CullHint.Always);
		lookAtLine.markDirty(DirtyType.RenderState);
	}

	/**
	 * Get lookAt line visibility
	 * 
	 * @return
	 */
	public boolean isLookAtLineVisible() {
		return (lookAtLine.getSceneHints().getCullHint() != CullHint.Always);
	}

	/**
	 * Set the bounds of the scene for computing clipping planes
	 */
	public void setSceneBounds() {
		BoundingVolume bounds = World.getInstance().getRoot().getWorldBound();
		sceneBounds.setRadius(bounds.getRadius());
		sceneBounds.setCenter(bounds.getCenter());
		basicCamera.setFrustum(sceneBounds);
		basicCamera.setFrustumNear(0.1);
		double farPlane = MathUtil.distanceToSphere(World.getInstance().getRoot().getWorldBound(),
			basicCamera.getLocation(), basicCamera.getDirection());
		basicCamera.setFrustumFar(farPlane);
	}

	/**
	 * Resize the camera
	 * 
	 * @param width
	 * @param height
	 */
	public void resize(int width, int height) {
		if ((width == 0) || (height == 0)) {
			return;
		}
		if (basicCamera == null) {
			return;
		}
		setViewport(width, width, height, 0);
		basicCamera.resize(width, height);
		centerX = width / 2;
		centerY = height / 2;
		// changed.set(true);
	}

	/**
	 * Prerender for viewshed or footprint
	 * 
	 * @param renderer
	 * @param viewshed
	 */
	public void prerender(Renderer renderer, Viewshed viewshed) {
		cull();
		viewshed.doPrerender(renderer);
		uncull();
	}

	/**
	 * Cull camera parts
	 */
	public void cull() {
		geomNode.getSceneHints().setCullHint(CullHint.Always);
		geomNode.updateGeometricState(0);
		lineSegment.getSceneHints().setCullHint(CullHint.Always);
		lineSegment.updateGeometricState(0);
	}

	/**
	 * Show camera parts
	 */
	public void uncull() {
		geomNode.getSceneHints().setCullHint(CullHint.Inherit);
		geomNode.updateGeometricState(0);
		lineSegment.getSceneHints().setCullHint(CullHint.Inherit);
		lineSegment.updateGeometricState(0);
	}

	public Node getGeometryNode() {
		return (geomNode);
	}

	/**
	 * Get distance from focal point to surface
	 * 
	 * @return
	 */
	public double getDistanceToSurface() {
		getSceneHints().setPickingHint(PickingHint.Pickable, false);
		Vector3 position = new Vector3();
		Vector3 normal = new Vector3();
		Vector2 mousePos = new Vector2(centerX, centerY);
		Ray3 pickRay = new Ray3();
		basicCamera.getPickRay(mousePos, false, pickRay);
		Spatial spat = World.getInstance().select(pickRay, position, normal, null, false);
		getSceneHints().setPickingHint(PickingHint.Pickable, true);
		if (spat == null) {
			return (Double.NaN);
		} else {
			return (basicCamera.getLocation().distance(position));
		}
	}

	/**
	 * Get label visibility
	 */
	@Override
	public boolean isLabelVisible() {
		return (label.isVisible());
	}

	/**
	 * Set label visibility
	 */
	@Override
	public void setLabelVisible(boolean visible) {
		label.setVisible(visible);
	}

	/**
	 * Point camera at given coordinate
	 * 
	 * @param point
	 * @return
	 */
	public double[] seek(ReadOnlyVector3 point) {
		Vector3 dir = new Vector3(point);
		dir.subtractLocal(basicCamera.getLocation());
		dir.normalizeLocal();
		double[] angle = MathUtil.directionToAzEl(dir, null, workVec, workMat);
		return (angle);
	}

	private double convertLon(double value) {
		if (value > 2 * Math.PI) {
			value -= 2 * Math.PI;
		} else if (value < 0) {
			value += 2 * Math.PI;
		}
		return (value);
	}

	/**
	 * Set the camera pan
	 * 
	 * @param value
	 */
	public void setAzimuth(double value) {
		value = Math.toRadians(value);
		if (panValue == value) {
			return;
		}
		panValue = value;
		// change sign to make negative pan go left and convert from -180/180 to
		// 0/360
		rotMat.fromAngleNormalAxis(convertLon(panValue), Vector3.UNIT_Z);
		pan.setRotation(rotMat);
	}

	/**
	 * Set the camera tilt
	 * 
	 * @param value
	 */
	public void setElevation(double value) {
		value = Math.toRadians(value);
		if (value > Math.PI) {
			value = Math.PI;
		} else if (value < -Math.PI) {
			value = -Math.PI;
		}
		if (tiltValue == value) {
			return;
		}
		tiltValue = value;
		rotMat.fromAngleNormalAxis(tiltValue, Vector3.UNIT_X);
		tilt.setRotation(rotMat);
	}

	/**
	 * Get the current azimuth (pan)
	 * 
	 * @return
	 */
	public double getAzimuth() {
		return (Math.toDegrees(panValue));
	}

	/**
	 * Get the current elevation (tilt)
	 * 
	 * @return
	 */
	public double getElevation() {
		return (Math.toDegrees(tiltValue));
	}

	/**
	 * Set tripod height
	 * 
	 * @param height
	 */
	public void setHeight(double height) {
		this.height = height;
		pan.setTranslation(0, 0, height);
		p1.set(0, 0, height);
		lineSegment.setPoints(p0, p1);
	}

	/**
	 * Get tripod height
	 * 
	 * @return
	 */
	public double getHeight() {
		return (height);
	}

	/**
	 * Update view dependent parts
	 */
	@Override
	public void update(BasicCamera camera) {
		scale = camera.getPixelSizeAt(getWorldTranslation(), true) * PIXEL_SIZE;
		if (Math.abs(scale - oldScale) > 0.0000001) {
			oldScale = scale;
			scaleShape(scale);
		}
	}

	/**
	 * Get point and distance to seek to
	 */
	@Override
	public double getSeekPointAndDistance(Vector3 point) {
		point.set(getTranslation());
		return (getRadius() * 1.5);
	}

	/**
	 * Get visibility
	 */
	@Override
	public boolean isVisible() {
		return (getSceneHints().getCullHint() != CullHint.Always);
	}

	/**
	 * Set visibility
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			getSceneHints().setCullHint(CullHint.Dynamic);
		} else {
			getSceneHints().setCullHint(CullHint.Always);
		}
	}

	private void setViewport(int width, int fullWidth, int fullHeight, int xOrigin) {
		if (basicCamera == null) {
			return;
		}
		int height = (int) (width / basicCamera.getAspect());
		double x = xOrigin;
		double y = 0;
		if (height > fullHeight) {
			height = fullHeight;
			int oldWidth = width;
			width = (int) (fullHeight * basicCamera.getAspect());
			x = xOrigin + (oldWidth - width) / 2;
		} else {
			y = (fullHeight - height) / 2;
		}
		basicCamera.setViewPort(x / fullWidth, (x + width) / fullWidth, y / fullHeight, (y + height) / fullHeight);
	}

	/**
	 * Get the color
	 */
	@Override
	public Color getColor() {
		return (color);
	}

	/**
	 * Get the size of this object
	 */
	@Override
	public double getSize() {
		return (1);
	}

	/**
	 * The landscape changed, update Z coordinates
	 */
	@Override
	public boolean updateElevation(QuadTree quadTree) {
		if (isPinned()) {
			return (false);
		}
		ReadOnlyVector3 t = getWorldTranslation();
		if (quadTree.contains(t.getX(), t.getY())) {
			double z = World.getInstance().getLandscape().getZ(t.getX(), t.getY(), quadTree);
			if (!Double.isNaN(z)) {
				setTranslation(t.getX(), t.getY(), z);
				return (true);
			}
		}
		return (false);
	}

	/**
	 * Get the map element type
	 */
	@Override
	public Type getType() {
		return (Type.FieldCamera);
	}

	@Override
	public Icon getIcon() {
		return (icon);
	}

	protected void scaleShape(double scale) {
		viewDependentNode.setScale(scale);
	}

	/**
	 * Set the color
	 * 
	 * @param newColor
	 */
	public void setColor(Color newColor) {
		color = newColor;
		colorRGBA = new ColorRGBA(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f,
			color.getAlpha() / 255f);
		if (materialState == null) {
			// add a material state
			materialState = new MaterialState();
			materialState.setColorMaterial(ColorMaterial.None);
			materialState.setEnabled(true);
			setRenderState(materialState);
		}
		setMaterialState();
		markDirty(DirtyType.RenderState);
	}

	protected void setMaterialState() {
		materialState.setAmbient(MaterialFace.FrontAndBack, colorRGBA);
		materialState.setEmissive(MaterialFace.FrontAndBack, colorRGBA);
		materialState.setDiffuse(MaterialFace.FrontAndBack, ColorRGBA.BLACK);
		highlightColorRGBA = new ColorRGBA(colorRGBA.getRed() + 0.5f, colorRGBA.getGreen() + 0.5f,
			colorRGBA.getBlue() + 0.5f, colorRGBA.getAlpha());
	}

	@Override
	protected void enableHighlight(boolean enable) {
		if (enable) {
			billboard.setTexture(highlightTexture);
			materialState.setEmissive(MaterialFace.FrontAndBack, highlightColorRGBA);
		} else {
			billboard.setTexture(nominalTexture);
			materialState.setEmissive(MaterialFace.FrontAndBack, colorRGBA);
		}
	}

	/**
	 * Get distance from camera focal point to surface
	 * 
	 * @param locale
	 * @return
	 */
	public float getDistanceToSurface(World locale) {
		getSceneHints().setPickingHint(PickingHint.Pickable, false);
		PrimitivePickResults pr = new PrimitivePickResults();
		Vector3 racPos = new Vector3(getWorldTranslation());
		ReadOnlyVector3 vpDir = cameraNode.getCamera().getDirection();
		Vector3 racDir = new Vector3((float) vpDir.getX(), (float) vpDir.getY(), (float) vpDir.getZ());
		final Ray3 mouseRay = new Ray3(racPos, racDir);
		pr.clear();
		pr.setCheckDistance(true);
		PickingUtil.findPick(locale, mouseRay, pr);
		if (pr.getNumber() == 0) {
			return (Float.NaN);
		}
		PickData pd = pr.getPickData(0);
		if (pd != null) {
			double dist = pd.getIntersectionRecord().getClosestDistance();
			return ((float) dist);
		}

		return (Float.NaN);
	}

	/**
	 * Landscape vertical exaggeration changed
	 */
	@Override
	public void setVerticalExaggeration(double vertExag, double oldVertExag, double minZ) {
		ReadOnlyVector3 wTrans = getWorldTranslation();
		Vector3 tmp = new Vector3(wTrans.getX(), wTrans.getY(), (wTrans.getZ() - minZ) * vertExag / oldVertExag + minZ);
		getParent().worldToLocal(tmp, tmp);
		setTranslation(tmp);
	}

	@Override
	public void setName(String name) {
		super.setName(name);
		if (label != null) {
			label.setText(name);
			label.markDirty(DirtyType.RenderState);
		}
		markDirty(DirtyType.RenderState);
	}

	protected static Texture getIconTexture() {
		if (nominalTexture == null) {
			nominalTexture = ImageUtil.createTexture(Icons.getIconURL("camera.png"), true);
			highlightTexture = ImageUtil.createTexture(Icons.getIconURL("camera-highlight.png"), true);
		}
		return (nominalTexture);
	}

	/**
	 * Set defaults
	 * 
	 * @param properties
	 */
	public static void setDefaultsFromProperties(Properties properties) {
		defaultColor = StringUtil.getColorValue(properties, "MapElement.FieldCamera.defaultColor", defaultColor, false);
		defaultHeight = StringUtil.getDoubleValue(properties, "MapElement.FieldCamera.defaultHeight", true,
			defaultHeight, false);
		defaultDefinition = properties.getProperty("MapElement.FieldCamera.defaultDefinition", defaultDefinition);
		defaultLabelVisible = StringUtil.getBooleanValue(properties, "MapElement.FieldCamera.defaultLabelVisible",
			defaultLabelVisible, false);
		defaultPinned = StringUtil.getBooleanValue(properties, "MapElement.FieldCamera.defaultPinned", defaultPinned,
			false);
		defaultFovVisible = StringUtil.getBooleanValue(properties, "MapElement.FieldCamera.defaultFovVisible",
			defaultFovVisible, false);
		defaultLineVisible = StringUtil.getBooleanValue(properties, "MapElement.FieldCamera.defaultLineVisible",
			defaultLineVisible, false);
		defaultAzimuth = StringUtil.getDoubleValue(properties, "MapElement.FieldCamera.defaultAzimuth", false,
			defaultAzimuth, false);
		defaultTilt = StringUtil.getDoubleValue(properties, "MapElement.FieldCamera.defaultTilt", false, defaultTilt,
			false);
	}

	/**
	 * Save defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		properties.setProperty("MapElement.FieldCamera.defaultColor", StringUtil.colorToString(defaultColor));
		properties.setProperty("MapElement.FieldCamera.defaultHeight", Double.toString(defaultHeight));
		properties.setProperty("MapElement.FieldCamera.defaultDefinition", defaultDefinition);
		properties.setProperty("MapElement.FieldCamera.defaultLabelVisible", Boolean.toString(defaultLabelVisible));
		properties.setProperty("MapElement.FieldCamera.defaultPinned", Boolean.toString(defaultPinned));
		properties.setProperty("MapElement.FieldCamera.defaultFovVisible", Boolean.toString(defaultFovVisible));
		properties.setProperty("MapElement.FieldCamera.defaultLineVisible", Boolean.toString(defaultLineVisible));
		properties.setProperty("MapElement.FieldCamera.defaultAzimuth", Double.toString(defaultAzimuth));
		properties.setProperty("MapElement.FieldCamera.defaultTilt", Double.toString(defaultTilt));
	}
}
