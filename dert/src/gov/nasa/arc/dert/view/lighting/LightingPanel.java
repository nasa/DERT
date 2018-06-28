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

package gov.nasa.arc.dert.view.lighting;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.edit.CoordAction;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.ui.CoordTextField;
import gov.nasa.arc.dert.ui.DateTextField;
import gov.nasa.arc.dert.ui.DoubleSpinner;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.ui.FieldPanel;
import gov.nasa.arc.dert.ui.GroupPanel;
import gov.nasa.arc.dert.ui.VerticalPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Provides content for LightingView.
 *
 */
public class LightingPanel extends JPanel {

	private JRadioButton solButton, lampButton;
	private JCheckBox headlightButton, shadowButton;
	private DoubleSpinner mainDiffuseSpinner, mainAmbientSpinner, globalAmbientSpinner, headDiffuseSpinner;
	private JButton advancedShadow, defaultSphereButton;
	private CoordTextField shadowCenterText;
	private DoubleTextField shadowRadiusText;
	private DateTextField lmstEpoch;
	private JLabel modeLabel;

	private Vector3 coord = new Vector3();

	public LightingPanel() {
		ArrayList<Component> vCompList = new ArrayList<Component>();
		
		setLayout(new BorderLayout());

		// Main Light section
		GroupPanel mainPanel = new GroupPanel("Main Light");
		mainPanel.setLayout(new BorderLayout());
		ArrayList<Component> compList = new ArrayList<Component>();
		
		// Light mode
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		modeLabel = new JLabel("Mode", SwingConstants.RIGHT);
		if (World.getInstance().getLighting().isLampMode()) {
			modeLabel.setIcon(Icons.getImageIcon("luxo.png"));
		} else {
			modeLabel.setIcon(Icons.getImageIcon("sun.png"));
		}
		compList.add(modeLabel);
		ButtonGroup group = new ButtonGroup();
		solButton = new JRadioButton("Solar");
		solButton.setToolTipText("light is positioned by time");
		solButton.setSelected(!World.getInstance().getLighting().isLampMode());
		group.add(solButton);
		panel.add(solButton);
		lampButton = new JRadioButton("Artificial");
		lampButton.setToolTipText("light is positioned by azimuth and elevation");
		lampButton.setSelected(World.getInstance().getLighting().isLampMode());
		group.add(lampButton);
		panel.add(lampButton);
		compList.add(panel);
		
		// Light attributes
		compList.add(new JLabel("Diffuse Intensity", SwingConstants.RIGHT));
		mainDiffuseSpinner = new DoubleSpinner(World.getInstance().getLighting().getDiffuseIntensity(), 0, 1, 0.05,
			false) {
			@Override
			public void stateChanged(ChangeEvent event) {
				double value = (Double) getValue();
				World.getInstance().getLighting().setDiffuseIntensity(value);
			}
		};
		compList.add(mainDiffuseSpinner);
		compList.add(new JLabel("Ambient Intensity", SwingConstants.RIGHT));
		mainAmbientSpinner = new DoubleSpinner(World.getInstance().getLighting().getAmbientIntensity(), 0, 1, 0.05,
			false) {
			@Override
			public void stateChanged(ChangeEvent event) {
				double value = (Double) getValue();
				World.getInstance().getLighting().setAmbientIntensity(value);
			}
		};
		compList.add(mainAmbientSpinner);
		compList.add(new JLabel("Global Ambient Intensity", SwingConstants.RIGHT));
		globalAmbientSpinner = new DoubleSpinner(World.getInstance().getLighting().getGlobalIntensity(), 0, 1, 0.05,
			false) {
			@Override
			public void stateChanged(ChangeEvent event) {
				double value = (Double) getValue();
				World.getInstance().getLighting().setGlobalIntensity(value);
			}
		};
		compList.add(globalAmbientSpinner);
		
		// Time epoch for determing Local Mars Solar Time
		compList.add(new JLabel("LMST Epoch", SwingConstants.RIGHT));
		lmstEpoch = new DateTextField(10, World.getInstance().getLighting().getEpoch(), Lighting.DATE_FORMAT) {
			@Override
			public void handleChange(Date value) {
				World.getInstance().getLighting().setEpoch(value);
			}
		};
		compList.add(lmstEpoch);
		
		FieldPanel fPanel = new FieldPanel(compList);
		mainPanel.add(fPanel, BorderLayout.CENTER);
		vCompList.add(mainPanel);

		// Headlight section
		GroupPanel headPanel = new GroupPanel("Headlight");
		headPanel.setLayout(new BorderLayout());
		compList = new ArrayList<Component>();
		
		compList.add(new JLabel("Headlight", SwingConstants.RIGHT));
		headlightButton = new JCheckBox("enable");
		headlightButton.setSelected(World.getInstance().getLighting().isHeadlightEnabled());
		compList.add(headlightButton);
		
		compList.add(new JLabel("Diffuse Intensity", SwingConstants.RIGHT));
		headDiffuseSpinner = new DoubleSpinner(World.getInstance().getLighting().getHeadlightIntensity(), 0, 1, 0.05,
			false) {
			@Override
			public void stateChanged(ChangeEvent event) {
				double value = (Double) getValue();
				World.getInstance().getLighting().setHeadlightIntensity(value);
			}
		};
		compList.add(headDiffuseSpinner);
		
		fPanel = new FieldPanel(compList);
		headPanel.add(fPanel, BorderLayout.CENTER);
		vCompList.add(headPanel);

		// Shadows section
		GroupPanel shadowPanel = new GroupPanel("Shadows");
		shadowPanel.setLayout(new BorderLayout());
		compList = new ArrayList<Component>();
		
		compList.add(new JLabel("Shadow Map", SwingConstants.RIGHT));
		shadowButton = new JCheckBox("enable");
		shadowButton.setToolTipText("display shadows");
		shadowButton.setSelected(World.getInstance().getLighting().isShadowEnabled());
		shadowButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				World.getInstance().getLighting().enableShadow(shadowButton.isSelected());
			}
		});
		compList.add(shadowButton);

		// Shadow sphere
		compList.add(new JLabel("Shadow Center", SwingConstants.RIGHT));
		shadowCenterText = new CoordTextField(10, "coordinates of shadow sphere center", Landscape.format, false) {
			@Override
			public void doChange(ReadOnlyVector3 result) {
				coord.set(result);
				Lighting lighting = World.getInstance().getLighting();
				lighting.getShadowMap().setCenter(coord);
				Landscape.getInstance().localToWorldCoordinate(coord);
				Landscape.getInstance().worldToSphericalCoordinate(coord);
				lighting.setRefLoc(coord);
			}
		};
		CoordAction.listenerList.add(shadowCenterText);
		coord.set(World.getInstance().getLighting().getRefLoc());
		Landscape.getInstance().sphericalToLocalCoordinate(coord);
		shadowCenterText.setLocalValue(coord);
		compList.add(shadowCenterText);
		compList.add(new JLabel("Shadow Radius", SwingConstants.RIGHT));
		shadowRadiusText = new DoubleTextField(10, World.getInstance().getLighting().getShadowMap().getRadius(), true, Landscape.format) {
			@Override
			public void handleChange(double radius) {
				Lighting lighting = World.getInstance().getLighting();
				lighting.getShadowMap().setRadius(radius);
			}
		};
		compList.add(shadowRadiusText);

		defaultSphereButton = new JButton("Default");
		defaultSphereButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Lighting lighting = World.getInstance().getLighting();
				BoundingVolume bv = World.getInstance().getContents().getWorldBound();
				shadowRadiusText.setValue((float) bv.getRadius());
				coord.set(Landscape.getInstance().getCenter());
				shadowCenterText.setLocalValue(coord);
				World.getInstance().getLighting().setRefLoc(Landscape.getInstance().getCenterLonLat());
				lighting.getShadowMap().setCenter(coord);
				lighting.getShadowMap().setRadius((float) bv.getRadius());
			}
		});
		compList.add(defaultSphereButton);
		advancedShadow = new JButton("Advanced");
		advancedShadow.setToolTipText("edit advanced shadow settings");
		advancedShadow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ShadowSettingsDialog dialog = new ShadowSettingsDialog();
				dialog.open();
			}
		});
		compList.add(advancedShadow);
		
		fPanel = new FieldPanel(compList);
		shadowPanel.add(fPanel, BorderLayout.CENTER);
		vCompList.add(shadowPanel);
		
		VerticalPanel vPanel = new VerticalPanel(vCompList, 0);
		add(vPanel, BorderLayout.CENTER);

		solButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (solButton.isSelected()) {
					lightSelected();
				}
			}
		});
		lampButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (lampButton.isSelected()) {
					lightSelected();
				}
			}
		});
		headlightButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				lightSelected();
			}
		});
	}

	private void lightSelected() {
		Lighting lighting = World.getInstance().getLighting();
		if (lampButton.isSelected()) {
			modeLabel.setIcon(Icons.getImageIcon("luxo.png"));
		} else {
			modeLabel.setIcon(Icons.getImageIcon("sun.png"));
		}
		lighting.enableHeadlight(headlightButton.isSelected());
		lighting.setLampMode(lampButton.isSelected());
		Dert.getMainWindow().getToolPanel().updateLightIcon();
		World.getInstance().getMarble().setSolarDirection(lighting.getLightDirection());
		World.getInstance().markDirty(DirtyType.RenderState);
	}
	
	public void dispose() {
		CoordAction.listenerList.remove(shadowCenterText);
	}

}
