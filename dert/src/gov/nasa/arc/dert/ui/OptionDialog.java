package gov.nasa.arc.dert.ui;

import gov.nasa.arc.dert.icon.Icons;

import java.awt.Window;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class OptionDialog {
	
	public static final ImageIcon deleteIcon = Icons.getImageIcon("delete.png");
	
	public static String showSingleInputDialog(Window parent, String message, String value) {
		String str = (String)JOptionPane.showInputDialog(parent, message, "DERT Input", JOptionPane.PLAIN_MESSAGE, Icons.DERT_ICON_24, null, value);
		return(str);
	}
	
	public static void showErrorMessageDialog(Window parent, String message) {
		JOptionPane.showMessageDialog(parent, message, "DERT Error", JOptionPane.PLAIN_MESSAGE, Icons.DERT_ICON_24);
	}
	
	public static int showConfirmDialog(Window parent, String message, int option) {
		int answer = JOptionPane.showConfirmDialog(parent, message, "DERT Confirmation",
				option, JOptionPane.PLAIN_MESSAGE, Icons.DERT_ICON_24);
		return(answer);
	}
	
	public static boolean showDeleteConfirmDialog(Window parent, String message) {
		int answer = JOptionPane.showConfirmDialog(parent, message, "DERT Confirm Delete",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, deleteIcon);
		return(answer == JOptionPane.YES_OPTION);
	}

}
