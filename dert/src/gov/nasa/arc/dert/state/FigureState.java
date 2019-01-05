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

import gov.nasa.arc.dert.scene.landmark.Figure;
import gov.nasa.arc.dert.scenegraph.Shape;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.util.StateUtil;

import java.util.Map;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides a state object for the Figure.
 *
 */
public class FigureState extends LandmarkState {

	// Surface normal
	public Vector3 normal;

	// Orientation
	public double azimuth, tilt;

	// Type of shape
	public ShapeType shape;

	// Options
	public boolean showNormal, autoScale;

	/**
	 * Constructor
	 * 
	 * @param position
	 * @param normal
	 */
	public FigureState(ReadOnlyVector3 position, ReadOnlyVector3 normal, ShapeType shape) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration()
			.incrementMapElementCount(MapElementState.Type.Figure), MapElementState.Type.Figure, "Figure",
			Figure.defaultSize, Figure.defaultColor, Figure.defaultLabelVisible, position);
		this.normal = new Vector3(normal);
		azimuth = Figure.defaultAzimuth;
		tilt = Figure.defaultTilt;
		this.shape = shape;
		showNormal = Figure.defaultSurfaceNormalVisible;
		autoScale = Figure.defaultAutoScale && Shape.SCALABLE[shape.ordinal()];
	}
	
	/**
	 * Constructor for hash map.
	 */
	public FigureState(Map<String,Object> map) {
		super(map);
		normal = StateUtil.getVector3(map, "Normal", Vector3.ZERO);
		azimuth = StateUtil.getDouble(map, "Azimuth", Figure.defaultAzimuth);
		tilt = StateUtil.getDouble(map, "Tilt", Figure.defaultTilt);
		String str = StateUtil.getString(map, "Shape", Figure.defaultShapeType.toString());
		try {
			shape = ShapeType.valueOf(str);
		}
		catch (Exception e) {
			shape = Figure.defaultShapeType;
		}
		showNormal = StateUtil.getBoolean(map, "ShowNormal", Figure.defaultSurfaceNormalVisible);
		autoScale = StateUtil.getBoolean(map, "AutoScale", Figure.defaultAutoScale);
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof FigureState)) 
			return(false);
		FigureState that = (FigureState)state;
		if (!super.isEqualTo(that)) 
			return(false);
		if (!this.normal.equals(that.normal)) 
			return(false);
		if (this.azimuth != that.azimuth) 
			return(false);
		if (this.tilt != that.tilt)
			return(false);
		if (this.shape != that.shape) 
			return(false);
		if (this.showNormal != that.showNormal) 
			return(false);
		if (this.autoScale != that.autoScale) 
			return(false);
		return(true);
	}

	@Override
	public Map<String,Object> save() {
		Map<String,Object> map = super.save();
		if (mapElement != null) {
			Figure figure = (Figure) mapElement;
			normal = new Vector3(figure.getNormal());
			azimuth = figure.getAzimuth();
			tilt = figure.getTilt();
			shape = figure.getShapeType();
			showNormal = figure.isSurfaceNormalVisible();
			autoScale = figure.isAutoScale();
		}
		StateUtil.putVector3(map, "Normal", normal);
		map.put("Azimuth", new Double(azimuth));
		map.put("Tilt", new Double(tilt));
		map.put("Shape", shape.toString());
		map.put("ShowNormal", new Boolean(showNormal));
		map.put("AutoScale", new Boolean(autoScale));
		return(map);
	}
	
	@Override
	public String toString() {
		String str = super.toString();
		str = "["+azimuth+","+tilt+","+normal+","+shape+","+showNormal+","+autoScale+"] "+str;
		return(str);
	}
}
