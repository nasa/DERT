package gov.nasa.arc.dert.scenegraph;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.Matrix3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.shape.Cylinder;
import com.ardor3d.scenegraph.shape.Quad;

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
		Cylinder cyl = new Cylinder("Pole", 20, 20, size * 0.015f, size, true);
		cyl.setModelBound(new BoundingBox());
		cyl.updateModelBound();
		cyl.setTranslation(-size * 0.185f, 0, 0);
		attachChild(cyl);
		Quad quad = new Quad("Flag", size * 0.4f, size * 0.3f);
		quad.setModelBound(new BoundingBox());
		quad.updateModelBound();
		quad.setTranslation(0, 0f, size * 0.3f);
		quad.setRotation(new Matrix3().fromAngles(Math.PI / 2, 0, 0));
		attachChild(quad);
	}

}
