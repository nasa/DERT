package gov.nasa.arc.dert.viewpoint;

import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.StateUtil;

import java.util.HashMap;

import com.ardor3d.math.Vector3;

/**
 * Data structure for storing viewpoint attributes.
 *
 */
public class ViewpointStore {

	public String name;
	public Vector3 location;
	public Vector3 direction;
	public Vector3 lookAt;
	public double frustumLeft, frustumRight, frustumBottom, frustumTop;
	public double frustumNear, frustumFar;
	public double distance;
	public double azimuth, elevation;
	public int magIndex;
	public double zOffset;
	public String mode = "Nominal"; // Nominal, Hike, or Map

	public ViewpointStore() {
		name = "";
		location = new Vector3();
		direction = new Vector3();
		lookAt = new Vector3();
	}

	public ViewpointStore(String name, BasicCamera camera) {
		this.name = name;
		location = new Vector3();
		direction = new Vector3();
		lookAt = new Vector3();
		set(camera);
	}
	
	public ViewpointStore(String name, ViewpointStore that) {
		set(name, that);
	}
	
	public static ViewpointStore fromHashMap(HashMap<String,Object> map) {
		if (map == null)
			return(null);
		ViewpointStore store = new ViewpointStore();
		store.name = StateUtil.getString(map, "Name", null);
		store.location = StateUtil.getVector3(map, "Location", null);
		store.direction = StateUtil.getVector3(map, "Direction", null);
		store.lookAt = StateUtil.getVector3(map, "LookAt", null);
		store.frustumLeft = StateUtil.getDouble(map, "FrustumLeft", 0);
		store.frustumRight = StateUtil.getDouble(map, "FrustumRight", 0);
		store.frustumBottom = StateUtil.getDouble(map, "FrustumBottom", 0);
		store.frustumTop = StateUtil.getDouble(map, "FrustumTop", 0);
		store.frustumNear = StateUtil.getDouble(map, "FrustumNear", 0);
		store.frustumFar = StateUtil.getDouble(map, "FrustumFar", 0);
		store.distance = StateUtil.getDouble(map, "Distance", 0);
		store.azimuth = StateUtil.getDouble(map, "Azimuth", 0);
		store.elevation = StateUtil.getDouble(map, "Elevation", 0);
		store.magIndex = StateUtil.getInteger(map, "MagnificationIndex", 0);
		store.mode = StateUtil.getString(map, "Mode", "Nominal");
		store.zOffset = StateUtil.getDouble(map, "ZOffset", 0);
		return(store);
	}
	
	public boolean isEqualTo(ViewpointStore that) {
		if (that == null)
			return(false);
		if (!this.name.equals(that.name)) 
			return(false);
		if (!this.mode.equals(that.mode)) 
			return(false);
		if (this.zOffset != that.zOffset) 
			return(false);
		if (this.frustumLeft != that.frustumLeft) 
			return(false);
		if (this.frustumRight != that.frustumRight) 
			return(false);
		if (this.frustumBottom != that.frustumBottom) 
			return(false);
		if (this.frustumTop != that.frustumTop) 
			return(false);
		if (this.frustumNear != that.frustumNear) 
			return(false);
		if (this.frustumFar != that.frustumFar) 
			return(false);
		if (this.distance != that.distance)
			return(false);
		if (this.azimuth != that.azimuth) 
			return(false);
		if (this.elevation != that.elevation) 
			return(false);
		if (this.magIndex != that.magIndex) 
			return(false);
		if (!this.location.equals(that.location)) 
			return(false);
		if (!this.direction.equals(that.direction)) 
			return(false);
		if (!this.lookAt.equals(that.lookAt)) 
			return(false);
		return(true);
	}

	public void set(String name, ViewpointStore that) {
		this.name = name;
		this.location = new Vector3(that.location);
		this.direction = new Vector3(that.direction);
		this.lookAt = new Vector3(that.lookAt);
		this.frustumLeft = that.frustumLeft;
		this.frustumRight = that.frustumRight;
		this.frustumBottom = that.frustumBottom;
		this.frustumTop = that.frustumTop;
		this.frustumNear = that.frustumNear;
		this.frustumFar = that.frustumFar;
		this.distance = that.distance;
		this.azimuth = that.azimuth;
		this.elevation = that.elevation;
		this.magIndex = that.magIndex;
		this.mode = that.mode;
		this.zOffset = that.zOffset;
	}

	public void set(BasicCamera camera) {
		location.set(camera.getLocation());
		direction.set(camera.getDirection());
		Vector3 angle =  MathUtil.directionToAzEl(direction, null);
		azimuth = angle.getX();
		elevation = angle.getY();
		distance = camera.getDistanceToCoR();
		lookAt.set(camera.getLookAt());
		magIndex = camera.getMagIndex();
		frustumLeft = camera.getFrustumLeft();
		frustumRight = camera.getFrustumRight();
		frustumBottom = camera.getFrustumBottom();
		frustumTop = camera.getFrustumTop();
		frustumNear = camera.getFrustumNear();
		frustumFar = camera.getFrustumFar();
	}
	
	public HashMap<String,Object> toHashMap() {
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("Name", name);
		StateUtil.putVector3(map, "Location", location);
		StateUtil.putVector3(map, "Direction", direction);
		StateUtil.putVector3(map, "LookAt", lookAt);
		map.put("FrustumLeft", new Double(frustumLeft));
		map.put("FrustumRight", new Double(frustumRight));
		map.put("FrustumBottom", new Double(frustumBottom));
		map.put("FrustumTop", new Double(frustumTop));
		map.put("FrustumNear", new Double(frustumNear));
		map.put("FrustumFar", new Double(frustumFar));
		map.put("Distance", new Double(distance));
		map.put("Azimuth", new Double(azimuth));
		map.put("Elevation", new Double(elevation));
		map.put("MagnificationIndex", new Integer(magIndex));
		map.put("Mode", mode);
		map.put("ZOffset", new Double(zOffset));
		return(map);
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
		str += "  Mode: "+mode+"\n";
		return (str);
	}

	public ViewpointStore getInbetween(ViewpointStore that, double pct) {
		ViewpointStore vps = new ViewpointStore();
		vps.name = this.name + pct;
		vps.location = this.location.lerp(that.location, pct, vps.location);
		if ((vps.mode != null) && vps.mode.equals("Hike"))
			vps.location.setZ(Landscape.getInstance().getZ(location.getX(), location.getY())+zOffset);
//		vps.direction = this.direction.lerp(that.direction, pct, vps.direction);
		vps.lookAt = this.lookAt.lerp(that.lookAt, pct, vps.lookAt);
		double azDelta = that.azimuth-this.azimuth;
		// adjust for 0/360 crossover
		if (azDelta > Math.PI)
			azDelta -= Math.PI*2;
		else if (azDelta < -Math.PI)
			azDelta += Math.PI*2;			
		vps.azimuth = azDelta * pct + this.azimuth;
		vps.elevation = (that.elevation - this.elevation) * pct + this.elevation;
		vps.direction = MathUtil.azElToDirection(vps.azimuth, vps.elevation, null);
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
