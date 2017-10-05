package gov.nasa.arc.dert.landscape;

import gov.nasa.arc.dert.io.TileSource;
import gov.nasa.arc.dert.terrain.Layer;
import gov.nasa.arc.dert.terrain.LayerInfo;
import gov.nasa.arc.dert.terrain.QuadTreeTile;
import gov.nasa.arc.dert.terrain.RasterLayer;
import gov.nasa.arc.dert.util.ColorMap;
import gov.nasa.arc.dert.util.ColorMapListener;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Properties;

import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.math.Vector2;
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
public class FieldLayer extends Layer implements ColorMapListener {

	public static String defaultColorMapName;

	// Source of height map data
	private RasterLayer dataSource;

	// The texture holding the color map
	private Texture2D colorMapTexture;

	// The color map
	private ColorMap colorMap;
	
	// Tile dimensions
	private int tileWidth, tileLength;

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param layerInfo
	 * @param source
	 */
	public FieldLayer(LayerInfo layerInfo, TileSource source) throws IOException {
		super(layerInfo);
		dataSource = new RasterLayer(layerInfo, source);
		tileWidth = dataSource.getTileWidth();
		tileLength = dataSource.getTileLength();
		this.layerInfo = layerInfo;
		numLevels = dataSource.getNumberOfLevels();
		numTiles = dataSource.getNumberOfTiles();
		bytesPerTile = (dataSource.getTileWidth() + 1) * (dataSource.getTileLength() + 1) * 8;
		colorMap = layerInfo.colorMap;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (colorMap != null)
			colorMap.removeListener(this);
		dataSource.dispose();
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

	private void initColormap() {
		if (colorMap == null) {
			colorMap = new ColorMap(layerInfo.colorMapName, layerName, dataSource.getMinimumValue()[0],
				dataSource.getMaximumValue()[0], layerInfo.minimum, layerInfo.maximum, layerInfo.gradient);
			colorMap.addListener(this);
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
		Landscape.getInstance().markDirty(DirtyType.RenderState);
	}

	/**
	 * The range of the color map changed.
	 */
	@Override
	public void rangeChanged(ColorMap cMap) {
		Landscape.getInstance().markDirty(DirtyType.RenderState);
	}
	
	public void createColorMapTextureCoords(String key, Mesh mesh, int tUnit) {
		FloatBuffer texCoords = getFloatTexCoords(key);
		if (texCoords != null)
			mesh.getMeshData().setTextureBuffer(texCoords, tUnit);
	}
	
	private FloatBuffer getFloatTexCoords(String key) {
		Vector2 coord = new Vector2();
		QuadTreeTile tile = dataSource.getTile(key);
		FloatBuffer data = tile.raster.asFloatBuffer();
		int tWidth = tileWidth + 1;
		int tLength = tileLength + 1;
		int size = tWidth * tLength * 2;
		FloatBuffer texCoords = BufferUtils.createFloatBuffer(size);
		for (int r = 0; r < tLength; ++r) {
			for (int c = 0; c < tWidth; ++c) {
				double val = data.get();
				colorMap.getTextureCoordinate(val, coord);
				texCoords.put(coord.getXf()).put(coord.getYf());
			}
		}
		texCoords.limit(size);
		texCoords.rewind();
		return (texCoords);
	}


}
