package gov.nasa.arc.dert.viewpoint;

import gov.nasa.arc.dert.render.JoglRendererDouble;
import gov.nasa.arc.dert.util.MathUtil;

import java.nio.DoubleBuffer;

import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyMatrix3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Extension of Ardor3D Camera that provides a zoom and uses double precision.
 *
 */
public class BasicCamera extends Camera {

	public static final double DEFAULT_FOVX = 45;
	public static final int DEFAULT_MAGNIFICATION = 9;
	public static final double[] magFactor = new double[1020];

	static {
		double z = 0.0;
		for (int i = 0; i < 20; ++i) {
			z += 0.1;
			magFactor[i] = z;
		}
		for (int i = 20; i < magFactor.length; ++i) {
			z += 1;
			magFactor[i] = z;
		}
	}

	private int magIndex = 9;
	private double aspect = 1;
	private double nearPlane, farPlane;
	private Vector3 lookAt = new Vector3(0, 0, -10);
	private int[] viewport = new int[4];
	private double fovX;
	private boolean onFoot;
	private final DoubleBuffer _matrixBuffer = BufferUtils.createDoubleBuffer(16);

	/**
	 * Constructor
	 * 
	 * @param width
	 * @param height
	 */
	public BasicCamera(int width, int height) {
		this(width, height, DEFAULT_FOVX, width / (double) height, 0);
	}

	/**
	 * Constructor
	 * 
	 * @param width
	 * @param height
	 * @param fovX
	 * @param aspect
	 * @param distance
	 */
	public BasicCamera(int width, int height, double fovX, double aspect, double distance) {
		super(width, height);
		this.fovX = fovX;
		this.aspect = aspect;
		_fovY = fovX / aspect;
	}

	/**
	 * Constructor
	 * 
	 * @param that
	 */
	public BasicCamera(BasicCamera that) {
		super(that);
		_fovY = that.getFovY();
		fovX = that.getFovX();
		lookAt.set(that.getLookAt());
		aspect = that.getAspect();
	}

	/**
	 * Copy the source camera's fields to this camera
	 * 
	 * @param source
	 *            the camera to copy from
	 */
	@Override
	public void set(final Camera source) {
		_width = source.getWidth();
		_height = source.getHeight();

		_location.set(source.getLocation());
		_left.set(source.getLeft());
		_up.set(source.getUp());
		_direction.set(source.getDirection());
		_fovY = source.getFovY();
		fovX = ((BasicCamera) source).getFovX();
		aspect = ((BasicCamera) source).getAspect();

		_depthRangeNear = source.getDepthRangeNear();
		_depthRangeFar = source.getDepthRangeFar();
		_depthRangeDirty = true;

		magIndex = ((BasicCamera) source).getMagIndex();

		_frustumNear = source.getFrustumNear();
		_frustumFar = source.getFrustumFar();
		_frustumLeft = source.getFrustumLeft() / magFactor[magIndex];
		_frustumRight = source.getFrustumRight() / magFactor[magIndex];
		_frustumTop = source.getFrustumTop() / magFactor[magIndex];
		_frustumBottom = source.getFrustumBottom() / magFactor[magIndex];

		_viewPortLeft = source.getViewPortLeft();
		_viewPortRight = source.getViewPortRight();
		_viewPortTop = source.getViewPortTop();
		_viewPortBottom = source.getViewPortBottom();

		_planeQuantity = 6;

		setProjectionMode(source.getProjectionMode());

		onFrustumChange();
		onViewPortChange();
		onFrameChange();
	}

	@Override
	public String toString() {
		return ("BasicCamera: Location " + getLocation() + ", Direction " + getDirection() + ", Up " + getUp()
			+ ", Left " + getLeft() + ", Near " + getFrustumNear() + ", Far " + getFrustumFar() + ", Left "
			+ getFrustumLeft() + ", Right " + getFrustumRight() + ", Bottom " + getFrustumBottom() + ", Top " + getFrustumTop());
	}

	/**
	 * Apply the camera's modelview matrix using the given Renderer.
	 * 
	 * @param renderer
	 *            the Renderer to use.
	 */
	@Override
	protected void applyModelViewMatrix(final Renderer renderer) {
		_matrixBuffer.rewind();
		// Use doubles
		getModelViewMatrix().toDoubleBuffer(_matrixBuffer);
		_matrixBuffer.rewind();
		((JoglRendererDouble) renderer).setModelViewMatrix(_matrixBuffer);
	}

