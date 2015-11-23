package gov.nasa.arc.dert.raster;

import java.io.IOException;

/**
 * Defines the methods for accessing a raster file.
 *
 */
public interface RasterFile {

	/**
	 * Designation of data type for raster file data.
	 * 
	 * @author lkeelyme
	 *
	 */
	public static enum DataType {
		Byte, UnsignedByte, Integer, UnsignedInteger, Short, UnsignedShort, Float, Double, Long, Unknown
	}

	/**
	 * Open the raster file with read (r), write (w), or append (a) access.
	 * 
	 * @param access
	 */
	public boolean open(String access);

	/**
	 * Close the raster file.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException;

	/**
	 * Get the raster file path.
	 * 
	 * @return the path
	 */
	public String getFilePath();

	/**
	 * Get the raster file name.
	 * 
	 * @return the name
	 */
	public String getFileName();

	/**
	 * Get the raster width in pixels.
	 * 
	 * @return the width
	 */
	public int getRasterWidth();

	/**
	 * Get the raster length (height) in pixels.
	 * 
	 * @return the length
	 */
	public int getRasterLength();

	/**
	 * Get the type of the raster data.
	 * 
	 * @return the type
	 */
	public DataType getDataType();

	/**
	 * Get the number of samples per pixel.
	 * 
	 * @return samples
	 */
	public int getSamplesPerPixel();

	/**
	 * Get the minimum sample value.
	 * 
	 * @return array of samplesPerPixel values
	 */
	public double[] getMinimumSampleValue();

	/**
	 * Get the maximum sample value.
	 * 
	 * @return array of samplesPerPixel values
	 */
	public double[] getMaximumSampleValue();

	/**
	 * Get the value that represents no data.
	 * 
	 * @return missing value
	 */
	public float getMissingValue();

	/**
	 * Set the value that represents no data.
	 * 
	 * @param missing
	 *            value
	 */
	public void setMissingValue(float missing);

	/**
	 * Get information about the projection used for this raster file.
	 * 
	 * @return projection info object
	 */
	public ProjectionInfo getProjectionInfo();

	/**
	 * Load data from raster file into a float array, converting data.
	 * 
	 * @param buffer
	 */
	public void load(Raster raster) throws IOException;

	/**
	 * Load data from raster file as an RGBA image, to be used as a texture map.
	 * 
	 * @param buffer
	 */
	public void loadRGBA(Raster raster) throws IOException;

	/**
	 * Load data from raster file as height map.
	 * 
	 * @param buffer
	 */
	public void loadHeightMap(Raster raster) throws IOException;

	/**
	 * Load data from raster file as height map.
	 * 
	 * @param buffer
	 */
	public void loadGray(Raster raster) throws IOException;

}
