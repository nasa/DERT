package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scene.tool.Path.BodyType;
import gov.nasa.arc.dert.scene.tool.Path.LabelType;
import gov.nasa.arc.dert.scene.tool.Waypoint;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.View;
import gov.nasa.arc.dert.view.mapelement.EditDialog;
import gov.nasa.arc.dert.view.mapelement.NotesDialog;
import gov.nasa.arc.dert.view.mapelement.PathView;
import gov.nasa.arc.dert.viewpoint.FlyThroughParameters;

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
	
	public FlyThroughParameters flyParams;
	
	// Params for PathView
	public double refElev = Double.NaN;
	public int volMethod = 1;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public PathState(ReadOnlyVector3 position) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration()
			.incrementMapElementCount(MapElementState.Type.Path), MapElementState.Type.Path, "Path", Path.defaultSize,
			Path.defaultColor, Path.defaultLabelVisible);
		bodyType = Path.defaultBodyType;
		labelType = Path.defaultLabelType;
		lineWidth = Path.defaultLineWidth;
		waypointsVisible = Path.defaultWaypointsVisible;
		viewData = new ViewData(-1, -1, -1, -1, true);
		pointList = new ArrayList<WaypointState>();
		WaypointState wp = new WaypointState(0, position, name + ".", Path.defaultSize, color, labelVisible, locked);
		pointList.add(wp);
		flyParams = new FlyThroughParameters();
	}
	
	/**
	 * Constructor for hash map.
	 */
	public PathState(HashMap<String,Object> map) {
		super(map);
		bodyType = Path.stringToBodyType(StateUtil.getString(map, "BodyType", null));
		labelType = Path.stringToLabelType(StateUtil.getString(map, "LabelType", null));
		lineWidth = StateUtil.getDouble(map, "LineWidth", Path.defaultLineWidth);
		flyParams = FlyThroughParameters.fromArray((double[])map.get("FlyParams"));
		flyParams.imageSequencePath = StateUtil.getString(map, "ImageSequencePath", null);
		waypointsVisible = StateUtil.getBoolean(map, "WaypointsVisible", Path.defaultWaypointsVisible);
		int n = StateUtil.getInteger(map, "WaypointCount", 0);
		pointList = new ArrayList<WaypointState>();
		for (int i=0; i<n; ++i)
			pointList.add(new WaypointState((HashMap<String,Object>)map.get("Waypoint"+i)));
		refElev = StateUtil.getDouble(map, "ReferenceElevation", Double.NaN);
		volMethod = StateUtil.getInteger(map, "VolumeMethod", 1);
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
		map.put("FlyParams", flyParams.toArray());
		map.put("ImageSequencePath", flyParams.imageSequencePath);
		map.put("WaypointsVisible", new Boolean(waypointsVisible));
		map.put("WaypointCount", new Integer(pointList.size()));
		for (int i=0; i<pointList.size(); ++i)
			map.put("Waypoint"+i, pointList.get(i).save());
		if (viewData.view != null) {
			PathView pv = (PathView)(viewData.view);
			refElev = pv.getVolElevation();
			volMethod = pv.getVolumeMethod();
			map.put("ReferenceElevation", new Double(refElev));
			map.put("VolumeMethod", new Integer(volMethod));
		}
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

	/**
	 * Open the editor
	 */
	@Override
	public EditDialog openEditor() {
		if (mapElement == null)
			return(null);
		if (editDialog == null)
			editDialog = new EditDialog(Dert.getMainWindow(), "Edit "+mapElement.getType(), mapElement);
		else {
			editDialog.setMapElement(mapElement);
			editDialog.update();
		}
		editDialog.open();
		return(editDialog);
	}

	/**
	 * Open the annotation
	 */
	@Override
	public NotesDialog openAnnotation() {
		if (mapElement == null)
			return(null);
		if (annotationDialog == null) {
			annotationDialog = new NotesDialog(Dert.getMainWindow(), name, 400, 200, mapElement) {			
				@Override
				public void setMapElement(MapElement me) {
					super.setMapElement(me);
					if (me instanceof Path)
						location.setEnabled(false);
					else
						location.setEnabled(true);
				}
				@Override
				protected void updateText() {
					if (mapElement instanceof Path)
						super.updateText();
					else {
						Path path = ((Waypoint)mapElement).getPath();
						String str = path.getName()+":\n"+path.getState().getAnnotation()+"\n";
						str += mapElement.getName()+":\n"+mapElement.getState().getAnnotation()+"\n";
						textArea.setText(str);
						textArea.setCaretPosition(0);
					}
				}
			};
		}
		else {
			annotationDialog.setMapElement(mapElement);
			annotationDialog.update();
		}
		annotationDialog.open();
		return(annotationDialog);
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

	/**
	 * Set the MapElement
	 * 
	 * @param mapElement
	 */
	@Override
	public void setMapElement(MapElement mapElement) {
		// first time
		if ((this.mapElement == null) && (mapElement instanceof Path))
			this.mapElement = mapElement;
		if (annotationDialog != null)
			annotationDialog.setMapElement(mapElement);
		if (editDialog != null)
			editDialog.setMapElement(mapElement);
	}
	
	@Override
	public void createView() {
		PathView view = new PathView(this, refElev, volMethod);
		setView(view);
		viewData.createWindow(Dert.getMainWindow(), name + " View", X_OFFSET, Y_OFFSET);
	}

	@Override
	public void setView(View view) {
		viewData.setView(view);
		((PathView)view).doRefresh();
	}
	
	/**
	 * Notify user that the currently displayed statistics is old. We don't
	 * automatically update the window for performance reasons.
	 */
	public void pathDirty() {
		PathView pv = (PathView)viewData.view;
		if (pv != null)
			pv.pathDirty();
	}
	
	@Override
	public String toString() {
		String str = "["+bodyType+","+labelType+","+waypointsVisible+","+lineWidth+"]"+super.toString();
		return(str);
	}
}
