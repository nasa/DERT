package gov.nasa.arc.dert.view.world;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.render.BasicScene;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.state.WorldState;
import gov.nasa.arc.dert.viewpoint.AnaglyphCamera;
import gov.nasa.arc.dert.viewpoint.BasicCamera;
import gov.nasa.arc.dert.viewpoint.ViewDependent;
import gov.nasa.arc.dert.viewpoint.ViewpointNode;

import java.util.ArrayList;

import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyEventListener;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.event.SceneGraphManager;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.geom.Debugger;

/**
 * The Ardor3D Scene for the WorldView.
 *
 */
public class WorldScene extends BasicScene implements DirtyEventListener {

	// List of objects that must be updated when the camera position changes
	private ArrayList<ViewDependent> viewDependentList;

	// Background color
	private ReadOnlyColorRGBA backgroundColor = ColorRGBA.LIGHT_GRAY;

	// Viewpoint
	private ViewpointNode viewpointNode;
	
	// Viewpoint crosshair and text
	private RGBAxes crosshair;
	private Node text;

	// Flags
	private boolean showCrosshair = true;
	private boolean showNormals = false;
	private boolean showTextOverlay = true;

	// Countdown used after new scene is created to ensure the quadtrees for the
	// viewpoint are loaded.
	// TODO: Come up with a better way to do this.
	private int initializingCount;

	/**
	 * Constructor
	 */
	public WorldScene() {
		SceneGraphManager.getSceneGraphManager().addDirtyEventListener(this);
	}

	/**
	 * Initialize this Scene
	 */
	@Override
	public void init(CanvasRenderer canvasRenderer) {
		canvasRenderer.getRenderer().setBackgroundColor(backgroundColor);
	}

	/**
	 * Set the map element state
	 * 
	 * @param wState
	 */
	public void setState(WorldState wState) {
		
		World world = World.getInstance();
		setRootNode(world);
		world.initialize();
		
		viewpointNode = new ViewpointNode(world.getName() + "_viewpoint", null);
		viewDependentList = new ArrayList<ViewDependent>();
		world.attachChild(viewpointNode);
		crosshair = viewpointNode.getCrosshair();
		world.attachChild(crosshair);
		text = viewpointNode.getText();
		initializingCount = Landscape.getInstance().getBaseMapLevel() * 2;
		updateBounds();
		spatialDirty(null, DirtyType.Attached); // add the tiles to the
												// viewdependent list
	}

	/**
	 * Get the viewpoint
	 * 
	 * @return
	 */
	public ViewpointNode getViewpointNode() {
		return (viewpointNode);
	}

	/**
	 * Update the bounds
	 */
	public void updateBounds() {
		viewpointNode.setSceneBounds();
	}

	/**
	 * Update method called by framework
	 */
	@Override
	public void update(ReadOnlyTimer timer) {
		// When first starting up, set the viewpoint changed field until we have
		// all of the correct
		// quadtrees loaded for the viewpoint.
		if (initializingCount > 0) {
			viewpointNode.changed.set(true);
			initializingCount--;
		}
		if (viewpointNode.changed.get()) {
			for (int i = 0; i < viewDependentList.size(); ++i) {
				viewDependentList.get(i).update(viewpointNode.getCamera());
			}
		}
	}

	@Override
	public void preRender(Renderer renderer) {
		World world = World.getInstance();
		world.getLighting().prerender(viewpointNode.getCamera(), renderer,
			world.getDirtyEventHandler().getChanged() || viewpointNode.changed.getAndSet(false));
		if (world.getDirtyEventHandler().getChanged()) {
			Landscape.getInstance().getLayerManager().renderLayers(renderer);
		}

		viewpointNode.updateGeometricState(0);
		viewpointNode.getCamera().update();
		ReadOnlyColorRGBA bgCol = world.getBackgroundColor();
		if (bgCol != backgroundColor) {
			renderer.setBackgroundColor(bgCol);
			backgroundColor = bgCol;
		}
	}

