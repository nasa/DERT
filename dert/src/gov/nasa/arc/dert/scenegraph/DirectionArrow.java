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

package gov.nasa.arc.dert.scenegraph;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Provides a simple arrow indicating a direction. The brighter end of the line
 * segment is the arrowhead. Adapted from an Ardor3D example.
 *
 */
public class DirectionArrow extends Line {

	private static float[] axisVertex = { 0, 0, 0, 0, 0, 1 };
	private static final ReadOnlyColorRGBA[] axisColor = { ColorRGBA.BLACK, ColorRGBA.YELLOW };
	private static final int[] axisIndex = { 0, 1 };
	private static MaterialState axisMaterialState;

	private Vector3 direction, start;
	private Matrix3 rotMatrix;

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param length
	 * @param color
	 */
	public DirectionArrow(String name, float length, ReadOnlyColorRGBA color) {
		super(name);
		axisVertex[5] = length;
		axisColor[1] = color;
		setLineWidth(3.0f);
		direction = new Vector3(0, 0, 1);
		start = new Vector3(0, 0, 1);
		rotMatrix = new Matrix3();
		setRotation(rotMatrix);
		getMeshData().setIndexMode(IndexMode.Lines);
		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(axisVertex);
		vertexBuffer.rewind();
		getMeshData().setVertexBuffer(vertexBuffer);
		FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(axisColor);
		colorBuffer.rewind();
		getMeshData().setColorBuffer(colorBuffer);
		IntBuffer indexBuffer = BufferUtils.createIntBuffer(axisIndex);
		indexBuffer.rewind();
		getMeshData().setIndexBuffer(indexBuffer);
		axisMaterialState = new MaterialState();
		axisMaterialState.setColorMaterial(MaterialState.ColorMaterial.Emissive);
		axisMaterialState.setEnabled(true);
		getSceneHints().setLightCombineMode(LightCombineMode.Off);
		getSceneHints().setCullHint(CullHint.Never);
		setRenderState(axisMaterialState);
		setModelBound(new BoundingBox());
		updateGeometricState(0, false);
	}

	/**
	 * Set the direction
	 * 
	 * @param direction
	 */
	public void setDirection(ReadOnlyVector3 direction) {
		System.err.println("DirectionArrow.setDirection "+start+" "+direction);
		this.direction.set(direction);
		rotMatrix.fromStartEndLocal(start, direction);
		setRotation(rotMatrix);
		updateGeometricState(0);
	}

	/**
	 * Get the direction
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getDirection() {
		return (direction);
	}
	
	public void setLength(double length) {
		axisVertex[5] = (float)length;
		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(axisVertex);
		vertexBuffer.rewind();
		getMeshData().setVertexBuffer(vertexBuffer);
	}

}
