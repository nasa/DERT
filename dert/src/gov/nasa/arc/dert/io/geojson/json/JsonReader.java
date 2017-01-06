package gov.nasa.arc.dert.io.geojson.json;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;

public class JsonReader {
	
	private BufferedReader bReader;
	private StringBuffer sBuffer;
	private int currentChar;
	
	public JsonReader(Reader reader) {
		bReader = new BufferedReader(reader);
		sBuffer = new StringBuffer();
	}
	
	public JsonObject readObject() {
		return(getObject());
	}
	
	public void close() {
		try {
			bReader.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void skipWhitespace() {
		try {
			do {
				currentChar = bReader.read();
			} while (Character.isWhitespace(currentChar));
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	private void skipTo(char c, boolean skipCurrent) {
		try {
			if (!skipCurrent) {
				if (currentChar == c)
					return;
			}
			do {
				currentChar = bReader.read();
			} while (currentChar != c);
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	private JsonObject getObject() {
		try {
			JsonObject jObject = new JsonObject();
			skipTo('{', false);
			while (true) {
				skipWhitespace();
				String key = getString();
				if (currentChar != ':')
					throw new IllegalStateException("Missing ':' in JSON Object.");
				Object value = getValue();
				jObject.add(key, value);
//				System.err.println("JsonReader.getObject "+((char)currentChar)+" "+key+" "+value);
				if (currentChar == '}') {
					skipWhitespace();
					return(jObject);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return(null);
	}
	
	private String getString() {
		try {
			boolean escape = false;
			sBuffer.setLength(0);
			skipTo('"', false);
			while (true) {
				currentChar = bReader.read();
				switch (currentChar) {
				case '\\':
					if (escape) {
						sBuffer.append((char)currentChar);
						escape = false;
					}
					else
						escape = true;
					break;
				case '"':
					if (escape) {
						sBuffer.append((char)currentChar);
						escape = false;
					}
					else {
						skipWhitespace();
						return(sBuffer.toString());
					}
					break;
				case '/':
					if (escape) {
						sBuffer.append((char)currentChar);
						escape = false;
					}
					// else what?
					break;
				case 'b':
					if (escape) {
						sBuffer.append('\b');
						escape = false;
					}
					else
						sBuffer.append((char)currentChar);
					break;
				case 'f':
					if (escape) {
						sBuffer.append('\f');
						escape = false;
					}
					else
						sBuffer.append((char)currentChar);
					break;
				case 'n':
					if (escape) {
						sBuffer.append('\n');
						escape = false;
					}
					else
						sBuffer.append((char)currentChar);
					break;
				case 'r':
					if (escape) {
						sBuffer.append('\r');
						escape = false;
					}
					else
						sBuffer.append((char)currentChar);
					break;
				case 't':
					if (escape) {
						sBuffer.append('\t');
						escape = false;
					}
					else
						sBuffer.append((char)currentChar);
					break;
				case 'u':
					if (escape) {
						String hex = "0x"+(char)bReader.read()+(char)bReader.read()+(char)bReader.read();
						currentChar = bReader.read();
						hex += (char)currentChar;
						sBuffer.append((char)(int)Integer.decode(hex));
						escape = false;
					}
					else
						sBuffer.append((char)currentChar);
					break;
				default:
					sBuffer.append((char)currentChar);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return(null);
	}
	
	private Object getValue() {
		try {
			skipWhitespace();
			while (true) {
				switch (currentChar) {
				case '"':
					return(getString());
				case '{':
					return(getObject());
				case '[':
					return(getArray());
				case 't':
					skipTo('e', true);
					skipWhitespace();
					return(new Boolean(true));
				case 'f':
					skipTo('e', true);
					skipWhitespace();
					return(new Boolean(false));
				case 'n':
					skipTo('l', true);
					skipTo('l', true);
					skipWhitespace();
					return(null);
				default:
					return(getNumber());
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return(null);
	}
	
	private Object getNumber() {
		try {
			sBuffer.setLength(0);
			while (true) {
				if (Character.isWhitespace(currentChar)) {
					skipWhitespace();
					if (sBuffer.length() == 0)
						return(null);
					String nStr = sBuffer.toString();
					if (nStr.contains("."))
						return(new Double(nStr));
					else
						return(new Integer(nStr));
				}
				switch (currentChar) {
				case ',':
				case ']':
				case '}':
					if (sBuffer.length() == 0)
						return(null);
					String nStr = sBuffer.toString();
					if (nStr.contains("."))
						return(new Double(nStr));
					else
						return(new Integer(nStr));
				default:
					sBuffer.append((char)currentChar);
				}
				currentChar = bReader.read();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return(null);
	}
	
	private Object[] getArray() {
		try {
			ArrayList<Object> array = new ArrayList<Object>();
			while (true) {
				array.add(getValue());
				if (currentChar == ']') {
					skipWhitespace();
					return(array.toArray());
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return(null);
	}

}
