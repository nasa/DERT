package gov.nasa.arc.dert.landscape.factory;

import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.GBCHelper;
import gov.nasa.arc.dert.util.FileHelper;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Provides a panel to create a multi-resolution tiled pyramid from a vector
 * file in GeoJSON format. The tiles are 4 times the size of the destination
 * landscape elevation tiles.
 *
 */
public class VectorLayerPanel extends JPanel {

	// UI elements
	private JTextField fileText, landscapeText, messageText, nameText;
	private ColorSelectionPanel colorPanel;
	private JLabel nameLabel;
	private JTextField elevAttrNameText;

	// Factory to build the pyramid
	private VectorPyramidLayerFactory factory;

	// Path to the landscape
	private String landscapePath;

	// Path to vector file
	private String filePath;

	// Name of this layer
	private String layerName;

	// Default color for the lines
	private Color color;

	// Elevation attribute name from gdal_contour
	private String elevAttrName;

	/**
	 * Constructor
	 * 
	 * @param messageText
	 *            text field for displaying messages
	 */
	public VectorLayerPanel(JTextField mText, String lPath, String fPath, String lName, Color col, String elevAttrName) {
		messageText = mText;
		landscapePath = lPath;
		filePath = fPath;
		layerName = lName;
		color = col;
		this.elevAttrName = elevAttrName;

		JPanel container = this;

		GridBagLayout gridLayout = new GridBagLayout();
		container.setLayout(gridLayout);

		// Vector file

		JLabel label = new JLabel("Input File:");
		label.setToolTipText("Enter GeoJSON file.");
		container
			.add(label, GBCHelper.getGBC(0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 0));

		fileText = new JTextField();
		if (filePath != null) {
			fileText.setText(filePath);
		}
		container.add(fileText,
			GBCHelper.getGBC(1, 0, 3, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0));

		JButton button = new JButton("Browse");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setInputFile();
			}
		});
		container.add(button,
			GBCHelper.getGBC(4, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 0, 0));

		// Color

		label = new JLabel("Default Color:");
		label.setToolTipText("Select the default color for the vectors.");
		container
			.add(label, GBCHelper.getGBC(0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 0));

		colorPanel = new ColorSelectionPanel(color);
		container.add(colorPanel,
			GBCHelper.getGBC(1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0));

		// Landscape directory

		label = new JLabel("Landscape: ");
		container
			.add(label, GBCHelper.getGBC(0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 0));
		label.setToolTipText("Enter the landscape directory.");

		landscapeText = new JTextField();
		if (landscapePath != null) {
			landscapeText.setText(landscapePath);
		}
		container.add(landscapeText,
			GBCHelper.getGBC(1, 3, 2, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0));

		button = new JButton("Browse");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setLandscapeDirectory();
			}
		});
		container.add(button,
			GBCHelper.getGBC(4, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 0, 0));

		// Layer name

		nameLabel = new JLabel("Layer Name: ");
		container.add(nameLabel,
			GBCHelper.getGBC(0, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 0));
		nameLabel.setToolTipText("Enter the output vector layer name.");

		nameText = new JTextField();
		nameText.setText(layerName);
		container.add(nameText,
			GBCHelper.getGBC(1, 4, 2, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0));

		// Is this a contour map?

		label = new JLabel("Elev Attr Name: ");
		container
			.add(label, GBCHelper.getGBC(0, 5, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 0));
		elevAttrNameText = new JTextField();
		if (elevAttrName != null) {
			elevAttrNameText.setText(elevAttrName);
		}
		elevAttrNameText.setToolTipText("gdal_contour elevation attribute name (specified with the -a argument)");
		container.add(elevAttrNameText,
			GBCHelper.getGBC(1, 5, 2, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0));
	}

	/**
	 * Apply button was pressed
	 */
	public boolean applyPressed() {

		// Get the landscape
		landscapePath = landscapeText.getText().trim();
		if (landscapePath.length() == 0) {
			messageText.setText("Please select a landscape.");
			return (false);
		}

		// Get the input file path.
		filePath = fileText.getText().trim();
		if (filePath.length() == 0) {
			messageText.setText("Please select an input file.");
			return (false);
		}
		if (!filePath.toLowerCase().endsWith(".json")) {
			messageText.setText("Only GeoJSON format is supported.");
			return (false);
		}
		layerName = nameText.getText();
		if (layerName.isEmpty()) {
			messageText.setText("Invalid layer name.");
			return (false);
		}

		// Get the color and contour map flag
		color = colorPanel.getColor();
		elevAttrName = elevAttrNameText.getText();
		if (elevAttrName.isEmpty()) {
			elevAttrName = null;
		}

		messageText.setText("Creating pyramid . . .");
		return (true);
	}

	/**
	 * Build the pyramid.
	 * 
	 * @return true if should close the window
	 */
	public boolean run() {
		try {
			File elevDir = new File(landscapePath, "elevation");
			if (!elevDir.exists()) {
				messageText.setText("Cannot find Landscape elevation layer.");
				return (false);
			}
			File propFile = new File(elevDir, "layer.properties");
			if (!propFile.exists()) {
				messageText.setText("Cannot find layer.properties file for Landscape elevation layer.");
				return (false);
			}
			factory = new VectorPyramidLayerFactory(filePath);
			factory.buildPyramid(landscapePath, layerName, color, elevAttrName, messageText);
			return (true);
		} catch (Exception e) {
			messageText.setText("Unable to complete pyramid.");
			e.printStackTrace();
			factory = null;
			return (false);
		}

	}

	/**
	 * Cancel button was pressed
	 * 
	 * @return true if should close the window
	 */
	public boolean cancelPressed() {
		if (factory != null) {
			factory.cancel();
			factory = null;
			return (false);
		} else {
			return (true);
		}
	}

	/**
	 * Get the input raster file.
	 */
	private void setInputFile() {
		String fPath = FileHelper.getFilePathForOpen("Input File Selection", "JSON file", "json");
		if (fPath != null) {
			fileText.setText(fPath);
			nameText.setText(StringUtil.getLabelFromFilePath(fPath));
		}
	}

	/**
	 * Get the destination landscape directory.
	 */
	private void setLandscapeDirectory() {
		String fPath = FileHelper.getDirectoryPathForOpen("Landscape Selection");
		if (fPath != null) {
			landscapeText.setText(fPath);
		}
	}

}
