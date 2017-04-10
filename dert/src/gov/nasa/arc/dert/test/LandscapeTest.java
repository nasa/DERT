package gov.nasa.arc.dert.test;

import gov.nasa.arc.dert.io.FileSystemTileSource;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.LayerManager;
import gov.nasa.arc.dert.landscape.factory.LayerFactory;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.Tessellator;

import java.awt.Color;
import java.io.File;
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
	
	public boolean testLandscape(String testLoc) {
		
		System.err.println("Create "+testLoc);
		File testDir = new File(testLoc);
		if (!testDir.exists()) {
			if (!testDir.mkdir())
				return(false);
			File dertDir = new File(testLoc, "dert");
			if (!dertDir.mkdir())
				return(false);
		}
		
		System.err.println("Create DEM");
		demFactory = new TestDemFactory(1024);
		if (!demFactory.createDem(testLoc+"/testdem.tif"))
			return(false);
		
		System.err.println("Create layer");
		String[] args = new String[] {"-landscape="+testLoc, "-file="+testLoc+"/testdem.tif", "-tilesize=128", "-type=elevation"};
		LayerFactory lf = new LayerFactory(args);
		if (!lf.createLayer())
			return(false);
		
		System.err.println("Create tile source");
		FileSystemTileSource tileSource = new FileSystemTileSource(testLoc);
		if (!tileSource.connect("dert", "dert")) {
			return (false);
		}
		String[][] layerInfo = tileSource.getLayerInfo();

		if (layerInfo.length == 0)
			return (false);

		System.err.println("Create layer manager");
		LayerManager layerManager = new LayerManager();
		if (!layerManager.initialize(tileSource))
			return (false);

		System.err.println("Create landscape");
		Landscape landscape = Landscape.createInstance(tileSource, layerManager, Color.white);
		landscape.initialize();
		
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
