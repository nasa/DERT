package gov.nasa.arc.dert.action.file;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.MenuItemAction;

public class ExitAction
	extends MenuItemAction {
	
	public ExitAction() {
		super("Exit");
	}
	
	@Override
	public void run() {
		Dert.quit();
	}

}