	/**
	 * Apply the camera's projection matrix using the given Renderer.
	 * 
	 * @param renderer
	 *            the Renderer to use.
	 */
	@Override
	protected void applyProjectionMatrix(final Renderer renderer) {
		_matrixBuffer.rewind();
		// Use doubles
		getProjectionMatrix().toDoubleBuffer(_matrixBuffer);
		_matrixBuffer.rewind();
		((JoglRendererDouble) renderer).setProjectionMatrix(_matrixBuffer);
	}

	/**
	 * @return the value of the bottom frustum plane.
	 */
	@Override
	public double getFrustumBottom() {
		return _frustumBottom * magFactor[magIndex];
	}

	/**
	 * @param frustumBottom
	 *            the new value of the bottom frustum plane.
	 */
	@Override
	public void setFrustumBottom(final double frustumBottom) {
		_frustumBottom = frustumBottom / magFactor[magIndex];
		onFrustumChange();
	}

	/**
	 * @return the value of the left frustum plane.
	 */
	@Override
	public double getFrustumLeft() {
		return _frustumLeft * magFactor[magIndex];
	}

	/**
	 * @param frustumLeft
	 *            the new value of the left frustum plane.
	 */
	@Override
	public void setFrustumLeft(final double frustumLeft) {
		_frustumLeft = frustumLeft / magFactor[magIndex];
		onFrustumChange();
	}

	/**
	 * @return frustumRight the value of the right frustum plane.
	 */
	@Override
	public double getFrustumRight() {
		return _frustumRight * magFactor[magIndex];
	}

	/**
	 * @param frustumRight
	 *            the new value of the right frustum plane.
	 */
	@Override
	public void setFrustumRight(final double frustumRight) {
		_frustumRight = frustumRight / magFactor[magIndex];
		onFrustumChange();
	}

	/**
	 * @return the value of the top frustum plane.
	 */
	@Override
	public double getFrustumTop() {
		return _frustumTop * magFactor[magIndex];
	}

	/**
	 * @param frustumTop
	 *            the new value of the top frustum plane.
	 */
	@Override
	public void setFrustumTop(final double frustumTop) {
		_frustumTop = frustumTop / magFactor[magIndex];
		onFrustumChange();
	}

	/**
	 * Sets the frustum plane values of this camera using the given values.
	 * 
	 * @param near
	 * @param far
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 */
	@Override
	public void setFrustum(final double near, final double far, final double left, final double right,
		final double top, final double bottom) {
		// System.err.println("BasicCamera.setFrustum "+viewport+" "+near+" "+far+" "+left+" "+right+" "+top+" "+bottom);
		_frustumNear = near;
		_frustumFar = far;
		_frustumLeft = left / magFactor[magIndex];
		_frustumRight = right / magFactor[magIndex];
		_frustumTop = top / magFactor[magIndex];
		_frustumBottom = bottom / magFactor[magIndex];
		onFrustumChange();
	}

	/**
	 * Sets the frustum plane values of this camera using those of a given
	 * source camera
	 * 
	 * @param source
	 *            a source camera.
	 */
	@Override
	public void setFrustum(final Camera source) {
		magIndex = ((BasicCamera) source).getMagIndex();
		_frustumNear = source.getFrustumNear();
		_frustumFar = source.getFrustumFar();
		_frustumLeft = source.getFrustumLeft() / magFactor[magIndex];
		_frustumRight = source.getFrustumRight() / magFactor[magIndex];
		_frustumTop = source.getFrustumTop() / magFactor[magIndex];
		_frustumBottom = source.getFrustumBottom() / magFactor[magIndex];
		_fovY = source.getFovY();
		fovX = ((BasicCamera) source).getFovX();
		onFrustumChange();
	}

	/**
	 * Sets the frustum plane values of this camera using the given perspective
	 * values.
	 * 
	 * @param fovY
	 *            the full angle of view on the Y axis, in degrees.
	 * @param aspect
	 *            the aspect ratio of our view (generally in [0,1]). Often this
	 *            is canvas width / canvas height.
	 * @param near
	 *            our near plane value
	 * @param far
	 *            our far plane value
	 */
	@Override
	public void setFrustumPerspective(final double fovY, final double aspect, final double near, final double far) {
		if (Double.isNaN(aspect) || Double.isInfinite(aspect)) {
			// ignore.
			System.err.println("Invalid aspect given to setFrustumPerspective: " + aspect);
			return;
		}
		this.aspect = aspect;
		_fovY = fovY;
		fovX = fovY * aspect;
		final double h = Math.tan(_fovY * MathUtils.DEG_TO_RAD * .5) * near;
		final double w = h * aspect;
		_frustumLeft = -w / magFactor[magIndex];
		_frustumRight = w / magFactor[magIndex];
		_frustumBottom = -h / magFactor[magIndex];
		_frustumTop = h / magFactor[magIndex];
		_frustumNear = near;
		_frustumFar = far;
		onFrustumChange();
	}

