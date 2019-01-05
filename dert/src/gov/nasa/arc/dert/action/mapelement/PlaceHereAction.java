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

package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.action.UndoHandler;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.landmark.Landmark;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scene.tool.Profile;
import gov.nasa.arc.dert.scene.tool.Tool;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.view.world.MoveEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JOptionPane;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Spatial;

/**
 * Context menu item for placing a map element at a point in the landscape. User
 * is prompted with a list of map elements.
 *
 */
public class PlaceHereAction extends MenuItemAction {

	private Vector3 position;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public PlaceHereAction(ReadOnlyVector3 position) {
		super("Place Here ...");
		this.position = new Vector3(position);
	}

	@Override
	protected void run() {
		// Get a list of movable map elements
		List<Spatial> landmarks = World.getInstance().getLandmarks().getChildren();
		List<Spatial> tools = World.getInstance().getTools().getChildren();
		ArrayList<Spatial> list = new ArrayList<Spatial>();
		for (int i = 0; i < landmarks.size(); ++i) {
			list.add(landmarks.get(i));
		}
		for (int i = 0; i < tools.size(); ++i) {
			Spatial spat = tools.get(i);
			if (!((spat instanceof Path) || (spat instanceof Profile))) {
				list.add(spat);
			}
		}
		if (list.size() == 0) {
			return;
		}

		// Sort the list alphabetically
		Collections.sort(list, new Comparator<Spatial>() {
			@Override
			public int compare(Spatial me1, Spatial me2) {
				return (me1.getName().compareTo(me2.getName()));
			}

			@Override
			public boolean equals(Object obj) {
				return (this == obj);
			}
		});
		Spatial[] spatials = new Spatial[list.size()];
		list.toArray(spatials);

		// ask user to select one
		Movable movable = (Movable) JOptionPane.showInputDialog(Dert.getMainWindow(), "Select a Map Element",
			"Place Here", JOptionPane.PLAIN_MESSAGE, null, spatials, spatials[0]);

		// move the map element and hand it to the undo handler
		if (movable != null) {
			Vector3 trans = new Vector3(movable.getTranslation());
			movable.setLocation(position, false);
			movable.updateGeometricState(0);
			if (movable instanceof Landmark)
				((Landmark)movable).update(Dert.getWorldView().getViewpoint().getCamera());
			else if (movable instanceof Tool)
				((Tool)movable).update(Dert.getWorldView().getViewpoint().getCamera());
			UndoHandler.getInstance().addEdit(new MoveEdit(movable, trans));
		}
	}

}
