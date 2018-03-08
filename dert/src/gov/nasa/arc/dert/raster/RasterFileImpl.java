package gov.nasa.arc.dert.raster;

import gov.nasa.arc.dert.landscape.io.QuadTreeTile.DataType;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.StringUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Properties;

/**
 * Abstract base class representing a raster file.
 *
 */
public abstract class RasterFileImpl implements RasterFile {

	// File access, "r" for read, "w" for write, or "a" for append
	protected String access;

	// The file path
	protected String filePath;

	// Raster dimensions
	protected int rasterWidth, rasterLength;

	// This object is initialized
	protected boolean initialized;

	// Missing value
	protected float missing = Float.NaN;

	// Raster extrema
	protected double[] minimum, maximum;

	// Raster data type
	protected DataType dataType;

	// Data attributes
	protected int samplesPerPixel = 1, bytesPerSample = 1;

	// File properties
	protected Properties properties;

	// Byte order (big endian or little endian)
	protected ByteOrder byteOrder;
	
	protected float scalingFactor = 1f;

	/**
	 * Constructor.
	 * 
	 * @param filePath
	 *            the path to this raster file
	 * @param properties
	 *            dert properties
	 */
	public RasterFileImpl(String filePath, Properties properties) {
		this.filePath = filePath;
		this.properties = properties;
		byteOrder = ByteOrder.nativeOrder();
	}

	/**
	 * Open the RasterFile.
	 * 
	 * @param access
	 *            the access mode, "r" (reading), "w" (writing), or "a"
	 *            (appending)
	 */
	@Override
	public boolean open(String access) {
		this.access = access;
		if (access.equals("r") && !initialized) {
			return (initialize());
		}
		return (true);
	}

	protected abstract boolean initialize();

	/**
	 * Close the RasterFile. If the file was opened for writing or appending,
	 * keys and data will be flushed.
	 */
	@Override
	public void close() throws IOException {
	}

	/**
	 * Get the raster file path.
	 * 
	 * @return the path
	 */
	@Override
	public String getFilePath() {
		return (filePath);
	}

	/**
	 * Get the raster file name.
	 * 
	 * @return the name
	 */
	@Override
	public String getFileName() {
		return (StringUtil.getFileNameFromFilePath(filePath));
	}

	/**
	 * Get the raster width in pixels.
	 * 
	 * @return the width
	 */
	@Override
	public int getRasterWidth() {
		return (rasterWidth);
	}

	/**
	 * Get the raster length (height) in pixels.
	 * 
	 * @return the length
	 */
	@Override
	public int getRasterLength() {
		return (rasterLength);
	}

	/**
	 * Get the missing value.
	 * 
	 * @return missing value
	 */
	@Override
	public float getMissingValue() {
		return (missing);
	}

	/**
	 * Set the missing value.
	 * 
	 * @param missing
	 */
	@Override
	public void setMissingValue(float missing) {
		this.missing = missing;
	}

	/**
	 * Get the minimum sample value.
	 * 
	 * @return array of samplesPerPixel values
	 */
	@Override
	public double[] getMinimumSampleValue() {
		double[] min = Arrays.copyOf(minimum, minimum.length);
		for (int i=0; i<min.length; ++i)
			min[i] *= scalingFactor;
		return (min);
	}

	/**
	 * Get the maximum sample value.
	 * 
	 * @return array of samplesPerPixel values
	 */
	@Override
	public double[] getMaximumSampleValue() {
		double[] max = Arrays.copyOf(maximum, maximum.length);
		for (int i=0; i<max.length; ++i)
			max[i] *= scalingFactor;
		return (max);
	}

	/**
	 * Get the type of the raster data.
	 * 
	 * @return the type
	 */
	@Override
	public DataType getDataType() {
		return (dataType);
	}

	/**
	 * Get the number of samples per pixel.
	 * 
	 * @return samples
	 */
	@Override
	public int getSamplesPerPixel() {
		return (samplesPerPixel);
	}

	/**
	 * Load entire file contents into a raster.
	 * 
	 * @param raster
	 */
	@Override
	public abstract void load(Raster raster) throws IOException;

	/**
	 * Load entire file contents into a raster converting to float if data
	 * type is something else.
	 * 
	 * @param buffer
	 * @param dataType
	 */
	@Override
	public abstract void loadHeightMap(Raster raster) throws IOException;

