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

package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.tool.ScaleBar;
import gov.nasa.arc.dert.ui.DoubleSpinner;
import gov.nasa.arc.dert.ui.DoubleTextField;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Provides controls for setting options for scales.
 *
 */
public class ScaleBarPanel extends MapElementBasePanel {

	// Controls
	private DoubleTextField sizeText, radiusText;
	private DoubleSpinner azSpinner, tiltSpinner;
	private JSpinner cellsSpinner;
	private JCheckBox autoLabelButton;
	private JLabel dimensions;

	private ScaleBar scale;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public ScaleBarPanel(MapElement mapElement) {
		super(mapElement);
	}

	@Override
	protected void addFields(ArrayList<Component> compList) {
		super.addFields(compList);

		compList.add(new JLabel("Dimensions", SwingConstants.RIGHT));
		dimensions = new JLabel();
		compList.add(dimensions);

		compList.add(new JLabel("Cell Count", SwingConstants.RIGHT));
		cellsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000, 1));
		compList.add(cellsSpinner);
		cellsSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				int cells = (Integer) cellsSpinner.getValue();
				scale.setCellCount(cells);
				setDimensions();
			}
		});

		compList.add(new JLabel("Cell Size", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, Landscape.defaultCellSize/10, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				scale.setSize(value);
			}
		};
		compList.add(sizeText);

		compList.add(new JLabel("Radius", SwingConstants.RIGHT));
		radiusText = new DoubleTextField(8, Landscape.defaultCellSize/100, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				scale.setCellRadius(value);
			}
		};
		compList.add(radiusText);

		compList.add(new JLabel("AutoLabel", SwingConstants.RIGHT));		
		autoLabelButton = new JCheckBox("enabled");
		autoLabelButton.setToolTipText("set label automatically");
		autoLabelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				scale.setAutoLabel(autoLabelButton.isSelected());
			}
		});
		compList.add(autoLabelButton);

		compList.add(new JLabel("Azimuth", SwingConstants.RIGHT));
		azSpinner = new DoubleSpinner(0, 0, 359, 1, true) {
			@Override
			public void stateChanged(ChangeEvent event) {
				super.stateChanged(event);
				double azimuth = ((Double) azSpinner.getValue());
				scale.setAzimuth(azimuth);
			}
		};
		azSpinner.setToolTipText("rotate scale around vertical axis");
		compList.add(azSpinner);

		compList.add(new JLabel("Tilt", SwingConstants.RIGHT));
		tiltSpinner = new DoubleSpinner(0, -90, 90, 1, true) {
			@Override
			public void stateChanged(ChangeEvent event) {
				super.stateChanged(event);
				double tilt = ((Double) tiltSpinner.getValue());
				scale.setTilt(tilt);
			}
		};
		tiltSpinner.setToolTipText("rotate scale around horizontal axis");
		compList.add(tiltSpinner);
	}

	private void setDimensions() {
		double size = scale.getSize();
		int cells = scale.getCellCount();
		String str = "Length=" + formatter.format(cells * size);
		dimensions.setText(str);
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		super.setMapElement(mapElement);
		scale = (ScaleBar) mapElement;
		setLocation(locationText, locLabel, scale.getTranslation());
		sizeText.setValue(scale.getSize());
		radiusText.setValue(scale.getCellRadius());
		autoLabelButton.setSelected(scale.isAutoLabel());
		cellsSpinner.setValue(scale.getCellCount());
		azSpinner.setValue(scale.getAzimuth());
		tiltSpinner.setValue(scale.getTilt());
	}

}
