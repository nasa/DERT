package gov.nasa.arc.dert.view.fieldcamera;

import java.nio.FloatBuffer;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Provides a simple cross hair for the FieldCamera view.
 *
 */
public class SimpleCrosshair extends Line {

	private final static float[] crosshairVertex = { -15f, 0, 0, 15f, 0, 0, 0, -15f, 0, 0, 15f, 0 };
	private final static int[] crosshairIndex = { 0, 1, 2, 3 };

	/**
	 * Constructor
	 * 
	 * @param color
	 */
	public SimpleCrosshair(ReadOnlyColorRGBA color) {
		super("Crosshair");
		ReadOnlyColorRGBA[] crosshairColor = { color, color, color, color };
		getMeshData().setIndexMode(IndexMode.Lines);
		getMeshData().setVertexBuffer(BufferUtils.createFloatBuffer(crosshairVertex));
		FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(crosshairColor);
		colorBuffer.rewind();
		getMeshData().setColorBuffer(colorBuffer);
		getMeshData().setIndexBuffer(BufferUtils.createIntBuffer(crosshairIndex));
		getMeshData().getIndexBuffer().limit(4);
		getMeshData().getIndexBuffer().rewind();
		getSceneHints().setAllPickingHints(false);
		setModelBound(new BoundingBox());
		updateModelBound();
		MaterialState crosshairMaterialState = new MaterialState();
		crosshairMaterialState.setColorMaterial(MaterialState.ColorMaterial.Emissive);
		crosshairMaterialState.setEnabled(true);
		getSceneHints().setLightCombineMode(LightCombineMode.Off);
		setRenderState(crosshairMaterialState);
		updateGeometricState(0, true);
	}

}
