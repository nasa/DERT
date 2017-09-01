package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.View;
import gov.nasa.arc.dert.view.viewpoint.ViewpointView;
import gov.nasa.arc.dert.viewpoint.ViewpointStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * State object for ViewpointView.
 *
 */
public class ViewpointState extends PanelState {

	public Vector<ViewpointStore> viewpointList;

	/**
	 * Constructor
	 */
	public ViewpointState() {
		super("Viewpoint", "DERT Viewpoint", new ViewData(525, 300, false));
		viewpointList = new Vector<ViewpointStore>();
	}
	
	/**
	 * Constructor from hash map
	 */
	public ViewpointState(Map<String,Object> map) {
		super(map);
		int n = StateUtil.getInteger(map, "ViewpointCount", 0);
		viewpointList = new Vector<ViewpointStore>();
		for (int i=0; i<n; ++i)
			viewpointList.add(ViewpointStore.fromHashMap((HashMap<String,Object>)map.get("Viewpoint"+i)));
	}

	/**
	 * Save contents to a HashMap
	 */
	@Override
	public Map<String,Object> save() {
		Map<String,Object> map = super.save();
		viewpointList = Dert.getWorldView().getScenePanel().getViewpointController().getViewpointList();
		map.put("ViewpointCount", new Integer(viewpointList.size()));
		for (int i=0; i<viewpointList.size(); ++i)
			map.put("Viewpoint"+i, viewpointList.get(i).toHashMap());
		return(map);
	}
	
	@Override
	public String toString() {
		String str = super.toString();
		for (int i=0; i<viewpointList.size(); ++i)
			str += viewpointList.get(i);
		return(str);
	}
	
	@Override
	protected View createView() {
		return(new ViewpointView((ViewpointState) this));
	}

}
