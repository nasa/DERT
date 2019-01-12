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

package gov.nasa.arc.dert.lighting;

import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.ephemeris.Ephemeris;
import gov.nasa.arc.dert.util.MathUtil;

import com.ardor3d.light.DirectionalLight;
import com.ardor3d.light.Light;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyMatrix3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Sphere;

/**
 * Provides an Ardor3D node for a directional light, carrying a sphere to
 * visualize it.
 *
 */
public class DirectionalLightNode extends Node {

	public static int SOL_PIXELS = 5;
	public static ReadOnlyColorRGBA SOL_COLOR = ColorRGBA.YELLOW;

	// The OpenGL light
	private DirectionalLight light;

	// Target light state
	private LightState lightState;

	// Ardor3D object that is affected by the light
	private Spatial target;

	// Light direction
	private Vector3 direction = new Vector3(0, 0, -1);

	// Light location
	private Vector3 location = new Vector3(0, 0, 1);

	// Global ambient light for the geometry
	private ColorRGBA globalAmbient = new ColorRGBA(0.2f, 0.2f, 0.2f, 1);

	// spherical coordinates of the light source in terms of the
	// plane of the landscape (ENU coordinates: see
	// http://en.wikipedia.org/wiki/Geodetic_system)
	private double azimuth, elevation; // radians

	// Location of the orb
	private Vector3 orbLocation;

	// The orb representing the light in the scene
	private Sphere orb;

	// Used to set light position
	private Vector3 dirVector = new Vector3();
	private Matrix3 rotMatrix = new Matrix3();
	private Vector3 startVector = new Vector3(0, 0, 1);

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param azimuth
	 * @param elevation
	 * @param direction
	 */
	public DirectionalLightNode(String name, double azimuth, double elevation, ReadOnlyVector3 direction) {
		super(name);
		this.azimuth = azimuth;
		this.elevation = elevation;
		this.direction.set(direction);
		light = new DirectionalLight();
		light.setSpecular(new ColorRGBA(0.2f, 0.2f, 0.2f, 1f));
		light.setDirection(this.direction);
		getSceneHints().setCullHint(CullHint.Never);
		orbLocation = new Vector3();
		buildOrb();
	}

	/**
	 * Enable the light
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		light.setEnabled(enabled);
		lightState.setGlobalAmbient(globalAmbient);
		lightState.setTwoSidedLighting(false);
		target.markDirty(DirtyType.RenderState);
	}

	/**
	 * Set target global ambient light
	 * 
	 * @param globalAmbient
	 */
	public void setGlobalAmbient(ColorRGBA globalAmbient) {
		lightState.setGlobalAmbient(globalAmbient);
	}

	/**
	 * Set the target and its Ardor3D light state
	 * 
	 * @param spatial
	 */
	public void setTarget(Spatial spatial) {
		if (lightState != null) {
			lightState.detach(light);
			target.setRenderState(lightState);
			target.updateGeometricState(0, true);
		}
		target = spatial;
		lightState = (LightState) target.getLocalRenderState(RenderState.StateType.Light);
		if (lightState == null) {
			lightState = new LightState();
			lightState.setGlobalAmbient(globalAmbient);
		}
		lightState.setEnabled(true);
		lightState.attach(light);
		target.setRenderState(lightState);
		setPositionFromAzEl();
		target.updateGeometricState(0, true);

		orbLocation = new Vector3();
		buildOrb();
	}

	/**
	 * 
	 * Get the light
	 */
	public Light getLight() {
		return (light);
	}

	/**
	 * Modify the light data based on any change the light node has made.
	 * 
	 * @param time
	 *            the time between frames.
	 * 
	 */
	public void updateWorldData(float time) {
		super.updateGeometricState(time);
		super.updateWorldTransform(false);
		direction.set(0, 0, -1);
		ReadOnlyMatrix3 rotMat = _worldTransform.getMatrix();
		rotMat.applyPost(direction, direction);
		direction.normalizeLocal();
		location.set(_worldTransform.getTranslation());

		if (light != null) {
			light.setDirection(direction);
		}
	}

	/**
	 * Get the azimuth of the current light location
	 * 
	 * @return
	 */
	public double getAzimuth() {
		return (azimuth);
	}

	/**
	 * Get the elevation of the current light location
	 * 
	 * @return
	 */
	public double getElevation() {
		return (elevation);
	}

	/**
	 * Set the light azimuth and elevation
	 * 
	 * @param azimuth
	 * @param elevation
	 */
	public void setAzEl(double azimuth, double elevation) {
		this.azimuth = azimuth;
		this.elevation = elevation;
	}

	/**
	 * Set the light location from the current azimuth and elevation
	 */
	public void setPositionFromAzEl() {
		MathUtil.azElToDirection(azimuth, elevation, dirVector);
		setPositionFromDirection();
	}

	/**
	 * Get the direction to the light
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getDirectionToLight() {
		return (dirVector);
	}

	/**
	 * Get the direction the light is pointing
	 * 
	 * @return
	 */
	public Vector3 getDirection() {
		return (direction);
	}

	private void setPositionFromDirection() {
		rotMatrix.fromStartEndLocal(startVector, dirVector);
		setRotation(rotMatrix);
		updateWorldData(0);
		target.markDirty(DirtyType.RenderState);
	}

	private void buildOrb() {
		orb = new Sphere("_orb", 20, 20, 1);
		orb.setSolidColor(SOL_COLOR);
		orb.updateGeometricState(0);
	}

	/**
	 * Update the orb size with the current viewpoint camera
	 * 
	 * @param camera
	 */
	public void updateOrb(BasicCamera camera) {
		double far = camera.getFrustumFar() * 0.9;
		orbLocation.set(dirVector);
		orbLocation.multiplyLocal(far);
		orbLocation.addLocal(camera.getLocation());
		double s = camera.getPixelSizeAt(orbLocation, false) * SOL_PIXELS;
		orb.setScale(s, s, s);
		orb.setTranslation(orbLocation);
		orb.updateGeometricState(0);
	}

	/**
	 * Render the orb
	 * 
	 * @param renderer
	 */
	public void drawOrb(Renderer renderer) {
		orb.onDraw(renderer);
	}

	/**
	 * Set the current time relative to a reference lon/lat/alt. Set the location of
	 * the light based on the time.
	 * 
	 * @param time
	 * @param planet
	 * @param source
	 * @param refLocLat
	 * @param refLocLon
	 */
	public void setTime(long time, String planet, String source, ReadOnlyVector3 refLoc) {
		String timeStr = Ephemeris.getInstance().time2UtcStr(time);
		double[] val = Ephemeris.getTargetVector(planet, source, timeStr, refLoc.getX(), refLoc.getY(), refLoc.getZ());
		if (val != null) {
			dirVector.set((float) val[0], (float) val[1], (float) val[2]);
			azimuth = (float) val[4];
			elevation = (float) val[5];
			setPositionFromDirection();
		}
	}
}