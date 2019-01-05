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

package gov.nasa.arc.dert.view.graph;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.state.ProfileState;
import gov.nasa.arc.dert.ui.GBCHelper;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.JPanelView;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.ardor3d.math.Vector3;

/**
 * Presents the Profile tool data as a graph.
 *
 */
public class GraphView extends JPanelView {

	// The graph
	private Graph graph;

	// Field showing last picked point in the graph
	private JTextField lastPick;
	private Vector3 coord;

	// Java2D drawing surface
	private Canvas canvas;
	
	// display mode
	private JCheckBox axesEqualScale;

	/**
	 * Constructor
	 * 
	 * @param viewState
	 */
	public GraphView(ProfileState viewState) {
		super(viewState);
		setBackground(Color.white);
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		coord = new Vector3();
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new GridBagLayout());
		controlPanel.setBackground(Color.white);
		controlPanel.add(new JLabel("   Distance, Elevation: "),
			GBCHelper.getGBC(0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0));
		lastPick = new JTextField();
		lastPick.setBorder(null);
		controlPanel.add(lastPick,
			GBCHelper.getGBC(1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0));
		axesEqualScale = new JCheckBox("Y/X = 1");
		axesEqualScale.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				graph.setAxesEqualScale(axesEqualScale.isSelected());				
				canvas.repaint();
			}
		});
		axesEqualScale.setSelected(viewState.axesEqualScale);
		controlPanel.add(axesEqualScale, GBCHelper.getGBC(2, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 0));		
		add(controlPanel, BorderLayout.NORTH);

		canvas = new Canvas() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				doPaint((Graphics2D) g);
			}
		};
		canvas.setBackground(Color.white);
		canvas.setSize(viewState.getViewData().getWidth(), viewState.getViewData().getHeight());

		canvas.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent event) {
			}

			@Override
			public void mouseEntered(MouseEvent event) {
			}

			@Override
			public void mouseExited(MouseEvent event) {
			}

			@Override
			public void mousePressed(MouseEvent event) {
				int mouseX = event.getX();
				int mouseY = canvas.getHeight() - event.getY();
				if (event.getButton() == 1) {
					float[] point = graph.getValueAt(mouseX, mouseY);
					if (point == null) {
						lastPick.setText("");
					} else {
						lastPick.setText(StringUtil.format(point[0]) + ", " + StringUtil.format(point[1]));						
						coord.set(point[2], point[3], point[4]);
						Landscape.getInstance().worldToLocalCoordinate(coord);						
						World.getInstance().getMarble().update(coord, null, null);
					}
				}
				canvas.requestFocus();
				canvas.repaint();
			}

			@Override
			public void mouseReleased(MouseEvent event) {
			}
		});
		canvas.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent event) {
				graph.resize(event.getComponent().getWidth(), event.getComponent().getHeight());
			}
		});
		add(canvas, BorderLayout.CENTER);
		graph = viewState.getGraph();
	}

	/**
	 * Set the graph data
	 * 
	 * @param vertex
	 * @param vertexCount
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 */
	public void setData(float[] vertex, int vertexCount, float xMin, float xMax, float yMin, float yMax, float[] origVertex) {
		graph.setData(vertex, vertexCount, xMin, xMax, yMin, yMax, origVertex);
		lastPick.setText("");
		canvas.repaint();
	}

	/**
	 * Set the graph color
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		graph.setColor(color);
		canvas.repaint();
	}

	protected void doPaint(Graphics2D g2d) {
		graph.render(g2d);
	}

}
