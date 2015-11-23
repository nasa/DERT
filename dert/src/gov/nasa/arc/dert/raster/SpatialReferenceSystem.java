package gov.nasa.arc.dert.raster;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides a spatial reference system for the landscape.
 *
 */
public class SpatialReferenceSystem {

	// The projection
	protected Projection projection;

	// The projection information
	protected ProjectionInfo projInfo;

	/**
	 * Constructor
	 * 
	 * @param projInfo
	 */
	public SpatialReferenceSystem(ProjectionInfo projInfo) {
		this.projInfo = projInfo;
		projection = new Projection(projInfo);
	}

	/**
	 * Get the distance between two points
	 * 
	 * @param p0
	 * @param p1
	 * @return
	 */
	public double getDistance(ReadOnlyVector3 p0, ReadOnlyVector3 p1) {
		return (p0.distance(p1));
	}

	/**
	 * Get the projection
	 * 
	 * @return
	 */
	public Projection getProjection() {
		return (projection);
	}

	/**
	 * Get the lon/lat coordinates at the center of the landscape
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getCenterLonLat() {
		Vector3 lonLat = new Vector3();
		projection.localToWorld(lonLat);
		projection.worldToSpherical(lonLat);
		return (lonLat);
	}
}
