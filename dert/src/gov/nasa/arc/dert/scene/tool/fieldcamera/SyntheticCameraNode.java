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
 
Tile Rendering Library - Brian Paul 
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

package gov.nasa.arc.dert.scene.tool.fieldcamera;

import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scenegraph.HiddenLine;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.SpatialUtil;
import gov.nasa.arc.dert.view.fieldcamera.SimpleCrosshair;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.extension.CameraNode;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.PickingHint;

/**
 * A node that carries a camera as well as FOV frustum, and site line objects.
 *
 */
public class SyntheticCameraNode extends Node {

	public AtomicBoolean changed = new AtomicBoolean();

	// The cross hair at the center of the view
	protected SimpleCrosshair crosshair;
	protected boolean crosshairVisible = true;

	// Field Camera Box
	private CameraNode cameraNode;
	private Node geomNode;

	// Camera
	private BasicCamera basicCamera;
	private BoundingSphere sceneBounds = new BoundingSphere(10, new Vector3());
	private Vector3 oldTranslation = new Vector3();
	private Matrix3 oldRotation = new Matrix3();
	private double fovX;
	private double aspect;

	// FOV frustum and site line
	private FrustumPyramid fovFrustum;
	private HiddenLine siteLine;
	private double lineLength;

	// Center of scene
	private int centerX, centerY;

	public SyntheticCameraNode(FieldCameraInfo fieldCameraInfo) {
		super("_fieldCameraNode");
		
		basicCamera = new BasicCamera(1, 1);		

		// set orientation relative to OpenGL default axes
		Matrix3 rotX = new Matrix3().fromAngleNormalAxis(Math.PI / 2, Vector3.UNIT_X);
		setRotation(rotX);
		
		// geometryNode will be culled before rendering
		geomNode = new Node("_geometry");

		// create site line and FOV frustum
		siteLine = new HiddenLine("_line", Vector3.ZERO, new Vector3(0, 0, -1));
		siteLine.getSceneHints().setAllPickingHints(false);
		setSiteLineLength(2 * Landscape.getInstance().getWorldBound().getRadius());
		geomNode.attachChild(siteLine);
		fovFrustum = new FrustumPyramid("_frustum", basicCamera);
		fovFrustum.getSceneHints().setAllPickingHints(false);
		geomNode.attachChild(fovFrustum);

		// the camera for this fieldCamera
		cameraNode = new CameraNode() {
			@Override
			public void updateWorldTransform(boolean recurse) {
				super.updateWorldTransform(recurse);
				double farPlane = MathUtil.distanceToSphere(sceneBounds, basicCamera.getLocation(), basicCamera.getDirection());
				basicCamera.setFrustumFar(farPlane);
				changed.set(!(oldTranslation.equals(getWorldTranslation()) && oldRotation.equals(getWorldRotation())));
				oldTranslation.set(getWorldTranslation());
				oldRotation.set(getWorldRotation());
				setSiteLineLength(0.8 * farPlane);
				if (fovFrustum.isVisible())
					fovFrustum.setLength(0.8*farPlane);
			}
		};
		cameraNode.setCamera(basicCamera);

		attachChild(cameraNode);
		
		crosshair = new SimpleCrosshair(ColorRGBA.WHITE);
		crosshair.setTranslation(0, 0, -1);
		crosshair.getSceneHints().setCullHint(CullHint.Always);
		crosshair.updateGeometricState(0);
		double s = basicCamera.getPixelSizeAt(crosshair.getWorldTranslation(), true);
		crosshair.setScale(s, s, s);
		geomNode.attachChild(crosshair);

		attachChild(geomNode);

		setFieldCameraDefinition(fieldCameraInfo);
		
		changed.set(true);

		getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
	}
	
	public void setHiddenDashed(boolean hiddenDashed) {
		siteLine.enableDash(hiddenDashed);
	}

	/**
	 * Set the field camera definition
	 * 
	 * @param fieldCameraDef
	 * @return
	 */
	public void setFieldCameraDefinition(FieldCameraInfo fieldCameraInfo) {
		fovX = fieldCameraInfo.fovX;
		aspect = fieldCameraInfo.fovX / fieldCameraInfo.fovY;
		basicCamera.setFovX(fovX);
		basicCamera.setAspect(aspect);
		basicCamera.setFrustumFar(Landscape.getInstance().getWorldBound().getRadius());
		fovFrustum.setCamera(basicCamera);
		setTranslation(fieldCameraInfo.mountingOffset);
	}

