package gov.nasa.arc.dert.scenegraph;

import gov.nasa.arc.dert.viewpoint.BasicCamera;

import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera.ProjectionMode;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.hint.CullHint;

/**
 * An extension of RasterText that changes scale with the distance from the
 * viewpoint.
 *
 */
public class ViewDependentText extends RasterText {

	protected final Vector3 look = new Vector3();
	protected final Vector3 left = new Vector3();
	protected final Matrix3 rot = new Matrix3();
	protected boolean autoScale, autoHide;
	protected Vector3 location = new Vector3();
	protected double oldScale;
	protected boolean flag;

	public ViewDependentText(String name, String textString, boolean autoScale, boolean autoHide) {
		this(name, textString, autoScale, autoHide, AlignType.Left);
	}

	public ViewDependentText(String name, String textString, boolean autoScale, boolean autoHide, AlignType alignment) {
		super(name, textString, alignment);
		this.autoScale = autoScale;
		this.autoHide = autoHide;
	}

	@Override
	public synchronized void draw(final Renderer r) {
		_worldTransform.setRotation(rot);
		_worldTransform.setScale(_localTransform.getScale());
		super.draw(r);
	}

	/**
	 * Update size according to camera location.
	 * 
	 * @param camera
	 */
	public void update(BasicCamera camera) {
		left.set(camera.getLeft()).negateLocal();
		look.set(camera.getDirection()).negateLocal();
		rot.fromAxes(left, camera.getUp(), look);

		location.set(camera.getLocation());
		location.negateLocal().addLocal(_worldTransform.getTranslation());
		double z = camera.getDirection().dot(location);
		if ((z < camera.getFrustumNear()) || (z > camera.getFrustumFar())) {
			return;
		}

		double hZ; // height of the screen in world coordinates at Z
		if (camera.getProjectionMode() == ProjectionMode.Parallel) {
			hZ = camera.getFrustumTop();
		} else {
			hZ = z * camera.getFrustumTop() / camera.getFrustumNear();
		}

		double screenScale = 2 * hZ / camera.getHeight(); // maintain uniform
															// size in screen
															// coords

		if (autoHide) {
			if (1 / screenScale < 0.015) {
				getSceneHints().setCullHint(CullHint.Always);
			} else {
				getSceneHints().setCullHint(cullHint);
			}
		}
		if (autoScale) {
			double scale = screenScale * scaleFactor;
			if (Math.abs(scale - oldScale) > 0.0000001) {
				setScaleFactor(screenScale);
				oldScale = scale;
			}
		}

	}

	public void setAutoScale(boolean autoScale) {
		this.autoScale = autoScale;
	}

}
