package gov.nasa.arc.dert.scene.tool;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scenegraph.FigureMarker;
import gov.nasa.arc.dert.scenegraph.LineSegment;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.state.WaypointState;

import javax.swing.Icon;

import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.CullHint;

/**
 * Provides a map element that serves as a waypoint in a path
 *
 */
public class Waypoint extends FigureMarker implements MapElement {

	public static final Icon icon = Icons.getImageIcon("waypoint_16.png");

	// Map element state
	protected WaypointState state;
	protected LineSegment line;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public Waypoint(WaypointState state) {
		super(state.name, state.location, state.size, state.zOff, state.color, state.labelVisible, true, state.locked);
		contents.detachChild(surfaceNormalArrow);
		surfaceNormalArrow = null;
		setShape(ShapeType.sphere);
		setVisible(state.visible);
		label.setTranslation(0, 2*size, 0);
		line = new LineSegment("_textLine", new Vector3(0,0,0), new Vector3(0, 1.8*size, 0));
		line.setColor(labelColorRGBA);
		billboard.attachChild(line);
		billboard.getSceneHints().setCullHint(state.labelVisible ? CullHint.Inherit : CullHint.Always);
		this.state = state;
		state.setMapElement(this);
		// Update this node and its children so they will be drawn.
		updateGeometricState(0);
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
//		return (getRadius() * 1.5);
		return (size*scale*2);
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

	/**
	 * Set label visibility
	 * 
	 * @param labelVisible
	 */
	@Override
	public void setLabelVisible(boolean labelVisible) {
		super.setLabelVisible(labelVisible);
		billboard.getSceneHints().setCullHint(labelVisible ? CullHint.Inherit : CullHint.Always);
	}
	
	@Override
	public void setSize(double size) {
		super.setSize(size);
		label.setTranslation(0, 2*size, 0);
		line.setPoints(Vector3.ZERO, new Vector3(0, 1.8*size, 0));
	}

	@Override
	public Icon getIcon() {
		return (icon);
	}
	
	@Override
	public boolean isLocked() {
		Path path = getPath();
		if (path == null)
			return(false);
		return (path.isLocked());
	}

}
