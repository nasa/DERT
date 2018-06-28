/**

DERT is a viewer for digital terrain models created from data collected during NASA missions.

DERT is Released in under the NASA Open Source Agreement (NOSA) found in the “LICENSE” folder where you
downloaded DERT.

DERT includes 3rd Party software. The complete copyright notice listing for DERT is:

Copyright © 2015 United States Government as represented by the Administrator of the National Aeronautics and
Space Administration.  No copyright is claimed in the United States under Title 17, U.S.Code. All Other Rights
Reserved.

Desktop Exploration of Remote Terrain (DERT) could not have been written without the aid of a number of free,
open source libraries. These libraries and their notices are listed below. Find the complete third party license
listings in the separate “DERT Third Party Licenses” pdf document found where you downloaded DERT in the
LICENSE folder.
 
JogAmp Ardor3D Continuation
Copyright © 2008-2012 Ardor Labs, Inc.
 
JogAmp
Copyright 2010 JogAmp Community. All rights reserved.
 
JOGL Portions Sun Microsystems
Copyright © 2003-2009 Sun Microsystems, Inc. All Rights Reserved.
 
JOGL Portions Silicon Graphics
Copyright © 1991-2000 Silicon Graphics, Inc.
 
Light Weight Java Gaming Library Project (LWJGL)
Copyright © 2002-2004 LWJGL Project All rights reserved.
 
Tile Rendering Library - Brain Paul 
Copyright © 1997-2005 Brian Paul. All Rights Reserved.
 
OpenKODE, EGL, OpenGL , OpenGL ES1 & ES2
Copyright © 2007-2010 The Khronos Group Inc.
 
Cg
Copyright © 2002, NVIDIA Corporation
 
Typecast - David Schweinsberg 
Copyright © 1999-2003 The Apache Software Foundation. All rights reserved.
 
PNGJ - Herman J. Gonzalez and Shawn Hartsock
Copyright © 2004 The Apache Software Foundation. All rights reserved.
 
Apache Harmony - Open Source Java SE
Copyright © 2006, 2010 The Apache Software Foundation.
 
Guava
Copyright © 2010 The Guava Authors
 
GlueGen Portions
Copyright © 2010 JogAmp Community. All rights reserved.
 
GlueGen Portions - Sun Microsystems
Copyright © 2003-2005 Sun Microsystems, Inc. All Rights Reserved.
 
SPICE
Copyright © 2003, California Institute of Technology.
U.S. Government sponsorship acknowledged.
 
LibTIFF
Copyright © 1988-1997 Sam Leffler
Copyright © 1991-1997 Silicon Graphics, Inc.
 
PROJ.4
Copyright © 2000, Frank Warmerdam

LibJPEG - Independent JPEG Group
Copyright © 1991-2018, Thomas G. Lane, Guido Vollbeding
 

Disclaimers

No Warranty: THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY KIND,
EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
THAT THE SUBJECT SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY
WARRANTY THAT THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE. THIS AGREEMENT
DOES NOT, IN ANY MANNER, CONSTITUTE AN ENDORSEMENT BY GOVERNMENT AGENCY OR ANY
PRIOR RECIPIENT OF ANY RESULTS, RESULTING DESIGNS, HARDWARE, SOFTWARE PRODUCTS OR
ANY OTHER APPLICATIONS RESULTING FROM USE OF THE SUBJECT SOFTWARE.  FURTHER,
GOVERNMENT AGENCY DISCLAIMS ALL WARRANTIES AND LIABILITIES REGARDING THIRD-PARTY
SOFTWARE, IF PRESENT IN THE ORIGINAL SOFTWARE, AND DISTRIBUTES IT "AS IS."

Waiver and Indemnity:  RECIPIENT AGREES TO WAIVE ANY AND ALL CLAIMS AGAINST THE UNITED
STATES GOVERNMENT, ITS CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR
RECIPIENT.  IF RECIPIENT'S USE OF THE SUBJECT SOFTWARE RESULTS IN ANY LIABILITIES,
DEMANDS, DAMAGES, EXPENSES OR LOSSES ARISING FROM SUCH USE, INCLUDING ANY DAMAGES
FROM PRODUCTS BASED ON, OR RESULTING FROM, RECIPIENT'S USE OF THE SUBJECT SOFTWARE,
RECIPIENT SHALL INDEMNIFY AND HOLD HARMLESS THE UNITED STATES GOVERNMENT, ITS
CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR RECIPIENT, TO THE EXTENT
PERMITTED BY LAW.  RECIPIENT'S SOLE REMEDY FOR ANY SUCH MATTER SHALL BE THE IMMEDIATE,
UNILATERAL TERMINATION OF THIS AGREEMENT.

**/

package gov.nasa.arc.dert.view.world;

import gov.nasa.arc.dert.action.edit.CoordAction;
import gov.nasa.arc.dert.camera.AnaglyphCamera;
import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.render.BasicScene;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.state.WorldState;
import gov.nasa.arc.dert.viewpoint.Viewpoint;

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
		if (viewpointChanged)
			rootNode.update(viewpoint.getCamera());
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
			viewpoint.setSceneBounds();
			break;
		case Detached:
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