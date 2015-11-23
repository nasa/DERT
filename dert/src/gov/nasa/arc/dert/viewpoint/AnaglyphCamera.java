package gov.nasa.arc.dert.viewpoint;

import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.ColorMaskState;
import com.ardor3d.renderer.state.RenderState.StateType;

/**
 * Provides a stereo camera. Adapted from Ardor3D StereoCamera.
 *
 */
public class AnaglyphCamera extends BasicCamera {

	private final BasicCamera _leftCamera;
	private final BasicCamera _rightCamera;

	private double _focalDistance = 1;
	private double _eyeSeparation = _focalDistance / 30;

	private ColorMaskState redColorMask, cyanColorMask;

	/**
	 * Constructor
	 */
	public AnaglyphCamera() {
		this(100, 100);
	}

	/**
	 * Constructor
	 * 
	 * @param width
	 * @param height
	 */
	public AnaglyphCamera(final int width, final int height) {
		super(width, height);
		_leftCamera = new BasicCamera(width, height);
		_rightCamera = new BasicCamera(width, height);
		redColorMask = new ColorMaskState();
		redColorMask.setAll(true);
		redColorMask.setBlue(false);
		redColorMask.setGreen(false);
		cyanColorMask = new ColorMaskState();
		cyanColorMask.setAll(true);
		cyanColorMask.setRed(false);
	}

	/**
	 * Constructor
	 * 
	 * @param camera
	 */
	public AnaglyphCamera(final BasicCamera camera) {
		super(camera);
		_leftCamera = new BasicCamera(camera);
		_rightCamera = new BasicCamera(camera);
		redColorMask = new ColorMaskState();
		redColorMask.setAll(true);
		redColorMask.setBlue(false);
		redColorMask.setGreen(false);
		cyanColorMask = new ColorMaskState();
		cyanColorMask.setAll(true);
		cyanColorMask.setRed(false);
	}

	@Override
	public void resize(final int width, final int height) {
		super.resize(width, height);
		_leftCamera.resize(width, height);
		_rightCamera.resize(width, height);
	}

	/**
	 * Set up the cameras
	 */
	public void setupLeftRightCameras() {
		// Set viewport:
		_leftCamera.setViewPort(0, 1, 0, 1);
		_rightCamera.setViewPort(0, 1, 0, 1);

		// Set frustum:
		final double aspectRatio = (getWidth() / (double) getHeight());
		final double halfView = getFrustumNear() * MathUtils.tan(_fovY * MathUtils.DEG_TO_RAD / 2);

		final double top = halfView;
		final double bottom = -halfView;
		final double horizontalShift = 0.5 * _eyeSeparation * getFrustumNear() / _focalDistance;

		// LEFT:
		{
			final double left = -aspectRatio * halfView + horizontalShift;
			final double right = aspectRatio * halfView + horizontalShift;

			_leftCamera.setFrustum(getFrustumNear(), getFrustumFar(), left, right, top, bottom);
		}

		// RIGHT:
		{
			final double left = -aspectRatio * halfView - horizontalShift;
			final double right = aspectRatio * halfView - horizontalShift;

			_rightCamera.setFrustum(getFrustumNear(), getFrustumFar(), left, right, top, bottom);
		}
	}

	/**
	 * Update the camera frames when the viewpoint moves
	 */
	public void updateLeftRightCameraFrames() {
		// update camera frame
		final Vector3 rightDir = Vector3.fetchTempInstance();
		final Vector3 work = Vector3.fetchTempInstance();
		rightDir.set(getDirection()).crossLocal(getUp()).multiplyLocal(_eyeSeparation / 2.0);
		_leftCamera.setFrame(getLocation().subtract(rightDir, work), getLeft(), getUp(), getDirection());
		_rightCamera.setFrame(getLocation().add(rightDir, work), getLeft(), getUp(), getDirection());
		Vector3.releaseTempInstance(work);
		Vector3.releaseTempInstance(rightDir);
	}

	/**
	 * Switch to left camera for drawing
	 * 
	 * @param r
	 */
	public void switchToLeftCamera(final Renderer r) {
		ContextManager.getCurrentContext().enforceState(redColorMask);
		_leftCamera.update();
		_leftCamera.apply(r);
	}

	/**
	 * Switch to right camera for drawing
	 * 
	 * @param r
	 */
	public void switchToRightCamera(final Renderer r) {
		ContextManager.getCurrentContext().enforceState(cyanColorMask);
		_rightCamera.update();
		_rightCamera.apply(r);
	}

	/**
	 * @return the leftCamera
	 */
	public BasicCamera getLeftCamera() {
		return _leftCamera;
	}

	/**
	 * @return the rightCamera
	 */
	public BasicCamera getRightCamera() {
		return _rightCamera;
	}

	/**
	 * @return the focalDistance
	 */
	public double getFocalDistance() {
		return _focalDistance;
	}

	/**
	 * @param focalDistance
	 *            the focalDistance to set
	 */
	public void setFocalDistance(final double focalDistance) {
		_focalDistance = focalDistance;
	}

	/**
	 * @return the eyeSeparation
	 */
	public double getEyeSeparation() {
		return _eyeSeparation;
	}

	/**
	 * @param eyeSeparation
	 *            the eyeSeparation to set
	 */
	public void setEyeSeparation(final double eyeSeparation) {
		_eyeSeparation = eyeSeparation;
	}

	public void finish() {
		ContextManager.getCurrentContext().clearEnforcedState(StateType.ColorMask);
	}

}
