package gov.nasa.arc.dert.util;

import java.nio.FloatBuffer;
import java.util.List;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.intersection.IntersectionRecord;
import com.ardor3d.intersection.PickData;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Spatial;

/**
 * Provides math helper methods.
 *
 */
public class MathUtil {

	public final static double PI2 = 2 * Math.PI;

	/**
	 * Given a direction vector, return the azimuth and elevation angles.
	 * 
	 * @param direction
	 * @param angle
	 * @return
	 */
	public static double[] directionToAzEl(ReadOnlyVector3 direction, double[] angle, Vector3 workVec, Matrix3 workMat) {
		if (angle == null) {
			angle = new double[3];
		}
		double azAngle = 0;

		// Get the Azimuth
		if ((Math.abs(direction.getX()) > 0.0000001) || (Math.abs(direction.getY()) > 0.0000001)) {
			// Use only the x and y components for rotation around the Z axis
			workVec.set(direction);
			workVec.setZ(0);
			workVec.normalizeLocal();
			azAngle = Vector3.UNIT_Y.smallestAngleBetween(workVec);
			if (workVec.getX() > 0) {
				azAngle = -azAngle;
			}
		}

		// Get the Elevation
		// Rotate the vector into the North plane
		workMat.fromAngleNormalAxis(-azAngle, Vector3.UNIT_Z);
		workMat.applyPost(direction, workVec);
		workVec.setX(workVec.getY());
		workVec.setY(workVec.getZ());
		workVec.setZ(0);
		workVec.normalizeLocal();
		double tiltAngle = Vector3.UNIT_X.smallestAngleBetween(workVec);
		if (workVec.getY() < 0) {
			tiltAngle = -tiltAngle;
		}

		angle[0] = azAngle;
		angle[1] = tiltAngle;
		angle[2] = 0;
		return (angle);
	}

	/**
	 * Given azimuth and elevation, return a point in 3D space.
	 * 
	 * @param az
	 * @param el
	 * @param startVector
	 * @param azAxis
	 * @param elAxis
	 * @param result
	 * @return
	 */
	public static Vector3 azElToPoint(double az, double el, ReadOnlyVector3 startVector, ReadOnlyVector3 azAxis,
		ReadOnlyVector3 elAxis, Vector3 result) {
		if (result == null) {
			result = new Vector3();
		}
		Matrix3 mat = new Matrix3();
		mat.fromAngleAxis(az, azAxis);
		Matrix3 mat2 = new Matrix3();
		mat2.fromAngleAxis(el, elAxis);
		mat.multiplyLocal(mat2);
		result.set(startVector);
		mat.applyPost(result, result);
		return (result);
	}

