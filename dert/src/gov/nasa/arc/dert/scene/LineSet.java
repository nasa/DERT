package gov.nasa.arc.dert.scene;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.io.geojson.GeoJsonObject;
import gov.nasa.arc.dert.io.geojson.GeojsonLoader;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.QuadTree;
import gov.nasa.arc.dert.raster.SpatialReferenceSystem;
import gov.nasa.arc.dert.scenegraph.GroupNode;
import gov.nasa.arc.dert.scenegraph.LineStrip;
import gov.nasa.arc.dert.state.LineSetState;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.Color;
import java.util.Properties;

import javax.swing.Icon;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;

/**
 * Provides a MapElement that consists of a set of lines. Input is from a
 * GeoJSON file
 *
 */
public class LineSet extends GroupNode implements MapElement {

	public static final Icon icon = Icons.getImageIcon("lineset.png");
	public static Color defaultColor = Color.white;

	// Line color
	private Color color;

	// Path to GeoJSON file
	private String filePath;

	// The map element state object
	private LineSetState state;

	// The location of the origin
	private Vector3 location;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public LineSet(LineSetState state) {
		this(state, null, Landscape.getInstance().getSpatialReferenceSystem());
	}

	/**
	 * Constructor for contours
	 * 
	 * @param state
	 * @param elevAttrName
	 */
	public LineSet(LineSetState state, String elevAttrName, SpatialReferenceSystem srs) {
		super(state.name);
		location = new Vector3();
		this.filePath = state.filePath;
		color = state.color;
		setVisible(state.visible);
		this.state = state;
		state.setMapElement(this);
		// Load the vector file into an Ardor3D object.
		GeojsonLoader jsonLoader = new GeojsonLoader(srs);
		GeoJsonObject gjRoot = jsonLoader.load(filePath);
		jsonLoader.geoJsonToArdor3D(gjRoot, this, Color.white, state.color, elevAttrName);
		if (getNumberOfChildren() == 0) {
			throw new IllegalStateException("No vectors found.");
		}
	}

	/**
	 * Get the MapElement state
	 */
	@Override
	public MapElementState getState() {
		return (state);
	}

	/**
	 * Is this visible
	 */
	@Override
	public boolean isVisible() {
		return (getSceneHints().getCullHint() != CullHint.Always);
	}

	/**
	 * Set visible
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
	 * Pin this Lineset (does nothing)
	 */
	@Override
	public void setPinned(boolean pinned) {
		// nothing here
	}

	/**
	 * Find out if pinned
	 */
	@Override
	public boolean isPinned() {
		return (true);
	}

	/**
	 * Get the color
	 */
	@Override
	public Color getColor() {
		return (color);
	}

	/**
	 * Set the color
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
		ColorRGBA colorRGBA = new ColorRGBA(color.getRed() / 255.0f, color.getGreen() / 255.0f,
			color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
		setColor(this, colorRGBA);
		markDirty(DirtyType.RenderState);
	}

	private void setColor(Node node, ColorRGBA colorRGBA) {
		int n = node.getNumberOfChildren();
		for (int i = 0; i < n; ++i) {
			Spatial child = getChild(i);
			if (child instanceof LineStrip) {
				((LineStrip) child).setDefaultColor(colorRGBA);
			} else if (child instanceof GroupNode) {
				setColor((GroupNode) child, colorRGBA);
			}
		}
	}

	/**
	 * Get the GeoJSON file path
	 * 
	 * @return
	 */
	public String getFilePath() {
		return (filePath);
	}

	/**
	 * Update the elevation (Z coordinate) for the lines
	 */
	@Override
	public boolean updateElevation(QuadTree quadTree) {
		boolean modified = false;
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			Spatial child = getChild(i);
			if (child instanceof LineStrip) {
				LineStrip lineStrip = (LineStrip) child;
				if (lineStrip.intersects(quadTree)) {
					lineStrip.updateElevation(quadTree);
					modified = true;
				}
			}
		}
		return (modified);
	}

	/**
	 * Set the vertical exaggeration
	 */
	@Override
	public void setVerticalExaggeration(double vertExag, double oldVertExag, double minZ) {
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			Spatial child = getChild(i);
			if (child instanceof LineStrip) {
				LineStrip lineStrip = (LineStrip) child;
				lineStrip.setScale(1, 1, vertExag);
			}
		}
	}

	/**
	 * Get the map element type
	 */
	@Override
	public Type getType() {
		return (Type.LineSet);
	}

	/**
	 * Get the point to seek for this lineset (center).
	 */
	@Override
	public double getSeekPointAndDistance(Vector3 point) {
		BoundingVolume bv = getWorldBound();
		point.set(bv.getCenter());
		return (bv.getRadius() * 1.5);
	}

	/**
	 * Set the label visibility (does nothing).
	 */
	@Override
	public void setLabelVisible(boolean visible) {
		// do nothing
	}

	/**
	 * Find out if the label is visible
	 */
	@Override
	public boolean isLabelVisible() {
		return (false);
	}

	/**
	 * Get the size (returns 1).
	 */
	@Override
	public double getSize() {
		return (1);
	}

	/**
	 * Get the lineset icon
	 * 
	 * @return
	 */
	public Icon getIcon() {
		return (icon);
	}

	/**
	 * Get the location (translation)
	 */
	@Override
	public ReadOnlyVector3 getLocation() {
		location.set(getWorldTranslation());
		Landscape.getInstance().localToWorldCoordinate(location);
		return (location);
	}

	/**
	 * Set the defaults
	 * 
	 * @param properties
	 */
	public static void setDefaultsFromProperties(Properties properties) {
		defaultColor = StringUtil.getColorValue(properties, "MapElement.LineSet.defaultColor", defaultColor, false);
	}

	/**
	 * Save the defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		properties.setProperty("MapElement.LineSet.defaultColor", StringUtil.colorToString(defaultColor));
	}

}
