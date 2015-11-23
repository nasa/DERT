package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.tool.Tool;

import java.awt.Color;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Node;

/**
 * Base class for state objects for Tools.
 *
 */
public class ToolState extends MapElementState {

	// Location of tool
	public Vector3 position;

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
	 * @param position
	 */
	public ToolState(long id, MapElementState.Type mapElementType, String prefix, double size, Color color,
		boolean labelVisible, boolean pinned, ReadOnlyVector3 position) {
		super(id, mapElementType, prefix, size, color, labelVisible, pinned);
		if (position != null) {
			this.position = new Vector3(position);
		}
	}

	/**
	 * Get the Tool.
	 * 
	 * @return
	 */
	public Tool getTool() {
		return ((Tool) mapElement);
	}

	@Override
	public void save() {
		super.save();
		if (mapElement != null) {
			Tool tool = (Tool) mapElement;
			Node node = (Node) tool;
			position = new Vector3(node.getTranslation());
		}
	}

}
