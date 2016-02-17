package gov.nasa.arc.dert.util;

import java.nio.FloatBuffer;
import java.util.List;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.intersection.IntersectionRecord;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides math helper methods.
 *
 */
public class MathUtil {

	public final static double PI2 = 2 * Math.PI;
	public final static double epsilonD = 0.00000000000001;
	public final static float epsilonF = 0.00000001f;

	/**
	 * Given a direction vector, return the azimuth and elevation angles.
	 * 
	 * @param direction	the direction vector
	 * @param angle	the storage for the azimuth and elevation angles (will be allocated if null)
	 * @return	the angles
	 * 
	 */
	public static Vector3 directionToAzEl(ReadOnlyVector3 direction, Vector3 angle) {
		if (angle == null) {
			angle = new Vector3();
		}
		double azAngle = 0;

		// Get the Azimuth
		if ((Math.abs(direction.getX()) > 0.0000001) || (Math.abs(direction.getY()) > 0.0000001)) {
			// Use only the x and y components for rotation around the Z axis
			angle.set(direction);
			angle.setZ(0);
			angle.normalizeLocal();
			azAngle = (Math.acos(Vector3.UNIT_Y.dot(angle)));
			if (angle.getX() < 0) {
				azAngle = Math.PI*2-azAngle;
			}
		}

		// Get the Elevation
		// Project the direction onto an XY plane with Z as Y and XY as X.
		angle.set(Math.sqrt(direction.getX()*direction.getX()+direction.getY()*direction.getY()), direction.getZ(), 0);
		angle.normalizeLocal();
		double tiltAngle = Math.acos(Vector3.UNIT_X.dot(angle));
		if (angle.getY() < 0) {
			tiltAngle = -tiltAngle;
		}

		angle.set(azAngle, tiltAngle, 0);
		return (angle);
	}

	/**
	 * Given azimuth and elevation, return a point in 3D space.
	 * 
	 * @param az	azimuth in radians
	 * @param el	elevation in radians
	 * @param result	the resulting point (will be allocated if null)
	 * @return the result
	 */
	public static Vector3 azElToPoint(double az, double el, Vector3 result) {
		if (result == null) {
			result = new Vector3();
		}
		Matrix3 mat = Matrix3.fetchTempInstance();
		mat.fromAngleAxis(az, Vector3.NEG_UNIT_Z);
		Matrix3 mat2 = Matrix3.fetchTempInstance();
		mat2.fromAngleAxis(el, Vector3.UNIT_X);
		mat.multiplyLocal(mat2);
		result.set(Vector3.UNIT_Y);
		mat.applyPost(result, result);
		Matrix3.releaseTempInstance(mat);
		Matrix3.releaseTempInstance(mat2);
		return (result);
	}

	/**
	 * Determine if a point is inside a closed polygon.
	 * 
	 * @param p
	 * @param vertex
	 * @return
	 */
	public static boolean isInsidePolygon(ReadOnlyVector3 p, ReadOnlyVector3[] vertex) {
		return (windingNumber(p, vertex) != 0);
	}

	private static int windingNumber(ReadOnlyVector3 p, ReadOnlyVector3[] vertex) {
		int wNumber = 0;
		for (int i = 0; i < vertex.length - 1; i++) {
			if (vertex[i].getY() <= p.getY()) {
				if (vertex[i+1].getY() > p.getY()) {
					if (isLeft(vertex[i], vertex[i+1], p) > 0) {
						wNumber++;
					}
				}
			}
			else {
				if (vertex[i+1].getY() <= p.getY()) {
					if (isLeft(vertex[i], vertex[i+1], p) < 0) {
						wNumber--;
					}
				}
			}
		}
		return wNumber;
	}

	private static double isLeft(ReadOnlyVector3 p0, ReadOnlyVector3 p1, ReadOnlyVector3 p2) {
		return (((p1.getX() - p0.getX()) * (p2.getY() - p0.getY()) - (p2.getX() - p0.getX())
			* (p1.getY() - p0.getY())));
	}

	/**
	 * Determine if a set of points is listed in the counter clockwise direction
	 * on the XY plane.
	 * 
	 * @param points
	 * @return
	 */
	public static boolean isCounterClockwise(List<ReadOnlyVector3> points) {
		double area = computePolygonArea2D(points);
		if (area < 0) {
			return (false);
		}
		return (true);
	}

