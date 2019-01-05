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

package gov.nasa.arc.dert.ui;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.SurfaceAndLayersState;
import gov.nasa.arc.dert.util.ColorMap;
import gov.nasa.arc.dert.view.surfaceandlayers.SurfaceAndLayersView;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

/**
 * Provides a dialog for changing color map options.
 *
 */
public class ColorMapDialog extends AbstractDialog {

	// Range spinners
	private DoubleSpinner minSpinner, maxSpinner;

	// Range limits and defaults
	private double defaultMin, defaultMax, lowerLimit, upperLimit;

	// List of available color maps
	private JComboBox colorMapName;

	// Gradient selection
	private JCheckBox gradient;

	// The selected color map
	private ColorMap colorMap;

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param title
	 * @param lowerLimit
	 * @param upperLimit
	 * @param cMap
	 */
	public ColorMapDialog(Dialog parent, String title, double lowerLimit, double upperLimit, ColorMap cMap) {
		super(parent, title, false, false);
		this.defaultMin = cMap.getMinimum();
		this.defaultMax = cMap.getMaximum();
		this.colorMap = cMap;
		this.upperLimit = upperLimit;
		this.lowerLimit = lowerLimit;
	}

	@Override
	protected void build() {
		super.build();
		cancelButton.setText("Default");
		okButton.setText("Close");
		
		ArrayList<Component> compList = new ArrayList<Component>();
		
		compList.add(new JLabel("Color Map", SwingConstants.RIGHT));
		colorMapName = new JComboBox(ColorMap.getColorMapNames());
		colorMapName.setEditable(false);
		colorMapName.setSelectedItem(colorMap.getName());
		colorMapName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				colorMap.setName((String) colorMapName.getSelectedItem());
				defaultMin = colorMap.getMinimum();
				defaultMax = colorMap.getMaximum();
				setRange(defaultMin, defaultMax);
				SurfaceAndLayersState sls = (SurfaceAndLayersState)ConfigurationManager.getInstance().getCurrentConfiguration().getState("SurfaceAndLayersState");
				SurfaceAndLayersView slv = (SurfaceAndLayersView)sls.getViewData().getView();
				if ((slv != null) && slv.isVisible())
					slv.updateVisibleLayers();
			}
		});
		compList.add(colorMapName);
		compList.add(new JLabel("Gradient", SwingConstants.RIGHT));
		gradient = new JCheckBox("");
		gradient.setSelected(colorMap.isGradient());
		gradient.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				colorMap.setGradient(gradient.isSelected());
			}
		});
		compList.add(gradient);
		compList.add(new JLabel("Maximum", SwingConstants.RIGHT));
		int n = (int)Math.log10(Math.abs(upperLimit-defaultMin));
		double step = Math.pow(10, n)/100;
		maxSpinner = new DoubleSpinner(defaultMax, defaultMin, upperLimit, step, false, Landscape.format) {
			@Override
			public void stateChanged(ChangeEvent event) {
				super.stateChanged(event);
				double max = ((Double) maxSpinner.getValue());
				maximumChanged(max);
				minSpinner.setMaximum(max);
			}
		};
		compList.add(maxSpinner);

		compList.add(new JLabel("Minimum", SwingConstants.RIGHT));
		n = (int)Math.log10(Math.abs(defaultMax-lowerLimit));
		step = Math.pow(10, n)/100;
		minSpinner = new DoubleSpinner(defaultMin, lowerLimit, defaultMax, step, false, Landscape.format) {
			@Override
			public void stateChanged(ChangeEvent event) {
				super.stateChanged(event);
				double min = ((Double) minSpinner.getValue());
				minimumChanged(min);
				maxSpinner.setMinimum(min);
			}
		};
		compList.add(minSpinner);
		
		contentArea.setLayout(new BorderLayout());
		contentArea.add(new FieldPanel(compList), BorderLayout.CENTER);

		getRootPane().setDefaultButton(null);
	}

	@Override
	protected boolean okPressed() {
		return (true);
	}

	@Override
	protected boolean cancelPressed() {
		minSpinner.setValue(defaultMin);
		maxSpinner.setValue(defaultMax);
		return (false);
	}

	/**
	 * Set the range of the color bar
	 * 
	 * @param min
	 * @param max
	 */
	public void setRange(double min, double max) {
		minSpinner.setValueNoChange(min);
		maxSpinner.setValueNoChange(max);
	}

	/**
	 * Notify the color map that the minimum range value changed.
	 * 
	 * @param value
	 */
	public void minimumChanged(double value) {
		colorMap.setRange(value, colorMap.getMaximum());
	}

	/**
	 * Notify the color map that the maximum range value changed.
	 * 
	 * @param value
	 */
	public void maximumChanged(double value) {
		colorMap.setRange(colorMap.getMinimum(), value);
	}
}
