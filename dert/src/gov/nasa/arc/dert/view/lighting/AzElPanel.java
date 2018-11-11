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

import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.ui.AzElDisk;
import gov.nasa.arc.dert.ui.DoubleArrayTextField;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.ardor3d.math.Vector3;

/**
 * Provides the controls for positioning the artificial light with the
 * LightPositionView.
 *
 */
public class AzElPanel extends JPanel {

	// The Az/El control
	private AzElDisk azElDisk;

	// The displays of current az/el and direction
	private DoubleArrayTextField azElText, dirText;

	// The light fields
	private Vector3 direction;
	private double[] azElArray, dirArray;
	private float[] lastAzEl;

	/**
	 * Constructor
	 */
	public AzElPanel() {
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout());
		direction = new Vector3();
		dirArray = direction.toArray(null);
		azElArray = new double[2];
		lastAzEl = new float[2];
		lastAzEl[0] = (float) Math.toDegrees(World.getInstance().getLighting().getLight().getAzimuth());
		if (lastAzEl[0] < 0)
			lastAzEl[0] += 360;
		lastAzEl[1] = (float) Math.toDegrees(World.getInstance().getLighting().getLight().getElevation());
		azElDisk = new AzElDisk(lastAzEl[0], lastAzEl[1]) {
			@Override
			public void applyAzEl(double az, double el) {
				azElArray[0] = az;
				azElArray[1] = el;
				azElText.setValue(azElArray);
				az = (float) Math.toRadians(az);
				el = (float) Math.toRadians(el);
				MathUtil.azElToDirection(az, el, direction);
				direction.negateLocal();
				direction.toArray(dirArray);
				dirText.setValue(dirArray);
				World.getInstance().getLighting().setLightPosition(az, el);
			}
		};
		add(azElDisk, BorderLayout.CENTER);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(2, 1));
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JLabel label = new JLabel("Az/El:", SwingConstants.LEFT);
		panel.add(label, BorderLayout.WEST);
		azElText = new DoubleArrayTextField(8, lastAzEl, "0.00") {
			@Override
			protected void handleChange(double[] value) {
				if (value == null) {
					return;
				}
				if ((value == null) || (value.length != 2)) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				if (value[0] < 0) {
					value[0] += 360;
				}
				azElDisk.setCurrentAzEl(value[0], value[1]);
			}
		};
		azElText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String str = event.getActionCommand();
				try {
					double[] value = StringUtil.stringToDoubleArray(str);
					if ((value == null) || (value.length != 2)) {
						Toolkit.getDefaultToolkit().beep();
						return;
					}
					if (value[0] < 0) {
						value[0] += 360;
					}
					azElDisk.setCurrentAzEl((float) value[0], (float) value[1]);
				} catch (Exception e) {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		});
		panel.add(azElText, BorderLayout.CENTER);
		topPanel.add(panel);
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		label = new JLabel("Direction:", SwingConstants.LEFT);
		panel.add(label, BorderLayout.WEST);
		dirText = new DoubleArrayTextField(10, new float[3], "0.00");
		dirText.setBackground(panel.getBackground());
		dirText.setEditable(false);
		panel.add(dirText, BorderLayout.CENTER);
		topPanel.add(panel);
		add(topPanel, BorderLayout.NORTH);
	}
}
