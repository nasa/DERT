/**

DERT is a viewer for digital terrain models created from data collected during NASA missions.

DERT is Released in under the NASA Open Source Agreement (NOSA) found in the “LICENSE” folder where you
downloaded DERT.

DERT includes 3rd Party software. The complete copyright notice listing for DERT is:

Copyright © 2015 United States Government as represented by the Administrator of the National Aeronautics and
Space Administration.  No copyright is claimed in the United States under Title 17, U.S.Code. All Other Rights
Reserved.

Desktop Exploration of Remote Terrain (DERT) could not have been written without the aid of a number of free,
open source libraries. These libraries and their notices are listed below. Find the complete third party license
listings in the separate “DERT Third Party Licenses” pdf document found where you downloaded DERT in the
LICENSE folder.
 
JogAmp Ardor3D Continuation
Copyright © 2008-2012 Ardor Labs, Inc.
 
JogAmp
Copyright 2010 JogAmp Community. All rights reserved.
 
JOGL Portions Sun Microsystems
Copyright © 2003-2009 Sun Microsystems, Inc. All Rights Reserved.
 
JOGL Portions Silicon Graphics
Copyright © 1991-2000 Silicon Graphics, Inc.
 
Light Weight Java Gaming Library Project (LWJGL)
Copyright © 2002-2004 LWJGL Project All rights reserved.
 
Tile Rendering Library - Brain Paul 
Copyright © 1997-2005 Brian Paul. All Rights Reserved.
 
OpenKODE, EGL, OpenGL , OpenGL ES1 & ES2
Copyright © 2007-2010 The Khronos Group Inc.
 
Cg
Copyright © 2002, NVIDIA Corporation
 
Typecast - David Schweinsberg 
Copyright © 1999-2003 The Apache Software Foundation. All rights reserved.
 
PNGJ - Herman J. Gonzalez and Shawn Hartsock
Copyright © 2004 The Apache Software Foundation. All rights reserved.
 
Apache Harmony - Open Source Java SE
Copyright © 2006, 2010 The Apache Software Foundation.
 
Guava
Copyright © 2010 The Guava Authors
 
GlueGen Portions
Copyright © 2010 JogAmp Community. All rights reserved.
 
GlueGen Portions - Sun Microsystems
Copyright © 2003-2005 Sun Microsystems, Inc. All Rights Reserved.
 
SPICE
Copyright © 2003, California Institute of Technology.
U.S. Government sponsorship acknowledged.
 
LibTIFF
Copyright © 1988-1997 Sam Leffler
Copyright © 1991-1997 Silicon Graphics, Inc.
 
PROJ.4
Copyright © 2000, Frank Warmerdam

LibJPEG - Independent JPEG Group
Copyright © 1991-2018, Thomas G. Lane, Guido Vollbeding
 

Disclaimers

No Warranty: THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY KIND,
EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
THAT THE SUBJECT SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY
WARRANTY THAT THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE. THIS AGREEMENT
DOES NOT, IN ANY MANNER, CONSTITUTE AN ENDORSEMENT BY GOVERNMENT AGENCY OR ANY
PRIOR RECIPIENT OF ANY RESULTS, RESULTING DESIGNS, HARDWARE, SOFTWARE PRODUCTS OR
ANY OTHER APPLICATIONS RESULTING FROM USE OF THE SUBJECT SOFTWARE.  FURTHER,
GOVERNMENT AGENCY DISCLAIMS ALL WARRANTIES AND LIABILITIES REGARDING THIRD-PARTY
SOFTWARE, IF PRESENT IN THE ORIGINAL SOFTWARE, AND DISTRIBUTES IT "AS IS."

Waiver and Indemnity:  RECIPIENT AGREES TO WAIVE ANY AND ALL CLAIMS AGAINST THE UNITED
STATES GOVERNMENT, ITS CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR
RECIPIENT.  IF RECIPIENT'S USE OF THE SUBJECT SOFTWARE RESULTS IN ANY LIABILITIES,
DEMANDS, DAMAGES, EXPENSES OR LOSSES ARISING FROM SUCH USE, INCLUDING ANY DAMAGES
FROM PRODUCTS BASED ON, OR RESULTING FROM, RECIPIENT'S USE OF THE SUBJECT SOFTWARE,
RECIPIENT SHALL INDEMNIFY AND HOLD HARMLESS THE UNITED STATES GOVERNMENT, ITS
CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR RECIPIENT, TO THE EXTENT
PERMITTED BY LAW.  RECIPIENT'S SOLE REMEDY FOR ANY SUCH MATTER SHALL BE THE IMMEDIATE,
UNILATERAL TERMINATION OF THIS AGREEMENT.

**/

package gov.nasa.arc.dert.layerfactory;

import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.GBCHelper;
import gov.nasa.arc.dert.ui.LandscapeChooserDialog;
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
	
	// Last path visited
	private String lastPath;

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
		label.setToolTipText("enter GeoJSON file.");
		container.add(label, GBCHelper.getGBC(0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 0));

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
		label.setToolTipText("select the default color for the vectors");
		container
			.add(label, GBCHelper.getGBC(0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 0));

		colorPanel = new ColorSelectionPanel(color);
		container.add(colorPanel,
			GBCHelper.getGBC(1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0));

		// Landscape directory

		label = new JLabel("Landscape: ");
		container
			.add(label, GBCHelper.getGBC(0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 0));
		label.setToolTipText("enter the landscape directory");

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
		nameLabel.setToolTipText("enter the output vector layer name");

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
			lastPath = FileHelper.getLastFilePath();
		}
	}

	/**
	 * Get the destination landscape directory.
	 */
	private void setLandscapeDirectory() {
//		String fPath = FileHelper.getDirectoryPathForOpen("Landscape Selection");
		LandscapeChooserDialog chooser = new LandscapeChooserDialog(lastPath);
		chooser.open();
		String landscapePath = chooser.getLandscape();
		if (landscapePath != null) {
			landscapeText.setText(landscapePath);
			lastPath = chooser.getLastFilePath();
		}
	}

}
