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

package gov.nasa.arc.dert.landscape.layer;

import gov.nasa.arc.dert.util.ColorMap;
import gov.nasa.arc.dert.util.StateUtil;

import java.util.HashMap;

/**
 * Data structure for information about a landscape layer.
 *
 */
public class LayerInfo implements Comparable<LayerInfo> {

	public static enum LayerType {
		none, elevation, colorimage, grayimage, field, footprint, viewshed, derivative
	}

	// Name of the layer, presented in the UI
	public String name;

	// Layer type
	public LayerType type;

	// The percent that the layer contributes to the overall color of the
	// landscape
	public double opacity = 1;

	// The texture index for the layer
	public int layerNumber = -1;

	// The name of a color map used with this layer (for derivatives and fields only)
	public String colorMapName;

	// Flag to use a gradient with the color map
	public boolean gradient = false;

	// The minimum value of this layer
	public double minimum = Double.NaN;

	// The maximum value of this layer
	public double maximum = Double.NaN;
	
	// Auto blending enabled for this layer
	public boolean autoblend = false;
	
	// This layer, when added to the visible list, is showing.
	public int show = 1;

	// The color map object
	public transient ColorMap colorMap;

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param type
	 * @param colorMapName
	 * @param minimum
	 * @param maximum
	 * @param gradient
	 */
	public LayerInfo(String name, String type, String colorMapName, double minimum, double maximum, boolean gradient) {
		this(name, type, -1);
		this.colorMapName = colorMapName;
		this.minimum = minimum;
		this.maximum = maximum;
		this.gradient = gradient;
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param type
	 * @param blendFactor
	 * @param isOverlay
	 * @param layerNumber
	 */
	public LayerInfo(String name, String type, int layerNumber) {
		this.name = name;
		this.type = LayerType.valueOf(type);
		this.layerNumber = layerNumber;
		autoblend = !(this.type == LayerType.none);
	}
	
	/**
	 * Constructor from hash map.
	 */
	public LayerInfo(HashMap<String,Object> map) {
		name = StateUtil.getString(map, "Name", null);
		if (name == null)
			throw new NullPointerException("Name for LayerInfo is null.");
		String str = StateUtil.getString(map, "Type", "none");
		type = LayerType.valueOf(str);
		opacity = StateUtil.getDouble(map, "Opacity", opacity);
		layerNumber = StateUtil.getInteger(map, "LayerNumber", layerNumber);
		autoblend = StateUtil.getBoolean(map, "Autoblend", autoblend);
		show = StateUtil.getInteger(map, "Show", show);
		str = StateUtil.getString(map, "ColorMap.name", null);
		if (str != null) {
			colorMapName = str;
			gradient = StateUtil.getBoolean(map, "ColorMap.gradient", gradient);
			minimum = StateUtil.getDouble(map, "ColorMap.minimum", minimum);
			maximum = StateUtil.getDouble(map, "ColorMap.maximum", maximum);
		}
	}

	/**
	 * Constructor
	 * 
	 * @param that
	 */
	public LayerInfo(LayerInfo that) {
		this.name = that.name;
		this.type = that.type;
		this.opacity = that.opacity;
		this.layerNumber = that.layerNumber;
		this.gradient = that.gradient;
		this.colorMapName = that.colorMapName;
		this.minimum = that.minimum;
		this.maximum = that.maximum;
		this.colorMap = that.colorMap;
		this.autoblend = that.autoblend;
		this.show = that.show;
	}

	/**
	 * Compare two LayerInfo objects
	 */
	@Override
	public int compareTo(LayerInfo that) {
		return (this.name.compareTo(that.name));
	}

	@Override
	public String toString() {
		String str = name;
		if ((type == LayerType.footprint) || (type == LayerType.viewshed)) {
			str += " " + type;
		}
		return (str);
	}

	/**
	 * Prepare this LayerInfo object to be persisted.
	 */
	public HashMap<String,Object> getAsHashMap() {
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("Name", name);
		map.put("Type", type.toString());
		map.put("Opacity", new Double(opacity));
		map.put("LayerNumber", new Integer(layerNumber));
		map.put("Autoblend", new Boolean(autoblend));
		map.put("Show", new Integer(show));
		if (colorMap != null) {
			map.put("ColorMap.name", colorMap.getName());
			map.put("ColorMap.gradient", new Boolean(colorMap.isGradient()));
			map.put("ColorMap.minimum", new Double(colorMap.getMinimum()));
			map.put("ColorMap.maximum", new Double(colorMap.getMaximum()));
		}
		else if (colorMapName != null) {
			map.put("ColorMap.name", colorMapName);
			map.put("ColorMap.gradient", new Boolean(gradient));
			map.put("ColorMap.minimum", new Double(minimum));
			map.put("ColorMap.maximum", new Double(maximum));
		}
		return(map);
	}

}
