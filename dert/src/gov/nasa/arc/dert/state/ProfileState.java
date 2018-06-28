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

package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.tool.Profile;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.View;
import gov.nasa.arc.dert.view.graph.Graph;
import gov.nasa.arc.dert.view.graph.GraphView;

import java.awt.Color;
import java.util.Map;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides a state object for the Profile tool.
 *
 */
public class ProfileState extends ToolState {

	// Profile end points
	public Vector3 p0, p1;
	public boolean axesEqualScale;
	public float lineWidth;
	public boolean endpointsVisible;
	public double zOff0, zOff1;

	// Graph
	protected Graph graph;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public ProfileState(ReadOnlyVector3 position) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration().incrementMapElementCount(MapElementState.Type.Profile), MapElementState.Type.Profile, "Profile",
			Profile.defaultSize, Profile.defaultColor, Profile.defaultLabelVisible);
		viewData = new ViewData(ViewData.DEFAULT_WINDOW_WIDTH, 300, false);
		viewData.setVisible(true);
		p0 = new Vector3(position);
		p1 = new Vector3(Landscape.getInstance().getCenter());
		p1.subtractLocal(p0);
		p1.normalizeLocal();
		p1.multiplyLocal(Landscape.defaultCellSize / 2);
		p1.addLocal(p0);
		p1.setZ(Landscape.getInstance().getZ(p1.getX(), p1.getY()));
		axesEqualScale = true;
		endpointsVisible = true;
		lineWidth = Profile.defaultLineWidth;
	}
	
	/**
	 * Constructor for hash map
	 */
	public ProfileState(Map<String,Object> map) {
		super(map);
		p0 = StateUtil.getVector3(map, "P0", Vector3.ZERO);
		p1 = StateUtil.getVector3(map, "P1", Vector3.ZERO);
		axesEqualScale = StateUtil.getBoolean(map, "AxesEqualScale", true);
		endpointsVisible = StateUtil.getBoolean(map, "EndpointsVisible", true);
		lineWidth = (float)StateUtil.getDouble(map, "LineWidth", Profile.defaultLineWidth);
		zOff0 = StateUtil.getDouble(map, "ZOffset0", 0);
		zOff1 = StateUtil.getDouble(map, "ZOffset1", 0);
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof ProfileState)) 
			return(false);
		ProfileState that = (ProfileState)state;
		if (!super.isEqualTo(that)) 
			return(false);
		if (this.axesEqualScale != that.axesEqualScale) 
			return(false);
		if (this.lineWidth != that.lineWidth)
			return(false);
		if (this.endpointsVisible != that.endpointsVisible)
			return(false);
		if (this.zOff0 != that.zOff0)
			return(false);
		if (this.zOff1 != that.zOff1)
			return(false);
		if (!this.p0.equals(that.p0)) 
			return(false);
		if (!this.p1.equals(that.p1)) 
			return(false);
		return(true);
	}

	@Override
	public Map<String,Object> save() {
		Map<String,Object> map = super.save();
		if (mapElement != null) {
			Profile profile = (Profile) mapElement;
			p0 = new Vector3(profile.getEndpointA());
			p1 = new Vector3(profile.getEndpointB());
			endpointsVisible = profile.isEndpointsVisible();
			zOff0 = profile.getMarkerA().getZOffset();
			zOff1 = profile.getMarkerB().getZOffset();
		}
		if (graph != null)
			axesEqualScale = graph.isAxesEqualScale();
		StateUtil.putVector3(map, "P0", p0);
		StateUtil.putVector3(map, "P1", p1);
		map.put("AxesEqualScale", new Boolean(axesEqualScale));
		map.put("LineWidth", new Double(lineWidth));
		map.put("EndpointsVisible", new Double(lineWidth));
		map.put("ZOffset0", new Double(zOff0));
		map.put("ZOffset1", new Double(zOff1));
		return(map);
	}

	/**
	 * Set the graph data
	 * 
	 * @param vertex
	 * @param vertexCount
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 */
	public void setData(float[] vertex, int vertexCount, float xMin, float xMax, float yMin, float yMax, float[] origVertex) {
		if (graph != null) {
			((GraphView)viewData.view).setData(vertex, vertexCount, xMin, xMax, yMin, yMax, origVertex);
		}
	}

	/**
	 * Set the graph color
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		if (graph != null) {
			((GraphView)viewData.view).setColor(color);
		}
	}

	/**
	 * Get the graph
	 * 
	 * @return
	 */
	public Graph getGraph() {
		if (graph != null) {
			return (graph);
		}
		try {
			color = getMapElement().getColor();
			graph = new Graph(100000, color, axesEqualScale);
			return (graph);
		} catch (Exception e) {
			System.out.println("Unable to load graph, see log.");
			e.printStackTrace();
			return (null);
		}
	}

	@Override
	public void setView(View view) {
		super.setView(view);
		((Profile) mapElement).updateGraph();
	}
	
	protected void createView() {
		setView(new GraphView(this));
//		viewData.createWindow(Dert.getMainWindow(), name + " Graph", X_OFFSET, Y_OFFSET);
		viewData.createWindow(Dert.getMainWindow(), name + " Graph");
	}
	
	@Override
	public String toString() {
		String str = "["+p0+","+p1+"]"+super.toString();
		return(str);
	}

}
