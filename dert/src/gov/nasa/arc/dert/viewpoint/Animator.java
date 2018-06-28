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

package gov.nasa.arc.dert.viewpoint;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.render.SceneFramework;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.view.viewpoint.AnimationPanel;
import gov.nasa.arc.dert.viewpoint.Viewpoint.ViewpointMode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.Timer;

import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;

/**
 * Controls the ViewpointNode with input from the InputHandler.
 *
 */
public class Animator {
	
	private Viewpoint viewpoint;

	// Fly through
	private Timer flyThroughTimer;
	private Vector<ViewpointStore> flyList;
	private int flyIndex;
	private FlyThroughParameters flyParams;
	private DecimalFormat formatter1 = new DecimalFormat("00");
	private DecimalFormat formatter2 = new DecimalFormat("00.000");
	private ViewpointStore oldViewpoint;
	
	private AnimationPanel animationPanel;
	
	// Curve for fly through
//	private CatmullRomSpline spline;

	/**
	 * Constructor
	 */
	public Animator(AnimationPanel animationPanel) {
		this.animationPanel = animationPanel;
		viewpoint = Dert.getWorldView().getScenePanel().getViewpointController().getViewpoint();
	}

	/**
	 * Set the fly through parameters
	 * 
	 * @param flyParams
	 */
	public void setFlyParams(FlyThroughParameters flyParams) {
		this.flyParams = flyParams;
	}

	/**
	 * Get the fly through parameters
	 * 
	 * return flyParams
	 */
	public FlyThroughParameters getFlyParams() {
		return(flyParams);
	}

	/**
	 * Stop flight
	 */
	public void stopFlyThrough() {
		if (flyThroughTimer == null)
			return;
		flyThroughTimer.stop();
		flyIndex = 0;
		Dert.getWorldView().getScenePanel().enableFrameGrab(null);
		// start the rendering framework again
		SceneFramework.getInstance().suspend(false);
		// put us back where we were
		if (oldViewpoint != null)
			viewpoint.set(oldViewpoint, false);
		flyThroughTimer = null;
		animationPanel.enableParameters(true);
	}

	/**
	 * Pause flight
	 */
	public void pauseFlyThrough() {
		if (flyThroughTimer != null)
			flyThroughTimer.stop();
	}

