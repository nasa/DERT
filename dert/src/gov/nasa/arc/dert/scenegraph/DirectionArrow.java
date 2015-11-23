package gov.nasa.arc.dert.scenegraph;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Provides a simple arrow indicating a direction. The brighter end of the line
 * segment is the arrowhead. Adapted from an Ardor3D example.
 *
 */
public class DirectionArrow extends Line {

	private static float[] axisVertex = { 0, 0, 0, 0, 0, 1 };
	private static final ReadOnlyColorRGBA[] axisColor = { ColorRGBA.BLACK, ColorRGBA.YELLOW };
	private static final int[] axisIndex = { 0, 1 };
	private static MaterialState axisMaterialState;

	private Vector3 direction, start;
	private Matrix3 rotMatrix;

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param length
	 * @param color
	 */
	public DirectionArrow(String name, float length, ReadOnlyColorRGBA color) {
		super(name);
		axisVertex[5] = length;
		axisColor[1] = color;
		setLineWidth(3.0f);
		direction = new Vector3(0, 0, 1);
		start = new Vector3(0, 0, 1);
		rotMatrix = new Matrix3();
		setRotation(rotMatrix);
		getMeshData().setIndexMode(IndexMode.Lines);
		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(axisVertex);
		vertexBuffer.rewind();
		getMeshData().setVertexBuffer(vertexBuffer);
		FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(axisColor);
		colorBuffer.rewind();
		getMeshData().setColorBuffer(colorBuffer);
		IntBuffer indexBuffer = BufferUtils.createIntBuffer(axisIndex);
		indexBuffer.rewind();
		getMeshData().setIndexBuffer(indexBuffer);
		axisMaterialState = new MaterialState();
		axisMaterialState.setColorMaterial(MaterialState.ColorMaterial.Emissive);
		axisMaterialState.setEnabled(true);
		getSceneHints().setLightCombineMode(LightCombineMode.Off);
		getSceneHints().setCullHint(CullHint.Never);
		setRenderState(axisMaterialState);
		setModelBound(new BoundingBox());
		updateGeometricState(0, false);
	}

	/**
	 * Set the direction
	 * 
	 * @param direction
	 */
	public void setDirection(ReadOnlyVector3 direction) {
		this.direction.set(direction);
		rotMatrix.fromStartEndLocal(start, direction);
		setRotation(rotMatrix);
		updateGeometricState(0);
	}

	/**
	 * Get the direction
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getDirection() {
		return (direction);
	}

}
