package gov.nasa.arc.dert.render;

import gov.nasa.arc.dert.camera.BasicCamera;

import java.nio.ByteBuffer;

import com.ardor3d.image.Texture;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.ContextCapabilities;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.jogl.JoglTextureRenderer;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.geom.BufferUtils;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.glu.GLU;

/**
 * This is an extension of Ardor3D's JoglTextureRenderer that filters objects
 * that shouldn't cast a shadow and can save a copy of the color buffer.
 *
 */

public class BasicTextureRenderer extends JoglTextureRenderer {

	// The camera used for rendering the texture
	protected BasicCamera basicCamera;

	// Flags
	protected boolean isShadow, saveRGBA;

	// Memory used
	protected int size;

	// RGBA buffer
	protected ByteBuffer rgbaBuffer;

	public BasicTextureRenderer(final int width, final int height, final int depthBits, final int samples,
		final Renderer parentRenderer, final ContextCapabilities caps) {
		super(width, height, depthBits, samples, parentRenderer, caps);

		size = _width * _height;
		basicCamera = new BasicCamera(_width, _height, 90, _width / (double) _height);
		basicCamera.setFrustum(1.0f, 1000.0f, -0.50f, 0.50f, 0.50f, -0.50f);
		final Vector3 loc = new Vector3(0.0f, 0.0f, 0.0f);
		final Vector3 left = new Vector3(-1.0f, 0.0f, 0.0f);
		final Vector3 up = new Vector3(0.0f, 1.0f, 0.0f);
		final Vector3 dir = new Vector3(0.0f, 0f, -1.0f);
		basicCamera.setFrame(loc, left, up, dir);
	}

	@Override
	public Camera getCamera() {
		return (basicCamera);
	}

	/**
	 * This texture render is in shadow
	 * 
	 * @param isShadow
	 */
	public void setIsShadow(boolean isShadow) {
		this.isShadow = isShadow;
	}
	
	public void clear(final Texture tex, final int clear) {
//        final GL gl = GLContext.getCurrentGL();
//        gl.glClearColor(1, 1, 1, 1);

        setupForSingleTexDraw(tex);

        if (_samples > 0 && _supportsMultisample) {
            setMSFBO();
        }

        switchCameraIn(clear);
        switchCameraOut();

        if (_samples > 0 && _supportsMultisample) {
            blitMSFBO();
        }

        takedownForSingleTexDraw(tex);
        
//        final ReadOnlyColorRGBA bgColor = _parentRenderer.getBackgroundColor();
//        gl.glClearColor(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), bgColor.getAlpha());
	}

	@Override
	public void render(final Spatial toDraw, final Texture tex, final int clear) {
		if (toDraw == null) {
			return;
		}
		if (isShadow && !toDraw.getSceneHints().isCastsShadows()) {
			return;
		}
		((JoglRendererDouble) _parentRenderer).setInShadow(isShadow);
		_parentRenderer.clearBuffers(Renderer.BUFFER_COLOR_AND_DEPTH);
		super.render(toDraw, tex, clear);
		((JoglRendererDouble) _parentRenderer).setInShadow(false);
	}

	@Override
	protected void switchCameraOut() {
		_parentRenderer.flushFrame(false);
		if (saveRGBA) {
			saveRGBABuffer();
		}
		super.switchCameraOut();
	}

	/**
	 * Save the contents of the color buffer after rendering
	 * 
	 * @param saveRGBA
	 */
	public void setSaveRGBA(boolean saveRGBA) {
		this.saveRGBA = saveRGBA;
	}

	protected void saveRGBABuffer() {
		final GL gl = GLU.getCurrentGL();
		if (rgbaBuffer == null) {
			rgbaBuffer = BufferUtils.createByteBuffer(size * 4);
		}
		gl.glReadPixels(0, 0, _width, _height, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, rgbaBuffer);
		rgbaBuffer.rewind();
		saveRGBA = false;
	}

	/**
	 * Get the saved color buffer
	 * 
	 * @return
	 */
	public ByteBuffer getRGBABuffer() {
		return (rgbaBuffer);
	}
}