	/**
	 * Load entire file contents into a raster converting to unsigned byte if data
	 * type is something else.
	 * 
	 * @param raster
	 */
	@Override
	public abstract void loadGray(Raster raster) throws IOException;

	/**
	 * Load entire file contents into byte array. File data type must be short,
	 * unsigned short, byte, or unsigned byte.
	 */
	@Override
	public abstract void loadRGBA(Raster raster) throws IOException;

	protected void computeMinMax(ByteBuffer bBuf) {
		int len = bBuf.limit();
		switch (dataType) {
		case Float:
			len /= 4;
			for (int i = 0; i < len; ++i) {
				float val = bBuf.getFloat();
				for (int j = 0; j < samplesPerPixel; ++j) {
					if (!Float.isNaN(val) && (val != missing)) {
						minimum[j] = Math.min(minimum[j], val);
						maximum[j] = Math.max(maximum[j], val);
					}
				}
			}
			break;
		case Integer:
			len /= 4;
			for (int i = 0; i < len; ++i) {
				int val = bBuf.getInt();
				for (int j = 0; j < samplesPerPixel; ++j) {
					if (val != missing) {
						minimum[j] = Math.min(minimum[j], val);
						maximum[j] = Math.max(maximum[j], val);
					}
				}
			}
			break;
		case UnsignedInteger:
			len /= 4;
			for (int i = 0; i < len; ++i) {
				long val = MathUtil.unsignedInt(bBuf.getInt());
				for (int j = 0; j < samplesPerPixel; ++j) {
					if (val != missing) {
						minimum[j] = Math.min(minimum[j], val);
						maximum[j] = Math.max(maximum[j], val);
					}
				}
			}
			break;
		case Short:
			len /= 2;
			for (int i = 0; i < len; ++i) {
				short val = bBuf.getShort();
				for (int j = 0; j < samplesPerPixel; ++j) {
					if (val != missing) {
						minimum[j] = Math.min(minimum[j], val);
						maximum[j] = Math.max(maximum[j], val);
					}
				}
			}
			break;
		case UnsignedShort:
			len /= 2;
			for (int i = 0; i < len; ++i) {
				int val = MathUtil.unsignedShort(bBuf.getShort());
				for (int j = 0; j < samplesPerPixel; ++j) {
					if (val != missing) {
						minimum[j] = Math.min(minimum[j], val);
						maximum[j] = Math.max(maximum[j], val);
					}
				}
			}
			break;
		case Byte:
			// byte[][] bbuffer = (byte[][])raster;
			// for (int r=0; r<bbuffer.length; ++r)
			// for (int c=0; c<bbuffer[r].length; ++c)
			// for (int i=0; i<samplesPerPixel; ++i)
			// if (bbuffer[r][c] != missing) {
			// minimum[i] = Math.min(minimum[i],
			// MathUtil.unsignedInt(bbuffer[r][c]));
			// maximum[i] = Math.max(maximum[i],
			// MathUtil.unsignedInt(bbuffer[r][c]));
			// }
			// break;
		case UnsignedByte:
			// byte[][] ubbuffer = (byte[][])raster;
			// for (int r=0; r<ubbuffer.length; ++r)
			// for (int c=0; c<ubbuffer[r].length; ++c)
			// for (int i=0; i<samplesPerPixel; ++i) {
			// int val = MathUtil.unsignedByte(ubbuffer[r][c]);
			// if (val != missing) {
			// minimum[i] = Math.min(minimum[i], val);
			// maximum[i] = Math.max(maximum[i], val);
			// }
			// }
			for (int i = 0; i < samplesPerPixel; ++i) {
				minimum[i] = 0;
				maximum[i] = 255;
			}
			break;
		case Double:
			len /= 8;
			for (int i = 0; i < len; ++i) {
				double val = bBuf.getDouble();
				for (int j = 0; j < samplesPerPixel; ++j) {
					if (!Double.isNaN(val) && (val != missing)) {
						minimum[j] = Math.min(minimum[j], val);
						maximum[j] = Math.max(maximum[j], val);
					}
				}
			}
			break;
		case Long:
			len /= 8;
			for (int i = 0; i < len; ++i) {
				long val = bBuf.getLong();
				for (int j = 0; j < samplesPerPixel; ++j) {
					if (val != missing) {
						minimum[j] = Math.min(minimum[j], val);
						maximum[j] = Math.max(maximum[j], val);
					}
				}
			}
			break;
		case Unknown:
			break;
		}
		bBuf.rewind();
	}
}
