package gov.nasa.arc.dert.test;

import gov.nasa.arc.dert.util.MathUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Provided for testing math utilities.
 *
 */
public class MathUtilTest {
	
	public boolean testMathUtil() {
		System.err.println("Testing math utilities . . .");
		if (!testDirectionToAzEl()) {
			System.err.println("Test of MathUtil.directionToAzEl failed.");
			return(false);
		}
		if (!testAzElToDirection()) {
			System.err.println("Test of MathUtil.azElToDirection failed.");
			return(false);
		}
		if (!testIsInsidePolygon()) {
			System.err.println("Test of MathUtil.isInsidePolygon failed.");
			return(false);
		}
		if (!testComputePolygonArea2D()) {
			System.err.println("Test of MathUtil.computePolygonArea2D failed.");
			return(false);
		}
		if (!testComputePolygonNormal()) {
			System.err.println("Test of MathUtil.computePolygonNormal failed.");
			return(false);
		}
		if (!testGetArea()) {
			System.err.println("Test of MathUtil.getArea failed.");
			return(false);
		}
		if (!testCreateNormal()) {
			System.err.println("Test of MathUtil.creatNormal failed.");
			return(false);
		}
		if (!testBytes2Int()) {
			System.err.println("Test of MathUtil.bytes2Int failed.");
			return(false);
		}
		if (!testUnsignedInt()) {
			System.err.println("Test of MathUtil.unsignedInt failed.");
			return(false);
		}
		if (!testUnsignedShort()) {
			System.err.println("Test of MathUtil.unsignedShort failed.");
			return(false);
		}
		if (!testUnsignedByte()) {
			System.err.println("Test of MathUtil.unsignedByte failed.");
			return(false);
		}
		if (!testGetSlopeFromNormal()) {
			System.err.println("Test of MathUtil.getSlopeFromNormal failed.");
			return(false);
		}
		if (!testGetSlopeFromLine()) {
			System.err.println("Test of MathUtil.getSlopeFromLine failed.");
			return(false);
		}
		if (!testGetAspectFromLine()) {
			System.err.println("Test of MathUtil.getAspectFromLine failed.");
			return(false);
		}
		if (!testDistanceToSphere()) {
			System.err.println("Test of MathUtil.distanceToSphere failed.");
			return(false);
		}
		if (!testGetPlaneFromPointAndNormal()) {
			System.err.println("Test of MathUtil.getPlaneFromPointAndNormal failed.");
			return(false);
		}
		if (!testGetPlaneZ()) {
			System.err.println("Test of MathUtil.getPlaneZ failed.");
			return(false);
		}
		System.err.println(". . . complete.");
		return(true);
	}
	
