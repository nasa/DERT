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

package gov.nasa.arc.dert.landscape.quadtree;

import java.util.ArrayList;

import com.ardor3d.math.Vector3;


public class QuadKey {
	
	protected byte[] path;
	
	// Quadrants
	//
	//	+---+---+
	//	| 1 | 2 |
	//	+---+---+
	//	| 3 | 4 |
	//	+---+---+
	//
	
	public QuadKey() {
		path = new byte[0];
	}
	
	public QuadKey(byte[] path) {
		this.path = path;
	}
	
	public QuadKey(String str) {
		String[] token = str.split("/");
		if (token.length <= 1) {
			path = new byte[0];
		}
		else
			path = new byte[token.length-1];
		for (int i=1; i<token.length; ++i)
			path[i-1] = (byte)Integer.parseInt(token[i]);
	}
	
	public QuadKey(ArrayList<Byte> qList) {
		path = new byte[qList.size()];
		for (int i=0; i<path.length; ++i)
			path[i] = qList.get(i);
	}
	
	public QuadKey createChild(int quadrant) {
		byte[] childPath = new byte[path.length+1];
		System.arraycopy(path, 0, childPath, 0, path.length);
		childPath[path.length] = (byte)quadrant;
		return(new QuadKey(childPath));
	}
	
	@Override
	public String toString() {
		String str = "";
		for (int i=0; i<path.length; ++i)
			str += "/"+path[i];
		return(str);
	}

	public final int getLevel() {
		return(path.length);
	}
	
	public final byte[] getPath() {
		return(path);
	}
	
	public final byte getPath(int i) {
		return(path[i]);
	}
	
	public final int getQuadrant() {
		if (path.length == 0)
			return(0);
		return(path[path.length-1]);
	}
	
	public boolean equals(QuadKey that) {
		if (this.path.length != that.path.length)
			return(false);
		int n = Math.min(path.length, that.path.length);
		for (int i=0; i<n; ++i)
			if (this.path[i] != that.path[i])
				return(false);
		return(true);
	}
	
	public boolean startsWith(QuadKey that) {
		for (int i=0; i<that.path.length; ++i)
			if (this.path[i] != that.path[i])
				return(false);
		return(true);
	}
	
	public int findXAtLevel(int x, int level, int tileWidth) {
		int l = getLevel();
		if (level == l)
			return(x);
		if (level > l)
			return(-1);
		for (int i=l-1; i>=level; i--) {
			x /= 2;
			if ((path[i] == 2) || (path[i] == 4))
				x += tileWidth/2;
		}
		return(x);
	}
	
	public int findYAtLevel(int y, int level, int tileLength) {
		int l = getLevel();
		if (level == l)
			return(y);
		if (level > l)
			return(-1);
		for (int i=l-1; i>=level; i--) {
			y /= 2;
			if ((path[i] == 3) || (path[i] == 4))
				y += tileLength/2;
		}
		return(y);
	}

	/**
	 * Given a key, get the tile center relative to the center of the landscape
	 * 
	 * @param key
	 * @return
	 */
	public Vector3 getTileCenter(double terrainWidth, double terrainLength) {
		Vector3 p = new Vector3();
		if (path.length < 1)
			return (p);
		double wid = terrainWidth/2;
		double len = terrainLength/2;
		for (int i=0; i<path.length; ++i) {
			wid /= 2;
			len /= 2;
			switch (path[i]) {
			case 1:
				p.set(p.getX() - wid, p.getY() + len, 0);
				break;
			case 2:
				p.set(p.getX() + wid, p.getY() + len, 0);
				break;
			case 3:
				p.set(p.getX() - wid, p.getY() - len, 0);
				break;
			case 4:
				p.set(p.getX() + wid, p.getY() - len, 0);
				break;
			}
		}
		return (p);
	}

}
