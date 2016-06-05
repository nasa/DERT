package gov.nasa.arc.dert.render;

import gov.nasa.arc.dert.state.State;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Panel;

import com.ardor3d.framework.Updater;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.util.ReadOnlyTimer;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

/**
 * Provides a heavy weight window for the SceneCanvas.
 *
 */
public class SceneCanvasPanel extends Panel implements Updater {

	// The SceneCanvas for this panel
	protected SceneCanvas canvas;

	// The Ardor3D scene
	protected BasicScene scene;

	// The Ardor3D CanvasRenderer
	protected JoglCanvasRendererDouble canvasRenderer;

	// This panel has been initialized
	protected boolean initialized;

	// Listener for JOGL OpenGL events
	protected GLEventListener listener;

	// The state object associated with this panel
	protected State state;

	/**
	 * Constructor
	 * 
	 * @param width
	 * @param height
	 * @param scene
	 * @param mainCanvas
	 */
	public SceneCanvasPanel(int width, int height, BasicScene scene, boolean mainCanvas) {

		// create the CanvasRenderer and SceneCanvas
		canvasRenderer = new JoglCanvasRendererDouble(scene, false);
		canvas = SceneCanvas.createSceneCanvas(width, height, false, canvasRenderer, mainCanvas);
		canvas.setFocusable(true);

		// add the GLEventListener to the SceneCanvas
		listener = new GLEventListener() {

			@Override
			public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {
				resizeCanvas(width, height);
			}

			@Override
			public void init(GLAutoDrawable glautodrawable) {
				initialize();
			}

			@Override
			public void dispose(GLAutoDrawable glautodrawable) {
				// nothing here
			}

			@Override
			public void display(GLAutoDrawable glautodrawable) {
				SceneCanvasPanel.this.scene.needsRender.set(true);
			}
		};
		canvas.addGLEventListener(listener);
		setLayout(new BorderLayout());
		add(canvas, BorderLayout.CENTER);
		this.scene = scene;
		setPreferredSize(new Dimension(width, height));
	}

	/**
	 * Initialize this Panel
	 */
	public void initialize() {
		canvas.init();
		if (!initialized) {
			SceneFramework.getInstance().getFrameHandler().addCanvas(canvas);
			SceneFramework.getInstance().getFrameHandler().addUpdater(SceneCanvasPanel.this);
			initialized = true;
		}
	}

	/**
	 * Dispose of this panel
	 */
	public void dispose() {
		SceneFramework.getInstance().getFrameHandler().removeCanvas(canvas);
		SceneFramework.getInstance().getFrameHandler().removeUpdater(SceneCanvasPanel.this);
		canvas.removeGLEventListener(listener);
		remove(canvas);
	}

	@Override
	public void init() {
	}

	@Override
	public void update(ReadOnlyTimer timer) {
		scene.update(timer);
	}

	public void resizeCanvas(int width, int height) {
		scene.resize(width, height);
	}

	public BasicScene getScene() {
		return (scene);
	}

	public SceneCanvas getCanvas() {
		return (canvas);
	}

	public Renderer getRenderer() {
		return (canvasRenderer.getRenderer());
	}

	public void setState(State state) {
		this.state = state;
	}
	
	public void enableFrameGrab(String grabFilePath) {
		canvasRenderer.enableFrameGrab(grabFilePath, 0, 0, scene.getWidth(), scene.getHeight());
	}
}
