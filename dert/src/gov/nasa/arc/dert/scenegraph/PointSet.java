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

import gov.nasa.arc.dert.scene.tool.Waypoint;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.Tessellator;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.spline.CatmullRomSpline;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Provides a node for managing sets of points.
 *
 */
public class PointSet extends Node {

	// List of points
	private ArrayList<ReadOnlyVector3> pointList;

	// For polygon
	private Tessellator tessellator;
	
	// Curve for fly through
	private CatmullRomSpline spline;

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public PointSet(String name) {
		super(name);
		pointList = new ArrayList<ReadOnlyVector3>();
	}

	/**
	 * Add a point to the set.
	 * 
	 * @param p
	 *            the point
	 * @param index
	 *            the index to insert the point, -1 indicates to add at the end
	 * @return the index where the point was added
	 */
	public int addPoint(Waypoint p, int index) {
//		System.err.println("PointSet.addPoint "+getNumberOfChildren()+" "+this+" "+index+" "+p.getWorldBound()+" "+p.getTranslation());
		if ((index == getNumberOfChildren()) || (index < 0)) {
			attachChild(p);
			index = getChildIndex(p);			
		}
		else {
			attachChildAt(p, index);
			index = getChildIndex(p);
		}
//		updateGeometricState(0, true);
		return (index);
	}
	
//	public void showWaypointsAsSpheres(boolean useSpheres) {
//		int n = getNumberOfChildren();
//		for (int i=0; i<n; ++i) {
//			Waypoint wp = (Waypoint)getChild(i);
//			wp.showAsSphere(useSpheres);
//		}
//	}

	/**
	 * Remove a point from the set.
	 * 
	 * @param point
	 */
	public void removePoint(Waypoint point) {
		detachChild(point);
	}

	private ArrayList<ReadOnlyVector3> getPointList() {
		pointList.clear();
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			pointList.add(new Vector3(getChild(i).getTranslation()));
		}
		return (pointList);
	}
	
	public void setPointSize(double size) {
		for (int i=0; i<getNumberOfChildren(); ++i) {
			Waypoint wp = (Waypoint)getChild(i);
			wp.setSize(size);
		}
	}
	
	public Vector3[] getPolygonVertices() {
		getPointList();
		int n = getNumberOfChildren();
		if (n == 0) {
			return(null);
		}
		else {
			Vector3[] vertex = new Vector3[n + 1];
			for (int i = 0; i < n; ++i) {
				vertex[i] = new Vector3(pointList.get(i));
			}
			vertex[n] = new Vector3(vertex[0]);
			return(vertex);
		}
	}

	/**
	 * Find the centroid of this point set.
	 * 
	 * @return the centroid
	 */
	public Vector3 getCentroid() {
		int n = getNumberOfChildren();
		if (n == 0) {
			return (null);
		}
		double x = 0;
		double y = 0;
		double z = 0;
		getPointList();
		for (int i = 0; i < pointList.size(); ++i) {
			ReadOnlyVector3 trans = pointList.get(i);
			x += trans.getX();
			y += trans.getY();
			z += trans.getZ();
		}
		return (new Vector3(x / n, y / n, z / n));
	}

	/**
	 * Get the exact bounds of this point set.
	 * 
	 * @param lowerBound
	 * @param upperBound
	 */
	public void getBounds(Vector3 lowerBound, Vector3 upperBound) {
		int n = getNumberOfChildren();
		if (n == 0) {
			lowerBound.set(0, 0, 0);
			upperBound.set(0, 0, 0);
			return;
		}
		double xMin = Double.MAX_VALUE;
		double xMax = -Double.MAX_VALUE;
		double yMin = Double.MAX_VALUE;
		double yMax = -Double.MAX_VALUE;
		double zMin = Double.MAX_VALUE;
		double zMax = -Double.MAX_VALUE;
		getPointList();
		for (int i = 0; i < n; ++i) {
			ReadOnlyVector3 vertex = pointList.get(i);
			xMin = Math.min(xMin, vertex.getX());
			xMax = Math.max(xMax, vertex.getX());
			yMin = Math.min(yMin, vertex.getY());
			yMax = Math.max(yMax, vertex.getY());
			zMin = Math.min(zMin, vertex.getZ());
			zMax = Math.max(zMax, vertex.getZ());
		}
		lowerBound.set(xMin, yMin, zMin);
		upperBound.set(xMax, yMax, zMax);
	}

	/**
	 * Get the distance along the point set.
	 * 
	 * @return
	 */
	public double getDistance() {
		int pointCount = getNumberOfChildren();
		if (pointCount < 2) {
			return (0);
		}
		getPointList();
		ReadOnlyVector3 p0 = pointList.get(0);
		double distance = 0;
		for (int i = 1; i < pointCount; ++i) {
			ReadOnlyVector3 p1 = pointList.get(i);
			double d = p0.distance(p1);
			distance += d;
			p0 = p1;
		}
		return (distance);
	}

	/**
	 * Estimate the area covered by the region defined by these points.
	 * 
	 * @return the area
	 */
	public double getArea() {
		double area = Math.abs(MathUtil.computePolygonArea2D(getPointList()));
		return (area);
	}

	/**
	 * Create the line among points
	 */
