package gov.nasa.arc.dert.landscape;

import gov.nasa.arc.dert.io.TileSource;
import gov.nasa.arc.dert.raster.ProjectionInfo;
import gov.nasa.arc.dert.raster.RasterFile.DataType;
import gov.nasa.arc.dert.render.SharedTexture2D;
import gov.nasa.arc.dert.util.ImageUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.Console;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

import com.ardor3d.image.Image;
import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.image.util.ImageUtils;
import com.ardor3d.util.TextureKey;

/**
 * Provides a layer based on a raster file (for example, a heightmap or
 * orthoimage).
 *
 */
public class RasterLayer extends Layer {

	// The source of the raster data tiles
	private TileSource dataSource;

	// padding value
	protected float fillValue;

	// value range
	protected double[] minimumValue, maximumValue;

	// tile dimensions, tile dimensions+1, tile size in bytes
	protected int tileWidth, tileLength, tileWidth1, tileLength1;

	// information about the raster projection
	protected ProjectionInfo projInfo;

	// dimensions of raster used to create this layer
	protected int rasterWidth, rasterLength;

	// physical dimensions of the layer in meters
	protected double physicalWidth, physicalLength;

	// number of bytes per pixel
	protected int numBytes;

	// helper image utility
	protected ImageUtil imageUtil;

	/**
	 * Constructor
	 * 
	 * @param layerInfo
	 * @param source
	 * @throws IOException
	 */
	public RasterLayer(LayerInfo layerInfo, TileSource source) throws IOException {
		super(layerInfo);
		imageUtil = new ImageUtil();
		initialize(source, layerName);
	}

	protected void initialize(TileSource dataSource, String layerName) throws IOException {

		this.dataSource = dataSource;
		Properties properties = dataSource.getProperties(layerName);

		projInfo = new ProjectionInfo();
		projInfo.loadFromProperties(properties);
		numLevels = StringUtil.getIntegerValue(properties, "NumberOfLevels", true, 0, true);
		tileWidth = StringUtil.getIntegerValue(properties, "TileWidth", true, 0, true);
		tileWidth1 = tileWidth + 1;
		tileLength = StringUtil.getIntegerValue(properties, "TileLength", true, 0, true);
		tileLength1 = tileLength + 1;
		numTiles = StringUtil.getIntegerValue(properties, "NumberOfTiles", true, 0, true);
		switch (layerType) {
		case none:
		case footprint:
		case viewshed:
		case derivative:
			break;
		case elevation:
		case field:
			numBytes = 4;
			break;
		case colorimage:
			numBytes = 4;
			break;
		case grayimage:
			numBytes = 1;
			break;
		}

		bytesPerTile = (tileWidth1) * (tileLength1) * numBytes;
		rasterWidth = tileWidth * numTiles;
		rasterLength = tileLength * numTiles;
		physicalWidth = rasterWidth * projInfo.scale[0];
		physicalLength = rasterLength * projInfo.scale[1];
		minimumValue = StringUtil.getDoubleArray(properties, "MinimumValue", null, true);
		maximumValue = StringUtil.getDoubleArray(properties, "MaximumValue", null, true);
		fillValue = (float) StringUtil.getDoubleValue(properties, "EdgeFillValue", false, 0.0, false);
		Console.getInstance().println("\nProperties for " + layerName + ":");
		Console.getInstance().println("Layer Type = " + layerType);
		Console.getInstance().println("Number of Levels = " + numLevels);
		Console.getInstance().println("Tile Width = " + tileWidth);
		Console.getInstance().println("Tile Length = " + tileLength);
		Console.getInstance().println("Minimum Value = " + minimumValue[0]);
		Console.getInstance().println("Maximum Value = " + maximumValue[0]);
		Console.getInstance().println("Edge Fill Z-Value = " + fillValue);
		Console.getInstance().println(projInfo.toString());
	}

	/**
	 * Get the raster projection information
	 * 
	 * @return
	 */
	public ProjectionInfo getProjectionInfo() {
		return (projInfo);
	}

	/**
	 * Get the raster tile width
	 * 
	 * @return
	 */
	public int getTileWidth() {
		return (tileWidth);
	}

	/**
	 * Get the raster tile length
	 * 
	 * @return
	 */
	public int getTileLength() {
		return (tileLength);
	}

	/**
	 * Get the minimum value array
	 * 
	 * @return
	 */
	public double[] getMinimumValue() {
		return (minimumValue);
	}

	/**
	 * Get the maximum value array
	 * 
	 * @return
	 */
	public double[] getMaximumValue() {
		return (maximumValue);
	}

	/**
	 * Get the raster padding value
	 * 
	 * @return
	 */
	public float getFillValue() {
		if (Float.isNaN(fillValue)) {
			return ((float) minimumValue[0]);
		}
		return (fillValue);
	}

	/**
	 * Get the width of the full raster
	 * 
	 * @return
	 */
	public int getRasterWidth() {
		return (rasterWidth);
	}

	/**
	 * Get the length of the full raster
	 * 
	 * @return
	 */
	public int getRasterLength() {
		return (rasterLength);
	}

	protected QuadTreeTile readTile(String key) {
		try {
			DataType dataType = DataType.Byte;
			switch (layerType) {
			case none:
			case footprint:
			case viewshed:
			case derivative:
				break;
			case elevation:
			case field:
				dataType = DataType.Float;
				break;
			case colorimage:
				dataType = DataType.UnsignedInteger;
				break;
			case grayimage:
				dataType = DataType.UnsignedByte;
				break;
			}
			QuadTreeTile tile = dataSource.getTile(layerName, key, dataType);
			return (tile);
		} catch (Exception e) {
			System.out.println("Unable to read tile, see log.");
			e.printStackTrace();
			return (null);
		}
	}

	/**
	 * Get the raster tile source
	 * 
	 * @return
	 */
	public TileSource getTileSource() {
		return (dataSource);
	}

	/**
	 * Get a tile with the given key (file path)
	 */
	@Override
	public QuadTreeTile getTile(String key) {
		QuadTreeTile tile = readTile(key);
		return (tile);
	}

	/**
	 * Get the properties for this layer
	 */
	@Override
	public Properties getProperties() {
		return (dataSource.getProperties(layerName));
	}

	protected Image getTextureImage(String key) {
		final QuadTreeTile t = getTile(key);
		if (t == null) {
			return (null);
		}
		if (t.getImage() == null) {
			t.setImage(imageUtil.convertToArdor3DImage(t.raster, numBytes * 8, t.dataType, t.width, t.length));
		}
		return (t.getImage());
	}

	/**
	 * Given the tile key, get a tile as a texture for this layer
	 */
	@Override
	public Texture getTexture(String key, Texture store) {
		Image image = getTextureImage(key);
		if (image == null) {
			return (null);
		}
		if (image.getDataFormat() == ImageDataFormat.RGBA) {
			ByteBuffer byteBuffer = image.getData(0);
			int n = byteBuffer.limit();
			for (int i = 3; i < n; i += 4) {
				if (byteBuffer.get(i) == 0) {
					byteBuffer.put(i, (byte) 255);
				}
			}
		}
		Texture texture = new SharedTexture2D();
		texture.setTextureKey(TextureKey.getKey(null, false, TextureStoreFormat.GuessNoCompressedFormat, layerName
			+ key, Texture.MinificationFilter.BilinearNoMipMaps));
		texture.setImage(image);
		texture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
		texture.setTextureStoreFormat(ImageUtils.getTextureStoreFormat(texture.getTextureKey().getFormat(),
			texture.getImage()));
		return (texture);
	}
}
