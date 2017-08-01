package gov.nasa.arc.dert.action.file;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.MenuItemAction;

/**
 * Provides a pop up window that displays the current DERT version as well as an
 * about string and a list of supporting software.
 *
 */
public class AboutAction extends MenuItemAction {

	/**
	 * Constructor
	 * 
	 * @param version
	 * @param name
	 */
	public AboutAction() {
		super("About DERT");
	}

	@Override
	public void run() {	
		String heading = "Desktop Exploration of Remote Terrain (DERT), version "+Dert.version+"\nIntelligent Systems Division, NASA Ames Research Center\n\n";
		AboutBox aboutBox = new AboutBox(Dert.getMainWindow(), "DERT", heading);
		aboutBox.open();
	}

}
