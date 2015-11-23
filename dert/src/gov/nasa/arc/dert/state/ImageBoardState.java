package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.landmark.ImageBoard;

import java.awt.Color;

import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides a state object for an ImageBoard.
 *
 */
public class ImageBoardState extends LandmarkState {

	// The path to the image to be displayed
	public String imagePath;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public ImageBoardState(ReadOnlyVector3 position) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration()
			.incrementMapElementCount(MapElementState.Type.Billboard), MapElementState.Type.Billboard, "Billboard",
			ImageBoard.defaultSize, Color.white, ImageBoard.defaultLabelVisible, ImageBoard.defaultPinned, position);
		imagePath = ImageBoard.defaultImagePath;
	}

	@Override
	public void save() {
		super.save();
		imagePath = ((ImageBoard) mapElement).getImagePath();
	}
}
