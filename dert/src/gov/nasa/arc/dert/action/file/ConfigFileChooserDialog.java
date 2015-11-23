package gov.nasa.arc.dert.action.file;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.ui.AbstractDialog;
import gov.nasa.arc.dert.ui.DertFileChooser;
import gov.nasa.arc.dert.ui.GroupPanel;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Provides a dialog for choosing a DERT configuration or a landscape.
 *
 */
public class ConfigFileChooserDialog extends AbstractDialog {

	// The last directory and landscape chosen
	protected static String lastPath = System.getProperty("user.dir");
	protected static String lastLandscape;

	// Configuration list panel
	private JList configList;

	// File chooser panel
	private DertFileChooser fileChooser;

	// List of configuration files
	private String[] fileList;

	// Version of DERT for selecting configuration files
	private String version;

	// Paths
	private String[] configFilePath;
	private String landscapePath;

	// Flag indicating the selected file is to be deleted
	private boolean delete;

	// Button to create a new configuration
	private JButton newButton;

	/**
	 * Constructor
	 * 
	 * @param vrsn
	 * @param del
	 */
	public ConfigFileChooserDialog(String vrsn, boolean del) {
		super(Dert.getMainWindow(), "Select Landscape", true, false);
		version = vrsn;
		delete = del;
		width = 600;
		height = 500;
	}

	@Override
	protected void build() {
		super.build();
		contentArea.setLayout(new BorderLayout());

		// Landscape file chooser
		fileChooser = new DertFileChooser(lastPath, true, false);
		fileChooser.setControlButtonsAreShown(false);
		GroupPanel gPanel = new GroupPanel("Landscape");
		gPanel.setLayout(new GridLayout(1, 1));
		gPanel.add(fileChooser);
		contentArea.add(gPanel, BorderLayout.CENTER);

		// Configuration list
		configList = new JList(new String[] {});
		configList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				okButton.setEnabled(true);
			}
		});
		// allow multiple file deletes
		if (delete) {
			configList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else {
			configList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		configList.setVisibleRowCount(4);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().setView(configList);
		gPanel = new GroupPanel("Configuration");
		gPanel.setLayout(new GridLayout(1, 1));
		gPanel.add(scrollPane);
		contentArea.add(gPanel, BorderLayout.SOUTH);

		fileChooser.addPropertyChangeListener(new PropertyChangeListener() {
			/**
			 * A selection was made in the file chooser.
			 */
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				landscapePath = null;
				String[] list = new String[] {};
				configList.setListData(list);
				contentArea.revalidate();
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
				if (newButton != null) {
					newButton.setEnabled(true);
				}
				landscapePath = f.getAbsolutePath();
				lastLandscape = landscapePath;

				// Show list of configurations from the version dert
				// subdirectory in the selected landscape
				File dertFile = new File(f, "dert");
				if (!dertFile.exists()) {
					configList.setListData(list);
					return;
				}
				dertFile = new File(dertFile, "config" + version);
				if (dertFile.exists()) {
					list = dertFile.list();
					if (list == null) {
						list = new String[0];
					}
					fileList = new String[list.length];
					for (int i = 0; i < list.length; ++i) {
						fileList[i] = new File(dertFile, list[i]).getAbsolutePath();
						list[i] = StringUtil.getLabelFromFilePath(list[i]);
					}
					configList.setListData(list);
				}
				okButton.setEnabled(false);
				contentArea.revalidate();
			}
		});

		// If we are not deleting files add a button to create a new
		// configuration
		if (!delete) {
			newButton = new JButton("New Configuration");
			newButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					configFilePath = new String[] { landscapePath };
					lastPath = fileChooser.getCurrentDirectory().getAbsolutePath();
					dispose();
				}
			});
			newButton.setEnabled(false);
			buttonsPanel.add(newButton);
			okButton.setText("Open");
		}
		okButton.setEnabled(false);

		// can't seem to make this work on Mac
		// if (delete && (lastLandscape != null))
		// fileChooser.setSelectedFile(new File(lastLandscape));
	}

	/**
	 * User made a selection.
	 */
	@Override
	public boolean okPressed() {
		int[] index = configList.getSelectedIndices();
		if ((index == null) || (index.length == 0)) {
			if (delete) {
				configFilePath = null;
			} else {
				configFilePath = new String[] { landscapePath };
			}
		} else {
			configFilePath = new String[index.length];
			for (int i = 0; i < index.length; ++i) {
				configFilePath[i] = fileList[index[i]];
			}
		}
		setLastPath(fileChooser.getCurrentDirectory().getAbsolutePath());
		return (configFilePath != null);
	}

	public String[] getFilePaths() {
		return (configFilePath);
	}

	private static void setLastPath(String path) {
		lastPath = path;
	}

}
