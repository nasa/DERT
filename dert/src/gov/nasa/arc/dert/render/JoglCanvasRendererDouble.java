package gov.nasa.arc.dert.render;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.jogl.CapsUtil;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;

/**
 * Extends the Ardor3D JoglCanvasRenderer to use a JoglRendererDouble and
 * initialize a BasicScene.
 *
 */
public class JoglCanvasRendererDouble extends JoglCanvasRenderer {

	/**
	 * Constructor
	 * 
	 * @param scene
	 * @param useDebug
	 */
	public JoglCanvasRendererDouble(final Scene scene, boolean useDebug) {
		super(scene, useDebug, new CapsUtil(), true);
	}

	@Override
	public void init(final DisplaySettings settings, final boolean doSwap) {
		super.init(settings, doSwap);
		makeCurrentContext();
		_renderer = new JoglRendererDouble();
		((BasicScene) _scene).init(this);
		releaseCurrentContext();
	}

}
