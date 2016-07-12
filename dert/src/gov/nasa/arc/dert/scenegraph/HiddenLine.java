package gov.nasa.arc.dert.scenegraph;

import gov.nasa.arc.dert.scene.World;

import java.nio.FloatBuffer;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.MaterialFace;
import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.TextureCombineMode;
import com.ardor3d.util.geom.BufferUtils;

public class HiddenLine
	extends Node {
	
	private Line line;
	private Line dashedLine;
	private float lineWidth = 2;
	
	public HiddenLine(String name, IndexMode indexMode) {
		super(name);
		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(3);
		vertexBuffer.limit(0);
		line = new Line("_line");
		line.getMeshData().setIndexMode(indexMode);
		line.getMeshData().setVertexBuffer(vertexBuffer);
		line.getSceneHints().setCastsShadows(false);
		line.getSceneHints().setTextureCombineMode(TextureCombineMode.Off);
		line.setModelBound(new BoundingBox());
		line.updateModelBound();
		line.setLineWidth((float)lineWidth);
		attachChild(line);
		
		vertexBuffer = BufferUtils.createFloatBuffer(3);
		vertexBuffer.limit(0);
		dashedLine = new Line("_dashedline");
		dashedLine.getMeshData().setIndexMode(indexMode);
		dashedLine.getMeshData().setVertexBuffer(vertexBuffer);
		dashedLine.getSceneHints().setCastsShadows(false);
		dashedLine.getSceneHints().setTextureCombineMode(TextureCombineMode.Off);
		dashedLine.setModelBound(new BoundingBox());
		dashedLine.updateModelBound();
		boolean hiddenDashed = World.getInstance().isHiddenDashed();
		dashedLine.getSceneHints().setCullHint(hiddenDashed ? CullHint.Inherit : CullHint.Always);
		dashedLine.setLineWidth((float)lineWidth*0.5f);
		ZBufferState zbs = new ZBufferState();
		zbs.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
		zbs.setEnabled(false);
		dashedLine.setRenderState(zbs);
		dashedLine.setStipplePattern((short) 0xf0f0);
		attachChild(dashedLine);
	}

	/**
	 * Constructor
	 * 
	 * @param p0
	 *            first end point
	 * @param p1
	 *            second end point
	 */
	public HiddenLine(String name, ReadOnlyVector3 p0, ReadOnlyVector3 p1) {
		this(name, IndexMode.Lines);
		float[] vertex = new float[6];
		vertex[0] = (float) p0.getX();
		vertex[1] = (float) p0.getY();
		vertex[2] = (float) p0.getZ();
		vertex[3] = (float) p1.getX();
		vertex[4] = (float) p1.getY();
		vertex[5] = (float) p1.getZ();
		setVertexBuffer(BufferUtils.createFloatBuffer(vertex));
	}

	/**
	 * Set the endpoints of the line segment
	 * 
	 * @param p0
	 * @param p1
	 */
	public void setPoints(ReadOnlyVector3 p0, ReadOnlyVector3 p1) {
		float[] vertex = new float[6];
		vertex[0] = (float) p0.getX();
		vertex[1] = (float) p0.getY();
		vertex[2] = (float) p0.getZ();
		vertex[3] = (float) p1.getX();
		vertex[4] = (float) p1.getY();
		vertex[5] = (float) p1.getZ();
		setVertexBuffer(BufferUtils.createFloatBuffer(vertex));
	}
	
	public void setLineWidth(float lineWidth) {
		this.lineWidth = lineWidth;
		line.setLineWidth((float)lineWidth);
		dashedLine.setLineWidth((float)lineWidth*0.5f);
	}
	
	public float getLineWidth() {
		return(lineWidth);
	}
	
	public void enableDash(boolean enable) {
		dashedLine.getSceneHints().setCullHint(enable ? CullHint.Inherit : CullHint.Always);
	}
	
	public boolean isHiddenDashed() {
		return(dashedLine.getSceneHints().getCullHint() == CullHint.Inherit);
	}
	
	public FloatBuffer getVertexBuffer() {
		return(line.getMeshData().getVertexBuffer());
	}
	
	public void setVertexBuffer(FloatBuffer buffer) {
		line.getMeshData().setVertexBuffer(buffer);
		dashedLine.getMeshData().setVertexBuffer(buffer);
		markDirty(DirtyType.Bounding);
	}
	
	public void updateModelBound() {
		line.updateModelBound();
		dashedLine.updateModelBound();
	}
	
	public void setModelBound(BoundingVolume bounds) {
		line.setModelBound(bounds);
		dashedLine.setModelBound(bounds);
	}

	/**
	 * Set the color.
	 * 
	 * @param color
	 */
	public void setColor(ReadOnlyColorRGBA colorRGBA) {

		MaterialState lineMS = new MaterialState();
		lineMS.setDiffuse(ColorRGBA.BLACK);
		lineMS.setAmbient(ColorRGBA.BLACK);
		lineMS.setEmissive(MaterialState.MaterialFace.FrontAndBack, colorRGBA);
		line.setRenderState(lineMS);
		dashedLine.setRenderState(lineMS);
	}

	public void highlight(boolean enable, ColorRGBA colorRGBA) {
		MaterialState materialState = (MaterialState) line.getLocalRenderState(RenderState.StateType.Material);
		if (enable) {
			materialState.setAmbient(MaterialFace.FrontAndBack, colorRGBA);
			materialState.setDiffuse(MaterialFace.FrontAndBack, colorRGBA);
		}
		else {
			materialState.setDiffuse(ColorRGBA.BLACK);
			materialState.setAmbient(ColorRGBA.BLACK);
		}
		line.setRenderState(materialState);
		dashedLine.setRenderState(materialState);
	}

}
