package gov.nasa.arc.dert.scene.tapemeasure;

import gov.nasa.arc.dert.action.edit.CoordListener;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scenegraph.FigureMarker;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.ui.TextDialog;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.util.UIUtil;
import gov.nasa.arc.dert.viewpoint.BasicCamera;
import gov.nasa.arc.dert.viewpoint.ViewDependent;

import java.awt.Color;
import java.nio.FloatBuffer;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.MaterialFace;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.scenegraph.hint.TextureCombineMode;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Provides a measuring tool that simulates a tape measure. It consists
 * of two dots with a line inbetween that stretches like a rubberband.
 *
 */
public class TapeMeasure extends Node implements ViewDependent, CoordListener {
	
	// Use Z Buffer when drawing
	public static boolean zBufferEnabled;

	// The type of end point, the anchor is placed with the first click, the
	// current remains with the cursor
	public enum ModeType {
		Anchor, Current
	}

	// The mobile end point
	protected ModeType mode = ModeType.Anchor;

	// Color of each end point
	protected Color anchorColor = Color.blue, currentColor = Color.white;

	// The line segment
	protected Line line;

	// The figures for the end points
	protected FigureMarker currentPoint, anchorPoint;

	// Anchor point coordinate
	protected Vector3 anchor = new Vector3(), current = new Vector3();

	// Size of the end points
	protected float pointSize = 0.4f;

	// Default line segment color
	protected ReadOnlyColorRGBA lineColor = ColorRGBA.RED;

	// Measurement information dialog
	private TextDialog textDialog;

	// Measurement information strings
	private String anchorStr = "", currentStr = "", distanceStr = "", gradientStr = "", deltaZStr = "", azStr = "";

	// Temporary
	private Vector3 tmpVec;

	/**
	 * Constructor
	 */
	public TapeMeasure() {
		tmpVec = new Vector3();
		anchorPoint = new FigureMarker("_anchor", Vector3.ZERO, pointSize, anchorColor, false, false, false);
		anchorPoint.setShape(ShapeType.sphere);
		anchorPoint.getSceneHints().setAllPickingHints(false);
		MaterialState materialState = (MaterialState) anchorPoint.getLocalRenderState(StateType.Material);
		materialState.setAmbient(MaterialFace.FrontAndBack, UIUtil.colorToColorRGBA(anchorColor));
		materialState.setEmissive(MaterialFace.FrontAndBack, UIUtil.colorToColorRGBA(anchorColor));
		attachChild(anchorPoint);

		currentPoint = new FigureMarker("_current", Vector3.ZERO, pointSize, currentColor, false, false, false);
		currentPoint.setShape(ShapeType.sphere);
		currentPoint.getSceneHints().setAllPickingHints(false);
		materialState = (MaterialState) currentPoint.getLocalRenderState(StateType.Material);
		materialState.setAmbient(MaterialFace.FrontAndBack, UIUtil.colorToColorRGBA(currentColor));
		materialState.setEmissive(MaterialFace.FrontAndBack, UIUtil.colorToColorRGBA(currentColor));
		attachChild(currentPoint);
		createLine();
		attachChild(line);

		ZBufferState zBufferState = new ZBufferState();
		zBufferState.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
		zBufferState.setEnabled(zBufferEnabled);
		setRenderState(zBufferState);
		updateGeometricState(0);
	}

	/**
	 * Set the anchor point location
	 * 
	 * @param ap
	 */
	public void setAnchorPoint(ReadOnlyVector3 ap) {
		anchor.set(ap);
		anchorPoint.setTranslation(ap);
		current.set(ap);
		currentPoint.setTranslation(ap);
		updateWorldBound(true);
		updateLine();
	}

	/**
	 * Set the current point location
	 * 
	 * @param cp
	 */
	public void setCurrentPoint(ReadOnlyVector3 cp) {
		current.set(cp);
		currentPoint.setTranslation(cp);
		updateWorldBound(true);
		updateLine();
		coordDisplayChanged();
	}