	/**
	 * Sets the axes and location of the camera. Similar to
	 * {@link #setAxes(ReadOnlyMatrix3)}, but sets camera location as well.
	 * 
	 * @param location
	 *            the point position of the camera.
	 * @param axes
	 *            the orientation of the camera.
	 */
	@Override
	public void setFrame(final ReadOnlyVector3 location, final ReadOnlyMatrix3 axes) {
		// this assumes we are using the positive Z axis as the default
		// direction.
		// axes.getColumn(0, _left);
		// axes.getColumn(1, _up);
		// axes.getColumn(2, _direction);
		_location.set(location);

		// We use the traditional OpenGL default (negative Z axis) for
		// compatibility with other software. -LK
//		if (getProjectionMode() == ProjectionMode.Perspective) {
			_direction.set(0, 0, -1);
			axes.applyPost(_direction, _direction);
			_direction.normalizeLocal();
			_up.set(0, 1, 0);
			axes.applyPost(_up, _up);
			_up.normalizeLocal();
			_left.set(-1, 0, 0);
			axes.applyPost(_left, _left);
			_left.normalizeLocal();
//		}
		onFrameChange();
	}

	@Override
	public void resize(int width, int height) {
		if ((width == 0) || (height == 0)) {
			return;
		}
		super.resize(width, height);

		if (getProjectionMode() == ProjectionMode.Perspective) {
			setFrustumPerspective(getFovY(), aspect, getFrustumNear(), getFrustumFar());
		} else {
			double h = getHeight() / 2.0;
			double w = getWidth() / 2.0;
			setFrustum(0.00001, 1000.0, -w, w, h, -h);
			onFrustumChange();
		}
	}

	/**
	 * Set the center of rotation for the camera. If the point is null, compute
	 * it from the camera location, direction, and distance.
	 * 
	 * @param look
	 */
	public void setLookAt(ReadOnlyVector3 look) {
		lookAt.set(look);
	}

	/**
	 * Get the center of rotation point.
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getLookAt() {
		return (lookAt);
	}

	/**
	 * Get the distance to the center of rotation.
	 * 
	 * @return
	 */
	public double getDistanceToCoR() {
		return (_location.distance(lookAt));
	}

	/**
	 * Increment the zoom scale factor.
	 * 
	 * @param val
	 */
	public void magnify(int val) {
		int m = magIndex + val;
		if (m < 0) {
			m = 0;
		} else if (m >= magFactor.length) {
			m = magFactor.length - 1;
		}
		setMagnification(m);
	}

	public void magnify(double m) {
		for (int i = 0; i < magFactor.length; ++i) {
			if (m < magFactor[i]) {
				setMagnification(i);
				return;
			}
		}
		setMagnification(magFactor.length - 1);
	}

	public double getMagnification() {
		return (magFactor[magIndex]);
	}

	/**
	 * Set the zoom scale factor.
	 * 
	 * @param zoom
	 */
	public void setMagnification(int m) {

		if (magIndex != m) {
			_frustumLeft *= magFactor[magIndex];
			_frustumRight *= magFactor[magIndex];
			_frustumBottom *= magFactor[magIndex];
			_frustumTop *= magFactor[magIndex];
			magIndex = m;
			_frustumLeft /= magFactor[magIndex];
			_frustumRight /= magFactor[magIndex];
			_frustumBottom /= magFactor[magIndex];
			_frustumTop /= magFactor[magIndex];
			onFrustumChange();
		}
	}

	/**
	 * Get the zoom scale factor.
	 * 
	 * @return
	 */
	public int getMagIndex() {
		return (magIndex);
	}

	/**
	 * Get the aspect ratio.
	 * 
	 * @return
	 */
	public double getAspect() {
		return (aspect);
	}

	/**
	 * Get the field of view for the X axis.
	 * 
	 * @return
	 */
	public double getFovX() {
		return (fovX);
	}

	/**
	 * Determine if a spatial will be culled by the camera.
	 * 
	 * @param spat
	 * @return
	 */
	public boolean isCulled(Spatial spat) {
		final CullHint cm = spat.getSceneHints().getCullHint();
		if (cm == CullHint.Always) {
			return (true);
		} else if (cm == CullHint.Never) {
			return (false);
		} else if (cm == CullHint.Dynamic) {
			final int state = getPlaneState();
			boolean culled = contains(spat.getWorldBound()) == Camera.FrustumIntersect.Outside;
			setPlaneState(state);
			return (culled);
		} else {
			return (false);
		}
	}

