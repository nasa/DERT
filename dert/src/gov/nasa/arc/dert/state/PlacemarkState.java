package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.landmark.Placemark;
import gov.nasa.arc.dert.util.StateUtil;

import java.util.HashMap;

import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides a state object for a Placemark.
 *
 */
public class PlacemarkState extends LandmarkState {

	// Index of icon texture (flag or pushpin)
	public int textureIndex;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public PlacemarkState(ReadOnlyVector3 position) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration().incrementMapElementCount(MapElementState.Type.Placemark),
			MapElementState.Type.Placemark, "Placemark", Placemark.defaultSize, Placemark.defaultColor, Placemark.defaultLabelVisible, position);
		textureIndex = Placemark.defaultTextureIndex;
	}
	
	/**
	 * Constructor for hash map
	 */
	public PlacemarkState(HashMap<String,Object> map) {
		super(map);
		textureIndex = StateUtil.getInteger(map, "TextureIndex", Placemark.defaultTextureIndex);
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof PlacemarkState)) 
			return(false);
		PlacemarkState that = (PlacemarkState)state;
		if (!super.isEqualTo(that)) 
			return(false);
		if (this.textureIndex != that.textureIndex)
			return(false);
		return(true);
	}

	@Override
	public HashMap<String,Object> save() {
		HashMap<String,Object> map = super.save();
		if (mapElement != null) {
			Placemark placemark = (Placemark) mapElement;
			textureIndex = placemark.getTextureIndex();
		}
		map.put("TextureIndex", new Integer(textureIndex));
		return(map);
	}
	
	@Override
	public String toString() {
		return("TextureIndex="+textureIndex+" "+super.toString());
	}
}
