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
 
Tile Rendering Library - Brian Paul 
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

package gov.nasa.arc.dert.scene.tool;

import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.io.CsvWriter;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.quadtree.QuadTree;
import gov.nasa.arc.dert.scenegraph.HiddenLine;
import gov.nasa.arc.dert.scenegraph.MotionListener;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.scenegraph.PointSet;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.state.PathState;
import gov.nasa.arc.dert.state.WaypointState;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.SpatialUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.util.UIUtil;
import gov.nasa.arc.dert.view.Console;
import gov.nasa.arc.dert.view.world.GroundEdit;
import gov.nasa.arc.dert.viewpoint.ViewDependent;

import java.awt.Color;
import java.util.Properties;

import javax.swing.Icon;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.PickingHint;

/**
 * Provides a map element that is a set of points that can be used as a path or
 * an area. A single way point is added to the Path when it is created. All way
 * points may be removed.
 *
 */
public class Path extends Node implements MotionListener, Tool, ViewDependent {

	// Representative icon
	public static final Icon icon = Icons.getImageIcon("path_16.png");

	// Indicates how to draw the path body
	public static enum BodyType {
		Point, Line, Polygon
	}

	// Indicates what information to put in a waypoint label
	// Distance is from the previous way point
	// Cumulative distance is along the path from the first way point
	// Slope is to the next way point
	public static enum LabelType {
		Name, Distance, CumulativeDistance, Elevation, Slope, Note
	}

	// Default map element properties
	public static BodyType defaultBodyType = BodyType.Line;
	public static LabelType defaultLabelType = LabelType.Name;
	public static float defaultSize = 0.5f, defaultLineWidth = 2;
	public static Color defaultColor = Color.magenta;
	public static boolean defaultLabelVisible = true;
	public static boolean defaultWaypointsVisible = true;

	// Type of waypoint label
	private LabelType labelType = LabelType.Name;

	// How to draw the path body
	private BodyType bodyType;

	// Scene graph element to draw line body
	private HiddenLine line;

	// Scene graph element to draw polygon (area) body
	private Mesh poly;

	// Scene graph element group that manages and renders waypoints
	private PointSet pointSet;

	// Object used in coordinate conversion
	private Vector3 coord = new Vector3();

	// Index of the selected way point
	private int currentIndex = -1;

	// map element properties for rendering
	private double lineWidth;
	private ColorRGBA colorRGBA;
	private Color color;
	private double size;
	private boolean labelVisible, locked, waypointsVisible;
	private boolean lineIsEnabled, polyIsEnabled;

	// Indicates if this path is currently being created
	private int newPoints;

	// The map element state object
	protected PathState state;

	private Vector3 lowerBound, upperBound;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public Path(PathState state) {
		super(state.name);
		lowerBound = new Vector3();
		upperBound = new Vector3();
		this.state = state;
		state.setMapElement(this);

		this.labelType = state.labelType;
		this.bodyType = state.bodyType;
		this.size = state.size;
		this.labelVisible = state.labelVisible;
		this.waypointsVisible = state.waypointsVisible;
		this.locked = state.locked;
		this.color = state.color;
		this.lineWidth = state.lineWidth;

		// Create scene graph elements
		pointSet = new PointSet("_points_"+state.name);
		attachChild(pointSet);
		
		line = new HiddenLine("_line", IndexMode.LineStrip);
		line.setLineWidth((float)lineWidth);
		line.getSceneHints().setCullHint(CullHint.Always);
		line.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		line.getSceneHints().setCastsShadows(false);
		attachChild(line);
		poly = pointSet.createPolygon();
		attachChild(poly);

		setColor(state.color);

		for (int i = 0; i < state.pointList.size(); ++i) {
			WaypointState wps = state.pointList.get(i);
			Waypoint currentWaypoint = new Waypoint(wps);
			pointSet.addPoint(currentWaypoint, (int)wps.id);
			currentWaypoint.addMotionListener(this);
			updateLabels(currentWaypoint);
		}

		enableLine(this.bodyType == BodyType.Line);
		enablePolygon(this.bodyType == BodyType.Polygon);

		// Update this node and its children so they will be drawn.
		updateGeometricState(0);
		
		currentIndex = -1;
		setVisible(state.visible);
	}
	
	public void setHiddenDashed(boolean hiddenDashed) {
		line.enableDash(hiddenDashed);
	}

