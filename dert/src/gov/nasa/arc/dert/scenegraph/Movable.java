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

package gov.nasa.arc.dert.scenegraph;

import gov.nasa.arc.dert.action.UndoHandler;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.view.world.GroundEdit;
import gov.nasa.arc.dert.view.world.MoveEdit;

import java.util.ArrayList;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Node;

/**
 * Abstract base class for objects that can be moved along the terrain.
 *
 */
public abstract class Movable extends Node {

	private boolean locked, inMotion;
	protected double zOff;
	private ArrayList<MotionListener> listeners;
	protected Vector3 location, workVec;

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public Movable(String name) {
		super(name);
		location = new Vector3();
		workVec = new Vector3();
		listeners = new ArrayList<MotionListener>();
	}

	/**
	 * Determine mobility
	 * 
	 * @return
	 */
	public boolean isLocked() {
		return (locked);
	}
	
	public boolean getLocked() {
		return(locked);
	}

	/**
	 * Set mobility
	 * 
	 * @param locked
	 */
	public void setLocked(boolean locked) {
		this.locked = locked;
		if (locked) {
			inMotion = false;
		}
	}

	/**
	 * Determine if moving
	 * 
	 * @return
	 */
	public boolean isInMotion() {
		return (inMotion);
	}

	/**
	 * Set moving
	 * 
	 * @param inMotion
	 * @param pickPosition
	 */
	public void setInMotion(boolean inMotion, ReadOnlyVector3 pickPosition) {
		this.inMotion = inMotion;
		enableHighlight(inMotion);
	}

	protected abstract void enableHighlight(boolean enable);

	/**
	 * Add listener
	 * 
	 * @param mol
	 */
	public void addMotionListener(MotionListener mol) {
		listeners.add(mol);
	}

	/**
	 * Remove listener
	 * 
	 * @param mol
	 */
	public void removeMotionListener(MotionListener mol) {
		listeners.remove(mol);
	}

	/**
	 * Notify listeners
	 */
	public void notifyListeners() {
		if (inMotion) {
			updateListeners();
		}
	}

	/**
	 * Notify listeners
	 */
	public void updateListeners() {
		ReadOnlyVector3 position = getTranslation();
		for (int i = 0; i < listeners.size(); ++i) {
			listeners.get(i).move(this, position);
		}
	}

	/**
	 * Get the radius of this movable.
	 * 
	 * @return
	 */
	public double getRadius() {
		updateWorldTransform(true);
		updateWorldBound(true);
		BoundingVolume wb = getWorldBound();
		if (wb == null) {
			return (1);
		}
		ReadOnlyVector3 s = getWorldScale();
		return (wb.getRadius() / s.getX());
	}

	/**
	 * Set the location
	 * 
	 * @param i
	 * @param p
	 */
	public void setLocation(double x, double y, double z, boolean doEdit) {
		if (doEdit)
			UndoHandler.getInstance().addEdit(new MoveEdit(this, new Vector3(location)));
		location.set(x, y, z);
		setTranslation(x, y, z+zOff);
		updateListeners();
	}
	
	public void setLocation(ReadOnlyVector3 loc, boolean doEdit) {
		setLocation(loc.getX(), loc.getY(), loc.getZ(), doEdit);
	}
	
	public ReadOnlyVector3 getLocation() {
		return(location);
	}
	
	public void setZOffset(double z, boolean doTrans) {
		zOff = z;
		if (doTrans) {
			setTranslation(location.getX(), location.getY(), location.getZ()+zOff);
			updateListeners();
		}
	}
	
	public double getZOffset() {
		return(zOff);
	}
	
	public GroundEdit ground() {
		GroundEdit ge = new GroundEdit(this, zOff);
		setZOffset(0, true);
		return(ge);
	}

	/**
	 * Get the location in planetary coordinates
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getLocationInWorld() {
		updateWorldTransform(false);
		workVec.set(getWorldTranslation());
		Landscape.getInstance().localToWorldCoordinate(workVec);
		return (workVec);
	}
}
