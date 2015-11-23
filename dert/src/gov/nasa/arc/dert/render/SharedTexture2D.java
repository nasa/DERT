package gov.nasa.arc.dert.render;

import com.ardor3d.image.Texture2D;
import com.ardor3d.renderer.RenderContext;

/**
 * Provides an Ardor3D texture that can be shared among multiple GLContexts.
 *
 */
public class SharedTexture2D extends Texture2D {

	protected int textureId;

	/**
	 * @param glContext
	 *            the object representing the OpenGL context this texture
	 *            belongs to. See {@link RenderContext#getGlContextRep()}
	 * @return the texture id of this texture in the given context. If the
	 *         texture is not found in the given context, 0 is returned.
	 */
	@Override
	public int getTextureIdForContext(final Object glContext) {
		return textureId;
	}

	/**
	 * @param glContext
	 *            the object representing the OpenGL context this texture
	 *            belongs to. See {@link RenderContext#getGlContextRep()}
	 * @return the texture id of this texture in the given context as an Integer
	 *         object. If the texture is not found in the given context, a 0
	 *         integer is returned.
	 */
	@Override
	public Integer getTextureIdForContextAsInteger(final Object glContext) {
		return new Integer(textureId);
	}

	/**
	 * Sets the id for this texture in regards to the given OpenGL context.
	 * 
	 * @param glContext
	 *            the object representing the OpenGL context this texture
	 *            belongs to. See {@link RenderContext#getGlContextRep()}
	 * @param textureId
	 *            the texture id of this texture. To be valid, this must be
	 *            greater than 0.
	 * @throws IllegalArgumentException
	 *             if textureId is less than or equal to 0.
	 */
	@Override
	public void setTextureIdForContext(final Object glContext, final int textureId) {
		this.textureId = textureId;
	}

	@Override
	public void setDirty() {
		super.setDirty();
		textureId = 0;
	}

}
