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

package gov.nasa.arc.dert.scene.featureset;

import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.quadtree.QuadTree;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scenegraph.FigureMarker;
import gov.nasa.arc.dert.scenegraph.LineStrip;
import gov.nasa.arc.dert.scenegraph.Marker;
import gov.nasa.arc.dert.state.FeatureState;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.util.SpatialUtil;
import gov.nasa.arc.dert.view.world.GroundEdit;
import gov.nasa.arc.dert.viewpoint.ViewDependent;

import java.awt.Color;
import java.util.HashMap;

import javax.swing.Icon;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;

public class Feature
	extends Node
	implements MapElement, ViewDependent {

	public static final Icon icon = Icons.getImageIcon("lineset_16.png");

	// Line color
	private Color color;

	// The location of the origin
	private Vector3 location;
	
	// Feature properties
	private HashMap<String,Object> properties;
	
	private boolean labelVisible;
	
	private FeatureState state;
	
	public Feature(FeatureState state, HashMap<String,Object> properties) {
		this(state.name, state.color, properties);
		this.state = state;
		state.setMapElement(this);
	}

	/**
	 * Constructor
	 * 
	 * @param state
	 * @param elevAttrName
	 */
	public Feature(String name, Color color, HashMap<String,Object> properties) {
		super(name);
		location = new Vector3();
		this.properties = properties;
		this.color = color;
		getSceneHints().setCullHint(CullHint.Dynamic);
	}

	/**
	 * Get the MapElement state
	 */
	@Override
	public MapElementState getState() {
		return (state);
	}

	/**
	 * Is this visible
	 */
	@Override
	public boolean isVisible() {
		return (SpatialUtil.isDisplayed(this));
	}

	/**
	 * Set visible
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			getSceneHints().setCullHint(CullHint.Dynamic);
		} else {
			getSceneHints().setCullHint(CullHint.Always);
		}
		markDirty(DirtyType.RenderState);
	}

	/**
	 * Pin this Feature (does nothing)
	 */
	@Override
	public void setLocked(boolean pinned) {
		// nothing here
	}

	/**
	 * Find out if pinned
	 */
	@Override
	public boolean isLocked() {
		return (false);
	}

	/**
	 * Get the color
	 */
	@Override
	public Color getColor() {
		return (color);
	}

	/**
	 * Set the color
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
		setColor(this);
	}

	private void setColor(Node node) {
		int n = node.getNumberOfChildren();
		for (int i = 0; i < n; ++i) {
			Spatial child = node.getChild(i);
			if (child instanceof LineStrip) {
				((LineStrip) child).setColor(color);
			}
			else if (child instanceof FigureMarker) {
				((FigureMarker) child).setColor(color);
			}
			else if (child instanceof Node) {
				setColor((Node) child);
			}
		}
	}

	/**
	 * Update the elevation (Z coordinate) for the lines
	 */
	@Override
	public boolean updateElevation(QuadTree quadTree) {
		boolean modified = false;
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			Spatial child = getChild(i);
			if (child instanceof LineStrip) {
				LineStrip lineStrip = (LineStrip) child;
				if (lineStrip.intersects(quadTree)) {
					lineStrip.updateElevation(quadTree);
					modified = true;
				}
			}
			else if (child instanceof FigureMarker) {
				FigureMarker fm = (FigureMarker)child;
				modified |= fm.updateElevation(quadTree);
			}
		}
		return (modified);
	}

	/**
	 * Set the vertical exaggeration
	 */
	@Override
	public void setVerticalExaggeration(double vertExag, double oldVertExag, double minZ) {
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			Spatial child = getChild(i);
			if (child instanceof LineStrip) {
				LineStrip lineStrip = (LineStrip) child;
				lineStrip.setScale(1, 1, vertExag);
			}
			else if (child instanceof FigureMarker) {
				FigureMarker fm = (FigureMarker)child;
				fm.setVerticalExaggeration(vertExag, oldVertExag, minZ);
			}
		}
	}

	/**
	 * Get the map element type
	 */
//	@Override
	public Type getType() {
		return (Type.Feature);
	}

	/**
	 * Get the point to seek for this lineset (center).
	 */
	@Override
	public double getSeekPointAndDistance(Vector3 point) {
		Spatial child = getChild(0);
		double distance = 1;
		if (child instanceof Marker) {
			point.set(child.getWorldTranslation());
			distance = Math.max(((Marker)child).getSize(), 20);
		}
		else {
			BoundingVolume bv = child.getWorldBound();
			point.set(bv.getCenter());
			distance = bv.getRadius();
		}
		return (distance);
	}

	/**
	 * Set the label visibility (does nothing).
	 */
	@Override
	public void setLabelVisible(boolean visible) {
		labelVisible = visible;
		setLabelVisible(this);
	}

	private void setLabelVisible(Node node) {
		int n = node.getNumberOfChildren();
		for (int i = 0; i < n; ++i) {
			Spatial child = node.getChild(i);
			if (child instanceof FigureMarker) {
				((FigureMarker) child).setLabelVisible(labelVisible);
			}
			else if (child instanceof Node) {
				setLabelVisible((Node) child);
			}
		}
	}

	/**
	 * Find out if the label is visible
	 */
	@Override
	public boolean isLabelVisible() {
		return (labelVisible);
	}
	
	public void setSize(float size) {
		setSize(this, size);
	}

	private void setSize(Node node, float size) {
		int n = node.getNumberOfChildren();
		for (int i = 0; i < n; ++i) {
			Spatial child = node.getChild(i);
			if (child instanceof FigureMarker) {
				((FigureMarker) child).setSize(size);
			}
			else if (child instanceof Node) {
				setSize((Node) child, size);
			}
		}
	}
	
	public void setLineWidth(float lineWidth) {
		setLineWidth(this, lineWidth);
	}

	private void setLineWidth(Node node, float lineWidth) {
		int n = node.getNumberOfChildren();
		for (int i = 0; i < n; ++i) {
			Spatial child = node.getChild(i);
			if (child instanceof LineStrip) {
				((LineStrip) child).setLineWidth(lineWidth);
				child.markDirty(DirtyType.RenderState);
			}
			else if (child instanceof Node) {
				setLineWidth((Node) child, lineWidth);
			}
		}
	}

	/**
	 * Get the size (returns 1).
	 */
	@Override
	public double getSize() {
		return (1);
	}

	/**
	 * Get the lineset icon
	 * 
	 * @return
	 */
	public Icon getIcon() {
		return (null);
	}

	/**
	 * Get the location of the origin in planetary coordinates
	 */
	public ReadOnlyVector3 getLocationInWorld() {
		location.set(getWorldTranslation());
		Landscape.getInstance().localToWorldCoordinate(location);
		return (location);
	}
	
	public GroundEdit ground() {
		return(null);
	}
	
	public void setZOffset(double zOff, boolean doTrans) {
		// do nothing
	}
	
	public double getZOffset() {
		return(0);
	}
	
	public HashMap<String,Object> getProperties() {
		return(properties);
	}
	
	public String toString() {
		return(getName());
	}
	
	public void update(BasicCamera camera) {
		if (!isVisible())
			return;
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			Spatial child = getChild(i);
			if (child instanceof ViewDependent)
				((ViewDependent)child).update(camera);
		}
	}
	
	public FeatureSet getFeatureSet() {
		Node parent = getParent();
		while (parent != null) {
			if (parent instanceof FeatureSet)
				return((FeatureSet)parent);
		}
		return(null);
	}
}
