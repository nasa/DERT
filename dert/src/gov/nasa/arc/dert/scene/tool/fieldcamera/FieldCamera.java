package gov.nasa.arc.dert.scene.tool.fieldcamera;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.render.Viewshed;
import gov.nasa.arc.dert.scene.tool.Tool;
import gov.nasa.arc.dert.scenegraph.ImageQuad;
import gov.nasa.arc.dert.scenegraph.LineSegment;
import gov.nasa.arc.dert.scenegraph.Marker;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.scenegraph.text.RasterText;
import gov.nasa.arc.dert.scenegraph.text.Text.AlignType;
import gov.nasa.arc.dert.state.FieldCameraState;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.terrain.QuadTree;
import gov.nasa.arc.dert.util.ImageUtil;
import gov.nasa.arc.dert.util.SpatialUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.util.UIUtil;
import gov.nasa.arc.dert.viewpoint.BasicCamera;
import gov.nasa.arc.dert.viewpoint.ViewDependent;

import java.awt.Color;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Icon;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.image.Texture;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
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
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.extension.BillboardNode;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.scenegraph.shape.Sphere;

/**
 * A camera map element that provides a view from the terrain level. It can be
 * placed anywhere in the scene and has adjustable height, pan, and tilt.
 *
 */
public class FieldCamera extends Movable implements Tool, ViewDependent {

	public static final Icon icon = Icons.getImageIcon("fieldcamera_16.png");
	protected static float AMBIENT_FACTOR = 0.75f;

	// Defaults
	public static Color defaultColor = Color.orange;
	public static boolean defaultLabelVisible = true;
	public static boolean defaultFovVisible = false;
	public static boolean defaultLineVisible = false;
	public static String defaultDefinition = "CameraDef1";

	// Icon textures
	protected static volatile Texture nominalTexture;
	protected static volatile Texture highlightTexture;

	public AtomicBoolean changed = new AtomicBoolean();

	// Camera Definition
	private FieldCameraInfo fieldCameraInfo;

	// Field Camera Box
	private Mesh box;
	private Node viewDependentNode;
	private BillboardNode billboard;
	private ImageQuad imageQuad;
	private Sphere base;
	
	// Field Camera node
	private SyntheticCameraNode cameraNode;

	// Pan and tilt
	private double height, panValue, tiltValue;
	private Node pan, tilt;
	private Matrix3 rotMat = new Matrix3();

	// Camera stand
	private Vector3 p0 = new Vector3(), p1 = new Vector3();
	private LineSegment lineSegment;

	// Attributes
	private ColorRGBA colorRGBA;
	private ColorRGBA highlightColorRGBA;
	private MaterialState materialState;
	private Color color;

	// scale factor for viewpoint resizing
	private double scale = 1, oldScale = 1;
	private RasterText label;

	// Map element state
	private FieldCameraState state;
	
	// Scale for sizing camera in relation to landscape
	private float pixelScale;

