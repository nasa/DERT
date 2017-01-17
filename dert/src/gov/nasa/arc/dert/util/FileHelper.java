package gov.nasa.arc.dert.util;

import gov.nasa.arc.dert.ui.DertFileChooser;
import gov.nasa.arc.dert.ui.OptionDialog;

import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Provides helper methods for choosing and copying files.
 *
 */
public class FileHelper {

	protected static String lastPath = System.getProperty("user.dir");

	/**
	 * Get a file path for an open operation.
	 * 
	 * @param title
	 * @param extName
	 * @param extValue
	 * @return
	 */
	public static String getFilePathForOpen(String title, String extName, String extValue) {
		JFileChooser chooser = new DertFileChooser(lastPath, false);
		chooser.setDialogTitle(title);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(extName, extValue, extValue.toUpperCase());
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(filter);
		int state = chooser.showOpenDialog(null);
		File file = chooser.getSelectedFile();
		lastPath = chooser.getCurrentDirectory().getAbsolutePath();
		if ((file != null) && (state == JFileChooser.APPROVE_OPTION)) {
			String path = file.getAbsolutePath();
			return (path);
		}
		return (null);
	}

	/**
	 * Get a file path for an open operation with a filter
	 * 
	 * @param title
	 * @param filter
	 * @return
	 */
	public static String getFilePathForOpen(String title, FileNameExtensionFilter filter) {
		JFileChooser chooser = new DertFileChooser(lastPath, false);
		chooser.setDialogTitle(title);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(filter);
		int state = chooser.showOpenDialog(null);
		File file = chooser.getSelectedFile();
		lastPath = chooser.getCurrentDirectory().getAbsolutePath();
		if ((file != null) && (state == JFileChooser.APPROVE_OPTION)) {
			String path = file.getAbsolutePath();
			return (path);
		}
		return (null);
	}

	/**
	 * Get a directory path for an open operation
	 * 
	 * @param title
	 * @return
	 */
	public static String getDirectoryPathForOpen(String title) {
		JFileChooser chooser = new DertFileChooser(lastPath, true);
		chooser.setDialogTitle(title);
		int state = chooser.showOpenDialog(null);
		File file = chooser.getSelectedFile();
		lastPath = chooser.getCurrentDirectory().getAbsolutePath();
		if ((file != null) && (state == JFileChooser.APPROVE_OPTION)) {
			String path = file.getAbsolutePath();
			return (path);
		}
		return (null);
	}

	/**
	 * Get a directory path for a write operation
	 * 
	 * @param title
	 * @return
	 */
	public static String getDirectoryPathForSave(String title) {
		final DertFileChooser chooser = new DertFileChooser(lastPath, true);
		chooser.addNewDirectoryButton(null);
		chooser.setDialogTitle(title);
		int state = chooser.showOpenDialog(null);
		File file = chooser.getSelectedFile();
		lastPath = chooser.getCurrentDirectory().getAbsolutePath();
		if ((file != null) && (state == JFileChooser.APPROVE_OPTION)) {
			String path = file.getAbsolutePath();
			return (path);
		}
		return (null);
	}

	/**
	 * Get a CSV formatted file path for a write operation
	 * 
	 * @return
	 */
	public static String getCSVFile() {
		JFileChooser chooser = new JFileChooser(new File(lastPath));
		chooser.setDialogTitle("Save to CSV File");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("*.csv", "csv", "CSV");
		chooser.setFileFilter(filter);
		int state = chooser.showSaveDialog(null);
		File file = chooser.getSelectedFile();
		if ((file != null) && (state == JFileChooser.APPROVE_OPTION)) {
			lastPath = chooser.getCurrentDirectory().getAbsolutePath();
			if (file.exists()) {
				int answer = OptionDialog.showConfirmDialog((Window)chooser.getTopLevelAncestor(), file.getName()
					+ " exists. Would you like to replace it?", JOptionPane.OK_CANCEL_OPTION);
				if (answer == JOptionPane.CANCEL_OPTION) {
					return (null);
				}
			}
			String path = file.getAbsolutePath();
			if (!(path.endsWith(".csv") || path.endsWith(".CSV"))) {
				path += ".csv";
			}
			return (path);
		}
		return (null);
	}

	/**
	 * Copy a file.
	 * 
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	public static void copyFile(File src, File dest) throws IOException {
		if (src.equals(dest)) {
			return;
		}

		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dest);

		byte[] buf = new byte[8192];
		int len;

		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}

		in.close();
		out.close();
	}
	
	public static String getLastFilePath() {
		return(lastPath);
	}

}
