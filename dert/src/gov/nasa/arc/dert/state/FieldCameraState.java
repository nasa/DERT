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
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCameraInfo;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCameraInfoManager;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.fieldcamera.FieldCameraView;

import java.util.Map;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides a state object for the FieldCamera
 *
 */
public class FieldCameraState extends ToolState {

	// The FieldCamera definition name
	public String fieldCameraDef;

	// Visibility flags
	public boolean fovVisible, lineVisible;
	public boolean crosshairVisible;

	// Camera viewpoint parameters
	public double azimuth, tilt, height;
	
	// Location of camera
	public Vector3 location;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public FieldCameraState(ReadOnlyVector3 position) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration()
			.incrementMapElementCount(MapElementState.Type.FieldCamera), MapElementState.Type.FieldCamera, "Camera", 1,
			FieldCamera.defaultColor, FieldCamera.defaultLabelVisible);
		viewData = new ViewData(600, 500, false);
		viewData.setVisible(true);
		fieldCameraDef = FieldCamera.defaultDefinition;
		fovVisible = FieldCamera.defaultFovVisible;
		lineVisible = FieldCamera.defaultLineVisible;
		azimuth = Double.NaN;
		tilt = Double.NaN;
		height = Double.NaN;
		location = new Vector3(position);
	}
	
	/**
	 * Constructor for hash map.
	 */
	public FieldCameraState(Map<String,Object> map) {
		super(map);
		crosshairVisible = StateUtil.getBoolean(map, "CrosshairVisible", false);
		location = StateUtil.getVector3(map, "Location", Vector3.ZERO);
		fieldCameraDef = StateUtil.getString(map, "FieldCameraDefinition", FieldCamera.defaultDefinition);
		fovVisible = StateUtil.getBoolean(map, "FovVisible", FieldCamera.defaultFovVisible);
		lineVisible = StateUtil.getBoolean(map, "LineVisible", FieldCamera.defaultLineVisible);
		azimuth = StateUtil.getDouble(map, "Azimuth", Double.NaN);
		tilt = StateUtil.getDouble(map, "Tilt", Double.NaN);
		height = StateUtil.getDouble(map, "Height", Double.NaN);
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof FieldCameraState))
			return(false);
		FieldCameraState that = (FieldCameraState)state;
		if (!super.isEqualTo(that))
			return(false);
		if (!this.fieldCameraDef.equals(that.fieldCameraDef))
			return(false);
		if (this.fovVisible != that.fovVisible) 
			return(false);
		if (this.lineVisible != that.lineVisible) 
			return(false);
		if (this.crosshairVisible != that.crosshairVisible) 
			return(false);
		if (this.azimuth != that.azimuth) 
			return(false);
		if (this.tilt != that.tilt) 
			return(false);
		if (this.height != that.height) 
			return(false);
		if (!this.location.equals(that.location)) 
			return(false);
		return(true);		
	}

	@Override
	public void dispose() {
		if (mapElement != null) {
			FieldCamera fieldCamera = (FieldCamera) mapElement;
			Landscape.getInstance().removeFieldCamera(fieldCamera.getName());
		}
		super.dispose();
	}

	@Override
	public Map<String,Object> save() {
		Map<String,Object> map = super.save();
		if (mapElement != null) {
			FieldCamera fieldCamera = (FieldCamera) mapElement;
			location = new Vector3(fieldCamera.getTranslation());
			fieldCameraDef = fieldCamera.getFieldCameraDefinition();
			fovVisible = fieldCamera.getSyntheticCameraNode().isFovVisible();
			lineVisible = fieldCamera.getSyntheticCameraNode().isSiteLineVisible();
			crosshairVisible = fieldCamera.getSyntheticCameraNode().isCrosshairVisible();
			azimuth = fieldCamera.getAzimuth();
			tilt = fieldCamera.getElevation();
			height = fieldCamera.getHeight();
		}
		map.put("CrosshairVisible", new Boolean(crosshairVisible));
		StateUtil.putVector3(map, "Location", location);
		map.put("FieldCameraDefinition", fieldCameraDef);
		map.put("FovVisible", new Boolean(fovVisible));
		map.put("LineVisible", new Boolean(lineVisible));
		map.put("Azimuth", new Double(azimuth));
		map.put("Tilt", new Double(tilt));
		map.put("Height", new Double(height));
		return(map);
	}

	/**
	 * Get the field camera definition information
	 * 
	 * @return
	 */
	public FieldCameraInfo getInfo() {
		try {
			return (FieldCameraInfoManager.getInstance().getFieldCameraInfo(fieldCameraDef));
		} catch (Exception e) {
			System.out.println("Unable to get camera definition " + fieldCameraDef + ", see log.");
			e.printStackTrace();
			return (null);
		}
	}
	
	protected void createView() {
		setView(new FieldCameraView((FieldCameraState) this));
//		viewData.createWindow(Dert.getMainWindow(), "DERT "+name, X_OFFSET, Y_OFFSET);
		viewData.createWindow(Dert.getMainWindow(), "DERT "+name);
	}
	
	@Override
	public String toString() {
		String str = super.toString();
		str = "["+fieldCameraDef+","+fovVisible+","+lineVisible+","+crosshairVisible+","+azimuth+","+tilt+","+height+","+location+"] "+str;
		return(str);
	}

}
