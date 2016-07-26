package gov.nasa.arc.dert.scenegraph;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.PickingHint;

/**
 * A base class for drawing text strings.
 *
 */
public abstract class Text extends Mesh {

	// Text alignment options
	public static enum AlignType {
		Left, Right, Center
	}

	// The text string
	protected String textString;

	// A scale for size (not used in RasterText)
	protected double scaleFactor;

	// The color
	protected ColorRGBA color = new ColorRGBA(1, 1, 1, 1);

	// The alignment
	protected AlignType alignment;

	// The location where the text begins
	protected Vector3 position;

	// For visibility
	protected CullHint cullHint = CullHint.Inherit;

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param textString
	 * @param alignment
	 */
	public Text(String name, String textString, AlignType alignment) {
		super(name);
		if (textString == null) {
			textString = "";
		}
		this.textString = textString;
		this.alignment = alignment;
		scaleFactor = 1;
		double w = getWidth(textString) / 2.0;
		double h = getHeight(textString) / 2.0;
		switch (alignment) {
		case Left:
			position = new Vector3();
			_modelBound = new BoundingBox(new Vector3(w, h, 0), w, h, 1);
			break;
		case Center:
			position = new Vector3(-w, 0, 0);
			_modelBound = new BoundingBox(Vector3.ZERO, w, h, 1);
			break;
		case Right:
			position = new Vector3(-w * 2, 0, 0);
			_modelBound = new BoundingBox(new Vector3(-w, h, 0), w, h, 1);
			break;
		}
		updateWorldTransform(true);
		updateWorldBound(true);
		getSceneHints().setCastsShadows(false);
		getSceneHints().setCullHint(cullHint);
		getSceneHints().setLightCombineMode(LightCombineMode.Off);
		getSceneHints().setPickingHint(PickingHint.Pickable, false);

		GLSLShaderObjectsState glsl = new GLSLShaderObjectsState();
		glsl.setEnabled(false);
		setRenderState(glsl);
		BlendState bs = new BlendState();
		bs.setBlendEnabled(false);
		setRenderState(bs);
		TextureState ts = new TextureState();
		ts.setEnabled(false);
		setRenderState(ts);

		setDefaultColor(color);
	}

	protected abstract double getWidth(String str);

	protected abstract double getHeight(String str);

	/**
	 * Set the color of the text
	 * 
	 * @param col
	 */
	public void setColor(ReadOnlyColorRGBA col) {
		color.set(col);
		setDefaultColor(color);
	}

	/**
	 * Set the scale factor of the text
	 * 
	 * @param scaleFactor
	 */
	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
		double w = getWidth(textString) / 2.0;
		switch (alignment) {
		case Left:
			position = new Vector3();
			break;
		case Center:
			position = new Vector3(-w, 0, 0);
			break;
		case Right:
			position = new Vector3(-w * 2, 0, 0);
			break;
		}
	}

	/**
	 * Set the text string
	 * 
	 * @param textString
	 */
	public void setText(String textString) {
		if (textString == null) {
			textString = "";
		}
		this.textString = textString;
		double w = getWidth(textString) / 2.0;
		double h = getHeight(textString) / 2.0;
		switch (alignment) {
		case Left:
			position = new Vector3();
			((BoundingBox)_modelBound).setXExtent(w);
			((BoundingBox)_modelBound).setYExtent(h);
			((BoundingBox)_modelBound).setCenter(w, h, 0);
			break;
		case Center:
			position = new Vector3(-w, 0, 0);
			((BoundingBox)_modelBound).setXExtent(w);
			((BoundingBox)_modelBound).setYExtent(h);
			((BoundingBox)_modelBound).setCenter(Vector3.ZERO);
			break;
		case Right:
			position = new Vector3(-w * 2, 0, 0);
			((BoundingBox)_modelBound).setXExtent(w);
			((BoundingBox)_modelBound).setYExtent(h);
			((BoundingBox)_modelBound).setCenter(-w, h, 0);
			break;
		}
		markDirty(DirtyType.Bounding);
	}

	/**
	 * Get the text scale factor
	 * 
	 * @return
	 */
	public double getScaleFactor() {
		return (scaleFactor);
	}

	/**
	 * Get the text string
	 * 
	 * @return
	 */
	public String getText() {
		return (textString);
	}

	/**
	 * Get the text string width
	 * 
	 * @return
	 */
	public double getWidth() {
		return (getWidth(textString));
	}

	/**
	 * Get the text string height
	 * 
	 * @return
	 */
	public double getHeight() {
		return (getHeight(textString));
	}

	/**
	 * Set visibility
	 */
	@Override
	public void setVisible(boolean visible) {
		cullHint = visible ? CullHint.Inherit : CullHint.Always;
		getSceneHints().setCullHint(cullHint);
	}

	/**
	 * Get visibility
	 */
	@Override
	public boolean isVisible() {
		return (cullHint == CullHint.Inherit);
	}

}
