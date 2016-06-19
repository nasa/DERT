package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.tool.Waypoint;
import gov.nasa.arc.dert.ui.TextDialog;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.util.StringUtil;

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
	public WaypointState(long id, ReadOnlyVector3 position, String prefix, double size, Color color,
		boolean labelVisible, boolean pinned, boolean zBufferEnabled) {
		super(id, MapElementState.Type.Waypoint, prefix, size, color, labelVisible, pinned);
		location = new Vector3(position);
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
			location = new Vector3(waypoint.getTranslation());
			parent = (PathState) waypoint.getPath().getState();
		}
		StateUtil.putVector3(map, "Location", location);
		return(map);
	}

	@Override
	public void openAnnotation() {
		if (annotationDialog == null) {
			annotationDialog = new TextDialog(null, name, 400, 200, true, false);
		}
		setAnnotation(null);
		annotationDialog.open();
	}

	@Override
	public void setAnnotation(String note) {
		if (note != null) {
			annotation = note;
		}
		if (annotationDialog != null) {
			String annot = "W A Y P O I N T :\n" + annotation;
			annot += "\n\n";
			// also display Path annotation
			if (mapElement != null) {
				Waypoint waypoint = (Waypoint) mapElement;
				parent = (PathState) waypoint.getPath().getState();
				if (parent != null) {
					annot += "P A T H :\n" + parent.getAnnotation();
				}
				annotationDialog.setMessage(StringUtil.format(mapElement.getLocation()));
			}
			annotationDialog.setText(annot);
		}
	}
	
	@Override
	public String toString() {
		String str = "["+location+"]"+super.toString();
		return(str);
	}

}
