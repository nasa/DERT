package gov.nasa.arc.dert.scene.tool;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scenegraph.BillboardMarker;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.state.WaypointState;
import gov.nasa.arc.dert.util.ImageUtil;

import javax.swing.Icon;

import com.ardor3d.image.Texture;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Node;

/**
 * Provides a map element that serves as a waypoint in a path
 *
 */
public class Waypoint extends BillboardMarker implements MapElement {

	public static final Icon icon = Icons.getImageIcon("waypoint_24.png");
	public static String defaultIconName = "waypoint.png";

	// Waypoint texture
	protected static Texture texture;

	// Map element state
	protected WaypointState state;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public Waypoint(WaypointState state) {
		super(state.name, state.position, state.size, state.color, state.labelVisible, state.pinned);
		if (texture == null) {
			texture = ImageUtil.createTexture(Icons.getIconURL(defaultIconName), true);
		}
		setTexture(texture, texture);
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
	 * Get the map element type
	 */
	@Override
	public Type getType() {
		return (Type.Waypoint);
	}

	/**
	 * Get the name of the parent path
	 * 
	 * @return
	 */
	public String getPathName() {
		String str = getName();
		int indx = str.lastIndexOf(".");
		str = str.substring(0, indx);
		return (str);
	}

	/**
	 * Get the parent path
	 * 
	 * @return
	 */
	public Path getPath() {
		Node parent = getParent();
		if (parent == null) {
			return (null);
		}
		return ((Path) parent.getParent());
	}

}