	private boolean testDirectionToAzEl() {
		Vector3 direction = new Vector3();
		Vector3 angle = new Vector3();
		try {
			// invalid zero direction
			MathUtil.directionToAzEl(direction, angle);
			System.err.println("MathUtil.directionToAzEl direction:("+direction.getX()+","+direction.getY()+","+direction.getZ()+") azimuth:"+Math.toDegrees(angle.getX())+" elevation:"+Math.toDegrees(angle.getY()));
			if (!MathUtil.equalsDouble(angle, new Vector3(0, Math.PI/2, 0)))
				return(false);
			
			// looking straight ahead
			direction.set(0, 1, 0);
			MathUtil.directionToAzEl(direction, angle);
			System.err.println("MathUtil.directionToAzEl direction:("+direction.getX()+","+direction.getY()+","+direction.getZ()+") azimuth:"+Math.toDegrees(angle.getX())+" elevation:"+Math.toDegrees(angle.getY()));
			if (!MathUtil.equalsDouble(angle, new Vector3(0, 0, 0)))
				return(false);
			
			// look to the right
			direction.set(1, 0, 0);
			MathUtil.directionToAzEl(direction, angle);
			System.err.println("MathUtil.directionToAzEl direction:("+direction.getX()+","+direction.getY()+","+direction.getZ()+") azimuth:"+Math.toDegrees(angle.getX())+" elevation:"+Math.toDegrees(angle.getY()));
			if (!MathUtil.equalsDouble(angle, new Vector3(Math.PI/2, 0, 0)))
				return(false);
			
			// look to the left
			direction.set(-1, 0, 0);
			MathUtil.directionToAzEl(direction, angle);
			System.err.println("MathUtil.directionToAzEl direction:("+direction.getX()+","+direction.getY()+","+direction.getZ()+") azimuth:"+Math.toDegrees(angle.getX())+" elevation:"+Math.toDegrees(angle.getY()));
			if (!MathUtil.equalsDouble(angle, new Vector3(1.5*Math.PI, 0, 0)))
				return(false);
			
			// look to the back
			direction.set(0, -1, 0);
			MathUtil.directionToAzEl(direction, angle);
			System.err.println("MathUtil.directionToAzEl direction:("+direction.getX()+","+direction.getY()+","+direction.getZ()+") azimuth:"+Math.toDegrees(angle.getX())+" elevation:"+Math.toDegrees(angle.getY()));
			if (!MathUtil.equalsDouble(angle, new Vector3(Math.PI, 0, 0)))
				return(false);
			
			// looking down
			direction.set(0, 0, -1);
			MathUtil.directionToAzEl(direction, angle);
			System.err.println("MathUtil.directionToAzEl direction:("+direction.getX()+","+direction.getY()+","+direction.getZ()+") azimuth:"+Math.toDegrees(angle.getX())+" elevation:"+Math.toDegrees(angle.getY()));
			if (!MathUtil.equalsDouble(angle, new Vector3(0, -Math.PI/2, 0)))
				return(false);
			
			// upper right front
			direction.set(1, 1, 1);
			MathUtil.directionToAzEl(direction, angle);
			System.err.println("MathUtil.directionToAzEl direction:("+direction.getX()+","+direction.getY()+","+direction.getZ()+") azimuth:"+Math.toDegrees(angle.getX())+" elevation:"+Math.toDegrees(angle.getY()));
			if (!MathUtil.equalsDouble(angle, new Vector3(Math.PI/4, 0.61547970867038, 0)))
				return(false);
			
			// upper left front
			direction.set(-1, 1, 1);
			MathUtil.directionToAzEl(direction, angle);
			System.err.println("MathUtil.directionToAzEl direction:("+direction.getX()+","+direction.getY()+","+direction.getZ()+") azimuth:"+Math.toDegrees(angle.getX())+" elevation:"+Math.toDegrees(angle.getY()));
			if (!MathUtil.equalsDouble(angle, new Vector3(1.75*Math.PI, 0.61547970867038, 0)))
				return(false);
			
			// upper right back
			direction.set(1, -1, 1);
			MathUtil.directionToAzEl(direction, angle);
			System.err.println("MathUtil.directionToAzEl direction:("+direction.getX()+","+direction.getY()+","+direction.getZ()+") azimuth:"+Math.toDegrees(angle.getX())+" elevation:"+Math.toDegrees(angle.getY()));
			if (!MathUtil.equalsDouble(angle, new Vector3(0.75*Math.PI, 0.61547970867038, 0)))
				return(false);
			
			// upper left back
			direction.set(-1, -1, 1);
			MathUtil.directionToAzEl(direction, angle);
			System.err.println("MathUtil.directionToAzEl direction:("+direction.getX()+","+direction.getY()+","+direction.getZ()+") azimuth:"+Math.toDegrees(angle.getX())+" elevation:"+Math.toDegrees(angle.getY()));
			if (!MathUtil.equalsDouble(angle, new Vector3(1.25*Math.PI, 0.61547970867038, 0)))
				return(false);
		}
		catch (Exception e) {
			e.printStackTrace();
			return(false);
		}
		return(true);
	}
	
