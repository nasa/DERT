package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.view.ConsoleView;
import gov.nasa.arc.dert.view.View;

import java.util.Map;

public class ConsoleState
	extends PanelState {
	
	public ConsoleState() {
		super("Console", "DERT Console", new ViewData(0, -604, 960, 250, false));
	}
	
	public ConsoleState(Map<String,Object> map) {
		super(map);
	}
	
	@Override
	protected View createView() {
		View view = new ConsoleView(this);
		return(view);
	}

}
