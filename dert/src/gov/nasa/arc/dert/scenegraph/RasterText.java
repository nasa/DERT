package gov.nasa.arc.dert.scenegraph;

import gov.nasa.arc.dert.viewpoint.BasicCamera;

import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera.ProjectionMode;
import com.ardor3d.renderer.ContextCapabilities;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.MeshData;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.gl2.GLUT;

/**
 * A class for drawing text strings that uses GLUT bitmap string function.
 *
 */
public class RasterText extends Text {

	public static int font = GLUT.BITMAP_HELVETICA_18;
	public static int fontHeight = 18;

	// A glut instance for rendering
	protected static GLUT glut;

	private final Vector3 look = new Vector3();
	private final Vector3 left = new Vector3();
	private final Matrix3 rot = new Matrix3();
//	private boolean autoHide = false, doHide;
	private Vector3 location = new Vector3();
	private double oldScale;
	
	static {
		glut = new GLUT();		
	}

	/**
	 * Constructor that defaults to left alignment
	 * 
	 * @param name
	 * @param textString
	 */
	public RasterText(String name, String textString) {
		this(name, textString, AlignType.Left);
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param textString
	 * @param alignment
	 */
	public RasterText(String name, String textString, AlignType alignment) {
		super(name, textString, alignment);
	}

	/**
	 * Set the font size
	 * 
	 * @param size
	 */
	public static void setFont(int size) {
		switch (size) {
		case 8:
			font = GLUT.BITMAP_8_BY_13;
			fontHeight = 8;
			break;
		case 9:
			font = GLUT.BITMAP_9_BY_15;
			fontHeight = 9;
			break;
		case 10:
			font = GLUT.BITMAP_HELVETICA_10;
			fontHeight = 10;
			break;
		case 12:
			font = GLUT.BITMAP_HELVETICA_12;
			fontHeight = 12;
			break;
		case 18:
			font = GLUT.BITMAP_HELVETICA_18;
			fontHeight = 18;
			break;
		case 24:
			font = GLUT.BITMAP_TIMES_ROMAN_24;
			fontHeight = 24;
			break;
		default:
			font = GLUT.BITMAP_HELVETICA_18;
			fontHeight = 18;
			break;
		}
	}

	@Override
	protected double getWidth(String str) {
		return (scaleFactor * glut.glutBitmapLength(font, str));
	}

	@Override
	protected double getHeight(String str) {
		return (scaleFactor * fontHeight);
	}

	@Override
	protected void renderArrays(final Renderer renderer, final MeshData meshData, final int primcount,
		final ContextCapabilities caps) {
		if (textString.isEmpty()) {
			return;
		}

		// Use arrays
		if (caps.isVBOSupported()) {
			renderer.unbindVBO();
		}

		final GL2 gl2 = GLContext.getCurrentGL().getGL2();
		gl2.glRasterPos3d(position.getX(), position.getY(), position.getZ());
		glut.glutBitmapString(font, textString);
	}

	@Override
	protected void renderVBO(final Renderer r, final MeshData meshData, final int primcount) {
		if (!textString.isEmpty()) {
			final GL2 gl2 = GLContext.getCurrentGL().getGL2();
			gl2.glRasterPos3d(position.getX(), position.getY(), position.getZ());
			glut.glutBitmapString(font, textString);
		}
	}

	@Override
	public synchronized void draw(final Renderer r) {
//		if (doHide)
//			return;
		update((BasicCamera)ContextManager.getCurrentContext().getCurrentCamera());
		_worldTransform.setRotation(rot);
		_worldTransform.setScale(_localTransform.getScale());
		super.draw(r);
	}

	/**
	 * Update position according to camera location.
	 * 
	 * @param camera
	 */
	private void update(BasicCamera camera) {
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

//		if (autoHide) {
//			doHide = (1 / screenScale < 0.5);
//		}
		double scale = screenScale * scaleFactor;
		if (Math.abs(scale - oldScale) > 0.0001) {
			setScaleFactor(screenScale);
			oldScale = scale;
		}
	}
}
