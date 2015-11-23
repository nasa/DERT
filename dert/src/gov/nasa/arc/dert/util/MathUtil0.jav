package gov.nasa.arc.dert.util;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.World;

import java.nio.FloatBuffer;
import java.util.List;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.intersection.IntersectionRecord;
import com.ardor3d.intersection.PickData;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Quaternion;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;

/**
 * Provides math helper methods.
 *
 */
public class MathUtil {
	
	private Vector3 tmpDir = new Vector3();
	private Matrix3 mat = new Matrix3();
	private Vector3 meanNormal = new Vector3();
    private Vector3 areaVec = new Vector3();
	private Vector3 vec0 = new Vector3();
	private Vector3 vec1 = new Vector3();
	private Vector3 vec2 = new Vector3();
	private Vector3 avgNormal = new Vector3();
	
	public MathUtil() {
		// nothing here
		}
	
	public double getTiltFromDirection(ReadOnlyVector3 dir) {
		double tilt = Vector3.UNIT_X.smallestAngleBetween(dir);
		if (dir.getY() < 0)
			tilt = -tilt;
		System.err.println("MathUtil.getTiltFromDirection "+dir+" "+Math.toDegrees(tilt));
		return(tilt);
	}
	
	public double[] directionToAzEl(ReadOnlyVector3 direction, double[] angle) {
		if (angle == null)
			angle = new double[3];
		double  azAngle = 0;
		
		// Get the Azimuth
		if ((Math.abs(direction.getX()) > 0.0000001) || (Math.abs(direction.getY()) > 0.0000001)) {		
			// Use only the x and y components for rotation around the Z axis
			tmpDir.set(direction);
			tmpDir.setZ(0);
			tmpDir.normalizeLocal();
			azAngle = Vector3.UNIT_Y.smallestAngleBetween(tmpDir);
			if (tmpDir.getX() > 0)
				azAngle = -azAngle;
		}
		
		// Get the Elevation
		// Rotate the vector into the North plane
		mat.fromAngleNormalAxis(-azAngle, Vector3.UNIT_Z);
		mat.applyPost(direction, tmpDir);
		tmpDir.setX(tmpDir.getY());
		tmpDir.setY(tmpDir.getZ());
		tmpDir.setZ(0);
		tmpDir.normalizeLocal();
		double tiltAngle = Vector3.UNIT_X.smallestAngleBetween(tmpDir);
		if (tmpDir.getY() < 0)
			tiltAngle = -tiltAngle;
		
		angle[0] = azAngle;
		angle[1] = tiltAngle;
		angle[2] = 0;
		return(angle);		
	}
	
	public static Vector3 azElToPoint(double az, double el, ReadOnlyVector3 startVector, ReadOnlyVector3 azAxis, ReadOnlyVector3 elAxis, Vector3 result) {
		if (result == null)
			result = new Vector3();
		Matrix3 mat = new Matrix3();
		mat.fromAngleAxis(az, azAxis);
		Matrix3 mat2 = new Matrix3();
		mat2.fromAngleAxis(el, elAxis);
		mat.multiplyLocal(mat2);
		result.set(startVector);
		mat.applyPost(result, result);
		return(result);
	}
	
//	public double[] directionToAzEl(ReadOnlyVector3 direction, double[] angle) {
//		System.err.println("MathUtil.directionToAzEl "+direction);
//		if (angle == null)
//			angle = new double[3];
//		double  azAngle = 0;
//		
//		// Get the Azimuth
//		if ((Math.abs(direction.getX()) > 0.0000001) || (Math.abs(direction.getY()) > 0.0000001)) {		
//			// Use only the x and y components for rotation around the Z axis
//			tmpDir.set(direction);
//			tmpDir.setZ(0);
////			tmpDir.normalizeLocal();	
//			// Create the rotation matrix from North to direction
//			mat.fromStartEndLocal(Vector3.UNIT_Y, tmpDir);
//			// Get angle for Z axis
//			mat.toAngles(angle);
//			azAngle = angle[2];
////			System.err.println("MathUtil.directionToAzEl Az "+Math.toDegrees(angle[0])+" "+Math.toDegrees(angle[1])+" "+Math.toDegrees(angle[2])+" "+tmpDir);
//			azAngle = getHeadingFromDirection(tmpDir);
//		}
//		
//		// Get the Elevation
//		double elAngle = 0;
//		// Rotate the vector into the North plane
//		mat.fromAngleNormalAxis(-azAngle, Vector3.UNIT_Z);
//		mat.applyPost(direction, tmpDir);
//		tmpDir.normalizeLocal();
//		System.err.println("MathUtil.directionToAzEl El 1 "+tmpDir);
//		tmpDir.setX(0);
//		// Create the rotation matrix from South to direction
//		mat.fromStartEndLocal(Vector3.UNIT_Y, tmpDir);
//		mat.toAngles(angle);
//		elAngle = angle[0];
//		System.err.println("MathUtil.directionToAzEl El 2 "+Math.toDegrees(angle[0])+" "+Math.toDegrees(angle[1])+" "+Math.toDegrees(angle[2]));
//		tmpDir.setX(tmpDir.getY());
//		tmpDir.setY(tmpDir.getZ());
//		tmpDir.setZ(0);
//		elAngle = getTiltFromDirection(tmpDir);
//		angle[0] = azAngle;
//		angle[1] = elAngle;
//		angle[2] = 0;
//		return(angle);
//	}
	
//	public void directionToAzElFromAxis(ReadOnlyVector3 dir, ReadOnlyVector3 axis, float[] angles) {
//		// Azimuth
//		tmpDir.set(dir);
//		tmpDir.setZ(0);
//		tmpDir.normalizeLocal();
//		System.err.println("MathUtil.directionToAzElFromAxis "+dir+" "+tmpDir+" "+axis+" "+signOf(dir.getZ()));
//		mat.fromStartEndLocal(axis, tmpDir);
//		kumQuat.fromRotationMatrix(mat);
//		angles[0] = (float)(signOf(dir.getX())*kumQuat.toAngleAxis(tmpUp));
//		//System.err.println("MathUtil.directionToAsEl Az "+tmpUp+" "+Math.toDegrees(angles[0]));
//		// Elevation
//		tmpDir.set(dir);		
//		mat.invertLocal();
//		mat.applyPost(tmpDir, tmpDir);
////		System.err.println("MathUtil.directionToAzElFromAxis "+tmpDir);
//		
//		tmpDir.setX(0);
//		tmpDir.normalizeLocal();
//		mat.fromStartEndLocal(axis, tmpDir);
//		kumQuat.fromRotationMatrix(mat);
//		angles[1] = (float)(signOf(dir.getZ())*kumQuat.toAngleAxis(tmpUp));
//		//System.err.println("MathUtil.directionToAsEl El "+tmpUp+" "+Math.toDegrees(angles[1]));
//	}
	
