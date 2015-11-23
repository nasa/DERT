package gov.nasa.arc.dert.util;

import com.ardor3d.intersection.BoundingPickResults;
import com.ardor3d.intersection.PickData;
import com.ardor3d.intersection.Pickable;
import com.ardor3d.scenegraph.Mesh;

/**
 * Provides a PickResults that allows access to the mesh list in the results of
 * a bounds pick.
 *
 */
public class SpatialPickResults extends BoundingPickResults {

	public Mesh[] getMeshList() {
		int n = getNumber();
		if (n == 0) {
			return (null);
		}
		Mesh[] mesh = new Mesh[n];
		for (int i = 0; i < n; ++i) {
			PickData pickData = getPickData(i);
			Pickable pickable = pickData.getTarget();
			if (pickable instanceof Mesh) {
				mesh[i] = (Mesh) pickable;
			}
		}
		return (mesh);
	}

	/**
	 * Places a new geometry (enclosed in PickData) into the results list.
	 * 
	 * @param data
	 *            the PickData to be placed in the results list.
	 */
	@Override
	public void addPickData(final PickData data) {
		if (data.getIntersectionRecord() == null) {
			return;
		}
		super.addPickData(data);
	}
}
