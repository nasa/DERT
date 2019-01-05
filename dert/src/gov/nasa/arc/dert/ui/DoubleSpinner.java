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

import java.text.DecimalFormat;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A JSpinner with a model that handles doubles
 *
 */
public class DoubleSpinner extends JSpinner implements ChangeListener {

	protected double lastValue;
	protected SpinnerNumberModel model;

	/**
	 * Constructor
	 * 
	 * @param value
	 * @param min
	 * @param max
	 * @param step
	 * @param wrap
	 */
	public DoubleSpinner(double value, double min, double max, double step, boolean wrap) {
		this(value, min, max, step, wrap, "###0.00");
	}

	/**
	 * Constructor
	 * 
	 * @param value
	 * @param min
	 * @param max
	 * @param step
	 * @param wrap
	 * @param format
	 */
	public DoubleSpinner(double value, double min, double max, double step, boolean wrap, String format) {
		super();
		try {
			DecimalFormat formatter = new DecimalFormat(format);
			String str = formatter.format(min);
			min = Double.parseDouble(str);
			str = formatter.format(max);
			max = Double.parseDouble(str);
			if (value < min) {
				value = min;
			} else if (value > max) {
				value = max;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (wrap) {
			model = new SpinnerNumberModel(value, min, max, step) {
				@Override
				public Object getNextValue() {
					Object obj = super.getNextValue();
					if (obj == null) {
						return (getMinimum());
					}
					return (obj);
				}

				@Override
				public Object getPreviousValue() {
					Object obj = super.getPreviousValue();
					if (obj == null) {
						return (getMaximum());
					}
					return (obj);
				}

				@Override
				public void setValue(Object obj) {
					double val = (Double) obj;
					double maximum = (Double) super.getMaximum();
					double minimum = (Double) super.getMinimum();
					if (val < minimum) {
						val = maximum - (minimum-val)+(Double)getStepSize();
					} else if (val > maximum) {
						val = val-maximum-(Double)getStepSize();
					}
					super.setValue(new Double(val));
				}
			};
		} else {
			model = new SpinnerNumberModel(value, min, max, step);
		}
		setModel(model);
		model.addChangeListener(this);
		setEditor(new JSpinner.NumberEditor(this, format));
		lastValue = value;
	}

	/**
	 * The spinner changed, store the last value
	 */
	@Override
	public void stateChanged(ChangeEvent event) {
		lastValue = (Double) getValue();
	}

	/**
	 * Set the last value without triggering the change listener
	 * 
	 * @param val
	 */
	public void setValueNoChange(double val) {
		model.removeChangeListener(this);
		super.setValue(new Double(val));
		lastValue = val;
		model.addChangeListener(this);
	}

	/**
	 * Set the spinner maximum without triggering the change listener
	 * 
	 * @param max
	 */
	public void setMaximum(double max) {
		model.removeChangeListener(this);
		model.setMaximum(new Double(max));
		model.addChangeListener(this);
		if (lastValue > max)
			model.setValue(max);
	}

	/**
	 * Set the spinner minimum without triggering the change listener
	 * 
	 * @param min
	 */
	public void setMinimum(double min) {
		model.removeChangeListener(this);
		model.setMinimum(new Double(min));
		model.addChangeListener(this);
		if (lastValue < min)
			model.setValue(min);
	}

}