	public static double signOf(double v) {
		if (v == 0)
			return(1);
		else
			return(Math.signum(v));
	}
	
	public double log2(double d) {
		return Math.log(d)/Math.log(2.0);
		}
	
	public boolean isInsidePolygon(ReadOnlyVector3 p, ReadOnlyVector3[] vertex) {
		return(windingNumber(p, vertex) != 0);
		}
	
	private final int windingNumber(ReadOnlyVector3 p, ReadOnlyVector3[] vertex) {
		int wNumber = 0;
		for (int i=0; i<vertex.length-1; i++) {
			if (vertex[i].getY() <= p.getY()) {
				if (vertex[i+1].getY() > p.getY()) {
					if (isLeft(vertex[i],vertex[i+1],p) > 0)
						wNumber ++;
					}
				}
			else {
				if (vertex[i+1].getY() <= p.getY()) {
					if (isLeft(vertex[i],vertex[i+1],p) < 0)
						wNumber --;
					}
		        }
		    }
		return wNumber;
		}
	
	private final float isLeft(ReadOnlyVector3 p0, ReadOnlyVector3 p1, ReadOnlyVector3 p2) {
	    return((float)((p1.getX()-p0.getX())*(p2.getY()-p0.getY())-(p2.getX()-p0.getX())*(p1.getY()-p0.getY())));
		}

    /**
     * Given 3 points in a 2d plane, this function computes if the points going from A-B-C
     * are moving counter clock wise.
     * @param p0 Point 0.
     * @param p1 Point 1.
     * @param p2 Point 2.
     * @return 1 If they are CCW, -1 if they are not CCW, 0 if p2 is between p0 and p1.
     */
    public static int counterClockwise(Vector2 p0, Vector2 p1, Vector2 p2){
        double dx1,dx2,dy1,dy2;
        dx1=p1.getX()-p0.getX();
        dy1=p1.getY()-p0.getY();
        dx2=p2.getX()-p0.getX();
        dy2=p2.getY()-p0.getY();
        if (dx1*dy2>dy1*dx2) return 1;
        if (dx1*dy2<dy1*dx2) return -1;
        if ((dx1*dx2 < 0) || (dy1*dy2 <0)) return -1;
        if ((dx1*dx1+dy1*dy1) < (dx2*dx2+dy2*dy2)) return 1;
        return 0;
    }
    
    public static boolean isCounterClockwise(List<ReadOnlyVector3> points) {
    	double area = computePolygonArea2D(points);
    	if (area < 0)
    		return(false);
    	return(true);
    }

    /**
     * Returns the area enclosed by the specified (x, y) points on a plane (the z coordinates are ignored).
     * @param points the (x, y) points which define the 2D polygon.
     * @return the area enclosed by the specified coordinates.
     */
    public static double computePolygonArea2D(List<ReadOnlyVector3> points) {
		if (points.size() < 3)
			return(0);
        java.util.Iterator<ReadOnlyVector3> iter = points.iterator();
        if (!iter.hasNext())
            return 0;
        double area = 0;
        ReadOnlyVector3 firstPoint = iter.next();
        ReadOnlyVector3 point = firstPoint;
        while (iter.hasNext()) {
        	ReadOnlyVector3 nextLocation = iter.next();
            area += point.getX() * nextLocation.getY();
            area -= nextLocation.getX() * point.getY();
            point = nextLocation;
        }
        if (!point.equals(firstPoint)) {
            area += point.getX() * firstPoint.getY();
            area -= firstPoint.getX() * point.getY();
        }
        area /= 2.0;
        return area;
    }
    
