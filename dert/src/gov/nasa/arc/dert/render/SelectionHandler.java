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

package gov.nasa.arc.dert.render;

import gov.nasa.arc.dert.landscape.quadtree.QuadTreeMesh;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.SpatialPickResults;

import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.intersection.IntersectionRecord;
import com.ardor3d.intersection.PickData;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitiveKey;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Spatial;

public class SelectionHandler {

	/**
	 * This class provides the capability to pick objects in a scene using the
	 * Ardor3D picking functions.
	 */

	// data structure to hold results of a pick
	private PickResults pickResults;
	
	// Helper
	private Vector3 work = new Vector3();

	/**
	 * Constructor
	 */
	public SelectionHandler() {
		super();
		CollisionTreeManager.INSTANCE.setMaxElements(1024);
	}

	/**
	 * Do a selection
	 * 
	 * @param pickRay
	 * @param position
	 * @param normal
	 * @param boundsPick
	 * @param terrainOnly
	 * @return
	 */
	public Spatial doSelection(Ray3 pickRay, Vector3 position, Vector3 normal, SpatialPickResults boundsPick,
		boolean terrainOnly) {

		// First do a pick on the object bounds to reduce time spent on more
		// expensive pick.
		Mesh[] mesh = boundsPick.getMeshList();
		if (mesh.length == 0) {
			return (null);
		}

		// pick each mesh
		int meshIndex = -1;
		IntersectionRecord record = null;
		int index = -1;
		double dist = Double.MAX_VALUE;
		for (int i = 0; i < mesh.length; ++i) {
			if (terrainOnly) {
				if (!(mesh[i] instanceof QuadTreeMesh))
					continue;
			}
			// get the mesh bounds
			PickData pd = boundsPick.getPickData(i);
			IntersectionRecord ir = pd.getIntersectionRecord();
			if (ir == null) {
				continue;
			}
			if (ir.getNumberOfIntersections() == 0) {
				continue;
			}

			pickResults = new PrimitivePickResults();
			PickingUtil.findPick(mesh[i], pickRay, pickResults);
			if (pickResults.getNumber() > 0) {
				for (int j = 0; j < pickResults.getNumber(); j++) {
					pd = pickResults.getPickData(j);
					ir = pd.getIntersectionRecord();
					int closestIndex = ir.getClosestIntersection();
					double d = ir.getIntersectionDistance(closestIndex);
					if (d < dist) {
						dist = d;
						index = closestIndex;
						record = ir;
						meshIndex = i;
					}
				}
			}
		}
		if (record == null) {
			return (null);
		}
		ReadOnlyVector3 pos = record.getIntersectionPoint(index);
		ReadOnlyVector3 nrml = record.getIntersectionNormal(index);
		if (nrml == null) {
			PrimitiveKey key = record.getIntersectionPrimitive(index);
			Vector3[] vertices = mesh[meshIndex].getMeshData().getPrimitiveVertices(key.getPrimitiveIndex(),
				key.getSection(), null);
			if (vertices.length > 2) {
				nrml = getNormal(vertices[0], vertices[1], vertices[2]);
			} else {
				nrml = new Vector3();
			}
		}
		position.set(pos);
		normal.set(nrml);
		if (meshIndex >= 0) {
//			System.err.println("SelectionHandler.doSelection "+mesh[meshIndex]);
			return (mesh[meshIndex]);
		}
		return (null);
	}

	/**
	 * Get the normal at the picked location.
	 * 
	 * @param v0
	 * @param v1
	 * @param v2
	 * @return
	 */
	public Vector3 getNormal(Vector3 v0, Vector3 v1, Vector3 v2) {
		Vector3 store = new Vector3();
		MathUtil.createNormal(store, v0, v1, v2, work);
		return (store);
	}
}
