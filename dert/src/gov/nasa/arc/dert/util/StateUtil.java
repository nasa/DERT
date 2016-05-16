package gov.nasa.arc.dert.util;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;

public class StateUtil {
	
	public static final String getString(HashMap<String,Object> map, String key, String defaultValue) {
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
	
	public static final int getInteger(HashMap<String,Object> map, String key, int defaultValue) {
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
	
	public static final double getDouble(HashMap<String,Object> map, String key, double defaultValue) {
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
	
	public static final boolean getBoolean(HashMap<String,Object> map, String key, boolean defaultValue) {
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
	
	public static final long getLong(HashMap<String,Object> map, String key, long defaultValue) {
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
	
	public static final Color getColor(HashMap<String,Object> map, String key, Color defaultValue) {
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
	
	public static final void putVector3(HashMap<String,Object> map, String key, ReadOnlyVector3 value) {
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
	
	public static final Vector3 getVector3(HashMap<String,Object> map, String key, ReadOnlyVector3 defaultValue) {
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
	
	public static final void putColorRGBA(HashMap<String,Object> map, String key, ReadOnlyColorRGBA value) {
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
	
	public static final ColorRGBA getColorRGBA(HashMap<String,Object> map, String key, ReadOnlyColorRGBA defaultValue) {
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
	
	public static final HashMap<String,Object> getFields(Object obj, HashMap<String,Object> map) {
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
