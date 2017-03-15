package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.scene.landmark.ImageBoard;
import gov.nasa.arc.dert.ui.OptionDialog;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.Console;
import gov.nasa.arc.dert.view.View;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
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
	public ImageBoardState(ReadOnlyVector3 position, String imagePath) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration()
			.incrementMapElementCount(MapElementState.Type.Billboard), MapElementState.Type.Billboard, "Billboard",
			ImageBoard.defaultSize, Color.white, ImageBoard.defaultLabelVisible, position);
		this.imagePath = imagePath;
		viewData = new ViewData(-1, -1, 0, 0, false);
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
	
	@Override
	public View open(boolean doIt) {
		if (viewData == null)
			return(null);
		
		if (doIt)
			viewData.setVisible(true);
		
		// The view is not visible
		if (!viewData.isVisible())
			return(null);
		
		// This state element has a view
		if (viewData.viewWindow != null) {
			viewData.viewWindow.setVisible(true);
			return (viewData.view);
		}
		
		if (mapElement == null)
			return(null);
		try {
			viewData.setVisible(true);
			ImageBoard imageBoard = (ImageBoard)mapElement;
			Desktop.getDesktop().open(new File(imageBoard.getImagePath()));
		} catch (Exception e) {
			e.printStackTrace();
			Console.println("Unable to open image, see log.");
			OptionDialog.showErrorMessageDialog(Dert.getMainWindow(), "Unable to open image.");
		}
		return(null);
	}
}
