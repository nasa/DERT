package gov.nasa.arc.dert.viewpoint;

import java.io.Serializable;

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

	public ViewpointStore() {
		// nothing here
	}

	public ViewpointStore(String name, BasicCamera camera, double az, double el) {
		this.name = name;
		set(camera, az, el);
	}

	public void set(BasicCamera camera, double az, double el) {
		location = new Vector3(camera.getLocation());
		direction = new Vector3(camera.getDirection());
		azimuth = az;
		elevation = el;
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

	public ViewpointStore getInbetween(ViewpointStore that, float percent) {
		ViewpointStore vps = new ViewpointStore();
		vps.name = this.name + percent;
		vps.location = this.location.lerp(that.location, percent, null);
		vps.direction = this.direction.lerp(that.direction, percent, null);
		vps.lookAt = this.lookAt.lerp(that.lookAt, percent, null);
		double azDelta = that.azimuth-this.azimuth;
		// adjust for 0/360 crossover
		if (azDelta > Math.PI)
			azDelta -= Math.PI*2;
		else if (azDelta < -Math.PI)
			azDelta += Math.PI*2;			
		vps.azimuth = azDelta * percent + this.azimuth;
		vps.elevation = (that.elevation - this.elevation) * percent + this.elevation;
		vps.distance = (that.distance - this.distance) * percent + this.distance;
		vps.magIndex = this.magIndex;
		vps.frustumLeft = (that.frustumLeft - this.frustumLeft) * percent + this.frustumLeft;
		vps.frustumRight = (that.frustumRight - this.frustumRight) * percent + this.frustumRight;
		vps.frustumBottom = (that.frustumBottom - this.frustumBottom) * percent + this.frustumBottom;
		vps.frustumTop = (that.frustumTop - this.frustumTop) * percent + this.frustumTop;
		vps.frustumNear = (that.frustumNear - this.frustumNear) * percent + this.frustumNear;
		vps.frustumFar = (that.frustumFar - this.frustumFar) * percent + this.frustumFar;
		return (vps);
	}
}
