package gov.nasa.arc.dert.action.file;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.MainWindow;
import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.state.Configuration;
import gov.nasa.arc.dert.state.ConfigurationManager;

public class SaveConfigAction
	extends MenuItemAction {
	
	public SaveConfigAction() {
		super("Save Configuration");
	}
	
	@Override
	public void run() {
		ConfigurationManager.getInstance().saveCurrentConfiguration();
		Configuration currentConfig = ConfigurationManager.getInstance().getCurrentConfiguration();
		MainWindow mw = Dert.getMainWindow();
		mw.setTitle(mw.getTitleString()+": " + currentConfig.toString());
	}

}
