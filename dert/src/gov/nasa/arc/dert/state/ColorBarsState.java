package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.view.ColorBarView;
import gov.nasa.arc.dert.view.View;

import java.util.Map;

public class ColorBarsState
	extends PanelState {
	
	public ColorBarsState() {
		super("ColorBars", "DERT Color Bars", new ViewData(700, 200, false));
	}
	
	public ColorBarsState(Map<String,Object> map) {
		super(map);
	}
	
	@Override
	protected View createView() {
		return(new ColorBarView(this));
	}

}
