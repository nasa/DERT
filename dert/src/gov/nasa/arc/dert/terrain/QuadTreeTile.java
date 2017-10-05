package gov.nasa.arc.dert.terrain;

import gov.nasa.arc.dert.raster.RasterFile.DataType;

import java.nio.ByteBuffer;

import com.ardor3d.image.Image;

/**
 * Data structure to store raster tile data and metadata.
 *
 */
public class QuadTreeTile {

	// The raster data
	public ByteBuffer raster;

	// The image containing the data
	private Image image;

	// The key for the tile (file path)
	public String key;

	// The tile dimensions
	public int width, length;

	// The raster data type
	public DataType dataType;

	// The number of bytes per pixel
	public int bytesPerPixel;

	/**
	 * Constructor with a ByteBuffer
	 * 
	 * @param raster
	 * @param key
	 * @param width
	 * @param length
	 * @param dataType
	 * @param bytesPerPixel
	 */
	public QuadTreeTile(ByteBuffer raster, String key, int width, int length, DataType dataType, int bytesPerPixel) {
		this.raster = raster;
		this.key = key;
		this.width = width;
		this.length = length;
		this.dataType = dataType;
		this.bytesPerPixel = bytesPerPixel;
	}

	/**
	 * Constructor with an Image
	 * 
	 * @param image
	 * @param key
	 * @param dataType
	 */
	public QuadTreeTile(Image image, String key, DataType dataType) {
		this.raster = image.getData(0);
		this.key = key;
		this.width = image.getWidth();
		this.length = image.getHeight();
		this.dataType = dataType;
		this.bytesPerPixel = image.getDataType().getBytesPerPixel(1);
		this.image = image;
	}

	/**
	 * This tile is empty
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return ((raster == null) && (image == null));
	}

	/**
	 * Set the image for this tile, raster becomes null.
	 * 
	 * @param image
	 */
	public final synchronized void setImage(Image image) {
		this.image = image;
		raster = null;
	}

	/**
	 * Get the image for this tile
	 * 
	 * @return
	 */
	public final synchronized Image getImage() {
		return (image);
	}
}
