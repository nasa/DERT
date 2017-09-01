package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.landmark.Landmark;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.util.StateUtil;

import java.awt.Color;
import java.util.Map;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Base class for state objects for Landmarks.
 *
 */
public abstract class LandmarkState extends MapElementState {

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
	public LandmarkState(int id, MapElementState.Type mapElementType, String prefix, double size, Color color,
		boolean labelVisible, ReadOnlyVector3 position) {
		super(id, mapElementType, prefix, size, color, labelVisible);
		this.position = new Vector3(position);
	}
	
	/**
	 * Constructor for hash map.
	 */
	public LandmarkState(Map<String,Object> map) {
		super(map);
		position = StateUtil.getVector3(map, "Position", Vector3.ZERO);
	}
	
	@Override
	public boolean isEqualTo(State state) {
		LandmarkState that = (LandmarkState)state;
		if (!this.position.equals(that.position)) 
			return(false);
		return(true);
	}

	@Override
	public Landmark getMapElement() {
		return ((Landmark) mapElement);
	}

	@Override
	public Map<String,Object> save() {
		Map<String,Object> map = super.save();
		if (mapElement != null) {
			Movable mov = (Movable) mapElement;
			position = new Vector3(mov.getLocation());
		}
		StateUtil.putVector3(map, "Position", position);
		return(map);
	}
	
	@Override
	public String toString() {
		String str = position+" "+super.toString();
		return(str);
	}
}
