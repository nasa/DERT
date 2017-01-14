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
	
	protected static ActivateOnFootAction INSTANCE;

	protected ViewpointController controller;
	
	public static ActivateOnFootAction getInstance() {
		if (INSTANCE == null)
			INSTANCE = new ActivateOnFootAction();
		return(INSTANCE);
	}

	/**
	 * Constructor
	 */
	protected ActivateOnFootAction() {
		super("activate hike mode", null, "viewpoint.png", false);
	}

	@Override
	protected void run() {
		enableHike(!checked);
	}
	
	public void enableHike(boolean enable) {
		if (enable == checked)
			return;
		ViewpointController controller = Dert.getWorldView().getScenePanel().getViewpointController();
		if (!controller.getViewpointNode().setHikeMode(enable)) {
			Toolkit.getDefaultToolkit().beep();
		}
		else {
			controller.updateLookAt();
			setHikeIcon(enable);
		}
	}
	
	public void setHikeIcon(boolean enable) {
		if (enable) {
			setIcon(Icons.getImageIcon("viewpointonfoot.png"));
		} else {
			setIcon(Icons.getImageIcon("viewpoint.png"));
		}		
		checked = enable;
	}

}
