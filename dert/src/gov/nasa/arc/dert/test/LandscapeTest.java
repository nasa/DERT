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

package gov.nasa.arc.dert.test;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.Tessellator;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.util.geom.BufferUtils;

public class LandscapeTest {
	
	private TestDemFactory demFactory;
	
	public boolean testLandscape(TestDemFactory demFactory) {
		
		this.demFactory = demFactory;
		
		Landscape landscape = Landscape.getInstance();
		
		if (!testGetVertices(landscape)) {
			System.err.println("Test of Landscape.getVertices failed.");
			return(false);
		}
		
		Vector3 coord = new Vector3(10, 10, 0);
		System.err.println("LandscapeTest Coordinate Tests for "+coord);		
		landscape.localToWorldCoordinate(coord);
		System.err.println("localToWorldCoordinate "+coord);
		landscape.worldToLocalCoordinate(coord);
		System.err.println("worldToLocalCoordinate "+coord);
		if (!((coord.getX() == 10) && (coord.getY() == 10) && (coord.getZ() == 0))) {
			System.err.println("Test of Landscape coordinates failed.");
			return(false);
		}
		coord.set(0.001, 0.001, 0);
		System.err.println("LandscapeTest Spherical Coordinate Tests for "+coord);		
		landscape.sphericalToWorldCoordinate(coord);
		System.err.println("sphericalToWorldCoordinate "+coord);
		landscape.worldToSphericalCoordinate(coord);
		System.err.println("worldToSphericalCoordinate "+coord);
		if (!MathUtil.equalsFloat(coord, new Vector3(0.001, 0.001, 0))) {
			System.err.println("Test of Landscape spherical coordinates failed.");
			return(false);
		}
		
		if (!testGetSampledMeanElevationOfRegion(landscape)) {
			System.err.println("Test of Landscape.getSampledMeanElevationOfRegion failed.");
			return(false);
		}
		
		if (!testGetSampledMeanSlopeOfRegion(landscape)) {
			System.err.println("Test of Landscape.getSampledMeanSlopeOfRegion failed.");
			return(false);
		}
		
		if (!testGetSampledVolumeOfRegion(landscape)) {
			System.err.println("Test of Landscape.getSampledVolumeOfRegion failed.");
			return(false);
		}
		
		if (!testGetSampledSurfaceAreaOfRegion(landscape)) {
			System.err.println("Test of Landscape.getSampledSurfaceAreaOfRegion failed.");
			return(false);
		}
		
		if (!testGetSampledDifferenceOfRegion(landscape)) {
			System.err.println("Test of Landscape.getSampledDifferenceOfRegion failed.");
			return(false);
		}
		
		
		return(true);
	}
	
	private boolean testGetVertices(Landscape landscape) {
		float[] vertex = new float[1024*1024];
		Vector3 p0 = new Vector3(-511, -511, 0);
		Vector3 p1 = new Vector3(511, 511, 0);
		int n = landscape.getVertices(vertex, p0, p1, true, false);
		double dx = p1.getX() - p0.getX();
		double dy = p1.getY() - p0.getY();
		double lineLength = Math.sqrt(dx * dx + dy * dy);
		double step = 1;
		dx /= lineLength;
		dy /= lineLength;
		double x = p0.getX();
		double y = p0.getY();
		float diff = -Float.MAX_VALUE;
		float z = 0;
		if (n != (int)(3*(lineLength+1)/step))
			return(false);
		for (int i=2; i<n; i+=3) {
			z = (float)(demFactory.getZ(x, y));
			diff = Math.max(diff, Math.abs(z-vertex[i]));
			x += dx;
			y += dy;
		}
		if (diff < 1) {
			System.err.println("LandscapeTest.testGetVertices: Line between "+p0+" and  "+p1+" returned "+n+" values, max error = "+diff);
			return(true);
		}
		return(false);
	}
	
