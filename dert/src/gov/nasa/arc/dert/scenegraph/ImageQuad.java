package gov.nasa.arc.dert.scenegraph;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.image.Texture;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.hint.TextureCombineMode;
import com.ardor3d.scenegraph.shape.Quad;

/**
 * A textured quadrangle.
 *
 */
public class ImageQuad extends Quad {
	
	private double scaleFactor = 1;

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public ImageQuad(String name) {
		super(name);
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param texture
	 */
	public ImageQuad(String name, Texture texture, double scaleFactor) {
		super(name, scaleFactor*texture.getImage().getWidth()/texture.getImage().getHeight(), scaleFactor);
		this.scaleFactor = scaleFactor;
		setTexture(texture);
		getSceneHints().setTextureCombineMode(TextureCombineMode.Replace);
		getSceneHints().setCastsShadows(false);
		setTranslation(0, scaleFactor/2, 0);
		setModelBound(new BoundingBox());
		updateModelBound();
		getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
	}

	/**
	 * Set the texture
	 * 
	 * @param texture
	 */
	public void setTexture(Texture texture) {
		final TextureState ts = new TextureState();
		ts.setEnabled(true);
		ts.setTexture(texture, 0);
		setRenderState(ts);
		resize(scaleFactor*texture.getImage().getWidth()/texture.getImage().getHeight(), scaleFactor);
	}

}