	/**
	 * A way point is moved.
	 */
	@Override
	public void move(Movable d, ReadOnlyVector3 pos) {
		if (lineIsEnabled) {
			pointSet.updateLine(line);
		}
		if (polyIsEnabled) {
			pointSet.updatePolygon(poly);
		}
		updateLabels(null);
		state.pathDirty();
	}

	/**
	 * Get the map element state for this Path.
	 */
	@Override
	public MapElementState getState() {
		return (state);
	}

	/**
	 * Set the Path name.
	 */
	@Override
	public void setName(String name) {
		super.setName(name);

		// Rename the waypoints
		int n = pointSet.getNumberOfChildren();
		for (int i = 0; i < n; ++i) {
			Waypoint wp = (Waypoint) pointSet.getChild(i);
			String str = wp.getName();

			// maintain the index number in the new name
			int p = str.lastIndexOf('.');
			wp.setName(name + str.substring(p));
		}
	}

	/**
	 * Get the number of way points in this Path.
	 * 
	 * @return number
	 */
	public int getNumberOfPoints() {
		return (pointSet.getNumberOfChildren());
	}

	/**
	 * Given the index, get the corresponding way point.
	 * 
	 * @param i
	 *            index
	 * @return the way point
	 */
	public Waypoint getWaypoint(int i) {
		return ((Waypoint) pointSet.getChild(i));
	}

	/**
	 * Get the index for the given way point
	 * 
	 * @param waypoint
	 *            the way point
	 * @return the index
	 */
	public int getWaypointIndex(Waypoint waypoint) {
		return (pointSet.getChildIndex(waypoint));
	}

	/**
	 * Make the way points visible or not.
	 * 
	 * @param show
	 *            true = visible
	 */
	public void setWaypointsVisible(boolean show) {
		waypointsVisible = show;
		if (show) {
			pointSet.getSceneHints().setCullHint(CullHint.Inherit);
		} else {
			pointSet.getSceneHints().setCullHint(CullHint.Always);
		}
		pointSet.markDirty(DirtyType.RenderState);
	}

	/**
	 * Check if way points are visible.
	 * 
	 * @return
	 */
	public boolean areWaypointsVisible() {
		return (waypointsVisible);
	}

	/**
	 * Given a point and an index, create and add a new way point. If the index
	 * is < 0, add the new way point to the end of the path.
	 * 
	 * @param p
	 *            the point
	 * @param index
	 * @return the new way point
	 */
	public Waypoint addWaypoint(ReadOnlyVector3 p, int index) {
		if (index < 0) {
			// add the new way point to the end so get the last index
			index = pointSet.getNumberOfChildren();
		}
		// create a way point state
		WaypointState wpState = new WaypointState(index, p, getName() + ".", size, color, labelVisible, locked);
		// add the new way point and return it
		return (addWaypoint(wpState));
	}

	/**
	 * Create a new way point from a map element state and add it to the Path.
	 * 
	 * @param state
	 * @return the new way point
	 */
	public Waypoint addWaypoint(WaypointState state) {
		// create the way point and add it
		int index = (int) state.id;
		Waypoint currentWaypoint = new Waypoint(state);
		index = pointSet.addPoint(currentWaypoint, index);
		state.id = index;
		currentWaypoint.addMotionListener(this);

		// if we didn't add it to the end, renumber the way points
		if (index < (pointSet.getNumberOfChildren() - 1)) {
			renumberWaypoints(index);
		}

		// update the path way point labels
		updateLabels(currentWaypoint);

		// add body segment for the new point
		enableLine(lineIsEnabled);
		enablePolygon(polyIsEnabled);

//		updateGeometricState(0);
		markDirty(DirtyType.RenderState);
		
		newPoints ++;
		return (currentWaypoint);
	}

	/**
	 * Remove a way point from this Path.
	 * 
	 * @param point
	 *            the way point
	 * @return the index of the removed point
	 */
	public int removeWaypoint(Waypoint point) {
		int index = pointSet.getChildIndex(point);
		if (index < 0) {
			return (index);
		}
		point.removeMotionListener(this);
		pointSet.removePoint(point);
		// if we didn't remove it from the end, renumber the way points
		if (index < (pointSet.getNumberOfChildren())) {
			renumberWaypoints(index);
		}
		// update body
		if (lineIsEnabled)
			pointSet.updateLine(line);
		if (polyIsEnabled)
			pointSet.updatePolygon(poly);
		updateGeometricState(0, false);
		return (index);
	}

