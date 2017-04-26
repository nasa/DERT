package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.View;
import gov.nasa.arc.dert.view.mapelement.EditDialog;
import gov.nasa.arc.dert.view.mapelement.NotesDialog;

import java.awt.Color;
import java.util.HashMap;

import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyType;

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
	public boolean locked, labelVisible;
	public double size;
	public Color color;
	public double zOff;

	// Map element type
	public Type mapElementType;

	// Index id
	public int id;

	// The MapElement associated with this state
	protected transient MapElement mapElement;
	// Dialog for viewing the annotation
	protected transient NotesDialog annotationDialog;
	// Dialog for editing the map element
	protected transient EditDialog editDialog;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param mapElementType
	 * @param prefix
	 */
	public MapElementState(int id, Type mapElementType, String prefix) {
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
	public MapElementState(int id, Type mapElementType, String prefix, double size, Color color, boolean labelVisible) {
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
		locked = StateUtil.getBoolean(map, "Locked", false);
		labelVisible = StateUtil.getBoolean(map, "LabelVisible", true);
		size = StateUtil.getDouble(map, "Size", 1);
		color = StateUtil.getColor(map, "Color", Color.white);
		String str = StateUtil.getString(map, "MapElementType", null);
		mapElementType = Type.valueOf(str);
		id = StateUtil.getInteger(map, "MapElementId", 0);
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
		if (this.locked != that.locked)
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
		str += " Visible="+visible+" Locked="+locked+" LabelVisible="+labelVisible+" Size="+size+" MapElementType="+mapElementType+" Color="+color+" Id="+id+" Note="+annotation;
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
		// first time
		if ((mapElement == null) && (me.getType() == mapElementType))
			mapElement = me;
		if (annotationDialog != null)
			annotationDialog.setMapElement(me);
		if (editDialog != null)
			editDialog.setMapElement(me);
	}

	@Override
	public HashMap<String,Object> save() {
		HashMap<String,Object> map = super.save();
		if (mapElement != null) {
			name = mapElement.getName();
			size = mapElement.getSize();
			color = mapElement.getColor();
			locked = mapElement.isLocked();
			visible = mapElement.isVisible();
			labelVisible = mapElement.isLabelVisible();
			zOff = mapElement.getZOffset();
		}
		map.put("Name", name);
		map.put("Size", new Double(size));
		map.put("ZOffset", new Double(zOff));
		map.put("Color", color);
		map.put("Locked", new Boolean(locked));
		map.put("Annotation", annotation);
		map.put("Visible", new Boolean(visible));
		map.put("LabelVisible", new Boolean(labelVisible));
		map.put("MapElementType", mapElementType.toString());
		map.put("MapElementId", new Integer(id));
		return(map);
	}

	/**
	 * Open the editor
	 */
	public EditDialog openEditor() {
		if (mapElement == null)
			return(null);
		if (editDialog == null)
			editDialog = new EditDialog(Dert.getMainWindow(), "Edit "+mapElement.getName(), mapElement);
		else
			editDialog.update();
		editDialog.open();
		return(editDialog);
	}

	/**
	 * Open the annotation
	 */
	public NotesDialog openAnnotation() {
		if (mapElement == null)
			return(null);
		if (annotationDialog == null)
			annotationDialog = new NotesDialog(Dert.getMainWindow(), name+" Notes", 400, 200, mapElement);
		else
			annotationDialog.update();
		annotationDialog.open();
		return(annotationDialog);
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
//		if (annotationDialog != null) {
//			annotationDialog.update();
//		}
//		if (editDialog != null) {
//			editDialog.update();
//		}
	}
	
	public void setLocked(boolean locked) {
		if (mapElement != null) {
			mapElement.setLocked(locked);
			((Spatial)mapElement).markDirty(DirtyType.RenderState);
		}
		if (annotationDialog != null) {
			annotationDialog.update();
		}
		if (editDialog != null) {
			editDialog.update();
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
		if (editDialog != null) {
			editDialog.dispose();
			editDialog.close();
		}
		editDialog = null;
	}

	/**
	 * Open a view associated with the MapElement
	 * 
	 * @return
	 */
	@Override
	public View open(boolean doIt) {
		// This state element has no view
		if (viewData == null) {
			return (null);
		}
		
		if (doIt)
			viewData.setVisible(true);
		
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
	
	public void move(Movable mo, ReadOnlyVector3 pos) {
		if (annotationDialog != null) {
			annotationDialog.update();
		}
		if (editDialog != null) {
			editDialog.update();
		}
	}

}
