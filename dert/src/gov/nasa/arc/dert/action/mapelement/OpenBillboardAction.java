package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.scene.landmark.ImageBoard;
import gov.nasa.arc.dert.view.Console;

import java.awt.Desktop;
import java.io.File;

/**
 * Context menu item for opening a billboard image in the default platform image
 * viewer.
 *
 */
public class OpenBillboardAction extends MenuItemAction {

	protected ImageBoard imageBoard;

	/**
	 * Constructor
	 * 
	 * @param imageBoard
	 */
	public OpenBillboardAction(ImageBoard imageBoard) {
		super("Open " + imageBoard.getName());
		this.imageBoard = imageBoard;
	}

	@Override
	protected void run() {
		try {
			Desktop.getDesktop().open(new File(imageBoard.getImagePath()));
		} catch (Exception e) {
			e.printStackTrace();
			Console.getInstance().println("Unable to open image, see log.");
		}
	}

}
