package gov.nasa.arc.dert.landscape;

import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.util.ColorMap;
import gov.nasa.arc.dert.util.ColorMapListener;
import gov.nasa.arc.dert.util.MathUtil;

import java.nio.FloatBuffer;
import java.util.Properties;

import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.util.geom.BufferUtils;

/**
 * A layer that provides derivatives of the height map as color maps.
 * Derivatives include elevation contour map, slope map, and aspect map. Colors
 * are applied via a texture map color map. Texture coordinates determine which
 * part of the color map texture is applied at a given vertex in the landscape
 * mesh.
 *
 */
public class DerivativeLayer extends Layer implements ColorMapListener {

	public static String defaultColorMapName;

	public static enum DerivativeType {
		Elevation, Slope, Aspect
	}

	// Type of derivative
	private DerivativeType type;

	// Source of height map data
	private RasterLayer dataSource;

	// The texture holding the color map
	private Texture2D colorMapTexture;

	// The color map
	private ColorMap colorMap;

	// Information about this layer
	private LayerInfo layerInfo;

	// Work field for aspect calculation
	private Vector2 work = new Vector2();

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param layerInfo
	 * @param source
	 */
	public DerivativeLayer(DerivativeType type, LayerInfo layerInfo, RasterLayer source) {
		super(layerInfo);
		this.type = type;
		this.layerInfo = layerInfo;
		dataSource = source;
		numLevels = dataSource.numLevels;
		numTiles = dataSource.numTiles;
		bytesPerTile = (dataSource.tileWidth + 1) * (dataSource.tileLength + 1) * 8;
	}

	@Override
	public void dispose() {
		// nothing here
	}

	@Override
	public QuadTreeTile getTile(String key) {
		return (dataSource.getTile(key));
	}

	@Override
	public Properties getProperties() {
		return (dataSource.getProperties());
	}

	@Override
	public Texture getTexture(String key, Texture store) {
		if (colorMapTexture == null) {
			initColormap();
		}
		return (colorMapTexture);
	}

	/**
	 * Get the color map.
	 * 
	 * @return
	 */
	public ColorMap getColorMap() {
		if (colorMapTexture == null) {
			initColormap();
		}
		return (colorMap);
	}

	/**
	 * Get the type of this derivative layer
	 * 
	 * @return
	 */
	public DerivativeType getDerivativeType() {
		return (type);
	}

	private void initColormap() {
		if (colorMap == null) {
			switch (type) {
			case Elevation:
				colorMap = new ColorMap(layerInfo.colorMapName, layerName, dataSource.getMinimumValue()[0],
					dataSource.getMaximumValue()[0], layerInfo.minimum, layerInfo.maximum, layerInfo.gradient);
				colorMap.addListener(this);
				break;
			case Slope:
				colorMap = new ColorMap(layerInfo.colorMapName, layerName, 0, 90, layerInfo.minimum, layerInfo.maximum,
					layerInfo.gradient);
				colorMap.addListener(this);
				break;
			case Aspect:
				colorMap = new ColorMap(layerInfo.colorMapName, layerName, 0, 90, layerInfo.minimum, layerInfo.maximum,
					layerInfo.gradient);
				colorMap.addListener(this);
				break;
			}
			if (colorMap == null) {
				throw new IllegalStateException("Error loading color map " + layerInfo.colorMapName + ".");
			}
			layerInfo.colorMap = colorMap;
		}
		colorMapTexture = colorMap.getTexture();
	}

	/**
	 * The color map changed.
	 */
	@Override
	public void mapChanged(ColorMap cMap) {
		World.getInstance().getLandscape().markDirty(DirtyType.RenderState);
	}

	/**
	 * The range of the color map changed.
	 */
	@Override
	public void rangeChanged(ColorMap cMap) {
		World.getInstance().getLandscape().markDirty(DirtyType.RenderState);
	}

	/**
	 * Create the color map texture coordinates for a tile mesh.
	 * 
	 * @param mesh
	 * @param textureUnit
	 */
	public void createColorMapTextureCoords(Mesh mesh, int textureUnit) {
		FloatBuffer vertex = mesh.getMeshData().getVertexBuffer();
		FloatBuffer normals = mesh.getMeshData().getNormalBuffer();
		FloatBuffer colors = mesh.getMeshData().getColorBuffer();
		FloatBuffer texCoords = mesh.getMeshData().getTextureBuffer(textureUnit);
		if (texCoords == null) {
			texCoords = BufferUtils.createFloatBuffer(colors.limit() / 2);
		}
		int dataSize = colors.limit() / 4;
		Vector2 coord = new Vector2();
		Vector3 normal = new Vector3();
		int k = 0;
		for (int i = 0; i < dataSize; ++i) {
			k = i * 2;
			float alpha = colors.get(i * 4 + 3);
			float z = 0;
			if (alpha != 0) {
				switch (type) {
				case Elevation:
					z = vertex.get(i * 3 + 2);
					colorMap.getTextureCoordinate(z, coord);
					texCoords.put(k, coord.getXf()).put(k + 1, coord.getYf());
					break;
				case Slope:
					normal.set(normals.get(i * 3), normals.get(i * 3 + 1), normals.get(i * 3 + 2));
					z = (float) MathUtil.getSlopeFromNormal(normal);
					colorMap.getTextureCoordinate(z, coord);
					texCoords.put(k, coord.getXf()).put(k + 1, coord.getYf());
					break;
				case Aspect:
					normal.set(normals.get(i * 3), normals.get(i * 3 + 1), normals.get(i * 3 + 2));
					z = (float) MathUtil.getAspectFromNormal(normal, work);
					colorMap.getTextureCoordinate(z, coord);
					texCoords.put(k, coord.getXf()).put(k + 1, coord.getYf());
					break;
				}
			} else {
				texCoords.put(k, 0).put(k + 1, -1);
			}
		}
		k += 2;
		texCoords.limit(k);
		texCoords.rewind();
		mesh.getMeshData().setTextureBuffer(texCoords, textureUnit);
	}

}
