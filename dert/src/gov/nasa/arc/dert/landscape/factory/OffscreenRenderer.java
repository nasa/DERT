package gov.nasa.arc.dert.landscape.factory;

import gov.nasa.arc.dert.util.ImageUtil;

import java.nio.ByteBuffer;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.jogl.CapsUtil;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Camera.ProjectionMode;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.jogl.JoglContextCapabilities;
import com.ardor3d.renderer.jogl.JoglRenderContext;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.geom.jogl.DirectNioBuffersSet;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;

/**
 * Provides an offscreen renderer using pBuffers. Adapted from Ardor3D example.
 *
 */
public class OffscreenRenderer {

	// Orthographic camera used to render each image
	private Camera camera;

	// Background color
	private ColorRGBA backgroundColor = new ColorRGBA(1, 1, 1, 1);

	// Image dimensions
	private int width = 0, height = 0;

	// Renderer
	private final Renderer renderer;

	// OpenGL settings for the drawable
	private final DisplaySettings settings;

	// The drawable
	private GLOffscreenAutoDrawable offscreenDrawable;

	// OpenGL context
	private GLContext context;

	private DirectNioBuffersSet directNioBuffersSet;

	// RGBA buffer
	private ByteBuffer rgbaBuffer;
	private int size;

	public OffscreenRenderer(final DisplaySettings settings, final Renderer renderer, ReadOnlyColorRGBA backgroundColor) {
		this.renderer = renderer;
		this.settings = settings;
		this.backgroundColor = new ColorRGBA(backgroundColor);

		width = settings.getWidth();
		height = settings.getHeight();
		size = width * height * settings.getColorDepth() / 8;
		System.out.println("Created " + width + "x" + height + " offscreen renderer.");

		camera = new Camera(width, height);
		camera.setProjectionMode(ProjectionMode.Parallel);
		camera.setFrustum(1.0, 1000, -0.50, 0.50, 0.50, -0.50);
		final Vector3 loc = new Vector3(0.0, 0.0, 0);
		final Vector3 left = new Vector3(-1.0, 0.0, 0.0);
		final Vector3 up = new Vector3(0.0, 1.0, 0.0);
		final Vector3 dir = new Vector3(0.0, 0, -1.0);
		camera.setFrame(loc, left, up, dir);
		initPbuffer();
	}

	protected void doDraw(final Spatial spat) {
		// Override parent's last frustum test to avoid accidental incorrect
		// cull
		if (spat.getParent() != null) {
			spat.getParent().setLastFrustumIntersection(Camera.FrustumIntersect.Intersects);
		}

		spat.onDraw(renderer);
	}

	/**
	 * Retrieve the camera this renderer is using.
	 * 
	 * @return the camera this renderer is using.
	 */
	public Camera getCamera() {
		return camera;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	/**
	 * Render the image
	 * 
	 * @param toDrawB
	 * @param clear
	 */
	public void render(final Spatial toDrawB, final int clear) {
		try {
			if (offscreenDrawable == null) {
				initPbuffer();
			}

			context.makeCurrent();
			ContextManager.switchContext(context);

			// clear the scene
			if (clear != 0) {
				final GL gl = GLContext.getCurrentGL();
				gl.glDisable(GL.GL_SCISSOR_TEST);
				renderer.clearBuffers(clear);
			}

			camera.update();
			camera.apply(renderer);

			doDraw(toDrawB);

			renderer.flushFrame(false);
			renderer.finishGraphics();

			saveRGBABuffer();

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	protected void saveRGBABuffer() {
		final GL gl = GLContext.getCurrentGL();
		if (rgbaBuffer == null) {
			rgbaBuffer = BufferUtils.createByteBuffer(size);
		} else {
			rgbaBuffer.limit(size);
		}
		rgbaBuffer.rewind();
		gl.glReadPixels(0, 0, width, height, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, rgbaBuffer);
		rgbaBuffer.rewind();
		ImageUtil.doSwap(rgbaBuffer);
		ImageUtil.doFlip(rgbaBuffer, width * 4, height);
	}

	public ByteBuffer getRGBABuffer() {
		return (rgbaBuffer);
	}

	private void initPbuffer() {

		try {
			if (offscreenDrawable != null) {
				context.destroy();
				offscreenDrawable.destroy();
				ContextManager.removeContext(offscreenDrawable.getContext());
			}

			// Make the GLPbuffer...
			CapsUtil capsUtil = new CapsUtil();
			final GLProfile profile = capsUtil.getProfile();
			final GLDrawableFactory fac = GLDrawableFactory.getFactory(profile);
			final GLCapabilities caps = new GLCapabilities(profile);
			caps.setHardwareAccelerated(true);
//			caps.setDoubleBuffered(true);
			caps.setRedBits(8);
			caps.setGreenBits(8);
			caps.setBlueBits(8);
			caps.setAlphaBits(8);
			caps.setDepthBits(24);
			caps.setNumSamples(settings.getSamples());
			caps.setSampleBuffers(settings.getSamples() != 0);
			caps.setStencilBits(settings.getStencilBits());
			caps.setDoubleBuffered(false);
			caps.setOnscreen(false);
			caps.setPBuffer(true);
			offscreenDrawable = fac.createOffscreenAutoDrawable(null, caps, null, width, height);
			context = offscreenDrawable.createContext(null);
			offscreenDrawable.setContext(context, false);
			context = offscreenDrawable.getContext();

			context.makeCurrent();

			if (directNioBuffersSet == null) {
				directNioBuffersSet = new DirectNioBuffersSet();
			}

			final JoglContextCapabilities contextCaps = new JoglContextCapabilities(offscreenDrawable.getGL(),
				directNioBuffersSet);
			ContextManager.addContext(context,
				new JoglRenderContext(context, contextCaps, ContextManager.getCurrentContext(), directNioBuffersSet));

		} catch (final Exception e) {
			e.printStackTrace();

			return;
		}

		try {
			context.makeCurrent();
			ContextManager.switchContext(context);
			final GL gl = GLContext.getCurrentGL();

			gl.glClearColor(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(),
				backgroundColor.getAlpha());
		} catch (final Exception e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * Dispose of resources
	 */
	public void cleanup() {
		if (offscreenDrawable == null)
			return;
		context.makeCurrent();
		ContextManager.switchContext(context);
		final GL gl = GLContext.getCurrentGL();
		gl.glFinish();
		context.destroy();
		offscreenDrawable.destroy();
		ContextManager.removeContext(offscreenDrawable.getContext());
	}

}