//	public Line createLine() {
//		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(3);
//		vertexBuffer.limit(0);
//		Line line = new Line("_polyline");
//		line.getMeshData().setIndexMode(IndexMode.LineStrip);
//		line.getMeshData().setVertexBuffer(vertexBuffer);
//		line.getSceneHints().setCastsShadows(false);
//		line.getSceneHints().setTextureCombineMode(TextureCombineMode.Off);
//		line.setModelBound(new BoundingBox());
//		line.updateModelBound();
//		line.getSceneHints().setCullHint(CullHint.Always);
//		line.getSceneHints().setPickingHint(PickingHint.Pickable, false);
//		return (line);
//	}

	/**
	 * Create the polygon among points
	 * 
	 * @return
	 */
	public Mesh createPolygon() {
		tessellator = new Tessellator();
		Mesh poly = new Mesh("_polygon");
		poly.setModelBound(new BoundingBox());
		poly.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
		poly.getSceneHints().setCullHint(CullHint.Always);
		poly.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		poly.getSceneHints().setCastsShadows(false);
		
		return (poly);
	}

	/**
	 * Update the line from the points
	 * 
	 * @param line
	 */
	public void updateLine(HiddenLine line) {
		int pointCount = getNumberOfChildren();
		if (pointCount < 2) {
			return;
		}
		FloatBuffer vertexBuffer = line.getVertexBuffer();
		if ((pointCount * 3) > vertexBuffer.capacity()) {
			vertexBuffer = BufferUtils.createFloatBuffer(pointCount * 3);
		}
		vertexBuffer.clear();
		ReadOnlyVector3 trans = null;
		getPointList();
		for (int i = 0; i < pointCount; ++i) {
			int j = i * 3;
			trans = pointList.get(i);
			vertexBuffer.put(j, trans.getXf());
			vertexBuffer.put(j + 1, trans.getYf());
			vertexBuffer.put(j + 2, trans.getZf());
		}
		vertexBuffer.limit(pointCount * 3);
		vertexBuffer.rewind();
		line.setVertexBuffer(vertexBuffer);
		line.updateModelBound();
	}

	/**
	 * Update the polygon from the points
	 * 
	 * @param poly
	 */
	public void updatePolygon(Mesh poly) {
		if (getNumberOfChildren() < 3) {
			return;
		}
		FloatBuffer vertexBuffer = tessellator.tessellate(getPointList(), null);
		FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(vertexBuffer.capacity());
		MathUtil.computePolygonNormal(vertexBuffer, normalBuffer, true);
		poly.getMeshData().setVertexBuffer(vertexBuffer);
		poly.getMeshData().setNormalBuffer(normalBuffer);
		poly.getMeshData().updateVertexCount();
		poly.markDirty(DirtyType.Bounding);
		poly.updateModelBound();
		poly.updateGeometricState(0);
	}
	
	/**
	 * Get a curve of points interpolated between the path waypoints.
	 * @param numFrames
	 * @return
	 */
	public Vector3[] getCurve(int steps) {
		getPointList();
		int n = pointList.size();
		if (steps <= 1) {
			Vector3[] vectors = new Vector3[n];
			pointList.toArray(vectors);
			return(vectors);
		}
		// linear interpolation
		if (n < 4) {
			ArrayList<Vector3> list = new ArrayList<Vector3>();
			float delta = 1.0f / steps;
			ReadOnlyVector3 v0 = pointList.get(0);
			for (int i = 1; i < pointList.size(); ++i) {
				for (float t = 0; t < 1.0; t += delta)
					list.add(v0.lerp(pointList.get(i), t, null));
				v0 = pointList.get(i);
			}
			list.add(new Vector3(pointList.get(n - 1)));
			Vector3[] vectors = new Vector3[list.size()];
			list.toArray(vectors);
			return(vectors);
		}
		// spline interpolation
		if (spline == null) 
			spline = new CatmullRomSpline();
		return(toVector3SplineInterpolation(steps));
	}
	

    /**
     * Interpolates the curve and returns an array of vectors.
     */
    private Vector3[] toVector3SplineInterpolation(final int steps) {

        getPointList();

        final int start = 1;
        final int end = pointList.size()-2;
        final int count = (end - start) * steps;

        final Vector3[] vectors = new Vector3[count];

        int index = start;

        for (int i = 0; i < count; i++) {
            final int is = i % steps;

            if (0 == is && i >= steps) {
                index++;
            }

//            final double t = is / (steps - 1.0);
            final double t = is / (double)steps;

            final int p0 = index - 1;
            final int p1 = index;
            final int p2 = index + 1;
            final int p3 = index + 2;

            vectors[i] = spline.interpolate(pointList.get(p0), pointList.get(p1), pointList.get(p2),
            		pointList.get(p3), t);
        }

        return vectors;
    }
	
//    @Override
//    public void draw(final Renderer r) {
//    	System.err.println("PointSet.draw "+getName()+" "+getNumberOfChildren()+" "+this);
//    	for (int i=0; i<getNumberOfChildren(); ++i)
//    		System.err.println("PointSet.draw "+i+" "+getChild(i)+" "+getChild(i).getClass());
//    	super.draw(r);
//    }

}
