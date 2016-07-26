package gov.nasa.arc.dert.view.world;

import gov.nasa.arc.dert.render.SceneCanvasPanel;
import gov.nasa.arc.dert.state.State;
import gov.nasa.arc.dert.state.WorldState;
import gov.nasa.arc.dert.view.InputManager;
import gov.nasa.arc.dert.viewpoint.AnaglyphCamera;
import gov.nasa.arc.dert.viewpoint.BasicCamera;
import gov.nasa.arc.dert.viewpoint.ViewpointController;
import gov.nasa.arc.dert.viewpoint.ViewpointNode;

import java.awt.Dimension;

import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.util.ReadOnlyTimer;

/**
 * Provides the SceneCanvasPanel (drawing surface) for the WorldView.
 *
 */
public class WorldScenePanel extends SceneCanvasPanel {

	// The Ardor3D scene
	private WorldScene worldScene;

	// Controls the viewpoint
	private ViewpointController controller;

	// Handles user input
	private WorldInputHandler inputHandler;
	private InputManager inputManager;

	/**
	 * Constructor
	 * 
	 * @param width
	 * @param height
	 */
	public WorldScenePanel(int width, int height) {
		super(width, height, new WorldScene(), true, false);
		worldScene = (WorldScene) scene;
		controller = new ViewpointController();
		inputHandler = new WorldInputHandler(controller, this);
	}

	private void addInputManager() {
		if (inputManager != null) {
			return;
		}
		inputManager = new InputManager(canvas, inputHandler);
	}

	@Override
	public void initialize() {
		super.initialize();
	}

	@Override
	public void setState(State state) {
		super.setState(state);
		WorldState wState = (WorldState) state;
		worldScene.setState(wState);
		canvasRenderer.setCamera(worldScene.getCamera());
		addInputManager();
		controller.setViewpointNode(worldScene.getViewpointNode());
		Dimension size = canvas.getSize();
		if (inputManager != null) {
			inputManager.resize(size.width, size.height);
		}
		controller.resize(size.width, size.height);
		worldScene.resize(size.width, size.height);
		worldScene.spatialDirty(null, DirtyType.RenderState);
		if (wState.currentViewpoint != null) {
			worldScene.getViewpointNode().setViewpoint(wState.currentViewpoint, true, false);
		} else {
			worldScene.getViewpointNode().reset();
		}
	}

	@Override
	public void update(ReadOnlyTimer timer) {
		if (controller.getViewpointNode() != null) {
			super.update(timer);
			controller.update();
		}
	}

	/**
	 * Get the viewpoint controller
	 * 
	 * @return
	 */
	public ViewpointController getViewpointController() {
		return (controller);
	}

	/**
	 * Get the input handler
	 * 
	 * @return
	 */
	public WorldInputHandler getInputHandler() {
		return (inputHandler);
	}

	/**
	 * Set stereo mode.
	 * 
	 * @param stereo
	 * @param focalDistance
	 * @param eyeSeparation
	 */
	public void setStereo(boolean stereo, double focalDistance, double eyeSeparation) {
		ViewpointNode viewpointNode = worldScene.getViewpointNode();
		BasicCamera bc = viewpointNode.getCamera();
		if (stereo) {
			if (!(bc instanceof AnaglyphCamera)) {
				bc = new AnaglyphCamera(bc);
				viewpointNode.setCamera(bc);
				canvasRenderer.setCamera(bc);
			}
			((AnaglyphCamera) bc).setFocalDistance(focalDistance);
			((AnaglyphCamera) bc).setEyeSeparation(eyeSeparation);
		} else {
			if (bc instanceof AnaglyphCamera) {
				bc = new BasicCamera(bc);
				viewpointNode.setCamera(bc);
				canvasRenderer.setCamera(bc);
			}
		}
		worldScene.needsRender.set(true);
	}

	/**
	 * Get stereo mode
	 * 
	 * @return
	 */
	public boolean isStereo() {
		return (worldScene.getViewpointNode().getCamera() instanceof AnaglyphCamera);
	}

	/**
	 * Set cross hair visibility
	 * 
	 * @param show
	 */
	public void setShowCrosshair(boolean show) {
		worldScene.setShowCrosshair(show);
	}

	/**
	 * Get cross hair visibility
	 * 
	 * @return
	 */
	public boolean isShowCrosshair() {
		return (worldScene.getShowCrosshair());
	}

	/**
	 * Set text overlay visibility
	 * 
	 * @param show
	 */
	public void setShowTextOverlay(boolean show) {
		worldScene.setShowTextOverlay(show);
	}

	/**
	 * Get text overlay visibility
	 * 
	 * @return
	 */
	public boolean isShowTextOverlay() {
		return (worldScene.getShowTextOverlay());
	}

	/**
	 * Set center scale visibility
	 * 
	 * @param show
	 */
	public void setShowCenterScale(boolean show) {
		worldScene.setShowCenterScale(show);
	}

	/**
	 * Get center scale visibility
	 * 
	 * @return
	 */
	public boolean isShowCenterScale() {
		return (worldScene.getShowCenterScale());
	}

}
