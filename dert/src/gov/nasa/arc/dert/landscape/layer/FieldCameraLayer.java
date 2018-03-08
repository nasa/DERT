package gov.nasa.arc.dert.landscape.layer;

import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.io.QuadTreeTile;
import gov.nasa.arc.dert.landscape.layer.LayerInfo.LayerType;
import gov.nasa.arc.dert.landscape.quadtree.QuadKey;
import gov.nasa.arc.dert.landscape.quadtree.QuadTreeMesh;
import gov.nasa.arc.dert.render.Viewshed;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.util.ImageUtil;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Properties;

import com.ardor3d.image.Image;
import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.image.PixelDataType;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix4;
import com.ardor3d.math.type.ReadOnlyMatrix4;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

/**
 * A layer that represents the footprint or viewshed of a FieldCamera. The
 * footprint is a projected texture. The viewshed is a projected depth texture
 * and is the opposite of a shadow map.
 *
 */
public class FieldCameraLayer extends Layer {

	// bias matrix for footprint
	private final static ReadOnlyMatrix4 BIAS = new Matrix4(0.5, 0.0, 0.0, 0.0, 0.0, 0.5, 0.0, 0.0, 0.0, 0.0, 0.5, 0.0,
		0.5, 0.5, 0.5, 1.0);

	// The field camera
	private FieldCamera fieldCamera;

	// the texture
	private Texture texture;

	// the texture unit for the layer
	private int textureUnit;

	// flag to indicate this is a viewshed
	private boolean viewshedEnabled;

	// color fields
	private float[] fColor;
	private Color color;

	// projection matrix
	private Matrix4 matrix;

	// Creates the projected texture for the viewshed
	private Viewshed viewshed;

	/**
	 * Constructor
	 * 
	 * @param layerInfo
	 * @param textureUnit
	 */
	public FieldCameraLayer(LayerInfo layerInfo, int textureUnit) {
		super(layerInfo);
		this.textureUnit = textureUnit;
		this.viewshedEnabled = layerInfo.type == LayerType.viewshed;
		color = new Color(0, 0, 0, 0);
		setColor(color);
	}

	/**
	 * Enable/disable the viewshed
	 * 
	 * @param viewshedEnabled
	 */
	public void setViewShed(boolean viewshedEnabled) {
		this.viewshedEnabled = viewshedEnabled;
		texture = null;
		if (!viewshedEnabled && (viewshed != null)) {
			viewshed.dispose();
			viewshed = null;
		}
	}

	/**
	 * Is the viewshed enabled
	 * 
	 * @return
	 */
	public boolean isViewshed() {
		return (viewshedEnabled);
	}

	@Override
	public void dispose() {
		super.dispose();
		Landscape.getInstance().getTextureState().setTexture(null, textureUnit);
		Landscape.getInstance().markDirty(DirtyType.RenderState);
	}

	@Override
	public QuadTreeTile getTile(QuadKey key) {
		return (null);
	}

	@Override
	public Properties getProperties() {
		return (null);
	}

	@Override
	public Texture getTexture(QuadKey key, QuadTreeMesh mesh, Texture store) {
		return (null);
	}

	private Texture createFootprintTexture() {
		// create the texture for the footprint
		int textureSize = ImageUtil.getMaxTextureRendererSize() / 4; // smaller
																		// increases
																		// performance
		int width = textureSize;
		int height = textureSize;
		int s = width * height;
		ByteBuffer buffer = BufferUtils.createByteBuffer(s * 4);
		for (int i = 0; i < s; ++i) {
			buffer.put((byte) color.getRed()).put((byte) color.getGreen()).put((byte) color.getBlue())
				.put((byte) color.getAlpha());
		}
		buffer.flip();
		buffer.rewind();
		ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>(1);
		list.add(buffer);
		Image image = new Image(ImageDataFormat.RGBA, PixelDataType.UnsignedByte, width, height, list, null);

		Texture texture = TextureManager.loadFromImage(image, Texture.MinificationFilter.BilinearNoMipMaps,
			TextureStoreFormat.RGBA8);
		matrix = new Matrix4();
		texture.setTextureMatrix(matrix);
		// texture.setHasBorder(true);
		texture.setWrap(Texture.WrapMode.BorderClamp);
		texture.setEnvironmentalMapMode(Texture2D.EnvironmentalMapMode.EyeLinear);
		texture.setApply(Texture2D.ApplyMode.Combine);
		texture.setBorderColor(ColorRGBA.BLACK_NO_ALPHA);
		return (texture);
	}

	/**
	 * Prerender this layer.
	 */
	@Override
	public void prerender(final Renderer renderer) {
		if (fieldCamera == null) {
			fieldCamera = (FieldCamera) World.getInstance().getTools().getChild(layerInfo.name);
		}
		Color instCol = fieldCamera.getColor();
		// color changed, remake the texture
		if (!instCol.equals(color)) {
			setColor(instCol);
			if (!viewshedEnabled) {
				texture = null;
			}
		}
		// viewshed
		if (viewshedEnabled) {
			// render the viewshed
			if (texture == null) {
				viewshed = new Viewshed(fieldCamera.getSyntheticCameraNode().getCamera(), textureUnit);
				texture = viewshed.getTexture();
			}
			fieldCamera.prerender(renderer, viewshed);
		}
		// footprint
		else {
			if (texture == null) {
				texture = createFootprintTexture();
			}
			BasicCamera camera = fieldCamera.getSyntheticCameraNode().getCamera();
			matrix.set(camera.getModelViewProjectionMatrix());
			matrix.multiplyLocal(BIAS);
			texture.setTextureMatrix(matrix);
		}
		Landscape.getInstance().getTextureState().setTexture(texture, textureUnit);
		Landscape.getInstance().markDirty(DirtyType.RenderState);
	}

	/**
	 * Get the color for this layer
	 * 
	 * @return
	 */
	public float[] getColor() {
		return (fColor);
	}

	/**
	 * Set the color for this layer.
	 * 
	 * @param instColor
	 */
	public void setColor(Color instColor) {
		color = instColor;
		if (fColor == null) {
			fColor = new float[4];
		}
		color.getComponents(fColor);
	}

}
