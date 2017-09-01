package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.View;
import gov.nasa.arc.dert.view.viewpoint.AnimationView;
import gov.nasa.arc.dert.viewpoint.FlyThroughParameters;

import java.util.ArrayList;
import java.util.Map;

/**
 * State object for ViewpointView.
 *
 */
public class AnimationState extends PanelState {

	private FlyThroughParameters flyParams;
	private int subjectId;

	/**
	 * Constructor
	 */
	public AnimationState() {
		super("Animation", "DERT Animation", new ViewData(-1, -1, 525, 300, false));
		flyParams = new FlyThroughParameters();
		subjectId = -1;
	}
	
	/**
	 * Constructor from hash map
	 */
	public AnimationState(Map<String,Object> map) {
		super(map);
		flyParams = FlyThroughParameters.fromArray((double[])map.get("FlyParams"));
		flyParams.imageSequencePath = StateUtil.getString(map, "ImageSequencePath", null);
		subjectId = StateUtil.getInteger(map, "SubjectId", -1);
	}

	/**
	 * Save contents to a HashMap
	 */
	@Override
	public Map<String,Object> save() {
		Map<String,Object> map = super.save();
		AnimationView av = (AnimationView)viewData.view;
		if (av != null) {
			flyParams = av.getViewpointFlyParams();
			Object obj = av.getSubject();
			if (obj instanceof Path)
				subjectId = ((Path)obj).getState().id;
			else
				subjectId = -1;
		}
		map.put("FlyParams", flyParams.toArray());
		map.put("ImageSequencePath", flyParams.imageSequencePath);
		map.put("SubjectId", new Integer(subjectId));
		return(map);
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
	
	public Object getSubject() {
		if (subjectId < 0)
			return(null);
		else {
			ArrayList<ToolState> tools = ConfigurationManager.getInstance().getCurrentConfiguration().getToolStates();
			for (int i=0; i<tools.size(); ++i) {
				ToolState ts = tools.get(i);
				if (ts instanceof PathState) {
					if (ts.id == subjectId)
						return(ts.mapElement);
				}
			}
		}
		return(null);
	}
	
	@Override
	public String toString() {
		String str = super.toString();
		str += flyParams;
		return(str);
	}
	
	@Override
	protected View createView() {
		return(new AnimationView(this));
	}

}
