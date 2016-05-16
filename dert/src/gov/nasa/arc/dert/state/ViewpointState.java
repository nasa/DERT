package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.viewpoint.FlyThroughParameters;
import gov.nasa.arc.dert.viewpoint.ViewpointStore;

import java.util.HashMap;
import java.util.Vector;

/**
 * State object for ViewpointView.
 *
 */
public class ViewpointState extends PanelState {

	private Vector<ViewpointStore> viewpointList;
	private FlyThroughParameters flyParams;

	/**
	 * Constructor
	 */
	public ViewpointState() {
		super(PanelType.Viewpoint, "DERT Viewpoint", new ViewData(-1, -1, 525, 400, false));
		viewpointList = new Vector<ViewpointStore>();
		flyParams = new FlyThroughParameters();
	}
	
	/**
	 * Constructor from hash map
	 */
	public ViewpointState(HashMap<String,Object> map) {
		super(map);
		flyParams = FlyThroughParameters.fromArray((double[])map.get("FlyParams"));
		int n = StateUtil.getInteger(map, "ViewpointCount", 0);
		viewpointList = new Vector<ViewpointStore>();
		for (int i=0; i<n; ++i)
			viewpointList.add(ViewpointStore.fromHashMap((HashMap<String,Object>)map.get("Viewpoint"+i)));
	}

	/**
	 * Save contents to a HashMap
	 */
	@Override
	public HashMap<String,Object> save() {
		HashMap<String,Object> map = super.save();
		map.put("FlyParams", flyParams.toArray());
		map.put("ViewpointCount", new Integer(viewpointList.size()));
		for (int i=0; i<viewpointList.size(); ++i)
			map.put("Viewpoint"+i, viewpointList.get(i).toHashMap());
		return(map);
	}

	/**
	 * Get the viewpoint list
	 * 
	 * @return
	 */
	public Vector<ViewpointStore> getViewpointList() {
		if (viewpointList == null) {
			viewpointList = new Vector<ViewpointStore>();
		}
		return (viewpointList);
	}

	/**
	 * Get the fly-through parameters for the viewpoint list
	 * 
	 * @return
	 */
	public FlyThroughParameters getFlyParams() {
		if (flyParams == null) {
			flyParams = new FlyThroughParameters();
		}
		return (flyParams);
	}
	
	@Override
	public String toString() {
		String str = super.toString();
		for (int i=0; i<viewpointList.size(); ++i)
			str += viewpointList.get(i);
		str += flyParams;
		return(str);
	}

}
