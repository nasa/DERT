package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.view.View;
import gov.nasa.arc.dert.view.lighting.LightingView;

import java.util.Map;

public class LightingState
	extends PanelState {
	
	public LightingState() {
		super("Lighting", "DERT Lighting and Shadows", new ViewData(-1, -1, false));
	}
	
	public LightingState(Map<String,Object> map) {
		super(map);
	}
	
	@Override
	protected View createView() {
		return(new LightingView(this));
	}

}
