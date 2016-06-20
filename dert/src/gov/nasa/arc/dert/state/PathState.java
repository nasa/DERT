package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scene.tool.Path.BodyType;
import gov.nasa.arc.dert.scene.tool.Path.LabelType;
import gov.nasa.arc.dert.scene.tool.Waypoint;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.TextView;
import gov.nasa.arc.dert.view.View;

import java.util.ArrayList;
import java.util.HashMap;

import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * A state object for the Path tool.
 *
 */
public class PathState extends ToolState {

	// The points for the path
	public ArrayList<WaypointState> pointList;

	// The type of path body (point, line, or polygon)
	public BodyType bodyType;

	// The label type
	public LabelType labelType;

	// Way point visibility
	public boolean waypointsVisible;

	// Line width
	public double lineWidth;

	private transient Thread statThread;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public PathState(ReadOnlyVector3 position) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration()
			.incrementMapElementCount(MapElementState.Type.Path), MapElementState.Type.Path, "Path", Path.defaultSize,
			Path.defaultColor, Path.defaultLabelVisible, Path.defaultPinned);
		bodyType = Path.defaultBodyType;
		labelType = Path.defaultLabelType;
		lineWidth = Path.defaultLineWidth;
		waypointsVisible = Path.defaultWaypointsVisible;
		viewData = new ViewData(-1, -1, 600, 250, true);
		pointList = new ArrayList<WaypointState>();
		WaypointState wp = new WaypointState(0, position, name + ".", size, color, labelVisible, pinned);
		pointList.add(wp);
	}
	
	/**
	 * Constructor for hash map.
	 */
	public PathState(HashMap<String,Object> map) {
		super(map);
		bodyType = Path.stringToBodyType(StateUtil.getString(map, "BodyType", null));
		labelType = Path.stringToLabelType(StateUtil.getString(map, "LabelType", null));
		lineWidth = StateUtil.getDouble(map, "LineWidth", Path.defaultLineWidth);
		waypointsVisible = StateUtil.getBoolean(map, "WaypointsVisible", Path.defaultWaypointsVisible);
		int n = StateUtil.getInteger(map, "WaypointCount", 0);
		pointList = new ArrayList<WaypointState>();
		for (int i=0; i<n; ++i)
			pointList.add(new WaypointState((HashMap<String,Object>)map.get("Waypoint"+i)));
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof PathState)) 
			return(false);
		PathState that = (PathState)state;
		if (!super.isEqualTo(that)) 
			return(false);
		if (this.bodyType != that.bodyType)
			return(false);
		if (this.labelType != that.labelType)
			return(false);
		if (this.waypointsVisible != that.waypointsVisible) 
			return(false);
		if (this.lineWidth != that.lineWidth)
			return(false);
		if (this.pointList.size() != that.pointList.size()) 
			return(false);
		for (int i=0; i<this.pointList.size(); ++i) {
			if (!this.pointList.get(i).isEqualTo(that.pointList.get(i)))
				return(false);
		}
		return(true);
	}

	@Override
	public HashMap<String,Object> save() {
		HashMap<String,Object> map = super.save();
		if (mapElement != null) {
			Path path = (Path) mapElement;
			getWaypointList();
			bodyType = path.getBodyType();
			labelType = path.getLabelType();
			lineWidth = path.getLineWidth();
			waypointsVisible = path.areWaypointsVisible();
		}
		map.put("BodyType", bodyType.toString());
		map.put("LabelType", labelType.toString());
		map.put("LineWidth", new Double(lineWidth));
		map.put("WaypointsVisible", new Boolean(waypointsVisible));
		map.put("WaypointCount", new Integer(pointList.size()));
		for (int i=0; i<pointList.size(); ++i)
			map.put("Waypoint"+i, pointList.get(i).save());
		return(map);
	}

	/**
	 * Get the list of way points
	 * 
	 * @return
	 */
	public ArrayList<WaypointState> getWaypointList() {
		if (mapElement != null) {
			Path path = (Path) mapElement;
			int n = path.getNumberOfPoints();
			pointList = new ArrayList<WaypointState>();
			for (int i = 0; i < n; ++i) {
				Waypoint wp = path.getWaypoint(i);
				WaypointState wps = (WaypointState) wp.getState();
				wps.save();
				pointList.add(wps);
			}
		}
		return (pointList);
	}

	@Override
	public void setAnnotation(String note) {
		if (note != null) {
			annotation = note;
		}
		if (mapElement != null) {
			Path path = (Path) mapElement;
			// update the way points
			int n = path.getNumberOfPoints();
			for (int i = 0; i < n; ++i) {
				Waypoint wp = path.getWaypoint(i);
				WaypointState wps = (WaypointState) wp.getState();
				wps.setAnnotation(null);
			}
		}
	}
	
	@Override
	public void createView() {
		View view = new TextView(this, true) {
			@Override
			public void doRefresh() {
				updateStatistics();
			}
		};
		setView(view);
		viewData.createWindow(Dert.getMainWindow(), name + " Info", X_OFFSET, Y_OFFSET);
	}

	@Override
	public void setView(View view) {
		viewData.setView(view);
		updateStatistics();
	}

	/**
	 * Show statistics for the Path in a separate window.
	 */
	public void updateStatistics() {
		if (statThread != null) {
			return;
		}

		((TextView)viewData.view).setMessage("Calculating ...");

		statThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Thread.yield();
				String str = ((Path)mapElement).getStatistics();
				((TextView)viewData.getView()).setText(str);
				((TextView)viewData.getView()).setMessage("");
				statThread = null;
			}
		});
		statThread.start();
	}
	
	@Override
	public String toString() {
		String str = "["+bodyType+","+labelType+","+waypointsVisible+","+lineWidth+"]"+super.toString();
		return(str);
	}
}
