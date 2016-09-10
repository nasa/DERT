package gov.nasa.arc.dert.ui;

import gov.nasa.arc.dert.Dert;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Provides a dialog for choosing a DERT configuration or a landscape.
 *
 */
public class LandscapeChooserDialog extends AbstractDialog {

	// The last directory chosen
	protected static String lastPath = System.getProperty("user.dir");

	// File chooser panel
	private DertFileChooser fileChooser;

	// Paths
	private String landscapePath;

	/**
	 * Constructor
	 * 
	 * @param vrsn
	 * @param del
	 */
	public LandscapeChooserDialog() {
		super(Dert.getMainWindow(), "Select Landscape", true, false);
		width = 600;
		height = 400;
	}

	@Override
	protected void build() {
		super.build();
		contentArea.setLayout(new BorderLayout());

		// Landscape file chooser
		fileChooser = new DertFileChooser(lastPath, true);
		fileChooser.setControlButtonsAreShown(false);
		contentArea.add(fileChooser, BorderLayout.CENTER);
		addNewLandscapeButton();
		okButton.setEnabled(false);

		fileChooser.addPropertyChangeListener(new PropertyChangeListener() {
			/**
			 * A selection was made in the file chooser.
			 */
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				landscapePath = null;
				
				// double click
				if (event.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
					File f = (File) event.getNewValue();
					if (f == null) {
						return;
					}
					landscapePath = f.getAbsolutePath();
					// Check if the selection is a landscape directory.
					// If so, the user has double-clicked on the landscape so we will return that landscape.
					File idFile = new File(f, ".landscape");
					if (idFile.exists()) {
						lastPath = f.getParent();
						close();
					}
					return;
				}
				
				// single click
				if (!event.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
					return;
				}
				File f = (File) event.getNewValue();
				if (f == null) {
					return;
				}

				// check if the selection is a landscape directory
				File idFile = new File(f, ".landscape");
				if (!idFile.exists()) {
					return;
				}
				landscapePath = f.getAbsolutePath();
				okButton.setEnabled(true);

			}
		});
	}

	private void addNewLandscapeButton() {
		JButton nl = new JButton("New Landscape");
		nl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String str = JOptionPane.showInputDialog(null,
					"Please enter the landscape name (no spaces).");
				if (str == null) {
					return;
				}
				File file = new File(fileChooser.getCurrentDirectory(), str);
				file.mkdirs();
				fileChooser.rescanCurrentDirectory();
				fileChooser.setSelectedFiles(new File[] { file });

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
		buttonsPanel.add(nl, 0);
	}

	/**
	 * User made a selection.
	 */
	@Override
	public boolean okPressed() {
		setLastPath(fileChooser.getCurrentDirectory().getAbsolutePath());
		return (landscapePath != null);
	}

	public String getLandscape() {
		return (landscapePath);
	}

	private static void setLastPath(String path) {
		lastPath = path;
	}

}