    public static double computeSurfaceArea(List<ReadOnlyVector3> points) {
		if (points.size() < 3)
			return(0);
        java.util.Iterator<ReadOnlyVector3> iter = points.iterator();
        if (!iter.hasNext())
            return 0;
        double area = 0;
        ReadOnlyVector3 firstPoint = iter.next();
        ReadOnlyVector3 point = firstPoint;
        while (iter.hasNext()) {
        	ReadOnlyVector3 nextLocation = iter.next();
            area += point.getX() * nextLocation.getY();
            area -= nextLocation.getX() * point.getY();
            point = nextLocation;
        }
        if (!point.equals(firstPoint)) {
            area += point.getX() * firstPoint.getY();
            area -= firstPoint.getX() * point.getY();
        }
        area /= 2.0;
        return area;    	
    }

	public double computeSurfaceArea(FloatBuffer vertexBuffer) {
		double area = 0;
		vertexBuffer.rewind();
		int n = vertexBuffer.limit()-3;
		computeAverageNormal(vertexBuffer);
		areaVec.set(0, 0, 0);
		for (int i=0; i<n; i+=3) {
			vec0.set(vertexBuffer.get(i), vertexBuffer.get(i+1), vertexBuffer.get(i+2));
			vec1.set(vertexBuffer.get(i+3), vertexBuffer.get(i+4), vertexBuffer.get(i+5));
			vec0.crossLocal(vec1);
			areaVec.addLocal(vec0);
			}
		vec0.set(vertexBuffer.get(n), vertexBuffer.get(n+1), vertexBuffer.get(n+2));
		vec1.set(vertexBuffer.get(0), vertexBuffer.get(1), vertexBuffer.get(2));
		vec0.crossLocal(vec1);
		areaVec.addLocal(vec0);
		area = Math.abs(0.5*avgNormal.dot(areaVec));
		return(area);
		}
	
	/**
	 * Compute the normals for a vertex buffer.
	 * @param vertexBuffer
	 * @param normalBuffer
	 * @param zPos if true, all normals will have a positive Z coordinate
	 */
	public void computeNormals(FloatBuffer vertexBuffer, FloatBuffer normalBuffer, boolean zPos) {
		// compute mean normal of points
		int n = vertexBuffer.limit();
		double count = 0;
		meanNormal.set(0, 0, 0);
		for (int i=0; i<n; i+=9) {
			vec0.set(vertexBuffer.get(i), vertexBuffer.get(i+1), vertexBuffer.get(i+2));
			vec1.set(vertexBuffer.get(i+3), vertexBuffer.get(i+4), vertexBuffer.get(i+5));
			vec2.set(vertexBuffer.get(i+6), vertexBuffer.get(i+7), vertexBuffer.get(i+8));
			vec0.subtractLocal(vec1);
			vec1.subtractLocal(vec2);
			vec0.crossLocal(vec1);
			if (zPos && (vec0.getZ() < 0))
				vec0.negateLocal();
			meanNormal.addLocal(vec0);
			count ++;
			}
		meanNormal.multiplyLocal(1.0/count);
		meanNormal.normalizeLocal();
		// assign normal for each vertex
		for (int i=0; i<vertexBuffer.limit(); i+=3) {
			normalBuffer.put(meanNormal.getXf());
			normalBuffer.put(meanNormal.getYf());
			normalBuffer.put(meanNormal.getZf());		
		}
		normalBuffer.flip();
	}
	
	protected final Vector3 computeAverageNormal(FloatBuffer vertexBuffer) {
		int n = vertexBuffer.limit()-3;
		Vector3 avgNormal = new Vector3(0, 0, 0);
		vec0.set(vertexBuffer.get(0), vertexBuffer.get(1), vertexBuffer.get(2));
		double count = 0;
		for (int i=3; i<n; i+=3) {
			vec1.set(vertexBuffer.get(i), vertexBuffer.get(i+1), vertexBuffer.get(i+2));
			vec2.set(vertexBuffer.get(i+3), vertexBuffer.get(i+4), vertexBuffer.get(i+5));
			vec1.subtract(vec2, vec2);
			vec0.subtract(vec1, vec1);
			vec1.crossLocal(vec2);
			avgNormal.addLocal(vec1);
			count ++;
			}
		avgNormal.multiplyLocal(1.0/count);
		avgNormal.normalizeLocal();
		return(avgNormal);
		}	
	
	public double getSampledSurfaceArea(ReadOnlyVector3[] vertex, int n, Spatial node) {
		double xMin = Double.MAX_VALUE;
		double xMax = -Double.MAX_VALUE;
		double yMin = Double.MAX_VALUE;
		double yMax = -Double.MAX_VALUE;
		double zMax = -Double.MAX_VALUE;
		for (int i=0; i<vertex.length; ++i) {
			xMin = Math.min(xMin, vertex[i].getX());
			xMax = Math.max(xMax, vertex[i].getX());
			yMin = Math.min(yMin, vertex[i].getY());
			yMax = Math.max(yMax, vertex[i].getY());
			zMax = Math.max(zMax, vertex[i].getZ());
			}
		double xd = (xMax-xMin)/n;
		double yd = (yMax-yMin)/n;
		Vector3 vert = new Vector3();
		Vector3 dir = new Vector3(0, 0, -1);
		double surfaceArea = 0;
		for (int i=0; i<n; ++i) {
			for (int j=0; j<n; ++j) {
				vert.set((float)(xMin+j*xd), (float)(yMin+i*yd), (float)zMax);
				if (isInsidePolygon((ReadOnlyVector3)vert, vertex)) {
					float el = getElevation(vert, dir, node);
					if (!Float.isNaN(el))
						surfaceArea += xd*yd;
					}
				}
			}		
		return(surfaceArea);
		}
	
