package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.landmark.ImageBoard;
import gov.nasa.arc.dert.util.StateUtil;

import java.awt.Color;
import java.util.HashMap;

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
			ImageBoard.defaultSize, Color.white, ImageBoard.defaultLabelVisible, position);
		imagePath = ImageBoard.defaultImagePath;
	}
	
	/**
	 * Constructor for hash map.
	 */
	public ImageBoardState(HashMap<String,Object> map) {
		super(map);
		imagePath = StateUtil.getString(map, "ImagePath", ImageBoard.defaultImagePath);
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof ImageBoardState)) 
			return(false);
		ImageBoardState that = (ImageBoardState)state;
		if (!super.isEqualTo(that)) 
			return(false);
		if (!this.imagePath.equals(that.imagePath)) 
			return(false);
		return(true);
	}

	@Override
	public HashMap<String,Object> save() {
		HashMap<String,Object> map = super.save();
		if (mapElement != null)
			imagePath = ((ImageBoard) mapElement).getImagePath();
		map.put("ImagePath", imagePath);
		return(map);
	}
	
	@Override
	public String toString() {
		String str = imagePath+" "+super.toString();
		return(str);
	}
}
