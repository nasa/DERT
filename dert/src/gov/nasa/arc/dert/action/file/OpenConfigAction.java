package gov.nasa.arc.dert.action.file;

import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.state.Configuration;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.view.Console;

import java.io.File;

/**
 * Provides a File menu item to open a landscape, opening an existing
 * configuration or creating a new one.
 *
 */
public class OpenConfigAction extends MenuItemAction {

	public OpenConfigAction() {
		super("Open Landscape ...");
	}

	@Override
	protected void run() {
		try {
			// Select a landscape or configuration
			String filePath = getFilePath();
			if (filePath == null) {
				return;
			}
			File file = new File(filePath);
			if (file.isDirectory()) {
				// landscape - create a new configuration
				Configuration newConfig = new Configuration("Untitled");
				newConfig.setLandscapePath(filePath);
//				ConfigurationManager.getInstance().createConfiguration(filePath);
				ConfigurationManager.getInstance().openConfiguration(newConfig);
			} else {
				// existing configuration
				Configuration config = ConfigurationManager.getInstance().loadConfiguration(filePath);
				if (config != null)
					ConfigurationManager.getInstance().openConfiguration(config);
			}
		} catch (Exception e) {
			Console.println("Unable to open worldview.  See log.");
			e.printStackTrace();
		}
	}

	protected String getFilePath() {
		ConfigFileChooserDialog chooser = new ConfigFileChooserDialog(false);
		chooser.open();
		String[] filePaths = chooser.getFilePaths();
		if ((filePaths == null) || (filePaths.length == 0)) {
			return (null);
		}
		return (filePaths[0]);
	}

}