	/**
	 * Set the line width.
	 * 
	 * @param width
	 */
	public void setLineWidth(float width) {
		lineWidth = width;
		line.setLineWidth(width);
		markDirty(DirtyType.RenderState);
	}

	/**
	 * Get the line width.
	 * 
	 * @return
	 */
	public float getLineWidth() {
		return ((float)lineWidth);
	}

	/**
	 * Set the line width.
	 * 
	 * @param width
	 */
	public void setPointSize(double size) {
		this.size = size;
		pointSet.setPointSize(size);
		markDirty(DirtyType.RenderState);
	}

	/**
	 * Set the color.
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;

		// set the color of the way points
		int n = pointSet.getNumberOfChildren();
		for (int i = 0; i < n; ++i) {
			((Waypoint) pointSet.getChild(i)).setColor(color);
		}

		// set the color of the body
		line.setColor(color);
//
//		MaterialState lineMS = new MaterialState();
//		lineMS.setDiffuse(new ColorRGBA(0, 0, 0, 1));
//		lineMS.setAmbient(new ColorRGBA(0, 0, 0, 1));
//		lineMS.setEmissive(MaterialState.MaterialFace.FrontAndBack, colorRGBA);
//		line.setRenderState(lineMS);
////		lineMS = new MaterialState();
////		lineMS.setDiffuse(new ColorRGBA(0, 0, 0, 1));
////		lineMS.setAmbient(new ColorRGBA(0, 0, 0, 1));
////		ColorRGBA dashColor = new ColorRGBA(colorRGBA.getRed(), colorRGBA.getGreen(), colorRGBA.getBlue(), colorRGBA.getAlpha()*0.5f);
////		lineMS.setEmissive(MaterialState.MaterialFace.FrontAndBack, dashColor);
//		dashedLine.setRenderState(lineMS);

		colorRGBA = UIUtil.colorToColorRGBA(color);
		MaterialState polyMS = new MaterialState();
		ColorRGBA polyColor = new ColorRGBA(colorRGBA);
		polyColor.setAlpha(0.5f);
		polyMS.setDiffuse(MaterialState.MaterialFace.FrontAndBack, polyColor);
		// polyMS.setEmissive(MaterialState.MaterialFace.FrontAndBack,
		// colorRGBA);
		polyMS.setAmbient(MaterialState.MaterialFace.FrontAndBack, polyColor);
		poly.setRenderState(polyMS);
	}

	/**
	 * Get the color.
	 */
	@Override
	public Color getColor() {
		return (color);
	}

	/**
	 * Update the way point labels when one is moved or otherwise changed.
	 * 
	 * @param wp
	 */
	private void updateLabels(Waypoint wp) {
		switch (labelType) {
		case Name:
			// only update this way point
			if (wp != null) {
				wp.setLabel(wp.getName());
			}
			break;
		case Distance:
			// recompute the distance for surrounding way points
			if (wp != null) {
				updateDistanceLabels(wp);
			}
			break;
		case CumulativeDistance:
			// recompute the cumulative distance for all way points
			updateCumulativeDistanceLabels();
			break;
		case Elevation:
			// only update the elevation for this way point
			if (wp != null) {
				coord.set(wp.getTranslation());
				Landscape.getInstance().localToWorldCoordinate(coord);
				wp.setLabel(StringUtil.format(coord.getZ()));
			}
			break;
		case Slope:
			// update the slopes for surrounding way points
			if (wp != null) {
				updateSlopeLabels(wp);
			}
			break;
		case Note:
			// only update this way point
			if (wp != null) {
				wp.setLabel(wp.getState().getAnnotation());
			}
			break;
		}
		markDirty(DirtyType.RenderState);
	}

	/**
	 * Set the body type.
	 * 
	 * @param t
	 *            the body type
	 */
	public void setBodyType(BodyType t) {
		bodyType = t;
		switch (bodyType) {
		case Point:
			enableLine(false);
			enablePolygon(false);
			break;
		case Line:
			enablePolygon(false);
			enableLine(true);
			break;
		case Polygon:
			enableLine(false);
			enablePolygon(true);
			break;
		}
		markDirty(DirtyType.RenderState);
	}

	/**
	 * Get the body type.
	 * 
	 * @return
	 */
	public BodyType getBodyType() {
		return (bodyType);
	}

