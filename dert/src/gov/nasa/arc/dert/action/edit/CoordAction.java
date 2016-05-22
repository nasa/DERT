package gov.nasa.arc.dert.action.edit;

import gov.nasa.arc.dert.action.ButtonAction;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.World;

import java.util.ArrayList;

public class CoordAction
	extends ButtonAction {
	
	public static final ArrayList<CoordListener> listenerList = new ArrayList<CoordListener>();
	
	public CoordAction() {
		super("enable/disable longitude and latitude coordinates", null, "graticule.png", false);
	}
	
	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		if (selected) 
			setIcon(Icons.getImageIcon("graticule_checked.png"));
		else
			setIcon(Icons.getImageIcon("graticule.png"));
	}
	
	@Override
	public void run() {
		selected = !selected;
		if (selected) {
			setIcon(Icons.getImageIcon("graticule_checked.png"));
			World.getInstance().setUseLonLat(true);
		}
		else {
			setIcon(Icons.getImageIcon("graticule.png"));
			World.getInstance().setUseLonLat(false);
		}
		for (int i=0; i<listenerList.size(); ++i)
			listenerList.get(i).coordDisplayChanged();
	}

}
