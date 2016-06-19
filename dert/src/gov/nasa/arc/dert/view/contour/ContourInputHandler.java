package gov.nasa.arc.dert.view.contour;

import gov.nasa.arc.dert.view.InputHandler;
import gov.nasa.arc.dert.viewpoint.BasicCamera;
import gov.nasa.arc.dert.viewpoint.ViewpointController;

import java.awt.Cursor;

import com.ardor3d.math.Vector3;

/**
 * InputHandler for ContourView
 *
 */
public class ContourInputHandler implements InputHandler {

	// Contour view camera
	private BasicCamera camera;

	// Contour drawing area
	private ContourScenePanel canvasPanel;

	// Helper
	private Vector3 workVec = new Vector3();

	// Mouse pressed flag
	private boolean mouseDown;

	/**
	 * Constructor
	 * 
	 * @param camera
	 * @param canvasPanel
	 */
	public ContourInputHandler(BasicCamera camera, ContourScenePanel canvasPanel) {
		this.camera = camera;
		this.canvasPanel = canvasPanel;
	}

	@Override
	public void mouseScroll(int delta) {
		camera.magnify(-ViewpointController.mouseScrollDirection * delta);
		canvasPanel.viewpointChanged();
	}

	@Override
	public void mousePress(int x, int y, int mouseButton, boolean isControlled, boolean shiftDown) {
		if (mouseButton == 1) {
			canvasPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			mouseDown = true;
		}
	}

	@Override
	public void mouseRelease(int x, int y, int mouseButton) {
		if (mouseButton == 1) {
			canvasPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			mouseDown = false;
		}
	}

	@Override
	public void mouseMove(int x, int y, int dx, int dy, int mouseButton, boolean isControlled, boolean shiftDown) {
		if (mouseDown) {
			double s = camera.getPixelSizeAt(camera.getLookAt(), true);
			workVec.set(-dx * s, -dy * s, 0);
			workVec.addLocal(camera.getLocation());
			camera.setLocation(workVec);
			workVec.setZ(camera.getLookAt().getZ());
			camera.setLookAt(workVec);
			canvasPanel.viewpointChanged();
		} else {
			canvasPanel.getCoords(x, y);
		}
	}

	@Override
	public void mouseClick(int x, int y, int mouseButton) {
		canvasPanel.getPickCoords(x, y);
	}

	@Override
	public void mouseDoubleClick(int x, int y, int mouseButton) {
	}

	@Override
	public void stepUp(boolean shiftDown) {
		double s = camera.getPixelSizeAt(camera.getLookAt(), true);
		workVec.set(0, -s, 0);
		workVec.addLocal(camera.getLocation());
		camera.setLocation(workVec);
		workVec.setZ(camera.getLookAt().getZ());
		camera.setLookAt(workVec);
		canvasPanel.viewpointChanged();
	}

	@Override
	public void stepDown(boolean shiftDown) {
		double s = camera.getPixelSizeAt(camera.getLookAt(), true);
		workVec.set(0, s, 0);
		workVec.addLocal(camera.getLocation());
		camera.setLocation(workVec);
		workVec.setZ(camera.getLookAt().getZ());
		camera.setLookAt(workVec);
		canvasPanel.viewpointChanged();
	}

	@Override
	public void stepRight(boolean shiftDown) {
		double s = camera.getPixelSizeAt(camera.getLookAt(), true);
		workVec.set(-s, 0, 0);
		workVec.addLocal(camera.getLocation());
		camera.setLocation(workVec);
		workVec.setZ(camera.getLookAt().getZ());
		camera.setLookAt(workVec);
		canvasPanel.viewpointChanged();
	}

	@Override
	public void stepLeft(boolean shiftDown) {
		double s = camera.getPixelSizeAt(camera.getLookAt(), true);
		workVec.set(s, 0, 0);
		workVec.addLocal(camera.getLocation());
		camera.setLocation(workVec);
		workVec.setZ(camera.getLookAt().getZ());
		camera.setLookAt(workVec);
		canvasPanel.viewpointChanged();
	}

}