	private boolean testAzElToDirection() {
		Vector3 point = new Vector3();
		double az = 0;
		double el = 0;
		try {			
			// looking straight ahead
			az = 0;
			el = 0;
			MathUtil.azElToDirection(az, el, point);
			System.err.println("MathUtil.azElToDirection azimuth:"+Math.toDegrees(az)+" elevation:"+Math.toDegrees(el)+" point:("+point.getX()+","+point.getY()+","+point.getZ()+")");
			if (!MathUtil.equalsDouble(point, new Vector3(0, 1, 0)))
				return(false);
			
			// look to the right
			az = 0.5*Math.PI;
			el = 0;
			MathUtil.azElToDirection(az, el, point);
			System.err.println("MathUtil.azElToDirection azimuth:"+Math.toDegrees(az)+" elevation:"+Math.toDegrees(el)+" point:("+point.getX()+","+point.getY()+","+point.getZ()+")");
			if (!MathUtil.equalsDouble(point, new Vector3(1, 0, 0)))
				return(false);
			
			// look to the left
			az = 1.5*Math.PI;
			el = 0;
			MathUtil.azElToDirection(az, el, point);
			System.err.println("MathUtil.azElToDirection azimuth:"+Math.toDegrees(az)+" elevation:"+Math.toDegrees(el)+" point:("+point.getX()+","+point.getY()+","+point.getZ()+")");
			if (!MathUtil.equalsDouble(point, new Vector3(-1, 0, 0)))
				return(false);
			
			// look to the back
			az = Math.PI;
			el = 0;
			MathUtil.azElToDirection(az, el, point);
			System.err.println("MathUtil.azElToDirection azimuth:"+Math.toDegrees(az)+" elevation:"+Math.toDegrees(el)+" point:("+point.getX()+","+point.getY()+","+point.getZ()+")");
			if (!MathUtil.equalsDouble(point, new Vector3(0, -1, 0)))
				return(false);
			
			// looking down
			az = 0;
			el = -0.5*Math.PI;
			MathUtil.azElToDirection(az, el, point);
			System.err.println("MathUtil.azElToDirection azimuth:"+Math.toDegrees(az)+" elevation:"+Math.toDegrees(el)+" point:("+point.getX()+","+point.getY()+","+point.getZ()+")");
			if (!MathUtil.equalsDouble(point, new Vector3(0, 0, -1)))
				return(false);
			
			// upper right front
			az = 0.25*Math.PI;
			el = 0.25*Math.PI;
			MathUtil.azElToDirection(az, el, point);
			System.err.println("MathUtil.azElToDirection azimuth:"+Math.toDegrees(az)+" elevation:"+Math.toDegrees(el)+" point:("+point.getX()+","+point.getY()+","+point.getZ()+")");
			if (!MathUtil.equalsDouble(point, new Vector3(0.5, 0.5, 0.7071067811865475)))
				return(false);
			
			// upper left front
			az = 1.75*Math.PI;
			el = 0.25*Math.PI;
			MathUtil.azElToDirection(az, el, point);
			System.err.println("MathUtil.azElToDirection azimuth:"+Math.toDegrees(az)+" elevation:"+Math.toDegrees(el)+" point:("+point.getX()+","+point.getY()+","+point.getZ()+")");
			if (!MathUtil.equalsDouble(point, new Vector3(-0.5, 0.5, 0.7071067811865475)))
				return(false);
			
			// upper right back
			az = 0.75*Math.PI;
			el = 0.25*Math.PI;
			MathUtil.azElToDirection(az, el, point);
			System.err.println("MathUtil.azElToDirection azimuth:"+Math.toDegrees(az)+" elevation:"+Math.toDegrees(el)+" point:("+point.getX()+","+point.getY()+","+point.getZ()+")");
			if (!MathUtil.equalsDouble(point, new Vector3(0.5, -0.5, 0.7071067811865475)))
				return(false);
			
			// upper left back
			az = 1.25*Math.PI;
			el = 0.25*Math.PI;
			MathUtil.azElToDirection(az, el, point);
			System.err.println("MathUtil.azElToDirection azimuth:"+Math.toDegrees(az)+" elevation:"+Math.toDegrees(el)+" point:("+point.getX()+","+point.getY()+","+point.getZ()+")");
			if (!MathUtil.equalsDouble(point, new Vector3(-0.5, -0.5, 0.7071067811865475)))
				return(false);
		}
		catch (Exception e) {
			e.printStackTrace();
			return(false);
		}
		return(true);
	}

