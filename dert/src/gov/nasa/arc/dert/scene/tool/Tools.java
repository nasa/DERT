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

package gov.nasa.arc.dert.scene.tool;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.quadtree.QuadTree;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.scenegraph.GroupNode;
import gov.nasa.arc.dert.state.FieldCameraState;
import gov.nasa.arc.dert.state.GridState;
import gov.nasa.arc.dert.state.PathState;
import gov.nasa.arc.dert.state.PlaneState;
import gov.nasa.arc.dert.state.ProfileState;
import gov.nasa.arc.dert.state.ScaleBarState;
import gov.nasa.arc.dert.state.ToolState;

import java.util.ArrayList;
import java.util.Properties;

import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Provides a group of Tool map elements
 *
 */
public class Tools extends GroupNode {

	// List of tool states
	private ArrayList<ToolState> toolList;
	
	private ZBufferState zBufferState;

	/**
	 * Constructor
	 * 
	 * @param toolList
	 */
	public Tools(ArrayList<ToolState> toolList) {
		super("Tools");
		this.toolList = toolList;
	}

	/**
	 * Create and add the tools to the scene graph
	 */
	public void initialize() {
		TextureState ts = new TextureState();
		ts.setEnabled(false);
		setRenderState(ts);
		for (int i = 0; i < toolList.size(); ++i) {
			ToolState state = toolList.get(i);
			addTool(state, false);
		}

		zBufferState = new ZBufferState();
		zBufferState.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
		zBufferState.setEnabled(true);
		setRenderState(zBufferState);
	}

	/**
	 * The landscape changed, update tool Z coordinates
	 * 
	 * @param quadTree
	 */
	public void landscapeChanged(final QuadTree quadTree) {
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			final Spatial child = getChild(i);
			if (child instanceof Tool) {
				((Tool) child).updateElevation(quadTree);
			}
		}
	}

	/**
	 * Get all tools of type FieldCamera
	 * 
	 * @return
	 */
	public ArrayList<FieldCamera> getFieldCameras() {
		ArrayList<FieldCamera> list = new ArrayList<FieldCamera>();
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			Spatial child = getChild(i);
			if (child instanceof FieldCamera) {
				list.add((FieldCamera) child);
			}
		}
		return (list);
	}

	/**
	 * Add a tool
	 * 
	 * @param state
	 * @param update
	 * @return
	 */
	public Tool addTool(ToolState state, boolean update) {
		Tool tool = null;
		switch (state.mapElementType) {
		case Figure:
		case Placemark:
		case Billboard:
		case Model:
			break;
		case Path:
			tool = new Path((PathState) state);
			break;
		case Plane:
			tool = new Plane((PlaneState) state);
			break;
		case Profile:
			tool = new Profile((ProfileState) state);
			break;
		case FieldCamera:
			tool = new FieldCamera((FieldCameraState) state);
			Landscape.getInstance().addFieldCamera(((FieldCamera)tool).getName());
			break;
		case CartesianGrid:
			tool = new CartesianGrid((GridState) state);
			break;
		case RadialGrid:
			tool = new RadialGrid((GridState) state);
			break;
		case Scale:
			tool = new ScaleBar((ScaleBarState)state);
			break;
		case Waypoint:
		case Marble:
		case FeatureSet:
		case Feature:
			break;
		}

		if (tool != null) {
			Spatial spatial = (Spatial) tool;
			attachChild(spatial);
			if (update) {
				spatial.updateGeometricState(0, true);
				tool.update(Dert.getWorldView().getViewpoint().getCamera());
			}
			if ((tool instanceof FieldCamera) || (tool instanceof Profile)) {
				state.open(true);
				spatial.markDirty(DirtyType.Transform);
			}
		}
		return (tool);
	}
	
	public void setHiddenDashed(boolean hiddenDashed) {
		for (int i=0; i<getNumberOfChildren(); ++i)
			((Tool)getChild(i)).setHiddenDashed(hiddenDashed);
	}

	/**
	 * Get a list of tools that are Paths
	 * 
	 * @return
	 */
	public ArrayList<Path> getFlyablePaths() {
		int n = getNumberOfChildren();
		ArrayList<Path> list = new ArrayList<Path>();
		for (int i = 0; i < n; ++i) {
			Spatial child = getChild(i);
			if (child instanceof Path) {
				Path path = (Path)child;
				if (path.getNumberOfPoints() > 1)
					list.add(path);
			}
		}
		return (list);
	}
	
	public void setOnTop(boolean onTop) {
		zBufferState.setEnabled(!onTop);
	}
	
	public boolean isOnTop() {
		return(!zBufferState.isEnabled());
	}

	/**
	 * Save defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		FieldCamera.saveDefaultsToProperties(properties);
		Path.saveDefaultsToProperties(properties);
		Plane.saveDefaultsToProperties(properties);
		CartesianGrid.saveDefaultsToProperties(properties);
		RadialGrid.saveDefaultsToProperties(properties);
		Profile.saveDefaultsToProperties(properties);
	}

}
