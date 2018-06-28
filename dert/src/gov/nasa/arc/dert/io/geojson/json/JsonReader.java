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
