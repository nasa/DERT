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
	protected boolean enabled;

	/**
	 * Constructor
	 */
	public ActivateZoomAction() {
		super("activate zoom mode", null, "magnify.png", false);
	}

	@Override
	protected void run() {
		enabled = !enabled;
		Dert.getWorldView().getScenePanel().getViewpointController().enableZoom(enabled);
		if (enabled) {
			setIcon(Icons.getImageIcon("magnifycheck.png"));
		} else {
			setIcon(Icons.getImageIcon("magnify.png"));
		}

	}

}
