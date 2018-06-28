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

package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.action.edit.CoordAction;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.tool.Plane;
import gov.nasa.arc.dert.ui.CoordTextField;
import gov.nasa.arc.dert.ui.DoubleSpinner;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Provides controls for setting options for plane tools.
 *
 */
public class PlanePanel extends MapElementBasePanel {

	// Controls
	private CoordTextField p0Location, p1Location, p2Location;
	private DoubleSpinner lengthSpinner, widthSpinner;
	private JCheckBox triangleCheckBox;
	private JLabel strikeAndDip;
	private JLabel p0Label, p1Label, p2Label;

	// The plane
	private Plane plane;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public PlanePanel(MapElement mapElement) {
		super(mapElement);
	}

	@Override
	protected void addFields(ArrayList<Component> compList) {

		p0Label = new JLabel("Point 0", SwingConstants.RIGHT);
		compList.add(p0Label);
		p0Location = new CoordTextField(22, "location of first point of triangle", Landscape.format, true) {
			@Override
			public void doChange(ReadOnlyVector3 result) {
				double z = Landscape.getInstance().getZ(result.getX(), result.getY());
				if (Double.isNaN(z)) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				if (Double.isNaN(result.getZ())) {
					plane.setPoint(0, result.getX(), result.getY(), z);
				}
				else {
					plane.getMarker(0).setZOffset(result.getZ()-z, false);
					plane.setPoint(0, result.getX(), result.getY(), z);
				}
			}			
		};
		CoordAction.listenerList.add(p0Location);
		compList.add(p0Location);

		p1Label = new JLabel("Point 1", SwingConstants.RIGHT);
		compList.add(p1Label);
		p1Location = new CoordTextField(22, "location of second point of triangle", Landscape.format, true) {
			@Override
			public void doChange(ReadOnlyVector3 result) {
				double z = Landscape.getInstance().getZ(result.getX(), result.getY());
				if (Double.isNaN(z)) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				if (Double.isNaN(result.getZ())) {
					plane.setPoint(1, result.getX(), result.getY(), z);
				}
				else {
					plane.getMarker(1).setZOffset(result.getZ()-z, false);
					plane.setPoint(1, result.getX(), result.getY(), z);
				}
			}			
		};
		CoordAction.listenerList.add(p1Location);
		compList.add(p1Location);

		p2Label = new JLabel("Point 2", SwingConstants.RIGHT);
		compList.add(p2Label);
		p2Location = new CoordTextField(22, "location of third point of triangle", Landscape.format, true) {
			@Override
			public void doChange(ReadOnlyVector3 result) {
				double z = Landscape.getInstance().getZ(result.getX(), result.getY());
				if (Double.isNaN(z)) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				if (Double.isNaN(result.getZ())) {
					plane.setPoint(2, result.getX(), result.getY(), z);
				}
				else {
					plane.getMarker(2).setZOffset(result.getZ()-z, false);
					plane.setPoint(2, result.getX(), result.getY(), z);
				}
			}			
		};
		CoordAction.listenerList.add(p2Location);
		compList.add(p2Location);
		
		compList.add(new JLabel("Triangle", SwingConstants.RIGHT));
		triangleCheckBox = new JCheckBox("visible");
		triangleCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				plane.setTriangleVisible(triangleCheckBox.isSelected());
				plane.markDirty(DirtyType.RenderState);
			}
		});
		compList.add(triangleCheckBox);

		double step = Landscape.getInstance().getPixelWidth();
		double min = 1;
		double max = 10000;
		double val = Plane.defaultSize;
		String fmt = "###0.00";
		if (step < 1) {
			min *= step;
			max *= step;
			val *= step;
			fmt = Landscape.format;
		}
//		else
//			step = 1;
		
		compList.add(new JLabel("Scale Dip Axis", SwingConstants.RIGHT));
		lengthSpinner = new DoubleSpinner(val, min, max, step, false, fmt) {
			@Override
			public void stateChanged(ChangeEvent event) {
				double lengthScale = ((Double) lengthSpinner.getValue());
				plane.setLengthScale(lengthScale);
			}
		};
		compList.add(lengthSpinner);

		compList.add(new JLabel("Scale Strike Axis", SwingConstants.RIGHT));
		widthSpinner = new DoubleSpinner(val, min, max, step, false, fmt) {
			@Override
			public void stateChanged(ChangeEvent event) {
				double widthScale = ((Double) widthSpinner.getValue());
				plane.setWidthScale(widthScale);
			}
		};
		compList.add(widthSpinner);
		
		strikeAndDip = new JLabel("                            ");
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		super.setMapElement(mapElement);
		if (this.mapElement != null)
			((Plane)this.mapElement).setPlanePanel(null);
		plane = (Plane) mapElement;
		plane.setPlanePanel(this);
		lengthSpinner.setValue(plane.getLengthScale());
		widthSpinner.setValue(plane.getWidthScale());
		triangleCheckBox.setSelected(plane.isTriangleVisible());
		updateLocation(mapElement);
		updateStrikeAndDip(plane.getStrike(), plane.getDip());
	}

	@Override
	public void updateLocation(MapElement mapElement) {
		setLocation(p0Location, p0Label, plane.getPoint(0));
		setLocation(p1Location, p1Label, plane.getPoint(1));
		setLocation(p2Location, p2Label, plane.getPoint(2));
//		System.err.println("PlanePanel.updateLocation "+plane.getStrike()+" "+plane.getDip());
//		String str = "Strike: ";
//		if (Plane.strikeAsCompassBearing) {
//			str += StringUtil.azimuthToCompassBearing(plane.getStrike());
//		} else {
//			str += StringUtil.format(plane.getStrike()) + StringUtil.DEGREE;
//		}
//		str += "   Dip: " + StringUtil.format(plane.getDip()) + StringUtil.DEGREE;
//		strikeAndDip.setText(str);
	}

	/**
	 * Method to update strike and dip values. This must be called after values are calculated
	 * in plane.
	 * @param strike
	 * @param dip
	 */
	public void updateStrikeAndDip(double strike, double dip) {
		String str = "Strike: ";
		if (Plane.strikeAsCompassBearing) {
			str += StringUtil.azimuthToCompassBearing(strike);
		} else {
			str += StringUtil.format(strike) + StringUtil.DEGREE;
		}
		str += "   Dip: " + StringUtil.format(dip) + StringUtil.DEGREE;
		strikeAndDip.setText(str);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		if (mapElement != null)
			((Plane)mapElement).setPlanePanel(null);
		if (p0Location != null)
			CoordAction.listenerList.remove(p0Location);
		if (p1Location != null)
			CoordAction.listenerList.remove(p1Location);
		if (p2Location != null)
			CoordAction.listenerList.remove(p2Location);
	}

}
