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

package gov.nasa.arc.dert.ui;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.view.Console;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFileChooser;

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
	public LandscapeChooserDialog(String path) {
		super(Dert.getMainWindow(), "Select Landscape", true, false);
		width = 600;
		height = 400;
		if (path != null) {
			File dotFile = new File(path, ".landscape");
			if (dotFile.exists())
				path = new File(path).getParent();
			lastPath = path;
		}
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
				String str = OptionDialog.showSingleInputDialog((Window)buttonsPanel.getTopLevelAncestor(), "Please enter the landscape name (no spaces).", "");
				if (str == null) {
					return;
				}
				str = str.trim();
				File file = null;
				if (!str.isEmpty()) {
					try {
						file = new File(fileChooser.getCurrentDirectory(), str);
						file.mkdirs();
						fileChooser.rescanCurrentDirectory();
// Can't seem to get the following line to work
//						fileChooser.setSelectedFile(file);
					}
					catch (Exception e) {
						OptionDialog.showErrorMessageDialog((Window)fileChooser.getTopLevelAncestor(), "Error creating landscape "+str+".");
						e.printStackTrace();
					}
				}
				else {
					OptionDialog.showErrorMessageDialog((Window)fileChooser.getTopLevelAncestor(), "Invalid landscape name.");
				}
				if (file == null)
					return;

				// add landscape identifier
				Properties landscapeProperties = new Properties();
				File propFile = new File(file, ".landscape");
				landscapeProperties.setProperty("LastWrite", System.getProperty("user.name"));
				try {
					landscapeProperties.store(new FileOutputStream(propFile), null);
				} catch (Exception e) {
					Console.println("Error creating landscape "+file);
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
	
	@Override
	public boolean cancelPressed() {
		landscapePath = null;
		return(super.cancelPressed());
	}

	public String getLandscape() {
		return (landscapePath);
	}

	private static void setLastPath(String path) {
		lastPath = path;
	}
	
	public String getLastFilePath() {
		return(lastPath);
	}

}
