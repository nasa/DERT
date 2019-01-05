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

package gov.nasa.arc.dert.io;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Read a comma separated value (CSV) formatted file.
 *
 */
public class CsvReader {

	// The file to be read
	protected String filename;
	
	// The input stream to be read;
	protected InputStream inputStream;

	// The reader
	protected BufferedReader reader;

	// Titles for the file columns
	protected String[] columnName;

	// Flag indicating the first line has the column titles
	protected boolean firstLineNames;

	// The value delimiter (defaults to a comma)
	protected String delimiter;

	// Indicates that line is to be ignored (defaults to a null)
	protected String ignore;

	/**
	 * Constructor
	 * 
	 * @param filename
	 *            the CSV file
	 * @param firstLineNames
	 *            the first line contains column titles
	 */
	public CsvReader(String filename, boolean firstLineNames) {
		this(filename, firstLineNames, ",", null);
	}

	/**
	 * Constructor
	 * 
	 * @param inputStream
	 *            the CSV file
	 * @param firstLineNames
	 *            the first line contains column titles
	 */
	public CsvReader(InputStream inputStream, boolean firstLineNames) {
		this(inputStream, firstLineNames, ",", null);
	}

	/**
	 * Constructor
	 * 
	 * @param filename
	 *            the CSV file
	 * @param firstLineNames
	 *            the first line contains column titles
	 * @param delimiter
	 *            the value delimiter (defaults to comma)
	 * @param ignore
	 *            if found in first column, ignore this line. (null = disabled)
	 */
	public CsvReader(String filename, boolean firstLineNames, String delimiter, String ignore) {
		this.filename = filename;
		this.firstLineNames = firstLineNames;
		this.delimiter = delimiter;
		this.ignore = ignore;
	}

	/**
	 * Constructor
	 * 
	 * @param inputStream
	 *            the CSV file
	 * @param firstLineNames
	 *            the first line contains column titles
	 * @param delimiter
	 *            the value delimiter (defaults to comma)
	 * @param ignore
	 *            if found in first column, ignore this line. (null = disabled)
	 */
	public CsvReader(InputStream inputStream, boolean firstLineNames, String delimiter, String ignore) {
		this.inputStream = inputStream;
		this.firstLineNames = firstLineNames;
		this.delimiter = delimiter;
		this.ignore = ignore;
	}

	/**
	 * Open the file and read the first line if it contains column titles.
	 * 
	 * @throws IOException
	 * @throws EOFException
	 */
	public void open() throws IOException, EOFException {
		if (filename != null)
			reader = new BufferedReader(new FileReader(filename));
		else
			reader = new BufferedReader(new InputStreamReader(inputStream));
		if (firstLineNames) {
			String str = reader.readLine();
			if (str == null) {
				throw new EOFException();
			}
			columnName = str.split(delimiter);
			for (int i = 0; i < columnName.length; ++i) {
				columnName[i] = columnName[i].trim();
			}
		}
	}

	/**
	 * Close the file
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		reader.close();
	}

	/**
	 * Get the number of columns
	 * 
	 * @return
	 */
	public int getNumColumns() {
		if (columnName == null) {
			return (0);
		}
		return (columnName.length);
	}

	/**
	 * Given an index, get the column name
	 * 
	 * @param i
	 * @return
	 */
	public String getColumnName(int i) {
		if (columnName == null) {
			return (null);
		}
		return (columnName[i]);
	}

	/**
	 * Read a line of the file and return an array of tokens. Return null if
	 * EOF. Each token is a string representing a value.
	 * 
	 * @return
	 * @throws IOException
	 */
	public String[] readLine() throws IOException {
		String str = null;
		do {
			str = reader.readLine();
			if (str == null) {
				return (null);
			}
		} while (isIgnored(str));
		
		String[] token = str.split(delimiter);
		for (int i = 0; i < token.length; ++i) {
			token[i] = token[i].trim();
		}
		return (token);
	}
		
	private boolean isIgnored(String str) {
		if (ignore == null)
			return(false);
		if (str.startsWith(ignore))
			return(true);
		return(false);
	}

}
