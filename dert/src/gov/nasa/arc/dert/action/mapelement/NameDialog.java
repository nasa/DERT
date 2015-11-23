package gov.nasa.arc.dert.action.mapelement;

import javax.swing.JOptionPane;

/**
 * Provides a dialog for renaming map elements.
 *
 */
public class NameDialog {

	public static String getName(String name) {
		String nameStr = JOptionPane.showInputDialog(null, "Please enter a name.", name);
		while (nameStr != null) {
			nameStr = nameStr.trim();
			if (nameStr.isEmpty()) {
				nameStr = JOptionPane.showInputDialog(null, "Invalid name.", nameStr);
			} else {
				return (nameStr);
			}
		}
		return (null);
	}

}
