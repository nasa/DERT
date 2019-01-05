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

package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCameraInfoManager;
import gov.nasa.arc.dert.view.fieldcamera.FieldCameraView;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Provides controls for setting options for field cameras.
 *
 */
public class FieldCameraPanel extends MapElementBasePanel {

	// Controls
	private JCheckBox fovCheckBox, lineCheckBox, crosshairCheckBox;
	private JComboBox defCombo;

	// FieldCamera
	private FieldCamera fieldCamera;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public FieldCameraPanel(MapElement mapElement) {
		super(mapElement);
	}

	@Override
	protected void addFields(ArrayList<Component> compList) {
		super.addFields(compList);
		fieldCamera = (FieldCamera)mapElement;
		
		compList.add(new JLabel("Definition", SwingConstants.RIGHT));
		String[] names = FieldCameraInfoManager.getInstance().getFieldCameraNames();
		defCombo = new JComboBox(names);
		defCombo.setToolTipText("select camera definition file");
		defCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String fieldCameraDef = (String) defCombo.getSelectedItem();
				if (fieldCamera.setFieldCameraDefinition(FieldCameraInfoManager.getInstance().getFieldCameraInfo(fieldCameraDef))) {
					FieldCameraView view = (FieldCameraView)fieldCamera.getState().getViewData().getView();
					if (view != null)
						view.setRange(fieldCamera.getFieldCameraInfo());					
				}
			}
		});
		compList.add(defCombo);

		compList.add(new JLabel("Crosshair", SwingConstants.RIGHT));
		crosshairCheckBox = new JCheckBox("visible");
		crosshairCheckBox.setSelected(fieldCamera.getSyntheticCameraNode().isCrosshairVisible());
		crosshairCheckBox.setToolTipText("display crosshair");
		crosshairCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				fieldCamera.getSyntheticCameraNode().setCrosshairVisible(crosshairCheckBox.isSelected());
			}
		});
		compList.add(crosshairCheckBox);

		compList.add(new JLabel("Field of View", SwingConstants.RIGHT));
		fovCheckBox = new JCheckBox("visible");
		fovCheckBox.setToolTipText("display the field of view frustum");
		fovCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				fieldCamera.getSyntheticCameraNode().setFovVisible(fovCheckBox.isSelected());
			}
		});
		compList.add(fovCheckBox);

		compList.add(new JLabel("Site Line", SwingConstants.RIGHT));
		lineCheckBox = new JCheckBox("visible");
		lineCheckBox.setToolTipText("display a line to the lookat point");
		lineCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				fieldCamera.getSyntheticCameraNode().setSiteLineVisible(lineCheckBox.isSelected());
			}
		});
		compList.add(lineCheckBox);
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		super.setMapElement(mapElement);
		fieldCamera = (FieldCamera) mapElement;
		setLocation(locationText, locLabel, fieldCamera.getTranslation());
		defCombo.setSelectedItem(fieldCamera.getFieldCameraDefinition());
		fovCheckBox.setSelected(fieldCamera.getSyntheticCameraNode().isFovVisible());
		lineCheckBox.setSelected(fieldCamera.getSyntheticCameraNode().isSiteLineVisible());
		crosshairCheckBox.setSelected(fieldCamera.getSyntheticCameraNode().isCrosshairVisible());
	}

}