	public boolean testIsInsidePolygon() {
		Vector3 p = null;
		Vector3[] vertex = null;
		// point inside triangle
		p = new Vector3(0.5, -0.5, 0);
		vertex = new Vector3[] {new Vector3(-1, -1, 0), new Vector3(1, -1, 0), new Vector3(1, 1, 0), new Vector3(-1, -1, 0)};
		System.err.println("MathUtil.isInsidePolygon point in triangle");
		if (!MathUtil.isInsidePolygon(p, vertex))
			return(false);
		// point inside square
		p = new Vector3();
		vertex = new Vector3[] {new Vector3(-1, -1, 0), new Vector3(1, -1, 0), new Vector3(1, 1, 0), new Vector3(-1, 1, 0), new Vector3(-1, -1, 0)};
		System.err.println("MathUtil.isInsidePolygon point in square");
		if (!MathUtil.isInsidePolygon(p, vertex))
			return(false);
		// point outside triangle
		p = new Vector3(-0.5, 0.5, 0);
		vertex = new Vector3[] {new Vector3(-1, -1, 0), new Vector3(1, -1, 0), new Vector3(1, 1, 0), new Vector3(-1, -1, 0)};
		System.err.println("MathUtil.isInsidePolygon point outside triangle");
		if (MathUtil.isInsidePolygon(p, vertex))
			return(false);
		// point outside square
		p = new Vector3(2, 2, 0);
		vertex = new Vector3[] {new Vector3(-1, -1, 0), new Vector3(1, -1, 0), new Vector3(1, 1, 0), new Vector3(-1, 1, 0), new Vector3(-1, -1, 0)};
		System.err.println("MathUtil.isInsidePolygon point outside square");
		if (MathUtil.isInsidePolygon(p, vertex))
			return(false);
		// point inside polygon
		p = new Vector3();
		vertex = new Vector3[] {new Vector3(-1, -1, 0), new Vector3(1, -1, 0), new Vector3(2, -0.5, 0), new Vector3(1, 0, 0), new Vector3(2, 0.5, 0),
				new Vector3(1, 1, 0), new Vector3(-1, 1, 0), new Vector3(-1, -1, 0)};
		System.err.println("MathUtil.isInsidePolygon point in polygon");
		if (!MathUtil.isInsidePolygon(p, vertex))
			return(false);
		// point outside polygon
		p = new Vector3(1.5, 0, 0);
		vertex = new Vector3[] {new Vector3(-1, -1, 0), new Vector3(1, -1, 0), new Vector3(2, -0.5, 0), new Vector3(1, 0, 0), new Vector3(2, 0.5, 0),
				new Vector3(1, 1, 0), new Vector3(-1, 1, 0), new Vector3(-1, -1, 0)};
		System.err.println("MathUtil.isInsidePolygon point outside polygon");
		if (MathUtil.isInsidePolygon(p, vertex))
			return(false);
		return(true);
	}
	
