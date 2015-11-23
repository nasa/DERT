package gov.nasa.arc.dert.scene.tool;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.io.CsvWriter;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.QuadTree;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scenegraph.MotionListener;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.scenegraph.PointSet;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.state.PathState;
import gov.nasa.arc.dert.state.WaypointState;
import gov.nasa.arc.dert.ui.TextDialog;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.Console;
import gov.nasa.arc.dert.viewpoint.BasicCamera;
import gov.nasa.arc.dert.viewpoint.ViewDependent;
import gov.nasa.arc.dert.viewpoint.ViewpointStore;

import java.awt.Color;
import java.util.Properties;

import javax.swing.Icon;

import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.scenegraph.Line;
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

	// Representative icon
	public static final Icon icon = Icons.getImageIcon("path.png");

	// Default map element properties
	public static BodyType defaultBodyType = BodyType.Line;
	public static LabelType defaultLabelType = LabelType.Name;
	public static float defaultSize = 1, defaultLineWidth = 2;
	public static Color defaultColor = Color.magenta;
	public static boolean defaultLabelVisible = true;
	public static boolean defaultWaypointsVisible = true;
	public static boolean defaultPinned = false;

	// Type of waypoint label
	private LabelType labelType = LabelType.Name;

	// How to draw the path body
	private BodyType bodyType;

	// Scene graph element to draw line body
	private Line line;

	// Scene graph element to draw polygon (area) body
	private Mesh poly;

	// Scene graph element group that manages and renders waypoints
	private PointSet pointSet;

	// Object used in coordinate conversion
	private Vector3 coord = new Vector3();

	// Index of the selected way point
	private int currentIndex = -1;

	// map element properties for rendering
	private float lineWidth;
	private ColorRGBA colorRGBA;
	private Color color;
	private double size;
	private boolean labelVisible, pinned, waypointsVisible;
	private boolean lineIsEnabled, polyIsEnabled;

	// Dialog for displaying path statistics
	private TextDialog statisticsDialog;

	// Indicates if this path is currently being created
	private boolean newPath;

	// The map element state object
	protected PathState state;

	private Vector3 lowerBound, upperBound, tmpVec, location;
	
	private Thread statThread;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public Path(PathState state) {
		super(state.name);
		lowerBound = new Vector3();
		upperBound = new Vector3();
		tmpVec = new Vector3();
		location = new Vector3();
		this.state = state;
		state.setMapElement(this);

		this.labelType = state.labelType;
		this.bodyType = state.bodyType;
		this.size = state.size;
		this.labelVisible = state.labelVisible;
		this.waypointsVisible = state.waypointsVisible;
		this.pinned = state.pinned;
		this.color = state.color;
		this.lineWidth = state.lineWidth;

		newPath = true;

		// Create scene graph elements
		pointSet = new PointSet("_points");
		attachChild(pointSet);
		line = pointSet.createLine();
		line.setLineWidth(lineWidth);
		attachChild(line);
		poly = pointSet.createPolygon();
		attachChild(poly);

		setColor(state.color);

		for (int i = 0; i < state.pointList.size(); ++i) {
			WaypointState wps = state.pointList.get(i);
			addWaypoint(wps);
		}

		setVisible(state.visible);
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
		updateStatistics();
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
	 * Show statistics for the Path in a separate window.
	 */
	public void open() {
		if (statThread != null) {
			return;
		}

		// create the window if it doesn't exist
		if (statisticsDialog == null) {
			statisticsDialog = new TextDialog(null, getName() + " Statistics", 600, 250, true, true) {
				@Override
				public void refresh() {
					open();
				}
			};
		}

		statisticsDialog.setMessage("Calculating ...");

		// update the window and open it
		statisticsDialog.setColor(Color.black);
		statisticsDialog.setText("");
		statisticsDialog.open();

		statThread = new Thread(new Runnable() {
			@Override
			public void run() {
				String str = getStatistics();
				statisticsDialog.setText(str);
				statisticsDialog.setMessage("");
				statThread = null;
			}
		});
		statThread.start();
	}

	/**
	 * Notify user that the currently displayed statistics is old. We don't
	 * automatically update the window for performance reasons.
	 */
	public void updateStatistics() {
		if (statisticsDialog == null) {
			return;
		}
		statisticsDialog.setColor(Color.gray);
		statisticsDialog.setMessage("Press refresh to recalculate.");
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
		WaypointState wpState = new WaypointState(index, p, getName() + ".", size, color, labelVisible, pinned);
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

		updateGeometricState(0);
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
		enableLine(lineIsEnabled);
		enablePolygon(polyIsEnabled);
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
		return (lineWidth);
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
		float[] col = color.getRGBComponents(null);
		colorRGBA = new ColorRGBA(col[0], col[1], col[2], col[3]);

		MaterialState lineMS = new MaterialState();
		lineMS.setDiffuse(new ColorRGBA(0, 0, 0, 1));
		lineMS.setAmbient(new ColorRGBA(0, 0, 0, 1));
		lineMS.setEmissive(MaterialState.MaterialFace.FrontAndBack, colorRGBA);
		line.setRenderState(lineMS);

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
				World.getInstance().getLandscape().localToWorldCoordinate(coord);
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
		return (getSceneHints().getCullHint() != CullHint.Always);
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
	}
	
	public void complete() {
		newPath = false;
	}

	/**
	 * The user has clicked in the scene. Add a way point.
	 * 
	 * @param loc
	 *            the location of the new way point
	 */
	public void click(ReadOnlyVector3 loc) {
		if (newPath) {
			addWaypoint(loc, -1);
		} else {
			addWaypoint(loc, currentIndex + 1);
			currentIndex++;
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
	 * Is this map element pinned?
	 */
	@Override
	public boolean isPinned() {
		return (pinned);
	}

	/**
	 * Pin down this map element so it cannot be moved.
	 */
	@Override
	public void setPinned(boolean pinned) {
		this.pinned = pinned;
		for (int i = 0; i < pointSet.getNumberOfChildren(); ++i) {
			((Waypoint) pointSet.getChild(i)).setPinned(pinned);
		}
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
			String[] column = { "Name", "X", "Y", "Z", "Annotation" };
			csvWriter = new CsvWriter(filename, column);
			csvWriter.open();
			String[] value = new String[column.length];
			Landscape landscape = World.getInstance().getLandscape();
			Vector3 coord = new Vector3();
			for (int i = 0; i < n; ++i) {
				Waypoint wp = (Waypoint) pointSet.getChild(i);
				coord.set(wp.getTranslation());
				double elev = landscape.getElevationAtHighestLevel(coord.getX(), coord.getY());
				landscape.localToWorldCoordinate(coord);
				coord.setZ(elev);
				value[0] = wp.getName();
				value[1] = Double.toString(coord.getX());
				value[2] = Double.toString(coord.getY());
				value[3] = Double.toString(coord.getZ());
				value[4] = wp.getState().getAnnotation();
				csvWriter.writeLine(value);
			}
			csvWriter.close();
			Console.getInstance().println(n + " records saved to " + filename);
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

	/**
	 * Get the camera viewpoint if it were located at the given way point and
	 * looking at the next way point.
	 * 
	 * @param index
	 * @param height
	 * @param camera
	 * @param sceneBounds
	 * @return
	 */
	public ViewpointStore getWaypointViewpoint(int index, double height, BasicCamera camera, BoundingSphere sceneBounds) {
		// place the camera at the given way point and altitude
		Waypoint wp = (Waypoint) pointSet.getChild(index);
		ReadOnlyVector3 loc = wp.getWorldTranslation();
		camera.setLocation(loc.getX(), loc.getY(), loc.getZ() + height);

		// if the way point is not the last one, point the camera at the next
		// way point location
		if (index < (pointSet.getNumberOfChildren() - 1)) {
			Waypoint nextWp = (Waypoint) pointSet.getChild(index + 1);
			Vector3 direction = new Vector3(nextWp.getWorldTranslation());
			direction.subtractLocal(wp.getWorldTranslation());
			direction.normalizeLocal();
			camera.setDirection(direction);
			Vector3 lookAt = new Vector3(nextWp.getWorldTranslation());
			lookAt.setZ(lookAt.getZ() + height);
			camera.setLookAt(lookAt);
		}
		// otherwise keep the current direction
		else {
			Vector3 lookAt = new Vector3(camera.getDirection());
			lookAt.addLocal(camera.getLocation());
			camera.setLookAt(lookAt);
		}
		// get the rotation angles for the viewpoint store
		Vector3 vec = new Vector3();
		camera.getDirection().negate(vec);
		vec.setZ(0);
		float az = (float) (Math.acos(Vector3.UNIT_Y.dot(vec)) + Math.PI);
		camera.getDirection().negate(vec);
		vec.set(vec.getY(), vec.getZ(), 0);
		float el = (float) Math.acos(Vector3.UNIT_Y.dot(vec));

		// set the frustum
		camera.setFrustum(sceneBounds);

		return (new ViewpointStore(wp.getName(), camera, az, el));
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
		}
	}

	private boolean enableLine(boolean enable) {
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
		updateGeometricState(0, true);
		return (lineIsEnabled);
	}

	private boolean enablePolygon(boolean enable) {
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
		updateGeometricState(0, true);
		return (polyIsEnabled);
	}

	public String getStatistics() {
		pointSet.updatePolygon(poly);
		int n = getNumberOfPoints();
		String str = "";
		str += "Number of Waypoints: " + n + "\n";
		if (n == 0) {
			str += "Lower Bounds: N/A\n";
			str += "Upper Bounds: N/A\n";
			str += "Centroid: N/A\n";
			str += "Total Path Distance: N/A\n";
			str += "Planimetric Area: N/A\n";
			str += "Sampled Mean Elevation: N/A\n";
			str += "Sampled Mean Slope: N/A\n";
			str += "Sampled Volume: N/A\n";
			str += "Sampled Surface Area: N/A\n";
		}
		pointSet.getBounds(lowerBound, upperBound);
		tmpVec.set(lowerBound);
		World.getInstance().getLandscape().localToWorldCoordinate(tmpVec);
		str += "Lower Bounds: " + tmpVec.getXf() + "," + tmpVec.getYf() + "," + tmpVec.getZf() + "\n";
		tmpVec.set(upperBound);
		World.getInstance().getLandscape().localToWorldCoordinate(tmpVec);
		str += "Upper Bounds: " + tmpVec.getXf() + "," + tmpVec.getYf() + "," + tmpVec.getZf() + "\n";
		Vector3 pos = pointSet.getCentroid();
		World.getInstance().getLandscape().localToWorldCoordinate(pos);
		str += "Centroid: " + pos.getXf() + "," + pos.getYf() + "," + pos.getZf() + "\n";
		str += "Total Path Distance: " + (float) pointSet.getDistance() + "\n";

		if (n < 3) {
			str += "Planimetric Area: N/A\n";
			float mElev = 0;
			for (int i = 0; i < n; ++i) {
				mElev += pointSet.getChild(i).getWorldTranslation().getZf();
			}
			mElev /= n;
			str += "Sampled Mean Elevation: " + mElev + "\n";
			str += "Sampled Mean Slope: N/A\n";
			str += "Sampled Volume: N/A\n";
			str += "Sampled Surface Area: N/A\n";
		} else {
			Vector3[] vertex = new Vector3[n + 1];
			for (int i = 0; i < n; ++i) {
				vertex[i] = new Vector3(pointSet.getChild(i).getWorldTranslation());
			}
			vertex[n] = new Vector3(vertex[0]);

			Landscape landscape = World.getInstance().getLandscape();
			str += "Planimetric Area: " + pointSet.getArea() + "\n";
			str += "Sampled Mean Elevation: "
				+ (float) landscape.getSampledMeanElevationOfRegion(vertex, lowerBound, upperBound) + "\n";
			str += "Sampled Mean Slope: "
				+ (float) landscape.getSampledMeanSlopeOfRegion(vertex, lowerBound, upperBound) + "\n";
			poly.getSceneHints().setPickingHint(PickingHint.Pickable, true);
			str += "Sampled Volume: " + landscape.getSampledVolumeOfRegion(vertex, lowerBound, upperBound, poly) + "\n";
			poly.getSceneHints().setPickingHint(PickingHint.Pickable, false);
			str += "Sampled Surface Area: " + landscape.getSampledSurfaceAreaOfRegion(vertex, lowerBound, upperBound)
				+ "\n";
		}
		return (str);
	}

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
			World.getInstance().getLandscape().localToWorldCoordinate(coord);
			if (withText) {
				sBuf.append(wp.getName() + "," + wp.getState().getAnnotation() + "," + coord.getX() + ","
					+ coord.getY() + "," + coord.getZ() + "\n");
			} else {
				sBuf.append(coord.getX() + "," + coord.getY() + "," + coord.getZ() + "\n");
			}
		}
		return (sBuf.toString());
	}

	@Override
	public ReadOnlyVector3 getLocation() {
		if (pointSet.getNumberOfChildren() == 0) {
			location.set(getWorldTranslation());
			World.getInstance().getLandscape().localToWorldCoordinate(location);
			return (location);
		} else {
			Waypoint wp = (Waypoint) getChild(0);
			return (wp.getLocation());
		}
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
		defaultPinned = StringUtil.getBooleanValue(properties, "MapElement.Path.defaultPinned", defaultPinned, false);
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
		properties.setProperty("MapElement.Path.defaultPinned", Boolean.toString(defaultPinned));
	}
}
