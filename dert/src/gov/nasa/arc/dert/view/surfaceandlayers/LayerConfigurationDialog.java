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
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.ui.AbstractDialog;
import gov.nasa.arc.dert.ui.GBCHelper;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.ardor3d.math.Vector3;

/**
 * Dialog for configuring which layers are visible on the landscape and their
 * order.
 *
 */
public class LayerConfigurationDialog extends AbstractDialog {

	// Controls
	private JList visibleLayerList, availableLayerList;
	private JButton up, down, add, remove;

	// Layers
	private Vector<LayerInfo> visibleLayers, availableLayers;
	private LayerInfo currentSelected, currentLayer;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public LayerConfigurationDialog(Dialog parent) {
		super(parent, "Layer Configuration", true, false);
	}

	@Override
	protected void build() {
		super.build();
		LayerManager layerManager = Landscape.getInstance().getLayerManager();
		availableLayers = new Vector<LayerInfo>(layerManager.getAvailableLayers());
		visibleLayers = new Vector<LayerInfo>(layerManager.getVisibleLayers());

		contentArea.setLayout(new GridBagLayout());

		// List of displayed layers
		visibleLayerList = new JList(visibleLayers);
		visibleLayerList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		visibleLayerList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				currentSelected = (LayerInfo) visibleLayerList.getSelectedValue();
				if (currentSelected == null) {
					return;
				}
				boolean doUp = currentSelected.layerNumber > 0;
				if ((currentSelected.layerNumber == 1)
					&& !((currentSelected.type == LayerType.colorimage) || (currentSelected.type == LayerType.grayimage))) {
					doUp = false;
				}
				up.setEnabled(doUp);
				down.setEnabled(currentSelected.layerNumber < 7);
				remove.setEnabled(currentSelected.type != LayerType.none);
				boolean doAdd = true;
				if (currentLayer == null) {
					doAdd = false;
				} else if ((currentSelected.layerNumber == 0)
					&& !((currentLayer.type == LayerType.colorimage) || (currentLayer.type == LayerType.grayimage))) {
					doAdd = false;
				}
				add.setEnabled(doAdd);
			}
		});
		JScrollPane scrollPane = new JScrollPane(visibleLayerList);
		scrollPane.setPreferredSize(new Dimension(200, 128));
		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.add(new JLabel("Visible Layers", SwingConstants.CENTER), BorderLayout.NORTH);
		listPanel.add(scrollPane, BorderLayout.CENTER);
		contentArea.add(listPanel,
			GBCHelper.getGBC(0, 0, 4, 4, GridBagConstraints.WEST, GridBagConstraints.BOTH, 1, 1));

		JPanel buttonPanel = new JPanel(new GridBagLayout());
		// Button to move currently selected layer up in the list
		up = new JButton("Up");
		up.setToolTipText("move selected visible layer up");
		up.setEnabled(false);
		up.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int selectedIndex = visibleLayerList.getSelectedIndex();
				LayerInfo li = visibleLayers.remove(selectedIndex);
				visibleLayers.add(selectedIndex - 1, li);
				li.layerNumber = selectedIndex;
				li = visibleLayers.get(selectedIndex);
				if (li != null) {
					li.layerNumber = selectedIndex;
				}
				visibleLayerList.setListData(visibleLayers);
				visibleLayerList.setSelectedIndex(selectedIndex - 1);
			}
		});
		buttonPanel.add(up, GBCHelper.getGBC(0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 0));

		// Button to move a layer from the available list to the selected list
		add = new JButton(Icons.getImageIcon("prev_16.png"));
		add.setToolTipText("move selected available layer to the selected position in the visible list");
		add.setEnabled(false);
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				// get the index of the slot in the visible layers
				int selectedIndex = visibleLayerList.getSelectedIndex();
				// get the index of the layer selected from the available layers
				int index = availableLayerList.getSelectedIndex();
				// remove the selected layer from the available layers
				LayerInfo li = availableLayers.remove(index);
				// get the layer from the slot selected in the visible layers
				LayerInfo sli = visibleLayers.get(selectedIndex);
				// put the newly selected layer into the slot
				visibleLayers.set(selectedIndex, li);
				// set the layer number (0=first layer, 1=second layer ...)
				li.layerNumber = selectedIndex;
				// put the updated visible list in the list display
				visibleLayerList.setListData(visibleLayers);
				// put the old layer back in the available layers list
				if (sli.type != LayerType.none) {
					sli.layerNumber = -1;
					availableLayers.add(sli);
				}
				// update the available list in the list display
				availableLayerList.setListData(availableLayers);
				visibleLayerList.setSelectedIndex(selectedIndex);
			}
		});
		buttonPanel.add(add, GBCHelper.getGBC(0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 0));

		// Button to move the selected layer from the display list to the
		// available list
		remove = new JButton(Icons.getImageIcon("next_16.png"));
		remove.setToolTipText("move selected visible layer to the available list");
		remove.setEnabled(false);
		remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int selectedIndex = visibleLayerList.getSelectedIndex();
				LayerInfo li = visibleLayers.get(selectedIndex);
				if (li.type != LayerType.none) {
					visibleLayers.set(selectedIndex, new LayerInfo("None", "none", selectedIndex));
					li.layerNumber = -1;
					availableLayers.add(li);
				}
				visibleLayerList.setListData(visibleLayers);
				availableLayerList.setListData(availableLayers);
				visibleLayerList.setSelectedIndex(selectedIndex);
			}
		});
		buttonPanel.add(remove, GBCHelper.getGBC(0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 0));

		// Button to move the selected layer down in the display list
		down = new JButton("Down");
		down.setToolTipText("move selected visible layer down");
		down.setEnabled(false);
		down.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int selectedIndex = visibleLayerList.getSelectedIndex();
				LayerInfo li = visibleLayers.remove(selectedIndex);
				li.layerNumber = selectedIndex + 1;
				visibleLayers.add(selectedIndex, li);
				li = visibleLayers.get(selectedIndex);
				if (li != null) {
					li.layerNumber = selectedIndex;
				}
				visibleLayerList.setListData(visibleLayers);
				visibleLayerList.setSelectedIndex(selectedIndex + 1);
			}
		});
		buttonPanel.add(down, GBCHelper.getGBC(0, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 0));
		contentArea.add(buttonPanel,
			GBCHelper.getGBC(5, 0, 1, 4, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 0));

		// List of available layers
		availableLayerList = new JList(availableLayers);
		availableLayerList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane = new JScrollPane(availableLayerList);
		scrollPane.setPreferredSize(new Dimension(200, 128));
		listPanel = new JPanel(new BorderLayout());
		listPanel.add(new JLabel("Available Layers", SwingConstants.CENTER), BorderLayout.NORTH);
		listPanel.add(scrollPane, BorderLayout.CENTER);
		availableLayerList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				currentLayer = (LayerInfo) availableLayerList.getSelectedValue();
				if (currentLayer == null) {
					return;
				}
				boolean doAdd = true;
				if (currentSelected == null) {
					doAdd = false;
				} else if ((currentSelected.layerNumber == 0)
					&& !((currentLayer.type == LayerType.colorimage) || (currentLayer.type == LayerType.grayimage))) {
					doAdd = false;
				}
				add.setEnabled(doAdd);
			}
		});
		contentArea.add(listPanel, GBCHelper.getGBC(6, 0, 4, 4, GridBagConstraints.EAST, GridBagConstraints.BOTH, 1, 1));
	}

	@Override
	public boolean okPressed() {
		for (int i=0; i<visibleLayers.size(); ++i) {
			LayerInfo li = visibleLayers.get(i);
			if (li.type == LayerType.derivative) {
				li.gmLoc = new Vector3(World.getInstance().getMarble().getTranslation());
				li.gmLoc.setZ(li.gmLoc.getZ()+Landscape.getInstance().getMinimumElevation());
				li.minimum = Double.NaN;
				li.maximum = Double.NaN;
			}
		}
		LayerManager layerManager = Landscape.getInstance().getLayerManager();
		layerManager.setLayerSelection(visibleLayers, availableLayers);
		result = new Boolean(true);
		return (true);
	}

}
