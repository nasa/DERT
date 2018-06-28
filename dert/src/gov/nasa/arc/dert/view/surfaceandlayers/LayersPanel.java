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

package gov.nasa.arc.dert.view.surfaceandlayers;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.LayerManager;
import gov.nasa.arc.dert.landscape.layer.LayerInfo;
import gov.nasa.arc.dert.state.State;
import gov.nasa.arc.dert.ui.GroupPanel;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Provides the layer controls for the SurfaceAndLayersView.
 *
 */
public class LayersPanel extends GroupPanel {

	// Controls
	private JCheckBox showLayersCheckBox;
	private JButton configureButton, distributeButton;
	private LayerPanel[] layer;

	// View state
	private State state;

	/**
	 * Constructor
	 * 
	 * @param s
	 */
	public LayersPanel(State s) {
		super("Layers");
		state = s;

		setLayout(new BorderLayout());

		Landscape landscape = Landscape.getInstance();
		Vector<LayerInfo> visibleLayers = landscape.getLayerManager().getVisibleLayers();

		JPanel topPanel = new JPanel(new FlowLayout());
		showLayersCheckBox = new JCheckBox("Show Layers");
		showLayersCheckBox.setToolTipText("display the visible layers");
		showLayersCheckBox.setSelected(landscape.isLayersEnabled());
		showLayersCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Landscape.getInstance().enableLayers(showLayersCheckBox.isSelected());
			}
		});
		topPanel.add(showLayersCheckBox);
		configureButton = new JButton("Configure");
		configureButton.setToolTipText("add and remove visible layers");
		configureButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				LayerConfigurationDialog dialog = new LayerConfigurationDialog((Dialog) state.getViewData().getViewWindow());
				if (dialog.open() != null) {
					updateVisibleLayers();
					Landscape.getInstance().resetLayers();
				}
			}
		});
		topPanel.add(configureButton);
		distributeButton = new JButton("Distribute");
		distributeButton.setToolTipText("distribute total color contribution equally among unlocked layers");
		distributeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				adjustBlendFactors(1, -1);
				Landscape.getInstance().markDirty(DirtyType.RenderState);
			}
		});
		topPanel.add(distributeButton);
		add(topPanel, BorderLayout.NORTH);

		// create the layer selections
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(LayerManager.NUM_LAYERS, 1, 0, 0));

		layer = new LayerPanel[LayerManager.NUM_LAYERS];

		for (int i = 0; i < LayerManager.NUM_LAYERS; ++i) {
			layer[i] = new LayerPanel(this, i);
			layer[i].set(visibleLayers.get(i));
			panel.add(layer[i]);
		}
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * An available layer has been added or removed
	 */
	public void updateVisibleLayers() {
		Vector<LayerInfo> visibleLayers = Landscape.getInstance().getLayerManager().getVisibleLayers();
		for (int i = 0; i < visibleLayers.size(); ++i) {
			layer[i].set(visibleLayers.get(i));
		}
		adjustBlendFactors(1, -1);
	}

	public void adjustBlendFactors(int v, int index) {
		// no change in value, return
		if (v == 0)
			return;
		
		// get the layer manager and current blend factors
		LayerManager layerManager = Landscape.getInstance().getLayerManager();
		Vector<LayerInfo> visibleLayers = layerManager.getVisibleLayers();
		
		// single spinner
		if (index >= 0) {
			
			// not automatically blended, return
			if (!visibleLayers.get(index).autoblend) {
				return;
			}
			
			// get number of auto blended layers
			int n = 0;
			for (int i=1; i<visibleLayers.size(); ++i)
				if (visibleLayers.get(i).autoblend && (i != index))
					n ++;
			
			// only one unlocked spinner, return
			if (n == 0)
				return;	
			
			// get the change for each spinner
			float d = -0.01f*v/(float)n;
			
			// set the spinners and update the layer manager
			for (int i=1; i<visibleLayers.size(); ++i)
				if (visibleLayers.get(i).autoblend && (i != index)) {
					float bf = (float)visibleLayers.get(i).opacity;
					bf += d;
					bf = Math.max(0, bf);
					bf = Math.min(1, bf);
					layer[i].setOpacity((int)(bf*100));
					layerManager.setLayerBlendFactor(i, bf);
				}
		}
		
		// distribute throughout all unlocked spinners
		else {
			// set the first layer (never unlocked)
			layer[0].setOpacity((int)(visibleLayers.get(0).opacity*100));
			layerManager.setLayerBlendFactor(0, (float)visibleLayers.get(0).opacity);
			
			int n = 1;
			for (int i = 1; i < visibleLayers.size(); ++i) {
				// set the unlocked spinners
				if (visibleLayers.get(i).autoblend) {
					n ++;
					float bf = 1.0f/n;
					layer[i].setOpacity((int)(bf*100));
					layerManager.setLayerBlendFactor(i, bf);
				}
				// set the locked spinners
				else {
					layer[i].setOpacity((int)(visibleLayers.get(i).opacity*100));
					layerManager.setLayerBlendFactor(i, (float)visibleLayers.get(i).opacity);
				}
			}
		}
	}

}
