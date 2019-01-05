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

package gov.nasa.arc.dert.view.surfaceandlayers;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.LayerManager;
import gov.nasa.arc.dert.landscape.layer.LayerInfo;
import gov.nasa.arc.dert.landscape.layer.LayerInfo.LayerType;
import gov.nasa.arc.dert.ui.IntSpinner;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;

import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Provides the layer controls for the SurfaceAndLayersView.
 *
 */
public class LayerPanel extends JPanel {
	
	// Lock icon
	protected static ImageIcon lockedIcon = Icons.getImageIcon("locked.png");
	protected static ImageIcon unlockedIcon = Icons.getImageIcon("unlocked.png");

	// Controls
	private JLabel layerLabel;
	private IntSpinner opacity;
	private JCheckBox lockBox;
	private JCheckBox showBox;
	private int index;
	
	private LayersPanel parent;

	/**
	 * Constructor
	 * 
	 * @param s
	 */
	public LayerPanel(LayersPanel parent, int index) {
		this.index = index;
		this.parent = parent;
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(new JLabel(" " + (index+1) + " "));
		addShowBox();
		addLockBox();
		addOpacitySpinner();
		layerLabel = new JLabel();
		add(layerLabel);
	}

	public void set(LayerInfo layerInfo) {
		String str = layerInfo.toString();
		if (layerInfo.type != LayerType.none)
			str += ", "+layerInfo.type;
		if (layerInfo.colorMap != null)
			str += ", colormap=" + layerInfo.colorMap.getName();
		layerLabel.setText(str);
		showBox.setSelected(layerInfo.show == 1);
		showBox.setEnabled(layerInfo.type != LayerType.none);
		lockBox.setSelected(!layerInfo.autoblend);
		lockBox.setEnabled(layerInfo.type != LayerType.none);
		opacity.setValueNoChange((int)(layerInfo.opacity*100));
		opacity.setEnabled(layerInfo.type != LayerType.none);
	}
	
	public void setOpacity(int value) {
		opacity.setValueNoChange(value);
	}
	
	private void addShowBox() {
		showBox = new JCheckBox();
		showBox.setToolTipText("Layer is visible");
		showBox.setSelected(true);
		showBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				LayerManager layerManager = Landscape.getInstance().getLayerManager();
				LayerInfo layerInfo = layerManager.getVisibleLayers().get(index);
				layerInfo.show = showBox.isSelected() ? 1 : 0;
				layerManager.setLayerBlendFactor(index, (float)layerInfo.opacity);
				Landscape.getInstance().markDirty(DirtyType.RenderState);
			}
		});
		add(showBox);
	}
	
	private void addLockBox() {
		lockBox = new JCheckBox(unlockedIcon);
		lockBox.setToolTipText("unlock for automatic opacity adjustment for this layer");
		lockBox.setSelectedIcon(lockedIcon);
		if (index == 0) {
			lockBox.setSelected(true);
			lockBox.setEnabled(false);			
		}
		else {
			lockBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					LayerManager layerManager = Landscape.getInstance().getLayerManager();
					LayerInfo layerInfo = layerManager.getVisibleLayers().get(index);
					layerInfo.autoblend = !lockBox.isSelected();
				}
			});
		}		
		add(lockBox);
	}

	private void addOpacitySpinner() {
		opacity = new IntSpinner(0, 0, 100, 1, false, "###") {
			@Override
			public void stateChanged(ChangeEvent event) {
				int value = (Integer) getValue();
				double blendValue = value/100.0;
				LayerManager layerManager = Landscape.getInstance().getLayerManager();
				layerManager.setLayerBlendFactor(index, (float) blendValue);
				parent.adjustBlendFactors(value-lastValue, index);
				Landscape.getInstance().markDirty(DirtyType.RenderState);
				lastValue = value;
			}
		};
		opacity.setToolTipText("percent opacity of this layer");
		add(opacity);
	}

}
