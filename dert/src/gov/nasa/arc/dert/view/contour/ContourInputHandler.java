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

package gov.nasa.arc.dert.view.contour;

import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.render.SceneCanvas;
import gov.nasa.arc.dert.view.InputManager;
import gov.nasa.arc.dert.viewpoint.ViewpointController;

import java.awt.Cursor;

import com.ardor3d.math.Vector3;

/**
 * InputHandler for ContourView
 *
 */
public class ContourInputHandler
	extends InputManager {

	// Contour view camera
	private BasicCamera camera;

	// Contour drawing area
	private ContourScenePanel canvasPanel;

	// Helper
	private Vector3 workVec = new Vector3();

	// Mouse pressed flag
	private boolean mouseDown;
	
	// Canvas scale
	private double xCanvasScale = 1, yCanvasScale = 1;

	/**
	 * Constructor
	 * 
	 * @param camera
	 * @param canvasPanel
	 */
	public ContourInputHandler(SceneCanvas canvas, BasicCamera camera, ContourScenePanel canvasPanel) {
		super(canvas);
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
			dx *= xCanvasScale;
			dy *= yCanvasScale;
			double s = camera.getPixelSizeAt(camera.getLookAt(), true);
			workVec.set(-dx * s, -dy * s, 0);
			workVec.addLocal(camera.getLocation());
			camera.setLocation(workVec);
			workVec.setZ(camera.getLookAt().getZ());
			camera.setLookAt(workVec);
			canvasPanel.viewpointChanged();
		} else {
			x *= xCanvasScale;
			y = height-y;
			y *= yCanvasScale;
			canvasPanel.getCoords(x, y);
		}
	}

	@Override
	public void mouseClick(int x, int y, int mouseButton) {
		canvasPanel.getPickCoords(x*xCanvasScale, (height-y)*yCanvasScale);
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
	
	public void setCanvasScale(double xScale, double yScale) {
		xCanvasScale = xScale;
		yCanvasScale = yScale;
	}

}
