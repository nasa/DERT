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

package gov.nasa.arc.dert.view.world;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.util.geom.BufferUtils;

/**
 * The view dependent cross hair that is drawn at the center of rotation in the
 * worldview. It provides three axes: green = North, red = East, blue == Up.
 *
 */
public class RGBAxes extends Line {

	// Coordinates and colors
	private static final ColorRGBA greenColor = new ColorRGBA(0.0f, 0.8f, 0.0f, 1.0f);
	private static final float[] axisVertex = { 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, -0.2f, 1.25f, 0, -0.2f, 1.8f, 0,
		0.2f, 1.25f, 0, 0.2f, 1.8f, 0 };
	private static final ReadOnlyColorRGBA[] axisColor = { ColorRGBA.BLACK, ColorRGBA.RED, ColorRGBA.GREEN,
		ColorRGBA.BLUE, greenColor, greenColor, greenColor, greenColor };
	private static final int[] axisIndex = { 0, 1, 0, 2, 0, 3, 4, 5, 5, 6, 6, 7 };
	private ZBufferState zBuf;

	/**
	 * Constructor
	 */
	public RGBAxes() {
		super("CenterOfRotation");
		setLineWidth(3.0f);
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
		setModelBound(new BoundingBox());
		updateModelBound();
		
		MaterialState axisMaterialState = new MaterialState();
		axisMaterialState.setColorMaterial(MaterialState.ColorMaterial.Emissive);
		axisMaterialState.setColorMaterialFace(MaterialState.MaterialFace.FrontAndBack);
		axisMaterialState.setEnabled(true);
		getSceneHints().setLightCombineMode(LightCombineMode.Off);
		setRenderState(axisMaterialState);
		
		zBuf = new ZBufferState();
		zBuf.setFunction(ZBufferState.TestFunction.Always);
		zBuf.setEnabled(true);
		setRenderState(zBuf);
		getSceneHints().setRenderBucketType(RenderBucketType.Skip);		
		
		updateGeometricState(0, false);
	}
}
