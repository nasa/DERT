package gov.nasa.arc.dert.raster.pds;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.stream.ImageInputStream;

/**
 * Provide a parser for PDS label files.
 *
 */
public class LabelParser {

	public static class KeyValue {
		public String key;
		public String value;

		public KeyValue(String key, String value) {
			this.key = key;
			this.value = value;
		}
	}

	public static enum PDSType {
		PDS_Integer, PDS_Float, PDS_Double, PDS_String, PDS_Long, PDS_Integer_Array, PDS_Double_Array, PDS_String_Array, PDS_Object, PDS_Group
	}

	protected static HashMap<String, PDSType> tagMap;

	static {
		tagMap = new HashMap<String, PDSType>();
		tagMap.put("PDS_VERSION_ID", PDSType.PDS_String);
		tagMap.put("TARGET_NAME", PDSType.PDS_String);
		tagMap.put("RECORD_TYPE", PDSType.PDS_String);
		tagMap.put("RECORD_BYTES", PDSType.PDS_Long);
		tagMap.put("FILE_RECORDS", PDSType.PDS_Integer);
		tagMap.put("^IMAGE", PDSType.PDS_String);
		tagMap.put("LINES", PDSType.PDS_Integer);
		tagMap.put("LINE_SAMPLES", PDSType.PDS_Integer);
		tagMap.put("BANDS", PDSType.PDS_Integer);
		tagMap.put("OFFSET", PDSType.PDS_Float);
		tagMap.put("SCALING_FACTOR", PDSType.PDS_Float);
		tagMap.put("SAMPLE_BITS", PDSType.PDS_Integer);
		tagMap.put("SAMPLE_BIT_MASK", PDSType.PDS_Integer);
		tagMap.put("SAMPLE_TYPE", PDSType.PDS_String);
		tagMap.put("MISSING_CONSTANT", PDSType.PDS_Float);
		tagMap.put("CORE_NULL", PDSType.PDS_Float);
		tagMap.put("VALID_MINIMUM", PDSType.PDS_Float);
		tagMap.put("VALID_MAXIMUM", PDSType.PDS_Float);
		tagMap.put("MINIMUM", PDSType.PDS_Float);
		tagMap.put("MAXIMUM", PDSType.PDS_Float);
		tagMap.put("A_AXIS_RADIUS", PDSType.PDS_Double);
		tagMap.put("B_AXIS_RADIUS", PDSType.PDS_Double);
		tagMap.put("C_AXIS_RADIUS", PDSType.PDS_Double);
		tagMap.put("CENTER_LATITUDE", PDSType.PDS_Double);
		tagMap.put("CENTER_LONGITUDE", PDSType.PDS_Double);
		tagMap.put("LINE_FIRST_PIXEL", PDSType.PDS_Integer);
		tagMap.put("LINE_LAST_PIXEL", PDSType.PDS_Integer);
		tagMap.put("SAMPLE_FIRST_PIXEL", PDSType.PDS_Integer);
		tagMap.put("SAMPLE_LAST_PIXEL", PDSType.PDS_Integer);
		tagMap.put("MAP_PROJECTION_ROTATION", PDSType.PDS_Double);
		tagMap.put("MAP_RESOLUTION", PDSType.PDS_Double);
		tagMap.put("MAP_SCALE", PDSType.PDS_Double);
		tagMap.put("MAXIMUM_LATITUDE", PDSType.PDS_Double);
		tagMap.put("MINIMUM_LATITUDE", PDSType.PDS_Double);
		tagMap.put("LINE_PROJECTION_OFFSET", PDSType.PDS_Double);
		tagMap.put("SAMPLE_PROJECTION_OFFSET", PDSType.PDS_Double);
		tagMap.put("EASTERNMOST_LONGITUDE", PDSType.PDS_Double);
		tagMap.put("WESTERNMOST_LONGITUDE", PDSType.PDS_Double);
		tagMap.put("NORTH_AZIMUTH", PDSType.PDS_Double);
		tagMap.put("CORE_LOW_INSTR_SATURATION", PDSType.PDS_Float);
		tagMap.put("CORE_HIGH_INSTR_SATURATION", PDSType.PDS_Float);
		tagMap.put("MRO:MINIMUM_STRETCH", PDSType.PDS_Integer_Array);
		tagMap.put("MRO:MAXIMUM_STRETCH", PDSType.PDS_Integer_Array);
		tagMap.put("MEX:DTM_MISSING_DN", PDSType.PDS_Float);
		tagMap.put("ROVER_MOTION_COUNTER", PDSType.PDS_Integer_Array);
		tagMap.put("ROVER_MOTION_COUNTER_NAME", PDSType.PDS_String_Array);
		tagMap.put("UNCOMPRESSED_FILE", PDSType.PDS_Object);
		tagMap.put("IMAGE", PDSType.PDS_Object);
		tagMap.put("IMAGE_MAP_PROJECTION", PDSType.PDS_Object);
		tagMap.put("VIEWING_PARAMETERS", PDSType.PDS_Object);
		tagMap.put("MEX:DTM", PDSType.PDS_Group);
	}

