package gov.nasa.arc.dert.scenegraph.text;

import java.awt.Font;
import java.awt.geom.Rectangle2D;

import com.ardor3d.renderer.ContextCapabilities;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.MeshData;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * A class for drawing text strings that uses a JOGL TextRenderer.
 *
 */
public class VectorText extends Text {

	protected final static int FONT_SIZE = 100;
	protected final static String FONT_NAME = "SansSerif";
	protected final static int FONT_TYPE = Font.BOLD;
	protected static TextRenderer textRenderer;
	
	static {
		textRenderer = new TextRenderer(new Font(FONT_NAME, FONT_TYPE, FONT_SIZE));
	}

	/**
	 * Constructor with default alignment
	 * 
	 * @param name
	 * @param textString
	 */
	public VectorText(String name, String textString) {
		this(name, textString, AlignType.Left);
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param textString
	 * @param alignment
	 */
	public VectorText(String name, String textString, AlignType alignment) {
		super(name, textString, alignment);
	}

	@Override
	protected double getTextWidth() {
		Rectangle2D rect = textRenderer.getBounds(textString);
		return(rect.getWidth());
	}

	@Override
	protected double getTextHeight() {
		Rectangle2D rect = textRenderer.getBounds(textString);
		return(rect.getHeight());
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
		gl2.glPushMatrix();		
        textRenderer.begin3DRendering();
        textRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        textRenderer.draw3D(textString, position.getXf(), position.getYf(), position.getZf(), (float)scaleFactor);
        textRenderer.end3DRendering();
		gl2.glPopMatrix();
	}

	@Override
	protected void renderVBO(final Renderer r, final MeshData meshData, final int primcount) {
		if (!textString.isEmpty()) {

			final GL2 gl2 = GLContext.getCurrentGL().getGL2();
			gl2.glPushMatrix();
	        textRenderer.begin3DRendering();
	        textRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	        textRenderer.draw3D(textString, position.getXf(), position.getYf(), position.getZf(), (float)scaleFactor);
	        textRenderer.end3DRendering();
			gl2.glPopMatrix();
		}
	}

}
