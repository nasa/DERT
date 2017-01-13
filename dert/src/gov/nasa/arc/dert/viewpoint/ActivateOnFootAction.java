package gov.nasa.arc.dert.viewpoint;

import java.awt.Toolkit;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.ButtonAction;
import gov.nasa.arc.dert.icon.Icons;

/**
 * Activates hike mode.
 *
 */
public class ActivateOnFootAction extends ButtonAction {

	protected ViewpointController controller;

	/**
	 * Constructor
	 */
	public ActivateOnFootAction() {
		super("activate hike mode", null, "viewpoint.png", false);
	}

	@Override
	protected void run() {
		checked = !checked;
		enableHike(checked);
	}
	
	public void enableHike(boolean enable) {
		ViewpointController controller = Dert.getWorldView().getScenePanel().getViewpointController();
		if (!controller.getViewpointNode().setHikeMode(enable)) {
			Toolkit.getDefaultToolkit().beep();
			enable = !enable;
		}
		else {
			controller.updateLookAt();
		}
		if (enable) {
			setIcon(Icons.getImageIcon("viewpointonfoot.png"));
		} else {
			setIcon(Icons.getImageIcon("viewpoint.png"));
		}		
	}

}
