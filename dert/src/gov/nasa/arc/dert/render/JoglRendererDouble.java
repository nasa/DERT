package gov.nasa.arc.dert.render;

import java.nio.Buffer;
import java.nio.DoubleBuffer;

import com.ardor3d.math.Matrix4;
import com.ardor3d.math.type.ReadOnlyTransform;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.jogl.JoglRenderer;
import com.ardor3d.renderer.jogl.state.record.JoglRendererRecord;
import com.ardor3d.scene.state.jogl.util.JoglRendererUtil;
import com.ardor3d.scenegraph.Renderable;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.geom.BufferUtils;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;

/**
 * Extends the Ardor3D JoglRenderer class to use doubles for transformations.
 *
 */
public class JoglRendererDouble extends JoglRenderer {

	// transformation matrix
	private final DoubleBuffer _transformBuffer = BufferUtils.createDoubleBuffer(16);
	private final Matrix4 _transformMatrix = new Matrix4();

	// rendering a shadow map
	private boolean inShadow;

	public void setModelViewMatrix(final DoubleBuffer matrix) {
		final JoglRendererRecord matRecord = (JoglRendererRecord) ContextManager.getCurrentContext()
			.getRendererRecord();
		synchronized (_transformMatrix) {
			matrix.rewind();
			JoglRendererUtil.switchMode(matRecord, GLMatrixFunc.GL_MODELVIEW);
			loadMatrix(matrix);
		}
	}

	public void setProjectionMatrix(final DoubleBuffer matrix) {
		final JoglRendererRecord matRecord = (JoglRendererRecord) ContextManager.getCurrentContext()
			.getRendererRecord();
		JoglRendererUtil.switchMode(matRecord, GLMatrixFunc.GL_PROJECTION);
		loadMatrix(matrix);
	}

	private void loadMatrix(final DoubleBuffer matrix) {
		GL gl = GLU.getCurrentGL();
		gl.getGL2().glLoadMatrixd(matrix);
	}

	@Override
	public boolean doTransforms(final ReadOnlyTransform transform) {
		final GL gl = GLU.getCurrentGL();
		// set world matrix
		if (!transform.isIdentity()) {
			synchronized (_transformMatrix) {

				// use a double buffer for better resolution

				transform.getGLApplyMatrix(_transformBuffer);

				final JoglRendererRecord matRecord = (JoglRendererRecord) ContextManager.getCurrentContext()
					.getRendererRecord();
				JoglRendererUtil.switchMode(matRecord, GLMatrixFunc.GL_MODELVIEW);
				gl.getGL2().glPushMatrix();
				gl.getGL2().glMultMatrixd(_transformBuffer);
				// gl.getGL2().glMultMatrixf(_transformBuffer);
				return true;
			}
		}
		return false;
	}

	public void setInShadow(boolean inShadow) {
		this.inShadow = inShadow;
	}

	@Override
	public void draw(final Renderable renderable) {
		if (inShadow) {
			if ((renderable instanceof Spatial) && !((Spatial) renderable).getSceneHints().isCastsShadows()) {
				return;
			}
		}
		super.draw(renderable);
	}

	/**
	 * Draw a background image
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param magnification
	 * @param format
	 * @param type
	 * @param pixels
	 */
	public void drawImage(float x, float y, int width, int height, float magnification, int format, int type,
		Buffer pixels) {
		GL gl = GLU.getCurrentGL();
		gl.getGL2().glWindowPos2f(x, y);
		gl.getGL2().glPixelZoom(magnification, magnification);
		gl.getGL2().glDrawPixels(width, height, format, type, pixels);
		gl.getGL2().glFlush();
	}
}
