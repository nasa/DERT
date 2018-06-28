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
import gov.nasa.arc.dert.scenegraph.GroupNode;
import gov.nasa.arc.dert.util.ImageUtil;
import gov.nasa.arc.dert.util.SpatialUtil;

import java.util.concurrent.atomic.AtomicBoolean;

import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.framework.Scene;
import com.ardor3d.image.Image;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.hint.NormalsMode;
import com.ardor3d.util.ReadOnlyTimer;
import com.jogamp.opengl.GL;

/**
 * Provides an abstract base class for 3D graphics scenes by implementing
 * Ardor3D Scene interface.
 *
 */
public abstract class BasicScene implements Scene {

	// Path to image presented at startup
	public static String imagePath;
	public static volatile Image dertImage;

	public final AtomicBoolean sceneChanged;

	protected GroupNode rootNode;
	protected int width, height;

	/**
	 * Constructor
	 */
	public BasicScene() {
		// load the default image
		if (dertImage == null) {
			dertImage = ImageUtil.loadImage(imagePath, true);
		}
		sceneChanged = new AtomicBoolean(true);
	}

	/**
	 * Initialize this scene
	 * 
	 * @param canvasRenderer
	 */
	public abstract void init(CanvasRenderer canvasRenderer);

	/**
	 * Set the root node of the scene graph
	 * 
	 * @param root
	 */
	public void setRootNode(GroupNode root) {
		rootNode = root;

		// Initialize root node
		ZBufferState buf = (ZBufferState) rootNode.getLocalRenderState(RenderState.StateType.ZBuffer);
		if (buf == null) {
			buf = new ZBufferState();
			buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
		}
		buf.setEnabled(true);
		rootNode.setRenderState(buf);

		BlendState as = (BlendState) rootNode.getLocalRenderState(RenderState.StateType.Blend);
		if (as == null) {
			as = new BlendState();
		}
		as.setBlendEnabled(true);
		rootNode.setRenderState(as);

		rootNode.getSceneHints().setRenderBucketType(RenderBucketType.Opaque);
		rootNode.getSceneHints().setNormalsMode(NormalsMode.NormalizeIfScaled);

		rootNode.updateGeometricState(0, true);
	}

	/**
	 * Perform a rendering event
	 */
	@Override
	public boolean renderUnto(final Renderer renderer) {

		// the scene graph is empty, show the default image
		if (rootNode == null) {
			renderer.clearBuffers(Renderer.BUFFER_COLOR_AND_DEPTH);
			int x = (int)(width - dertImage.getWidth()) / 2;
			int y = (int)(height - dertImage.getHeight()) / 2;
			switch (dertImage.getDataFormat()) {
			case BGR:
				((JoglRendererDouble) renderer).drawImage(x, y, dertImage.getWidth(), dertImage.getHeight(), 1.0f,
						GL.GL_BGR, GL.GL_UNSIGNED_BYTE, dertImage.getData(0));
				break;
			case BGRA:
				((JoglRendererDouble) renderer).drawImage(x, y, dertImage.getWidth(), dertImage.getHeight(), 1.0f,
						GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, dertImage.getData(0));
				break;
			case RGB:
				((JoglRendererDouble) renderer).drawImage(x, y, dertImage.getWidth(), dertImage.getHeight(), 1.0f,
						GL.GL_RGB, GL.GL_UNSIGNED_BYTE, dertImage.getData(0));
				break;
			case RGBA:
				((JoglRendererDouble) renderer).drawImage(x, y, dertImage.getWidth(), dertImage.getHeight(), 1.0f,
						GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, dertImage.getData(0));
			case Luminance:
				((JoglRendererDouble) renderer).drawImage(x, y, dertImage.getWidth(), dertImage.getHeight(), 1.0f,
						GL.GL_LUMINANCE, GL.GL_UNSIGNED_BYTE, dertImage.getData(0));
				break;
			case LuminanceAlpha:
				((JoglRendererDouble) renderer).drawImage(x, y, dertImage.getWidth(), dertImage.getHeight(), 1.0f,
						GL.GL_LUMINANCE_ALPHA, GL.GL_UNSIGNED_BYTE, dertImage.getData(0));
				break;
			default:
				break;
			}
		}
		else {
			render(renderer);
		}

		return (true);
	}

	/**
	 * Render the scene graph
	 * 
	 * @param renderer
	 */
	public abstract void render(Renderer renderer);

	/**
	 * Perform a pick in the scene
	 */
	@Override
	public PickResults doPick(final Ray3 pickRay) {
		return (SpatialUtil.doPick(rootNode, pickRay));
	}

	/**
	 * Get the root node of the scene graph
	 * 
	 * @return
	 */
	public final GroupNode getRootNode() {
		return (rootNode);
	}

	/**
	 * Update the objects in the scene graph
	 * 
	 * @param timer
	 */
	public void update(ReadOnlyTimer timer) {
	}

	/**
	 * Get the camera associated with this scene
	 * 
	 * @return
	 */
	public abstract BasicCamera getCamera();

	/**
	 * Resize this scene
	 * 
	 * @param width
	 * @param height
	 */
	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		sceneChanged.set(true);
	}
	
	public int getWidth() {
		return(width);
	}
	
	public int getHeight() {
		return(height);
	}
	
	public boolean needsRender() {
		return(sceneChanged.getAndSet(false));
	}
	
	public void preRender(Renderer renderer) {
		// do nothing
	}

}