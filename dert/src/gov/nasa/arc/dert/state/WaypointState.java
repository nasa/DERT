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

package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.tool.Waypoint;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.mapelement.EditDialog;

import java.awt.Color;
import java.util.Map;

import javax.swing.Icon;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides a state object for a Waypoint.
 *
 */
public class WaypointState extends MapElementState {

	public static final Icon icon = Icons.getImageIcon("waypoint_24.png");

	// Waypoint location
	public Vector3 location;

	// State object for waypoint parent path
	public transient PathState parent;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param position
	 * @param prefix
	 * @param size
	 * @param color
	 * @param labelVisible
	 * @param pinned
	 */
	public WaypointState(int id, ReadOnlyVector3 position, String prefix, double size, Color color,
		boolean labelVisible, boolean locked) {
		super(id, MapElementState.Type.Waypoint, prefix, size, color, labelVisible);
		location = new Vector3(position);
		this.locked = locked;
	}
	
	/**
	 * Constructor from hash map.
	 */
	public WaypointState(Map<String,Object> map) {
		super(map);
		location = StateUtil.getVector3(map, "Location", null);
		if (location == null)
			throw new NullPointerException("Waypoint location is missing.");
	}
	
	@Override
	public boolean isEqualTo(State state) {
		WaypointState that = (WaypointState)state;
		if (!super.isEqualTo(that))
			return(false);
		return(this.location.equals(that.location));
	}

	@Override
	public Map<String,Object> save() {
		Map<String,Object> map = super.save();
		if (mapElement != null) {
			Waypoint waypoint = (Waypoint) mapElement;
			location = new Vector3(waypoint.getLocation());
			parent = (PathState) waypoint.getPath().getState();
		}
		StateUtil.putVector3(map, "Location", location);
		// Waypoint is uses Path locked state.
		map.put("Locked", new Boolean(false));
		return(map);
	}

	/**
	 * Open the editor
	 */
	@Override
	public EditDialog openEditor() {
		if (mapElement == null)
			return(null);
		parent = (PathState) ((Waypoint)mapElement).getPath().getState();
		EditDialog ed = parent.getEditDialog();
		if (ed != null) {
			ed.open();
			ed.setMapElement(mapElement);
			ed.update();
		}
		return(ed);
	}

	/**
	 * Open the annotation
	 */
//	@Override
//	public NotesDialog openAnnotation() {
//		if (mapElement == null)
//			return(null);
//		parent = (PathState) ((Waypoint)mapElement).getPath().getState();
//		NotesDialog nd = parent.getAnnotationDialog();
//		if (nd != null) {
//			nd.open();
//			nd.setMapElement(mapElement);
//			nd.update();
//		}
//		return(nd);
//	}

//	@Override
//	public void setAnnotation(String note) {
//		if (note != null) {
//			annotation = note;
//		}
////		parent = (PathState) ((Waypoint)mapElement).getPath().getState();
////		parent.setMapElement(mapElement);
//	}

	
	@Override
	public String toString() {
		String str = "["+location+"]"+super.toString();
		return(str);
	}

}
