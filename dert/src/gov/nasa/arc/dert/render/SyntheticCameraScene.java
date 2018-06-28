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

package gov.nasa.arc.dert.render;

import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.fieldcamera.SyntheticCameraNode;
import gov.nasa.arc.dert.view.fieldcamera.SimpleCrosshair;

import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyEventListener;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.event.SceneGraphManager;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.util.ReadOnlyTimer;

/**
 * An Ardor3D Scene for the SyntheticCamera.
 *
 */
public class SyntheticCameraScene extends BasicScene implements DirtyEventListener {

	// The world background
	protected ReadOnlyColorRGBA backgroundColor = new ColorRGBA(Lighting.defaultBackgroundColor);

	// The FieldCamera map element
	protected SyntheticCameraNode syntheticCamera;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public SyntheticCameraScene(SyntheticCameraNode syntheticCamera, boolean crosshairVisible) {
		this.syntheticCamera = syntheticCamera;
		setRootNode(World.getInstance());
//		crosshair = new SimpleCrosshair(ColorRGBA.WHITE);
//		crosshair.setTranslation(0, 0, -1);
//		crosshair.getSceneHints().setCullHint(CullHint.Always);
//		crosshair.updateGeometricState(0);
//		setCrosshairVisible(crosshairVisible);
//		double s = syntheticCamera.getCamera().getPixelSizeAt(crosshair.getWorldTranslation(), true);
//		crosshair.setScale(s, s, s);
//		syntheticCamera.getGeometryNode().attachChild(crosshair);
		SceneGraphManager.getSceneGraphManager().addDirtyEventListener(this);
		syntheticCamera.setSceneBounds();
	}

	/**
	 * Initialize the Scene
	 */
	@Override
	public void init(CanvasRenderer canvasRenderer) {
		canvasRenderer.getRenderer().setBackgroundColor(backgroundColor);
	}

//	/**
//	 * Set cross hair visibility
//	 * 
//	 * @param visible
//	 */
//	public void setCrosshairVisible(boolean visible) {
//		crosshairVisible = visible;
//		sceneChanged.set(true);
//	}

	/**
	 * Get cross hair visibility
	 * 
	 * @return
	 */
//	public boolean isCrosshairVisible() {
//		return (crosshairVisible);
//	}
	
	@Override
	public void preRender(Renderer renderer) {
		ReadOnlyColorRGBA bgCol = World.getInstance().getLighting().getBackgroundColor();
		if (!bgCol.equals(backgroundColor)) {
			renderer.setBackgroundColor(bgCol);
			backgroundColor = bgCol;
		}
	}

	@Override
	public void render(Renderer renderer) {
		
		renderer.draw(rootNode);

		if (syntheticCamera.isCrosshairVisible()) {
			renderer.setOrtho();
			SimpleCrosshair crosshair = syntheticCamera.getCrosshair();
			crosshair.getSceneHints().setCullHint(CullHint.Never);
			crosshair.onDraw(renderer);
			crosshair.getSceneHints().setCullHint(CullHint.Always);
			renderer.unsetOrtho();
		}
		
	}

	/**
	 * The world changed
	 */
	@Override
	public boolean spatialDirty(Spatial spatial, DirtyType type) {
//		if (spatial == null) {
//			spatial = rootNode;
//		}
//		switch (type) {
//		case Attached:
//			break;
//		case Detached:
//			break;
//		case Bounding:
//			break;
//		case RenderState:
//			break;
//		case Transform:
//			break;
//		case Destroyed:
//			break;
//		}
		sceneChanged.set(true);
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
		
		double aspect = getCamera().getAspect();
		double canvasHeight = height;
		double canvasWidth = (int) (height * aspect);
		if (canvasWidth > width) {
			canvasWidth = width;
			canvasHeight = (width / aspect);
		}
		double canvasX = (width-canvasWidth)/2.0;
		double canvasY = (height-canvasHeight)/2.0;

		syntheticCamera.resize(width, height);
		BasicCamera cam = syntheticCamera.getCamera();
		cam.setViewPort(canvasX/width, (canvasX+canvasWidth)/width, canvasY/height, (canvasY+canvasHeight)/height);
//		double s = cam.getPixelSizeAt(crosshair.getWorldTranslation(), true);
//		crosshair.setScale(s, s, s);
		spatialDirty(null, DirtyType.RenderState);
	}

	/**
	 * Get the camera
	 */
	@Override
	public BasicCamera getCamera() {
		return (syntheticCamera.getCamera());
	}

	/**
	 * Update method called by framework
	 */
	@Override
	public void update(ReadOnlyTimer timer) {
		boolean worldChanged = World.getInstance().getDirtyEventHandler().changed.get();
		boolean terrainChanged = World.getInstance().getDirtyEventHandler().terrainChanged.get();
		sceneChanged.set(worldChanged || terrainChanged || sceneChanged.get());
	}

}