	protected String lineSeparator = System.getProperty("line.separator");

	/**
	 * Constructor
	 */
	public LabelParser() {
	}

	/**
	 * Given an ImageInputStream positioned at the beginning of the file read
	 * the label. Metadata from the label are organized as key/value pairs in a
	 * HashMap. Hierarchical objects are flattened by placing periods in between
	 * key segments.
	 * 
	 * @param iStream
	 *            the stream
	 * @return the label as key/value pairs
	 * @throws IOException
	 */
	public HashMap<String, Object> parseHeader(ImageInputStream iStream) throws IOException {
		ArrayList<KeyValue> labelMap = parseObject(iStream);
		return (mapMetadata(labelMap));
	}

	/**
	 * Produce an ArrayList of Key/Value pairs from an ImageInputStream
	 * 
	 * @param reader
	 *            the stream
	 * @return the array list
	 * @throws IOException
	 */
	protected ArrayList<KeyValue> parseObject(ImageInputStream reader) throws IOException {
		ArrayList<KeyValue> result = new ArrayList<KeyValue>();
		String str = null;
		String value = null;
		String key = null;
		while ((str = reader.readLine()) != null) {
			str = str.trim();
			if (str.startsWith("/*")) {
				continue;
			}
			if (str.equals("END")) {
				break;
			} else if (str.startsWith("OBJECT")) {
				int n = str.indexOf(" = ") + 3;
				String name = str.substring(n);
				ArrayList<KeyValue> list = parseObject(reader);
				for (int i = 0; i < list.size(); ++i) {
					result.add(new KeyValue(name + "." + list.get(i).key, list.get(i).value));
				}
			} else if (str.startsWith("END_OBJECT")) {
				break;
			} else if (str.startsWith("GROUP")) {
				int n = str.indexOf(" = ") + 3;
				String name = str.substring(n);
				ArrayList<KeyValue> list = parseObject(reader);
				for (int i = 0; i < list.size(); ++i) {
					result.add(new KeyValue(name + "." + list.get(i).key, list.get(i).value));
				}
			} else if (str.startsWith("END_GROUP")) {
				break;
			} else {
				int n = str.indexOf(" = ");
				// continuation
				if (n < 0) {
					value += str.trim();
				} else {
					key = str.substring(0, n).trim();
					value = str.substring(n + 3).trim();
					result.add(new KeyValue(key, value));
				}
			}
		}
		return (result);
	}

	protected final String getFirstKeySegment(String key) {
		int p = key.indexOf(".");
		return (key.substring(0, p));
	}

	protected final ArrayList<KeyValue> getKeyValueList(String name, ArrayList<KeyValue> parentList, int index) {
		ArrayList<KeyValue> childList = new ArrayList<KeyValue>();
		KeyValue kv = parentList.get(index);
		while (kv.key.startsWith(name)) {
			kv.key = kv.key.substring(name.length() + 1);
			parentList.remove(kv);
			childList.add(kv);
			kv = parentList.get(index);
		}
		return (childList);
	}