	/**
	 * Returns the area enclosed by the specified (x, y) points on a plane (the
	 * z coordinates are ignored).
	 * 
	 * @param points
	 *            the (x, y) points which define the 2D polygon.
	 * @return the area enclosed by the specified coordinates.
	 */
	public static double computePolygonArea2D(List<ReadOnlyVector3> points) {
		if (points.size() < 3) {
			return (0);
		}
		java.util.Iterator<ReadOnlyVector3> iter = points.iterator();
		if (!iter.hasNext()) {
			return 0;
		}
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

	/**
	 * Compute the mean normal for a polygon.
	 * 
	 * @param vertexBuffer
	 * @param normalBuffer
	 * @param zPos
	 *            if true, all normals will have a positive Z coordinate
	 */
	public static void computePolygonNormal(FloatBuffer vertexBuffer, FloatBuffer normalBuffer, boolean zPos) {
		// compute mean normal of points
		int n = vertexBuffer.limit();
		double count = 0;
		Vector3 meanNormal = new Vector3();
		Vector3 vec0 = new Vector3();
		Vector3 vec1 = new Vector3();
		Vector3 vec2 = new Vector3();
		meanNormal.set(0, 0, 0);
		for (int i = 0; i < n; i += 9) {
			vec0.set(vertexBuffer.get(i), vertexBuffer.get(i + 1), vertexBuffer.get(i + 2));
			vec1.set(vertexBuffer.get(i + 3), vertexBuffer.get(i + 4), vertexBuffer.get(i + 5));
			vec2.set(vertexBuffer.get(i + 6), vertexBuffer.get(i + 7), vertexBuffer.get(i + 8));
			vec0.subtractLocal(vec1);
			vec1.subtractLocal(vec2);
			vec0.crossLocal(vec1);
			if (zPos && (vec0.getZ() < 0)) {
				vec0.negateLocal();
			}
			meanNormal.addLocal(vec0);
			count++;
		}
		meanNormal.multiplyLocal(1.0 / count);
		meanNormal.normalizeLocal();
		// assign normal for each vertex
		for (int i = 0; i < vertexBuffer.limit(); i += 3) {
			normalBuffer.put(meanNormal.getXf());
			normalBuffer.put(meanNormal.getYf());
			normalBuffer.put(meanNormal.getZf());
		}
		normalBuffer.flip();
	}
	
	/**
	 * Get the area of the triangle defined by points p0, p1, and p2.
	 * @param p0
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static double getArea(Vector3 p0, Vector3 p1, Vector3 p2) {
		p1.subtractLocal(p0);
		p2.subtractLocal(p0);
		p1.cross(p2, p0);
		return(0.5*p0.length());
//		double x, y, z;
//		x = x1 - x0;
//		y = y1 - y0;
//		z = z1 - z0;
//		double a = Math.sqrt(x * x + y * y + z * z);
//		x = x2 - x1;
//		y = y2 - y1;
//		z = z2 - z1;
//		double b = Math.sqrt(x * x + y * y + z * z);
//		x = x0 - x2;
//		y = y0 - y2;
//		z = z0 - z2;
//		double c = Math.sqrt(x * x + y * y + z * z);
//		double s = (a + b + c) / 2;
//		return (Math.sqrt(s * (s - a) * (s - b) * (s - c)));
	}

	/**
	 * Given three points, create the normal for the plane they define.
	 * 
	 * @param norm
	 * @param v0
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static boolean createNormal(Vector3 norm, ReadOnlyVector3 v0, ReadOnlyVector3 v1, ReadOnlyVector3 v2) {
		if (Double.isNaN(v0.getZ()) || Double.isNaN(v1.getZ()) || Double.isNaN(v2.getZ())) {
			norm.set(0, 0, 0);
			return (false);
		}
		Vector3 work = Vector3.fetchTempInstance();
		norm.set(v1);
		norm.subtractLocal(v0);
		work.set(v2);
		work.subtractLocal(v0);
		norm.crossLocal(work);
		norm.normalizeLocal();
		Vector3.releaseTempInstance(work);
		return (true);
	}

	/**
	 * Convert 4 bytes to an int.
	 * 
	 * @param b0
	 * @param b1
	 * @param b2
	 * @param b3
	 * @return
	 */
	public static final int bytes2Int(byte b0, byte b1, byte b2, byte b3) {
		return (((b0 & 0xff) << 24) | ((b1 & 0xff) << 16) | ((b2 & 0xff) << 8) | (b3 & 0xff));
	}

	/**
	 * Convert an int to an unsigned int
	 * 
	 * @param val
	 * @return
	 */
	public static final long unsignedInt(int val) {
		return (val & 0xFFFFFFFF);
	}

	/**
	 * Convert a short to an unsigned short
	 * 
	 * @param val
	 * @return
	 */
	public static final int unsignedShort(short val) {
		return (val & 0xFFFF);
	}

	/**
	 * Convert a byte to an unsigned byte
	 * 
	 * @param val
	 * @return
	 */
	public static final short unsignedByte(byte val) {
		return ((short) (val & 0xFF));
	}

	/**
	 * Compute the slope from a surface normal
	 * 
	 * @param normal
	 * @return
	 */
	public static double getSlopeFromNormal(ReadOnlyVector3 normal) {
		ReadOnlyVector3 unit = Vector3.UNIT_Z;
		if (normal.getZ() < 0) {
			unit = Vector3.NEG_UNIT_Z;
		}
		double slope = Math.acos(normal.dot(unit));
		return (Math.toDegrees(slope));
	}

	/**
	 * Compute the slope from a line
	 * 
	 * @param v0
	 * @param v1
	 * @return
	 */
	public static double getSlopeFromLine(ReadOnlyVector3 v0, ReadOnlyVector3 v1) {
		Vector3 vec = Vector3.fetchTempInstance();
		vec.set(v1);
		vec.subtractLocal(v0);
		vec.normalizeLocal();
		ReadOnlyVector3 unit = Vector3.UNIT_Z;
		if (vec.getZ() < 0) {
			unit = Vector3.NEG_UNIT_Z;
		}
		double slope = Math.acos(vec.dot(unit));
		Vector3.releaseTempInstance(vec);
		return (90-Math.toDegrees(slope));
	}

	/**
	 * Compute the aspect from a surface normal
	 * 
	 * @param normal
	 * @param work
	 * @return
	 */
	public static double getAspectFromNormal(ReadOnlyVector3 normal) {
		return (getAspect(normal.getX(), normal.getY()));
	}

	/**
	 * Compute the aspect from a line.
	 * 
	 * @param v0
	 * @param v1
	 * @param work
	 * @return
	 */
	public static double getAspectFromLine(ReadOnlyVector3 v0, ReadOnlyVector3 v1) {
		double dx = v1.getX() - v0.getX();
		double dy = v1.getY() - v0.getY();
		return (getAspect(dx, dy));
	}

	private static double getAspect(double x, double y) {
		if ((x == 0) && (y == 0))
			return(0);
		Vector2 work = Vector2.fetchTempInstance();
		work.set(x, y);
		work.normalizeLocal();
		double aspect = work.angleBetween(Vector2.UNIT_Y);
		if (aspect < 0) {
			aspect += 2 * Math.PI;
		}
		aspect = Math.toDegrees(aspect);
		Vector2.releaseTempInstance(work);
		return (aspect);
	}

	/**
	 * Compute the distance from a point to the edge of a bound sphere.
	 * 
	 * @param bs
	 * @param point
	 * @param direction
	 * @return
	 */
	public static double distanceToSphere(BoundingVolume bs, ReadOnlyVector3 point, ReadOnlyVector3 direction) {
		IntersectionRecord record = bs.intersectsWhere(new Ray3(point, direction));
		if (record != null) {
			return (record.getFurthestDistance());
		} else {
			return (bs.getRadius() * 2);
		}
	}

	/**
	 * Get the plane equation from a point and a normal.
	 * 
	 * @param p0
	 * @param normal
	 * @param result
	 * @return
	 */
	public static double[] getPlaneFromPointAndNormal(ReadOnlyVector3 p0, ReadOnlyVector3 normal, double[] result) {
		if (result == null) {
			result = new double[4];
		}
		result[0] = normal.getX();
		result[1] = normal.getY();
		result[2] = normal.getZ();
		if (normal.getZ() == 0) {
			result[3] = Double.NaN;
		} else {
			result[3] = -(normal.getX() * p0.getX() + normal.getY() * p0.getY() + normal.getZ() * p0.getZ());
		}
		return (result);
	}

	/**
	 * Get the Z coordinate of a point in a plane.
	 * 
	 * @param x
	 * @param y
	 * @param planeEq
	 * @return
	 */
	public static double getPlaneZ(double x, double y, double[] planeEq) {
		double z = (-planeEq[3] - planeEq[0] * x - planeEq[1] * y) / planeEq[2];
		return (z);
	}
	
	/**
	 * Determine if two double vectors are within an epsilon.
	 * @param vec0
	 * @param vec1
	 * @return
	 */
	public static boolean equalsDouble(ReadOnlyVector3 vec0, ReadOnlyVector3 vec1) {
		if (vec0 == vec1)
			return(true);
		if (Math.abs(vec0.getX()-vec1.getX()) > epsilonD)
			return(false);
		if (Math.abs(vec0.getY()-vec1.getY()) > epsilonD)
			return(false);
		if (Math.abs(vec0.getZ()-vec1.getZ()) > epsilonD)
			return(false);
		return(true);
	}
	
	/**
	 * Determine if two float vectors are within an epsilon.
	 * @param vec0
	 * @param vec1
	 * @return
	 */
	public static boolean equalsFloat(ReadOnlyVector3 vec0, ReadOnlyVector3 vec1) {
		if (vec0 == vec1)
			return(true);
		if (Math.abs(vec0.getXf()-vec1.getXf()) > epsilonF)
			return(false);
		if (Math.abs(vec0.getYf()-vec1.getYf()) > epsilonF)
			return(false);
		if (Math.abs(vec0.getZf()-vec1.getZf()) > epsilonF)
			return(false);
		return(true);
	}
}
