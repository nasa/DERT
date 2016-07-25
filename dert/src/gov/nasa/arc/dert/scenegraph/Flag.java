package gov.nasa.arc.dert.scenegraph;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.scenegraph.shape.Cylinder;

/**
 * A 3D object that provides a flag shape.
 *
 */
public class Flag extends Node {

	/**
	 * Constructor
	 * 
	 * @param label
	 * @param size
	 */
	public Flag(String label, float size) {
		super(label);
		Cylinder cyl = new Cylinder("_pole", 20, 20, size * 0.015f, size, true);
		cyl.setModelBound(new BoundingBox());
		cyl.updateModelBound();
		attachChild(cyl);
		Box box = new Box("_flag", new Vector3(size * 0.185f, 0f, size * 0.3f), size * 0.2f, size*0.005f, size * 0.15f);
		box.setModelBound(new BoundingBox());
		box.updateModelBound();
		attachChild(box);
	}

}