	private float getElevation(Vector3 p0, Vector3 dir, Spatial node) {
        // Create a ray starting from the point, and going in the given direction
	    PrimitivePickResults pr = new PrimitivePickResults();
		final Ray3 mouseRay = new Ray3(p0, dir);
		pr.setCheckDistance(true);
		PickingUtil.findPick(node, mouseRay, pr);
		if (pr.getNumber() == 0)
			return(Float.NaN);
        PickData closest = pr.getPickData(0);
        for (int i=1; i<pr.getNumber(); ++i) {
        	PickData pd = pr.getPickData(i);
        	if (closest.getIntersectionRecord().getClosestDistance() > pd.getIntersectionRecord().getClosestDistance())
        		closest = pd;
        	}
		double dist = closest.getIntersectionRecord().getClosestDistance();
		return((float)dist);		
		}
	
//	public double getSampledVolume(Vector3[] vertex, int n, double z, Spatial node) {
//		double xMin = Double.MAX_VALUE;
//		double xMax = -Double.MAX_VALUE;
//		double yMin = Double.MAX_VALUE;
//		double yMax = -Double.MAX_VALUE;
//		for (int i=0; i<vertex.length; ++i) {
//			xMin = Math.min(xMin, vertex[i].getX());
//			xMax = Math.max(xMax, vertex[i].getX());
//			yMin = Math.min(yMin, vertex[i].getY());
//			yMax = Math.max(yMax, vertex[i].getY());
//		}
//		double xd = (xMax-xMin)/n;
//		double yd = (yMax-yMin)/n;
//		Vector3 vert = new Vector3();
//		Vector3 dir = new Vector3(0, 0, 1);
//		double volume = 0;
//		for (int i=0; i<n; ++i) {
//			for (int j=0; j<n; ++j) {
//				vert.set((float)(xMin+j*xd), (float)(yMin+i*yd), (float)z);
//				if (isInsidePolygon(vert, vertex)) {
//					float el = getElevation(vert, dir, node);
//					//System.err.println("MathUtil.getSampledValue "+el);
//					if (!Float.isNaN(el))
//						volume += el*xd*yd;
//				}
//			}
//		}		
//		return(volume);
//	}
	
	public double getSampledVolume(Vector3[] vertex, int n, double z, Spatial node) {
		double xMin = Double.MAX_VALUE;
		double xMax = -Double.MAX_VALUE;
		double yMin = Double.MAX_VALUE;
		double yMax = -Double.MAX_VALUE;
		for (int i=0; i<vertex.length; ++i) {
			xMin = Math.min(xMin, vertex[i].getX());
			xMax = Math.max(xMax, vertex[i].getX());
			yMin = Math.min(yMin, vertex[i].getY());
			yMax = Math.max(yMax, vertex[i].getY());
			}
		double xd = (xMax-xMin)/n;
		double yd = (yMax-yMin)/n;
		Vector3 vert = new Vector3();
//		Vector3 dir = new Vector3(0, 0, 1);
		double volume = 0;
		Landscape landscape = World.getInstance().getLandscape();
		for (int i=0; i<n; ++i) {
			for (int j=0; j<n; ++j) {
				vert.set((float)(xMin+j*xd), (float)(yMin+i*yd), (float)z);
				if (isInsidePolygon(vert, vertex)) {
					double el = landscape.getElevation(vert.getX(), vert.getY());
//					float el = mathutil.getElevation(vert, dir, node);
//					System.err.println("Path.getSampledValue "+el+" "+vert);
					if (!Double.isNaN(el))
						volume += (el-z)*xd*yd;
					}
				}
			}		
		return(volume);
	}
	
	public float[] computeProfile(float[] vertex, Vector3 p0, Vector3 p1, int n, Node node) {
		double xd = (p1.getX()-p0.getX())/(n-1);
		double yd = (p1.getY()-p0.getY())/(n-1);
		Vector3 p = p0.clone();
		Vector3 dir = new Vector3(0, 0, -1);
		for (int i=0; i<n; i+=3) {
			vertex[i] = (float)p.getX();
			vertex[i+1] = (float)p.getY();
			vertex[i+2] = getElevation(p, dir, node);
			p.setX(p.getX()+xd);
			p.setY(p.getY()+yd);
			}
		return(vertex);
		}
	
