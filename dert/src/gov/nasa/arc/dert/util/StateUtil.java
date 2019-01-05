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

package gov.nasa.arc.dert.util;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;

public class StateUtil {
	
	public static final String getString(Map<String,Object> map, String key, String defaultValue) {
		try {
			String obj = (String)map.get(key);
			if (obj == null)
				return(defaultValue);
			else
				return(obj);
		}
		catch (Exception e) {
			return(defaultValue);
		}
	}
	
	public static final int getInteger(Map<String,Object> map, String key, int defaultValue) {
		try {
			Integer obj = (Integer)map.get(key);
			if (obj == null)
				return(defaultValue);
			else
				return(obj);
		}
		catch (Exception e) {
			return(defaultValue);
		}
	}
	
	public static final double getDouble(Map<String,Object> map, String key, double defaultValue) {
		try {
			Double obj = (Double)map.get(key);
			if (obj == null)
				return(defaultValue);
			else
				return(obj);
		}
		catch (Exception e) {
			return(defaultValue);
		}
	}
	
	public static final boolean getBoolean(Map<String,Object> map, String key, boolean defaultValue) {
		try {
			Boolean obj = (Boolean)map.get(key);
			if (obj == null)
				return(defaultValue);
			else
				return(obj);
		}
		catch (Exception e) {
			return(defaultValue);
		}
	}
	
	public static final long getLong(Map<String,Object> map, String key, long defaultValue) {
		try {
			Long obj = (Long)map.get(key);
			if (obj == null)
				return(defaultValue);
			else
				return(obj);
		}
		catch (Exception e) {
			return(defaultValue);
		}
	}
	
	public static final Color getColor(Map<String,Object> map, String key, Color defaultValue) {
		try {
			Color obj = (Color)map.get(key);
			if (obj == null)
				return(defaultValue);
			else
				return(obj);
		}
		catch (Exception e) {
			return(defaultValue);
		}
	}
	
	public static final void putVector3(Map<String,Object> map, String key, ReadOnlyVector3 value) {
		try {
			if (value == null)
				map.put(key, null);
			else {
				double[] array = new double[3];
				value.toArray(array);
				map.put(key, array);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static final Vector3 getVector3(Map<String,Object> map, String key, ReadOnlyVector3 defaultValue) {
		try {
			double[] obj = (double[])map.get(key);
			if (obj == null) {
				if (defaultValue == null)
					return(null);
				return(new Vector3(defaultValue));
			}
			else
				return(new Vector3(obj[0], obj[1], obj[2]));
		}
		catch (Exception e) {
			if (defaultValue == null)
				return(null);
			return(new Vector3(defaultValue));
		}
	}
	
	public static final void putColorRGBA(Map<String,Object> map, String key, ReadOnlyColorRGBA value) {
		try {
			if (value == null)
				map.put(key, null);
			else {
				float[] array = new float[4];
				value.toArray(array);
				map.put(key, array);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static final ColorRGBA getColorRGBA(Map<String,Object> map, String key, ReadOnlyColorRGBA defaultValue) {
		try {
			float[] obj = (float[])map.get(key);
			if (obj == null) {
				if (defaultValue == null)
					return(null);
				return(new ColorRGBA(defaultValue));
			}
			else
				return(new ColorRGBA(obj[0], obj[1], obj[2], obj[3]));
		}
		catch (Exception e) {
			if (defaultValue == null)
				return(null);
			return(new ColorRGBA(defaultValue));
		}
	}
	
	public static final Map<String,Object> getFields(Object obj, Map<String,Object> map) {
		if (map == null)
			map = new HashMap<String,Object>();
		try {
			// get the record class object and a list of its methods
			Class<?> cl = obj.getClass();
			Field[] field = cl.getDeclaredFields();
			
			// look at each field
			for (int i = 0; i < field.length; ++i) {
				// skip transient fields
				if ((field[i].getModifiers() & Modifier.TRANSIENT) != 0)
					continue;
				String name = field[i].getName();	
				String simpleName = field[i].getType().getSimpleName();
				if (simpleName.endsWith("Vector3"))
					putVector3(map, name, (Vector3)field[i].get(obj));
				else if (simpleName.endsWith("ColorRGBA"))
					putColorRGBA(map, name, (ColorRGBA)field[i].get(obj));
				else
					map.put(name, field[i].get(obj));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return(map);
	}

}
