package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.view.View;
import gov.nasa.arc.dert.view.surfaceandlayers.SurfaceAndLayersView;

import java.util.Map;

public class SurfaceAndLayersState
	extends PanelState {
	
	public SurfaceAndLayersState() {
		super("SurfaceAndLayers", "DERT Surface and Layers", new ViewData(375, 600, false));
	}
	
	public SurfaceAndLayersState(Map<String,Object> map) {
		super(map);
	}
	
	@Override
	protected View createView() {
		return(new SurfaceAndLayersView(this));
	}

}