	public boolean testComputePolygonArea2D() {
		ArrayList<ReadOnlyVector3> points = new ArrayList<ReadOnlyVector3>();
		double area = 0;
		// unit triangle
		points.clear();
		points.add(new Vector3());
		points.add(new Vector3(1, 0, 0));
		points.add(new Vector3(1, 1, 0));
		area = MathUtil.computePolygonArea2D(points);
		System.err.println("MathUtil.computePolyArea2D unit triangle, area:"+area);
		if (area != 0.5)
			return(false);
		// unit square
		points.clear();
		points.add(new Vector3());
		points.add(new Vector3(1, 0, 0));
		points.add(new Vector3(1, 1, 0));
		points.add(new Vector3(0, 1, 0));
		area = MathUtil.computePolygonArea2D(points);
		System.err.println("MathUtil.computePolyArea2D unit square, area:"+area);
		if (area != 1.0)
			return(false);
		// positive polygon
		points.clear();
		points.add(new Vector3());
		points.add(new Vector3(1, 0, 0));
		points.add(new Vector3(1, 1, 0));
		points.add(new Vector3(2, 1, 0));
		points.add(new Vector3(2, 2, 0));
		points.add(new Vector3(1, 2, 0));
		points.add(new Vector3(1, 1, 0));
		points.add(new Vector3(0, 1, 0));
		area = MathUtil.computePolygonArea2D(points);
		System.err.println("MathUtil.computePolyArea2D positive polygon, area:"+area);
		if (area != 2.0)
			return(false);
		// negative polygon
		points.clear();
		points.add(new Vector3(0, 1, 0));
		points.add(new Vector3(1, 1, 0));
		points.add(new Vector3(1, 2, 0));
		points.add(new Vector3(2, 2, 0));
		points.add(new Vector3(2, 1, 0));
		points.add(new Vector3(1, 1, 0));
		points.add(new Vector3(1, 0, 0));
		points.add(new Vector3());		
		area = MathUtil.computePolygonArea2D(points);
		System.err.println("MathUtil.computePolyArea2D negative polygon, area:"+area);
		if (area != -2.0)
			return(false);
		return(true);
	}
	
	public boolean testComputePolygonNormal() {
		Mesh mesh = getQuad();
		FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(vertexBuffer.limit());
		
		MathUtil.computePolygonNormal(vertexBuffer, normalBuffer, true);
		System.err.println("MathUtil.computePolygonNormal flat square normal: "+normalBuffer.get(0)+","+normalBuffer.get(1)+","+normalBuffer.get(2));
		if ((normalBuffer.get(0) != 0) || (normalBuffer.get(1) != 0) || (normalBuffer.get(2) != 1))
			return(false);
		
		Matrix3 mat = new Matrix3();
		mat.fromAngleNormalAxis(Math.PI/4, Vector3.UNIT_Y);
		Vector3 vec = new Vector3();
		for (int i=0; i<24; ++i) {
			vec.set(vertexBuffer.get(i*3), vertexBuffer.get(i*3+1), vertexBuffer.get(i*3+2));
			mat.applyPost(vec, vec);
			vertexBuffer.put(i*3, vec.getXf());
			vertexBuffer.put(i*3+1, vec.getYf());
			vertexBuffer.put(i*3+2, vec.getZf());
		}
		MathUtil.computePolygonNormal(vertexBuffer, normalBuffer, true);
		System.err.println("MathUtil.computePolygonNormal rotated square normal: "+normalBuffer.get(0)+","+normalBuffer.get(1)+","+normalBuffer.get(2));
		vec.set(normalBuffer.get(0), normalBuffer.get(1), normalBuffer.get(2));
		if (!MathUtil.equalsFloat(vec, new Vector3(0.70710677,0.0,0.70710677)))
			return(false);
		
		return(true);
	}
	
	public boolean testGetArea() {
		double area = MathUtil.getAreaOfTriangle(0, 0, 0, 1, 0, 0, 1, 1, 0);
		System.err.println("MathUtil.getAreaOfTriangle XY-plane triangle area: "+area);
		if (area != 0.5)
			return(false);
		area = MathUtil.getAreaOfTriangle(0, 0, 0, 1, 0, 0, 1, 0, 1);
		System.err.println("MathUtil.getAreaOfTriangle XZ-plane triangle area: "+area);
		if (area != 0.5)
			return(false);
		area = MathUtil.getAreaOfTriangle(0, 0, 0, 1, 0, 0, 1, 1, 1);
		System.err.println("MathUtil.getAreaOfTriangle oblique triangle area: "+area);
		if (area != 0.7071067811865476)
			return(false);
		return(true);
	}
	
