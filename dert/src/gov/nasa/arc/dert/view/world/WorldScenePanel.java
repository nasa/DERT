package gov.nasa.arc.dert.view.world;

import gov.nasa.arc.dert.render.SceneCanvasPanel;
import gov.nasa.arc.dert.state.State;
import gov.nasa.arc.dert.state.WorldState;
import gov.nasa.arc.dert.viewpoint.AnaglyphCamera;
import gov.nasa.arc.dert.viewpoint.BasicCamera;
import gov.nasa.arc.dert.viewpoint.Viewpoint;
import gov.nasa.arc.dert.viewpoint.ViewpointController;

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

	/**
	 * Constructor
	 * 
	 * @param width
	 * @param height
	 */
	public WorldScenePanel(int width, int height) {
		super(width, height, new WorldScene(), true);
		worldScene = (WorldScene) scene;
		controller = new ViewpointController();
	}

	@Override
	public void initialize() {
		super.initialize();
	}

	@Override
	public void setState(State state) {
		controller = new ViewpointController();
		// Add mouse handling after selection of landscape to avoid NPEs.
		if (inputManager == null)
			inputManager = new WorldInputHandler(canvas, controller, this);
		super.setState(state);
		WorldState wState = (WorldState) state;
		worldScene.setState(wState);
		canvasRenderer.setCamera(worldScene.getCamera());
		controller.setViewpointNode(worldScene.getViewpoint());
		Dimension size = canvas.getSize();
		inputManager.setComponentSize(size.width, size.height);
		inputManager.setCanvasScale(canvasWidth/size.width, canvasHeight/size.height);
		worldScene.resize((int)canvasWidth, (int)canvasHeight);
		worldScene.spatialDirty(null, DirtyType.RenderState);
		if (wState.currentViewpoint != null) {
			worldScene.getViewpoint().set(wState.currentViewpoint, false);
		} else {
			worldScene.getViewpoint().reset();
		}
	}

	@Override
	public void update(ReadOnlyTimer timer) {
		if (controller.getViewpoint() != null) {
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
		return ((WorldInputHandler)inputManager);
	}

	/**
	 * Set stereo mode.
	 * 
	 * @param stereo
	 * @param focalDistance
	 * @param eyeSeparation
	 */
	public void setStereo(boolean stereo, double focalDistance, double eyeSeparation) {
		Viewpoint viewpoint = worldScene.getViewpoint();
		BasicCamera bc = viewpoint.getCamera();
		if (stereo) {
			if (!(bc instanceof AnaglyphCamera)) {
				bc = new AnaglyphCamera(bc);
				viewpoint.setCamera(bc);
				canvasRenderer.setCamera(bc);
			}
			((AnaglyphCamera) bc).setFocalDistance(focalDistance);
			((AnaglyphCamera) bc).setEyeSeparation(eyeSeparation);
		} else {
			if (bc instanceof AnaglyphCamera) {
				bc = new BasicCamera(bc);
				viewpoint.setCamera(bc);
				canvasRenderer.setCamera(bc);
			}
		}
		worldScene.sceneChanged.set(true);
	}

	/**
	 * Get stereo mode
	 * 
	 * @return
	 */
	public boolean isStereo() {
		return (worldScene.getViewpoint().getCamera() instanceof AnaglyphCamera);
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