	protected void updateLine() {
		FloatBuffer vertexBuffer = line.getMeshData().getVertexBuffer();
		vertexBuffer.put(0, (float) anchor.getX());
		vertexBuffer.put(1, (float) anchor.getY());
		vertexBuffer.put(2, (float) anchor.getZ());
		vertexBuffer.put(3, (float) current.getX());
		vertexBuffer.put(4, (float) current.getY());
		vertexBuffer.put(5, (float) current.getZ());
		line.markDirty(DirtyType.Bounding);
	}

	protected void createLine() {
		line = new Line("_tapeline");
		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(6);
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
		line.getMeshData().setIndexMode(IndexMode.LineStrip);
		line.getMeshData().setVertexBuffer(vertexBuffer);
		line.getSceneHints().setCastsShadows(false);
		line.getSceneHints().setTextureCombineMode(TextureCombineMode.Off);
		line.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		line.setModelBound(new BoundingBox());
		line.updateModelBound();

		MaterialState lineMS = new MaterialState();
		lineMS.setDiffuse(new ColorRGBA(0, 0, 0, 1));
		lineMS.setAmbient(new ColorRGBA(0, 0, 0, 1));
		lineMS.setEmissive(MaterialState.MaterialFace.FrontAndBack, lineColor);
		line.setRenderState(lineMS);
	}

	/**
	 * Set the measurement information dialog
	 * 
	 * @param dialog
	 */
	public void setDialog(TextDialog dialog) {
		textDialog = dialog;
		if (dialog == null) {
			getSceneHints().setCullHint(CullHint.Always);
		} else {
			String str = "Anchor (m) = N/A\nCurrent (m) = N/A\nDistance (m) = N/A\nSlope = N/A\nAzimuth = N/A\nChange in Elevation (m) = N/A";
			textDialog.setText(str);
			getSceneHints().setCullHint(CullHint.Never);
		}
		markDirty(DirtyType.RenderState);
	}

	/**
	 * The cursor was moved
	 * 
	 * @param pos
	 */
	public void move(Vector3 pos) {
		switch (mode) {
		case Anchor:
			break;
		case Current:
			setCurrentPoint(pos);
			break;
		}
	}

	/**
	 * Update the dialog
	 */
	@Override
	public void update(BasicCamera camera) {
		if (anchorPoint != null) {
			anchorPoint.update(camera);
		}
		if (currentPoint != null) {
			currentPoint.update(camera);
		}
		coordDisplayChanged();
	}

	/**
	 * User clicked in the scene. Place an end point of the tape.
	 * 
	 * @param vec
	 */
	public void click(ReadOnlyVector3 vec) {
		switch (mode) {
		case Anchor:
			setAnchorPoint(vec);
			mode = ModeType.Current;
			break;
		case Current:
			setCurrentPoint(vec);
			mode = ModeType.Anchor;
			break;
		}
	}
	
	public void coordDisplayChanged() {
		if (textDialog == null)
			return;
		tmpVec.set(anchor);
		Landscape.getInstance().localToWorldCoordinate(tmpVec);
		if (World.getInstance().getUseLonLat()) 
			Landscape.getInstance().worldToSphericalCoordinate(tmpVec);
		anchorStr = StringUtil.format(tmpVec);
		
		tmpVec.set(current);
		Landscape.getInstance().localToWorldCoordinate(tmpVec);
		if (World.getInstance().getUseLonLat()) 
			Landscape.getInstance().worldToSphericalCoordinate(tmpVec);
		currentStr = StringUtil.format(tmpVec);
		
		distanceStr = StringUtil.format(current.distance(anchor));
		gradientStr = StringUtil.format(MathUtil.getSlopeFromLine(anchor, current));
		deltaZStr = StringUtil.format(current.getZf() - anchor.getZf());
		azStr = StringUtil.format(MathUtil.getAspectFromLine(anchor, current));
		
		String str = "Anchor (m) = " + anchorStr + "\nCurrent (m) = " + currentStr + "\nDistance (m) = "
			+ distanceStr + "\nSlope = " + gradientStr + StringUtil.DEGREE + "\nAzimuth = " + azStr
			+ StringUtil.DEGREE + "\nChange in Elevation (m) = " + deltaZStr;
		textDialog.setText(str);	
	}
}
