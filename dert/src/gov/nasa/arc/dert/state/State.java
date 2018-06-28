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

package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a serialized object for persisting the state of DERT components.
 * Components are reconstituted from these objects.
 *
 */
public class State {

	// Types of State object
	public static enum StateType {
		Console, World, Panel, MapElement, Actor
	}

	// State name
	public String name;

	// State type
	public StateType type;

	// Data for associated view (if any)
	protected ViewData viewData;

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param type
	 * @param viewData
	 */
	public State(String name, StateType type, ViewData viewData) {
		this.name = name;
		this.type = type;
		this.viewData = viewData;
	}
	
	public State(Map<String,Object> map) {
		name = StateUtil.getString(map, "Name", null);
		if (name == null)
			throw new NullPointerException("State has no name.");
		String str = StateUtil.getString(map, "Type", null);
		if (str == null)
			throw new NullPointerException("State has no type.");
		type = StateType.valueOf(str);
		viewData = ViewData.fromArray((int[])map.get("ViewData"));
	}

	/**
	 * Save contents (called before Configuration is closed)
	 */
	public Map<String,Object> save() {
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("Name", name);
		map.put("Type", type.toString());
		if (viewData != null) {
			viewData.save();
			map.put("ViewData", viewData.toArray());
		}
		return(map);
	}
	
	public boolean isEqualTo(State that) {
		if (!this.name.equals(that.name)) 
			return(false);
		if (this.type != that.type)
			return(false);
		// the same viewdata objects or both are null
		if (this.viewData == that.viewData)
			return(true);
		// this view data is null but the other isn't
		if (this.viewData == null)
			return(false);
		// the other view data is null but this one isn't
		if (that.viewData == null)
			return(false);
		// see if the view datas are equal
		return(this.viewData.isEqualTo(that.viewData));
	}

	/**
	 * Dispose of resources
	 */
	public void dispose() {
		// nothing here
	}

	/**
	 * Set the view associated with this state object
	 * 
	 * @param view
	 */
	public void setView(View view) {
		if (viewData != null) {
			viewData.setView(view);
		}
	}

	@Override
	public String toString() {
		return (" Name="+name+" Type="+type+" ViewData="+viewData);
	}

	/**
	 * Set the name for this state object
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the ViewData object.
	 * @return
	 */
	public ViewData getViewData() {
		return(viewData);
	}
	
	public void setViewData(ViewData viewData) {
		this.viewData = viewData;
	}
	
	public View open(boolean doIt) {
		return(null);
	}

}
