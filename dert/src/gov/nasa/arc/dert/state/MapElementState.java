package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scenegraph.MotionListener;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.ui.TextDialog;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.View;

import java.awt.Color;
import java.util.HashMap;

import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;

/**
 * Base class for map element state objects.
 *
 */
public abstract class MapElementState extends State {

	// Types of map elements
	public static enum Type {
		Placemark, Figure, Billboard, FeatureSet, Feature, Path, Plane, CartesianGrid, RadialGrid, Profile, FieldCamera, Waypoint, Scale, Marble
	}
	
	protected static final int X_OFFSET = 20, Y_OFFSET = 20;

	// User defined note
	protected String annotation;

	// Options
	public boolean visible;
	public boolean pinned, labelVisible;
	public double size;
	public Color color;
	public double zOff;

	// Map element type
	public Type mapElementType;

	// Index id for groups
	public long id;

	// The MapElement associated with this state
	protected transient MapElement mapElement;
	// Dialog for viewing the annotation
	protected transient TextDialog annotationDialog;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param mapElementType
	 * @param prefix
	 */
	public MapElementState(long id, Type mapElementType, String prefix) {
		this(id, mapElementType, prefix, 1, Color.white, true);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param mapElementType
	 * @param prefix
	 * @param size
	 * @param color
	 * @param labelVisible
	 * @param pinned
	 */
	public MapElementState(long id, Type mapElementType, String prefix, double size, Color color, boolean labelVisible) {
		super(prefix + id, StateType.MapElement, null);
		this.id = id;
		this.size = size;
		this.color = color;
		this.labelVisible = labelVisible;
		visible = true;
		this.mapElementType = mapElementType;
		annotation = "";
	}
	
	/**
	 * Constructor from hash map.
	 */
	public MapElementState(HashMap<String,Object> map) {
		super(map);
		annotation = StateUtil.getString(map, "Annotation", "");
		visible = StateUtil.getBoolean(map, "Visible", true);
		pinned = StateUtil.getBoolean(map, "Pinned", false);
		labelVisible = StateUtil.getBoolean(map, "LabelVisible", true);
		size = StateUtil.getDouble(map, "Size", 1);
		color = StateUtil.getColor(map, "Color", Color.white);
		String str = StateUtil.getString(map, "MapElementType", null);
		mapElementType = Type.valueOf(str);
		id = StateUtil.getLong(map, "MapElementId", 0);
		zOff = StateUtil.getDouble(map, "ZOffset", 0);
	}
	
	@Override
	public boolean isEqualTo(State state) {
		MapElementState that = (MapElementState)state;
		if (!super.isEqualTo(that))
			return(false);
		if (!this.annotation.equals(that.annotation))
			return(false);
		if (this.visible != that.visible)
			return(false);
		if (this.pinned != that.pinned)
			return(false);
		if (this.labelVisible != that.labelVisible) 
			return(false);
		if (this.zOff != that.zOff) 
			return(false);
		if (this.size != that.size) 
			return(false);
		if (!this.color.equals(that.color))
			return(false);
		if (this.mapElementType != that.mapElementType) 
			return(false);
		if (this.id != that.id) 
			return(false);
		return(true);
	}
	
	@Override
	public String toString() {
		String str = super.toString();
		str += " Visible="+visible+" Pinned="+pinned+" LabelVisible="+labelVisible+" Size="+size+" MapElementType="+mapElementType+" Color="+color+" Id="+id+" Note="+annotation;
		return(str);
	}

	/**
	 * Get the MapElement
	 * 
	 * @return
	 */
	public MapElement getMapElement() {
		return (mapElement);
	}

	/**
	 * Set the MapElement
	 * 
	 * @param me
	 */
	public void setMapElement(MapElement me) {
		this.mapElement = me;
		if (mapElement instanceof Movable) {
			Movable movable = (Movable) mapElement;
			movable.addMotionListener(new MotionListener() {
				@Override
				public void move(Movable mo, ReadOnlyVector3 pos) {
					if (annotationDialog != null) {
						annotationDialog.setMessage(StringUtil.format(mapElement.getLocationInWorld()));
					}
				}
			});
		}
	}

	@Override
	public HashMap<String,Object> save() {
		HashMap<String,Object> map = super.save();
		if (mapElement != null) {
			name = mapElement.getName();
			size = mapElement.getSize();
			color = mapElement.getColor();
			pinned = mapElement.isPinned();
			visible = mapElement.isVisible();
			labelVisible = mapElement.isLabelVisible();
			zOff = mapElement.getZOffset();
		}
		map.put("Name", name);
		map.put("Size", new Double(size));
		map.put("ZOffset", new Double(zOff));
		map.put("Color", color);
		map.put("Pinned", new Boolean(pinned));
		map.put("Annotation", annotation);
		map.put("Visible", new Boolean(visible));
		map.put("LabelVisible", new Boolean(labelVisible));
		map.put("MapElementType", mapElementType.toString());
		map.put("MapElementId", new Long(id));
		return(map);
	}

	/**
	 * Open the annotation
	 */
	public void openAnnotation() {
		if (annotationDialog == null) {
			annotationDialog = new TextDialog(Dert.getMainWindow(), name, 400, 200, true, false);
		}
		if (mapElement != null) {
			annotationDialog.setMessage(StringUtil.format(mapElement.getLocationInWorld()));
		}
		annotationDialog.setText(annotation);
		annotationDialog.open();
	}

	/**
	 * Set the annotation
	 * 
	 * @param note
	 */
	public void setAnnotation(String note) {
		if (note != null) {
			annotation = note;
		}
		if (annotationDialog != null) {
			annotationDialog.setText(note);
		}
	}

	/**
	 * Get the annotation
	 * 
	 * @return
	 */
	public String getAnnotation() {
		return (annotation);
	}

	@Override
	public void dispose() {
		if (mapElement != null) {
			Spatial spatial = (Spatial) mapElement;
			Node parent = spatial.getParent();
			if (parent != null) {
				parent.detachChild(spatial);
			}
			mapElement = null;
		}
		if (annotationDialog != null) {
			annotationDialog.close();
		}
		annotationDialog = null;
	}

	/**
	 * Open a view associated with the MapElement
	 * 
	 * @return
	 */
	public View open() {
		// This state element has no view
		if (viewData == null) {
			return (null);
		}
		
		// The view is not visible
		if (!viewData.isVisible())
			return(null);
		
		// This state element has a view
		if (viewData.viewWindow != null) {
			viewData.viewWindow.setVisible(true);
			return (viewData.view);
		}
		
		// No view, create one
		createView();
		viewData.viewWindow.setVisible(true);
		return (viewData.view);
	}
	
	protected void createView() {
		// nothing here
	}

}
