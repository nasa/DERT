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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Provides local mean solar time controls for positioning the solar light in
 * the LightPositionView.
 *
 */
public class LMSTPanel extends JPanel {

	// Time controls
	private JSpinner hour, minute, second, sol;
	private JButton current;

	// Time and date fields
	private int lastSecond, lastMinute;
	private Date selectedDate;
	
	private boolean doSetTime;

	/**
	 * Constructor
	 */
	public LMSTPanel() {
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel(new GridLayout(2, 1));
		JPanel solPart = new JPanel(new FlowLayout(FlowLayout.LEFT));
		solPart.add(new JLabel("Sol", SwingConstants.RIGHT));
		SpinnerNumberModel model = new SpinnerNumberModel(new Integer(1), new Integer(1),
			new Integer(Integer.MAX_VALUE), new Integer(1));
		sol = new JSpinner(model);
		sol.setSize(100, -1);
		sol.setToolTipText("sol (starts at 1)");
		sol.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				if (doSetTime)
					World.getInstance().setTime(getSelectedDate().getTime());
			}
		});
		solPart.add(sol);

		mainPanel.add(solPart);

		JPanel timePart = new JPanel();
		timePart.setLayout(new FlowLayout());

		JLabel label = new JLabel("H:M:S ");
		timePart.add(label);
		hour = new JSpinner(new SpinnerNumberModel(new Integer(0), new Integer(0), new Integer(23), new Integer(1)));
		timePart.add(hour);
		hour.setToolTipText("hours (0-23)");
		hour.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				if (doSetTime)
					World.getInstance().setTime(getSelectedDate().getTime());
			}
		});
		minute = new JSpinner(new SpinnerNumberModel(new Integer(0), new Integer(0), new Integer(59), new Integer(1)));
		timePart.add(minute);
		minute.setToolTipText("minutes (0-59)");
		minute.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				if ((Integer) minute.getValue() == 0) {
					if (lastMinute == 59) {
						hour.setValue(new Integer((Integer) hour.getValue() + 1));
					}
				} else if ((Integer) minute.getValue() == 59) {
					if (lastMinute == 0) {
						hour.setValue(new Integer((Integer) hour.getValue() - 1));
					}
				}
				lastMinute = (Integer) minute.getValue();
				if (doSetTime)
					World.getInstance().setTime(getSelectedDate().getTime());
			}
		});
		second = new JSpinner(new SpinnerNumberModel(new Integer(0), new Integer(0), new Integer(59), new Integer(1)));
		timePart.add(second);
		second.setToolTipText("seconds (0-59)");
		second.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				if ((Integer) second.getValue() == 0) {
					if (lastSecond == 59) {
						minute.setValue(new Integer((Integer) minute.getValue() + 1));
					}
				} else if ((Integer) second.getValue() == 59) {
					if (lastSecond == 0) {
						minute.setValue(new Integer((Integer) minute.getValue() - 1));
					}
				}
				lastSecond = (Integer) second.getValue();
				if (doSetTime)
					World.getInstance().setTime(getSelectedDate().getTime());
			}
		});
		current = new JButton();
		timePart.add(current);
		current.setText("Now");
		current.setToolTipText("set to current date and time");
		current.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				doSetTime = false;
				setCurrentDate(new Date());
				doSetTime = true;
				World.getInstance().setTime(getSelectedDate().getTime());
			}
		});

		mainPanel.add(timePart);

		add(mainPanel, BorderLayout.NORTH);

		selectedDate = new Date(World.getInstance().getTime());
		setCurrentDate(selectedDate);
		doSetTime = true;

	}

	/**
	 * Set the current date
	 * 
	 * @param currentDate
	 */
	public void setCurrentDate(Date currentDate) {
		selectedDate = currentDate;
		int[] lmst = World.getInstance().getLighting().dateToLMST(selectedDate);
		sol.setValue(lmst[0]);
		lastSecond = lmst[3];
		second.setValue(lastSecond);
		lastMinute = lmst[2];
		minute.setValue(lastMinute);
		int hr = lmst[1];
		hour.setValue(hr);
	}

	private Date getSelectedDate() {
		selectedDate = World
			.getInstance()
			.getLighting()
			.lmstToDate((Integer) sol.getValue(), (Integer) hour.getValue(), (Integer) minute.getValue(),
				(Integer) second.getValue());
		return (selectedDate);
	}

}
