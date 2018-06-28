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

package gov.nasa.arc.dert.camera;

import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.ColorMaskState;
import com.ardor3d.renderer.state.RenderState.StateType;

/**
 * Provides a stereo camera. Adapted from Ardor3D StereoCamera.
 *
 */
public class AnaglyphCamera extends BasicCamera {

	private final BasicCamera _leftCamera;
	private final BasicCamera _rightCamera;

	private double _focalDistance = 1;
	private double _eyeSeparation = _focalDistance / 30;

	private ColorMaskState redColorMask, cyanColorMask;

	/**
	 * Constructor
	 */
	public AnaglyphCamera() {
		this(100, 100);
	}

	/**
	 * Constructor
	 * 
	 * @param width
	 * @param height
	 */
	public AnaglyphCamera(final int width, final int height) {
		super(width, height);
		_leftCamera = new BasicCamera(width, height);
		_rightCamera = new BasicCamera(width, height);
		redColorMask = new ColorMaskState();
		redColorMask.setAll(true);
		redColorMask.setBlue(false);
		redColorMask.setGreen(false);
		cyanColorMask = new ColorMaskState();
		cyanColorMask.setAll(true);
		cyanColorMask.setRed(false);
	}

	/**
	 * Constructor
	 * 
	 * @param camera
	 */
	public AnaglyphCamera(final Camera camera) {
		super(camera);
		_leftCamera = new BasicCamera(camera);
		_rightCamera = new BasicCamera(camera);
		redColorMask = new ColorMaskState();
		redColorMask.setAll(true);
		redColorMask.setBlue(false);
		redColorMask.setGreen(false);
		cyanColorMask = new ColorMaskState();
		cyanColorMask.setAll(true);
		cyanColorMask.setRed(false);
	}

	@Override
	public void resize(final int width, final int height) {
		super.resize(width, height);
		_leftCamera.resize(width, height);
		_rightCamera.resize(width, height);
	}

	/**
	 * Set up the cameras
	 */
	public void setupLeftRightCameras() {
		// Set viewport:
		_leftCamera.setViewPort(0, 1, 0, 1);
		_rightCamera.setViewPort(0, 1, 0, 1);

		// Set frustum:
		final double aspectRatio = (getWidth() / (double) getHeight());
		final double halfView = getFrustumNear() * MathUtils.tan(_fovY * MathUtils.DEG_TO_RAD / 2);

		final double top = halfView;
		final double bottom = -halfView;
		final double horizontalShift = 0.5 * _eyeSeparation * getFrustumNear() / _focalDistance;

		// LEFT:
		{
			final double left = -aspectRatio * halfView + horizontalShift;
			final double right = aspectRatio * halfView + horizontalShift;

			_leftCamera.setFrustum(getFrustumNear(), getFrustumFar(), left, right, top, bottom);
		}

		// RIGHT:
		{
			final double left = -aspectRatio * halfView - horizontalShift;
			final double right = aspectRatio * halfView - horizontalShift;

			_rightCamera.setFrustum(getFrustumNear(), getFrustumFar(), left, right, top, bottom);
		}
	}

	/**
	 * Update the camera frames when the viewpoint moves
	 */
	public void updateLeftRightCameraFrames() {
		// update camera frame
		final Vector3 rightDir = Vector3.fetchTempInstance();
		final Vector3 work = Vector3.fetchTempInstance();
		rightDir.set(getDirection()).crossLocal(getUp()).multiplyLocal(_eyeSeparation / 2.0);
		_leftCamera.setFrame(getLocation().subtract(rightDir, work), getLeft(), getUp(), getDirection());
		_rightCamera.setFrame(getLocation().add(rightDir, work), getLeft(), getUp(), getDirection());
		Vector3.releaseTempInstance(work);
		Vector3.releaseTempInstance(rightDir);
	}

	/**
	 * Switch to left camera for drawing
	 * 
	 * @param r
	 */
	public void switchToLeftCamera(final Renderer r) {
		ContextManager.getCurrentContext().enforceState(redColorMask);
		_leftCamera.update();
		_leftCamera.apply(r);
	}

	/**
	 * Switch to right camera for drawing
	 * 
	 * @param r
	 */
	public void switchToRightCamera(final Renderer r) {
		ContextManager.getCurrentContext().enforceState(cyanColorMask);
		_rightCamera.update();
		_rightCamera.apply(r);
	}

	/**
	 * @return the leftCamera
	 */
	public BasicCamera getLeftCamera() {
		return _leftCamera;
	}

	/**
	 * @return the rightCamera
	 */
	public BasicCamera getRightCamera() {
		return _rightCamera;
	}

	/**
	 * @return the focalDistance
	 */
	public double getFocalDistance() {
		return _focalDistance;
	}

	/**
	 * @param focalDistance
	 *            the focalDistance to set
	 */
	public void setFocalDistance(final double focalDistance) {
		_focalDistance = focalDistance;
	}

	/**
	 * @return the eyeSeparation
	 */
	public double getEyeSeparation() {
		return _eyeSeparation;
	}

	/**
	 * @param eyeSeparation
	 *            the eyeSeparation to set
	 */
	public void setEyeSeparation(final double eyeSeparation) {
		_eyeSeparation = eyeSeparation;
	}

	public void finish() {
		ContextManager.getCurrentContext().clearEnforcedState(StateType.ColorMask);
	}

}
