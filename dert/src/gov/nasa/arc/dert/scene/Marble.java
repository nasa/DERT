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

package gov.nasa.arc.dert.scene;

import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.quadtree.QuadTree;
import gov.nasa.arc.dert.scenegraph.DirectionArrow;
import gov.nasa.arc.dert.scenegraph.FigureMarker;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.state.MarbleState;
import gov.nasa.arc.dert.util.SpatialUtil;

import javax.swing.Icon;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;

/**
 * A green sphere that marks the location where the user last picked. It also
 * displays the surface normal at the point and the direction to the light.
 *
 */
public class Marble extends FigureMarker implements MapElement {

	// the direction arrow to the light
	private DirectionArrow solarDirectionArrow;

	// the MapElement state
	private MarbleState state;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public Marble(MarbleState state) {
		super("Marble", null, state.size, 0, state.color, state.labelVisible, true, false);
		setShape(ShapeType.sphere);
		surfaceNormalArrow.getSceneHints().setCullHint(CullHint.Never);
		getSceneHints().setAllPickingHints(false);
		solarDirectionArrow = new DirectionArrow("Direction to Sol", (float)(state.size*2), ColorRGBA.YELLOW);
		contents.attachChild(solarDirectionArrow);
		contents.detachChild(billboard);
		billboard = null;
		setVisible(state.visible);
		this.state = state;
		state.setMapElement(this);
	}

	/**
	 * Get the map element state
	 */
	@Override
	public MapElementState getState() {
		return (state);
	}

	/**
	 * Get the surface normal
	 */
	@Override
	public ReadOnlyVector3 getNormal() {
		return (surfaceNormal);
	}

	@Override
	public void setNormal(ReadOnlyVector3 normal) {
		if (normal != null) {
			super.setNormal(normal);
			state.updateText();
		}
	}

	/**
	 * Set the direction to the light
	 * 
	 * @param direction
	 */
	public void setSolarDirection(ReadOnlyVector3 direction) {
		solarDirectionArrow.setDirection(direction);
		state.updateText();
	}

	/**
	 * Move the marble
	 * 
	 * @param pos
	 * @param normal
	 * @param camera
	 */
	public void update(ReadOnlyVector3 pos, ReadOnlyVector3 normal, BasicCamera camera) {
		if (normal == null) {
			Vector3 store = new Vector3();
			Landscape.getInstance().getNormal(pos.getX(), pos.getY(), store);
			setNormal(store);
		} else {
			setNormal(normal);
		}
		setTranslation(pos);
		location.set(pos);
		if (camera != null) {
			update(camera);
		}
		state.updateText();
		updateGeometricState(0);
	}
	
	public void landscapeChanged(QuadTree quadTree) {
		updateElevation(quadTree);
	}

	/**
	 * Get the point and distance to seek to
	 */
	@Override
	public double getSeekPointAndDistance(Vector3 point) {
		point.set(getTranslation());
		double distance = Math.max(2*getSize(), 50*Landscape.getInstance().getPixelWidth());
		return (distance);
	}

	/**
	 * Get Map Element type
	 */
	@Override
	public Type getType() {
		return (Type.Marble);
	}

	/**
	 * Set the surface normal
	 * 
	 * @param show
	 */
	public void setSurfaceNormalVisible(boolean show) {
		surfaceNormalArrow.getSceneHints().setCullHint(show ? CullHint.Inherit : CullHint.Always);
		markDirty(DirtyType.RenderState);
	}

	/**
	 * Find out if surface normal is visible
	 * 
	 * @return
	 */
	public boolean isSurfaceNormalVisible() {
		return (SpatialUtil.isDisplayed(surfaceNormalArrow));
	}
	
	public Icon getIcon() {
		return(null);
	}

}