	public final boolean getBarycentricFromXY(Vector3 p, Vector3 p0, Vector3 p1, Vector3 p2, Vector3 result) {
		if ((p.getX() == p0.getX()) && (p.getY() == p0.getY())) {
			result.set(1, 0, 0);
			return(true);
			}
		else if ((p.getX() == p1.getX()) && (p.getY() == p1.getY())) {
			result.set(0, 1, 0);
			return(true);
			}
		else if ((p.getX() == p2.getX()) && (p.getY() == p2.getY())) {
			result.set(0, 0, 1);
			return(true);
			}
		
		double a = p0.getX()-p2.getX();
		double b = p1.getX()-p2.getX();
		double c = p0.getY()-p2.getY();
		double d = p1.getY()-p2.getY();		
		double det = a*d-b*c;
		if (det == 0) {
			System.err.println("MathUtil.getBaricentricFromXY determinate is 0");
			return(false);
		}
		
		b = p.getX()-p2.getX();
		c = p.getY()-p2.getY();
		
		double l0 = (d*b+(p2.getX()-p1.getX())*c)/det;
		double l1 = ((p2.getY()-p0.getY())*b+a*c)/det;
		double l2 = 1-l0-l1;
		
		result.set(l0, l1, l2);
		// check if point is in triangle
		if ((l0 < 0) || (l0 > 1) || (l1 < 0) || (l1 > 1) || (l2 < 0) || (l2 > 1))
			return(false);
		else
			return(true);
		}
	
	public final boolean getBarycentricFromXYZ(Vector3 p, Vector3 p0, Vector3 p1, Vector3 p2, Vector3 result) {
		if ((p.getX() == p0.getX()) && (p.getY() == p0.getY()) && (p.getZ() == p0.getZ())) {
			result.set(1, 0, 0);
			return(true);
			}
		else if ((p.getX() == p1.getX()) && (p.getY() == p1.getY()) && (p.getZ() == p1.getZ())) {
			result.set(0, 1, 0);
			return(true);
			}
		else if ((p.getX() == p2.getX()) && (p.getY() == p2.getY()) && (p.getZ() == p2.getZ())) {
			result.set(0, 0, 1);
			return(true);
			}
		double a = p0.getX()-p2.getX();
		double b = p1.getX()-p2.getX();
		double c = p2.getX()-p.getX();
		double d = p0.getY()-p2.getY();
		double e = p1.getY()-p2.getY();
		double f = p2.getY()-p.getY();
		double g = p0.getZ()-p2.getZ();
		double h = p1.getZ()-p2.getZ();
		double i = p2.getZ()-p.getZ();
//		double t = 0;
		// handle case where triangle is perpendicular to X axis
		if ((a == 0) && (b == 0)) {
			System.err.println("MathUtil.getBarycentricFromXY a == 0 && b == 0 "+p0+" "+p1+" "+p2);
			return(false);
			}
		/*
		else if ((a == 0) || (b == 0)) {
			//System.err.println("MathUtil.getBarycentricFromXY a or b zero "+p0+" "+p1+" "+p2);
			t = a;
			a = d;
			d = t;
			t = b;
			b = e;
			e = t;
			t = c;
			c = f;
			f = t;
			}
			*/
		if (a == 0) {
			a = 0.000001;
			//System.err.println("MathUtil.getBarycentricFromXY a == 0 "+p0+" "+p1+" "+p2);
			}
		if (b == 0) {
			b = 0.000001;
			//System.err.println("MathUtil.getBarycentricFromXY b == 0 "+p0+" "+p1+" "+p2);
			}
		double den = a*(e+h)-b*(d+g);
		// check for a line
		if (den == 0) {
			result.set(0, 0, 0);
			System.err.println("MathUtil.getBarycentricFromXYZ denW1 == 0 "+p0+" "+p1+" "+p2);
			return(false);
			}
		double w1 = (b*(f+i)-c*(e+h))/den;
		den = b*(d+g)-a*(e+h);
		// check for a line
		if (den == 0) {
			result.set(0, 0, 0);
			System.err.println("MathUtil.getBarycentricFromXYZ denW2 == 0 "+p0+" "+p1+" "+p2);
			return(false);
			}
		double w2 = (a*(f+i)-c*(d+g))/den;
		double w3 = 1-w1-w2;
		result.set(w1, w2, w3);
		// check if point is in triangle
		if ((w1 < 0) || (w2 < 0) || (w3 < 0))
			return(false);
		else
			return(true);
		}

    /**
     * <code>angleBetween</code> returns (in radians) the angle between two vectors.
     * It is assumed that both this vector and the given vector are normalized unit vectors.
     * 
     * @param otherVector a unit vector to find the angle against
     * @return the angle in radians.
     */
    public static double angleBetween(ReadOnlyVector3 v0, Vector3 v1) {
        return(Math.acos(v0.dot(v1)));
    	}

    private Vector3 tmpNormVec = new Vector3();
    public boolean createNormal(Vector3 norm, ReadOnlyVector3 v0, ReadOnlyVector3 v1, ReadOnlyVector3 v2) {
    	if (Double.isNaN(v0.getZ()) || Double.isNaN(v1.getZ()) || Double.isNaN(v2.getZ())) {
    		norm.set(0, 0, 0);
    		return(false);
    		}
    	norm.set(v1);
    	norm.subtractLocal(v0);
    	tmpNormVec.set(v2);
    	tmpNormVec.subtractLocal(v0);
        norm.crossLocal(tmpNormVec);
        norm.normalizeLocal();
        return(true);
    	}
	
	public double[] computeScaleForMesh(Mesh mesh) {
		Vector3 extent = SpatialUtil.getExtent(mesh);
		double xExtent = 2*extent.getX();
		double nx = Math.pow(10, Math.floor(Math.log10(xExtent))-3);
		double yExtent = 2*extent.getY();
		double ny = Math.pow(10, Math.floor(Math.log10(yExtent))-3);
		double zExtent = 2*extent.getZ();
		double nz = Math.pow(10, Math.floor(Math.log10(zExtent))-3);
		double[] scale = new double[] {nx, ny, nz};
		return(scale);
		}
	