	public long writeObject(BufferedWriter writer, ArrayList<KeyValue> kvList, String indent) throws IOException {
		long size = 0;
		for (int i = 0; i < kvList.size(); ++i) {
			KeyValue kv = kvList.get(i);
			if (kv.key.equals("") && (kv.value instanceof String)) {
				String str = indent + kv.value.toString() + lineSeparator;
				writer.write(str);
				size += str.length();
			} else if (kv.key.contains(".")) {
				String name = getFirstKeySegment(kv.key);
				PDSType type = tagMap.get(name);
				String str = null;
				if (type == PDSType.PDS_Object) {
					str = indent + "OBJECT = " + name;
				} else if (type == PDSType.PDS_Group) {
					str = indent + "GROUP = " + name;
				} else {
					throw new IllegalArgumentException("Unknown PDS type " + name);
				}
				str += lineSeparator;
				writer.write(str);
				size += str.length();
				size += writeObject(writer, getKeyValueList(name, kvList, i), indent + "  ");
				if (type == PDSType.PDS_Object) {
					str = indent + "END_OBJECT = " + kv.key;
				} else {
					str = indent + "END_GROUP = " + kv.key;
				}
				str += lineSeparator;
				writer.write(str);
				size += str.length();
			} else if (kv.value instanceof String) {
				String str = indent + kv.key + " = " + kv.value.toString() + lineSeparator;
				writer.write(str);
				size += str.length();
			}
		}
		String str = "END" + lineSeparator;
		writer.write(str);
		size += str.length();
		return (size);
	}