	/**
	 * Set the current way point.
	 * 
	 * @param waypoint
	 */
	public void setCurrentWaypoint(Waypoint waypoint) {
		// Set the current way point index.
		currentIndex = -1;
		if (waypoint == null) {
			return;
		}
		for (int i = 0; i < pointSet.getNumberOfChildren(); ++i) {
			if (pointSet.getChild(i) == waypoint) {
				currentIndex = i;
				break;
			}
		}
	}

	/**
	 * Set the label type.
	 * 
	 * @param type
	 */
	public void setLabelType(LabelType type) {
		labelType = type;
		// update all way point labels
		if (labelType == LabelType.CumulativeDistance) {
			updateCumulativeDistanceLabels();
		} else {
			for (int i = 0; i < pointSet.getNumberOfChildren(); ++i) {
				Waypoint wp = (Waypoint) pointSet.getChild(i);
				updateLabels(wp);
			}
		}
		markDirty(DirtyType.RenderState);
	}

	/**
	 * Get the label type.
	 * 
	 * @return
	 */
	public LabelType getLabelType() {
		return (labelType);
	}

	/**
	 * Is the map element visible.
	 */
	@Override
	public boolean isVisible() {
		return (SpatialUtil.isDisplayed(this));
	}

	/**
	 * Set the map element visibility.
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			getSceneHints().setCullHint(CullHint.Dynamic);
		} else {
			getSceneHints().setCullHint(CullHint.Always);
		}
		markDirty(DirtyType.RenderState);
	}
	
	public void complete() {
		Console.println("Added "+newPoints+" point"+((newPoints > 1) ? "s" : "")+" to "+getName()+".");
		newPoints = 0;
	}

	/**
	 * The user has clicked in the scene. Add a way point.
	 * 
	 * @param loc
	 *            the location of the new way point
	 */
	public void click(ReadOnlyVector3 loc) {
		if (currentIndex < 0) {
			addWaypoint(loc, -1);
		} else {
			addWaypoint(loc, currentIndex + 1);
		}
	}

	/**
	 * Get the way point icon size.
	 */
	@Override
	public double getSize() {
		return (size);
	}

	/**
	 * A quad tree has merged or split. Update the elevation of each way point
	 * if it is inside the quad tree.
	 * 
	 * @param quadTree
	 *            the Quad Tree.
	 */
	@Override
	public boolean updateElevation(QuadTree quadTree) {
		boolean result = false;
		for (int i = 0; i < pointSet.getNumberOfChildren(); ++i) {
			Waypoint wp = (Waypoint) pointSet.getChild(i);
			result |= wp.updateElevation(quadTree);
		}
		return (result);
	}

	/**
	 * Get the map element type.
	 */
	@Override
	public Type getType() {
		return (Type.Path);
	}

	/**
	 * Get the point to seek to.
	 */
	@Override
	public double getSeekPointAndDistance(Vector3 point) {
		if (pointSet.getNumberOfChildren() == 0) {
			return (Double.NaN);
		}
		Waypoint wp = getWaypoint(0);
		if (wp != null) {
			return (wp.getSeekPointAndDistance(point));
		}
		return (Double.NaN);
	}

	/**
	 * Get the icon that represents this map element.
	 */
	@Override
	public Icon getIcon() {
		return (icon);
	}

	/**
	 * Is this map element locked?
	 */
	@Override
	public boolean isLocked() {
		return (locked);
	}

	/**
	 * Lock down this map element so it cannot be moved.
	 */
	@Override
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	/**
	 * Is this map element's label visible?
	 */
	@Override
	public boolean isLabelVisible() {
		return (labelVisible);
	}

	/**
	 * Set the label on this map element visible.
	 */
	@Override
	public void setLabelVisible(boolean labelVisible) {
		this.labelVisible = labelVisible;
		for (int i = 0; i < pointSet.getNumberOfChildren(); ++i) {
			((Waypoint) pointSet.getChild(i)).setLabelVisible(labelVisible);
		}
		updateGeometricState(0);
	}

	/**
	 * Change the vertical exaggeration for this map element.
	 */
	@Override
	public void setVerticalExaggeration(double vertExag, double oldVertExag, double minZ) {
		for (int i = 0; i < pointSet.getNumberOfChildren(); ++i) {
			Waypoint wp = (Waypoint) pointSet.getChild(i);
			wp.setVerticalExaggeration(vertExag, oldVertExag, minZ);
		}
		if (lineIsEnabled) {
			pointSet.updateLine(line);
		}
		if (polyIsEnabled) {
			pointSet.updatePolygon(poly);
		}
	}

