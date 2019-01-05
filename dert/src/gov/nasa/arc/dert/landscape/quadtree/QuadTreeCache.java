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

import java.util.HashMap;

public class QuadTreeCache {

	// The maximum amount of memory for the cache (in bytes)
	public static long MAX_CACHE_MEMORY = 400000000l;
	public static int MAX_CLEANUP_COUNT = 1000;

	// A hash map to keep track of QuadTree tiles
	protected HashMap<String, QuadTree> quadTreeMap;

	// The number of cache cleanups since the last garbage collection
	protected int cleanupCount;

	// The maximum cache size (in bytes)
	protected long cacheSize;

	/**
	 * Constructor
	 * 
	 */
	public QuadTreeCache() {
		quadTreeMap = new HashMap<String, QuadTree>();
	}

	/**
	 * Given a key, return the associated QuadTree and update its timestamp.
	 * 
	 * @param key
	 * @return the QuadTree
	 */
	public synchronized QuadTree getQuadTree(String key) {
		QuadTree quadTree = quadTreeMap.get(key);
		if (quadTree != null)
			quadTree.timestamp = System.currentTimeMillis();
		return(quadTree);
	}

	/**
	 * Place a QuadTree in the cache. and set its timestamp
	 * 
	 * @param key
	 * @param quadTree
	 */
	public synchronized void putQuadTree(String key, QuadTree quadTree) {
		quadTree.timestamp = System.currentTimeMillis();
		quadTreeMap.put(key, quadTree);
		cacheSize += quadTree.getSize();
		cleanUpCache();
	}
	
	/**
	 * Clear the entire cache.
	 */
	public synchronized void clear() {
		quadTreeMap.clear();
		System.gc();
		cacheSize = 0;
		cleanupCount = 0;		
	}
	
	/**
	 * Clear those QuadTrees from the cache that have names that start with a string.
	 * @param label the string
	 */
	public synchronized void clear(String label) {
		Object[] key = new Object[quadTreeMap.size()];
		key = quadTreeMap.keySet().toArray(key);
		for (int i=0; i<key.length; ++i) {
			if (((String)key[i]).startsWith(label)) {
				QuadTree qt = quadTreeMap.remove(key[i]);
				cacheSize -= qt.getSize();
			}
		}
	}

	protected void cleanUpCache() {
		// not full yet
		if (cacheSize < MAX_CACHE_MEMORY) {
			return;
		}
		// get the oldest QuadTree not in use
		Object[] key = new Object[quadTreeMap.size()];
		key = quadTreeMap.keySet().toArray(key);
		Object oldestKey = null;
		QuadTree oldestItem = null;
		int k = 1;
		for (int i=0; i<key.length; ++i) {
			oldestKey = key[i];
			oldestItem = quadTreeMap.get(oldestKey);
			if (!oldestItem.inUse) {
				k = i+1;
				break;
			}
		}
		if (oldestItem == null)
			throw new IllegalStateException("Unable to clean up quad tree cache.  All tiles are in use. Increase maximum cache size.");
		for (int i = k; i < key.length; ++i) {
			QuadTree item = quadTreeMap.get(key[i]);
			if (!item.inUse) {
				if (item.timestamp < oldestItem.timestamp) {
					oldestKey = key[i];
					oldestItem = item;
				}
			}
		}
		if (oldestItem.inUse)
			throw new IllegalStateException("Unable to clean up quad tree cache.  All tiles are in use. Increase maximum cache size.");
		// remove the oldest item if not in use
		QuadTree qt = quadTreeMap.remove(oldestKey);
		cacheSize -= qt.getSize();
//		System.err.println("QuadTreeCache.cleanUpCache "+cleanupCount+" "+qt+" "+cacheSize+" "+MAX_CACHE_MEMORY+" "+qt.getSize());
		qt.dispose();
		qt = null;
		cleanupCount ++;
		if (cleanupCount == MAX_CLEANUP_COUNT) {
			System.gc();
			cleanupCount = 0;
		}
	}

	/**
	 * Dispose of the cache
	 */
	public synchronized void dispose() {
		clear();
	}

	/**
	 * Update the surface color for all elements in the cache
	 * 
	 * @param label identifier
	 * @param rgba color
	 */
	public synchronized void updateSurfaceColor(float[] rgba) {
		QuadTree[] entry = new QuadTree[quadTreeMap.size()];
		quadTreeMap.entrySet().toArray(entry);
		for (int i = 0; i < entry.length; ++i)
			entry[i].getMesh().updateSurfaceColor(rgba);
	}
}
