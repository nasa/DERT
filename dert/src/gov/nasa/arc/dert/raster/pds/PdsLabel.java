package gov.nasa.arc.dert.raster.pds;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import javax.imageio.stream.ImageInputStream;

/**
 * Provide a parser for PDS label files.
 *
 */
public class PdsLabel {

	public static enum PDSType {
		PDS_Double, PDS_Integer, PDS_String, PDS_Long, PDS_Double_Array, PDS_Int_Array, PDS_String_Array, PDS_Long_Array, PDS_Symbol, PDS_Symbol_Array, PDS_Pointer, PDS_Time, PDS_Time_Array, PDS_Object, PDS_Group
	}
	
	public static String NULL_VALUE = "\"NULL\"", NA_VALUE = "\"N/A\"", UNK_VALUE = "\"UNK\"";

	public static class KeyValue {
		public String key;
		public String value;
		public PDSType type;

		public KeyValue(String key, String value, PDSType type) {
			this.key = key;
			this.value = value;
			this.type = type;
		}
		@Override
		public String toString() {
			return(key+"("+type+")"+" = "+value);
		}
	}

	protected HashMap<String, Object> valueMap;
	protected HashMap<String, PDSType> typeMap;
	protected HashMap<String, Object> unitsMap;
	
	protected ArrayList<KeyValue> keyValueList;
	protected ArrayList<String> objectList;
	
	protected SimpleDateFormat dateFormat1;
	protected SimpleDateFormat dateFormat2;

	protected String lineSeparator = System.getProperty("line.separator");
	
	protected long labelSize;

