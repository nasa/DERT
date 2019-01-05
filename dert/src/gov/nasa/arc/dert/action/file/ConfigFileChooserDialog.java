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
 
Tile Rendering Library - Brian Paul 
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

import javax.swing.BorderFactory;
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
	public ConfigFileChooserDialog(boolean del) {
		super(Dert.getMainWindow(), "Select Landscape", true, false);
		delete = del;
		width = 600;
		height = 500;
	}

	@Override
	protected void build() {
		super.build();
		contentArea.setLayout(new BorderLayout());

		// Landscape file chooser
		fileChooser = new DertFileChooser(lastPath, true);
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
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
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
				
				// double click
				if (event.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
					File f = (File) event.getNewValue();
					if (f == null) {
						return;
					}
					landscapePath = f.getAbsolutePath();
					lastLandscape = landscapePath;
					// Check if the selection is a landscape directory.
					// If so, the user has double-clicked on the landscape so we will create a new configuration.
					File idFile = new File(f, ".landscape");
					if (idFile.exists()) {
						configFilePath = new String[] { landscapePath };
						setLastPath(fileChooser.getCurrentDirectory().getParentFile().getAbsolutePath());
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
				dertFile = new File(dertFile, "config");
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
			newButton.setToolTipText("create a new configuration using the selected landscape");
			newButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					configFilePath = new String[] { landscapePath };
					setLastPath(fileChooser.getCurrentDirectory().getParentFile().getAbsolutePath());
					close();
				}
			});
			newButton.setEnabled(false);
			buttonsPanel.add(newButton);
			okButton.setText("Open");
			okButton.setToolTipText("open the selected configuration");
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
		setLastPath(fileChooser.getCurrentDirectory().getParentFile().getAbsolutePath());
		return (configFilePath != null);
	}

	public String[] getFilePaths() {
		return (configFilePath);
	}
	
	protected static void setLastPath(String lPath) {
		lastPath = lPath;
	}

}
