package gov.nasa.arc.dert.viewpoint;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.edit.CoordListener;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scenegraph.RasterText;
import gov.nasa.arc.dert.scenegraph.Text.AlignType;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.view.viewpoint.ViewpointPanel;
import gov.nasa.arc.dert.view.world.CenterScale;
import gov.nasa.arc.dert.view.world.RGBAxes;

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
	private double elevation = 0;
	private double azimuth = 0;

	// Display of viewpoint attributes
	private ViewpointPanel viewpointPanel;

	private boolean strictFrustum;
	private boolean viewpointSelected;

	// Cross hair
	private RGBAxes crosshair;
	private Node overlay;
	private RasterText corText, dstText, magText, altText;
	private double textSize = 14;
	private CenterScale centerScale;
	private boolean mapMode, hikeMode;
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
		setCamera(new BasicCamera(1, 1, 45, 1, 0));
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
	 * Set the panel that will display viewpoint attributes.
	 * 
	 * @param viewpointPanel
	 */
	public void setViewpointPanel(ViewpointPanel viewpointPanel) {
		this.viewpointPanel = viewpointPanel;
	}

	public void updateStatus() {
		if (viewpointPanel != null) {
			viewpointPanel.updateData(viewpointSelected);
		}
		viewpointSelected = false;
		updateOverlay();
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
		// create rotation matrix from azimuth and elevation
		rotate.fromAngleNormalAxis(azimuth, Vector3.NEG_UNIT_Z);
		if (!hikeMode) {
			elevation = Math.PI / 4;
			workRot.fromAngleNormalAxis(elevation, Vector3.UNIT_X);
			rotate.multiplyLocal(workRot);
		}
		// start the location at (0,0,1) and rotate it
		location.set(Vector3.UNIT_Z);
		rotate.applyPost(location, location);
		location.normalizeLocal();
		// move the location out by the distance from the map element and add the seek point
		location.multiplyLocal(distance);
		location.addLocal(seekPoint);
		if (hikeMode) {
			double z = Landscape.getInstance().getZ(location.getX(), location.getY());
			location.setZ(z+zOffset);
			elevation = Math.PI / 4;
			workRot.fromAngleNormalAxis(elevation, Vector3.UNIT_X);
			rotate.multiplyLocal(workRot);
		}
		// set the camera location and direction
		camera.setFrame(location, rotate);
		camera.setLookAt(seekPoint);
		// update this node
		updateFromCamera();
		updateCrosshair();
		updateGeometricState(0);
		changed.set(true);
	}
	
	private void updateCrosshair() {
		tmpVec.set(camera.getLookAt());
		double scale = camera.getPixelSizeAt(tmpVec, true) * 20;
		crosshair.setScale(scale);
		crosshair.setTranslation(tmpVec);
		crosshair.updateWorldTransform(false);
	}
	
	private void createOverlays() {
		overlay = new Node("_textoverlay");
		corText = new RasterText("_cor", "", AlignType.Left, false);
		textSize = corText.getFont()+2;
		corText.setColor(ColorRGBA.WHITE);
		corText.setVisible(true);
		overlay.attachChild(corText);
		magText = new RasterText("_mag", "", AlignType.Left, false);
		magText.setColor(ColorRGBA.WHITE);
		magText.setVisible(true);
		magText.setTranslation(0, 2*textSize, 0);
		overlay.attachChild(magText);
		dstText = new RasterText("_dst", "", AlignType.Left, false);
		dstText.setColor(ColorRGBA.WHITE);
		dstText.setVisible(true);
		dstText.setTranslation(0, textSize, 0);
		overlay.attachChild(dstText);
		altText = new RasterText("_alt", "", AlignType.Left, false);
		altText.setColor(ColorRGBA.WHITE);
		altText.setVisible(true);
		altText.setTranslation(0, 3*textSize, 0);
		overlay.attachChild(altText);
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
		if (hikeMode)
			tmpVec.set(camera.getLocation());
		else
			tmpVec.set(camera.getLookAt());
		Landscape.getInstance().localToWorldCoordinate(tmpVec);
		String str = null;
		if (World.getInstance().getUseLonLat()) {
			Landscape.getInstance().worldToSphericalCoordinate(tmpVec);
			str = String.format("CoR Loc: "+Landscape.stringFormat+" %s "+Landscape.stringFormat+" %s", Math.abs(tmpVec.getXf()), (tmpVec.getXf() < 0 ? "W" : "E"), Math.abs(tmpVec.getYf()), (tmpVec.getYf() < 0 ? "S" : "N"));
		}
		else
			str = String.format("CoR Loc: "+Landscape.stringFormat+" "+Landscape.stringFormat, tmpVec.getXf(), tmpVec.getYf());
		corText.setText(str);
		tmpVec.set(camera.getLocation());
		Landscape.getInstance().localToWorldCoordinate(tmpVec);
		altText.setText(String.format("Alt: "+Landscape.stringFormat, tmpVec.getZ()));
		double dist = 0;
		if (!hikeMode)
			dist = camera.getDistanceToCoR();
		str = String.format("CoR Dist: "+Landscape.stringFormat, dist);
		dstText.setText(str);
		double mag = camera.getMagnification();
		str = String.format("Mag: "+Landscape.stringFormat, mag);
		magText.setText(str);
		double s = camera.getPixelSizeAt(camera.getLookAt(), true);
		str = String.format(Landscape.stringFormat, (s*100));
		centerScale.setText(str);
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
		if (hikeMode) {
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
		updateCrosshair();
		updateGeometricState(0);
		changed.set(true);
		updateStatus();
	}

	/**
	 * Update the scene bounds data.
	 */
	public void setSceneBounds() {
		BoundingVolume bounds = World.getInstance().getRoot().getWorldBound();
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
		setSceneBounds();
		rotate.setIdentity();
		azimuth = 0;
		elevation = 0;
		lookAt.set(Landscape.getInstance().getCenter());
		location.set(0.0, 0.0, sceneBounds.getRadius());
		location.addLocal(lookAt);
		camera.setMagnification(BasicCamera.DEFAULT_MAGNIFICATION);
		camera.setFrame(location, rotate);
		camera.setLookAt(lookAt);
		camera.setFrustum(sceneBounds);
		updateFromCamera();
		updateCrosshair();
		updateGeometricState(0);
		Dert.getMainWindow().updateCompass(azimuth);
		updateOverlay();
		changed.set(true);
	}

	/**
	 * Perform a drag operation
	 * 
	 * @param dx
	 * @param dy
	 */
	public void drag(double dx, double dy) {
		if (hikeMode) {
			workVec.set(-dx, -dy, 0);
			workRot.fromAngleNormalAxis(azimuth, Vector3.UNIT_Z);
			workRot.applyPost(workVec, workVec);
//			workVec.multiplyLocal(camera.getPixelSizeAt(camera.getLookAt(), true));
			workVec.multiplyLocal(zOffset);
		}
		else {
			workVec.set(-dx, -dy, 0);
			workRot.fromAngleNormalAxis(azimuth, Vector3.NEG_UNIT_Z);
			workRot.applyPost(workVec, workVec);
			workVec.multiplyLocal(camera.getPixelSizeAt(camera.getLookAt(), true));
		}
		translate(workVec);
	}

	/**
	 * Perform a dolly operation
	 * 
	 * @param zDelta
	 */
	public void dolly(double zDelta) {
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
		updateCrosshair();
		updateGeometricState(0);
		changed.set(true);
		updateStatus();
	}

	/**
	 * Move in the screen plane
	 * 
	 * @param dx
	 * @param dy
	 */
	public void translateInScreenPlane(double dx, double dy) {
		if (hikeMode || mapMode)
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
		viewpointSelected = vpSelected;
		strictFrustum = strict;
		hikeMode = vps.hikeMode;
		zOffset = vps.zOffset;
		azimuth = vps.azimuth;
		elevation = vps.elevation+Math.PI/2;
		camera.setMagnification(vps.magIndex);
		camera.setLookAt(vps.lookAt);
		if (strict) {
			camera.setFrustum(vps.frustumNear, vps.frustumFar, vps.frustumLeft, vps.frustumRight, vps.frustumTop,
				vps.frustumBottom);
		}
		rotateTurntable(vps.lookAt.distance(vps.location));
		updateFromCamera();
		updateCrosshair();
		updateGeometricState(0);
		updateOverlay();
		changed.set(true);
		Dert.getMainWindow().setViewpointMode(hikeMode);
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
		if (mapMode) {
			ViewpointStore store = new ViewpointStore(name, oldVP);
			return(store);
		}
		else {
			ViewpointStore store = new ViewpointStore(name, camera);
			store.zOffset = zOffset;
			store.hikeMode = hikeMode;
			return (store);
		}
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
		store.set(camera);
		store.hikeMode = hikeMode;
		store.zOffset = zOffset;
		if (hikeMode) {
			store.distance = 0;
			store.lookAt.set(camera.getLocation());
		}
		return (store);
	}

	/**
	 * Rotate the viewpoint
	 * 
	 * @param xRotAngle
	 * @param zRotAngle
	 */
	public void rotate(float xRotAngle, float zRotAngle) {
		if (mapMode)
			return;
		setAzAndEl(azimuth + (zRotAngle * 0.5 * Math.PI / 360), elevation + (xRotAngle * 0.5 * Math.PI / 360));
		if (hikeMode)
			rotateCamera();
		else
			rotateTurntable(camera.getDistanceToCoR());
		updateStatus();
	}

	/**
	 * Rotate around the center of rotation point (look at point) while facing
	 * the CoR.
	 */
	private void rotateTurntable(double distance) {
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
		location.addLocal(camera.getLookAt());
		// set the camera location and direction
		camera.setFrame(location, rotate);
		// update this node
		updateFromCamera();
		updateGeometricState(0);
		changed.set(true);
		Dert.getMainWindow().updateCompass(azimuth);
	}

	/**
	 * Rotate around the viewpoint location.
	 */
	private void rotateCamera() {
		// create rotation matrix from azimuth and elevation
		rotate.fromAngleNormalAxis(azimuth, Vector3.UNIT_Z);
		workRot.fromAngleNormalAxis(elevation, Vector3.UNIT_X);
		rotate.multiplyLocal(workRot);
		// set the camera location and direction
		camera.setFrame(camera.getLocation(), rotate);
		// update this node
		updateFromCamera();
		updateGeometricState(0);
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
		updateCrosshair();
		updateGeometricState(0);
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
		changeCamera(dir);
	}

	/**
	 * Propagate camera direction changes to lookat, distance, and angles.
	 * 
	 * @param dir
	 */
	private void changeCamera(ReadOnlyVector3 dir) {
		Vector3 angle = MathUtil.directionToAzEl(dir, null);
		// This function returns the az angle around +Z axis from +Y
		// and the el angle around +X axis from +Y
		// We want the az angle to rotate around the -Z axis and
		// the el angle to rotate around +X axis from the -Z axis.
		setAzAndEl(-angle.getX(), angle.getY() + Math.PI / 2);
		lookAt.set(dir);
		lookAt.scaleAddLocal(camera.getDistanceToCoR(), camera.getLocation());
		camera.setLookAt(lookAt);
		rotateTurntable(camera.getDistanceToCoR());
		updateFromCamera();
		updateCrosshair();
		updateGeometricState(0);
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
		updateCrosshair();
		updateGeometricState(0);
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
		azimuth = az;
		elevation = el;
		rotateTurntable(camera.getDistanceToCoR());
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
		changed.set(true);
		updateStatus();
	}

	/**
	 * Increase or decrease magnification by a delta
	 * 
	 * @param delta
	 */
	public void magnify(int delta) {
		camera.magnify(delta);
		updateCrosshair();
		updateGeometricState(0);
		changed.set(true);
		updateStatus();
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
		updateCrosshair();
		updateGeometricState(0);
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
		updateCrosshair();
		updateGeometricState(0);
		changed.set(true);
	}
	
	public void setMapMode(boolean mapMode) {
		this.mapMode = mapMode;
		if (mapMode) {
			oldVP = getViewpoint(oldVP);
			hikeMode = false;
			camera.setProjectionMode(ProjectionMode.Parallel);
			rotate.setIdentity();
			azimuth = 0;
			elevation = 0;
			location.set(0.0, 0.0, sceneBounds.getRadius());
			location.addLocal(sceneBounds.getCenter());
			camera.setMagnification(BasicCamera.DEFAULT_MAGNIFICATION);
			camera.setFrame(location, rotate);
			camera.setLookAt(sceneBounds.getCenter());
			camera.setFrustum(sceneBounds);
			updateFromCamera();
			updateCrosshair();
			updateGeometricState(0);
			Dert.getMainWindow().updateCompass(azimuth);
			updateOverlay();
		}
		else {
			camera.setProjectionMode(ProjectionMode.Perspective);
			setViewpoint(oldVP, true, false);
		}
		changed.set(true);
	}
	
	public boolean setHikeMode(boolean hikeMode) {
		if (hikeMode) {
			ReadOnlyVector3 trans = camera.getLookAt();
			double z = Landscape.getInstance().getZ(trans.getX(), trans.getY());
			if (Double.isNaN(z))
				return(false);
			if (Landscape.getInstance().getPixelScale() > 1)
				zOffset = 0.02;
			else
				zOffset = 2;
			this.hikeMode = hikeMode;
			location.set(trans);
			location.setZ(z + zOffset);
			camera.setMagnification(BasicCamera.DEFAULT_MAGNIFICATION);
			if (mapMode) {
				mapMode = false;
				camera.setProjectionMode(ProjectionMode.Perspective);				
				camera.setFrustum(sceneBounds);
			}
			rotate.setIdentity();
			azimuth = 0;
			elevation = Math.PI/2;
			rotate(0, 0);
			camera.setFrame(location, rotate);
			updateFromCamera();
			updateCrosshair();
			updateGeometricState(0);
			changed.set(true);			
			updateStatus();
		}
		else
			this.hikeMode = hikeMode;
		camera.setOnFoot(hikeMode);
		Dert.getMainWindow().setViewpointMode(hikeMode);
		return(true);
	}
	
	public boolean isMapMode() {
		return(mapMode);
	}
	
	public boolean isHikeMode() {
		return(hikeMode);
	}
}
