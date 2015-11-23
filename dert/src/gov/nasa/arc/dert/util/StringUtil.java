package gov.nasa.arc.dert.util;

import gov.nasa.arc.dert.landscape.Landscape;

import java.awt.Color;
import java.io.File;
import java.util.Properties;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides String and text helper methods.
 *
 */
public class StringUtil {

	public static final String DEGREE = "\u00B0";

	/**
	 * Format a double
	 * 
	 * @param val
	 * @return
	 */
	public static final String format(double val) {
		return (String.format(Landscape.stringFormat, val));
	}

	/**
	 * Format a Vector3.
	 * 
	 * @param vec
	 * @return
	 */
	public static final String format(ReadOnlyVector3 vec) {
		if (vec == null) {
			return ("");
		}
		return (String.format(Landscape.stringFormat, vec.getX()) + ","
			+ String.format(Landscape.stringFormat, vec.getY()) + "," + String.format(Landscape.stringFormat,
			vec.getZ()));
	}

	/**
	 * Convert a string to a Color.
	 * 
	 * @param str
	 * @param defaultColor
	 * @return
	 */
	public static final Color stringToColor(String str) {
		if ((str == null) || str.isEmpty()) {
			return (null);
		}
		int[] col = stringToIntArray(str);
		if (col.length < 3) {
			return (null);
		}
		if (col.length == 3) {
			return (new Color(col[0], col[1], col[2]));
		} else {
			return (new Color(col[0], col[1], col[2], col[3]));
		}
	}

	/**
	 * Convert a string to a ColorRGBA.
	 * 
	 * @param str
	 * @param defaultColor
	 * @return
	 */
	public static final ReadOnlyColorRGBA stringToColorRGBA(String str, ReadOnlyColorRGBA defaultColor) {
		if ((str == null) || str.isEmpty()) {
			if (defaultColor == null) {
				throw new NullPointerException();
			}
			return (defaultColor);
		}
		try {
			float[] col = stringToFloatArray(str);
			if (col.length < 3) {
				return (defaultColor);
			}
			if (col.length == 3) {
				return (new ColorRGBA(col[0], col[1], col[2], 1));
			} else {
				return (new ColorRGBA(col[0], col[1], col[2], col[3]));
			}
		} catch (Exception e) {
			if (defaultColor == null) {
				throw new IllegalArgumentException(e);
			}
			return (defaultColor);
		}
	}

	/**
	 * Convert a color to a string.
	 * 
	 * @param color
	 * @return
	 */
	public static final String colorToString(Color color) {
		return (color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha());
	}

	/**
	 * Create a label from a file path.
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getLabelFromFilePath(String filePath) {
		File file = new File(filePath);
		String label = file.getName();
		int index = label.lastIndexOf(".");
		if (index > 0) {
			label = label.substring(0, index);
		}
		return (label);
	}

	/**
	 * Get the file name (part after last separator) from a file path.
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getFileNameFromFilePath(String filePath) {
		File file = new File(filePath);
		return (file.getName());
	}

	/**
	 * Convert a double array to a comma separated value string
	 * 
	 * @param value
	 * @return
	 */
	public static String doubleArrayToString(double[] value) {
		if (value == null) {
			return ("null");
		}
		if (value.length == 0) {
			return ("");
		}
		StringBuilder sb = new StringBuilder();
		sb.append(value[0]);
		for (int i = 1; i < value.length; ++i) {
			sb.append("," + value[i]);
		}
		return (sb.toString());
	}

	/**
	 * Convert a string to a double array
	 * 
	 * @param str
	 * @return
	 */
	public static double[] stringToDoubleArray(String str) {
		if (str == null) {
			return (null);
		}
		String[] array = str.split(",");
		if (array.length == 0) {
			array = new String[] { str.trim() };
		}
		double[] nArray = new double[array.length];
		for (int i = 0; i < nArray.length; ++i) {
			nArray[i] = Double.parseDouble(array[i]);
		}
		return (nArray);
	}

	/**
	 * Convert a string to a float array
	 * 
	 * @param str
	 * @return
	 */
	public static float[] stringToFloatArray(String str) {
		if (str == null) {
			return (null);
		}
		String[] array = str.split(",");
		if (array.length == 0) {
			array = new String[] { str.trim() };
		}
		float[] nArray = new float[array.length];
		for (int i = 0; i < nArray.length; ++i) {
			nArray[i] = Float.parseFloat(array[i]);
		}
		return (nArray);
	}

	/**
	 * Convert a string to an int array
	 * 
	 * @param str
	 * @return
	 */
	public static int[] stringToIntArray(String str) {
		if (str == null) {
			return (null);
		}
		String[] array = str.split(",");
		if (array.length == 0) {
			array = new String[] { str.trim() };
		}
		int[] nArray = new int[array.length];
		for (int i = 0; i < nArray.length; ++i) {
			nArray[i] = Integer.parseInt(array[i]);
		}
		return (nArray);
	}

	/**
	 * Retrieve a boolean value with default
	 * 
	 * @param properties
	 * @param key
	 * @param def
	 * @return
	 */
	public static boolean getBooleanValue(Properties properties, String key, boolean def, boolean strict) {
		try {
			String str = properties.getProperty(key);
			if (str == null) {
				if (strict) {
					throw new NullPointerException();
				}
				return (def);
			}
			boolean val = Boolean.valueOf(str);
			return (val);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error reading property " + key + ".", e);
		}

	}

