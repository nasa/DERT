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

package gov.nasa.arc.dert.scenegraph;

import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.util.SpatialUtil;

import java.awt.Color;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.MaterialState.MaterialFace;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;

/**
 * Provides a 3D marker object.
 */
public class FigureMarker extends Marker {

	public static float AMBIENT_FACTOR = 0.75f;

	// Rotation
	protected double azimuth, tilt;
	protected Matrix3 rotMat;

	// 3D shape
	protected Shape shape;
	protected ShapeType shapeType = ShapeType.none;

	// Arrow to show surface normal
	protected DirectionArrow surfaceNormalArrow;
	protected Vector3 surfaceNormal = new Vector3();

	// flag to maintain the original size as viewpoint changes. If true, resize
	// as viewpoint changes.
	protected boolean autoScale;
	protected boolean autoShowLabel = false;

	/**
	 * Constructor
	 */
	public FigureMarker(String name, ReadOnlyVector3 point, double size, double zOff, Color color, boolean labelVisible,
		boolean autoScale, boolean pinned) {
		super(name, point, (float) size, zOff, color, labelVisible, pinned);
		this.autoScale = autoScale;
		surfaceNormalArrow = new DirectionArrow("Surface Normal", (float)(size*2), ColorRGBA.RED);
		surfaceNormalArrow.getSceneHints().setCullHint(CullHint.Always);
		contents.attachChild(surfaceNormalArrow);
	}

	/**
	 * Set the type of 3D shape
	 * 
	 * @param type
	 */
	public void setShape(ShapeType type, boolean force) {
		if (!force && (shapeType == type))
			return;
		if (shape != null)
			contents.detachChild(shape);
		shapeType = type;
		shape = Shape.createShape("_geometry", shapeType, (float)size);
		shape.updateWorldBound(true);
		SpatialUtil.setPickHost(shape, this);
		contents.attachChild(shape);
		Vector3 offset = Shape.SHAPE_TEXT_OFFSET[shapeType.ordinal()];
		label.setTranslation(offset.getX(), offset.getY()*size, offset.getZ());
		updateWorldTransform(true);
		updateWorldBound(true);
		autoScale &= Shape.SCALABLE[shapeType.ordinal()];
		scaleShape(scale);
	}
	
	public void setAutoShowLabel(boolean show) {
		autoShowLabel = show;
	}

	@Override
	protected void setMaterialState() {
		materialState.setAmbient(MaterialFace.FrontAndBack, new ColorRGBA(colorRGBA.getRed() * AMBIENT_FACTOR,
			colorRGBA.getGreen() * AMBIENT_FACTOR, colorRGBA.getBlue() * AMBIENT_FACTOR, colorRGBA.getAlpha()));
		materialState.setDiffuse(MaterialFace.FrontAndBack, colorRGBA);
		materialState.setEmissive(MaterialFace.FrontAndBack, ColorRGBA.BLACK);
	}

	@Override
	protected void enableHighlight(boolean enable) {
		if (enable) {
			materialState.setAmbient(MaterialFace.FrontAndBack, new ColorRGBA(colorRGBA.getRed() * AMBIENT_FACTOR,
				colorRGBA.getGreen() * AMBIENT_FACTOR, colorRGBA.getBlue() * AMBIENT_FACTOR, colorRGBA.getAlpha()));
			materialState.setDiffuse(MaterialFace.FrontAndBack, colorRGBA);
			materialState.setEmissive(MaterialFace.FrontAndBack, colorRGBA);
		} else {
			setMaterialState();
		}
		markDirty(DirtyType.RenderState);
	}

	@Override
	protected void scaleShape(double scale) {
		if (autoScale)
			contents.setScale(scale);
		else
			contents.setScale(1);
	}

	/**
	 * Determine if fixed size
	 * 
	 * @return
	 */
	public boolean isAutoScale() {
		return (autoScale);
	}

	/**
	 * Set the azimuth
	 * 
	 * @param azimuth
	 */
	public void setAzimuth(double azimuth) {
		if (this.azimuth == azimuth) {
			return;
		}
		this.azimuth = azimuth;
		if (shape != null) {
			rotMat = new Matrix3();
			shape.setRotation(rotMat.fromAngles(Math.toRadians(tilt), 0.0, -Math.toRadians(azimuth)));
		}
	}

	/**
	 * Get the azimuth
	 * 
	 * @return
	 */
	public double getAzimuth() {
		return (azimuth);
	}

	/**
	 * Set the tilt
	 * 
	 * @param tilt
	 */
	public void setTilt(double tilt) {
		if (this.tilt == tilt) {
			return;
		}
		this.tilt = tilt;
		if (shape != null) {
			rotMat = new Matrix3();
			shape.setRotation(rotMat.fromAngles(Math.toRadians(tilt), 0.0, -Math.toRadians(azimuth)));
		}
	}

	/**
	 * Get the tilt
	 * 
	 * @return
	 */
	public double getTilt() {
		return (tilt);
	}

	/**
	 * Set the surface normal
	 * 
	 * @param normal
	 */
	public void setNormal(ReadOnlyVector3 normal) {
		if (normal != null) {
			surfaceNormal.set(normal);
			if (surfaceNormalArrow != null)
				surfaceNormalArrow.setDirection(normal);
		}
	}

	/**
	 * Get the surface normal
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getNormal() {
		return (surfaceNormal);
	}

	/**
	 * Set the size
	 * 
	 * @param size
	 */
	@Override
	public void setSize(double size) {
		if (this.size == size) {
			return;
		}
		this.size = size;
		if (surfaceNormalArrow != null)
			surfaceNormalArrow.setLength(size*1.5);
		setShape(shapeType, true);
	}

	/**
	 * Get the type of shape
	 * 
	 * @return
	 */
	public ShapeType getShapeType() {
		return (shapeType);
	}

	/**
	 * Set fixed size
	 * 
	 * @param fixed
	 */
	public void setAutoScale(boolean auto) {
		if (autoScale == auto) {
			return;
		}
		autoScale = auto;
		if (!auto) {
			oldScale = scale;
			scale = 1;
		} else {
			scale = oldScale;
		}
		scaleShape(scale);
		markDirty(DirtyType.Transform);
	}

	/**
	 * Update size based on camera location
	 */
	@Override
	public void update(BasicCamera camera) {
		scale = camera.getPixelSizeAt(getWorldTranslation(), true) * PIXEL_SIZE;
		if (Math.abs(scale - oldScale) > 0.0000001) {
			oldScale = scale;
			if (autoScale) {
				scaleShape(scale);
			}
		}
		if (labelVisible) {
			if (autoShowLabel) {
				if (scale <= PIXEL_SIZE) {
					if (!label.isVisible())
						label.setVisible(true);
				}
				else if (label.isVisible())
					label.setVisible(false);
			}
		}
	}
	
	@Override
	public void setInMotion(boolean inMotion, ReadOnlyVector3 pickPosition) {
		if (isInMotion() && !inMotion) {
			if (surfaceNormalArrow != null) {
				if (SpatialUtil.isDisplayed(surfaceNormalArrow)) {
					Landscape.getInstance().getNormal(getLocation().getX(), getLocation().getY(), surfaceNormal);
					surfaceNormalArrow.setDirection(surfaceNormal);
				}
			}
		}
		super.setInMotion(inMotion, pickPosition);		
	}

}
