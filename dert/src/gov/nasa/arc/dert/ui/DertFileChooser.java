package gov.nasa.arc.dert.ui;

import gov.nasa.arc.dert.icon.Icons;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * A JFileChooser for Landscapes.
 *
 */
public class DertFileChooser extends JFileChooser {

	/**
	 * Filter all file except directories.
	 * 
	 * @author lkeelyme
	 *
	 */
	public class ConfigFileFilter extends FileFilter {

		public ConfigFileFilter() {
		}

		@Override
		public String getDescription() {
			return ("");
		}

		@Override
		public boolean accept(File f) {
			if (!f.isDirectory()) {
				return (false);
			}
			return (true);
		}
	}

	private boolean directoryOnly;

	/**
	 * Constructor
	 * 
	 * @param lastPath
	 * @param directoryOnly
	 * @param newLandscape
	 */
	public DertFileChooser(String lastPath, boolean dirOnly, boolean newLandscape) {
		super(new File(lastPath));
		this.directoryOnly = dirOnly;
		setMultiSelectionEnabled(false);

		if (directoryOnly) {
			setFileFilter(new ConfigFileFilter());
			removeFileType(getComponents());
		}
		// button to create a new landscape
		if (newLandscape) {
			addNewLandscapeButton(getComponents());
		}
	}

	@Override
	public Icon getIcon(File f) {
		// use landscape icon
		File dertFile = new File(f, ".landscape");
		if (dertFile.exists()) {
			return (Icons.getImageIcon("landscape-icon.png"));
		}
		return (super.getIcon(f));
	}

	private void removeFileType(Component[] child) {
		for (int i = 0; i < child.length; ++i) {
			if (child[i] instanceof JLabel) {
				JLabel label = (JLabel) child[i];
				if (label.getText().equals("File Format:")) {
					child[i].getParent().getParent().remove(child[i].getParent());
				}
			} else if (child[i] instanceof Container) {
				removeFileType(((Container) child[i]).getComponents());
			}
		}
	}

	private void addNewLandscapeButton(Component[] child) {
		for (int i = 0; i < child.length; ++i) {
			if (child[i] instanceof JButton) {
				JButton button = (JButton) child[i];
				String s = button.getText();
				if ((s != null) && s.equals("Cancel")) {
					JButton nl = new JButton("New Landscape");
					nl.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent event) {
							String str = JOptionPane.showInputDialog(null,
								"Please enter the landscape name (no spaces).");
							if (str == null) {
								return;
							}
							File file = new File(getCurrentDirectory(), str);
							file.mkdirs();
							rescanCurrentDirectory();
							setSelectedFiles(new File[] { file });

							// add landscape identifier
							Properties landscapeProperties = new Properties();
							File propFile = new File(file, ".landscape");
							landscapeProperties.setProperty("LastWrite", System.getProperty("user.name"));
							try {
								landscapeProperties.store(new FileOutputStream(propFile), null);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
					button.getParent().add(nl, 0);
				}
			} else if (child[i] instanceof Container) {
				addNewLandscapeButton(((Container) child[i]).getComponents());
			}
		}
	}

}
