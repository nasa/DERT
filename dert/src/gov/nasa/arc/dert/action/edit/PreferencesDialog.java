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

package gov.nasa.arc.dert.action.edit;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.scene.featureset.FeatureSet;
import gov.nasa.arc.dert.scene.landmark.Figure;
import gov.nasa.arc.dert.scene.landmark.ImageBoard;
import gov.nasa.arc.dert.scene.landmark.Placemark;
import gov.nasa.arc.dert.scene.tool.CartesianGrid;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scene.tool.Path.BodyType;
import gov.nasa.arc.dert.scene.tool.Plane;
import gov.nasa.arc.dert.scene.tool.Profile;
import gov.nasa.arc.dert.scene.tool.RadialGrid;
import gov.nasa.arc.dert.scene.tool.ScaleBar;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCameraInfoManager;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.ui.AbstractDialog;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.ui.FieldPanel;
import gov.nasa.arc.dert.ui.IconComboBox;
import gov.nasa.arc.dert.ui.VerticalPanel;
import gov.nasa.arc.dert.util.ColorMap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Provides a dialog for selecting stereo mode and setting stereo parameters.
 *
 */
public class PreferencesDialog extends AbstractDialog {

	/**
	 * Constructor
	 */
	public PreferencesDialog() {
		super(Dert.getMainWindow(), "Map Element Preferences", false, true, false);
		width = 400;
		height = 500;
	}

	@Override
	protected void build() {
		super.build();
		contentArea.setLayout(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Landmarks", getLandmarksPrefPanel());
		tabbedPane.addTab("Tools", getToolsPrefPanel());
		tabbedPane.addTab("FeatureSets", getFeatureSetsPrefPanel());
		contentArea.add(tabbedPane, BorderLayout.CENTER);
	}

	@Override
	public boolean okPressed() {
		return (true);
	}
	
	private JComponent getLandmarksPrefPanel() {
		ArrayList<Component> vCompList = new ArrayList<Component>();
		
		JPanel gPanel = new JPanel(new BorderLayout());
		gPanel.add(new JLabel("--- Placemark ---", SwingConstants.CENTER), BorderLayout.NORTH);
		
		ArrayList<Component> compList = new ArrayList<Component>();
		
		compList.add(new JLabel("Label", SwingConstants.RIGHT));
		JCheckBox checkBox = new JCheckBox("visible");
		checkBox.setSelected(Placemark.defaultLabelVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Placemark.defaultLabelVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		compList.add(checkBox);

		compList.add(new JLabel("Icon", SwingConstants.RIGHT));
		JComboBox comboBox = new IconComboBox(Placemark.ICON_LABEL, Placemark.icons);
		comboBox.setSelectedIndex(Placemark.defaultTextureIndex);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Placemark.defaultTextureIndex = ((JComboBox) event.getSource()).getSelectedIndex();
			}
		});
		compList.add(comboBox);

		compList.add(new JLabel("Color", SwingConstants.RIGHT));
		ColorSelectionPanel colorList = new ColorSelectionPanel(Placemark.defaultColor) {
			@Override
			public void doColor(Color color) {
				Placemark.defaultColor = color;
			}
		};
		compList.add(colorList);

