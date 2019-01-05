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

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.Marble;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.TextView;
import gov.nasa.arc.dert.view.View;

import java.awt.Color;
import java.util.Map;

import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * A state object for the green Marble.
 *
 */
public class MarbleState extends MapElementState {

	private transient Vector2 work;

	/**
	 * Constructor
	 */
	public MarbleState() {
		super(0, MapElementState.Type.Marble, "Marble");
		name = "Marble";
		viewData = new ViewData(400, 200, true);
		labelVisible = false;
		color = Color.green;
		size = 0.75;
	}
	
	/**
	 * Constructor for hash map.
	 */
	public MarbleState(Map<String,Object> map) {
		super(map);
	}

	@Override
	public void createView() {
		setView(new TextView(this, false));
//		viewData.createWindow(Dert.getMainWindow(), "DERT Marble Info", X_OFFSET, Y_OFFSET);
		viewData.createWindow(Dert.getMainWindow(), "DERT Marble Info");
		updateText();
	}

	/**
	 * Update the data displayed in the Marble view
	 */
	public void updateText() {
		Dert.getMainWindow().getToolPanel().updateMarbleLocationField();

		View view = viewData.getView();
		if (view == null) {
			return;
		}

		if (work == null) {
			work = new Vector2();
		}

		Marble marble = (Marble) mapElement;
		Vector3 loc = new Vector3(marble.getTranslation());
		Landscape.getInstance().localToWorldCoordinate(loc);
		String str = "Location (meters): " + StringUtil.format(loc) + "\n";
		Landscape.getInstance().worldToSphericalCoordinate(loc);
		str += "Longitude: " + StringUtil.format(loc.getX()) + StringUtil.DEGREE + "\n";
		str += "Latitude: " + StringUtil.format(loc.getY()) + StringUtil.DEGREE + "\n";
		ReadOnlyVector3 normal = marble.getNormal();
		str += "Surface Normal Vector: " + StringUtil.format(normal) + "\n";
		ReadOnlyVector3 dir = World.getInstance().getLighting().getLightDirection();
		str += "Solar Direction Vector: " + StringUtil.format(dir) + "\n";
		Vector3 angle = MathUtil.directionToAzEl(dir, null);
		str += "Solar Incidence Angle: "+StringUtil.format(90-Math.toDegrees(angle.getY())) + "\n";
		str += "Sub-solar Azimuth: "+StringUtil.format(Math.toDegrees(angle.getX())) + "\n";
		str += "Elevation (meters): " + StringUtil.format(loc.getZ()) + "\n";
		str += "Slope: " + StringUtil.format(MathUtil.getSlopeFromNormal(normal)) + StringUtil.DEGREE + "\n";
		str += "Aspect: " + StringUtil.format(MathUtil.getAspectFromNormal(normal)) + StringUtil.DEGREE + "\n";
		((TextView) view).setText(str);
	}

}