	/**
	 * Start flight
	 * A timer is used to run the flight. If frames are grabbed, each time a frame is rendered,
	 * it is saved to a file.
	 */
	public void startFlyThrough(final JLabel statusField) {
		if (flyThroughTimer == null) {
			if (flyParams.grab)
				Dert.getWorldView().getScenePanel().enableFrameGrab(flyParams.imageSequencePath);
			animationPanel.enableParameters(false);
			// Pause the rendering framework so it won't interfere.
			SceneFramework.getInstance().suspend(true);
			// make time step at least 1 second if we are grabbing frames
			final int millis = (flyParams.grab && (flyParams.millisPerFrame < 1000)) ? 1000 : flyParams.millisPerFrame;
			flyIndex = 0;
			oldViewpoint = viewpoint.get(oldViewpoint);
			flyThroughTimer = new Timer(millis, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					viewpoint.set(flyList.get(flyIndex), false);
					SceneFramework.getInstance().getFrameHandler().updateFrame();
					double t = (flyIndex * millis) / 1000.0;
					int hr = (int) (t / 3600);
					t -= hr * 3600;
					int min = (int) (t / 60);
					double sec = t - (min * 60);
					statusField.setText(formatter1.format(hr) + ":" + formatter1.format(min) + ":" + formatter2.format(sec) + "    Frame " + flyIndex);
					flyIndex++;
					if (flyIndex == flyList.size()) {
						if (!flyParams.loop)
							stopFlyThrough();
						flyIndex = 0;
					}
				}
			});
			flyThroughTimer.setDelay(millis);
		}
		flyThroughTimer.start();
	}

	/**
	 * Fly through a list of viewpoints
	 * 
	 * @param numFrames total number of frames
	 * @param millis number of milliseconds between frames
	 * @param loop repeat
	 * @param grab grab each frame to an image sequence
	 * @param seqPath the file path for the image sequence
	 */
	public String flyViewpoints(FlyThroughParameters flyParams) {
		this.flyParams = flyParams;
		if (flyParams.numFrames < 2)
			return("Too few frames to animate.");
		Vector<ViewpointStore> viewpointList = Dert.getWorldView().getScenePanel().getViewpointController().getViewpointList();
		if (viewpointList.size() < 2)
			return("Too few viewpoints to animate.");

		flyList = new Vector<ViewpointStore>();
		
		fillFlyList(viewpointList, flyList, flyParams.numFrames);
		
		return(null);
    }

	/**
	 * Fly through path waypoints
	 * 
	 * @param path the Path mapElement
	 * @param flyParams animation parameters
	 */
	public String flyPath(Path path, FlyThroughParameters flyParams) {
		this.flyParams = flyParams;
		if (flyParams.numFrames < 2)
			return("Too few frames to animate.");
		if (path.getNumberOfPoints() < 2)
			return("Too few waypoints to animate.");
		
		// create an interpolated curve from the path
		Vector3[] curve = path.getCurve(10);

		// create a list of viewpoints from the curve
		Vector<ViewpointStore> vpList = new Vector<ViewpointStore>();
		BasicCamera cam = new BasicCamera((Camera)viewpoint.getCamera());
		Vector3 loc = null;
		Vector3 look = null;
		ViewpointStore vps = null;
		Vector3 angle = null;
		for (int i = 0; i < curve.length-1; ++i) {

			// point the camera at the next way point location
			loc = new Vector3(curve[i].getX(), curve[i].getY(), curve[i].getZ()+flyParams.pathHeight);
			look = new Vector3(curve[i+1].getX(), curve[i+1].getY(), curve[i+1].getZ()+flyParams.pathHeight);
			// Set the camera frame.
			// Drop the tilt a little and rotate it 90 degrees since we are working parallel to the ground.
			angle = cam.setFrameAndLookAt(loc, look, Math.PI/2-Math.PI/20);
			// loc and look are the same point, we don't want that
			if (angle == null)
				continue;
			
			// set frustum and clipping planes
			cam.setClippingPlanes(viewpoint.getSceneBounds(), false);
			vps = new ViewpointStore(Integer.toString(i), cam);
			vpList.add(vps);
		}
		loc = look;
		look.addLocal(vps.direction);
		angle = cam.setFrameAndLookAt(loc, look, Math.PI/2-Math.PI/20);
		cam.setClippingPlanes(viewpoint.getSceneBounds(), false);
		vps = new ViewpointStore(Integer.toString(curve.length-1), cam);
		vpList.add(vps);
		flyList = new Vector<ViewpointStore>();
		fillFlyList(vpList, flyList, flyParams.numFrames);
		// set hike mode to true so we will use the viewpoint location as CoR
		for (int i=0; i<flyList.size(); ++i)
			flyList.get(i).mode = ViewpointMode.Hike.toString();
		return(null);
	}
	
	private void fillFlyList(Vector<ViewpointStore> vpList, Vector<ViewpointStore> flyList, int numFrames) {
		
		// get the total distance along the viewpoint list
		double dist = 0;
		ViewpointStore vps1 = vpList.get(0);
		ViewpointStore vps2 = null;
		for (int i=1; i<vpList.size(); ++i) {
			vps2 = vpList.get(i);
			dist += vps1.location.distance(vps2.location);
			vps1 = vps2;
		}
		
		// add inbetween viewpoints equally spaced along the list
		// equal spacing maintains a constant velocity
		double delta = dist/numFrames;
		vps2 = vpList.get(0);
		flyList.add(vps2);
		int k = 0;
		dist = 0;
		double d = delta;
		for (int i=0; i<numFrames; ++i) {
			if (d > dist) {
				d = d-dist;
				vps1 = vps2;
				k ++;
				if (k >= vpList.size())
					break;
				vps2 = vpList.get(k);
				dist = vps1.location.distance(vps2.location);
			}
			if (dist > 0) {
				flyList.add(vps1.getInbetween(vps2, d/dist));
				d += delta;
			}
		}
		flyList.add(vpList.get(vpList.size() - 1));
	}
}
