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

package gov.nasa.arc.dert.landscape.io;

import gov.nasa.arc.dert.io.CsvReader;
import gov.nasa.arc.dert.io.CsvWriter;
import gov.nasa.arc.dert.view.Console;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Provides a data structure to record a landscape tile path/id in a quad tree
 * structure.
 *
 */
public class DepthTree implements Serializable {

	public String key;
	public DepthTree[] child;

	@Override
	public String toString() {
		return (key);
	}
	
	public static DepthTree load(String filePath)
		throws IOException {
		CsvReader reader = new CsvReader(filePath, true, ",", "#");
		return(load(reader));
	}
	
	public static DepthTree load(InputStream inputStream)
		throws IOException {
		CsvReader reader = new CsvReader(inputStream, true, ",", "#");
		return(load(reader));
	}
	
	protected static DepthTree load(CsvReader reader)
		throws IOException {
		ArrayList<String[]> tokenList = new ArrayList<String[]>();
		String[] token = null;
		reader.open();
		token = reader.readLine();
		while (token != null) {
			if (token.length < 6) {
				throw new IllegalArgumentException("Invalid depth tree entry.");
			}
			tokenList.add(token);
			token = reader.readLine();
		}
		if (tokenList.size() == 0) {
			throw new IllegalStateException("Depth tree is empty.");
		}
		reader.close();

		Collections.sort(tokenList, new Comparator<String[]>() {
			public int compare(String[] str1, String[] str2) {
				return(Integer.valueOf(str1[0]).compareTo(Integer.valueOf(str2[0])));
			}
		});
		
		// Find the maximum index
		int maxIndex = 0;
		for (int i=0; i<tokenList.size(); ++i) {
			int val = Integer.valueOf(tokenList.get(i)[0]);
			if (val > maxIndex)
				maxIndex = val;
		}
		
		// Fill an array with tokens, each at its index.
		String[][] tokenArray = new String[maxIndex+1][];
		for (int i=0; i<tokenList.size(); ++i) {
			int index = Integer.valueOf(tokenList.get(i)[0]);
			tokenArray[index] = tokenList.get(i);
		}
		
		// Create the depth tree.
		if (tokenArray[0] == null)
			throw new IllegalStateException("Could not find top level depth tree node.");
		
		DepthTree dt = createLeaf(0, tokenArray);
		return(dt);
	}
	
	protected static DepthTree createLeaf(int index, String[][] token) {
		DepthTree dt = new DepthTree();
		dt.key = token[index][1].trim();
		for (int i=0; i<4; ++i) {
			int ii = Integer.valueOf(token[index][i+2]);
			if (ii >= 0) {
				if (dt.child == null)
					dt.child = new DepthTree[4];
				dt.child[i] = createLeaf(ii, token);
			}
		}
		return(dt);
	}

	public static void store(DepthTree depthTree, String filePath) {
		CsvWriter csvWriter = null;
		try {
			String[] column = { "Index", "Id", "Child1", "Child2", "Child3", "Child4" };
			csvWriter = new CsvWriter(filePath, column);
			csvWriter.open();
			ArrayList<String[]> leafList = new ArrayList<String[]>();
			addLeaf(depthTree, 0, leafList);
			for (int i = 0; i < leafList.size(); ++i) {
				csvWriter.writeLine(leafList.get(i));
			}
			csvWriter.close();
			Console.println(leafList.size() + " records saved to " + filePath);
		} catch (Exception e) {
			e.printStackTrace();
			if (csvWriter != null) {
				try {
					csvWriter.close();
				} catch (Exception e2) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static int addLeaf(DepthTree depthTree, int index, ArrayList<String[]> leafList) {
		String[] token = new String[6];
		token[0] = Integer.toString(index);
		token[1] = depthTree.key;
		leafList.add(token);
		if (depthTree.child == null) {
			token[2] = "-1";
			token[3] = "-1";
			token[4] = "-1";
			token[5] = "-1";
		}
		else {
			index ++;
			token[2] = Integer.toString(index);
			index = addLeaf(depthTree.child[0], index, leafList);
			index ++;
			token[3] = Integer.toString(index);
			index = addLeaf(depthTree.child[1], index, leafList);
			index ++;
			token[4] = Integer.toString(index);
			index = addLeaf(depthTree.child[2], index, leafList);
			index ++;
			token[5] = Integer.toString(index);
			index = addLeaf(depthTree.child[3], index, leafList);
		}
		return(index);
	}
}
