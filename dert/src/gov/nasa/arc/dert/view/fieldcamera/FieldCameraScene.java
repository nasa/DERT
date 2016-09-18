package gov.nasa.arc.dert.view.fieldcamera;

import gov.nasa.arc.dert.render.BasicScene;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.state.FieldCameraState;
import gov.nasa.arc.dert.viewpoint.BasicCamera;

import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyEventListener;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.event.SceneGraphManager;
import com.ardor3d.scenegraph.hint.CullHint;

/**
 * An Ardor3D Scene for the FieldCameraView.
 *
 */
public class FieldCameraScene extends BasicScene implements DirtyEventListener {

	// The cross hair at the center of the view
	private SimpleCrosshair crosshair;
	private boolean crosshairVisible = true;

	// The world background
	private ReadOnlyColorRGBA backgroundColor;

	// The FieldCamera map element
	private FieldCamera fieldCamera;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public FieldCameraScene(FieldCameraState state) {
		setRootNode(World.getInstance());
		fieldCamera = (FieldCamera) state.getMapElement();
		crosshair = new SimpleCrosshair(ColorRGBA.WHITE);
		crosshair.setTranslation(0, 0, -1);
		crosshair.getSceneHints().setCullHint(CullHint.Always);
		crosshair.updateGeometricState(0);
		setCrosshairVisible(state.crosshairVisible);
		double s = fieldCamera.getCamera().getPixelSizeAt(crosshair.getWorldTranslation(), true);
		crosshair.setScale(s, s, s);
		fieldCamera.getGeometryNode().attachChild(crosshair);
		SceneGraphManager.getSceneGraphManager().addDirtyEventListener(this);
		fieldCamera.setSceneBounds();
	}

	/**
	 * Initialize the Scene
	 */
	@Override
	public void init(CanvasRenderer canvasRenderer) {
		canvasRenderer.getRenderer().setBackgroundColor(ColorRGBA.DARK_GRAY);
	}

	/**
	 * Set cross hair visibility
	 * 
	 * @param visible
	 */
	public void setCrosshairVisible(boolean visible) {
		crosshairVisible = visible;
		needsRender.set(true);
	}

	/**
	 * Get cross hair visibility
	 * 
	 * @return
	 */
	public boolean isCrosshairVisible() {
		return (crosshairVisible);
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
	public void preRender(Renderer renderer) {
		fieldCamera.cull();
		ReadOnlyColorRGBA bgCol = World.getInstance().getLighting().getBackgroundColor();
		if (!bgCol.equals(backgroundColor)) {
			renderer.setBackgroundColor(bgCol);
			backgroundColor = bgCol;
		}
	}

	@Override
	public void postRender(Renderer renderer) {
		fieldCamera.uncull();

		if (crosshairVisible) {
			renderer.setOrtho();
			crosshair.getSceneHints().setCullHint(CullHint.Never);
			crosshair.onDraw(renderer);
			crosshair.getSceneHints().setCullHint(CullHint.Always);
			renderer.unsetOrtho();
		}
	}

	@Override
	public void render(Renderer renderer) {
		renderer.clearBuffers(Renderer.BUFFER_COLOR_AND_DEPTH);
		renderer.draw(rootNode);
	}

	/**
	 * The world changed
	 */
	@Override
	public boolean spatialDirty(Spatial spatial, DirtyType type) {
		if (spatial == null) {
			spatial = rootNode;
		}
		switch (type) {
		case Attached:
			break;
		case Detached:
			break;
		case Bounding:
			break;
		case RenderState:
			break;
		case Transform:
			break;
		case Destroyed:
			break;
		}
		needsRender.set(true);
		return (false);
	}

	@Override
	public boolean spatialClean(Spatial spatial, DirtyType type) {
		return (false);
	}

	/**
	 * The View was resized
	 */
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		fieldCamera.resize(width, height);
		double s = fieldCamera.getCamera().getPixelSizeAt(crosshair.getWorldTranslation(), true);
		crosshair.setScale(s, s, s);
		spatialDirty(null, DirtyType.RenderState);
	}

	/**
	 * Get the camera
	 */
	@Override
	public BasicCamera getCamera() {
		return (fieldCamera.getCamera());
	}

}