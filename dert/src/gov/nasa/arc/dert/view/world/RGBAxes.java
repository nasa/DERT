package gov.nasa.arc.dert.view.world;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.util.geom.BufferUtils;

/**
 * The view dependent cross hair that is drawn at the center of rotation in the
 * worldview. It provides three axes: green = North, red = East, blue == Up.
 *
 */
public class RGBAxes extends Line {

	// Coordinates and colors
	private static final ColorRGBA greenColor = new ColorRGBA(0.0f, 0.8f, 0.0f, 1.0f);
	private static final float[] axisVertex = { 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, -0.2f, 1.25f, 0, -0.2f, 1.8f, 0,
		0.2f, 1.25f, 0, 0.2f, 1.8f, 0 };
	private static final ReadOnlyColorRGBA[] axisColor = { ColorRGBA.BLACK, ColorRGBA.RED, ColorRGBA.GREEN,
		ColorRGBA.BLUE, greenColor, greenColor, greenColor, greenColor };
	private static final int[] axisIndex = { 0, 1, 0, 2, 0, 3, 4, 5, 5, 6, 6, 7 };
	private ZBufferState zBuf;

	/**
	 * Constructor
	 */
	public RGBAxes() {
		super("CenterOfRotation");
		setLineWidth(3.0f);
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
		setModelBound(new BoundingBox());
		updateModelBound();
		
		MaterialState axisMaterialState = new MaterialState();
		axisMaterialState.setColorMaterial(MaterialState.ColorMaterial.Emissive);
		axisMaterialState.setColorMaterialFace(MaterialState.MaterialFace.FrontAndBack);
		axisMaterialState.setEnabled(true);
		getSceneHints().setLightCombineMode(LightCombineMode.Off);
		setRenderState(axisMaterialState);
		
		zBuf = new ZBufferState();
		zBuf.setFunction(ZBufferState.TestFunction.Always);
		zBuf.setEnabled(true);
		setRenderState(zBuf);
		getSceneHints().setRenderBucketType(RenderBucketType.PostBucket);		
		
		updateGeometricState(0, false);
	}
}
