package gov.nasa.arc.dert.viewpoint;

import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.view.world.WorldInputHandler;

/**
 * Set the center of rotation for the viewpoint.
 *
 */
public class CoRAction extends MenuItemAction {

	protected WorldInputHandler inputHandler;

	/**
	 * Constructor
	 * 
	 * @param inputHandler
	 */
	public CoRAction(WorldInputHandler inputHandler) {
		super("Set Center Of Rotation");
		this.inputHandler = inputHandler;
	}

	@Override
	protected void run() {
		inputHandler.setCenterOfRotation();
	}

}