	private boolean testGetSampledMeanElevationOfRegion(Landscape landscape) {
		Vector3[] vertex = new Vector3[] {new Vector3(0,0,0), new Vector3(10,0, 0), new Vector3(10,10,0), new Vector3(0,10,0), new Vector3(0,0,0)};
		Vector3 lowerBound = new Vector3(0,0,0);
		Vector3 upperBound = new Vector3(10,10,0);
		double elev = landscape.getSampledMeanElevationOfRegion(vertex, lowerBound, upperBound);
		double z = 0;
		for (int r=0; r<10; ++r) {
			for (int c=0; c<10; ++c) {
				z += demFactory.getZ(c, r);
			}
		}
		z /= 100;
		System.err.println("LandscapeTest.testGetSampledMeanElevationOfRegion 10x10 region = "+elev+" "+z);
		return(Math.abs(elev-z) < 0.0000001);
	}
	
	private boolean testGetSampledMeanSlopeOfRegion(Landscape landscape) {
		Vector3[] vertex = new Vector3[] {new Vector3(0,0,0), new Vector3(10,0, 0), new Vector3(10,10,0), new Vector3(0,10,0), new Vector3(0,0,0)};
		Vector3 lowerBound = new Vector3(0,0,0);
		Vector3 upperBound = new Vector3(10,10,0);
		double sampledSlope = landscape.getSampledMeanSlopeOfRegion(vertex, lowerBound, upperBound);
		double slope = 0;
		Vector3 normal = new Vector3();
		Vector3 v0 = new Vector3();
		Vector3 v1 = new Vector3();
		Vector3 v2 = new Vector3();
		Vector3 meanNormal = new Vector3();
		Vector3 work = new Vector3();
		for (int r=0; r<10; ++r) {
			for (int c=0; c<10; ++c) {
				v0.set(c, r, demFactory.getZ(c, r));
				v1.set(c+1, r, demFactory.getZ(c+1, r));
				v2.set(c+1, r+1, demFactory.getZ(c+1, r+1));
				MathUtil.createNormal(normal, v0, v1, v2, work);
				meanNormal.addLocal(normal);
			}
		}
		meanNormal.multiplyLocal(0.01);
		slope = MathUtil.getSlopeFromNormal(meanNormal);
		System.err.println("LandscapeTest.testGetSampledMeanSlopeOfRegion 10x10 region = "+slope+" "+sampledSlope);
		return((int)sampledSlope == (int)slope);
	}
	
	private boolean testGetSampledVolumeOfRegion(Landscape landscape) {
		System.err.println("Landscape Minimum Elevation: "+landscape.getMinimumElevation());
		System.err.println("Landscape Maximum Elevation: "+landscape.getMaximumElevation());
		int numPix = 256;
		double zVal = 0-landscape.getMinimumElevation();
		Vector3 lowerBound = new Vector3(-numPix, -numPix, zVal);
		Vector3 upperBound = new Vector3(numPix, numPix, zVal);
		Vector3[] vertex = new Vector3[] {new Vector3(lowerBound), new Vector3(upperBound.getX(),lowerBound.getY(),lowerBound.getZ()), new Vector3(upperBound), new Vector3(lowerBound.getX(),upperBound.getY(),lowerBound.getZ()), new Vector3(lowerBound)};
		ArrayList<ReadOnlyVector3> pointList = new ArrayList<ReadOnlyVector3>();
		for (int i=0; i<vertex.length; ++i)
			pointList.add(vertex[i]);
		Tessellator tessellator = new Tessellator();
		FloatBuffer vertexBuffer = tessellator.tessellate(pointList, null);
		Mesh polygon = new Mesh("_polygon");
		vertexBuffer.rewind();
		FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(vertexBuffer.capacity());
		normalBuffer.rewind();
		MathUtil.computePolygonNormal(vertexBuffer, normalBuffer, true);
		polygon.getMeshData().setVertexBuffer(vertexBuffer);
		polygon.getMeshData().setNormalBuffer(normalBuffer);
		polygon.getSceneHints().setAllPickingHints(true);
		polygon.setModelBound(new BoundingBox());
		polygon.markDirty(DirtyType.Bounding);
		polygon.updateModelBound();
		polygon.updateGeometricState(0);
		double[] sampledVolume = landscape.getSampledVolumeOfRegion(vertex, lowerBound, upperBound, polygon);
		double volumeAbove = 0;
		double volumeBelow = 0;
		for (int r=-numPix; r<numPix; ++r) {
			for (int c=-numPix; c<numPix; ++c) {
				double z = demFactory.getZ(c, r);
				if (z > 0)
					volumeAbove += (z);
				else
					volumeBelow -= (z);
			}
		}
		System.err.println("LandscapeTest.testGetSampledVolumeOfRegion "+numPix+"x"+numPix+" region = above:"+volumeAbove+"="+sampledVolume[0]+" below:"+volumeBelow+"="+sampledVolume[1]);
		return(((int)sampledVolume[0] == (int)volumeAbove) && ((int)sampledVolume[1] == (int)volumeBelow));
	}
	
