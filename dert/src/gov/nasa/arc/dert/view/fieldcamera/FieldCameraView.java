package gov.nasa.arc.dert.view.fieldcamera;

import gov.nasa.arc.dert.state.FieldCameraState;
import gov.nasa.arc.dert.view.PanelView;

import java.awt.BorderLayout;

/**
 * Displays the world from a FieldCamera viewpoint.
 *
 */
public class FieldCameraView extends PanelView {

	private FieldCameraScenePanel panel;

	/**
	 * Constructor
	 * 
	 * @param viewState
	 */
	public FieldCameraView(FieldCameraState viewState) {
		super(viewState);
		panel = new FieldCameraScenePanel(viewState);
		add(panel, BorderLayout.CENTER);
	}

	/**
	 * Get cross hair visibility
	 * 
	 * @return
	 */
	public boolean isCrosshairVisible() {
		FieldCameraScene scene = (FieldCameraScene) panel.getScene();
		return (scene.isCrosshairVisible());
	}

	/**
	 * Close the view
	 */
	@Override
	public void close() {
		panel.dispose();
	}

}