	/**
	 * Checks a bounding volume against the planes of this camera's frustum and
	 * returns if it is completely or partially inside.
	 * 
	 * @param bound
	 *            the bound to check for culling
	 * @return true or false
	 */
	public boolean inFrustum(final BoundingVolume bound) {
		if (bound == null) {
			return false;
		}
		for (int i = 0; i < FRUSTUM_PLANES; ++i) {
			switch (bound.whichSide(_worldPlane[i])) {
			case Inside:
				return (false);
			case Outside:
				break;
			case Neither:
				return (true);
			}
		}

		return true;
	}

	/**
	 * Set the aspect ratio. Recompute the fovY value.
	 * 
	 * @param aspect
	 */
	public void setAspect(double aspect) {
		this.aspect = aspect;
		_fovY = fovX / aspect;
	}

	/**
	 * Set the field of view for the X axis. Recompute the field of view for the
	 * Y axis.
	 * 
	 * @param fovX
	 */
	public void setFovX(double fovX) {
		this.fovX = fovX;
		_fovY = fovX / aspect;
	}

	/**
	 * Set the clipping planes based on the given bounding sphere.
	 * 
	 * @param bs
	 */
	private void setClippingPlanes(BoundingSphere bs) {
		double r = bs.getRadius();
		double distance = getDistanceToCoR();
		if (distance <= r) {
			farPlane = MathUtil.distanceToSphere(bs, _location, _direction);
			nearPlane = Math.pow(10, Math.ceil(Math.log10(distance))) / 100;
		} else {
			nearPlane = distance - r + 0.00001;
			farPlane = nearPlane + 3 * r;
		}
		if (onFoot) {
			if (nearPlane > 1)
				nearPlane = 1;
		}
	}
	
	public void setOnFoot(boolean onFoot) {
		this.onFoot = onFoot;
	}

	/**
	 * Set the frustum based on the given bounding sphere.
	 * 
	 * @param bs
	 */
	public void setFrustum(BoundingSphere bs) {
		setClippingPlanes(bs);
		double xRadius = 1;
		double yRadius = 1;
		if (getProjectionMode() == Camera.ProjectionMode.Parallel) {
			xRadius = bs.getRadius();
			yRadius = xRadius;
			if (aspect < 1) {
				yRadius = xRadius / aspect;
			} else {
				xRadius = yRadius * aspect;
			}
		} else {
			yRadius = tanFOV() * nearPlane;
			xRadius = yRadius * aspect;
		}
		if ((xRadius == 0) || (yRadius == 0)) {
			return;
		}
		double frustumNear = nearPlane;
		if (frustumNear < 0.0001) {
			frustumNear = 0.0001;
		}
		setFrustum(frustumNear, farPlane, -xRadius, xRadius, yRadius, -yRadius);
	}

	/**
	 * Get the viewport in OpenGL terms.
	 * 
	 * @return
	 */
	public int[] getViewport() {
		viewport[0] = (int) (_viewPortLeft * _width);
		viewport[1] = (int) (_viewPortBottom * _height);
		viewport[2] = (int) ((_viewPortRight - _viewPortLeft) * _width);
		viewport[3] = (int) ((_viewPortTop - _viewPortBottom) * _height);
		return (viewport);
	}

	/**
	 * Get the tangent of the field of view on the Y axis
	 * 
	 * @return
	 */
	public final float tanFOV() {
		return ((float) Math.tan(Math.toRadians(_fovY * 0.5)));
	}

	/**
	 * Get the degrees per pixel adjusted by zoom.
	 * 
	 * @return degrees
	 */
	public final double degreesPerPixel() {
		double degPerPix = _fovY / _height;
		return (degPerPix / magFactor[magIndex]);
	}

	/**
	 * Get the pixel size at a given point.
	 * 
	 * @param point
	 * @param applyZoom
	 * @return
	 */
	public double getPixelSizeAt(ReadOnlyVector3 point, boolean applyZoom) {
		// get the distance from the viewpoint to the given point
		double depth = getLocation().distance(point);

		// punt if its outside the clipping planes (the point is clipped anyway)
		if ((depth > _frustumFar) || (depth < _frustumNear)) {
			return (-1);
		}

		double hgt = getHeight();
		switch (getProjectionMode()) {
		case Parallel:
			hgt = _frustumTop * 2;
			if (!applyZoom) {
				hgt *= magFactor[magIndex];
			}
			break;
		case Perspective:
			// here the height of the frustum at the given point is proportional
			// to the height at the near plane
			// multiply by 2 since top is just half of the height
			hgt = _frustumTop;
			if (!applyZoom) {
				hgt *= magFactor[magIndex];
			}
			hgt = 2 * depth * hgt / _frustumNear;
			break;
		case Custom:
			break;
		}

		// return units/pixel
		return (hgt / getHeight());
	}
}