	public BasicCamera getCamera() {
		return (basicCamera);
	}
	
	public Node getGeometryNode() {
		return(geomNode);
	}
	
	public SimpleCrosshair getCrosshair() {
		return(crosshair);
	}

	/**
	 * Set cross hair visibility
	 * 
	 * @param visible
	 */
	public void setCrosshairVisible(boolean visible) {
		crosshairVisible = visible;
		changed.set(true);
	}
	
	public boolean isCrosshairVisible() {
		return(crosshairVisible);
	}
	
	public void setColor(Color color) {
		fovFrustum.setColor(color);
		siteLine.setColor(color);
	}

	private void setSiteLineLength(double length) {
		if (lineLength == length) {
			return;
		}
		lineLength = length;
		siteLine.setScale(1, 1, length);
	}

	/**
	 * Get FOV visibility
	 * 
	 * @return
	 */
	public boolean isFovVisible() {
		return (SpatialUtil.isDisplayed(fovFrustum));
	}

	/**
	 * Set FOV visibility
	 * 
	 * @param enable
	 */
	public void setFovVisible(boolean visible) {
		fovFrustum.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
		fovFrustum.markDirty(DirtyType.RenderState);
	}

	/**
	 * Set lookAt line visibility
	 * 
	 * @param enable
	 */
	public void setSiteLineVisible(boolean visible) {
		siteLine.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
		siteLine.markDirty(DirtyType.RenderState);
	}

	/**
	 * Get lookAt line visibility
	 * 
	 * @return
	 */
	public boolean isSiteLineVisible() {
		return (SpatialUtil.isDisplayed(siteLine));
	}

	/**
	 * Set the bounds of the scene for computing clipping planes
	 */
	public void setSceneBounds() {
		BoundingVolume bounds = World.getInstance().getRoot().getWorldBound();
		sceneBounds.setRadius(bounds.getRadius());
		sceneBounds.setCenter(bounds.getCenter());
		basicCamera.setClippingPlanes(sceneBounds, true);
		basicCamera.setFrustumNear(0.1);
		double farPlane = MathUtil.distanceToSphere(World.getInstance().getRoot().getWorldBound(),
			basicCamera.getLocation(), basicCamera.getDirection());
		basicCamera.setFrustumFar(farPlane);
	}

	/**
	 * Resize the camera
	 * 
	 * @param width
	 * @param height
	 */
	public void resize(int width, int height) {
		if ((width == 0) || (height == 0)) {
			return;
		}
		if (basicCamera == null) {
			return;
		}
		basicCamera.resize(width, height);
		centerX = width / 2;
		centerY = height / 2;
		double s = basicCamera.getPixelSizeAt(crosshair.getWorldTranslation(), true);
		crosshair.setScale(s, s, s);
	}

	/**
	 * Get distance from focal point to surface
	 * 
	 * @return
	 */
	public double getDistanceToSurface() {
		getSceneHints().setPickingHint(PickingHint.Pickable, false);
		Vector3 position = new Vector3();
		Vector3 normal = new Vector3();
		Vector2 mousePos = new Vector2(centerX, centerY);
		Ray3 pickRay = new Ray3();
		basicCamera.getPickRay(mousePos, false, pickRay);
		Spatial spat = World.getInstance().select(pickRay, position, normal, null, true);
		getSceneHints().setPickingHint(PickingHint.Pickable, true);
		if (spat == null) {
			return (Double.NaN);
		} else {
			return (basicCamera.getLocation().distance(position));
		}
	}

	/**
	 * Point camera at given coordinate
	 * 
	 * @param point
	 * @return
	 */
	public Vector3 getAzElAngles(ReadOnlyVector3 point) {
		Vector3 dir = new Vector3(point);
		dir.subtractLocal(basicCamera.getLocation());
		dir.normalizeLocal();
		Vector3 angle = MathUtil.directionToAzEl(dir, null);
		return (angle);
	}
}
