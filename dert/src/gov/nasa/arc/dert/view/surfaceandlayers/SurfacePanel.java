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

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.LayerManager;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.state.State;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.DoubleSpinner;
import gov.nasa.arc.dert.ui.GBCHelper;
import gov.nasa.arc.dert.ui.GroupPanel;
import gov.nasa.arc.dert.view.world.WorldScene;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Provides controls for landscape surface options.
 *
 */
public class SurfacePanel extends GroupPanel {

	// Controls
	private JCheckBox shadingCheckBox, wireframeCheckBox, surfaceNormalsCheckBox;
	private JCheckBox gridButton;
	private DoubleSpinner vertExag;
	private ColorSelectionPanel surfaceColor;
	private JComboBox cellSizeCombo;
	private ColorSelectionPanel gridColor;

	// Grid
	private ReadOnlyColorRGBA gridColorRGBA;
	private DecimalFormat formatter;
	private double[] units;

	/**
	 * Constructor
	 * 
	 * @param s
	 */
	public SurfacePanel(State s) {
		super("Surface");
		formatter = new DecimalFormat("0");

		final Landscape landscape = Landscape.getInstance();
		final WorldScene scene = (WorldScene) Dert.getWorldView().getScenePanel().getScene();

		setLayout(new GridBagLayout());

		wireframeCheckBox = new JCheckBox("Wireframe");
		wireframeCheckBox.setToolTipText("display landscape as wireframe");
		wireframeCheckBox.setSelected(landscape.isWireFrame());
		wireframeCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				WireframeState wfs = new WireframeState();
				wfs.setEnabled(wireframeCheckBox.isSelected());
				Landscape.getInstance().setRenderState(wfs);
			}
		});
		add(wireframeCheckBox, GBCHelper.getGBC(0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0));

		shadingCheckBox = new JCheckBox("Surface Shading");
		shadingCheckBox.setToolTipText("shading determined by surface topography");
		shadingCheckBox.setSelected(landscape.isShadingFromSurface());
		shadingCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Landscape.getInstance().setShadingFromSurface(shadingCheckBox.isSelected());
			}
		});
		add(shadingCheckBox, GBCHelper.getGBC(0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0));

		surfaceNormalsCheckBox = new JCheckBox("Surface Normals");
		surfaceNormalsCheckBox.setToolTipText("display normal vector for each surface point");
		surfaceNormalsCheckBox.setSelected(scene.isNormalsEnabled());
		surfaceNormalsCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				scene.enableNormals(surfaceNormalsCheckBox.isSelected());
			}
		});
		add(surfaceNormalsCheckBox,
			GBCHelper.getGBC(0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0));

		JLabel label = new JLabel("Vert Exaggeration", SwingConstants.RIGHT);
		add(label, GBCHelper.getGBC(1, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0));
		vertExag = new DoubleSpinner(World.getInstance().getVerticalExaggeration(), 0.01, 10, 0.05, false, "#0.00") {
			@Override
			public void stateChanged(ChangeEvent event) {
				double val = ((Double) vertExag.getValue());
				World.getInstance().setVerticalExaggeration(val);
			}
		};
		vertExag.setToolTipText("scale elevation");
		add(vertExag, GBCHelper.getGBC(2, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0));

		label = new JLabel("Surface Color", SwingConstants.RIGHT);
		add(label, GBCHelper.getGBC(1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0));
		surfaceColor = new ColorSelectionPanel(landscape.getSurfaceColor()) {
			@Override
			public void doColor(Color color) {
				landscape.setSurfaceColor(color);
			}
		};
		add(surfaceColor, GBCHelper.getGBC(2, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0));

		gridButton = new JCheckBox("Surface Grid");
		gridButton.setToolTipText("display a grid on the surface");
		gridButton.setSelected(landscape.getLayerManager().isGridEnabled());
		gridButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				LayerManager layerManager = Landscape.getInstance().getLayerManager();
				layerManager.enableGrid(gridButton.isSelected());
				Landscape.getInstance().markDirty(DirtyType.RenderState);
			}
		});
		add(gridButton, GBCHelper.getGBC(0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0));

		label = new JLabel("Grid Cell Size", SwingConstants.RIGHT);
		add(label, GBCHelper.getGBC(1, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0));
		String[] item = createUnits();
		cellSizeCombo = new JComboBox(item);
		double gridCellSize = landscape.getLayerManager().getGridCellSize();
		for (int i = 0; i < units.length; ++i) {
			if (gridCellSize <= units[i]) {
				cellSizeCombo.setSelectedIndex(i);
				break;
			}
		}
		cellSizeCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int index = cellSizeCombo.getSelectedIndex();
				LayerManager layerManager = Landscape.getInstance().getLayerManager();
				layerManager.setGridCellSize(units[index]);
				Landscape.getInstance().markDirty(DirtyType.RenderState);
			}
		});
		add(cellSizeCombo, GBCHelper.getGBC(2, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0));

		label = new JLabel("Grid Color", SwingConstants.RIGHT);
		add(label, GBCHelper.getGBC(1, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0));
		gridColorRGBA = landscape.getLayerManager().getGridColor();
		final Color gCol = new Color(gridColorRGBA.getRed(), gridColorRGBA.getGreen(), gridColorRGBA.getBlue(),
			gridColorRGBA.getAlpha());
		gridColor = new ColorSelectionPanel(gCol) {
			@Override
			public void doColor(Color color) {
				gridColorRGBA = new ColorRGBA(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f,
					color.getAlpha() / 255f);
				LayerManager layerManager = Landscape.getInstance().getLayerManager();
				layerManager.setGridColor(gridColorRGBA);
				Landscape.getInstance().markDirty(DirtyType.RenderState);
			}
		};
		add(gridColor, GBCHelper.getGBC(2, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0));
	}

	private String[] createUnits() {
		units = new double[10];
		units[0] = 0.1 * Landscape.defaultCellSize;
		units[1] = 0.5 * Landscape.defaultCellSize;
		units[2] = Landscape.defaultCellSize;
		units[3] = 2 * Landscape.defaultCellSize;
		units[4] = 2.5 * Landscape.defaultCellSize;
		units[5] = 5 * Landscape.defaultCellSize;
		units[6] = 10 * Landscape.defaultCellSize;
		units[7] = 20 * Landscape.defaultCellSize;
		units[8] = 25 * Landscape.defaultCellSize;
		units[9] = 50 * Landscape.defaultCellSize;
		if (Landscape.defaultCellSize < 1) {
			formatter.applyPattern("0.000");
		}
		String[] item = new String[units.length];
		for (int i = 0; i < units.length; ++i) {
			item[i] = formatter.format(units[i]);
		}
		return (item);
	}

}