		compList.add(new JLabel("Size", SwingConstants.RIGHT));
		DoubleTextField sizeText = new DoubleTextField(8, Placemark.defaultSize, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				Placemark.defaultSize = value;
			}
		};
		compList.add(sizeText);
		
		gPanel.add(new FieldPanel(compList), BorderLayout.CENTER);		
		vCompList.add(gPanel);
		
		gPanel = new JPanel(new BorderLayout());
		gPanel.add(new JLabel("--- 3D Figure ---", SwingConstants.CENTER), BorderLayout.NORTH);
		
		compList = new ArrayList<Component>();

		compList.add(new JLabel("Label", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(Figure.defaultLabelVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Figure.defaultLabelVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		compList.add(checkBox);

		compList.add(new JLabel("Normal", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(Figure.defaultSurfaceNormalVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Figure.defaultSurfaceNormalVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		compList.add(checkBox);

		compList.add(new JLabel("Shape", SwingConstants.RIGHT));
		comboBox = new JComboBox(ShapeType.values());
		comboBox.setSelectedItem(Figure.defaultShapeType);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Figure.defaultShapeType = (ShapeType) ((JComboBox) event.getSource()).getSelectedItem();
			}
		});
		compList.add(comboBox);

		compList.add(new JLabel("Color", SwingConstants.RIGHT));
		colorList = new ColorSelectionPanel(Figure.defaultColor) {
			@Override
			public void doColor(Color color) {
				Figure.defaultColor = color;
			}
		};
		compList.add(colorList);

		compList.add(new JLabel("Size", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, Figure.defaultSize, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				Figure.defaultSize = value;
			}
		};
		compList.add(sizeText);

		compList.add(new JLabel("Scale", SwingConstants.RIGHT));
		checkBox = new JCheckBox("automatic");
		checkBox.setSelected(Figure.defaultAutoScale);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Figure.defaultAutoScale = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		compList.add(checkBox);
		
		gPanel.add(new FieldPanel(compList), BorderLayout.CENTER);
		
		vCompList.add(gPanel);
		
		gPanel = new JPanel(new BorderLayout());
		gPanel.add(new JLabel("--- Billboard ---", SwingConstants.CENTER), BorderLayout.NORTH);
		
		compList = new ArrayList<Component>();

		compList.add(new JLabel("Label", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(ImageBoard.defaultLabelVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ImageBoard.defaultLabelVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		compList.add(checkBox);

		compList.add(new JLabel("Size", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, ImageBoard.defaultSize, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				ImageBoard.defaultSize = value;
			}
		};
		compList.add(sizeText);
		
		gPanel.add(new FieldPanel(compList));
		
		vCompList.add(gPanel);
		
		VerticalPanel prefPanel = new VerticalPanel(vCompList, 30);
		
		JScrollPane scrollPane = new JScrollPane(prefPanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		return(scrollPane);
	}
	
	private JComponent getToolsPrefPanel() {
		ArrayList<Component> vCompList = new ArrayList<Component>();
		
		// Path Preferences
		JPanel gPanel = new JPanel(new BorderLayout());
		gPanel.add(new JLabel("--- Path ---", SwingConstants.CENTER), BorderLayout.NORTH);
		ArrayList<Component> compList = new ArrayList<Component>();

		compList.add(new JLabel("Label", SwingConstants.RIGHT));
		JCheckBox checkBox = new JCheckBox("visible");
		checkBox.setSelected(Path.defaultLabelVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Path.defaultLabelVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		compList.add(checkBox);

		compList.add(new JLabel("Waypoints", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(Path.defaultWaypointsVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Path.defaultWaypointsVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		compList.add(checkBox);

		compList.add(new JLabel("Type", SwingConstants.RIGHT));
		JComboBox comboBox = new JComboBox(BodyType.values());
		comboBox.setSelectedItem(Path.defaultBodyType);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Path.defaultBodyType = (BodyType) ((JComboBox) event.getSource()).getSelectedItem();
			}
		});
		compList.add(comboBox);

		compList.add(new JLabel("Color", SwingConstants.RIGHT));
		ColorSelectionPanel colorList = new ColorSelectionPanel(Path.defaultColor) {
			@Override
			public void doColor(Color color) {
				Path.defaultColor = color;
			}
		};
		compList.add(colorList);

		compList.add(new JLabel("Linewidth", SwingConstants.RIGHT));
		DoubleTextField ptlwText = new DoubleTextField(8, Path.defaultLineWidth, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				Path.defaultLineWidth = (float)value;
			}
		};
		compList.add(ptlwText);

		compList.add(new JLabel("Point Size", SwingConstants.RIGHT));
		DoubleTextField ptPtSzText = new DoubleTextField(8, Path.defaultSize, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				Path.defaultSize = (float)value;
			}
		};
		compList.add(ptPtSzText);
		
		gPanel.add(new FieldPanel(compList), BorderLayout.CENTER);
		vCompList.add(gPanel);

		// Plane Preferences
		gPanel = new JPanel(new BorderLayout());
		gPanel.add(new JLabel("--- Plane ---", SwingConstants.CENTER), BorderLayout.NORTH);
		compList = new ArrayList<Component>();

		compList.add(new JLabel("Label", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(Plane.defaultLabelVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Plane.defaultLabelVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		compList.add(checkBox);

		compList.add(new JLabel("Color Map", SwingConstants.RIGHT));
		comboBox = new JComboBox(ColorMap.getColorMapNames());
		comboBox.setSelectedItem(Plane.defaultColorMap);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Plane.defaultColorMap = (String) ((JComboBox) event.getSource()).getSelectedItem();
			}
		});
		compList.add(comboBox);

		compList.add(new JLabel("Color", SwingConstants.RIGHT));
		colorList = new ColorSelectionPanel(Plane.defaultColor) {
			@Override
			public void doColor(Color color) {
				Plane.defaultColor = color;
			}
		};
		compList.add(colorList);

		compList.add(new JLabel("Strike Format", SwingConstants.RIGHT));
		checkBox = new JCheckBox("compass bearing");
		checkBox.setSelected(Plane.strikeAsCompassBearing);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Plane.strikeAsCompassBearing = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		compList.add(checkBox);
		
		gPanel.add(new FieldPanel(compList), BorderLayout.CENTER);
		vCompList.add(gPanel);

		// Cartesian Grid Preferences
		gPanel = new JPanel(new BorderLayout());
		gPanel.add(new JLabel("--- Cartesian Grid ---", SwingConstants.CENTER), BorderLayout.NORTH);
		compList = new ArrayList<Component>();

		compList.add(new JLabel("Label", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(CartesianGrid.defaultLabelVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				CartesianGrid.defaultLabelVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		compList.add(checkBox);

		compList.add(new JLabel("Color", SwingConstants.RIGHT));
		colorList = new ColorSelectionPanel(CartesianGrid.defaultColor) {
			@Override
			public void doColor(Color color) {
				CartesianGrid.defaultColor = color;
			}
		};
		compList.add(colorList);

		compList.add(new JLabel("Columns", SwingConstants.RIGHT));
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(CartesianGrid.defaultColumns, 1, 1000000, 1));
		compList.add(spinner);
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				CartesianGrid.defaultColumns = (Integer) ((JSpinner) event.getSource()).getValue();
			}
		});

		compList.add(new JLabel("Rows", SwingConstants.RIGHT));
		spinner = new JSpinner(new SpinnerNumberModel(CartesianGrid.defaultRows, 1, 1000000, 1));
		compList.add(spinner);
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				CartesianGrid.defaultRows = (Integer) ((JSpinner) event.getSource()).getValue();
			}
		});

		compList.add(new JLabel("Linewidth", SwingConstants.RIGHT));
		DoubleTextField clwText = new DoubleTextField(8, CartesianGrid.defaultLineWidth, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				CartesianGrid.defaultLineWidth = (float)value;
			}
		};
		compList.add(clwText);
		
		gPanel.add(new FieldPanel(compList), BorderLayout.CENTER);
		vCompList.add(gPanel);

		// Radial Grid Preferences
		gPanel = new JPanel(new BorderLayout());
		gPanel.add(new JLabel("--- Radial Grid ---", SwingConstants.CENTER), BorderLayout.NORTH);
		compList = new ArrayList<Component>();

		compList.add(new JLabel("Label", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(RadialGrid.defaultLabelVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				RadialGrid.defaultLabelVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		compList.add(checkBox);

		compList.add(new JLabel("Color", SwingConstants.RIGHT));
		colorList = new ColorSelectionPanel(RadialGrid.defaultColor) {
			@Override
			public void doColor(Color color) {
				RadialGrid.defaultColor = color;
			}
		};
		compList.add(colorList);

		compList.add(new JLabel("Rings", SwingConstants.RIGHT));
		spinner = new JSpinner(new SpinnerNumberModel(RadialGrid.defaultRings, 1, 1000000, 1));
		compList.add(spinner);
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				RadialGrid.defaultRings = (Integer) ((JSpinner) event.getSource()).getValue();
			}
		});

		compList.add(new JLabel("Linewidth", SwingConstants.RIGHT));
		DoubleTextField rlwText = new DoubleTextField(8, RadialGrid.defaultLineWidth, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				RadialGrid.defaultLineWidth = (float)value;
			}
		};
		compList.add(rlwText);

		compList.add(new JLabel("Rose", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(RadialGrid.defaultCompassRose);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				RadialGrid.defaultCompassRose = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		compList.add(checkBox);
		
		gPanel.add(new FieldPanel(compList), BorderLayout.CENTER);
		vCompList.add(gPanel);

		// FieldCamera Preferences
		gPanel = new JPanel(new BorderLayout());
		gPanel.add(new JLabel("--- Camera ---", SwingConstants.CENTER), BorderLayout.NORTH);
		compList = new ArrayList<Component>();

		compList.add(new JLabel("Label", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(FieldCamera.defaultLabelVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				FieldCamera.defaultLabelVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		compList.add(checkBox);

		compList.add(new JLabel("Color", SwingConstants.RIGHT));
		colorList = new ColorSelectionPanel(FieldCamera.defaultColor) {
			@Override
			public void doColor(Color color) {
				FieldCamera.defaultColor = color;
			}
		};
		compList.add(colorList);

		compList.add(new JLabel("FOV", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(FieldCamera.defaultFovVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				FieldCamera.defaultFovVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		compList.add(checkBox);

		compList.add(new JLabel("LookAt Line", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(FieldCamera.defaultLineVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				FieldCamera.defaultLineVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		compList.add(checkBox);

		compList.add(new JLabel("Definition", SwingConstants.RIGHT));
		comboBox = new JComboBox(FieldCameraInfoManager.getInstance().getFieldCameraNames());
		comboBox.setSelectedItem(FieldCamera.defaultDefinition);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				FieldCamera.defaultDefinition = (String) ((JComboBox) event.getSource()).getSelectedItem();
			}
		});
		compList.add(comboBox);
		
		gPanel.add(new FieldPanel(compList), BorderLayout.CENTER);
		vCompList.add(gPanel);

		// Profile Preferences
		gPanel = new JPanel(new BorderLayout());
		gPanel.add(new JLabel("--- Profile ---", SwingConstants.CENTER), BorderLayout.NORTH);
		compList = new ArrayList<Component>();

		compList.add(new JLabel("Label", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(Profile.defaultLabelVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Profile.defaultLabelVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		compList.add(checkBox);

		compList.add(new JLabel("Color", SwingConstants.RIGHT));
		colorList = new ColorSelectionPanel(Profile.defaultColor) {
			@Override
			public void doColor(Color color) {
				Profile.defaultColor = color;
			}
		};
		compList.add(colorList);

		compList.add(new JLabel("Linewidth", SwingConstants.RIGHT));
		DoubleTextField plwText = new DoubleTextField(8, Profile.defaultLineWidth, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				Profile.defaultLineWidth = (float)value;
			}
		};
		compList.add(plwText);
		
		gPanel.add(new FieldPanel(compList), BorderLayout.CENTER);
		vCompList.add(gPanel);

		// Scale Preferences
		gPanel = new JPanel(new BorderLayout());
		gPanel.add(new JLabel("--- Scale ---", SwingConstants.CENTER), BorderLayout.NORTH);
		compList = new ArrayList<Component>();

		compList.add(new JLabel("Label", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(ScaleBar.defaultLabelVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ScaleBar.defaultLabelVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		compList.add(checkBox);

		compList.add(new JLabel("Autolabel", SwingConstants.RIGHT));
		checkBox = new JCheckBox("enabled");
		checkBox.setSelected(ScaleBar.defaultAutoLabel);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ScaleBar.defaultAutoLabel = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		compList.add(checkBox);

		compList.add(new JLabel("Color", SwingConstants.RIGHT));
		colorList = new ColorSelectionPanel(CartesianGrid.defaultColor) {
			@Override
			public void doColor(Color color) {
				CartesianGrid.defaultColor = color;
			}
		};
		compList.add(colorList);

		compList.add(new JLabel("Cell count", SwingConstants.RIGHT));
		spinner = new JSpinner(new SpinnerNumberModel(ScaleBar.defaultCellCount, 1, 1000000, 1));
		compList.add(spinner);
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				ScaleBar.defaultCellCount = (Integer) ((JSpinner) event.getSource()).getValue();
			}
		});
		
		gPanel.add(new FieldPanel(compList), BorderLayout.CENTER);
		vCompList.add(gPanel);
		
		VerticalPanel prefPanel = new VerticalPanel(vCompList, 30);
		JScrollPane scrollPane = new JScrollPane(prefPanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		return(scrollPane);
	}
	
	private JComponent getFeatureSetsPrefPanel() {
		ArrayList<Component> vCompList = new ArrayList<Component>();
		
		// Path Preferences
		JPanel gPanel = new JPanel(new BorderLayout());
		gPanel.add(new JLabel("--- FeatureSet ---", SwingConstants.CENTER), BorderLayout.NORTH);
		ArrayList<Component> compList = new ArrayList<Component>();

		// FeatureSet Preferences
		compList.add(new JLabel("Color", SwingConstants.RIGHT));
		ColorSelectionPanel colorList = new ColorSelectionPanel(FeatureSet.defaultColor) {
			@Override
			public void doColor(Color color) {
				FeatureSet.defaultColor = color;
			}
		};
		compList.add(colorList);

		compList.add(new JLabel("Linewidth", SwingConstants.RIGHT));
		DoubleTextField lwText = new DoubleTextField(8, FeatureSet.defaultLineWidth, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				FeatureSet.defaultLineWidth = (float)value;
			}
		};
		compList.add(lwText);

		compList.add(new JLabel("Point Size", SwingConstants.RIGHT));
		DoubleTextField ptSzText = new DoubleTextField(8, FeatureSet.defaultSize, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				FeatureSet.defaultSize = (float)value;
			}
		};
		compList.add(ptSzText);
		
		gPanel.add(new FieldPanel(compList), BorderLayout.CENTER);
		vCompList.add(gPanel);
		
		VerticalPanel prefPanel = new VerticalPanel(vCompList, 30);
		JScrollPane scrollPane = new JScrollPane(prefPanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		return(scrollPane);
	}

}
