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

package gov.nasa.arc.dert.util;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.util.geom.BufferUtils;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUtessellator;
import com.jogamp.opengl.glu.GLUtessellatorCallback;
import com.jogamp.opengl.glu.GLUtessellatorCallbackAdapter;

/**
 * Tessellator provides methods to tessellate a polygon that can be convex,
 * concave, and have holes.
 *
 */
public class Tessellator {

	protected List<ReadOnlyVector3> outerVertex;
	protected List<List<ReadOnlyVector3>> innerVertex;
	protected ArrayList<Vector3> tessellatedPolygon = new ArrayList<Vector3>();
	protected FloatBuffer vertexBuffer;
	protected Vector3 referencePoint;

	public Tessellator() {
		// nothing here
	}

	protected void tessellateInterior(GLUtessellatorCallback callback) {
		GLU glu = new GLU();
		GLUtessellator tess = GLU.gluNewTess();
		this.beginTessellation(tess, callback);

		try {
			this.doTessellate(glu, tess, callback);
		} finally {
			this.endTessellation(tess);
			GLU.gluDeleteTess(tess);
		}

		vertexBuffer = BufferUtils.createFloatBuffer(tessellatedPolygon.size() * 3);
		for (int i = 0; i < tessellatedPolygon.size(); ++i) {
			Vector3 vec = tessellatedPolygon.get(i);
			vertexBuffer.put(vec.getXf());
			vertexBuffer.put(vec.getYf());
			vertexBuffer.put(vec.getZf());
		}
		vertexBuffer.flip();
	}

	protected void beginTessellation(GLUtessellator tess, GLUtessellatorCallback callback) {
		GLU.gluTessNormal(tess, 0.0, 0.0, 1.0);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_BEGIN, callback);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_VERTEX, callback);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_END, callback);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_COMBINE, callback);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_EDGE_FLAG, callback);
	}

	protected void endTessellation(GLUtessellator tess) {
		GLU.gluTessCallback(tess, GLU.GLU_TESS_BEGIN, null);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_VERTEX, null);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_END, null);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_COMBINE, null);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_EDGE_FLAG, null);
	}

	protected void doTessellate(GLU glu, GLUtessellator tess, GLUtessellatorCallback callback) {
		// Determine the winding order of the shape vertices, and setup the GLU
		// winding rule which corresponds to
		// the shapes winding order.
		int windingRule = (MathUtil.isCounterClockwise(outerVertex)) ? GLU.GLU_TESS_WINDING_POSITIVE
			: GLU.GLU_TESS_WINDING_NEGATIVE;
		// System.err.println("Tessellator.doTessellate "+windingRule+" "+GLU.GLU_TESS_WINDING_POSITIVE+" "+GLU.GLU_TESS_WINDING_NEGATIVE);

		GLU.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, windingRule);
		GLU.gluTessBeginPolygon(tess, null);
		GLU.gluTessBeginContour(tess);

		for (ReadOnlyVector3 pos : outerVertex) {
			double[] compArray = new double[3];
			compArray[0] = pos.getX();
			compArray[1] = pos.getY();
			compArray[2] = pos.getZ();
			GLU.gluTessVertex(tess, compArray, 0, compArray);
		}
		GLU.gluTessEndContour(tess);

		if (innerVertex != null) {
			for (int i = 0; i < innerVertex.size(); ++i) {
				GLU.gluTessBeginContour(tess);
				for (ReadOnlyVector3 ll : innerVertex.get(i)) {
					ReadOnlyVector3 pos = ll;
					double[] compArray = new double[3];
					compArray[0] = pos.getX();
					compArray[1] = pos.getY();
					compArray[2] = pos.getZ();
					GLU.gluTessVertex(tess, compArray, 0, compArray);
				}
				GLU.gluTessEndContour(tess);
			}
		}
		GLU.gluTessEndPolygon(tess);
	}

	/**
	 * Callback class for tessellating polygon interior.
	 *
	 */
	protected class TessellatorCallback extends GLUtessellatorCallbackAdapter {

		public TessellatorCallback() {
			// nothing here
		}

		@Override
		public void begin(int type) {
			// nothing here
		}

		@Override
		public void vertex(Object vertexData) {
			Vector3 pos = new Vector3(((double[]) vertexData)[0], ((double[]) vertexData)[1],
				((double[]) vertexData)[2]);
			// System.err.println("Tessellator.vertex "+pos);
			pos.addLocal(referencePoint);
			tessellatedPolygon.add(pos);
		}

		@Override
		public void end() {
			// nothing here
		}

		@Override
		public void combine(double[] coords, Object[] data, float[] weight, Object[] outData) {
			outData[0] = coords;
		}

		@Override
		public void edgeFlag(boolean boundaryEdge) {
			// nothing here
		}
	}

	/**
	 * Uses the first coordinate as a reference point that is subtracted from
	 * all others to reduce the size of the number. The polygon is also
	 * tessellated and the vertex buffer is set for rendering.
	 * 
	 * @param inPositions
	 *            new coordinates
	 * @param dc
	 *            DrawContext
	 */
	public FloatBuffer tessellate(ArrayList<ReadOnlyVector3> outerVertex, List<List<ReadOnlyVector3>> innerVertex) {
		if ((outerVertex == null) || (outerVertex.size() == 0)) {
			return (null);
		}
		ArrayList<ReadOnlyVector3> positions = new ArrayList<ReadOnlyVector3>();
		referencePoint = new Vector3(outerVertex.get(0));
		// System.err.println("Tessellator.tessellate "+referencePoint);
		for (ReadOnlyVector3 vert : outerVertex) {
			Vector3 pos = vert.subtract(referencePoint, null);
			positions.add(pos);
		}
		this.outerVertex = positions;

		if (innerVertex != null) {
			this.innerVertex = new ArrayList<List<ReadOnlyVector3>>();
			for (int i = 0; i < innerVertex.size(); ++i) {
				List<ReadOnlyVector3> innerVert = innerVertex.get(i);
				positions = new ArrayList<ReadOnlyVector3>();
				for (ReadOnlyVector3 vert : innerVert) {
					Vector3 pos = vert.subtract(referencePoint, null);
					positions.add(pos);
				}
				this.innerVertex.add(positions);
			}
		}

		tessellatedPolygon.clear();
		this.tessellateInterior(new TessellatorCallback());
		return (vertexBuffer);
	}
}
