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

	public ViewpointStore getInbetween(ViewpointStore that, double pct) {
		ViewpointStore vps = new ViewpointStore();
		vps.name = this.name + pct;
		vps.location = this.location.lerp(that.location, pct, vps.location);
		vps.direction = this.direction.lerp(that.direction, pct, vps.direction);
		vps.lookAt = this.lookAt.lerp(that.lookAt, pct, vps.lookAt);
//		System.err.println("ViewpointStore.getInbetween "+vps.location+" "+this.location+" "+that.location);
//		angle =  MathUtil.directionToAzEl(vps.direction, angle, workVec, workMat);
//		vps.azimuth = angle[0];
//		vps.elevation = angle[1];
		double azDelta = that.azimuth-this.azimuth;
		// adjust for 0/360 crossover
		if (azDelta > Math.PI)
			azDelta -= Math.PI*2;
		else if (azDelta < -Math.PI)
			azDelta += Math.PI*2;			
		vps.azimuth = azDelta * pct + this.azimuth;
		vps.elevation = (that.elevation - this.elevation) * pct + this.elevation;
		vps.distance = vps.location.distance(vps.lookAt);
		vps.magIndex = this.magIndex;
		vps.frustumLeft = (that.frustumLeft - this.frustumLeft) * pct + this.frustumLeft;
		vps.frustumRight = (that.frustumRight - this.frustumRight) * pct + this.frustumRight;
		vps.frustumBottom = (that.frustumBottom - this.frustumBottom) * pct + this.frustumBottom;
		vps.frustumTop = (that.frustumTop - this.frustumTop) * pct + this.frustumTop;
		vps.frustumNear = (that.frustumNear - this.frustumNear) * pct + this.frustumNear;
		vps.frustumFar = (that.frustumFar - this.frustumFar) * pct + this.frustumFar;
		return (vps);
	}
}
