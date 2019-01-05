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

package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scene.tool.Path.BodyType;
import gov.nasa.arc.dert.scene.tool.Path.LabelType;
import gov.nasa.arc.dert.scene.tool.Waypoint;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.View;
import gov.nasa.arc.dert.view.mapelement.EditDialog;
import gov.nasa.arc.dert.view.mapelement.PathView;
import gov.nasa.arc.dert.viewpoint.FlyThroughParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * A state object for the Path tool.
 *
 */
public class PathState extends ToolState {

	// The points for the path
	public ArrayList<WaypointState> pointList;

	// The type of path body (point, line, or polygon)
	public BodyType bodyType;

	// The label type
	public LabelType labelType;

	// Way point visibility
	public boolean waypointsVisible;

	// Line width
	public double lineWidth;
	
	public FlyThroughParameters flyParams;
	
	// Params for PathView
	public double refElev = Double.NaN;
	public int volMethod = 1;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public PathState(ReadOnlyVector3 position) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration()
			.incrementMapElementCount(MapElementState.Type.Path), MapElementState.Type.Path, "Path", Path.defaultSize,
			Path.defaultColor, Path.defaultLabelVisible);
		bodyType = Path.defaultBodyType;
		labelType = Path.defaultLabelType;
		lineWidth = Path.defaultLineWidth;
		waypointsVisible = Path.defaultWaypointsVisible;
		viewData = new ViewData(-1, -1, true);
		pointList = new ArrayList<WaypointState>();
		WaypointState wp = new WaypointState(0, position, name + ".", Path.defaultSize, color, labelVisible, locked);
		pointList.add(wp);
		flyParams = new FlyThroughParameters();
	}
	
	/**
	 * Constructor for hash map.
	 */
	public PathState(Map<String,Object> map) {
		super(map);
		bodyType = Path.stringToBodyType(StateUtil.getString(map, "BodyType", null));
		labelType = Path.stringToLabelType(StateUtil.getString(map, "LabelType", null));
		lineWidth = StateUtil.getDouble(map, "LineWidth", Path.defaultLineWidth);
		flyParams = FlyThroughParameters.fromArray((double[])map.get("FlyParams"));
		flyParams.imageSequencePath = StateUtil.getString(map, "ImageSequencePath", null);
		waypointsVisible = StateUtil.getBoolean(map, "WaypointsVisible", Path.defaultWaypointsVisible);
		int n = StateUtil.getInteger(map, "WaypointCount", 0);
		pointList = new ArrayList<WaypointState>();
		for (int i=0; i<n; ++i)
			pointList.add(new WaypointState((HashMap<String,Object>)map.get("Waypoint"+i)));
		refElev = StateUtil.getDouble(map, "ReferenceElevation", Double.NaN);
		volMethod = StateUtil.getInteger(map, "VolumeMethod", 1);
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof PathState)) 
			return(false);
		PathState that = (PathState)state;
		if (!super.isEqualTo(that)) 
			return(false);
		if (this.bodyType != that.bodyType)
			return(false);
		if (this.labelType != that.labelType)
			return(false);
		if (this.waypointsVisible != that.waypointsVisible) 
			return(false);
		if (this.lineWidth != that.lineWidth)
			return(false);
		if (this.pointList.size() != that.pointList.size()) 
			return(false);
		for (int i=0; i<this.pointList.size(); ++i) {
			if (!this.pointList.get(i).isEqualTo(that.pointList.get(i)))
				return(false);
		}
		return(true);
	}

	@Override
	public Map<String,Object> save() {
		Map<String,Object> map = super.save();
		if (mapElement != null) {
			Path path = (Path) mapElement;
			getWaypointList();
			bodyType = path.getBodyType();
			labelType = path.getLabelType();
			lineWidth = path.getLineWidth();
			waypointsVisible = path.areWaypointsVisible();
		}
		map.put("BodyType", bodyType.toString());
		map.put("LabelType", labelType.toString());
		map.put("LineWidth", new Double(lineWidth));
		map.put("FlyParams", flyParams.toArray());
		map.put("ImageSequencePath", flyParams.imageSequencePath);
		map.put("WaypointsVisible", new Boolean(waypointsVisible));
		map.put("WaypointCount", new Integer(pointList.size()));
		for (int i=0; i<pointList.size(); ++i)
			map.put("Waypoint"+i, pointList.get(i).save());
		if (viewData.view != null) {
			PathView pv = (PathView)(viewData.view);
			refElev = pv.getVolElevation();
			volMethod = pv.getVolumeMethod();
			map.put("ReferenceElevation", new Double(refElev));
			map.put("VolumeMethod", new Integer(volMethod));
		}
		return(map);
	}

	/**
	 * Get the list of way points
	 * 
	 * @return
	 */
	public ArrayList<WaypointState> getWaypointList() {
		if (mapElement != null) {
			Path path = (Path) mapElement;
			int n = path.getNumberOfPoints();
			pointList = new ArrayList<WaypointState>();
			for (int i = 0; i < n; ++i) {
				Waypoint wp = path.getWaypoint(i);
				WaypointState wps = (WaypointState) wp.getState();
				wps.save();
				pointList.add(wps);
			}
		}
		return (pointList);
	}

	/**
	 * Open the editor
	 */
	@Override
	public EditDialog openEditor() {
		if (mapElement == null)
			return(null);
		getEditDialog();
		editDialog.open();
		editDialog.setMapElement(mapElement);
		editDialog.update();
		return(editDialog);
	}
	
	public EditDialog getEditDialog() {
		if (editDialog == null)
			editDialog = new EditDialog(Dert.getMainWindow(), "Edit "+mapElement.getName(), mapElement);
		return(editDialog);
	}

//	@Override
//	public void setAnnotation(String note) {
//		if (note != null) {
//			annotation = note;
//		}
//		if (mapElement != null) {
//			Path path = (Path) mapElement;
//			// update the way points
//			int n = path.getNumberOfPoints();
//			for (int i = 0; i < n; ++i) {
//				Waypoint wp = path.getWaypoint(i);
//				WaypointState wps = (WaypointState) wp.getState();
//				wps.setAnnotation(null);
//			}
//		}
//	}

	/**
	 * Set the MapElement
	 * 
	 * @param mapElement
	 */
	@Override
	public void setMapElement(MapElement mapElement) {
		// first time
		if ((this.mapElement == null) && (mapElement instanceof Path))
			this.mapElement = mapElement;
		if (editDialog != null)
			editDialog.setMapElement(mapElement);
	}
	
	@Override
	public void createView() {
		PathView view = new PathView(this, refElev, volMethod);
		setView(view);
//		viewData.createWindow(Dert.getMainWindow(), name + " View", X_OFFSET, Y_OFFSET);
		viewData.createWindow(Dert.getMainWindow(), name + " View");
	}

	@Override
	public void setView(View view) {
		viewData.setView(view);
		((PathView)view).doRefresh();
	}
	
	/**
	 * Notify user that the currently displayed statistics is old. We don't
	 * automatically update the window for performance reasons.
	 */
	public void pathDirty() {
		PathView pv = (PathView)viewData.view;
		if (pv != null)
			pv.pathDirty();
	}
	
	@Override
	public String toString() {
		String str = "["+bodyType+","+labelType+","+waypointsVisible+","+lineWidth+"]"+super.toString();
		return(str);
	}
}
