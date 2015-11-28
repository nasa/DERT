package gov.nasa.arc.dert.viewpoint;

import gov.nasa.arc.dert.util.MathUtil;

import java.io.Serializable;

import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;

/**
 * Data structure for storing viewpoint attributes.
 *
 */
public class ViewpointStore implements Serializable {

	public String name;
	public Vector3 location;
	public Vector3 direction;
	public Vector3 lookAt;
	public double frustumLeft, frustumRight, frustumBottom, frustumTop;
	public double frustumNear, frustumFar;
	public double distance;
	public double azimuth, elevation;
	public int magIndex;
	
	private double[] angle;
	private Vector3 workVec;
	private Matrix3 workMat;

	public ViewpointStore() {
		angle = new double[3];
		workVec = new Vector3();
		workMat = new Matrix3();
	}

	public ViewpointStore(String name, BasicCamera camera) {
		this.name = name;
		angle = new double[3];
		workVec = new Vector3();
		workMat = new Matrix3();
		set(camera);
	}

	public void set(BasicCamera camera) {
		location = new Vector3(camera.getLocation());
		direction = new Vector3(camera.getDirection());
		angle =  MathUtil.directionToAzEl(direction, angle, workVec, workMat);
		azimuth = angle[0];
		elevation = angle[1];
		distance = camera.getDistanceToCoR();
		lookAt = new Vector3(camera.getLookAt());
		magIndex = camera.getMagIndex();
		frustumLeft = camera.getFrustumLeft();
		frustumRight = camera.getFrustumRight();
		frustumBottom = camera.getFrustumBottom();
		frustumTop = camera.getFrustumTop();
		frustumNear = camera.getFrustumNear();
		frustumFar = camera.getFrustumFar();
	}

	@Override
	public String toString() {
		if (name != null) {
			return (name);
		}
		String str = "";
		str += "Viewpoint " + name + "\n";
		str += "  Location: " + location + "\n";
		str += "  Direction: " + direction + "\n";
		str += "  LookAt: " + lookAt + "\n";
		str += "  Distance: " + distance + "\n";
		str += "  Near: " + frustumNear + ", Far: " + frustumFar + "\n";
		str += "  Left: " + frustumLeft + ", Right: " + frustumRight + ", Bottom: " + frustumBottom + ", Top: "
			+ frustumTop + "\n";
		str += "  Azimuth: " + Math.toDegrees(azimuth) + ", Elevation: " + Math.toDegrees(elevation) + "\n";
		str += "  Scale: " + BasicCamera.magFactor[magIndex] + "\n";
		return (str);
	}

	public ViewpointStore getInbetween(ViewpointStore that, float lPct, float dPct) {
		ViewpointStore vps = new ViewpointStore();
		vps.name = this.name + lPct;
		vps.location = this.location.lerp(that.location, lPct, vps.location);
		vps.direction = this.direction.lerp(that.direction, dPct, vps.direction);
		vps.lookAt = this.lookAt.lerp(that.lookAt, dPct, vps.lookAt);
		angle =  MathUtil.directionToAzEl(vps.direction, angle, workVec, workMat);
		vps.azimuth = angle[0];
		vps.elevation = angle[1];
		vps.distance = vps.location.distance(vps.lookAt);
		vps.magIndex = this.magIndex;
		vps.frustumLeft = (that.frustumLeft - this.frustumLeft) * dPct + this.frustumLeft;
		vps.frustumRight = (that.frustumRight - this.frustumRight) * dPct + this.frustumRight;
		vps.frustumBottom = (that.frustumBottom - this.frustumBottom) * dPct + this.frustumBottom;
		vps.frustumTop = (that.frustumTop - this.frustumTop) * dPct + this.frustumTop;
		vps.frustumNear = (that.frustumNear - this.frustumNear) * dPct + this.frustumNear;
		vps.frustumFar = (that.frustumFar - this.frustumFar) * dPct + this.frustumFar;
		return (vps);
	}
}
