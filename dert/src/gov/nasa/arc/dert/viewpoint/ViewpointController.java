package gov.nasa.arc.dert.viewpoint;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.render.SceneFramework;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scenegraph.Ray3WithLine;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.PathState;
import gov.nasa.arc.dert.state.ViewpointState;
import gov.nasa.arc.dert.viewpoint.Viewpoint.ViewpointMode;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.Timer;

import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Spatial;

/**
 * Controls the ViewpointNode with input from the InputHandler.
 *
 */
public class ViewpointController {

	// Determines if dolly/magnification is with or against scroll direction
	public static int mouseScrollDirection = -1;

	// Mouse position
	private double mouseX, mouseY;

	// In magnification mode
	private boolean isZoom;

	// Index of current viewpoint in list
	private int viewpointIndex;

	// Viewpoint list
	private Vector<ViewpointStore> viewpointList;

	// Node that carries the camera in the scene
	private Viewpoint viewpoint;

	// Helpers
	private Vector2 mousePos = new Vector2();
	private Vector3 pickPosition = new Vector3();
	private Vector3 pickNormal = new Vector3();

	// kinetic scrolling
	private long timestamp;
	private double velocity, amplitude;
	private double lastDx, lastDy;
	private double timeConstant = 325;

	// Fly through
	private Timer flyThroughTimer;
	private Vector<ViewpointStore> flyList;
	private int flyIndex;
	private FlyThroughParameters flyParams;
	private DecimalFormat formatter1 = new DecimalFormat("00");
	private DecimalFormat formatter2 = new DecimalFormat("00.000");
	private ViewpointStore oldViewpoint;
	
	// Curve for fly through
//	private CatmullRomSpline spline;

	/**
	 * Constructor
	 */
	public ViewpointController() {
		ViewpointState vpState = (ViewpointState)ConfigurationManager.getInstance().getCurrentConfiguration().getState("ViewpointState");
		Vector<ViewpointStore> vpList = vpState.viewpointList;
		viewpointList = new Vector<ViewpointStore>(vpList.size());
		for (int i=0; i<vpList.size(); ++i)
			viewpointList.addElement(vpList.elementAt(i));
	}

	/**
	 * Set the viewpoint
	 * 
	 * @param viewpoint
	 */
	public void setViewpointNode(Viewpoint viewpoint) {
		this.viewpoint = viewpoint;
		viewpointIndex = -1;
	}

	/**
	 * Get the viewpoint
	 * 
	 * @return
	 */
	public Viewpoint getViewpoint() {
		return (viewpoint);
	}

	/**
	 * Perform a ray pick at the given mouse X/Y.
	 * 
	 * @param x
	 * @param y
	 * @param position
	 * @param normal
	 * @param noQuadTree
	 * @return
	 */
	public Spatial doPick(double x, double y, Vector3 position, Vector3 normal, boolean terrainOnly) {
		mousePos.set(x, y);
		Ray3 pickRay = new Ray3WithLine();
		viewpoint.getCamera().getPickRay(mousePos, false, pickRay);
		return (World.getInstance().select(pickRay, position, normal, null, terrainOnly));
	}

	/**
	 * Enable magnify mode
	 * 
	 * @param enable
	 */
	public void enableZoom(boolean enable) {
		isZoom = enable;
	}

	/**
	 * Handle a mouse move.
	 * 
	 * @param x
	 * @param y
	 * @param dx
	 * @param dy
	 * @param button
	 * @param isControlled
	 *            control key held down for smaller movements
	 */
	public void mouseMove(double x, double y, double dx, double dy, int button) {
		if ((mouseX < 0) || (Math.abs(dx) > 100) || (Math.abs(dy) > 100)) {
			dx = 0;
			dy = 0;
		} else {
			dx = x - mouseX;
			dy = y - mouseY;
		}
		mouseX = x;
		mouseY = y;
		switch (button) {
		case 0:
			mouseX = -1;
			mouseY = -1;
			break;
		// Translating the terrain along its plane
		case 1:
			long now = System.currentTimeMillis();
			long elapsed = now - timestamp;
			timestamp = now;
			double delta = Math.sqrt(dx * dx + dy * dy);
			velocity = (100 * delta / (1 + elapsed)) * 0.8 + 0.2 * velocity;
//			System.err.println("ViewpointController.mouseMove "+velocity+" "+amplitude+" "+elapsed+" "+dx+" "+dy);
			viewpoint.drag(dx, dy);
			lastDx = dx;
			lastDy = dy;
			break;
		// translating the terrain in the screen plane
		case 2:
			viewpoint.translateInScreenPlane(-dx, -dy);
			break;
		// rotating the terrain
		case 3:
			viewpoint.rotate((float)dy, (float)dx);
			break;
		}
	}

	/**
	 * The mouse was scrolled
	 * 
	 * @param delta
	 */
	public void mouseScroll(int delta) {
		if (isZoom) {
			viewpoint.magnify(-mouseScrollDirection * delta);
		} else {
			viewpoint.dolly(mouseScrollDirection * 2 * delta);
			updateCoR();
		}
	}

	/**
	 * Mouse button was pressed
	 * 
	 * @param x
	 * @param y
	 * @param mouseButton
	 */
	public void mousePress(double x, double y, int mouseButton) {
		mouseX = x;
		mouseY = y;
		timestamp = System.currentTimeMillis();
//		System.err.println("ViewpointController.mousePress "+mouseX+" "+mouseY);
		velocity = 0;
		amplitude = 0;
		// turn off center scale text since we won't have an accurate value until mouse release
		if ((mouseButton == 1) || (viewpoint.getMode() == ViewpointMode.Hike))
			viewpoint.setCenterOfRotation(null, false);
	}

