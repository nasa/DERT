package gov.nasa.arc.dert.scenegraph;

import gov.nasa.arc.dert.util.SpatialUtil;

import java.awt.Color;

import com.ardor3d.image.Texture;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.MaterialState.MaterialFace;

/**
 * Provides a Marker object with a billboard.
 */
public class BillboardMarker extends Marker {

	protected Texture nominalTexture, highlightTexture;
	protected ColorRGBA highlightColorRGBA;
	protected ImageQuad imageQuad;

	/**
	 * Constructor
	 */
	public BillboardMarker(String name, ReadOnlyVector3 point, double size, double zOff, Color color, boolean labelVisible,
		boolean pinned) {
		super(name, point, size, zOff, color, labelVisible, pinned);
		setSize(size);
		getSceneHints().setCastsShadows(false);
		label.setTranslation(0, 1.6, 0);
	}

	/**
	 * Set the texture
	 * 
	 * @param nominalTexture
	 * @param highlightTexture
	 */
	public void setTexture(Texture nominalTexture, Texture highlightTexture) {
		this.nominalTexture = nominalTexture;
		this.highlightTexture = highlightTexture;
		if (imageQuad != null) {
			billboard.detachChild(imageQuad);
		}
		imageQuad = new ImageQuad("_billboard", nominalTexture, 1.5);
		billboard.attachChild(imageQuad);
		SpatialUtil.setPickHost(imageQuad, this);
		updateWorldBound(true);
		scaleShape(scale);
	}

	@Override
	protected void setMaterialState() {
		materialState.setAmbient(MaterialFace.FrontAndBack, colorRGBA);
		materialState.setEmissive(MaterialFace.FrontAndBack, colorRGBA);
		materialState.setDiffuse(MaterialFace.FrontAndBack, ColorRGBA.BLACK);
		highlightColorRGBA = new ColorRGBA(colorRGBA.getRed() + 0.5f, colorRGBA.getGreen() + 0.5f,
			colorRGBA.getBlue() + 0.5f, colorRGBA.getAlpha());
	}

	@Override
	protected void enableHighlight(boolean enable) {
		if (enable) {
			imageQuad.setTexture(highlightTexture);
			materialState.setEmissive(MaterialFace.FrontAndBack, highlightColorRGBA);
		} else {
			imageQuad.setTexture(nominalTexture);
			materialState.setEmissive(MaterialFace.FrontAndBack, colorRGBA);
		}
	}

}