	/**
	 * Determine if a point is inside a polygon.
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
				if (vertex[i + 1].getY() > p.getY()) {
					if (isLeft(vertex[i], vertex[i + 1], p) > 0) {
						wNumber++;
					}
				}
			} else {
				if (vertex[i + 1].getY() <= p.getY()) {
					if (isLeft(vertex[i], vertex[i + 1], p) < 0) {
						wNumber--;
					}
				}
			}
		}
		return wNumber;
	}

	private static float isLeft(ReadOnlyVector3 p0, ReadOnlyVector3 p1, ReadOnlyVector3 p2) {
		return ((float) ((p1.getX() - p0.getX()) * (p2.getY() - p0.getY()) - (p2.getX() - p0.getX())
			* (p1.getY() - p0.getY())));
	}

	/**
	 * Given 3 points, A, B, and C in a 2d plane, this function computes if the
	 * points going from A-B-C are moving counter clock wise.
	 * 
	 * @param p0
	 *            Point 0.
	 * @param p1
	 *            Point 1.
	 * @param p2
	 *            Point 2.
	 * @return 1 If they are CCW, -1 if they are not CCW, 0 if p2 is between p0
	 *         and p1.
	 */
	public static int counterClockwise(Vector2 p0, Vector2 p1, Vector2 p2) {
		double dx1, dx2, dy1, dy2;
		dx1 = p1.getX() - p0.getX();
		dy1 = p1.getY() - p0.getY();
		dx2 = p2.getX() - p0.getX();
		dy2 = p2.getY() - p0.getY();
		if (dx1 * dy2 > dy1 * dx2) {
			return 1;
		}
		if (dx1 * dy2 < dy1 * dx2) {
			return -1;
		}
		if ((dx1 * dx2 < 0) || (dy1 * dy2 < 0)) {
			return -1;
		}
		if ((dx1 * dx1 + dy1 * dy1) < (dx2 * dx2 + dy2 * dy2)) {
			return 1;
		}
		return (0);
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
	 * Compute the normals for a vertex buffer.
	 * 
	 * @param vertexBuffer
	 * @param normalBuffer
	 * @param zPos
	 *            if true, all normals will have a positive Z coordinate
	 */
	public static void computeNormals(FloatBuffer vertexBuffer, FloatBuffer normalBuffer, boolean zPos) {
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
	 * Get the surface area in the region defined by the vertices by sampling
	 * the spatial.
	 * 
	 * @param vertex
	 * @param n
	 * @param node
	 * @return
	 */
	public static double getSampledSurfaceArea(ReadOnlyVector3[] vertex, int n, Spatial node) {
		double xMin = Double.MAX_VALUE;
		double xMax = -Double.MAX_VALUE;
		double yMin = Double.MAX_VALUE;
		double yMax = -Double.MAX_VALUE;
		double zMax = -Double.MAX_VALUE;
		for (int i = 0; i < vertex.length; ++i) {
			xMin = Math.min(xMin, vertex[i].getX());
			xMax = Math.max(xMax, vertex[i].getX());
			yMin = Math.min(yMin, vertex[i].getY());
			yMax = Math.max(yMax, vertex[i].getY());
			zMax = Math.max(zMax, vertex[i].getZ());
		}
		double xd = (xMax - xMin) / n;
		double yd = (yMax - yMin) / n;
		Vector3 vert = new Vector3();
		Vector3 dir = new Vector3(0, 0, -1);
		double surfaceArea = 0;
		PrimitivePickResults pr = new PrimitivePickResults();
		for (int i = 0; i < n; ++i) {
			for (int j = 0; j < n; ++j) {
				vert.set((float) (xMin + j * xd), (float) (yMin + i * yd), (float) zMax);
				if (isInsidePolygon(vert, vertex)) {
					float el = getElevation(vert, dir, node, pr);
					if (!Float.isNaN(el)) {
						surfaceArea += xd * yd;
					}
				}
			}
		}
		return (surfaceArea);
	}

	private static float getElevation(Vector3 p0, Vector3 dir, Spatial node, PrimitivePickResults pr) {
		// Create a ray starting from the point, and going in the given
		// direction
		final Ray3 mouseRay = new Ray3(p0, dir);
		pr.setCheckDistance(true);
		PickingUtil.findPick(node, mouseRay, pr);
		if (pr.getNumber() == 0) {
			return (Float.NaN);
		}
		PickData closest = pr.getPickData(0);
		for (int i = 1; i < pr.getNumber(); ++i) {
			PickData pd = pr.getPickData(i);
			if (closest.getIntersectionRecord().getClosestDistance() > pd.getIntersectionRecord().getClosestDistance()) {
				closest = pd;
			}
		}
		double dist = closest.getIntersectionRecord().getClosestDistance();
		return ((float) dist);
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
	public static boolean createNormal(Vector3 norm, ReadOnlyVector3 v0, ReadOnlyVector3 v1, ReadOnlyVector3 v2,
		Vector3 work) {
		if (Double.isNaN(v0.getZ()) || Double.isNaN(v1.getZ()) || Double.isNaN(v2.getZ())) {
			norm.set(0, 0, 0);
			return (false);
		}
		norm.set(v1);
		norm.subtractLocal(v0);
		work.set(v2);
		work.subtractLocal(v0);
		norm.crossLocal(work);
		norm.normalizeLocal();
		return (true);
	}

	/**
	 * Get the next power of two greater than the given value
	 * 
	 * @param val
	 * @return
	 */
	public static long nextPowerOf2(long val) {
		long po2 = 1;
		while (po2 < val) {
			po2 *= 2;
		}
		return (po2);
	}

	/**
	 * Convert a list of radian values to degrees
	 * 
	 * @param ll
	 */
	public static void radianToDegree(double[] ll) {
		if (ll == null) {
			return;
		}
		for (int i = 0; i < ll.length; ++i) {
			ll[i] = Math.toDegrees(ll[i]);
		}
	}

	/**
	 * Clip the angle down to within a range of -180 to 180.
	 * 
	 * @param llb
	 */
	public static void clipLonLat(double[] llb) {
		for (int i = 0; i < llb.length; i += 2) {
			if (llb[i] > 180) {
				System.out.print("Found longitude of " + llb[i] + " degrees ...");
				llb[i] -= 360;
				System.out.println(" setting to " + llb[i] + " degrees.");
			}
		}
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
		double dx = v1.getX() - v0.getX();
		double dy = v1.getY() - v0.getY();
		double dz = v1.getZ() - v0.getZ();
		return ((float) (90 * Math.abs(dz / Math.sqrt(dx * dx + dy * dy))));
	}

	/**
	 * Compute the aspect from a surface normal
	 * 
	 * @param normal
	 * @param work
	 * @return
	 */
	public static double getAspectFromNormal(ReadOnlyVector3 normal, Vector2 work) {
		return (getAspect(normal.getX(), normal.getY(), work));
	}

	/**
	 * Compute the aspect from a line.
	 * 
	 * @param v0
	 * @param v1
	 * @param work
	 * @return
	 */
	public static double getAspectFromLine(ReadOnlyVector3 v0, ReadOnlyVector3 v1, Vector2 work) {
		double dx = v1.getX() - v0.getX();
		double dy = v1.getY() - v0.getY();
		return (getAspect(dx, dy, work));
	}

	private static double getAspect(double x, double y, Vector2 work) {
		work.set(x, y);
		work.normalizeLocal();
		double aspect = work.angleBetween(Vector2.UNIT_Y);
		if (aspect < 0) {
			aspect += 2 * Math.PI;
		}
		aspect = Math.toDegrees(aspect);
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
			result[3] = normal.getX() * p0.getX() + normal.getY() * p0.getY() + normal.getZ() * p0.getZ();
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
		double z = (planeEq[3] - planeEq[0] * x - planeEq[1] * y) / planeEq[2];
		return (z);
	}
}
