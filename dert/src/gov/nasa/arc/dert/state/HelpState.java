package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.view.HelpView;
import gov.nasa.arc.dert.view.View;

import java.util.Map;

public class HelpState
	extends PanelState {
	
	public HelpState() {
		super("Help", "DERT Help", new ViewData(-1, -1, ViewData.DEFAULT_WINDOW_WIDTH, ViewData.DEFAULT_WINDOW_HEIGHT, false));
	}
	
	public HelpState(Map<String,Object> map) {
		super(map);
	}
	
	@Override
	protected View createView() {
		return(new HelpView(this));
	}

}
