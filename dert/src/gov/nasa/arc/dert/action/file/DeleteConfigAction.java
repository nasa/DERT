package gov.nasa.arc.dert.action.file;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.ui.OptionDialog;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.Console;

/**
 * Provides a File menu item for deleting a configuration.
 *
 */
public class DeleteConfigAction extends MenuItemAction {

	/**
	 * Constructor
	 * 
	 */
	public DeleteConfigAction() {
		super("Delete Configuration ...");
	}

	@Override
	protected void run() {
		try {
			// Get a list of configurations to delete.
			String[] filePath = getFilePaths();
			if ((filePath == null) || (filePath.length == 0)) {
				return;
			}
			String label = StringUtil.getLabelFromFilePath(filePath[0]);
			if (filePath.length > 1) {
				label += " ...";
			}

			// Prompt user to confirm delete.
			boolean yes = OptionDialog.showDeleteConfirmDialog(Dert.getMainWindow(), "Delete " + label + "?");
			if (yes) {
				for (int i = 0; i < filePath.length; ++i) {
					ConfigurationManager.getInstance().removeConfiguration(filePath[i]);
				}
			}
		} catch (Exception e) {
			Console.println("Error deleting configuration.");
			e.printStackTrace();
		}
	}

	protected String[] getFilePaths() {
		ConfigFileChooserDialog chooser = new ConfigFileChooserDialog(true);
		chooser.open();
		return (chooser.getFilePaths());
	}

}
