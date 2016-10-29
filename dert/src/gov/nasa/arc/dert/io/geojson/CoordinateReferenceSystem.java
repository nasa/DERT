package gov.nasa.arc.dert.io.geojson;

import gov.nasa.arc.dert.io.geojson.json.JsonObject;
import gov.nasa.arc.dert.raster.Projection;

import com.ardor3d.math.Vector3;

/**
 * Provides a CoordinateReferenceSystem object for GeoJSON. Currently not used.
 *
 */
public class CoordinateReferenceSystem {
	
	private Projection projection;
	
	public CoordinateReferenceSystem(JsonObject jsonObject) {
		// nothing here yet
	}
	
	public CoordinateReferenceSystem(Projection projection) {
		this.projection = projection;
	}
	
	public void translate(double[] coord, Vector3 vec) {
		if (coord.length == 2) {
			vec.set(coord[0], coord[1], 0);
			doTranslate(vec);
			coord[0] = vec.getX();
			coord[1] = vec.getY();
		}
		else if (coord.length == 3) {
			vec.set(coord[0], coord[1], coord[2]);
			doTranslate(vec);
			coord[0] = vec.getX();
			coord[1] = vec.getY();
			coord[2] = vec.getZ();
		}
	}
	
	protected void doTranslate(Vector3 vec) {
		projection.sphericalToWorld(vec);
	}

}