	public boolean testCreateNormal() {
		boolean created = false;
		Vector3 work = new Vector3();
		created = MathUtil.createNormal(new Vector3(), new Vector3(0, 0, Double.NaN), new Vector3(), new Vector3(), work);
		if (created)
			return(false);
		created = MathUtil.createNormal(new Vector3(), new Vector3(), new Vector3(0, 0, Double.NaN), new Vector3(), work);
		if (created)
			return(false);
		created = MathUtil.createNormal(new Vector3(), new Vector3(), new Vector3(), new Vector3(0, 0, Double.NaN), work);
		if (created)
			return(false);
		Vector3 norm = new Vector3();
		created = MathUtil.createNormal(norm, new Vector3(), new Vector3(), new Vector3(), work);
		System.err.println("MathUtil.createNormal: normal="+norm);
		if (!created)
			return(false);
		if (!norm.equals(Vector3.ZERO))
			return(false);
		created = MathUtil.createNormal(norm, new Vector3(), new Vector3(1,0,0), new Vector3(1,1,0), work);
		System.err.println("MathUtil.createNormal: normal="+norm);
		if (!created)
			return(false);
		if (!norm.equals(Vector3.UNIT_Z))
			return(false);
		created = MathUtil.createNormal(norm, new Vector3(), new Vector3(0,1,0), new Vector3(1,1,0), work);
		System.err.println("MathUtil.createNormal: normal="+norm);
		if (!created)
			return(false);
		if (!norm.equals(Vector3.NEG_UNIT_Z))
			return(false);
		return(true);
	}
	
	public boolean testBytes2Int() {
		int i = MathUtil.bytes2Int((byte)255, (byte)255, (byte)255, (byte)255);
		System.err.println("MathUtil.bytes2Int: int="+i);
		if (i != 0xffffffff)
			return(false);
		i = MathUtil.bytes2Int((byte)0, (byte)255, (byte)0, (byte)255);
		System.err.println("MathUtil.bytes2Int: int="+i);
		if (i != 0x00ff00ff)
			return(false);
		return(true);
	}
	
	public boolean testUnsignedInt() {
		long l = MathUtil.unsignedInt(0xffffffff);
		System.err.println("MathUtil.unsignedInt: long="+l);
		if (l != 0xffffffff)
			return(false);
		l = MathUtil.unsignedInt(0x00ff00ff);
		System.err.println("MathUtil.unsignedInt: long="+l);
		if (l != 0x00ff00ff)
			return(false);
		return(true);
	}
	
	public boolean testUnsignedShort() {
		int i = MathUtil.unsignedShort((short)0xffff);
		System.err.println("MathUtil.unsignedShort: int="+i);
		if (i != 0xffff)
			return(false);
		i = MathUtil.unsignedShort((short)0x00ff);
		System.err.println("MathUtil.unsignedShort: int="+i);
		if (i != 0x00ff)
			return(false);
		return(true);
	}
	
	public boolean testUnsignedByte() {
		short s = MathUtil.unsignedByte((byte)0xff);
		System.err.println("MathUtil.unsignedByte: short="+s);
		if (s != 0xff)
			return(false);
		s = MathUtil.unsignedByte((byte)0x0f);
		System.err.println("MathUtil.unsignedByte: short="+s);
		if (s != 0x0f)
			return(false);
		return(true);
	}
	
	public boolean testGetSlopeFromNormal() {
		double slope = MathUtil.getSlopeFromNormal(new Vector3(0, 0, 1));
		System.err.println("MathUtil.getSlopeFromNormal: normal=(0,0,1), slope="+slope);
		if (slope != 0)
			return(false);
		slope = MathUtil.getSlopeFromNormal(new Vector3(0, 0, -1));
		System.err.println("MathUtil.getSlopeFromNormal: normal=(0,0,-1), slope="+slope);
		if (slope != 0)
			return(false);
		slope = MathUtil.getSlopeFromNormal(new Vector3(1, 0, 0));
		System.err.println("MathUtil.getSlopeFromNormal: normal=(1,0,0), slope="+slope);
		if (slope != 90)
			return(false);
		slope = MathUtil.getSlopeFromNormal(new Vector3(0, 1, 0));
		System.err.println("MathUtil.getSlopeFromNormal: normal=(0,1,0), slope="+slope);
		if (slope != 90)
			return(false);
		slope = MathUtil.getSlopeFromNormal(new Vector3(-1, 0, 0));		
		System.err.println("MathUtil.getSlopeFromNormal: normal=(-1,0,0), slope="+slope);
		if (slope != 90)
			return(false);
		slope = MathUtil.getSlopeFromNormal(new Vector3(0, -1, 0));
		System.err.println("MathUtil.getSlopeFromNormal: normal=(0,-1,0), slope="+slope);
		if (slope != 90)
			return(false);
		slope = MathUtil.getSlopeFromNormal(new Vector3(0.70710677,0.0,0.70710677));		
		System.err.println("MathUtil.getSlopeFromNormal: normal=(0.70710677,0.0,0.70710677), slope="+slope);
		if (Math.round(slope) != 45)
			return(false);
		slope = MathUtil.getSlopeFromNormal(new Vector3(-0.70710677,0.0,-0.70710677));		
		System.err.println("MathUtil.getSlopeFromNormal: normal=(-0.70710677,0.0,-0.70710677), slope="+slope);
		if (Math.round(slope) != 45)
			return(false);
		return(true);
	}
	
