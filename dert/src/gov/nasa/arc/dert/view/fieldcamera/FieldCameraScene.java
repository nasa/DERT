package gov.nasa.arc.dert.view.fieldcamera;

import gov.nasa.arc.dert.render.SyntheticCameraScene;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.state.FieldCameraState;

import com.ardor3d.renderer.Renderer;

/**
 * An Ardor3D Scene for the FieldCameraView.
 *
 */
public class FieldCameraScene extends SyntheticCameraScene {

	// The FieldCamera map element
	private FieldCamera fieldCamera;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public FieldCameraScene(FieldCameraState state) {
		super(((FieldCamera)state.getMapElement()).getSyntheticCameraNode(), state.crosshairVisible);
		fieldCamera = (FieldCamera) state.getMapElement();
	}

	/**
	 * Get the FieldCamera map element
	 * 
	 * @return
	 */
	public FieldCamera getFieldCamera() {
		return (fieldCamera);
	}

	@Override
	public void render(Renderer renderer) {
		fieldCamera.cull();
		
		super.render(renderer);
		
		fieldCamera.uncull();
	}

}