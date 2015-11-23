package gov.nasa.arc.dert.scenegraph;

import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.viewpoint.BasicCamera;
import gov.nasa.arc.dert.viewpoint.ViewDependent;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.MaterialFace;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.util.geom.BufferUtils;

/**
 * A line segment that stretches like a rubber band as the user moves the
 * cursor.
 *
 */
public class Rubberband extends Node implements ViewDependent {

	// The type of end point, the anchor is placed with the first click, the
	// current remains with the cursor
	public enum ModeType {
		Anchor, Current
	}

	// The mobile end point
	protected ModeType mode = ModeType.Anchor;

	// Color of each end point
	protected Color anchorColor, currentColor;

	// The line segment
	protected Line line;

	// The figures for the end points
	protected FigureMarker currentPoint, anchorPoint;

	// Anchor point coordinate
	protected Vector3 anchor = new Vector3();

	// Size of the end points
	protected float pointSize;

	// Default line segment color
	protected ColorRGBA[] lineColor = { new ColorRGBA(ColorRGBA.WHITE), new ColorRGBA(ColorRGBA.WHITE) };

	public Rubberband(String name, float size, ShapeType shape, Color lineColor, Color anchorColor, Color currentColor) {
		super(name);
		pointSize = size;
		ColorRGBA lineColorRGBA = new ColorRGBA(lineColor.getRed() / 255f, lineColor.getGreen() / 255f,
			lineColor.getBlue() / 255f, lineColor.getAlpha() / 255f);
		this.lineColor[0].set(lineColorRGBA);
		this.lineColor[1].set(lineColorRGBA);
		this.anchorColor = anchorColor;
		this.currentColor = currentColor;

		ColorRGBA pointColorRGBA = new ColorRGBA(anchorColor.getRed() / 255f, anchorColor.getGreen() / 255f,
			anchorColor.getBlue() / 255f, anchorColor.getAlpha() / 255f);
		anchorPoint = new FigureMarker("_anchor", Vector3.ZERO, pointSize, anchorColor, false, false, false);
		anchorPoint.setShape(shape);
		anchorPoint.getSceneHints().setAllPickingHints(false);
		MaterialState materialState = (MaterialState) anchorPoint.getLocalRenderState(StateType.Material);
		materialState.setAmbient(MaterialFace.FrontAndBack, pointColorRGBA);
		materialState.setEmissive(MaterialFace.FrontAndBack, pointColorRGBA);
		attachChild(anchorPoint);

		pointColorRGBA = new ColorRGBA(currentColor.getRed() / 255f, currentColor.getGreen() / 255f,
			currentColor.getBlue() / 255f, currentColor.getAlpha() / 255f);
		currentPoint = new FigureMarker("_current", Vector3.ZERO, pointSize, currentColor, false, false, false);
		currentPoint.setShape(shape);
		currentPoint.getSceneHints().setAllPickingHints(false);
		materialState = (MaterialState) currentPoint.getLocalRenderState(StateType.Material);
		materialState.setAmbient(MaterialFace.FrontAndBack, pointColorRGBA);
		materialState.setEmissive(MaterialFace.FrontAndBack, pointColorRGBA);
		attachChild(currentPoint);
		createLine();
		line.getSceneHints().setAllPickingHints(false);
		attachChild(line);
	}

	/**
	 * Set the anchor point location
	 * 
	 * @param ap
	 */
	public void setAnchorPoint(ReadOnlyVector3 ap) {
		anchor.set(ap);
		anchorPoint.setTranslation(ap);
		currentPoint.setTranslation(ap);
		updateWorldBound(true);
		updateLine(anchor, ap);
	}

	/**
	 * Set the current point location
	 * 
	 * @param cp
	 */
	public void setCurrentPoint(ReadOnlyVector3 cp) {
		currentPoint.setTranslation(cp);
		updateWorldBound(true);
		updateLine(anchor, cp);
	}

	protected void updateLine(ReadOnlyVector3 pos0, ReadOnlyVector3 pos1) {
		FloatBuffer vertexBuffer = line.getMeshData().getVertexBuffer();
		vertexBuffer.put(0, (float) pos0.getX());
		vertexBuffer.put(1, (float) pos0.getY());
		vertexBuffer.put(2, (float) pos0.getZ());
		vertexBuffer.put(3, (float) pos1.getX());
		vertexBuffer.put(4, (float) pos1.getY());
		vertexBuffer.put(5, (float) pos1.getZ());
		line.markDirty(DirtyType.Bounding);
	}

	protected void createLine() {
		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(6);
		IntBuffer indexBuffer = BufferUtils.createIntBuffer(2);
		vertexBuffer.limit(6);
		vertexBuffer.rewind();
		ReadOnlyVector3 trans = anchorPoint.getTranslation();
		vertexBuffer.put(0, (float) trans.getX());
		vertexBuffer.put(1, (float) trans.getY());
		vertexBuffer.put(2, (float) trans.getZ());
		trans = currentPoint.getTranslation();
		vertexBuffer.put(3, (float) trans.getX());
		vertexBuffer.put(4, (float) trans.getY());
		vertexBuffer.put(5, (float) trans.getZ());
		indexBuffer.limit(2);
		indexBuffer.rewind();
		indexBuffer.put(0, 0);
		indexBuffer.put(1, 1);
		line = new Line("_line", vertexBuffer, null, null, null);
		line.getMeshData().setIndexBuffer(indexBuffer);
		line.getMeshData().setIndexMode(IndexMode.Lines);
		line.setLineWidth(2);
		line.setModelBound(new BoundingBox());
		line.updateModelBound();
		line.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		MaterialState ms = new MaterialState();
		ms.setColorMaterial(MaterialState.ColorMaterial.Emissive);
		ms.setEnabled(true);
		line.getMeshData().setColorBuffer(BufferUtils.createFloatBuffer(this.lineColor));
		line.setRenderState(ms);
		line.updateModelBound();
	}

	/**
	 * Update size based on camera location
	 */
	@Override
	public void update(BasicCamera camera) {
		if (anchorPoint != null) {
			anchorPoint.update(camera);
		}
		if (currentPoint != null) {
			currentPoint.update(camera);
		}
	}

}
