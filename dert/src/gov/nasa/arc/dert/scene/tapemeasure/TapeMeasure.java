package gov.nasa.arc.dert.scene.tapemeasure;

import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scenegraph.Rubberband;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.ui.TextDialog;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.viewpoint.BasicCamera;

import java.awt.Color;

import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;

/**
 * Provides a measuring tool that simulates a tape measure.
 *
 */
public class TapeMeasure extends Rubberband {

	// Measurement information dialog
	private TextDialog textDialog;

	// Measurement information strings
	private String anchorStr = "", currentStr = "", distanceStr = "", gradientStr = "", deltaZStr = "", azStr = "";

	// Aspect calculation
	private Vector2 work = new Vector2();

	// Temporary
	private Vector3 tmpVec;

	/**
	 * Constructor
	 */
	public TapeMeasure() {
		super("Tape", 0.4f, ShapeType.sphere, Color.red, Color.blue, Color.white);
		tmpVec = new Vector3();
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
			setCurrentPosition(pos);
			break;
		}
	}

	private void setAnchorPosition(ReadOnlyVector3 coord) {
		tmpVec.set(coord);
		World.getInstance().getLandscape().localToWorldCoordinate(tmpVec);
		anchorStr = StringUtil.format(tmpVec);
		distanceStr = "0.0";
		gradientStr = "0.0";
		deltaZStr = "0.0";
		azStr = "0.0";
	}

	private void setCurrentPosition(ReadOnlyVector3 coord) {
		tmpVec.set(coord);
		World.getInstance().getLandscape().localToWorldCoordinate(tmpVec);
		currentStr = StringUtil.format(tmpVec);
		distanceStr = StringUtil.format(coord.distance(anchor));
		gradientStr = StringUtil.format(MathUtil.getSlopeFromLine(anchor, coord));
		deltaZStr = StringUtil.format(coord.getZf() - anchor.getZf());
		azStr = StringUtil.format(MathUtil.getAspectFromLine(anchor, coord, work));
		if (textDialog != null) {
			String str = "Anchor (m) = " + anchorStr + "\nCurrent (m) = " + currentStr + "\nDistance (m) = "
				+ distanceStr + "\nSlope = " + gradientStr + StringUtil.DEGREE + "\nAzimuth = " + azStr
				+ StringUtil.DEGREE + "\nChange in Elevation (m) = " + deltaZStr;
			textDialog.setText(str);
		}
	}

	/**
	 * Update the dialog
	 */
	@Override
	public void update(BasicCamera camera) {
		super.update(camera);
		if (textDialog != null) {
			String str = "Anchor (m) = " + anchorStr + "\nCurrent (m) = " + currentStr + "\nDistance (m) = "
				+ distanceStr + "\nSlope = " + gradientStr + StringUtil.DEGREE + "\nAzimuth = " + azStr
				+ StringUtil.DEGREE + "\nChange in Elevation (m) = " + deltaZStr;
			textDialog.setText(str);
		}
	}

	/**
	 * User clicked in the scene. Place an endpoint of the tape.
	 * 
	 * @param vec
	 */
	public void click(ReadOnlyVector3 vec) {
		switch (mode) {
		case Anchor:
			setAnchorPoint(vec);
			setAnchorPosition(vec);
			mode = ModeType.Current;
			break;
		case Current:
			setCurrentPoint(vec);
			setCurrentPosition(vec);
			mode = ModeType.Anchor;
			break;
		}
	}
}