	public static int getNextPowerOf2(int val) {
        int po2 = 2;
        while (po2 < val)
        	po2 *= 2;
        return(po2);
	}
	
	public static boolean isConvex(int n, Vector3[] vl) {
		float angleSum = 0;
		Vector3 e1 = new Vector3();
		Vector3 e2 = new Vector3();
		for (int i=0; i<n; ++i) {
			if (i == 0)
				vl[n-1].subtract(vl[i], e1);
			else
				vl[i-1].subtract(vl[i], e1);
			
			if (i == n-1)
				vl[0].subtract(vl[i], e2);
			else
				vl[i+1].subtract(vl[i], e2);
			e1.normalizeLocal();
			e2.normalizeLocal();
			double dot = e1.dot(e2);
			double theta = Math.acos(dot);
			angleSum += theta;
		}
		double convexAngleSum = (n-2)*Math.PI/2;
		if (angleSum < (convexAngleSum-n*0.00001))
			return(false);
		return(true);
	}
/*	
	private Vector3 e1 = new Vector3();
	private Vector3 e2 = new Vector3();
	private Vector3 h = new Vector3();
	private Vector3 s = new Vector3();
	private Vector3 q = new Vector3();	
	private static double epsilon = 1e-15;
	public boolean intersectsTriangle(Vector3 p0, Vector3 p1, Vector3 p2, Vector3 pos, Vector3 normal, Ray3 pickRay) {

        // get triangle edge vectors and plane normal
        p1.subtract(p0, e1);
        p2.subtract(p0, e2);
        e1.cross(e2, normal);
        normal.normalizeLocal();
        pickRay.getDirection().cross(e2, h);
        
        double a = e1.dot(h);
        if ((a > -0.0000001) && (a < 0.0000001)) {
        	//System.err.println("SelectionHandler.intersectsTriangle a = "+a);
        	return(false);
        }
        
        double f = 1/a;
        pickRay.getOrigin().subtract(p0, s);
        double u = f*s.dot(h);
        if ((u < 0.0) || (u > 1.0)) {
        	//System.err.println("SelectionHandler.intersectsTriangle u = "+u);
        	return(false);
        }
        
        s.cross(e1, q);
        double v = f*pickRay.getDirection().dot(q);
        if ((v < 0.0) || (u+v > 1.0)) {
        	//System.err.println("SelectionHandler.intersectsTriangle v = "+v+" "+(v+u));
        	return(false);
        }
        
        double t = f*e2.dot(q);
        if (t <= 0.0000001) {
        	//System.err.println("SelectionHandler.intersectsTriangle t = "+t);
        	return(false);
        }

        Vector3 intersect = pickRay.getOrigin().add(pickRay.getDirection().multiply(t, null), null);
        pos.set(intersect);
        //System.err.println("SelectionHandler.intersectsTriangle "+pickRay+" "+t+" "+mesh.getName()+" "+pos);
        return(true);
	}
*/	
	private Vector3 e1 = new Vector3();
	private Vector3 e2 = new Vector3();
	private Vector3 h = new Vector3();
	private Vector3 s = new Vector3();
	private Vector3 q = new Vector3();	
	private static double epsilon = 1e-7;
	public boolean intersectsTriangle(Vector3 p0, Vector3 p1, Vector3 p2, Vector3 pos, Vector3 normal, Ray3 pickRay) {

        // get triangle edge vectors and plane normal
        p1.subtract(p0, e1);
        p2.subtract(p0, e2);
        
        e1.cross(e2, normal);
        normal.normalizeLocal();
        
        pickRay.getDirection().cross(e2, h);
        
        double a = e1.dot(h);
        if (Math.abs(a) < epsilon) {
        	//System.err.println("SelectionHandler.intersectsTriangle a = "+a);
        	return(false);
        }
        
        double f = 1/a;
        pickRay.getOrigin().subtract(p0, s);
        double u = f*s.dot(h);
        if ((u < 0.0) || (u > 1.0)) {
        	//System.err.println("SelectionHandler.intersectsTriangle u = "+u);
        	return(false);
        }
        
        s.cross(e1, q);
        double v = f*pickRay.getDirection().dot(q);
        if ((v < 0.0) || (u+v > 1.0)) {
        	//System.err.println("SelectionHandler.intersectsTriangle v = "+v+" "+(v+u));
        	return(false);
        }
        
        double t = f*e2.dot(q);
        if (t < 0.0) {
        	//System.err.println("SelectionHandler.intersectsTriangle t = "+t);
        	return(false);
        }

        Vector3 intersect = pickRay.getOrigin().add(pickRay.getDirection().multiply(t, null), null);
        pos.set(intersect);
        //System.err.println("SelectionHandler.intersectsTriangle "+pickRay+" "+t+" "+mesh.getName()+" "+pos);
        return(true);
	}
	
	public boolean intersectsQuad(Vector3 p0, Vector3 p1, Vector3 p2, Vector3 p3, Vector3 pos, Vector3 normal, Ray3 pickRay) {
		//System.err.println("SelectionHandler.intersectsQuad "+i0+" "+i1+" "+i2+" "+i3);
		if (intersectsTriangle(p0, p1, p2, pos, normal, pickRay))
			return(true);
		if (intersectsTriangle(p2, p1, p3, pos, normal, pickRay))
			return(true);
		return(false);
	}
	
