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

package gov.nasa.arc.dert.render;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.util.MathUtil;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.image.Texture.DepthTextureCompareFunc;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Projected depth texture that implements a shadow map.
 *
 */
public class ShadowMap extends ProjectedDepthTexture {

	// Texture unit reserved for shadows
	public static final int SHADOW_MAP_UNIT = 7;

	// projection direction fields
	private Vector3 lightCameraLocation, lightDirection, dirTmp;

	// center of shadowed region
	private Vector3 center;

	// radius of shadowed region
	private double radius;

	// this shadow map is visible
	private boolean isEnabled;
	
	// Vector to store solar azimuth and elevation
	private Vector3 angle;

	public ShadowMap(ReadOnlyVector3 center, double radius, Spatial occluder, Spatial target) {
		super(occluder, target, DepthTextureCompareFunc.LessThanEqual, true, true);
		this.center = new Vector3(center);
		if (radius < 1) {
			radius = 1;
		}
		this.radius = radius;
		lightCameraLocation = new Vector3();
		lightDirection = new Vector3();
		dirTmp = new Vector3();
		angle = new Vector3();
	}

	/**
	 * Find out if this is visible
	 * 
	 * @return
	 */
	public boolean getEnabled() {
		return (isEnabled);
	}

	/**
	 * Make this shadow map visible
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		isEnabled = enabled;
		World world = World.getInstance();
		TextureState textureState = world.getTextureState();
		if (enabled) {
			textureState.setTexture(texture, SHADOW_MAP_UNIT);
		} else {
			textureState.setTexture(null, SHADOW_MAP_UNIT);
		}
		target.markDirty(DirtyType.RenderState);
		Landscape.getInstance().getLayerManager().enableShadow(enabled);
	}

	/**
	 * Set the center of the shadowed region
	 * 
	 * @param center
	 */
	public void setCenter(ReadOnlyVector3 center) {
		if (center == null)
			this.center.set(Landscape.getInstance().getCenter());
		else
			this.center.set(center);
		target.markDirty(DirtyType.RenderState);
	}

	/**
	 * Get the center of the shadowed region
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getCenter() {
		return (center);
	}

	/**
	 * Set the radius of the shadowed region
	 * 
	 * @param radius
	 */
	public void setRadius(double radius) {
		if (radius == 0) {
			BoundingVolume bv = World.getInstance().getContents().getWorldBound();
			radius = bv.getRadius();
		}
		if (radius < 1) {
			radius = 1;
		}
		this.radius = radius;
		target.markDirty(DirtyType.RenderState);
	}

	/**
	 * Get the radius of the shadowed region
	 * 
	 * @return
	 */
	public double getRadius() {
		return (radius);
	}

	/**
	 * Set the light direction for the shadows
	 * 
	 * @param dir
	 */
	public void updateLightDirection(ReadOnlyVector3 dir) {
		lightDirection.set(dir);
		angle = MathUtil.directionToAzEl(dir, angle);
	}

	@Override
	public void doPrerender(Renderer renderer) {
		if (isEnabled) {
			super.doPrerender(renderer);
		}
	}

	@Override
	protected void updateProjection() {

		// set location of camera
		lightCameraLocation.set(center);
		dirTmp.set(lightDirection);
		dirTmp.negateLocal();
		dirTmp.multiplyLocal(radius);
		lightCameraLocation.addLocal(dirTmp);

		// setup the camera
		Camera camera = textureRenderer.getCamera();
		camera.setLocation(lightCameraLocation);
		camera.lookAt(center, Vector3.UNIT_Y);
		camera.setFrustum(0.1, 2 * radius, -radius, radius, radius, -radius);
		camera.update();

		projectionMatrix.set(camera.getModelViewProjectionMatrix());
		projectionMatrix.multiplyLocal(BIAS);
	}

	@Override
	public void update(final Renderer r) {
		if (angle.getY() >= 0)
			Landscape.getInstance().getLayerManager().setAllDark(true);
		else
			Landscape.getInstance().getLayerManager().setAllDark(false);
		super.update(r);
		World.getInstance().getTextureState().setTexture(texture, SHADOW_MAP_UNIT);

		target.markDirty(DirtyType.RenderState);

	}

	@Override
	public void dispose() {
		setEnabled(false);
		super.dispose();
	}

}