	public FieldCamera(FieldCameraState state) {
		super(state.name);
		this.state = state;
		if (state.location != null)
			setLocation(state.location, false);
		fieldCameraInfo = FieldCameraInfoManager.getInstance().getFieldCameraInfo(state.fieldCameraDef);
		
		pixelScale = (float)Landscape.getInstance().getPixelWidth();

		// camera stand
		base = new Sphere("_base", 50, 50, pixelScale);
		base.setModelBound(new BoundingBox());
		attachChild(base);
		lineSegment = new LineSegment("_post", p0, p1);
		attachChild(lineSegment);
		
		// FieldCamera node
		cameraNode = new SyntheticCameraNode(fieldCameraInfo);
		cameraNode.setFovVisible(state.fovVisible);
		cameraNode.setSiteLineVisible(state.lineVisible);
		
		// camera box
		box = new Box("_box", new Vector3(), pixelScale, pixelScale, pixelScale);
		box.setModelBound(new BoundingBox());
		cameraNode.getGeometryNode().attachChild(box);

		// special billboard for spotting fieldCamera from far away
		viewDependentNode = new Node("_viewDependent");
		billboard = new BillboardNode("_billboard");
		imageQuad = new ImageQuad("_imageQuad", getIconTexture(), 1.25);
		billboard.attachChild(imageQuad);
		SpatialUtil.setPickHost(imageQuad, this);
		viewDependentNode.attachChild(billboard);
		label = new RasterText("_label", state.name, AlignType.Center, true);
		label.setScaleFactor(0.75f);
		label.setColor(ColorRGBA.WHITE);
		label.setTranslation(0, 1.3, 0);
		label.setVisible(state.labelVisible);
		billboard.attachChild(label);
		viewDependentNode.setTranslation(0.0, pixelScale, 0.0);
		cameraNode.getGeometryNode().attachChild(viewDependentNode);

		// tilt and pan
		tilt = new Node("_tilt");
		tilt.attachChild(cameraNode);
		pan = new Node("_pan");
		pan.attachChild(tilt);
		attachChild(pan);

		// initialize other fields
		setColor(state.color);

		setAzElHgt(fieldCameraInfo);
		
		changed.set(true);

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
	
	public void setHiddenDashed(boolean hiddenDashed) {
		cameraNode.setHiddenDashed(hiddenDashed);
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
	public boolean setFieldCameraDefinition(FieldCameraInfo fieldCameraInfo) {
		if (this.fieldCameraInfo.name.equals(fieldCameraInfo.name))
			return(false);
		cameraNode.setTranslation(fieldCameraInfo.mountingOffset);		
		cameraNode.setFieldCameraDefinition(fieldCameraInfo);
		setAzElHgt(fieldCameraInfo);
		return(true);
	}
		
	private void setAzElHgt(FieldCameraInfo fieldCameraInfo) {		
		if (!Double.isNaN(state.azimuth)) 
			setAzimuth(state.azimuth);
		else
			setAzimuth(fieldCameraInfo.tripodPan);
		if (!Double.isNaN(state.tilt)) 
			setElevation(state.tilt);
		else
			setElevation(fieldCameraInfo.tripodTilt);
		if (!Double.isNaN(state.height)) 
			setHeight(state.height);
		else
			setHeight(pixelScale*fieldCameraInfo.tripodHeight);
	}

	/**
	 * Get the field camera definition name
	 * 
	 * @return
	 */
	public String getFieldCameraDefinition() {
		return (fieldCameraInfo.name);
	}

	public ReadOnlyColorRGBA getColorRGBA() {
		return (colorRGBA);
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
		cameraNode.getGeometryNode().getSceneHints().setCullHint(CullHint.Always);
		cameraNode.getGeometryNode().updateGeometricState(0);
		lineSegment.getSceneHints().setCullHint(CullHint.Always);
		lineSegment.updateGeometricState(0);
		base.getSceneHints().setCullHint(CullHint.Always);
		base.updateGeometricState(0);
	}

	/**
	 * Show camera parts
	 */
	public void uncull() {
		cameraNode.getGeometryNode().getSceneHints().setCullHint(CullHint.Inherit);
		cameraNode.getGeometryNode().updateGeometricState(0);
		lineSegment.getSceneHints().setCullHint(CullHint.Inherit);
		lineSegment.updateGeometricState(0);
		base.getSceneHints().setCullHint(CullHint.Inherit);
		base.updateGeometricState(0);
	}

	public SyntheticCameraNode getSyntheticCameraNode() {
		return (cameraNode);
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
		if (height == 0)
			height = 0.00001;
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
		scale = camera.getPixelSizeAt(getWorldTranslation(), true) * Marker.PIXEL_SIZE;
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
		return (SpatialUtil.isDisplayed(this));
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
		markDirty(DirtyType.RenderState);
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
	 * The landscape changed but we don't want the camera bobbing up and down
	 */
	@Override
	public boolean updateElevation(QuadTree quadTree) {
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
		colorRGBA = UIUtil.colorToColorRGBA(color);
		if (materialState == null) {
			// add a material state
			materialState = new MaterialState();
			materialState.setColorMaterial(ColorMaterial.None);
			materialState.setEnabled(true);
			setRenderState(materialState);
		}
		cameraNode.setColor(color);
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
			imageQuad.setTexture(highlightTexture);
			materialState.setEmissive(MaterialFace.FrontAndBack, highlightColorRGBA);
		} else {
			imageQuad.setTexture(nominalTexture);
			materialState.setEmissive(MaterialFace.FrontAndBack, colorRGBA);
		}
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
		defaultDefinition = properties.getProperty("MapElement.FieldCamera.defaultDefinition", defaultDefinition);
		defaultLabelVisible = StringUtil.getBooleanValue(properties, "MapElement.FieldCamera.defaultLabelVisible",
			defaultLabelVisible, false);
		defaultFovVisible = StringUtil.getBooleanValue(properties, "MapElement.FieldCamera.defaultFovVisible",
			defaultFovVisible, false);
		defaultLineVisible = StringUtil.getBooleanValue(properties, "MapElement.FieldCamera.defaultLineVisible",
			defaultLineVisible, false);
	}

	/**
	 * Save defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		properties.setProperty("MapElement.FieldCamera.defaultColor", StringUtil.colorToString(defaultColor));
		properties.setProperty("MapElement.FieldCamera.defaultDefinition", defaultDefinition);
		properties.setProperty("MapElement.FieldCamera.defaultLabelVisible", Boolean.toString(defaultLabelVisible));
		properties.setProperty("MapElement.FieldCamera.defaultFovVisible", Boolean.toString(defaultFovVisible));
		properties.setProperty("MapElement.FieldCamera.defaultLineVisible", Boolean.toString(defaultLineVisible));
	}
}