	public static double[] getDoubleArrayFromString(String str, String sep) {
		if (str == null)
			return(null);
		String[] token = str.split(sep);
		double[] data = new double[token.length];
		for (int i=0; i<token.length; ++i)
			data[i] = Double.parseDouble(token[i]);
		return(data);
	}
	
	public static Vector3 getVector3FromString(String str, String sep) {
		if (str == null)
			return(null);
		String[] token = str.split(sep);
		Vector3 vec = null;
		if (token.length < 3)
			System.out.println("Number of vector components < 3.");
		else
			vec = new Vector3(Double.parseDouble(token[0]), Double.parseDouble(token[1]), Double.parseDouble(token[2]));
		return(vec);
	}
	
	public static int getVector3FromString(String str, String sep, Vector3 vec) {
		if (str == null)
			return(0);
		try {
			String[] token = str.split(sep);
			switch (token.length) {
			case 0:
				return(0);
			case 1:
				vec.set(Double.parseDouble(token[0]), 0, 0);			
				return(1);
			case 2:
				vec.set(Double.parseDouble(token[0]), Double.parseDouble(token[1]), 0);
				return(2);
			case 3:
				vec.set(Double.parseDouble(token[0]), Double.parseDouble(token[1]), Double.parseDouble(token[2]));
				return(3);
			}
		}
		catch (Exception e) {
			// error in location string
		}
		return(0);
	}
	
	public static Quaternion getQuaternionFromString(String str, String sep) {
		if (str == null)
			return(null);
		String[] token = str.split(sep);
		if (token.length < 4) {
			System.out.println("Number of quaternion components < 4.");
			return(null);
		}
		Quaternion kumquat = new Quaternion(Double.parseDouble(token[0]), Double.parseDouble(token[1]), Double.parseDouble(token[2]), Double.parseDouble(token[3]));
		return(kumquat);
	}
	
	public static Quaternion getQuaternionFromStringWFirst(String str, String sep) {
		if (str == null)
			return(null);
		String[] token = str.split(sep);
		if (token.length < 4) {
			System.out.println("Number of quaternion components < 4.");
			return(null);
		}
		Quaternion kumquat = new Quaternion(Double.parseDouble(token[1]), Double.parseDouble(token[2]), Double.parseDouble(token[3]), Double.parseDouble(token[0]));
		return(kumquat);
	}
	
	public static Vector2 getVector2FromString(String str, String sep) {
		if (str == null)
			return(null);
		String[] token = str.split(sep);
		if (token.length < 2) {
			System.out.println("Number of vector components < 2.");
			return(null);
		}
		Vector2 vec = new Vector2(Double.parseDouble(token[0]), Double.parseDouble(token[1]));
		return(vec);
	}
	
	public static long nextPowerOf2(long val) {
		long po2 = 1;
		while (po2 < val)
			po2 *= 2;
		return(po2);
	}
	
	public static void degreeToRadian(double[] ll) {
		if (ll == null)
			return;
		for (int i=0; i<ll.length; ++i) {
			ll[i] = Math.toRadians(ll[i]);
		}
	}
	
	public static void radianToDegree(double[] ll) {
		if (ll == null)
			return;
		for (int i=0; i<ll.length; ++i) {
			ll[i] = Math.toDegrees(ll[i]);
		}
	}
/*	
	public static void clipLonLat(double[] llb) {
		for (int i=0; i<llb.length; i+=2) {
			if (llb[i] < 0) {
				System.out.print("Found longitude of "+llb[i]+" degrees ...");
				llb[i] += 360;
				System.out.println(" setting to "+llb[i]+" degrees.");
			}
		}
	}
*/	
	
	public static double fixAngle(double angle) {
		if (angle > 180)
			return(angle-360);
		return(angle);
	}
	
	public static void clipLonLat(double[] llb) {
		for (int i=0; i<llb.length; i+=2) {
			if (llb[i] > 180) {
				System.out.print("Found longitude of "+llb[i]+" degrees ...");
				llb[i] -= 360;
				System.out.println(" setting to "+llb[i]+" degrees.");
			}
		}
	}
	
	public static double clipLon(double lon) {
		if (lon > 180) {
			System.out.print("Found longitude of "+lon+" degrees ...");
			lon -= 360;
			System.out.println(" setting to "+lon+" degrees.");
		}
		return(lon);
	}
	
	public static int powerOf2AsInt(int val) {
		return((int)(Math.log(val)/Math.log(2)+0.5));
	}
	
	public static double powerOf2(double val) {
		return(Math.log(val)/Math.log(2));
	}

	public static final int bytes2Int(byte b0, byte b1, byte b2, byte b3) {
		return(((b0 & 0xff) << 24) | ((b1 & 0xff) << 16) | ((b2 & 0xff) << 8) | (b3 & 0xff));
	}

	public static final byte[] int2Bytes(int val, byte[] bArray) {
		if (bArray == null)
			bArray = new byte[4];
		bArray[0] = (byte)((val >> 24) & 0xff);
		bArray[1] = (byte)((val >> 16) & 0xff);
		bArray[2] = (byte)((val >> 8) & 0xff);
		bArray[3] = (byte)(val & 0xff);
		return(bArray);
	}
	