	/**
	 * Constructor
	 */
	public PdsLabel() {
		dateFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		dateFormat1.setTimeZone(TimeZone.getTimeZone("UTC"));
		dateFormat2 = new SimpleDateFormat("yyyy-DDD'T'HH:mm:ss.SSS");
		dateFormat2.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		objectList = new ArrayList<String>();
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
	public void parseHeader(ImageInputStream iStream) throws IOException {
		keyValueList = parsePDSObject(iStream);	
	}
	
	/**
	 * Get an integer value for a given key.
	 */
	public Integer getInteger(String key) {
		KeyValue kv = getKeyValue(key);
		Object v = getPDSValue(kv, true);
		if (v == null)
			return(null);
		if (kv.type == PDSType.PDS_Integer)
			return((Integer)v);
		else if (kv.type == PDSType.PDS_Long)
			return(new Integer(((Long)v).intValue()));
		return(null);
	}
	
	/**
	 * Get an integer value for a given key.
	 */
	public Integer[] getIntegerArray(String key) {
		KeyValue kv = getKeyValue(key);
		Object v = getPDSValue(kv, true);
		if (v == null)
			return(null);
		if (kv.type == PDSType.PDS_Int_Array)
			return((Integer[])v);
		else if (kv.type == PDSType.PDS_Long_Array) {
			Long[] l = (Long[])v;
			Integer[] iA = new Integer[l.length];
			for (int i=0; i<l.length; ++i)
				iA[i] = new Integer(l[i].intValue());
			return(iA);
		}
		return(null);
	}
	
	/**
	 * Get an integer value for a given key.
	 */
	public Double getDouble(String key) {
		KeyValue kv = getKeyValue(key);
		Object v = getPDSValue(kv, true);
		if (v == null)
			return(null);
		if (kv.type == PDSType.PDS_Double)
			return((Double)v);
		else if (kv.type == PDSType.PDS_Integer)
			return(new Double((Integer)v));
		else if (kv.type == PDSType.PDS_Long) {
			return(longToDouble((Long)v));
		}
		return(null);
	}
	
	/**
	 * Get an integer value for a given key.
	 */
	public Double[] getDoubleArray(String key) {
		KeyValue kv = getKeyValue(key);
		Object v = getPDSValue(kv, true);
		if (v == null)
			return(null);
		if (kv.type == PDSType.PDS_Double_Array)
			return((Double[])v);
		else if (kv.type == PDSType.PDS_Long_Array) {
			Long[] l = (Long[])v;
			Double[] dA = new Double[l.length];
			for (int i=0; i<l.length; ++i)
				dA[i] = longToDouble(l[i]);
			return(dA);
		}
		return(null);
	}
	
	/**
	 * Get a PDS symbol for a given key.
	 */
	public String getSymbol(String key) {
		KeyValue kv = getKeyValue(key);
		Object v = getPDSValue(kv, true);
		if (v == null)
			return(null);
		if (kv.type == PDSType.PDS_Symbol)
			return((String)v);
		else if (kv.type == PDSType.PDS_String)
			return((String)v);
		return(null);
	}
	
	/**
	 * Get a PDS symbol for a given key.
	 */
	public String getString(String key) {
		KeyValue kv = getKeyValue(key);
		Object v = getPDSValue(kv, true);
		if (v == null)
			return(null);
		if (kv.type == PDSType.PDS_String)
			return((String)v);
		else if (kv.type == PDSType.PDS_Symbol)
			return((String)v);
		return(null);
	}
	
	/**
	 * Get a PDS pointer for a given key.
	 */
	public Object[] getPointer(String key) {
		KeyValue kv = getKeyValue(key);
		Object v = getPDSValue(kv, true);
		if (v == null)
			return(null);
		if (kv.type == PDSType.PDS_Pointer)
			return((Object[])v);
		return(null);
	}
	
	protected final KeyValue getKeyValue(String key) {
		for (int i=0; i<keyValueList.size(); i++) {
			KeyValue kv = keyValueList.get(i);
			if (key.equals(kv.key))
				return(kv);
		}
		return(null);
	}

	/**
	 * Produce an ArrayList of Key/Value pairs from an ImageInputStream
	 * 
	 * @param reader
	 *            the stream
	 * @return the array list
	 * @throws IOException
	 */
	protected ArrayList<KeyValue> parsePDSObject(ImageInputStream reader) throws IOException {
		ArrayList<KeyValue> result = new ArrayList<KeyValue>();
		String str = null;
		String value = null;
		String key = null;
		KeyValue current = null;
		while ((str = reader.readLine()) != null) {
			String trimmed = str.trim();
			if (trimmed.isEmpty())
				continue;
			if (trimmed.startsWith("/*")) {
				continue;
			}
			if (trimmed.equals("END")) {
				break;
			} else if (trimmed.startsWith("OBJECT ")) {
				int n = trimmed.indexOf(" = ") + 3;
				String name = trimmed.substring(n);
				result.add(new KeyValue(name, name, PDSType.PDS_Object));
				ArrayList<KeyValue> list = parsePDSObject(reader);
				for (int i = 0; i < list.size(); ++i) {
					result.add(new KeyValue(name + "." + list.get(i).key, list.get(i).value, list.get(i).type));
				}
				current = null;
			} else if (trimmed.startsWith("END_OBJECT ")) {
				current = null;
				break;
			} else if (trimmed.startsWith("GROUP ")) {
				int n = trimmed.indexOf(" = ") + 3;
				String name = trimmed.substring(n);
				result.add(new KeyValue(name, name, PDSType.PDS_Group));
				ArrayList<KeyValue> list = parsePDSObject(reader);
				for (int i = 0; i < list.size(); ++i) {
					result.add(new KeyValue(name + "." + list.get(i).key, list.get(i).value, list.get(i).type));
				}
				current = null;
			} else if (trimmed.startsWith("END_GROUP ")) {
				current = null;
				break;
			} else {
				int n = str.indexOf(" = ");
				// continuation
				if (n < 0) {
					for (int j=0; j<str.length(); ++j)
						if (str.charAt(j) != ' ') {
							if (j > 0)
								str = str.substring(j-1);
						}
					value += str;
					if (current != null)
						current.value = value;
				} else {
					key = str.substring(0, n).trim();
					value = str.substring(n + 3);
					current = new KeyValue(key, value, null);
					result.add(current);
				}
			}
		}
		for (int i=0; i<result.size(); ++i) {
			KeyValue kv = result.get(i);
			assignPDSType(kv);
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

	public long writeObject(BufferedWriter writer, ArrayList<KeyValue> kvList) throws IOException {
		Comparator<KeyValue> comp = new Comparator<KeyValue>() {
			public int compare(KeyValue kv1, KeyValue kv2) {
				return(kv1.key.compareTo(kv2.key));
			}
			public boolean equals(Object obj) {
				return(this == obj);
			}
		};
		Collections.sort(kvList, comp);
		labelSize = 0;
		writeObject(writer, kvList, "", "", 0);
		String str = "END" + lineSeparator;
		writer.write(str);
		labelSize += str.length();
		return(labelSize);
	}

	protected int writeObject(BufferedWriter writer, ArrayList<KeyValue> kvList, String indent, String prefix, int index) throws IOException {
		int i = index;
		while (i < kvList.size()) {
			KeyValue kv = kvList.get(i);
			if (kv.type == PDSType.PDS_Group) {
				String str = indent+"GROUP = "+kv.value+lineSeparator;
				writer.write(str);
				labelSize += str.length();
				i = writeObject(writer, kvList, indent+"  ", prefix+kv.value+".", i+1);
				str = indent+"END_GROUP = "+kv.value+lineSeparator;
				writer.write(str);
				labelSize += str.length();
			}
			else if (kv.type == PDSType.PDS_Object) {
				String str = indent+"OBJECT = "+kv.value+lineSeparator;
				writer.write(str);
				labelSize += str.length();
				i = writeObject(writer, kvList, indent+"  ", prefix+kv.value+".", i+1);
				str = indent+"END_OBJECT = "+kv.value+lineSeparator;
				writer.write(str);
				labelSize += str.length();
			}
			else {
				if (prefix.isEmpty()) {
					String str = kv.key+" = "+kv.value;
					writer.write(str);
					labelSize += str.length();
				}
				else {
					if (kv.key.startsWith(prefix)) {
						String key = kv.key.substring(prefix.length());
						String str = indent+key+" = "+kv.value;
						writer.write(str);
						labelSize += str.length();
					}
					else {
						return(i);
					}
				}
			}
			i ++;
		}
		return (i);
	}

	/**
	 * Get the value of a PDS element according to type.
	 * 
	 * @param kv	the KeyValue containing the element
	 * @param normalize		adjust the value to meters or degrees if necessary
	 * @return an object
	 */
	protected Object getPDSValue(KeyValue kv, boolean normalize) {
		if (kv == null)
			return(null);
		if (kv.type == null)
			return(null);
		if (kv.value == null)
			return(null);
		if (isNull(kv.value))
			return(null);
		
		String tagValue = kv.value;
		Object value = null;
		try {
			switch (kv.type) {
			case PDS_Object:
			case PDS_Group:
				break;
			case PDS_Pointer:
				Object[] result = new Object[2];
				if (tagValue.startsWith("(")) {
					String[] token = getArrayTokens(tagValue);
					result[0] = getPDSString(token[0]);
					if (token.length > 1) {
						result[1] = new Long(getPDSLong(token[1]));
					}
					else {
						result[1] = new Long(0);
					}
				}
				else if (tagValue.startsWith("\"")) {
					result[0] = getPDSString(tagValue);
					result[1] = new Long(0);
				}
				else {
					result[0] = null;
					result[1] = new Long(getPDSLong(tagValue));
				}
				value = result;
				break;
			case PDS_String:
				value = getPDSString(tagValue);
				break;
			case PDS_Double:
				value = getPDSDouble(tagValue, normalize);
				break;
			case PDS_Integer:
				value = new Integer(getPDSInteger(tagValue, normalize));
				break;
			case PDS_Long:
				value = new Long(getPDSLong(tagValue));
				break;
			case PDS_Int_Array:
				value = getPDSIntegerArray(tagValue, normalize);
				break;
			case PDS_Long_Array:
				value = getPDSLongArray(tagValue);
				break;
			case PDS_Double_Array:
				value = getPDSDoubleArray(tagValue, normalize);
				break;
			case PDS_String_Array:
				value = getPDSStringArray(tagValue);
				break;
			case PDS_Symbol:
				value = getPDSSymbol(tagValue);
				break;
			case PDS_Symbol_Array:
				value = getPDSSymbolArray(tagValue);
				break;
			case PDS_Time:
				value = getPDSTime(tagValue);
				break;
			case PDS_Time_Array:
				value = getPDSTimeArray(tagValue);
				break;
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to parse value " + tagValue + " for " + kv.key + ".", e);
		}
		return(value);
	}

	/**
	 * Get an integer value from a string.
	 * 
	 * @param str
	 *            the string
	 * @return the integer
	 */
	protected final Integer getPDSInteger(String str, boolean normalize) {
		if (isNull(str))
			return(null);
		String vStr = str;
		int i = str.indexOf(" ");
		if (i >= 0) {
			vStr = vStr.substring(0, i).trim();
		}
		Integer value = new Integer(vStr);
		if (normalize) {
			String units = getNumericUnits(str);
			value = normalize(value, units);
		}
		return (value);
	}

	/**
	 * Get a long value from a string.
	 * 
	 * @param str
	 *            the string
	 * @return the long
	 */
	protected final Long getPDSLong(String str) {
		if (isNull(str))
			return(null);
		int pSpace = str.indexOf(' ');
		if (pSpace >= 0)
			str = str.substring(0, pSpace);
		long val;
		if (str.startsWith("2#")) {
			val = getBinaryAsLong(str);
		} else if (str.startsWith("16#")) {
			val = getHexAsLong(str);
		} else {
			val = Long.parseLong(str);
		}
		return (new Long(val));
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
	
	protected Double longToDouble(Long lval) {
		if (lval == null)
			return(null);
		return(new Double(Double.longBitsToDouble(lval)));
	}

	/**
	 * Get a double value from a string.
	 * 
	 * @param str
	 *            the string
	 * @return the double
	 */
	protected final Double getPDSDouble(String str, boolean normalize) {
		if (isNull(str))
			return(null);
		String vStr = str;
		int pSpace = vStr.indexOf(' ');
		if (pSpace >= 0)
			vStr = vStr.substring(0, pSpace);
		Double value = new Double(vStr);
		if (normalize) {
			String units = getNumericUnits(str);
			value = normalize(value, units);
		}
		return (value);
	}
	
	protected final String getPDSString(String str) {
		if (str == null)
			return(null);
		return(str.replace('"', ' ').trim());
	}
	
	protected final String getPDSSymbol(String str) {
		if (str == null)
			return(null);
		return(str.trim());
	}
	
	protected final Date getPDSTime(String str)
		throws ParseException {
		Date value = null;
		if (!str.contains("."))
			str += ".000";
		if ((str.charAt(4) == '-') && (str.charAt(7) == '-'))
			value = dateFormat1.parse(str);
		else
			value = dateFormat2.parse(str);
		return(value);
	}

	/**
	 * Get an Integer array from a string.
	 * 
	 * @param str
	 *            the string
	 * @return the Integer array
	 */
	protected final Integer[] getPDSIntegerArray(String str, boolean normalize) {
		String[] token = getArrayTokens(str);
		Integer[] iArray = new Integer[token.length];
		for (int i = 0; i < token.length; ++i) {
			iArray[i] = getPDSInteger(token[i].trim(), normalize);
		}
		return (iArray);
	}

	/**
	 * Get a long array from a string.
	 * 
	 * @param str
	 *            the string
	 * @return the long array
	 */
	protected final Long[] getPDSLongArray(String str) {
		String[] token = getArrayTokens(str);
		Long[] lArray = new Long[token.length];
		for (int i = 0; i < token.length; ++i) {
			lArray[i] = getPDSLong(token[i].trim());
		}
		return (lArray);
	}

	/**
	 * Get a double array from a string.
	 * 
	 * @param str
	 *            the string
	 * @return the double array
	 */
	protected final Double[] getPDSDoubleArray(String str, boolean normalize) {
		String[] token = getArrayTokens(str);
		Double[] dArray = new Double[token.length];
		for (int i = 0; i < token.length; ++i) {
			dArray[i] = getPDSDouble(token[i].trim(), normalize);
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
	protected final String[] getPDSStringArray(String str) {
		String[] token = getArrayTokens(str);
		String[] sArray = new String[token.length];
		for (int i = 0; i < token.length; ++i) {
			sArray[i] = getPDSString(token[i]);
		}
		return (sArray);
	}

	/**
	 * Get an array of PDS keys from a string.
	 * 
	 * @param str
	 *            the string
	 * @return the key array
	 */
	protected final String[] getPDSSymbolArray(String str) {
		String[] token = getArrayTokens(str);
		String[] sArray = new String[token.length];
		for (int i = 0; i < token.length; ++i) {
			sArray[i] = getPDSSymbol(token[i]);
		}
		return (sArray);
	}

	/**
	 * Get an array of PDS keys from a string.
	 * 
	 * @param str
	 *            the string
	 * @return the key array
	 */
	protected final Date[] getPDSTimeArray(String str)
		throws ParseException {
		String[] token = getArrayTokens(str);
		Date[] dArray = new Date[token.length];
		for (int i = 0; i < token.length; ++i) {
			dArray[i] = getPDSTime(token[i]);
		}
		return (dArray);
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
		String[] token = str.split(",");
		if (token.length == 0)
			token = new String[] {str};
		return(token);
	}

	/**
	 * Normalize value to meters or degrees.
	 * 
	 */
	public final double normalize(Double val, String unit) {
		if (unit == null)
			return(val);
		unit = unit.toLowerCase();
		if (unit.contains("km")) {
			return (1000*val);
		}
		if (unit.contains("rad")) {
			return (Math.toDegrees(val));
		}
		return (val);
	}

	/**
	 * Normalize value to meters or degrees.
	 * 
	 */
	public final int normalize(Integer val, String unit) {
		if (unit == null)
			return(val);
		unit = unit.toLowerCase();
		if (unit.contains("km")) {
			return (1000*val);
		}
		if (unit.contains("rad")) {
			return ((int)Math.toDegrees(val));
		}
		return (val);
	}
	
	protected void assignPDSType(KeyValue kv) {
		if (kv.type != null)
			return;
		if (kv.key.startsWith("^"))
			kv.type = PDSType.PDS_Pointer;
		else if (kv.value == null)
			kv.type = null;
		else if (kv.value.isEmpty() || isNull(kv.value))
			kv.type = null;
		else
			kv.type = findPDSType(kv.value);
	}
	
	protected PDSType findPDSType(String str) {
		str = str.trim();
		if (str.isEmpty())
			return(null);
		if (isNull(str))
			return(null);
		// Array
		if (str.startsWith("(") || str.startsWith("{")) {
			String[] token = getArrayTokens(str);
			PDSType t = findPDSType(token[0]);
			if (t == null)
				return(null);
			switch (t) {
			case PDS_Double:
				return(PDSType.PDS_Double_Array);
			case PDS_Integer:
				return(PDSType.PDS_Int_Array);
			case PDS_String:
				return(PDSType.PDS_String_Array);
			case PDS_Long:
				return(PDSType.PDS_Long_Array);
			case PDS_Symbol:
				return(PDSType.PDS_Symbol_Array);
			case PDS_Time:
				return(PDSType.PDS_Time_Array);
			default:
				return(null);
			}
		}
		// PDS_String
		if (str.contains("\""))
			return(PDSType.PDS_String);
		// Numeric
		int pSpace = str.indexOf(' ');
		if (pSpace >= 0)
			str = str.substring(0, pSpace);
		// PDS_Double, PDS_Integer, PDS_Long, PDS_Time
		PDSType t = getNumericType(str);
		if (t != null)
			return(t);
		// PDS_Symbol
		return(PDSType.PDS_Symbol);			
	}
	
	protected final boolean isNull(String str) {
		if (str.equals(NULL_VALUE) || str.equals(NA_VALUE) || str.equals(UNK_VALUE))
			return(true);
		return(false);
	}
	
	protected PDSType getNumericType(String str) {
		if ((str == null) || str.isEmpty())
			return(null);
		boolean isNum = false;
		char c = str.charAt(0);
		if (Character.isDigit(c))
			isNum = true;
		else if ((c == '-') || (c == '+') || (c == '.')) {
			if (str.length() > 1) {
				if (Character.isDigit(str.charAt(1)))
					isNum = true;
			}
		}
		if (!isNum)
			return(null);
		if (str.contains(":") && str.contains("T") && str.contains("-"))
			return(PDSType.PDS_Time);
		if (str.contains("#"))
			return(PDSType.PDS_Long);
		int pDot = str.indexOf('.');
		if (pDot >= 0)
			return(PDSType.PDS_Double);
		else
			return(PDSType.PDS_Integer);
	}
	
	protected String getNumericUnits(String str) {
		int pSpace = str.indexOf('<');
		if (pSpace >= 0) {
			String units = str.substring(pSpace+1);
			units = units.replace('>', ' ');
			units = units.trim();
			return(units);
		}
		return(null);
	}
}
