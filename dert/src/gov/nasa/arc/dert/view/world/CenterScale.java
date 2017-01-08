package gov.nasa.arc.dert.view.world;

import gov.nasa.arc.dert.scenegraph.text.RasterText;
import gov.nasa.arc.dert.scenegraph.text.Text.AlignType;

import java.nio.FloatBuffer;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Provides a simple scale at the center of the world view.
 *
 */
public class CenterScale extends Node {

	private final static float[] scaleVertex = { -50f,0,0, 50f,0,0, -50,-5,0, -50f,5,0, 50,-5,0, 50,5,0, 0,-5,0, 0,0,0 };
	private final static int[] scaleIndex = { 0,1, 2,3, 4,5, 6,7};
	private ReadOnlyColorRGBA[] scaleColor;
	
	private Line line;
	private RasterText text;

	/**
	 * Constructor
	 * 
	 * @param color
	 */
	public CenterScale(ReadOnlyColorRGBA color) {
		super("Center Scale");
		line = new Line("_line");
		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(scaleVertex);
		line.getMeshData().setVertexBuffer(vertexBuffer);
		scaleColor = new ReadOnlyColorRGBA[scaleVertex.length/3];
		for (int i=0; i<scaleColor.length; ++i)
			scaleColor[i] = color;
		line.getMeshData().setIndexMode(IndexMode.Lines);
		FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(scaleColor);
		colorBuffer.rewind();
		line.getMeshData().setColorBuffer(colorBuffer);
		line.getMeshData().setIndexBuffer(BufferUtils.createIntBuffer(scaleIndex));
		line.getMeshData().getIndexBuffer().limit(scaleIndex.length);
		line.getMeshData().getIndexBuffer().rewind();
		line.getSceneHints().setAllPickingHints(false);
		line.setModelBound(new BoundingBox());
		line.updateModelBound();
		MaterialState crosshairMaterialState = new MaterialState();
		crosshairMaterialState.setColorMaterial(MaterialState.ColorMaterial.Emissive);
		crosshairMaterialState.setEnabled(true);
		line.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		line.setRenderState(crosshairMaterialState);
		line.updateGeometricState(0, true);
		
		attachChild(line);
		
		text = new RasterText("_ctr", "", AlignType.Center, false);
		text.setColor(ColorRGBA.WHITE);
		text.setVisible(true);
		text.setTranslation(0, -20, 0);
		attachChild(text);
	}
	
	public void setText(String str) {
		text.setText(str);
	}

}
