package gov.nasa.arc.dert.io.geojson;

import gov.nasa.arc.dert.scenegraph.LineStrip;
import gov.nasa.arc.dert.scenegraph.VectorText;

import java.nio.FloatBuffer;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.scenegraph.Node;

/**
 * An Ardor3D Node with a continuous line at the same elevation (Z coordinate)
 * and a label indicating the elevation.
 *
 */
public class ContourLine extends Node {

	/**
	 * Constructor
	 * 
	 * @param line
	 * @param elevation
	 * @param defaultColor
	 */
	public ContourLine(LineStrip line, double elevation, ReadOnlyColorRGBA defaultColor) {
		super(Integer.toString((int) elevation));

		attachChild(line);
		VectorText label = new VectorText(getName(), getName());
		label.setScaleFactor(0.05f);
		label.setColor(defaultColor);
		BoundingVolume lBound = line.getModelBound();

		// draw label if it is smaller than the contour
		if (label.getWidth() < lBound.getRadius()) {
			FloatBuffer vertex = line.getMeshData().getVertexBuffer();
			float x0 = vertex.get(0);
			float y0 = vertex.get(1);
			float z0 = vertex.get(2);
			Vector3 vec0 = new Vector3(x0, y0, z0);
			// insert the text into the contour
			for (int i = 3; i < vertex.limit(); i += 3) {
				float x1 = vertex.get(i);
				float y1 = vertex.get(i + 1);
				float z1 = vertex.get(i + 2);
				Vector3 vec1 = new Vector3(x1, y1, z1);
				double d = vec0.distance(vec1);
				if (d >= label.getWidth()) {
					Matrix3 rotMat = new Matrix3();
					vec1.subtractLocal(vec0);
					vec1.normalizeLocal();
					rotMat.fromStartEndLocal(Vector3.UNIT_X, vec1);
					label.setTranslation(x0, y0, z0);
					label.setRotation(rotMat);
					attachChild(label);
					int k = 0;
					for (int j = i; j < vertex.limit(); ++j) {
						vertex.put(k++, vertex.get(j));
					}
					vertex.limit(k);
					vertex.rewind();
					line.getMeshData().setVertexBuffer(vertex);
					break;
				}
			}
		}
		updateGeometricState(0);
	}

}
