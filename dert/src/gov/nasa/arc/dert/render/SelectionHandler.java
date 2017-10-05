package gov.nasa.arc.dert.render;

import gov.nasa.arc.dert.terrain.QuadTreeMesh;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.SpatialPickResults;

import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.intersection.IntersectionRecord;
import com.ardor3d.intersection.PickData;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitiveKey;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Spatial;

public class SelectionHandler {

	/**
	 * This class provides the capability to pick objects in a scene using the
	 * Ardor3D picking functions.
	 */

	// data structure to hold results of a pick
	private PickResults pickResults;
	
	// Helper
	private Vector3 work = new Vector3();

	/**
	 * Constructor
	 */
	public SelectionHandler() {
		super();
		CollisionTreeManager.INSTANCE.setMaxElements(1024);
	}

	/**
	 * Do a selection
	 * 
	 * @param pickRay
	 * @param position
	 * @param normal
	 * @param boundsPick
	 * @param noQuadTree
	 * @return
	 */
	public Spatial doSelection(Ray3 pickRay, Vector3 position, Vector3 normal, SpatialPickResults boundsPick,
		boolean terrainOnly) {

		// First do a pick on the object bounds to reduce time spent on more
		// expensive pick.
		Mesh[] mesh = boundsPick.getMeshList();
		if (mesh.length == 0) {
			return (null);
		}

		// pick each mesh
		int meshIndex = -1;
		IntersectionRecord record = null;
		int index = -1;
		double dist = Double.MAX_VALUE;
		for (int i = 0; i < mesh.length; ++i) {
			if (terrainOnly) {
				if (!(mesh[i] instanceof QuadTreeMesh))
					continue;
			}
			// get the mesh bounds
			PickData pd = boundsPick.getPickData(i);
			IntersectionRecord ir = pd.getIntersectionRecord();
			if (ir == null) {
				continue;
			}
			if (ir.getNumberOfIntersections() == 0) {
				continue;
			}

			pickResults = new PrimitivePickResults();
			PickingUtil.findPick(mesh[i], pickRay, pickResults);
			if (pickResults.getNumber() > 0) {
				for (int j = 0; j < pickResults.getNumber(); j++) {
					pd = pickResults.getPickData(j);
					ir = pd.getIntersectionRecord();
					int closestIndex = ir.getClosestIntersection();
					double d = ir.getIntersectionDistance(closestIndex);
					if (d < dist) {
						dist = d;
						index = closestIndex;
						record = ir;
						meshIndex = i;
					}
				}
			}
		}
		if (record == null) {
			return (null);
		}
		ReadOnlyVector3 pos = record.getIntersectionPoint(index);
		ReadOnlyVector3 nrml = record.getIntersectionNormal(index);
		if (nrml == null) {
			PrimitiveKey key = record.getIntersectionPrimitive(index);
			Vector3[] vertices = mesh[meshIndex].getMeshData().getPrimitiveVertices(key.getPrimitiveIndex(),
				key.getSection(), null);
			if (vertices.length > 2) {
				nrml = getNormal(vertices[0], vertices[1], vertices[2]);
			} else {
				nrml = new Vector3();
			}
		}
		position.set(pos);
		normal.set(nrml);
		if (meshIndex >= 0) {
			return (mesh[meshIndex]);
		}
		return (null);
	}

	/**
	 * Get the normal at the picked location.
	 * 
	 * @param v0
	 * @param v1
	 * @param v2
	 * @return
	 */
	public Vector3 getNormal(Vector3 v0, Vector3 v1, Vector3 v2) {
		Vector3 store = new Vector3();
		MathUtil.createNormal(store, v0, v1, v2, work);
		return (store);
	}
}
