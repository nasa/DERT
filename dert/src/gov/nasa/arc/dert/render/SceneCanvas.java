package gov.nasa.arc.dert.render;

import java.nio.IntBuffer;
import java.util.concurrent.CountDownLatch;

import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.util.geom.BufferUtils;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;

/**
 * Provides a JOGL rendering surface for DERT. Adapted from Ardor3D example.
 *
 */
public class SceneCanvas extends GLCanvas implements Canvas {

	public static String openGLVendor;
	public static String openGLRenderer;
	public static String openGLVersion;
	public static int depthBits = 24;
	public static int maxFrameBufferSize;

	// Ardor3D CanvasRenderer
	protected JoglCanvasRenderer canvasRenderer;

	// OpenGL display settings
	private final DisplaySettings settings;

	protected static String osName = System.getProperty("os.name").toLowerCase().substring(0, 3);

	// this SceneCanvas has been initialized
	private boolean initialized = false;

	// this is the main rendering surface for the application
	private boolean mainCanvas;

	// a shared OpenGL context
	protected static GLContext sharedContext;

	/**
	 * Create a SceneCanvas. Try 32 bit color depth, then 24, if there is an
	 * error.
	 * 
	 * @param width
	 * @param height
	 * @param fullScreen
	 * @param canvasRenderer
	 * @param mainCanvas
	 * @return
	 */
	public static SceneCanvas createSceneCanvas(int width, int height, boolean fullScreen,
		JoglCanvasRenderer canvasRenderer, boolean mainCanvas) {
		GLCapabilities glCaps = new GLCapabilities(GLProfile.getDefault());
		if (glCaps.getAlphaBits() < 8) {
			glCaps.setAlphaBits(8);
		}
		if (glCaps.getRedBits() < 8) {
			glCaps.setRedBits(8);
		}
		if (glCaps.getGreenBits() < 8) {
			glCaps.setGreenBits(8);
		}
		if (glCaps.getBlueBits() < 8) {
			glCaps.setBlueBits(8);
		}
		int colorDepth = glCaps.getRedBits() + glCaps.getGreenBits() + glCaps.getBlueBits();
		if (glCaps.getDepthBits() < 32) {
			glCaps.setDepthBits(32);
		}
		DisplaySettings displaySettings = new DisplaySettings(width, height, colorDepth, 0, glCaps.getAlphaBits(),
			glCaps.getDepthBits(), glCaps.getStencilBits(), 0, fullScreen, glCaps.getStereo());
		SceneCanvas canvas = null;
		try {
			canvas = new SceneCanvas(displaySettings, glCaps, canvasRenderer, mainCanvas);
		} catch (Exception e32) {
			e32.printStackTrace();
			try {
				glCaps.setDepthBits(24);
				displaySettings = new DisplaySettings(width, height, colorDepth, 0, glCaps.getAlphaBits(),
					glCaps.getDepthBits(), glCaps.getStencilBits(), 0, fullScreen, glCaps.getStereo());
				canvas = new SceneCanvas(displaySettings, glCaps, canvasRenderer, mainCanvas);
			} catch (Exception e24) {
				e24.printStackTrace();
				throw new IllegalStateException("System is not configured for 24 or 32 bit graphics.");
			}
		}
		return (canvas);
	}

	/**
	 * Constructor
	 * 
	 * @param settings
	 * @param glCaps
	 * @param canvasRenderer
	 * @param mainCanvas
	 */
	public SceneCanvas(DisplaySettings settings, GLCapabilities glCaps, JoglCanvasRenderer canvasRenderer,
		boolean mainCanvas) {
		super(glCaps);
		this.mainCanvas = mainCanvas;
		if (sharedContext != null) {
			setSharedContext(sharedContext);
		}
		this.settings = settings;
		this.canvasRenderer = canvasRenderer;
		setAutoSwapBufferMode(false);
	}

	/**
	 * Initialize this SceneCanvas
	 */
	@Override
	public void init() {
		if (initialized) {
			return;
		}
		GLContext glContext = getContext();
		// this is the world view
		if (mainCanvas) {
			if (openGLVendor == null) {
				GL gl = glContext.getGL();
				openGLVendor = gl.glGetString(GL.GL_VENDOR);
				openGLRenderer = gl.glGetString(GL.GL_RENDERER);
				openGLVersion = gl.glGetString(GL.GL_VERSION);
				IntBuffer arg1 = BufferUtils.createIntBuffer(1);
				gl.glGetIntegerv(GL.GL_DEPTH_BITS, arg1);
				arg1.rewind();
				depthBits = arg1.get(0);
			}
			sharedContext = glContext;
		}
		canvasRenderer.setContext(glContext);
		canvasRenderer.init(settings, true);// true - do swap in renderer.
		initialized = true;
	}

	/**
	 * Draw the scene for this canvas
	 * 
	 * @param latch
	 */
	@Override
	public void draw(final CountDownLatch latch) {
		if (!initialized) {
			return;
		}

		if (isShowing()) {
			canvasRenderer.draw();
		}
		if (latch != null) {
			latch.countDown();
		}
	}

	/**
	 * Get the canvas renderer for this canvas
	 * 
	 * @return
	 */
	@Override
	public CanvasRenderer getCanvasRenderer() {
		return (canvasRenderer);
	}
}