	/**
	 * Update the map element icons according to the camera position.
	 */
	@Override
	public void update(BasicCamera camera) {
		for (int i = 0; i < pointSet.getNumberOfChildren(); ++i) {
			((Waypoint) pointSet.getChild(i)).update(camera);
		}
	}

	/**
	 * Dispose of this map element's resources.
	 */
	public void dispose() {
		// nothing here
	}

	/**
	 * Write this path's way points to a file in comma separated value format.
	 * 
	 * @param filename
	 */
	public void saveAsCsv(String filename) {
		CsvWriter csvWriter = null;
		try {
			int n = pointSet.getNumberOfChildren();
			String[] column = { "Index", "Name", "X", "Y", "Z", "Annotation" };
			csvWriter = new CsvWriter(filename, column);
			csvWriter.open();
			String[] value = new String[column.length];
			Landscape landscape = Landscape.getInstance();
			Vector3 coord = new Vector3();
			for (int i = 0; i < n; ++i) {
				Waypoint wp = (Waypoint) pointSet.getChild(i);
				coord.set(wp.getTranslation());
				double elev = landscape.getElevationAtHighestLevel(coord.getX(), coord.getY());
				landscape.localToWorldCoordinate(coord);
				coord.setZ(elev);
				value[0] = Integer.toString(i);
				value[1] = wp.getName();
				value[2] = Double.toString(coord.getX());
				value[3] = Double.toString(coord.getY());
				value[4] = Double.toString(coord.getZ());
				value[5] = wp.getState().getAnnotation();
				csvWriter.writeLine(value);
			}
			csvWriter.close();
			Console.println(n + " records saved to " + filename);
		} catch (Exception e) {
			e.printStackTrace();
			if (csvWriter != null) {
				try {
					csvWriter.close();
				} catch (Exception e2) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public String toString() {
		return (getName());
	}

	private void updateDistanceLabels(Waypoint wp) {
		int index = getWaypointIndex(wp);
		// first way point does not have a distance
		if (index == 0) {
			wp.setLabel(StringUtil.format(0));
		} else {
			Waypoint wp0 = getWaypoint(index - 1);
			ReadOnlyVector3 p0 = wp0.getTranslation();
			ReadOnlyVector3 p1 = wp.getTranslation();
			double d = p0.distance(p1);
			wp.setLabel(StringUtil.format(d));
		}
		// if not last way point, update next way point label
		if (index < (pointSet.getNumberOfChildren() - 1)) {
			Waypoint wp0 = wp;
			wp = getWaypoint(index + 1);
			ReadOnlyVector3 p0 = wp0.getTranslation();
			ReadOnlyVector3 p1 = wp.getTranslation();
			double d = p0.distance(p1);
			wp.setLabel(StringUtil.format(d));
		}
	}

	private void updateCumulativeDistanceLabels() {
		int pointCount = pointSet.getNumberOfChildren();
		Waypoint wp0 = (Waypoint) pointSet.getChild(0);
		double distance = 0;
		for (int i = 0; i < pointCount; ++i) {
			Waypoint wp = (Waypoint) pointSet.getChild(i);
			ReadOnlyVector3 p0 = wp0.getTranslation();
			ReadOnlyVector3 p1 = wp.getTranslation();
			double d = p0.distance(p1);
			distance += d;
			wp.setLabel(StringUtil.format(distance));
			wp0 = wp;
		}
	}

	private void updateSlopeLabels(Waypoint wp) {
		int index = getWaypointIndex(wp);
		// last way point does not have a slope
		if (index == (pointSet.getNumberOfChildren() - 1)) {
			wp.setLabel(StringUtil.format(0));
		} else {
			Waypoint wp1 = getWaypoint(index + 1);
			ReadOnlyVector3 p0 = wp.getTranslation();
			ReadOnlyVector3 p1 = wp1.getTranslation();
			double s = MathUtil.getSlopeFromLine(p0, p1);
			wp.setLabel(StringUtil.format(s));
		}
		// if not first way point, update previous way point slope
		if (index > 0) {
			Waypoint wp1 = wp;
			wp = getWaypoint(index - 1);
			ReadOnlyVector3 p0 = wp.getTranslation();
			ReadOnlyVector3 p1 = wp1.getTranslation();
			double s = MathUtil.getSlopeFromLine(p0, p1);
			wp.setLabel(StringUtil.format(s));
		}
	}

	private String createWaypointLabel(int index) {
		if (index < 0) {
			index = pointSet.getNumberOfChildren();
		}
		String str = getName() + "." + index;
		return (str);
	}

	private void renumberWaypoints(int from) {
		for (int i = from; i < pointSet.getNumberOfChildren(); ++i) {
			pointSet.getChild(i).setName(createWaypointLabel(i));
			((Waypoint)pointSet.getChild(i)).getState().id = i;
		}
	}

	private void enableLine(boolean enable) {
		if (enable) {
			pointSet.updateLine(line);
			if (pointSet.getNumberOfChildren() > 1) {
				line.getSceneHints().setCullHint(CullHint.Inherit);
			} else {
				line.getSceneHints().setCullHint(CullHint.Always);
			}
			lineIsEnabled = true;
		} else {
			line.getSceneHints().setCullHint(CullHint.Always);
			lineIsEnabled = false;
		}
	}

	private void enablePolygon(boolean enable) {
		if (enable) {
			pointSet.updatePolygon(poly);
			if (pointSet.getNumberOfChildren() > 2) {
				poly.getSceneHints().setCullHint(CullHint.Inherit);
			} else {
				poly.getSceneHints().setCullHint(CullHint.Always);
			}
			polyIsEnabled = true;
		} else {
			poly.getSceneHints().setCullHint(CullHint.Always);
			poly.getSceneHints().setPickingHint(PickingHint.Pickable, false);
			polyIsEnabled = false;
		}
//		pointSet.showWaypointsAsSpheres(polyIsEnabled);
//		updateGeometricState(0, true);
	}
	
	public int getDimensions(Vector3 lBound, Vector3 uBound, Vector3 centroid, double[] distArea) {
		int n = getNumberOfPoints();
		if (n > 0) {
			pointSet.updatePolygon(poly);
			pointSet.getBounds(lowerBound, upperBound);
			lBound.set(lowerBound);
			uBound.set(upperBound);
			centroid.set(pointSet.getCentroid());
			distArea[0] = pointSet.getDistance();
		}
		if (n < 3) {
			distArea[1] = Double.NaN;
		}
		else {
			Vector3[] vertex = new Vector3[n + 1];
			for (int i = 0; i < n; ++i) {
				vertex[i] = new Vector3(pointSet.getChild(i).getTranslation());
			}
			vertex[n] = new Vector3(vertex[0]);

			distArea[1] = pointSet.getArea();
		}
		return(n);
	}
	
	public Vector3[] getPolygonVertices() {
		return(pointSet.getPolygonVertices());
	}
	
	public double[] getVolume(double volElev) {
		int n = getNumberOfPoints();
		double[] vol = null;
		if (n >= 3) {
			Vector3[] vertex = pointSet.getPolygonVertices();
			if (!Double.isNaN(volElev)) {
				vol = Landscape.getInstance().getSampledVolumeOfRegion(vertex, lowerBound, upperBound, volElev);
			}
			else {
				poly.getSceneHints().setPickingHint(PickingHint.Pickable, true);
				vol = Landscape.getInstance().getSampledVolumeOfRegion(vertex, lowerBound, upperBound, poly);
				poly.getSceneHints().setPickingHint(PickingHint.Pickable, false);
			}
		}
		return(vol);
	}

//	public String getStatistics() {
//		pointSet.updatePolygon(poly);
//		int n = getNumberOfPoints();
//		String str = "";
//		try {
//			str += "Number of Waypoints: " + n + "\n";
//			if (n == 0) {
//				str += "Lower Bounds: N/A\n";
//				str += "Upper Bounds: N/A\n";
//				str += "Centroid: N/A\n";
//				str += "Total Path Distance: N/A\n";
//				str += "Planimetric Area: N/A\n";
//				str += "Mean Elevation: N/A\n";
//				str += "Mean Slope: N/A\n";
//				str += "Volume: N/A\n";
//				str += "Surface Area: N/A\n";
//			}
//			pointSet.getBounds(lowerBound, upperBound);
//			tmpVec.set(lowerBound);
//			Landscape landscape = Landscape.getInstance();
//			landscape.localToWorldCoordinate(tmpVec);
//			str += "Lower Bounds: " + String.format(Landscape.stringFormat, tmpVec.getXf()) + "," + String.format(Landscape.stringFormat, tmpVec.getYf()) + "," + String.format(Landscape.stringFormat, tmpVec.getZf()) + "\n";
//			tmpVec.set(upperBound);
//			landscape.localToWorldCoordinate(tmpVec);
//			str += "Upper Bounds: " + String.format(Landscape.stringFormat, tmpVec.getXf()) + "," + String.format(Landscape.stringFormat, tmpVec.getYf()) + "," + String.format(Landscape.stringFormat, tmpVec.getZf()) + "\n";
//			Vector3 pos = pointSet.getCentroid();
//			landscape.localToWorldCoordinate(pos);
//			str += "Centroid: " + String.format(Landscape.stringFormat, pos.getXf()) + "," + String.format(Landscape.stringFormat, pos.getYf()) + "," + String.format(Landscape.stringFormat, pos.getZf()) + "\n";
//			str += "Total Path Distance: " + String.format(Landscape.stringFormat, pointSet.getDistance()) + "\n";
//	
//			if (n < 3) {
//				str += "Planimetric Area: N/A\n";
//				float mElev = 0;
//				for (int i = 0; i < n; ++i) {
//					mElev += pointSet.getChild(i).getWorldTranslation().getZf();
//				}
//				mElev /= n;
//				str += "Mean Elevation: " + String.format(Landscape.stringFormat, mElev) + "\n";
//				str += "Mean Slope: N/A\n";
//				str += "Volume: N/A\n";
//				str += "Surface Area: N/A\n";
//			} else {
//				Vector3[] vertex = new Vector3[n + 1];
//				for (int i = 0; i < n; ++i) {
//					vertex[i] = new Vector3(pointSet.getChild(i).getWorldTranslation());
//				}
//				vertex[n] = new Vector3(vertex[0]);
//	
//				str += "Planimetric Area: " + String.format(Landscape.stringFormat, pointSet.getArea()) + "\n";
//				double sampledVal = landscape.getSampledMeanElevationOfRegion(vertex, lowerBound, upperBound);
//				if (Double.isNaN(sampledVal))
//					return(str);
//				str += "Mean Elevation: "+String.format(Landscape.stringFormat, sampledVal)+"\n";
//				sampledVal = landscape.getSampledMeanSlopeOfRegion(vertex, lowerBound, upperBound);
//				if (Double.isNaN(sampledVal))
//					return(str);
//				str += "Mean Slope: "+String.format(Landscape.stringFormat, sampledVal)+"\n";
//				sampledVal = landscape.getSampledSurfaceAreaOfRegion(vertex, lowerBound, upperBound);
//				if (Double.isNaN(sampledVal))
//					return(str);
//				str += "Surface Area: "+String.format(Landscape.stringFormat, sampledVal)+"\n";
//				
//				if (doVolume) {
//					double[] vol = null;
//					String limit = null;
//					if (!Double.isNaN(volElev)) {
//						vol = landscape.getSampledVolumeOfRegion(vertex, lowerBound, upperBound, volElev);
//						limit = "Elevation "+String.format(Landscape.stringFormat, volElev);
//					}
//					else {
//						poly.getSceneHints().setPickingHint(PickingHint.Pickable, true);
//						vol = landscape.getSampledVolumeOfRegion(vertex, lowerBound, upperBound, poly);
//						poly.getSceneHints().setPickingHint(PickingHint.Pickable, false);
//						limit = "Polygon";
//					}
//					if (vol == null)
//						return(str);
//					str += "Volume Above "+limit+": " + String.format(Landscape.stringFormat, vol[0]) + "\n";
//					str += "Volume Below "+limit+": " + String.format(Landscape.stringFormat, vol[1]) + "\n";
//				}
//			}
//		}
//		catch (Exception e) {
//			// do nothing
//			e.printStackTrace();
//		}
//		return (str);
//	}

	/**
	 * Get a string containing a list of the points with names, locations and
	 * optionally annotations separated with commas and new lines.
	 * 
	 * @param withText
	 * @return
	 */
	public String list(boolean withText) {
		StringBuffer sBuf = new StringBuffer();
		Vector3 coord = new Vector3();
		for (int i = 0; i < pointSet.getNumberOfChildren(); ++i) {
			Waypoint wp = (Waypoint) pointSet.getChild(i);
			coord.set(wp.getTranslation());
			Landscape.getInstance().localToWorldCoordinate(coord);
			if (withText) {
				sBuf.append(wp.getName() + "," + wp.getState().getAnnotation() + "," + coord.getX() + ","
					+ coord.getY() + "," + coord.getZ() + "\n");
			} else {
				sBuf.append(coord.getX() + "," + coord.getY() + "," + coord.getZ() + "\n");
			}
		}
		return (sBuf.toString());
	}

	public ReadOnlyVector3 getLocationInWorld() {
//		if (pointSet.getNumberOfChildren() == 0) {
//			location.set(getWorldTranslation());
//			Landscape.getInstance().localToWorldCoordinate(location);
//			return (location);
//		} else {
//			Waypoint wp = (Waypoint) pointSet.getChild(0);
//			return (wp.getLocationInWorld());
//		}
		return(null);
	}
	
	public GroundEdit ground() {
		int n = pointSet.getNumberOfChildren();
		GroundEdit[] groundEdit = new GroundEdit[n];
		for (int i = 0; i < n; ++i) {
			Waypoint wp = (Waypoint)pointSet.getChild(i);
			groundEdit[i] = wp.ground();
			
		}
		GroundEdit ge = new GroundEdit(this, groundEdit);
		return(ge);
	}
	
	public void setZOffset(double zOff, boolean doTrans) {
		// do nothing
	}
	
	public double getZOffset() {
		return(0);
	}
	
	public Vector3[] getCurve(int steps) {
		return(pointSet.getCurve(steps));
	}
	
	public double getCenterElevation() {
		Vector3 pos = pointSet.getCentroid();
		Landscape.getInstance().localToWorldCoordinate(pos);
		return(pos.getZ());
	}

	/**
	 * Set defaults
	 * 
	 * @param properties
	 */
	public static void setDefaultsFromProperties(Properties properties) {
		defaultColor = StringUtil.getColorValue(properties, "MapElement.Path.defaultColor", defaultColor, false);
		defaultSize = (float) StringUtil.getDoubleValue(properties, "MapElement.Path.defaultSize", true, defaultSize,
			false);
		defaultLineWidth = (float) StringUtil.getDoubleValue(properties, "MapElement.Path.defaultLineWidth", true,
			defaultLineWidth, false);
		String str = properties.getProperty("MapElement.Path.defaultBodyType", defaultBodyType.toString());
		defaultBodyType = BodyType.valueOf(str);
		str = properties.getProperty("MapElement.Path.defaultLabelType", defaultLabelType.toString());
		defaultLabelType = LabelType.valueOf(str);
		defaultLabelVisible = StringUtil.getBooleanValue(properties, "MapElement.Path.defaultLabelVisible",
			defaultLabelVisible, false);
		defaultWaypointsVisible = StringUtil.getBooleanValue(properties, "MapElement.Path.defaultWaypointsVisible",
			defaultWaypointsVisible, false);
	}

	/**
	 * Save defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		properties.setProperty("MapElement.Path.defaultColor", StringUtil.colorToString(defaultColor));
		properties.setProperty("MapElement.Path.defaultSize", Float.toString(defaultSize));
		properties.setProperty("MapElement.Path.defaultLineWidth", Float.toString(defaultLineWidth));
		properties.setProperty("MapElement.Path.defaultBodyType", defaultBodyType.toString());
		properties.setProperty("MapElement.Path.defaultLabelType", defaultLabelType.toString());
		properties.setProperty("MapElement.Path.defaultWaypointsVisible", Boolean.toString(defaultWaypointsVisible));
		properties.setProperty("MapElement.Path.defaultLabelVisible", Boolean.toString(defaultLabelVisible));
	}
	
	public static BodyType stringToBodyType(String str) {
		if (str == null)
			return(defaultBodyType);
		try {
			BodyType bt = BodyType.valueOf(str);
			return(bt);
		}
		catch (Exception e) {
			return(defaultBodyType);
		}
	}
	
	public static LabelType stringToLabelType(String str) {
		if (str == null)
			return(defaultLabelType);
		try {
			LabelType lt = LabelType.valueOf(str);
			return(lt);
		}
		catch (Exception e) {
			return(defaultLabelType);
		}
	}
}