	/**
	 * Mouse button was released
	 * 
	 * @param x
	 * @param y
	 * @param mouseButton
	 */
	public void mouseRelease(int x, int y, int mouseButton) {
		mouseX = x;
		mouseY = y;
		long now = System.currentTimeMillis();
		// Make sure mouse has not been released after a pause.
		if ((Math.abs(velocity) > 10) && ((now-timestamp) < 100)) {
			amplitude = 0.8 * velocity;
			timestamp = now;
			double length = Math.sqrt(lastDx * lastDx + lastDy * lastDy);
			lastDx /= length;
			lastDy /= length;
		} else {
			amplitude = 0;
			updateCoR();
		}
	}
	
	public void updateCoR() {
		Spatial spat = doPick(viewpoint.getCenterX(), viewpoint.getCenterY(), pickPosition, pickNormal, false);
		if (spat != null) {
			viewpoint.setCenterOfRotation(pickPosition, false);
		}		
	}
	
	public void seek(MapElement mapElement) {
		viewpoint.seek(mapElement);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				updateCoR();
			}
		});
	}

	/**
	 * Update the kinetic scrolling
	 */
	public void update() {
		if (amplitude > 0) {
			long elapsed = System.currentTimeMillis() - timestamp;
			double delta = amplitude * Math.exp(-elapsed / timeConstant);
//			System.err.println("ViewpointController.update "+amplitude+" "+elapsed+" "+delta);
			if (Math.abs(delta) > 0.5) {
				viewpoint.drag(lastDx * delta, lastDy * delta);
			} else {
				amplitude = 0;
				updateCoR();
			}
		}
	}

	/**
	 * Left arrow key
	 */
	public void stepLeft(boolean shiftDown) {
		if (shiftDown)
			viewpoint.drag(-1, 0);
		else
			viewpoint.rotate(0, 1);
	}

	/**
	 * Right arrow key
	 */
	public void stepRight(boolean shiftDown) {
		if (shiftDown)
			viewpoint.drag(1, 0);
		else
			viewpoint.rotate(0, -1);
	}

	/**
	 * Up arrow key
	 */
	public void stepUp(boolean shiftDown) {
		if (shiftDown)
			viewpoint.drag(0, 1);
		else
			viewpoint.rotate(1, 0);
	}

	/**
	 * Down arrow key
	 */
	public void stepDown(boolean shiftDown) {
		if (shiftDown)
			viewpoint.drag(0, -1);
		else
			viewpoint.rotate(-1, 0);
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
	 * Add a viewpoint to the list
	 * 
	 * @param index
	 * @param name
	 */
	public void addViewpoint(int index, String name) {
		ViewpointStore vps = viewpoint.get(name);
		viewpointList.add(index, vps);
	}

	/**
	 * Remove a viewpoint from the list
	 * 
	 * @param vps
	 */
	public int removeViewpoints(int[] indices) {
		for (int i=indices.length-1; i>=0; --i)
			viewpointList.remove(indices[i]);
		viewpointIndex = indices[0]-1;
		if (viewpointList.size() == 0) {
			viewpointIndex = -1;
		} else if (viewpointIndex < 0) {
			viewpointIndex = viewpointList.size() - 1;
		}
		return(viewpointIndex);
	}

	/**
	 * Go to the previous viewpoint in the list
	 */
	public void previousViewpoint() {
		if (viewpointList.size() == 0) {
			return;
		}
		viewpointIndex--;
		if (viewpointIndex < 0) {
			viewpointIndex = viewpointList.size() - 1;
		}
		viewpoint.set(viewpointList.get(viewpointIndex), true);
	}

	/**
	 * Go to the next viewpoint in the list
	 */
	public void nextViewpoint() {
		if (viewpointList.size() == 0) {
			return;
		}
		viewpointIndex++;
		if (viewpointIndex >= viewpointList.size()) {
			viewpointIndex = 0;
		}
		viewpoint.set(viewpointList.get(viewpointIndex), true);
	}

	/**
	 * Go to a specific viewpoint
	 * 
	 * @param vp
	 */
	public void gotoViewpoint(ViewpointStore vp) {
		int index = viewpointList.indexOf(vp);
		if (index < 0) {
			return;
		}
		viewpointIndex = index;
		viewpoint.set(vp, true);
	}

	/**
	 * Get the number of viewpoints
	 * 
	 * @return
	 */
	public int getViewpointListCount() {
		return (viewpointList.size());
	}

	/**
	 * Get the viewpoint list
	 * 
	 * @return
	 */
	public Vector<ViewpointStore> getViewpointList() {
		return (viewpointList);
	}

	/**
	 * Get the flythrough parameters
	 * 
	 * @return
	 */
	public FlyThroughParameters getFlyThroughParameters() {
		if (flyParams == null)
			flyParams = new FlyThroughParameters();
		return (flyParams);
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
			if (flyParams.grab) {
				Dert.getWorldView().getScenePanel().enableFrameGrab(flyParams.imageSequencePath);
			}
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
	 * Close the fly through dialog
	 */
	public void closeFlyThrough() {
		// stop the flythrough if it is going
		stopFlyThrough();
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
	public void flyViewpoints(FlyThroughParameters flyParams) {

		flyList = new Vector<ViewpointStore>();
		
		fillFlyList(viewpointList, flyList, flyParams.numFrames);
    }

	/**
	 * Fly through path waypoints
	 * 
	 * @param path the Path mapElement
	 */
	public void flyPath(Path path) {
		flyParams = ((PathState)path.getState()).flyParams;
		
		// create an interpolated curve from the path
		Vector3[] curve = path.getCurve(10);

		// create a list of viewpoints from the curve
		Vector<ViewpointStore> vpList = new Vector<ViewpointStore>();
		BasicCamera cam = new BasicCamera((BasicCamera)viewpoint.getCamera());
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
