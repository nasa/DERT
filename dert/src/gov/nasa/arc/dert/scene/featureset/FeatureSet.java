package gov.nasa.arc.dert.scene.featureset;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.io.geojson.GeojsonLoader;
import gov.nasa.arc.dert.io.geojson.json.GeoJsonObject;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.QuadTree;
import gov.nasa.arc.dert.raster.SpatialReferenceSystem;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scenegraph.GroupNode;
import gov.nasa.arc.dert.state.FeatureSetState;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.util.SpatialUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.world.GroundEdit;
import gov.nasa.arc.dert.viewpoint.BasicCamera;
import gov.nasa.arc.dert.viewpoint.ViewDependent;

import java.awt.Color;
import java.util.List;
import java.util.Properties;

import javax.swing.Icon;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;

/**
 * Provides a MapElement that consists of a set of lines and/or points. Input is from a
 * GeoJSON file
 *
 */
public class FeatureSet extends GroupNode implements MapElement, ViewDependent {

	public static final Icon icon = Icons.getImageIcon("lineset_16.png");
	public static Color defaultColor = Color.white;
	public static float defaultSize = 0.75f, defaultLineWidth = 2;

	// Line color
	private Color color;

	// Path to GeoJSON file
	private String filePath;

	// The map element state object
	private FeatureSetState state;

	// The location of the origin
	private Vector3 location;
	
	private boolean labelVisible;
	
	private boolean ground;
	
	private float size, lineWidth;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public FeatureSet(FeatureSetState state) {
		this(state, null, Landscape.getInstance().getSpatialReferenceSystem());
	}

	/**
	 * Constructor for contours
	 * 
	 * @param state
	 * @param elevAttrName
	 */
	public FeatureSet(FeatureSetState state, String elevAttrName, SpatialReferenceSystem srs) {
		super(state.name);
		location = new Vector3();
		this.filePath = state.filePath;
		ground = state.ground;
		color = state.color;
		size = (float)state.size;
		lineWidth = state.lineWidth;
		setVisible(state.visible);
		this.state = state;
		state.setMapElement(this);
		// Load the vector file into an Ardor3D object.
		GeojsonLoader jsonLoader = new GeojsonLoader(srs, elevAttrName, state.labelProp, ground, size, lineWidth);
		GeoJsonObject gjRoot = jsonLoader.load(filePath);
		jsonLoader.geoJsonToArdor3D(gjRoot, this, color);
		if (getNumberOfChildren() == 0) {
			throw new IllegalStateException("No vectors found.");
		}
		setLabelVisible(state.labelVisible);
		getSceneHints().setCullHint(CullHint.Dynamic);
	}
	
	public Feature getFeature(int id) {
		List<Spatial> child = getChildren();
		for (int i=0; i<child.size(); ++i) {
			Spatial spat = child.get(i);
			if (spat instanceof Feature) {
				Feature f = (Feature)spat;
				if (f.getState().id == id)
					return(f);
			}
		}
		return(null);
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
		return (SpatialUtil.isDisplayed(this));
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
	public void setLocked(boolean pinned) {
		// nothing here
	}

	/**
	 * Find out if pinned
	 */
	@Override
	public boolean isLocked() {
		return (false);
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
		for (int i=0; i<getNumberOfChildren(); ++i) {
			Feature feature = (Feature)getChild(i);
			feature.setColor(color);
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
		if (!ground)
			return(false);
		boolean modified = false;
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			Spatial child = getChild(i);
			if (child instanceof Feature) {
				Feature feature = (Feature) child;
				modified |= feature.updateElevation(quadTree);
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
			if (child instanceof Feature) {
				Feature feature = (Feature) child;
				feature.setVerticalExaggeration(vertExag, oldVertExag, minZ);
			}
		}
	}

	/**
	 * Get the map element type
	 */
//	@Override
	public Type getType() {
		return (Type.FeatureSet);
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
		labelVisible = visible;
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			Spatial child = getChild(i);
			if (child instanceof Feature) {
				Feature feature = (Feature) child;
				feature.setLabelVisible(visible);
			}
		}
	}

	/**
	 * Find out if the label is visible
	 */
	@Override
	public boolean isLabelVisible() {
		return (labelVisible);
	}

	/**
	 * Set the point size.
	 */
	public void setPointSize(float size) {
		this.size = size;
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			Spatial child = getChild(i);
			if (child instanceof Feature) {
				Feature feature = (Feature) child;
				feature.setSize(size);
			}
		}
	}

	/**
	 * Get the size (returns 1).
	 */
	@Override
	public double getSize() {
		return (size);
	}

	/**
	 * Set the line width.
	 */
	public void setLineWidth(float lineWidth) {
		this.lineWidth = lineWidth;
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			Spatial child = getChild(i);
			if (child instanceof Feature) {
				Feature feature = (Feature) child;
				feature.setLineWidth(lineWidth);
			}
		}
	}
	
	/**
	 * Get the line width.
	 * @return
	 */
	public float getLineWidth() {
		return(lineWidth);
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
	 * Get the location of the origin in planetary coordinates
	 */
	public ReadOnlyVector3 getLocationInWorld() {
		location.set(getWorldTranslation());
		Landscape.getInstance().localToWorldCoordinate(location);
		return (location);
	}
	
	public GroundEdit ground() {
		return(null);
	}
	
	public void setZOffset(double zOff, boolean doTrans) {
		// do nothing
	}
	
	public double getZOffset() {
		return(0);
	}
	
	public void update(BasicCamera camera) {
		if (!isVisible())
			return;
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			Feature feature = (Feature)getChild(i);
			if (feature.isVisible())
				feature.update(camera);
		}
	}

	/**
	 * Set the defaults
	 * 
	 * @param properties
	 */
	public static void setDefaultsFromProperties(Properties properties) {
		defaultColor = StringUtil.getColorValue(properties, "MapElement.FeatureSet.defaultColor", defaultColor, false);
		defaultSize = (float) StringUtil.getDoubleValue(properties, "MapElement.FeatureSet.defaultSize", true, defaultSize, false);
		defaultLineWidth = (float) StringUtil.getDoubleValue(properties, "MapElement.FeatureSet.defaultLineWidth", true, defaultLineWidth, false);
	}

	/**
	 * Save the defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		properties.setProperty("MapElement.FeatureSet.defaultColor", StringUtil.colorToString(defaultColor));
		properties.setProperty("MapElement.FeatureSet.defaultSize", Float.toString(defaultSize));
		properties.setProperty("MapElement.FeatureSet.defaultLineWidth", Float.toString(defaultLineWidth));
	}

}
