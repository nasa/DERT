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

import gov.nasa.arc.dert.landscape.quadtree.QuadKey;

import java.util.ArrayList;

/**
 * Interface to source of landscape tiles.
 *
 */
public abstract class AbstractTileSource
	implements TileSource {

	// Quad tree structure of existing tile keys
	protected DepthTree depthTree;

	/**
	 * Given an X,Y coordinate, find the highest level tile key that contains
	 * that coordinate.
	 * 
	 * @param x
	 *            , y the coordinate
	 * @param worldWidth
	 *            , worldHeight the physical dimensions of the source
	 * @return the key string
	 */
	public synchronized QuadKey getKey(double x, double y, double worldWidth, double worldLength) {
		return (getKey(depthTree, x, y, new ArrayList<Byte>(), worldWidth / 2, worldLength / 2, -1));
	}

	/**
	 * Given an X,Y coordinate and level, find the key of the tile that contains
	 * that coordinate. Returns next best level if doesn't reach requested level.
	 * 
	 * @param x
	 *            , y the coordinate
	 * @param worldWidth
	 *            , worldHeight the physical dimensions of the source
	 * @return the key string
	 */
	public synchronized QuadKey getKey(double x, double y, double worldWidth, double worldLength, int lvl) {
		return (getKey(depthTree, x, y, new ArrayList<Byte>(), worldWidth / 2, worldLength / 2, lvl));
	}

	private QuadKey getKey(DepthTree depthTree, double x, double y, ArrayList<Byte> qList, double width, double length, int lvl) {
		if (depthTree.child == null)
			return (new QuadKey(qList));
		if (lvl == 0)
			return(new QuadKey(qList));	
		double w = width / 2;
		double l = length / 2;
		if (x < 0) {
			if (y >= 0) {
				depthTree = depthTree.child[0];
				qList.add(new Byte((byte)1));
				return (getKey(depthTree, x + w, y - l, qList, w, l, lvl-1));
			} else {
				depthTree = depthTree.child[2];
				qList.add(new Byte((byte)3));
				return (getKey(depthTree, x + w, y + l, qList, w, l, lvl-1));
			}
		} else {
			if (y >= 0) {
				depthTree = depthTree.child[1];
				qList.add(new Byte((byte)2));
				return (getKey(depthTree, x - w, y - l, qList, w, l, lvl-1));
			} else {
				depthTree = depthTree.child[3];
				qList.add(new Byte((byte)4));
				return (getKey(depthTree, x - w, y + l, qList, w, l, lvl-1));
			}
		}
	}
	
	protected abstract DepthTree getDepthTree();

}
