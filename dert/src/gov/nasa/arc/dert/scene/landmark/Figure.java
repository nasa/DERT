package gov.nasa.arc.dert.scene.landmark;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scenegraph.FigureMarker;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.state.FigureState;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.Color;
import java.util.Properties;

import javax.swing.Icon;

import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;

/**
 * Provides a 3D figure map element. This element has depth and causes shadows.
 *
 */
public class Figure extends FigureMarker implements Landmark {

	public static final Icon icon = Icons.getImageIcon("figure.png");

	// Defaults
	public static Color defaultColor = Color.red;
	public static double defaultSize = 1.0f;
	public static ShapeType defaultShapeType = ShapeType.box;
	public static boolean defaultLabelVisible = true;
	public static boolean defaultAutoScale = true;
	public static boolean defaultSurfaceNormalVisible = false;
	public static double defaultAzimuth = 0;
	public static double defaultTilt = 0;

	// The map element state
	private FigureState state;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public Figure(FigureState state) {
		super(state.name, state.position, state.size, state.color, state.labelVisible, state.autoScale, state.pinned);
		setShape(state.shape);
		setAzimuth(state.azimuth);
		setTilt(state.tilt);
		setSurfaceNormalVisible(state.showNormal);
		setVisible(state.visible);
		this.state = state;
		state.setMapElement(this);
	}

	/**
	 * Get the map element state
	 */
	@Override
	public MapElementState getState() {
		return (state);
	}

	/**
	 * Get the point and distance to seek to
	 */
	@Override
	public double getSeekPointAndDistance(Vector3 point) {
		point.set(getTranslation());
		return (getRadius() * 1.5);
	}

	/**
	 * Show the surface normal
	 * 
	 * @param show
	 */
	public void setSurfaceNormalVisible(boolean show) {
		surfaceNormalArrow.getSceneHints().setCullHint(show ? CullHint.Inherit : CullHint.Always);
		markDirty(DirtyType.RenderState);
	}

	/**
	 * Find out if the surface normal is visible
	 * 
	 * @return
	 */
	public boolean isSurfaceNormalVisible() {
		return (surfaceNormalArrow.getSceneHints().getCullHint() != CullHint.Always);
	}

	/**
	 * Get the map element type
	 */
	@Override
	public Type getType() {
		return (Type.Figure);
	}

	/**
	 * Get the map element icon
	 */
	@Override
	public Icon getIcon() {
		return (icon);
	}

	/**
	 * Set the defaults
	 * 
	 * @param properties
	 */
	public static void setDefaultsFromProperties(Properties properties) {
		defaultColor = StringUtil.getColorValue(properties, "MapElement.Figure.defaultColor", defaultColor, false);
		defaultSize = (float) StringUtil.getDoubleValue(properties, "MapElement.Figure.defaultSize", true, defaultSize,
			false);
		String str = properties.getProperty("MapElement.Figure.defaultShapeType", defaultShapeType.toString());
		defaultShapeType = ShapeType.valueOf(str);
		defaultLabelVisible = StringUtil.getBooleanValue(properties, "MapElement.Figure.defaultLabelVisible",
			defaultLabelVisible, false);
		defaultAutoScale = StringUtil.getBooleanValue(properties, "MapElement.Figure.defaultAutoScale", defaultAutoScale, false);
		defaultSurfaceNormalVisible = StringUtil.getBooleanValue(properties,
			"MapElement.Figure.defaultSurfaceNormalVisible", defaultSurfaceNormalVisible, false);
		defaultAzimuth = StringUtil.getDoubleValue(properties, "MapElement.Figure.defaultAzimuth", false,
			defaultAzimuth, false);
		defaultTilt = StringUtil.getDoubleValue(properties, "MapElement.Figure.defaultTilt", false, defaultTilt, false);
	}

	/**
	 * Save the defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		properties.setProperty("MapElement.Figure.defaultColor", StringUtil.colorToString(defaultColor));
		properties.setProperty("MapElement.Figure.defaultSize", Double.toString(defaultSize));
		properties.setProperty("MapElement.Figure.defaultShapeType", defaultShapeType.toString());
		properties.setProperty("MapElement.Figure.defaultLabelVisible", Boolean.toString(defaultLabelVisible));
		properties.setProperty("MapElement.Figure.defaultAutoScale", Boolean.toString(defaultAutoScale));
		properties.setProperty("MapElement.Figure.defaultSurfaceNormalVisible", Boolean.toString(defaultSurfaceNormalVisible));
		properties.setProperty("MapElement.Figure.defaultAzimuth", Double.toString(defaultAzimuth));
		properties.setProperty("MapElement.Figure.defaultTilt", Double.toString(defaultTilt));
	}
}