	/**
	 * Retrieve an integer value with default.
	 * 
	 * @param properties
	 * @param key
	 * @param posOnly
	 * @param def
	 * @return
	 */
	public static int getIntegerValue(Properties properties, String key, boolean posOnly, int def, boolean strict) {
		try {
			String str = properties.getProperty(key);
			if (str == null) {
				if (strict) {
					throw new NullPointerException();
				}
				return (def);
			}
			int val = Integer.valueOf(str);
			if (posOnly && (val <= 0)) {
				return (def);
			}
			return (val);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error reading property " + key + ".", e);
		}

	}

	/**
	 * Retrieve a long value with default.
	 * 
	 * @param properties
	 * @param key
	 * @param posOnly
	 * @param def
	 * @return
	 */
	public static long getLongValue(Properties properties, String key, boolean posOnly, long def, boolean strict) {
		try {
			String str = properties.getProperty(key);
			if (str == null) {
				if (strict) {
					throw new NullPointerException();
				}
				return (def);
			}
			long val = Long.valueOf(str);
			if (posOnly && (val <= 0)) {
				return (def);
			}
			return (val);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error reading property " + key + ".", e);
		}

	}

	/**
	 * Retrieve a double property with default.
	 * 
	 * @param properties
	 * @param key
	 * @param posOnly
	 * @param def
	 * @return
	 */
	public static double getDoubleValue(Properties properties, String key, boolean posOnly, double def, boolean strict) {
		try {
			String str = properties.getProperty(key);
			if (str == null) {
				if (strict) {
					throw new NullPointerException();
				}
				return (def);
			}
			double val = Double.valueOf(str);
			if (posOnly && (val <= 0)) {
				return (def);
			}
			return (val);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error reading property " + key + ".", e);
		}

	}

	/**
	 * Retrieve a String property
	 * 
	 * @param properties
	 * @param key
	 * @param def
	 * @return
	 */
	public static String getStringValue(Properties properties, String key, String def, boolean strict) {
		try {
			String str = properties.getProperty(key);
			if (str == null) {
				if (strict) {
					throw new NullPointerException();
				}
				return (def);
			}
			return (str);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error reading property " + key + ".", e);
		}

	}

	/**
	 * Retrieve a float array property
	 * 
	 * @param properties
	 * @param key
	 * @param def
	 * @return
	 */
	public static float[] getFloatArray(Properties properties, String key, float[] def, boolean strict) {
		try {
			String str = properties.getProperty(key);
			if (str == null) {
				if (strict) {
					throw new NullPointerException();
				}
				return (def);
			}
			float[] val = stringToFloatArray(str);
			return (val);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error reading property " + key + ".", e);
		}

	}

	/**
	 * Retrieve an integer array property
	 * 
	 * @param properties
	 * @param key
	 * @param def
	 * @return
	 */
	public static int[] getIntegerArray(Properties properties, String key, int[] def, boolean strict) {
		try {
			String str = properties.getProperty(key);
			if (str == null) {
				if (strict) {
					throw new NullPointerException();
				}
				return (def);
			}
			int[] val = stringToIntArray(str);
			return (val);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error reading property " + key + ".", e);
		}

	}

	/**
	 * Retrieve a double array property
	 * 
	 * @param properties
	 * @param key
	 * @param def
	 * @return
	 */
	public static double[] getDoubleArray(Properties properties, String key, double[] def, boolean strict) {
		try {
			String str = properties.getProperty(key);
			if (str == null) {
				if (strict) {
					throw new NullPointerException();
				}
				return (def);
			}
			double[] val = stringToDoubleArray(str);
			return (val);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error reading property " + key + ".", e);
		}

	}

	/**
	 * Retrieve a Color property
	 * 
	 * @param properties
	 * @param key
	 * @param defColor
	 * @return
	 */
	public static Color getColorValue(Properties properties, String key, Color defColor, boolean strict) {
		try {
			String str = properties.getProperty(key);
			if (str == null) {
				if (strict) {
					throw new NullPointerException();
				}
				return (defColor);
			}
			Color color = stringToColor(str);
			if (color == null) {
				return (defColor);
			}
			return (color);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error reading property " + key + ".", e);
		}
	}

	/**
	 * Find a string in an array of strings.
	 * 
	 * @param str
	 * @param list
	 * @param ignoreCase
	 * @return
	 */
	public static Object findString(String str, Object[] list, boolean ignoreCase) {
		if (str == null) {
			return (null);
		}
		if (ignoreCase) {
			for (int i = 0; i < list.length; ++i) {
				if (str.equalsIgnoreCase(list[i].toString())) {
					return (list[i]);
				}
			}
		} else {
			for (int i = 0; i < list.length; ++i) {
				if (str.equals(list[i].toString())) {
					return (list[i]);
				}
			}
		}
		return (null);
	}

	/**
	 * Convert azimuth to a compass bearing string.
	 * 
	 * @param azimuth
	 * @return
	 */
	public static String azimuthToCompassBearing(double azimuth) {
		String str = "";
		if (azimuth < 0) {
			azimuth += 360;
		}
		if (azimuth == 0) {
			str = "N" + String.format("%04.1f", azimuth);
		}
		if (azimuth < 90) {
			str = "N" + String.format("%04.1f", azimuth) + "E";
		} else if (azimuth == 90) {
			str = String.format("%04.1f", azimuth) + "E";
		} else if (azimuth < 180) {
			str = "S" + String.format("%04.1f", (180 - azimuth)) + "E";
		} else if (azimuth == 180) {
			str = "S" + String.format("%04.1f", azimuth);
		} else if (azimuth < 270) {
			str = "S" + String.format("%04.1f", (azimuth - 180)) + "W";
		} else if (azimuth == 270) {
			str = String.format("%04.1f", (azimuth - 180)) + "W";
		} else {
			str = "N" + String.format("%04.1f", (360 - azimuth)) + "W";
		}
		return (str);
	}

}
