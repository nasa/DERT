package gov.nasa.arc.dert.view.world;

import gov.nasa.arc.dert.action.edit.CoordAction;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.lighting.Lighting;
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
	private ColorRGBA backgroundColor = new ColorRGBA(Lighting.defaultBackgroundColor);

	// Viewpoint
	private ViewpointNode viewpointNode;
	
	// Viewpoint crosshair and text
	private RGBAxes crosshair;
	private Node textOverlay, centerScale;

	// Flags
	private boolean showCrosshair = true;
	private boolean showNormals = false;
	private boolean showTextOverlay = true;
	private boolean showCenterScale = false;
	
	private boolean worldChanged, terrainChanged;

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
		
		if (viewpointNode != null)
			CoordAction.listenerList.remove(viewpointNode);
		viewpointNode = new ViewpointNode(world.getName() + "_viewpoint", null);
		viewDependentList = new ArrayList<ViewDependent>();
		world.attachChild(viewpointNode);
		crosshair = viewpointNode.getCrosshair();
		world.attachChild(crosshair);
		textOverlay = viewpointNode.getTextOverlay();
		centerScale = viewpointNode.getCenterScale();
		CoordAction.listenerList.add(viewpointNode);
		viewpointNode.setSceneBounds();
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
	 * Update method called by framework
	 */
	@Override
	public void update(ReadOnlyTimer timer) {
		// update the landscape quad tree
		Landscape.getInstance().update(viewpointNode.getCamera());
		// has the viewpoint changed?
		boolean viewpointChanged = viewpointNode.changed.getAndSet(false);
		// if either the viewpoint or landscape changed, update the other view dependent objects
		if (viewpointChanged) {
			for (int i = 0; i < viewDependentList.size(); ++i)
				viewDependentList.get(i).update(viewpointNode.getCamera());
		}
		worldChanged = World.getInstance().getDirtyEventHandler().changed.get();
		terrainChanged = World.getInstance().getDirtyEventHandler().terrainChanged.get();
//		System.err.println("WorldScene.update "+viewpointChanged+" "+worldChanged+" "+terrainChanged+" "+Landscape.getInstance().quadTreeChanged+" "+initializingCount);
		sceneChanged.set(viewpointChanged || worldChanged || terrainChanged || sceneChanged.get());
	}

	@Override
	public void preRender(Renderer renderer) {
		if (rootNode == null)
			return;
		Lighting lighting = ((World)rootNode).getLighting();
		lighting.prerender(viewpointNode.getCamera(), renderer, worldChanged);
		if (terrainChanged) {
			Landscape.getInstance().getLayerManager().renderLayers(renderer);
		}

		viewpointNode.updateGeometricState(0);
		viewpointNode.getCamera().update();
		ReadOnlyColorRGBA bgCol = lighting.getBackgroundColor();
		if (!bgCol.equals(backgroundColor)) {
			renderer.setBackgroundColor(bgCol);
			backgroundColor.set(bgCol);
		}
	}

	private void postRender(Renderer renderer) {
		((World)rootNode).getLighting().postrender(viewpointNode.getCamera(), renderer, worldChanged);

		if (showNormals) {
			Debugger.drawNormals(rootNode, renderer);
		}
		if (showCrosshair) {
			crosshair.getSceneHints().setCullHint(CullHint.Never);
			crosshair.onDraw(renderer);
			crosshair.getSceneHints().setCullHint(CullHint.Always);
		}
		if (showTextOverlay) {
			renderer.setOrtho();
			textOverlay.getSceneHints().setCullHint(CullHint.Never);
			textOverlay.onDraw(renderer);
			textOverlay.getSceneHints().setCullHint(CullHint.Always);
			renderer.unsetOrtho();
		}
		if (showCenterScale) {
			renderer.setOrtho();
			centerScale.getSceneHints().setCullHint(CullHint.Never);
			centerScale.onDraw(renderer);
			centerScale.getSceneHints().setCullHint(CullHint.Always);
			renderer.unsetOrtho();
		}
	}

	@Override
	public void render(Renderer renderer) {
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
		postRender(renderer);
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
			viewpointNode.setSceneBounds();
			break;
		case Detached:
			removeViewDependents(spatial);
			viewpointNode.setSceneBounds();
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
		sceneChanged.set(true);
		return (false);
	}

	@Override
	public boolean spatialClean(Spatial spatial, DirtyType type) {
		return (false);
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		if (viewpointNode != null)
			viewpointNode.resize(width, height);
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
		sceneChanged.set(true);
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
	 * Set text overlay visibility
	 * 
	 * @param show
	 */
	public void setShowTextOverlay(boolean show) {
		showTextOverlay = show;
		sceneChanged.set(true);
	}

	/**
	 * Get text overlay visibility
	 * 
	 * @return
	 */
	public boolean getShowTextOverlay() {
		return (showTextOverlay);
	}

	/**
	 * Set center scale visibility
	 * 
	 * @param show
	 */
	public void setShowCenterScale(boolean show) {
		showCenterScale = show;
		sceneChanged.set(true);
	}

	/**
	 * Get center scale visibility
	 * 
	 * @return
	 */
	public boolean getShowCenterScale() {
		return (showCenterScale);
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