	@Override
	public void postRender(Renderer renderer) {
		World world = World.getInstance();
		world.getLighting().postrender(viewpointNode.getCamera(), renderer,
			world.getDirtyEventHandler().getChanged());
		world.getDirtyEventHandler().setChanged(false);
		drawNormals(renderer);
		if (showCrosshair) {
			crosshair.getSceneHints().setCullHint(CullHint.Never);
			crosshair.onDraw(renderer);
			crosshair.getSceneHints().setCullHint(CullHint.Always);
		}
		if (showTextOverlay) {
			renderer.setOrtho();
			text.getSceneHints().setCullHint(CullHint.Never);
			text.onDraw(renderer);
			text.getSceneHints().setCullHint(CullHint.Always);
			renderer.unsetOrtho();
		}
	}

	@Override
	public boolean needsRender(Renderer renderer) {
		// System.err.println("WorldScene.needsRender "+viewpointNode.changed.get());
		// boolean doDraw = viewpointNode.changed.getAndSet(false);
		boolean doDraw = viewpointNode.changed.get();
		doDraw |= super.needsRender(renderer);
		return (doDraw);
	}

	@Override
	public void render(Renderer renderer) {
		// renderer.clearBuffers(Renderer.BUFFER_COLOR_AND_DEPTH);
		if (viewpointNode.getCamera() instanceof AnaglyphCamera) {
			AnaglyphCamera camera = (AnaglyphCamera) viewpointNode.getCamera();
			camera.setupLeftRightCameras();
			camera.updateLeftRightCameraFrames();

			camera.switchToLeftCamera(renderer);
			renderer.draw(rootNode);
			renderer.renderBuckets();

			renderer.clearBuffers(Renderer.BUFFER_DEPTH);
			camera.switchToRightCamera(renderer);
			renderer.draw(rootNode);
			renderer.renderBuckets();

			camera.finish();
		} else {
			renderer.clearBuffers(Renderer.BUFFER_COLOR_AND_DEPTH);
			renderer.draw(rootNode);
		}
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
			addViewDependents(spatial);
			updateBounds();
			break;
		case Detached:
			removeViewDependents(spatial);
			updateBounds();
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

	private void addViewDependents(Spatial spatial) {
		if (spatial instanceof ViewDependent) {
			viewDependentList.add((ViewDependent) spatial);
			((ViewDependent) spatial).update(viewpointNode.getCamera());
		}
		if (spatial instanceof Node) {
			Node node = (Node) spatial;
			for (int i = 0; i < node.getNumberOfChildren(); ++i) {
				addViewDependents(node.getChild(i));
			}
		}
	}

	private void removeViewDependents(Spatial spatial) {
		if (spatial instanceof ViewDependent) {
			viewDependentList.remove(spatial);
		}
		if (spatial instanceof Node) {
			Node node = (Node) spatial;
			for (int i = 0; i < node.getNumberOfChildren(); ++i) {
				removeViewDependents(node.getChild(i));
			}
		}
	}

	@Override
	public BasicCamera getCamera() {
		return ((BasicCamera) viewpointNode.getCamera());
	}

	/**
	 * Set cross hair visibility
	 * 
	 * @param show
	 */
	public void setShowCrosshair(boolean show) {
		showCrosshair = show;
		needsRender.set(true);
	}

	/**
	 * Get cross hair visibility
	 * 
	 * @return
	 */
	public boolean getShowCrosshair() {
		return (showCrosshair);
	}

	/**
	 * Set cross hair visibility
	 * 
	 * @param show
	 */
	public void setShowTextOverlay(boolean show) {
		showTextOverlay = show;
		needsRender.set(true);
	}

	/**
	 * Get cross hair visibility
	 * 
	 * @return
	 */
	public boolean getShowTextOverlay() {
		return (showTextOverlay);
	}

	/**
	 * Draw surface normals
	 * 
	 * @param r
	 */
	private void drawNormals(Renderer r) {

		if (showNormals) {
			Debugger.drawNormals(rootNode, r);
		}
	}

	/**
	 * Show surface normals
	 * 
	 * @param enable
	 */
	public void enableNormals(boolean enable) {
		showNormals = enable;
		spatialDirty(rootNode, DirtyType.RenderState);
	}

	/**
	 * Find out if surface normals are visible
	 * 
	 * @return
	 */
	public boolean isNormalsEnabled() {
		return (showNormals);
	}

}