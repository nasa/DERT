package gov.nasa.arc.dert.action.file;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.MainWindow;
import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.state.Configuration;
import gov.nasa.arc.dert.state.ConfigurationManager;

public class SaveConfigAsAction
	extends MenuItemAction {
	
	public SaveConfigAsAction() {
		super("Save Configuration As ...");
	}
	
	@Override
	public void run() {
		ConfigurationManager.getInstance().saveCurrentConfigurationAs(true);
		Configuration currentConfig = ConfigurationManager.getInstance().getCurrentConfiguration();
		MainWindow mw = Dert.getMainWindow();
		mw.setTitle(mw.getTitleString()+" - " + Landscape.getInstance().getGlobeName() + ":" + World.getInstance().getName() + ":" + currentConfig.toString());
	}

}