	public boolean testGetSlopeFromLine() {
		double slope = MathUtil.getSlopeFromLine(new Vector3(0, 0, 0), new Vector3(0, 0, 0));
		System.err.println("MathUtil.getSlopeFromLine: slope="+slope);
		if (slope != 0)
			return(false);
		slope = MathUtil.getSlopeFromLine(new Vector3(0, 0, 0), new Vector3(0, 0, -1));
		System.err.println("MathUtil.getSlopeFromLine: slope="+slope);
		if (slope != 90)
			return(false);
		slope = MathUtil.getSlopeFromLine(new Vector3(0, 0, 0), new Vector3(0, 0, 1));
		System.err.println("MathUtil.getSlopeFromLine: slope="+slope);
		if (slope != 90)
			return(false);
		slope = MathUtil.getSlopeFromLine(new Vector3(0, 0, 0), new Vector3(0.70710677,0.0,0.70710677));
		System.err.println("MathUtil.getSlopeFromLine: slope="+slope);
		if (Math.round(slope) != 45)
			return(false);
		slope = MathUtil.getSlopeFromLine(new Vector3(0, 0, 0), new Vector3(-0.70710677,0.0,-0.70710677));
		System.err.println("MathUtil.getSlopeFromLine: slope="+slope);
		if (Math.round(slope) != 45)
			return(false);
		return(true);
	}
	
	public boolean testGetAspectFromLine() {
		double aspect = MathUtil.getAspectFromLine(new Vector3(0, 0, 0), new Vector3(0, 0, 0));
		System.err.println("MathUtil.getAspectFromLine: aspect="+aspect);
		if (aspect != 0)
			return(false);
		aspect = MathUtil.getAspectFromLine(new Vector3(0, 0, 0), new Vector3(0, 0, -1));
		System.err.println("MathUtil.getAspectFromLine: aspect="+aspect);
		if (aspect != 0)
			return(false);
		aspect = MathUtil.getAspectFromLine(new Vector3(0, 0, 0), new Vector3(0, 0, 1));
		System.err.println("MathUtil.getAspectFromLine: aspect="+aspect);
		if (aspect != 0)
			return(false);
		aspect = MathUtil.getAspectFromLine(new Vector3(0, 0, 0), new Vector3(1,0,1));
		System.err.println("MathUtil.getAspectFromLine: aspect="+aspect);
		if (Math.round(aspect) != 90)
			return(false);
		aspect = MathUtil.getAspectFromLine(new Vector3(0, 0, 0), new Vector3(-1,0,-1));
		System.err.println("MathUtil.getAspectFromLine: aspect="+aspect);
		if (Math.round(aspect) != 270)
			return(false);
		return(true);
	}
	
	public boolean testDistanceToSphere() {
		BoundingSphere bs = new BoundingSphere();
		bs.setCenter(0, 0, 0);
		bs.setRadius(100);
		double distance = MathUtil.distanceToSphere(bs, new Vector3(), Vector3.UNIT_X);
		System.err.println("MathUtil.distanceToSphere: distance="+distance);
		if (distance != 100)
			return(false);
		distance = MathUtil.distanceToSphere(bs, new Vector3(0,50,0), Vector3.UNIT_Y);
		System.err.println("MathUtil.distanceToSphere: distance="+distance);
		if (distance != 50)
			return(false);
		return(true);
	}
	
