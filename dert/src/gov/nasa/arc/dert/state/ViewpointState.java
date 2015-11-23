package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.viewpoint.FlyThroughParameters;
import gov.nasa.arc.dert.viewpoint.ViewpointStore;

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

}
