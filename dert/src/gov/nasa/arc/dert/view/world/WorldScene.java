package gov.nasa.arc.dert.view.world;

import gov.nasa.arc.dert.action.edit.CoordAction;
import gov.nasa.arc.dert.camera.AnaglyphCamera;
import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.render.BasicScene;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.state.WorldState;
import gov.nasa.arc.dert.viewpoint.ViewDependent;
import gov.nasa.arc.dert.viewpoint.Viewpoint;

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
	private Viewpoint viewpoint;
	
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
		
		if (viewpoint != null)
			CoordAction.listenerList.remove(viewpoint);
		viewpoint = new Viewpoint(world.getName() + "_viewpoint", null);
		viewDependentList = new ArrayList<ViewDependent>();
		crosshair = viewpoint.getCrosshair();
//		world.attachChild(crosshair);
		textOverlay = viewpoint.getTextOverlay();
		centerScale = viewpoint.getCenterScale();
		CoordAction.listenerList.add(viewpoint);
		viewpoint.setSceneBounds();
		spatialDirty(null, DirtyType.Attached); // add the tiles to the
												// viewdependent list
	}

	/**
	 * Get the viewpoint
	 * 
	 * @return
	 */
	public Viewpoint getViewpoint() {
		return (viewpoint);
	}

	/**
	 * Update method called by framework
	 */
	@Override
	public void update(ReadOnlyTimer timer) {
		// update the landscape quad tree
		Landscape.getInstance().update(viewpoint.getCamera());
		// has the viewpoint changed?
		boolean viewpointChanged = viewpoint.changed.getAndSet(false);
		// if the viewpoint changed, update the other view dependent objects
		if (viewpointChanged) {
			for (int i = 0; i < viewDependentList.size(); ++i) {
				viewDependentList.get(i).update(viewpoint.getCamera());
			}
		}
		worldChanged = World.getInstance().getDirtyEventHandler().changed.get();
		terrainChanged = World.getInstance().getDirtyEventHandler().terrainChanged.get();
//		System.err.println("WorldScene.update "+viewpointChanged+" "+worldChanged+" "+terrainChanged+" "+sceneChanged.get());
		sceneChanged.set(viewpointChanged || worldChanged || terrainChanged || sceneChanged.get());
		if (sceneChanged.get())
			rootNode.updateGeometricState(0);
	}

	@Override
	public void preRender(Renderer renderer) {
		if (rootNode == null)
			return;
		Lighting lighting = ((World)rootNode).getLighting();
		lighting.prerender(viewpoint.getCamera(), renderer, worldChanged);
//		System.err.println("WorldScene.preRender "+terrainChanged+" "+worldChanged);
		if (terrainChanged || worldChanged) {
			Landscape.getInstance().getLayerManager().renderLayers(renderer);
		}

		viewpoint.getCamera().update();
		ReadOnlyColorRGBA bgCol = lighting.getBackgroundColor();
		if (!bgCol.equals(backgroundColor)) {
			renderer.setBackgroundColor(bgCol);
			backgroundColor.set(bgCol);
		}
	}

	private void postRender(Renderer renderer) {
		((World)rootNode).getLighting().postrender(viewpoint.getCamera(), renderer, worldChanged);

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
		if (viewpoint.getCamera() instanceof AnaglyphCamera) {
			AnaglyphCamera camera = (AnaglyphCamera) viewpoint.getCamera();
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
//		System.err.println("WorldScene.spatialDirty "+spatial+" "+type+" "+spatial.getParent());
		switch (type) {
		case Attached:
			addViewDependents(spatial);
			viewpoint.setSceneBounds();
			break;
		case Detached:
			removeViewDependents(spatial);
			viewpoint.setSceneBounds();
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
		if (viewpoint != null)
			viewpoint.resize(width, height);
	}

	private void addViewDependents(Spatial spatial) {
		if (spatial instanceof ViewDependent) {
			viewDependentList.add((ViewDependent) spatial);
			((ViewDependent) spatial).update(viewpoint.getCamera());
		}
		else if (spatial instanceof Node) {
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
		else if (spatial instanceof Node) {
			Node node = (Node) spatial;
			for (int i = 0; i < node.getNumberOfChildren(); ++i) {
				removeViewDependents(node.getChild(i));
			}
		}
	}

	@Override
	public BasicCamera getCamera() {
		return ((BasicCamera) viewpoint.getCamera());
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