package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.landmark.Landmark;

import java.awt.Color;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Node;

/**
 * Base class for state objects for Landmarks.
 *
 */
public class LandmarkState extends MapElementState {

	// Location of landmark
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
	public LandmarkState(long id, MapElementState.Type mapElementType, String prefix, double size, Color color,
		boolean labelVisible, boolean pinned, ReadOnlyVector3 position) {
		super(id, mapElementType, prefix, size, color, labelVisible, pinned);
		this.position = new Vector3(position);
	}

	@Override
	public Landmark getMapElement() {
		return ((Landmark) mapElement);
	}

	@Override
	public void save() {
		super.save();
		if (mapElement != null) {
			Landmark landmark = (Landmark) mapElement;
			Node node = (Node) landmark;
			position = new Vector3(node.getTranslation());
		}
	}
}
