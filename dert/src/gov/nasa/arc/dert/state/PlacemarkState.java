package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.landmark.Placemark;

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
		super(ConfigurationManager.getInstance().getCurrentConfiguration()
			.incrementMapElementCount(MapElementState.Type.Placemark), MapElementState.Type.Placemark, "Placemark",
			Placemark.defaultSize, Placemark.defaultColor, Placemark.defaultLabelVisible, Placemark.defaultPinned,
			position);
		textureIndex = Placemark.defaultTextureIndex;
	}

	@Override
	public void save() {
		super.save();
		if (mapElement != null) {
			Placemark placemark = (Placemark) mapElement;
			textureIndex = placemark.getTextureIndex();
		}
	}
}
