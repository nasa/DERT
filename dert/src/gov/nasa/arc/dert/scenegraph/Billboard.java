package gov.nasa.arc.dert.scenegraph;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.image.Texture;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.extension.BillboardNode;
import com.ardor3d.scenegraph.hint.TextureCombineMode;
import com.ardor3d.scenegraph.shape.Quad;

/**
 * An extension of the Ardor3D BillboardNode that displays a textured quad that
 * is screen-aligned.
 *
 */
public class Billboard extends BillboardNode {

	private Quad quad;
	private double xSize, ySize;

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public Billboard(String name) {
		super(name);
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param texture
	 */
	public Billboard(String name, Texture texture) {
		super(name);
		getSceneHints().setCastsShadows(false);
		setAlignment(BillboardAlignment.ScreenAligned);
		float width = texture.getImage().getWidth();
		float height = texture.getImage().getHeight();
		ySize = 1;
		xSize = 1;
		if (height > width) {
			xSize = width / height;
		} else {
			ySize = height / width;
		}
		xSize *= 1.5;
		ySize *= 1.5;
		quad = new Quad("_quad", xSize, ySize);
		setTexture(texture);
		quad.getSceneHints().setTextureCombineMode(TextureCombineMode.Replace);
		quad.getSceneHints().setCastsShadows(false);
		quad.setTranslation(0, ySize / 2, 0);
		quad.setModelBound(new BoundingBox());
		quad.updateModelBound();
		attachChild(quad);
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
		quad.setRenderState(ts);
		quad.markDirty(DirtyType.RenderState);
	}

	/**
	 * Get the texture width
	 * 
	 * @return
	 */
	public double getWidth() {
		return (xSize);
	}

	/**
	 * Get the texture height
	 * 
	 * @return
	 */
	public double getHeight() {
		return (ySize);
	}

}
