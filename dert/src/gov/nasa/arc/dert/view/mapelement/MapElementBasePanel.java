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
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.ui.CoordTextField;
import gov.nasa.arc.dert.ui.FieldPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Spatial;

/**
 * Provides an abstract base class for all map element panels.
 *
 */
public abstract class MapElementBasePanel extends JPanel {
	
//	protected static ImageIcon locked = Icons.getImageIcon("locked.png");
	
	// Common controls
	protected JPanel topPanel;
	protected JLabel locLabel;
	protected CoordTextField locationText;

	// Helpers
	protected NumberFormat formatter;
	protected Vector3 coord;

	// MapElement being edited
	protected MapElement mapElement;
	protected MapElement parentElement;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public MapElementBasePanel(MapElement mapElement) {
		setLayout(new BorderLayout());
		coord = new Vector3();
		formatter = new DecimalFormat(Landscape.format);
		this.parentElement = mapElement;
		this.mapElement = mapElement;
		build();
	}

	protected void build() {
		ArrayList<Component> compList = new ArrayList<Component>();
		
		addFields(compList);
		
		add(new FieldPanel(compList), BorderLayout.CENTER);
	}
	
	protected void addFields(ArrayList<Component> compList) {
		locLabel = new JLabel("Location", SwingConstants.RIGHT);
		compList.add(locLabel);
		locationText = new CoordTextField(22, "location of map element", Landscape.format, true) {
			@Override
			public void handleChange(Vector3 store) {
				if (mapElement instanceof Path)
					return;
				super.handleChange(store);
			}
			@Override
			public void doChange(ReadOnlyVector3 result) {
				Movable movable = (Movable)mapElement;
				double z = Landscape.getInstance().getZ(result.getX(), result.getY());
				if (Double.isNaN(z)) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				if (Double.isNaN(result.getZ())) {
					movable.setLocation(result.getX(), result.getY(), z, true);
				}
				else {
					movable.setZOffset(result.getZ()-z, false);
					movable.setLocation(result.getX(), result.getY(), z, true);
				}
			}
		};
		CoordAction.listenerList.add(locationText);
		compList.add(locationText);
	}

	protected void setLocation(CoordTextField locationText, JLabel label, ReadOnlyVector3 position) {
		if (position == null) {
			position = World.getInstance().getMarble().getTranslation();
		}
		if (locationText != null) {
			locationText.setLocalValue(position);
//			if (mapElement.isLocked())
//				label.setIcon(locked);
//			else
//				label.setIcon(null);
		}
	}

	/**
	 * Map element was moved
	 * 
	 * @param mapElement
	 */
	public void updateLocation(MapElement mapElement) {
		if (mapElement instanceof Path) {
			return;
		}
		if (locationText != null) {
			setLocation(locationText, locLabel, ((Spatial) mapElement).getTranslation());
		}
	}

	/**
	 * Map element was renamed
	 * 
	 * @param mapElement
	 */
	public void updateData(MapElement mapElement) {
	}

	/**
	 * Set the map element to be viewed or edited
	 * 
	 * @param mapElement
	 */
	public void setMapElement(MapElement mapElement) {
		this.mapElement = mapElement;
	}

	public void dispose() {
		if (locationText != null)
			CoordAction.listenerList.remove(locationText);
	}
	
	public void update() {
		updateLocation(mapElement);
	}
}
