package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.view.View;
import gov.nasa.arc.dert.view.lighting.LightPositionView;

import java.util.Map;

public class LightPositionState
	extends PanelState {
	
	public LightPositionState() {
		super("LightPosition", "DERT Light Position", new ViewData(-1, -1, false));
	}
	
	public LightPositionState(Map<String,Object> map) {
		super(map);
	}
	
	@Override
	protected View createView() {
		return(new LightPositionView(this));
	}

}
