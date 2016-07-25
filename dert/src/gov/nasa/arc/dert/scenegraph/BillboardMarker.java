package gov.nasa.arc.dert.scenegraph;

import gov.nasa.arc.dert.scenegraph.Text.AlignType;
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

	/**
	 * Constructor
	 */
	public BillboardMarker(String name, ReadOnlyVector3 point, double size, Color color, boolean labelVisible,
		boolean pinned) {
		super(name, point, size, color, labelVisible, pinned);
		setSize(size);
		getSceneHints().setCastsShadows(false);
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
		if (billboard != null) {
			contents.detachChild(billboard);
		}
		billboard = new Billboard("_billboard", nominalTexture);
		billboard.attachChild(label);
		contents.attachChild(billboard);
		SpatialUtil.setPickHost(billboard, this);
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
			billboard.setTexture(highlightTexture);
			materialState.setEmissive(MaterialFace.FrontAndBack, highlightColorRGBA);
		} else {
			billboard.setTexture(nominalTexture);
			materialState.setEmissive(MaterialFace.FrontAndBack, colorRGBA);
		}
	}

	@Override
	protected void createLabel(boolean labelVisible) {
		label = new RasterText("_label", labelStr, AlignType.Center);
		label.setScaleFactor((float) (0.75 * size));
		label.setColor(labelColorRGBA);
		label.setTranslation(0, 1.5, 0);
		label.setVisible(labelVisible);
	}

}
