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

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.tool.CartesianGrid;
import gov.nasa.arc.dert.scene.tool.Grid;
import gov.nasa.arc.dert.scene.tool.RadialGrid;
import gov.nasa.arc.dert.util.StateUtil;

import java.awt.Color;
import java.util.Map;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides a state object for a CartesianGrid or RadialGrid.
 *
 */
public class GridState extends ToolState {

	// Number of rings for radial grid
	public int rings;

	// Draw compass rose on radial grid
	public boolean compassRose;

	// Number of columns and rows in Cartesian grid
	public int columns, rows;
	
	// Location of center of grid
	public Vector3 location;
	
	public float lineWidth;

	/**
	 * Create a CartesianGrid state
	 * 
	 * @param position
	 * @return
	 */
	public static GridState createCartesianGridState(ReadOnlyVector3 position) {
		GridState state = new GridState(MapElementState.Type.CartesianGrid, "CartesianGrid", Landscape.defaultCellSize,
			CartesianGrid.defaultColor, CartesianGrid.defaultLabelVisible, position,
			CartesianGrid.defaultLineWidth);
		state.columns = CartesianGrid.defaultColumns;
		state.rows = CartesianGrid.defaultRows;
		return (state);
	}

	/**
	 * Create a RadialGrid state
	 * 
	 * @param position
	 * @return
	 */
	public static GridState createRadialGridState(ReadOnlyVector3 position) {
		GridState state = new GridState(MapElementState.Type.RadialGrid, "RadialGrid", Landscape.defaultCellSize,
			RadialGrid.defaultColor, RadialGrid.defaultLabelVisible, position,
			RadialGrid.defaultLineWidth);
		state.rings = RadialGrid.defaultRings;
		state.compassRose = RadialGrid.defaultCompassRose;
		return (state);
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof GridState))
			return(false);
		GridState that = (GridState)state;
		if (!super.isEqualTo(that))
			return(false);
		if (this.rings != that.rings) 
			return(false);
		if (this.columns != that.columns) 
			return(false);
		if (this.rows != that.rows) 
			return(false);
		if (this.compassRose != that.compassRose) 
			return(false);
		if (this.lineWidth != that.lineWidth) 
			return(false);
		if (!this.location.equals(that.location)) 
			return(false);
		return(true);
	}

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param prefix
	 * @param size
	 * @param color
	 * @param labelVisible
	 * @param pinned
	 * @param position
	 */
	protected GridState(MapElementState.Type type, String prefix, double size, Color color, boolean labelVisible,
		ReadOnlyVector3 position, float lineWidth) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration().incrementMapElementCount(type), type,
			prefix, size, color, labelVisible);
		location = new Vector3(position);
		this.lineWidth = lineWidth;
	}
	
	/**
	 * Constructor for hash map.
	 */
	public GridState(Map<String,Object> map) {
		super(map);
		rings = StateUtil.getInteger(map, "Rings", 0);
		columns = StateUtil.getInteger(map, "Columns", 0);
		rows = StateUtil.getInteger(map, "Rows", 0);
		compassRose = StateUtil.getBoolean(map, "CompassRose", false);
		location = StateUtil.getVector3(map, "Location", Vector3.ZERO);
		lineWidth = (float)StateUtil.getDouble(map, "LineWidth", 1);
	}

	@Override
	public Map<String,Object> save() {
		Map<String,Object> map = super.save();
		if (mapElement != null) {
			if (mapElementType == MapElementState.Type.RadialGrid) {
				rings = ((RadialGrid) mapElement).getRings();
				compassRose = ((RadialGrid) mapElement).isCompassRose();
			} else {
				columns = ((CartesianGrid) mapElement).getColumns();
				rows = ((CartesianGrid) mapElement).getRows();
			}
			location = new Vector3(((Grid)mapElement).getLocation());
		}
		map.put("Rings", new Integer(rings));
		map.put("Columns", new Integer(columns));
		map.put("Rows", new Integer(rows));
		map.put("CompassRose", new Boolean(compassRose));
		map.put("LineWidth", new Double(lineWidth));
		StateUtil.putVector3(map, "Location", location);
		return(map);
	}
	
	@Override
	public String toString() {
		String str = "["+columns+","+rows+","+rings+","+compassRose+","+location+"] "+super.toString();
		return(str);
	}
}
