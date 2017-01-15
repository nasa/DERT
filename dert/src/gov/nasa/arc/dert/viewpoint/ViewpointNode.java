package gov.nasa.arc.dert.viewpoint;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.edit.CoordListener;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scenegraph.Marker;
import gov.nasa.arc.dert.scenegraph.text.RasterText;
import gov.nasa.arc.dert.scenegraph.text.Text.AlignType;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.view.world.CenterScale;
import gov.nasa.arc.dert.view.world.RGBAxes;

import java.awt.Toolkit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera.ProjectionMode;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.scenegraph.Node;

/**
 * Provides a node that carries the main camera for the WorldView. Note: This
 * node must be updated explicitly with updateGeometricState. It is not included
 * in the scene graph that is listened to for events.
 *
 */
public class ViewpointNode
	extends Node
	implements CoordListener {
	
	public static final double ELEV_HOME = Math.PI/2;
	public enum ViewpointMode {Nominal, Hike, Map}

	// Viewpoint has changed
	public AtomicBoolean changed = new AtomicBoolean();

	// The scene bounds
	private BoundingSphere sceneBounds = new BoundingSphere(10, new Vector3());

	// Closest distance
	private double closestDistance = 0.1;

	// The camera
	private BasicCamera camera;

	// Helper vectors and matrices
	private Vector3 direction = new Vector3();
	private Matrix3 rotate = new Matrix3();
	private Vector3 location = new Vector3();
	private Vector3 lookAt = new Vector3();
	private Matrix3 workRot = new Matrix3();
	private Vector3 workVec = new Vector3();
	private Vector3 tmpVec = new Vector3();
	private Vector3 seekPoint = new Vector3();

	// Orientation
	// Angle of rotation from horizontal plane
	private double elevation = 0;
	// Angle of rotation around center of rotation (lookAt or location)
	private double azimuth = 0;

	private boolean strictFrustum;

	// Cross hair
	private RGBAxes crosshair;
	private Node overlay;
	private RasterText corText, magText, altText, locText, azElText;
	private double textSize = 14;
	private CenterScale centerScale;
	private ViewpointMode mode;
	private ViewpointStore oldVP;
	private double zOffset;
	

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param store
	 */
	public ViewpointNode(String name, ViewpointStore store) {
		setName(name);
		setCamera(new BasicCamera(1, 1, 45, 1));
		crosshair = new RGBAxes();
		createOverlays();
		if (store != null) {
			setViewpoint(store, true, true);
			updateOverlay();
		}
	}

	/**
	 * Set the camera to use for this viewpoint node.
	 * 
	 * @param camera
	 */
	public void setCamera(BasicCamera camera) {
		this.camera = camera;
		if (camera != null) {
			updateWorldTransform(true);
			camera.setFrustum(sceneBounds);
			updateGeometricState(0);
			changed.set(true);
		}
	}
	
	/**
	 * Get the camera.
	 * 
	 */
	public BasicCamera getCamera() {
		return(camera);
	}

	/**
	 * Point at the given map element.
	 * 
	 * @param mapElement
	 */
	public void seek(MapElement mapElement) {
		
		double distance = mapElement.getSeekPointAndDistance(seekPoint);
		if (Double.isNaN(distance)) {
			return;
		}
		azimuth = 0;
		if (mode == ViewpointMode.Hike) {
			location.set(seekPoint.getX(), seekPoint.getY()-distance, 0);
			double z = Landscape.getInstance().getZ(location.getX(), location.getY());
			if (Double.isNaN(z)) {
				Toolkit.getDefaultToolkit().beep();
				return;
			}
			location.setZ(z+zOffset);
			tmpVec.set(seekPoint);
			tmpVec.subtractLocal(location);			
			tmpVec.normalizeLocal();
			Vector3 angle = MathUtil.directionToAzEl(tmpVec, null);
			elevation = ELEV_HOME+angle.getY();
			rotateCameraAroundLocation(location);
		}
		else if (mode == ViewpointMode.Nominal) {
			elevation = 1.25;
			rotateCameraAroundLookAtPoint(seekPoint, distance);
		}
		else {
			elevation = 0;
			rotateCameraAroundLookAtPoint(seekPoint, distance);
		}
		
		camera.setFrustum(sceneBounds);
		
		// update this node
		updateFromCamera();
		updateGeometricState(0);
		updateCrosshair();
		updateOverlay();
		changed.set(true);
	}
	
	/**
	 * This method uses the localToWorld transform so use it after a call to updateGeometricState.
	 */
	private void updateCrosshair() {
		// Locate the cursor just outside of the near plane.
		// Determine the size of the cursor.
		tmpVec.set(Vector3.UNIT_Z);
		double d = camera.getFrustumNear()+Landscape.getInstance().getPixelWidth();
		tmpVec.multiplyLocal(camera.getFrustumNear()+Landscape.getInstance().getPixelWidth());
		localToWorld(tmpVec, tmpVec);
		double scale = camera.getPixelSizeAt(tmpVec, true) * Marker.PIXEL_SIZE;
		if (scale <= 0)
			scale = 1;
		
		// Add the size to the near plane and recalculate the location so the cursor isn't clipped.
		tmpVec.set(Vector3.UNIT_Z);
		tmpVec.multiplyLocal(d+2*scale);
		localToWorld(tmpVec, tmpVec);

		crosshair.setScale(scale);
		crosshair.setTranslation(tmpVec);
		
		crosshair.updateWorldTransform(false);
	}
	
	private void createOverlays() {
		overlay = new Node("_textoverlay");
		corText = new RasterText("_cor", "", AlignType.Left, false);
		textSize = corText.getHeight()+2;
		corText.setTranslation(0, textSize, 0);
		corText.setColor(ColorRGBA.WHITE);
		corText.setVisible(true);
		overlay.attachChild(corText);
		magText = new RasterText("_mag", "", AlignType.Left, false);
		magText.setColor(ColorRGBA.WHITE);
		magText.setVisible(true);
		magText.setTranslation(0, 2*textSize, 0);
		overlay.attachChild(magText);
		altText = new RasterText("_alt", "", AlignType.Left, false);
		altText.setColor(ColorRGBA.WHITE);
		altText.setVisible(true);
		altText.setTranslation(0, 3*textSize, 0);
		overlay.attachChild(altText);
		azElText = new RasterText("_azel", "", AlignType.Left, false);
		azElText.setColor(ColorRGBA.WHITE);
		azElText.setVisible(true);
		azElText.setTranslation(0, 4*textSize, 0);
		overlay.attachChild(azElText);
		locText = new RasterText("_loc", "", AlignType.Left, false);
		locText.setColor(ColorRGBA.WHITE);
		locText.setVisible(true);
		locText.setTranslation(0, 5*textSize, 0);
		overlay.attachChild(locText);
		overlay.setTranslation(textSize, textSize, 0);
		overlay.updateGeometricState(0);
		overlay.getSceneHints().setRenderBucketType(RenderBucketType.Ortho);
		centerScale = new CenterScale(ColorRGBA.WHITE);
		centerScale.updateGeometricState(0);
		centerScale.getSceneHints().setRenderBucketType(RenderBucketType.Ortho);
	}
	
	public void coordDisplayChanged() {
		updateOverlay();
		changed.set(true);
	}
	
	private void updateOverlay() {
		// location of center of rotation
		if (mode == ViewpointMode.Hike)
			tmpVec.set(camera.getLocation());
		else
			tmpVec.set(camera.getLookAt());
		Landscape.getInstance().localToWorldCoordinate(tmpVec);
		String str = null;
		if (World.getInstance().getUseLonLat()) {
			Landscape.getInstance().worldToSphericalCoordinate(tmpVec);
			str = String.format("CoR: "+Landscape.stringFormat+"%s, "+Landscape.stringFormat+"%s", Math.abs(tmpVec.getXf()), (tmpVec.getXf() < 0 ? "W" : "E"), Math.abs(tmpVec.getYf()), (tmpVec.getYf() < 0 ? "S" : "N"));
		}
		else
			str = String.format("CoR: "+Landscape.stringFormat+", "+Landscape.stringFormat, tmpVec.getX(), tmpVec.getY());
		str += String.format(", "+Landscape.stringFormat, tmpVec.getZ());
		corText.setText(str);
		
		// location of viewpoint
		tmpVec.set(camera.getLocation());
		double height = tmpVec.getZ()-Landscape.getInstance().getZ(tmpVec.getX(), tmpVec.getY());
		Landscape.getInstance().localToWorldCoordinate(tmpVec);
		altText.setText(String.format("VP Hgt Abv Grnd: "+Landscape.stringFormat, height));
		if (World.getInstance().getUseLonLat()) {
			Landscape.getInstance().worldToSphericalCoordinate(tmpVec);
			str = String.format("VP Loc: "+Landscape.stringFormat+"%s, "+Landscape.stringFormat+"%s", Math.abs(tmpVec.getXf()), (tmpVec.getXf() < 0 ? "W" : "E"), Math.abs(tmpVec.getYf()), (tmpVec.getYf() < 0 ? "S" : "N"));
		}
		else
			str = String.format("VP Loc: "+Landscape.stringFormat+", "+Landscape.stringFormat, tmpVec.getX(), tmpVec.getY());
		str += String.format(", "+Landscape.stringFormat, tmpVec.getZ());
		locText.setText(str);
		
		// Az/El location
		str = String.format("VP Dir Az/El: "+Landscape.stringFormat+", "+Landscape.stringFormat, Math.toDegrees(azimuth), Math.toDegrees(elevation-ELEV_HOME));
		azElText.setText(str);
		
		// Magnification
		double mag = camera.getMagnification();
		str = String.format("VP Mag: "+Landscape.stringFormat, mag);
		magText.setText(str);
		
		// Scale
		double s = camera.getPixelSizeAt(camera.getLookAt(), true);
		centerScale.setText(s*100, camera.getLookAt().distance(camera.getLocation()));
	}
	
	public RGBAxes getCrosshair() {
		return(crosshair);
	}
	
	public Node getTextOverlay() {
		return(overlay);
	}
	
	public Node getCenterScale() {
		return(centerScale);
	}

	@Override
	public void updateWorldTransform(boolean recurse) {
		super.updateWorldTransform(recurse);
		if (camera == null) {
			return;
		}
		if (!strictFrustum) {
			camera.setFrustum(sceneBounds);
		}
		strictFrustum = false;
	}

	@Override
	public String toString() {
		return (getName() + " location:" + getWorldTranslation() + " left:" + camera.getLeft() + " up:"
			+ camera.getUp() + " direction:" + camera.getDirection());
	}

	protected void translate(ReadOnlyVector3 trans) {
		lookAt.set(camera.getLookAt());
		lookAt.addLocal(trans);
		location.set(camera.getLocation());
		location.addLocal(trans);
		if (mode == ViewpointMode.Hike) {
			if (!sceneBounds.contains(location))
				return;
			double z = Landscape.getInstance().getZ(location.getX(), location.getY());
			location.setZ(z+zOffset);
		}
		else {
			if (!sceneBounds.contains(lookAt))
				return;
		}
		camera.setLocation(location);
		camera.setLookAt(lookAt);
		updateFromCamera();
		updateGeometricState(0);
		updateCrosshair();
		updateOverlay();
		changed.set(true);
	}

	/**
	 * Update the scene bounds data.
	 */
	public void setSceneBounds() {
		BoundingVolume bounds = World.getInstance().getContents().getWorldBound();
		sceneBounds.setRadius(bounds.getRadius());
		sceneBounds.setCenter(bounds.getCenter());
		closestDistance = 0.0001 * sceneBounds.getRadius();
		changed.set(true);
	}

	/**
	 * Get the scene bounds data
	 * 
	 * @return
	 */
	public BoundingSphere getSceneBounds() {
		return (sceneBounds);
	}

	/**
	 * World view was resized
	 * 
	 * @param width
	 * @param height
	 */
	public void resize(int width, int height) {
		camera.setAspect(width / (double) height);
		camera.resize(width, height);
		updateCrosshair();
		centerScale.setTranslation(camera.getWidth()/2.0, camera.getHeight()/2.0, 0);
		centerScale.updateGeometricState(0);
		changed.set(true);
	}
	
	public int getCenterX() {
		return(camera.getWidth()/2);
	}
	
	public int getCenterY() {
		return(camera.getHeight()/2);
	}

	/**
	 * Reset the viewpoint to the overhead position
	 */
	public void reset() {
//		System.err.println("ViewpointNode.reset A: sceneBounds="+sceneBounds+" closestDistance="+closestDistance+" hikeMode="+hikeMode);
//		System.err.println("ViewpointNode.reset A: rotate="+rotate+" azimuth="+azimuth+" elevation="+elevation);
//		System.err.println("ViewpointNode.reset A: lookAt="+camera.getLookAt()+" location="+camera.getLocation()+" magnification="+camera.getMagnification());
		if (mode == ViewpointMode.Hike)
			mode = ViewpointMode.Nominal;
		ViewpointMenuAction.getInstance().setModeIcon(mode);
		World.getInstance().getContents().updateGeometricState(0);
		setSceneBounds();
		rotate.setIdentity();
		azimuth = 0;
		elevation = 0;
		lookAt.set(Landscape.getInstance().getCenter());
		location.set(0.0, 0.0, Landscape.getInstance().getWorldBound().getRadius());
		location.addLocal(lookAt);
		camera.setMagnification(BasicCamera.DEFAULT_MAGNIFICATION);
		camera.setFrame(location, rotate);
		camera.setLookAt(lookAt);
		camera.setFrustum(sceneBounds);
		updateFromCamera();
		updateGeometricState(0);
		updateCrosshair();
		Dert.getMainWindow().updateCompass(azimuth);
		updateOverlay();
		changed.set(true);
//		System.err.println("ViewpointNode.reset B: sceneBounds="+sceneBounds+" closestDistance="+closestDistance+" hikeMode="+hikeMode);
//		System.err.println("ViewpointNode.reset B: rotate="+rotate+" azimuth="+azimuth+" elevation="+elevation);
//		System.err.println("ViewpointNode.reset B: lookAt="+lookAt+" location="+location+" magnification="+camera.getMagnification());
	}

	/**
	 * Perform a drag operation
	 * 
	 * @param dx
	 * @param dy
	 */
	public void drag(double dx, double dy) {
		workVec.set(-dx, -dy, 0);
		workRot.fromAngleNormalAxis(azimuth, Vector3.NEG_UNIT_Z);
		workRot.applyPost(workVec, workVec);
		if (mode == ViewpointMode.Hike) {
			workVec.multiplyLocal(zOffset);
		}
		else {
			workVec.multiplyLocal(2*camera.getPixelSizeAt(camera.getLookAt(), true));
		}
		translate(workVec);
	}

	/**
	 * Perform a dolly operation
	 * 
	 * @param zDelta
	 */
	public void dolly(double zDelta) {
		if (mode == ViewpointMode.Hike) {
			drag(0, zDelta);
		}
		else {
			double distance = camera.getDistanceToCoR();
			zDelta = zDelta * distance * 0.01;
			if (((distance + zDelta) < closestDistance) && (zDelta < 0)) {
				return;
			}
			direction.set(camera.getDirection());
			location.set(camera.getLocation());
			location.subtractLocal(direction.multiplyLocal(zDelta));
			distance = location.distance(sceneBounds.getCenter());
			if ((distance > sceneBounds.getRadius() * 4) && (zDelta > 0)) {
				return;
			}
			camera.setLocation(location);
			updateFromCamera();
			updateGeometricState(0);
			updateCrosshair();
			updateOverlay();
			changed.set(true);
		}
	}

	/**
	 * Move in the screen plane
	 * 
	 * @param dx
	 * @param dy
	 */
	public void translateInScreenPlane(double dx, double dy) {
		if (mode != ViewpointMode.Nominal)
			return;
		double s = camera.getPixelSizeAt(camera.getLookAt(), false) * 0.5;
		workVec.set(dx * s, dy * s, 0);
		rotate.applyPost(workVec, workVec);
		translate(workVec);
	}

	/**
	 * Convert mouse deltas to movement parallel to the screen plane at the
	 * given position
	 * 
	 * @param dx
	 * @param dy
	 * @param result
	 * @param position
	 */
	public void coordInScreenPlane(double dx, double dy, Vector3 result, ReadOnlyVector3 position) {
		double s = camera.getPixelSizeAt(position, false);
		result.set(dx * s, dy * s, 0);
		rotate.applyPost(result, result);
	}

	/**
	 * Set the viewpoint
	 * 
	 * @param vps
	 * @param strict
	 *            set the camera frustum
	 */
	public void setViewpoint(ViewpointStore vps, boolean strict, boolean vpSelected) {
		strictFrustum = strict;
		mode = ViewpointMode.valueOf(vps.mode);
		zOffset = vps.zOffset;
		azimuth = vps.azimuth;
		elevation = vps.elevation+ELEV_HOME;
		camera.setMagnification(vps.magIndex);
		camera.setLookAt(vps.lookAt);
		if (strict) {
			camera.setFrustum(vps.frustumNear, vps.frustumFar, vps.frustumLeft, vps.frustumRight, vps.frustumTop,
				vps.frustumBottom);
		}
		if (mode == ViewpointMode.Hike)
			rotateCameraAroundLocation(vps.location);
		else
			rotateCameraAroundLookAtPoint(vps.lookAt, vps.lookAt.distance(vps.location));
		ViewpointMenuAction.getInstance().setModeIcon(mode);
		updateFromCamera();
		updateGeometricState(0);
		updateCrosshair();
		updateOverlay();
		changed.set(true);
	}
	
	/**
	 * Adapted from Ardor3D CameraNode.
	 */
    private void updateFromCamera() {
        final ReadOnlyVector3 camLeft = camera.getLeft();
        final ReadOnlyVector3 camUp = camera.getUp();
        final ReadOnlyVector3 camDir = camera.getDirection();
        final ReadOnlyVector3 camLoc = camera.getLocation();

        final Matrix3 rotation = Matrix3.fetchTempInstance();
        rotation.fromAxes(camLeft, camUp, camDir);

        setRotation(rotation);
        setTranslation(camLoc);

        Matrix3.releaseTempInstance(rotation);
    }

	/**
	 * Get the viewpoint
	 * 
	 * @param name
	 * @return
	 */
	public ViewpointStore getViewpoint(String name) {
		ViewpointStore store = new ViewpointStore(name, camera);
		store.zOffset = zOffset;
		store.mode = mode.toString();
		return (store);
	}

	/**
	 * Get the viewpoint
	 * 
	 * @param store
	 * @return
	 */
	public ViewpointStore getViewpoint(ViewpointStore store) {
		if (store == null) {
			store = new ViewpointStore();
		}
		store.mode = mode.toString();
		store.set(camera);
		store.zOffset = zOffset;
		return (store);
	}

	/**
	 * Rotate the viewpoint
	 * 
	 * @param xRotAngle
	 * @param zRotAngle
	 */
	public void rotate(float xRotAngle, float zRotAngle) {
		if (mode == ViewpointMode.Map)
			return;
		setAzAndEl(azimuth + (zRotAngle * 0.5 * Math.PI / 360), elevation + (xRotAngle * 0.5 * Math.PI / 360));
		if (mode == ViewpointMode.Hike)
			rotateCameraAroundLocation(camera.getLocation());
		else
			rotateCameraAroundLookAtPoint(camera.getLookAt(), camera.getDistanceToCoR());
	}

	/**
	 * Rotate around the center of rotation point (look at point) while facing
	 * the CoR.
	 */
	private void rotateCameraAroundLookAtPoint(ReadOnlyVector3 lookAt, double distance) {
		// create rotation matrix from azimuth and elevation
		rotate.fromAngleNormalAxis(azimuth, Vector3.NEG_UNIT_Z);
		workRot.fromAngleNormalAxis(elevation, Vector3.UNIT_X);
		rotate.multiplyLocal(workRot);
		// start the location at (0,0,1) and rotate it
		location.set(Vector3.UNIT_Z);
		rotate.applyPost(location, location);
		location.normalizeLocal();
		// move the location out by the distance from center and add the CoR coordinates to translate
		location.multiplyLocal(distance);
		location.addLocal(lookAt);
		// set the camera location and direction
		camera.setFrame(location, rotate);
		camera.setLookAt(lookAt);
		// update this node
		updateFromCamera();
		updateGeometricState(0);
		updateCrosshair();
		updateOverlay();
		changed.set(true);
		Dert.getMainWindow().updateCompass(azimuth);
	}

	/**
	 * Rotate around the viewpoint location.
	 */
	private void rotateCameraAroundLocation(ReadOnlyVector3 loc) {
		double d = camera.getLookAt().distance(camera.getLocation());
		// create rotation matrix from azimuth and elevation
		rotate.fromAngleNormalAxis(azimuth, Vector3.NEG_UNIT_Z);
		workRot.fromAngleNormalAxis(elevation, Vector3.UNIT_X);
		rotate.multiplyLocal(workRot);
		// set the camera location and direction
		camera.setFrame(loc, rotate);
		tmpVec.set(Vector3.UNIT_Z);
		tmpVec.multiplyLocal(d);
		localToWorld(tmpVec, tmpVec);
		camera.setLookAt(tmpVec);
		// update this node
		updateFromCamera();
		updateGeometricState(0);
		updateCrosshair();
		updateOverlay();
		changed.set(true);
		Dert.getMainWindow().updateCompass(azimuth);
	}

	/**
	 * Change the location of the viewpoint while retaining the direction.
	 * 
	 * @param loc
	 * @return
	 */
	public boolean changeLocation(ReadOnlyVector3 loc) {
		if (!locInBounds(loc))
			return(false);
		lookAt.set(camera.getDirection());
		lookAt.scaleAddLocal(camera.getDistanceToCoR(), loc);
		camera.setLocation(loc);
		camera.setLookAt(lookAt);
		updateFromCamera();
		updateGeometricState(0);
		updateCrosshair();
		changed.set(true);
		return (true);
	}
	
	public boolean locInBounds(ReadOnlyVector3 loc) {
		if (loc.distance(sceneBounds.getCenter()) > sceneBounds.getRadius() * 4) {
			return (false);
		}
		return(true);
		
	}

	/**
	 * Change the direction of the viewpoint while retaining the location.
	 * 
	 * @param dir
	 */
	public void changeDirection(Vector3 dir) {
		dir.normalizeLocal();
		Vector3 angle = MathUtil.directionToAzEl(dir, null);
		setAzAndEl(angle.getX(), angle.getY() + Math.PI / 2);
		rotateCameraAroundLocation(camera.getLocation());
		updateFromCamera();
		updateGeometricState(0);
		updateCrosshair();
		changed.set(true);
	}

	/**
	 * Change the distance from the center of rotation while retaining the
	 * viewpoint direction.
	 * 
	 * @param dist
	 * @return
	 */
	public boolean changeDistance(double dist) {
		location.set(camera.getDirection());
		location.negateLocal();
		location.scaleAddLocal(dist, camera.getLookAt());
		if (!locInBounds(location))
			return(false);
//		setTranslation(loc);
		camera.setLocation(location);
		updateFromCamera();
		updateGeometricState(0);
		updateCrosshair();
		changed.set(true);
		return (true);
	}

	/**
	 * Change the altitude of the viewpoint above the landscape.
	 * 
	 * @param height
	 * @return
	 */
	public boolean changeAltitude(double alt) {
		ReadOnlyVector3 trans = getWorldTranslation();
		double z = Landscape.getInstance().getZ(trans.getX(), trans.getY());
		if (Double.isNaN(z))
			return(false);
		location.set(trans);
		location.setZ(z + alt);
		return (changeLocation(location));
	}

	/**
	 * Get the altitude
	 * 
	 * @return
	 */
	public double getAltitude() {
		ReadOnlyVector3 trans = camera.getLocation();
		Landscape landscape = Landscape.getInstance();
		return (trans.getZ() - landscape.getZ(trans.getX(), trans.getY()));
	}

	/**
	 * Change az and el and apply the changes to the viewpoint rotation
	 * 
	 * @param az
	 * @param el
	 */
	public void changeAzimuthAndElevation(double az, double el) {
		setAzAndEl(az, el);
		elevation += ELEV_HOME;
		rotateCameraAroundLocation(camera.getLocation());
	}

	private void setAzAndEl(double az, double el) {
		azimuth = az % MathUtil.PI2;
		elevation = el % MathUtil.PI2;
		if (azimuth < 0) {
			azimuth += MathUtil.PI2;
		}
		if (elevation > Math.PI) {
			elevation = Math.PI;
		} else if (elevation < -Math.PI) {
			elevation = -Math.PI;
		}
	}

	/**
	 * Set the magnification value
	 * 
	 * @param val
	 */
	public void changeMagnification(double val) {
		camera.magnify(val);
		updateGeometricState(0);
		updateOverlay();
		changed.set(true);
	}

	/**
	 * Increase or decrease magnification by a delta
	 * 
	 * @param delta
	 */
	public void magnify(int delta) {
		camera.magnify(delta);
		updateGeometricState(0);
		updateCrosshair();
		updateOverlay();
		changed.set(true);
	}

	/**
	 * Set the center of rotation, translating the viewpoint and using the same
	 * rotation.
	 * 
	 * @param cor
	 */
	public void setCenterOfRotation(ReadOnlyVector3 cor) {
		camera.setLookAt(cor);
		rotate(0, 0);
		updateFromCamera();
		updateGeometricState(0);
		updateCrosshair();
		changed.set(true);
	}

	/**
	 * Set the look at point
	 * 
	 * @param lookAt
	 */
	public void setLookAt(ReadOnlyVector3 lookAt) {
		camera.setLookAt(lookAt);
		updateFromCamera();
		updateGeometricState(0);
		updateCrosshair();
		updateOverlay();
		changed.set(true);
	}
	
	public boolean setMode(ViewpointMode mode) {
		if (mode == ViewpointMode.Map) {
			oldVP = getViewpoint(oldVP);
			this.mode = mode;
			reset();
		}
		else if (mode == ViewpointMode.Hike) {
			ReadOnlyVector3 trans = camera.getLookAt();
			double z = Landscape.getInstance().getZ(trans.getX(), trans.getY());
			if (Double.isNaN(z))
				return(false);
			camera.setMaxNearPlane(1);
			zOffset = 2*Landscape.getInstance().getPixelWidth();
			location.set(trans);
			location.setZ(z + zOffset);
			if (mode == ViewpointMode.Map) {
				camera.setProjectionMode(ProjectionMode.Perspective);				
				camera.setFrustum(sceneBounds);
			}
			this.mode = mode;
			rotate.setIdentity();
			azimuth = 0;
			elevation = ELEV_HOME;
			rotate(0, 0);
			camera.setFrame(location, rotate);
			updateFromCamera();
			updateGeometricState(0);
			updateCrosshair();
			updateOverlay();
			changed.set(true);			
		}
		else {
			camera.setMaxNearPlane(Float.MAX_VALUE);
			if (this.mode == ViewpointMode.Map)
				setViewpoint(oldVP, true, false);
			else
				this.mode = mode;
		}
		changed.set(true);
		return(true);
	}
	
	public ViewpointMode getMode() {
		return(mode);
	}
}
