package gov.nasa.arc.dert.viewpoint;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.ButtonAction;
import gov.nasa.arc.dert.icon.Icons;

/**
 * Activates magnification mode.
 *
 */
public class ActivateZoomAction extends ButtonAction {

	protected ViewpointController controller;

	/**
	 * Constructor
	 */
	public ActivateZoomAction() {
		super("activate zoom mode", null, "magnify.png", false);
	}

	@Override
	protected void run() {
		checked = !checked;
		enableZoom(checked);
	}
	
	public void enableZoom(boolean enable) {
		Dert.getWorldView().getScenePanel().getViewpointController().enableZoom(enable);
		if (enable) {
			setIcon(Icons.getImageIcon("magnifycheck.png"));
		} else {
			setIcon(Icons.getImageIcon("magnify.png"));
		}		
	}

}