	/**
	 * Convert strings to objects according to the value type listed above.
	 * 
	 * @param labelList
	 *            the array list of key value pairs
	 * @return a hashmap of object values
	 */
	protected HashMap<String, Object> mapMetadata(ArrayList<KeyValue> labelList) {
		HashMap<String, Object> metadata = new HashMap<String, Object>();
		for (int i = 0; i < labelList.size(); ++i) {
			String objName = labelList.get(i).key;
			int p = objName.lastIndexOf(".") + 1;
			String tagName = objName.substring(p);
			String tagValue = labelList.get(i).value;
			PDSType type = tagMap.get(tagName);
			if (tagValue.startsWith("\"") || (type == null)) {
				type = PDSType.PDS_String;
			}
			try {
				switch (type) {
				case PDS_Object:
				case PDS_Group:
					break;
				case PDS_String:
					tagValue = tagValue.replace('"', ' ').trim();
					metadata.put(objName, tagValue);
					break;
				case PDS_Double:
					metadata.put(objName, new Double(getDouble(tagValue)));
					break;
				case PDS_Float:
					metadata.put(objName, new Float(getFloat(tagValue)));
					break;
				case PDS_Integer:
					metadata.put(objName, new Integer(getInteger(tagValue)));
					break;
				case PDS_Long:
					metadata.put(objName, new Long(getLong(tagValue)));
					break;
				case PDS_Integer_Array:
					metadata.put(objName, getIntArray(tagValue));
					break;
				case PDS_Double_Array:
					metadata.put(objName, getDoubleArray(tagValue));
					break;
				case PDS_String_Array:
					metadata.put(objName, getStringArray(tagValue));
					break;
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Unable to parse value " + tagValue + " for " + objName + ".", e);
			}
		}
		return (metadata);
	}

	/**
	 * Get a value designated as binary as an int.
	 * 
	 * @param str
	 *            the number
	 * @return the int
	 */
	protected final int getBinaryAsInteger(String str) {
		return ((int) getBinaryAsLong(str));
	}

	/**
	 * Get a value designated as hex as an int.
	 * 
	 * @param str
	 *            the number
	 * @return the int
	 */
	protected final int getHexAsInteger(String str) {
		// remove 16# and # at end of string
		str = str.substring(3, str.length() - 1);
		// parse binary string
		return (Integer.parseInt(str, 16));
	}

	/**
	 * Get a value designated as binary as a long.
	 * 
	 * @param str
	 *            the number
	 * @return the long
	 */
	protected final long getBinaryAsLong(String str) {
		// remove 2# and # at end of string
		str = str.substring(2, str.length() - 1);
		// parse binary string
		return (Long.parseLong(str, 2));
	}

	/**
	 * Get a value designated as hex as an long.
	 * 
	 * @param str
	 *            the number
	 * @return the long
	 */
	protected final long getHexAsLong(String str) {
		// remove 16# and # at end of string
		str = str.substring(3, str.length() - 1);
		// parse binary string
		return (Long.parseLong(str, 16));
	}

	/**
	 * Get an integer value from a string.
	 * 
	 * @param str
	 *            the string
	 * @return the integer
	 */
	protected final int getInteger(String str) {
		int val = 0;
		int i = str.lastIndexOf("<");
		if (i >= 0) {
			str = str.substring(0, i).trim();
		}
		if (str.startsWith("2#")) {
			val = getBinaryAsInteger(str);
		} else if (str.startsWith("16#")) {
			val = getHexAsInteger(str);
		} else {
			val = Integer.parseInt(str);
		}
		return (val);
	}

	/**
	 * Get a long value from a string.
	 * 
	 * @param str
	 *            the string
	 * @return the long
	 */
	public final long getLong(String str) {
		long val = 0;
		int i = str.lastIndexOf("<");
		if (i >= 0) {
			str = str.substring(0, i).trim();
		}
		if (str.startsWith("2#")) {
			val = getBinaryAsLong(str);
		} else if (str.startsWith("16#")) {
			val = getHexAsLong(str);
		} else {
			val = Long.parseLong(str);
		}
		return (val);
	}

	/**
	 * Get a float value from a string.
	 * 
	 * @param str
	 *            the string
	 * @return the float
	 */
	protected final float getFloat(String str) {
		float val = 0;
		float units = 1;
		int i = str.lastIndexOf("<");
		if (i >= 0) {
			String unitStr = str.substring(i);
			str = str.substring(0, i);
			units = (float) getUnitFactor(unitStr);
		}
		if (str.startsWith("16#")) {
			long lval = getHexAsLong(str);
			val = Float.intBitsToFloat((int) lval);
		} else {
			val = Float.parseFloat(str);
		}
		val *= units;
		return (val);
	}

	/**
	 * Get a double value from a string.
	 * 
	 * @param str
	 *            the string
	 * @return the double
	 */
	protected final double getDouble(String str) {
		double val = 0;
		double units = 1;
		int i = str.lastIndexOf("<");
		if (i >= 0) {
			String unitStr = str.substring(i);
			str = str.substring(0, i);
			units = getUnitFactor(unitStr);
		}
		if (str.startsWith("16#")) {
			long lval = getHexAsLong(str);
			val = Double.longBitsToDouble(lval);
		} else {
			val = Double.parseDouble(str);
		}
		val *= units;
		return (val);
	}

	/**
	 * Get an int array from a string.
	 * 
	 * @param str
	 *            the string
	 * @return the int array
	 */
	protected final int[] getIntArray(String str) {
		String[] token = getArrayTokens(str);
		int[] iArray = new int[token.length];
		for (int i = 0; i < token.length; ++i) {
			iArray[i] = Integer.parseInt(token[i].trim());
		}
		return (iArray);
	}

	/**
	 * Get a double array from a string.
	 * 
	 * @param str
	 *            the string
	 * @return the double array
	 */
	protected final double[] getDoubleArray(String str) {
		String[] token = getArrayTokens(str);
		double[] dArray = new double[token.length];
		for (int i = 0; i < token.length; ++i) {
			dArray[i] = Double.parseDouble(token[i].trim());
		}
		return (dArray);
	}

	/**
	 * Get a string array from a string.
	 * 
	 * @param str
	 *            the string
	 * @return the string array
	 */
	protected final String[] getStringArray(String str) {
		String[] token = getArrayTokens(str);
		String[] sArray = new String[token.length];
		for (int i = 0; i < token.length; ++i) {
			sArray[i] = token[i].substring(1, token[i].length() - 1);
		}
		return (sArray);
	}

	/**
	 * Split the value into tokens.
	 * 
	 * @param str
	 *            the value
	 * @return array of tokens
	 */
	protected final String[] getArrayTokens(String str) {
		str = str.substring(1, str.length() - 1);
		return (str.split(","));
	}

	/**
	 * Get a scale factor for converting units.
	 * 
	 * @param str
	 *            the units
	 * @return the scale factor
	 */
	protected final double getUnitFactor(String str) {
		str = str.toLowerCase();
		if (str.contains("km")) {
			return (1000);
		}
		if (str.contains("rad")) {
			return (Math.toDegrees(1));
		}
		return (1);
	}
	
	/**
	 * Set the type for a PDS element.
	 * 
	 */
	public static void setTagType(String tag, PDSType type) {
		tagMap.put(tag, type);
	}
	
	/**
	 * Get the type for a PDS element.
	 * 
	 */
	public static PDSType setTagType(String tag) {
		return(tagMap.get(tag));
	}
}
