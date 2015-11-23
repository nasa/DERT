package gov.nasa.arc.dert.action.file;

import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.Console;

import javax.swing.JOptionPane;

/**
 * Provides a File menu item for deleting a configuration.
 *
 */
public class DeleteConfigAction extends MenuItemAction {

	// Version of DERT for configuration selection.
	protected String version;

	/**
	 * Constructor
	 * 
	 * @param version
	 */
	public DeleteConfigAction(String version) {
		super("Delete Configuration ...");
		this.version = version;
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
			int answer = JOptionPane.showConfirmDialog(null, "Delete " + label + "?", "Confirm Delete",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, Icons.getImageIcon("delete.png"));
			if (answer == JOptionPane.OK_OPTION) {
				for (int i = 0; i < filePath.length; ++i) {
					ConfigurationManager.getInstance().removeConfiguration(filePath[i]);
				}
			}
		} catch (Exception e) {
			Console.getInstance().println("Unable to open new view.  See log.");
			e.printStackTrace();
		}
	}

	protected String[] getFilePaths() {
		ConfigFileChooserDialog chooser = new ConfigFileChooserDialog(version, true);
		chooser.open();
		return (chooser.getFilePaths());
	}

}
