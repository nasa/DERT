package gov.nasa.arc.dert.render;

import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.scene.World;

import com.ardor3d.image.Texture.DepthTextureCompareFunc;
import com.ardor3d.renderer.Camera;

/**
 * Provides a projected depth texture that displays the viewshed for a camera. A
 * viewshed is the opposite of the a shadow map. Only the areas visible by the
 * camera are colored.
 *
 */
public class Viewshed extends ProjectedDepthTexture {

	private BasicCamera camera;

	/**
	 * Constructor
	 * 
	 * @param camera
	 * @param textureUnit
	 */
	public Viewshed(BasicCamera camera, int textureUnit) {
		super(World.getInstance(), World.getInstance(), DepthTextureCompareFunc.LessThanEqual, false, false);
		this.camera = camera;
	}

	/**
	 * Render the viewshed.
	 * 
	 * @param r
	 *            The renderer to use
	 */
	@Override
	public void updateProjection() {
		Camera tCam = textureRenderer.getCamera();
		tCam.setFrustumPerspective(camera.getFovY(), camera.getAspect(), camera.getFrustumNear(),
			camera.getFrustumFar()); // viewshed doesn't draw with near plane < 0.1
		tCam.setFrame(camera.getLocation(), camera.getLeft(), camera.getUp(), camera.getDirection());
		tCam.update();
		projectionMatrix.set(tCam.getModelViewProjectionMatrix());
		projectionMatrix.multiplyLocal(BIAS);
	}

}