	public boolean testGetPlaneFromPointAndNormal() {
		Vector3 pt = new Vector3();
		double[] planeEq = MathUtil.getPlaneFromPointAndNormal(pt, new Vector3(0,0,1), null);
		System.err.println("MathUtil.getPlaneFromPointAndNormal: plane eq = "+planeEq[0]+" "+planeEq[1]+" "+planeEq[2]+" "+planeEq[3]);
		Vector3 vec = new Vector3(planeEq[0], planeEq[1], planeEq[2]);
		double distance = Math.abs(planeEq[3])/vec.length();
		if (distance != 0)
			return(false);
		pt.set(1, 1, 1);
		planeEq = MathUtil.getPlaneFromPointAndNormal(pt, new Vector3(0,0,1), null);
		System.err.println("MathUtil.getPlaneFromPointAndNormal: plane eq = "+planeEq[0]+" "+planeEq[1]+" "+planeEq[2]+" "+planeEq[3]);
		vec = new Vector3(planeEq[0], planeEq[1], planeEq[2]);
		double l = vec.length();
		vec.multiplyLocal(pt);		
		distance = Math.abs(vec.getX()+vec.getY()+vec.getZ()+planeEq[3])/l;
		if (Math.round(distance) != 0)
			return(false);
		return(true);
	}
	
	public boolean testGetPlaneZ() {
		Vector3 pt = new Vector3();
		double[] planeEq = MathUtil.getPlaneFromPointAndNormal(pt, new Vector3(0,0,1), null);
		double z = MathUtil.getPlaneZ(1, 1, planeEq);
		System.err.println("MathUtil.getPlaneZ: z = "+z);
		if (z != 0)
			return(false);
		pt.set(1, 1, 1);
		planeEq = MathUtil.getPlaneFromPointAndNormal(pt, new Vector3(0,0,1), null);
		z = MathUtil.getPlaneZ(0, 0, planeEq);
		System.err.println("MathUtil.getPlaneZ: z = "+z);
		if (z != 1)
			return(false);
		return(true);
	}
	
	private Mesh getQuad() {
		int numVertices = 72;
		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(numVertices);

		vertexBuffer.put(0).put(0).put(0);
		vertexBuffer.put(1).put(0).put(0);
		vertexBuffer.put(1).put(1).put(0);

		vertexBuffer.put(0).put(0).put(0);
		vertexBuffer.put(1).put(1).put(0);
		vertexBuffer.put(0).put(1).put(0);

		vertexBuffer.put(0).put(0).put(0);
		vertexBuffer.put(0).put(1).put(0);
		vertexBuffer.put(-1).put(1).put(0);

		vertexBuffer.put(0).put(0).put(0);
		vertexBuffer.put(-1).put(1).put(0);
		vertexBuffer.put(-1).put(0).put(0);

		vertexBuffer.put(0).put(0).put(0);
		vertexBuffer.put(-1).put(0).put(0);
		vertexBuffer.put(-1).put(-1).put(0);

		vertexBuffer.put(0).put(0).put(0);
		vertexBuffer.put(-1).put(-1).put(0);
		vertexBuffer.put(0).put(-1).put(0);

		vertexBuffer.put(0).put(0).put(0);
		vertexBuffer.put(0).put(-1).put(0);
		vertexBuffer.put(1).put(-1).put(0);

		vertexBuffer.put(0).put(0).put(0);
		vertexBuffer.put(1).put(-1).put(0);
		vertexBuffer.put(1).put(0).put(0);
		
		vertexBuffer.flip();
		
		Mesh mesh = new Mesh("Quad");
		mesh.getMeshData().setVertexBuffer(vertexBuffer);
		mesh.setModelBound(new BoundingBox());
		mesh.updateModelBound();
		mesh.updateGeometricState(0);
		return(mesh);
	}
}
