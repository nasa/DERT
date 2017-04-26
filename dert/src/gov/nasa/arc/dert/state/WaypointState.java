package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.tool.Waypoint;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.mapelement.EditDialog;

import java.awt.Color;
import java.util.HashMap;

import javax.swing.Icon;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides a state object for a Waypoint.
 *
 */
public class WaypointState extends MapElementState {

	public static final Icon icon = Icons.getImageIcon("waypoint_24.png");

	// Waypoint location
	public Vector3 location;

	// State object for waypoint parent path
	public transient PathState parent;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param position
	 * @param prefix
	 * @param size
	 * @param color
	 * @param labelVisible
	 * @param pinned
	 */
	public WaypointState(int id, ReadOnlyVector3 position, String prefix, double size, Color color,
		boolean labelVisible, boolean locked) {
		super(id, MapElementState.Type.Waypoint, prefix, size, color, labelVisible);
		location = new Vector3(position);
		this.locked = locked;
	}
	
	/**
	 * Constructor from hash map.
	 */
	public WaypointState(HashMap<String,Object> map) {
		super(map);
		location = StateUtil.getVector3(map, "Location", null);
		if (location == null)
			throw new NullPointerException("Waypoint location is missing.");
	}
	
	@Override
	public boolean isEqualTo(State state) {
		WaypointState that = (WaypointState)state;
		if (!super.isEqualTo(that))
			return(false);
		return(this.location.equals(that.location));
	}

	@Override
	public HashMap<String,Object> save() {
		HashMap<String,Object> map = super.save();
		if (mapElement != null) {
			Waypoint waypoint = (Waypoint) mapElement;
			location = new Vector3(waypoint.getLocation());
			parent = (PathState) waypoint.getPath().getState();
		}
		StateUtil.putVector3(map, "Location", location);
		// Waypoint is uses Path locked state.
		map.put("Locked", new Boolean(false));
		return(map);
	}

	/**
	 * Open the editor
	 */
	@Override
	public EditDialog openEditor() {
		if (mapElement == null)
			return(null);
		parent = (PathState) ((Waypoint)mapElement).getPath().getState();
		EditDialog ed = parent.getEditDialog();
		if (ed != null) {
			ed.open();
			ed.setMapElement(mapElement);
			ed.update();
		}
		return(ed);
	}

	/**
	 * Open the annotation
	 */
//	@Override
//	public NotesDialog openAnnotation() {
//		if (mapElement == null)
//			return(null);
//		parent = (PathState) ((Waypoint)mapElement).getPath().getState();
//		NotesDialog nd = parent.getAnnotationDialog();
//		if (nd != null) {
//			nd.open();
//			nd.setMapElement(mapElement);
//			nd.update();
//		}
//		return(nd);
//	}

//	@Override
//	public void setAnnotation(String note) {
//		if (note != null) {
//			annotation = note;
//		}
////		parent = (PathState) ((Waypoint)mapElement).getPath().getState();
////		parent.setMapElement(mapElement);
//	}

	
	@Override
	public String toString() {
		String str = "["+location+"]"+super.toString();
		return(str);
	}

}
