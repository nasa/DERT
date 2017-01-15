package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.ui.OptionDialog;

import java.awt.Window;

/**
 * Provides a dialog for renaming map elements.
 *
 */
public class NameDialog {

	public static String getName(Window parent, String name) {
		String nameStr = OptionDialog.showSingleInputDialog(parent, "Please enter a name.", name);
		while (nameStr != null) {
			nameStr = nameStr.trim();
			if (nameStr.isEmpty()) {
				nameStr = OptionDialog.showSingleInputDialog(parent, "Invalid name. Please enter a name.", name);
			} else {
				return (nameStr);
			}
		}
		return (null);
	}

}