	private boolean testGetSampledSurfaceAreaOfRegion(Landscape landscape) {
		double zVal = -landscape.getMinimumElevation();
		Vector3[] vertex = new Vector3[] {new Vector3(0,0,zVal), new Vector3(10,0,zVal), new Vector3(10,10,zVal), new Vector3(0,10,zVal), new Vector3(0,0,zVal)};
		Vector3 lowerBound = new Vector3(0,0,zVal);
		Vector3 upperBound = new Vector3(10,10,zVal);
		double sampledSurfaceArea = landscape.getSampledSurfaceAreaOfRegion(vertex, lowerBound, upperBound);
		double surfaceArea = 0;
		double xd = 0.5;
		double yd = 0.5;
		for (int r=0; r<10; ++r) {
			for (int c=0; c<10; ++c) {
				double x = c+xd;
				double y = r+yd;
				surfaceArea += getAreaOfTriangle(x, y, x - xd, y + yd, x, y + yd);
				surfaceArea += getAreaOfTriangle(x, y, x + xd, y + yd, x, y + yd);
				surfaceArea += getAreaOfTriangle(x, y, x - xd, y, x - xd, y + yd);
				surfaceArea += getAreaOfTriangle(x, y, x + xd, y, x + xd, y + yd);
				surfaceArea += getAreaOfTriangle(x, y, x - xd, y, x - xd, y - yd);
				surfaceArea += getAreaOfTriangle(x, y, x + xd, y, x + xd, y - yd);
				surfaceArea += getAreaOfTriangle(x, y, x - xd, y - yd, x, y - yd);
				surfaceArea += getAreaOfTriangle(x, y, x + xd, y - yd, x, y - yd);
			}
		}
		System.err.println("LandscapeTest.testGetSampledSurfaceAreaOfRegion 10x10 region = "+surfaceArea+" "+sampledSurfaceArea);
		return((int)sampledSurfaceArea == (int)surfaceArea);
	}

	private double getAreaOfTriangle(double x0, double y0, double x1, double y1, double x2, double y2) {
		double z0 = demFactory.getZ(x0, y0);
		double z1 = demFactory.getZ(x1, y1);
		double z2 = demFactory.getZ(x2, y2);
		double area = MathUtil.getAreaOfTriangle(x0, y0, z0, x1, y1, z1, x2, y2, z2);
		return(area);
	}
	
	private boolean testGetSampledDifferenceOfRegion(Landscape landscape) {
		double zVal = -landscape.getMinimumElevation();
		Vector3[] vertex = new Vector3[] {new Vector3(0,0,zVal), new Vector3(10,0,zVal), new Vector3(10,10,zVal), new Vector3(0,10,zVal), new Vector3(0,0,zVal)};
		Vector3 lowerBound = new Vector3(0,0,zVal);
		Vector3 upperBound = new Vector3(10,10,zVal);
		float[][] diff = new float[10][10];
		float[] minMaxElev = new float[2];
		double[] planeEq = MathUtil.getPlaneFromPointAndNormal(vertex[0], Vector3.UNIT_Z, null);
		int[] diffDim = landscape.getSampledDifferenceOfRegion(vertex, lowerBound, upperBound, planeEq, 1, diff, minMaxElev);
		for (int r=0; r<10; ++r) {
			for (int c=0; c<10; ++c) {
				double z = demFactory.getZ(c, r);
				if ((int)z != (int)diff[r][c])
					return(false);
			}
		}
		System.err.println("LandscapeTest.testGetSampledDifferenceOfRegion "+diffDim[0]+" x "+diffDim[1]+" region.");
		return(true);
	}
}
