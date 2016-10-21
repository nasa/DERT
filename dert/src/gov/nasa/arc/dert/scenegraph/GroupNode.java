package gov.nasa.arc.dert.scenegraph;

import gov.nasa.arc.dert.scene.MapElement;

import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;

/**
 * An Ardor3D Node that implements a group.
 *
 */
public class GroupNode extends Node {

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public GroupNode(String name) {
		super(name);
	}

	/**
	 * Set the vertical exaggeration for this group
	 * 
	 * @param vertExag
	 * @param oldVertExag
	 * @param minZ
	 */
	public void setVerticalExaggeration(double vertExag, double oldVertExag, double minZ) {
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			Spatial child = getChild(i);
			if (child instanceof MapElement) {
				((MapElement) child).setVerticalExaggeration(vertExag, oldVertExag, minZ);
			} else if (child instanceof GroupNode) {
				((GroupNode) child).setVerticalExaggeration(vertExag, oldVertExag, minZ);
			}
		}
	}
	
	@Override
	public String toString() {
		return(getName());
	}
}