	public static final long unsignedInt(int val) {
		return((long)(val & 0xFFFFFFFF));
	}
	
	public static final int unsignedShort(short val) {
		return((int)(val & 0xFFFF));
	}
	
	public static final short unsignedByte(byte val) {
		return((short)(val & 0xFF));
	}
    
    public double getSlopeFromNormal(ReadOnlyVector3 normal) {
    	ReadOnlyVector3 unit = Vector3.UNIT_Z;
    	if (normal.getZ() < 0)
    		unit = Vector3.NEG_UNIT_Z;
    	double slope = Math.acos(normal.dot(unit));
    	return(Math.toDegrees(slope));
    }
	
	public double getSlopeFromLine(ReadOnlyVector3 v0, ReadOnlyVector3 v1) {
		double dx = v1.getX()-v0.getX();
		double dy = v1.getY()-v0.getY();
		double dz = v1.getZ()-v0.getZ();
		return((float)(90*Math.abs(dz/Math.sqrt(dx*dx+dy*dy))));
	}
	
	public double getAspectFromNormal(ReadOnlyVector3 normal) {
		return(getAspect(normal.getX(), normal.getY()));
	}
    
    private Vector2 vec2Tmp = new Vector2();
    private double getAspect(double x, double y) {
    	vec2Tmp.set(x, y);
    	vec2Tmp.normalizeLocal();
    	double aspect = vec2Tmp.angleBetween(Vector2.UNIT_Y);
    	if (aspect < 0)
    		aspect += 2*Math.PI;
    	aspect = Math.toDegrees(aspect);
    	return(aspect);
    }
	
	public double getAspectFromLine(ReadOnlyVector3 v0, ReadOnlyVector3 v1) {
		double dx = v1.getX()-v0.getX();
		double dy = v1.getY()-v0.getY();
		return(getAspect(dx, dy));
	}
    
    public static double distanceToSphere(BoundingVolume bs, ReadOnlyVector3 point, ReadOnlyVector3 direction) {
    	IntersectionRecord record = bs.intersectsWhere(new Ray3(point, direction));
    	if (record != null)
    		return(record.getFurthestDistance());
    	else
    		return(bs.getRadius()*2);
    }
    
    public static int getDecimalPlaces(double value) {
    	value = Math.abs(value);
    	if (value > 10)
    		return(0);
    	if (value > 1)
    		return(1);
    	value = Math.log10(value);
    	value = Math.abs(value);
    	int n = (int)Math.ceil(value)+1;
		return(n);
    }
    
    public ReadOnlyVector3 getClosestPointOnLine(ReadOnlyVector3 p0, ReadOnlyVector3 p1, ReadOnlyVector3 p3) {
    	vec0.set(p1);
    	vec0.subtractLocal(p0);
    	vec0.normalizeLocal();
    	vec1.set(p3);
    	vec1.subtractLocal(p0);
    	double t = vec0.dot(vec1);
    	vec0.scaleAddLocal(t, p0);
    	return(vec0);
    }
    
    public double[] getPlaneFrom3Points(ReadOnlyVector3 p0, ReadOnlyVector3 p1, ReadOnlyVector3 p2, double[] result) {
    	vec0.set(p1);
    	vec0.subtractLocal(p0);
    	vec1.set(p2);
    	vec1.subtractLocal(p0);
    	vec0.crossLocal(vec1);
    	vec0.normalizeLocal();
    	if (result == null)
    		result = new double[4];
    	result[0] = vec0.getX();
    	result[1] = vec0.getY();
    	result[2] = vec0.getZ();
    	if (vec0.getZ() == 0)
    		result[3] = Double.NaN;
    	else
    		result[3] = -(vec0.getX()*p0.getX()+vec0.getY()*p0.getY()+vec0.getZ()*p0.getZ());
    	return(result);
    }
    
    public double[] getPlaneFromPointAndNormal(ReadOnlyVector3 p0, ReadOnlyVector3 normal, double[] result) {
    	if (result == null)
    		result = new double[4];
    	result[0] = normal.getX();
    	result[1] = normal.getY();
    	result[2] = normal.getZ();
    	if (normal.getZ() == 0)
    		result[3] = Double.NaN;
    	else
    		result[3] = normal.getX()*p0.getX()+normal.getY()*p0.getY()+normal.getZ()*p0.getZ();
    	return(result);
    }
    
    public Vector3 getNormalizedIntersectionOf2Planes(double[] plane0, double[] plane1, Vector3 result) {
    	if (result == null)
    		result = new Vector3();
    	result.setX(plane0[1]*plane1[2]-plane1[1]*plane0[2]);
    	result.setY(plane0[2]*plane1[0]-plane1[3]*plane0[0]);
    	result.setZ(plane0[0]*plane1[1]-plane1[0]*plane0[1]);
    	double det = result.lengthSquared();
    	if (det < 0.0001)
    		return(null);
    	result.normalizeLocal();
    	return(result);
    }
	
	public double getPlaneZ(double x, double y, double[] planeEq) {
		double z = (planeEq[3]-planeEq[0]*x-planeEq[1]*y)/planeEq[2];
		return(z);
	}
}
