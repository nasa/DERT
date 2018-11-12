/**

DERT is a viewer for digital terrain models created from data collected during NASA missions.

DERT is Released in under the NASA Open Source Agreement (NOSA) found in the “LICENSE” folder where you
downloaded DERT.

DERT includes 3rd Party software. The complete copyright notice listing for DERT is:

Copyright © 2015 United States Government as represented by the Administrator of the National Aeronautics and
Space Administration.  No copyright is claimed in the United States under Title 17, U.S.Code. All Other Rights
Reserved.

Desktop Exploration of Remote Terrain (DERT) could not have been written without the aid of a number of free,
open source libraries. These libraries and their notices are listed below. Find the complete third party license
listings in the separate “DERT Third Party Licenses” pdf document found where you downloaded DERT in the
LICENSE folder.
 
JogAmp Ardor3D Continuation
Copyright © 2008-2012 Ardor Labs, Inc.
 
JogAmp
Copyright 2010 JogAmp Community. All rights reserved.
 
JOGL Portions Sun Microsystems
Copyright © 2003-2009 Sun Microsystems, Inc. All Rights Reserved.
 
JOGL Portions Silicon Graphics
Copyright © 1991-2000 Silicon Graphics, Inc.
 
Light Weight Java Gaming Library Project (LWJGL)
Copyright © 2002-2004 LWJGL Project All rights reserved.
 
Tile Rendering Library - Brain Paul 
Copyright © 1997-2005 Brian Paul. All Rights Reserved.
 
OpenKODE, EGL, OpenGL , OpenGL ES1 & ES2
Copyright © 2007-2010 The Khronos Group Inc.
 
Cg
Copyright © 2002, NVIDIA Corporation
 
Typecast - David Schweinsberg 
Copyright © 1999-2003 The Apache Software Foundation. All rights reserved.
 
PNGJ - Herman J. Gonzalez and Shawn Hartsock
Copyright © 2004 The Apache Software Foundation. All rights reserved.
 
Apache Harmony - Open Source Java SE
Copyright © 2006, 2010 The Apache Software Foundation.
 
Guava
Copyright © 2010 The Guava Authors
 
GlueGen Portions
Copyright © 2010 JogAmp Community. All rights reserved.
 
GlueGen Portions - Sun Microsystems
Copyright © 2003-2005 Sun Microsystems, Inc. All Rights Reserved.
 
SPICE
Copyright © 2003, California Institute of Technology.
U.S. Government sponsorship acknowledged.
 
LibTIFF
Copyright © 1988-1997 Sam Leffler
Copyright © 1991-1997 Silicon Graphics, Inc.
 
PROJ.4
Copyright © 2000, Frank Warmerdam

LibJPEG - Independent JPEG Group
Copyright © 1991-2018, Thomas G. Lane, Guido Vollbeding
 

Disclaimers

No Warranty: THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY KIND,
EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
THAT THE SUBJECT SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY
WARRANTY THAT THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE. THIS AGREEMENT
DOES NOT, IN ANY MANNER, CONSTITUTE AN ENDORSEMENT BY GOVERNMENT AGENCY OR ANY
PRIOR RECIPIENT OF ANY RESULTS, RESULTING DESIGNS, HARDWARE, SOFTWARE PRODUCTS OR
ANY OTHER APPLICATIONS RESULTING FROM USE OF THE SUBJECT SOFTWARE.  FURTHER,
GOVERNMENT AGENCY DISCLAIMS ALL WARRANTIES AND LIABILITIES REGARDING THIRD-PARTY
SOFTWARE, IF PRESENT IN THE ORIGINAL SOFTWARE, AND DISTRIBUTES IT "AS IS."

Waiver and Indemnity:  RECIPIENT AGREES TO WAIVE ANY AND ALL CLAIMS AGAINST THE UNITED
STATES GOVERNMENT, ITS CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR
RECIPIENT.  IF RECIPIENT'S USE OF THE SUBJECT SOFTWARE RESULTS IN ANY LIABILITIES,
DEMANDS, DAMAGES, EXPENSES OR LOSSES ARISING FROM SUCH USE, INCLUDING ANY DAMAGES
FROM PRODUCTS BASED ON, OR RESULTING FROM, RECIPIENT'S USE OF THE SUBJECT SOFTWARE,
RECIPIENT SHALL INDEMNIFY AND HOLD HARMLESS THE UNITED STATES GOVERNMENT, ITS
CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR RECIPIENT, TO THE EXTENT
PERMITTED BY LAW.  RECIPIENT'S SOLE REMEDY FOR ANY SUCH MATTER SHALL BE THE IMMEDIATE,
UNILATERAL TERMINATION OF THIS AGREEMENT.

**/

package gov.nasa.arc.dert.scene.tapemeasure;

import gov.nasa.arc.dert.action.edit.CoordListener;
import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scenegraph.FigureMarker;
import gov.nasa.arc.dert.scenegraph.HiddenLine;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.ui.TextDialog;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.util.UIUtil;
import gov.nasa.arc.dert.viewpoint.ViewDependent;

import java.awt.Color;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.MaterialFace;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.PickingHint;

/**
 * Provides a measuring tool that simulates a tape measure. It consists
 * of two dots with a line inbetween that stretches like a rubberband.
 *
 */
public class TapeMeasure extends Node implements ViewDependent, CoordListener {

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
	protected HiddenLine line;

	// The figures for the end points
	protected FigureMarker currentPoint, anchorPoint;

	// Anchor point coordinate
	protected Vector3 anchor = new Vector3(), current = new Vector3();

	// Size of the end points
	protected float pointSize = 0.4f;

	// Default line segment color
	protected Color lineColor = Color.red;

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
		anchorPoint = new FigureMarker("_anchor", Vector3.ZERO, pointSize, 0, anchorColor, false, true, false);
		anchorPoint.setShape(ShapeType.sphere, false);
		anchorPoint.getSceneHints().setAllPickingHints(false);
		MaterialState materialState = (MaterialState) anchorPoint.getLocalRenderState(StateType.Material);
		materialState.setAmbient(MaterialFace.FrontAndBack, UIUtil.colorToColorRGBA(anchorColor));
		materialState.setEmissive(MaterialFace.FrontAndBack, UIUtil.colorToColorRGBA(anchorColor));
		attachChild(anchorPoint);

		currentPoint = new FigureMarker("_current", new Vector3(0, 0, 1), pointSize, 0, currentColor, false, true, false);
		currentPoint.setShape(ShapeType.sphere, false);
		currentPoint.getSceneHints().setAllPickingHints(false);
		materialState = (MaterialState) currentPoint.getLocalRenderState(StateType.Material);
		materialState.setAmbient(MaterialFace.FrontAndBack, UIUtil.colorToColorRGBA(currentColor));
		materialState.setEmissive(MaterialFace.FrontAndBack, UIUtil.colorToColorRGBA(currentColor));
		attachChild(currentPoint);
		createLine();
		attachChild(line);

		updateGeometricState(0);
	}
	
	public void setHiddenDashed(boolean hiddenDashed) {
		line.enableDash(hiddenDashed);
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
		line.setPoints(anchor, current);
	}

	protected void createLine() {
		line = new HiddenLine("_tapeline", anchorPoint.getTranslation(), currentPoint.getTranslation());
		line.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		line.setModelBound(new BoundingBox());
		line.updateModelBound();
		line.setColor(lineColor);
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
