package gov.nasa.arc.dert.raster.pds;

import gov.nasa.arc.dert.landscape.io.QuadTreeTile.DataType;
import gov.nasa.arc.dert.landscape.srs.ProjectionInfo;
import gov.nasa.arc.dert.raster.Raster;
import gov.nasa.arc.dert.raster.RasterFileImpl;
import gov.nasa.arc.dert.raster.pds.PdsLabel.KeyValue;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import javax.imageio.stream.FileImageInputStream;

public class PDS extends RasterFileImpl {

	// Maximum number of rows in a strip
	protected static int STRIP_ROWS = 512;

	// Raster file path
	protected String dataFilePath;

	// Input stream from raster file
	protected FileImageInputStream iStream;

	// Start address of PDS image data
	protected long imageStart;

	// Type of band storage
	protected String bandStorageType;

	// Information about the projection
	protected ProjectionInfo projInfo;

	// Output stream for writing
	protected OutputStream outputStream;
	protected DataOutputStream dataStream;

	// Parser for PDS label
	protected PdsLabel pdsLabel;

	/**
	 * Constructor
	 * 
	 * @param filePath
	 *            the PDS file path.
	 * @param properties
	 *            DERT properties
	 */
	public PDS(String filePath, Properties properties) {
		super(filePath, properties);
	}

	/**
	 * Open the PDS file.
	 * 
	 * @param access
	 *            the access mode, "r" (reading), "w" (writing), or "a"
	 *            (appending)
	 */
	@Override
	public boolean open(String access) {
		if (access.equals("r")) {
			try {
				iStream = new FileImageInputStream(new File(filePath));
				// load the metadata from the label
				pdsLabel = new PdsLabel();
				pdsLabel.parseHeader(iStream);
				iStream.close();
				iStream = null;
			} catch (IOException e) {
				try {
					iStream.close();
				} catch (Exception x) {
					// nothing here
				}
				return (false);
			}
		}
		return (super.open(access));
	}

	/**
	 * Initialize by loading raster width and length, projection information,
	 * minimum and maximum sample values, and determine the data type.
	 */
	@Override
	protected boolean initialize() {

		Integer ival = pdsLabel.getInteger("IMAGE.LINES");
		if (ival == null) {
			ival = pdsLabel.getInteger("UNCOMPRESSED_FILE.IMAGE.LINES");
		}
		if (ival == null) {
			System.err.println("LINES parameter missing.");
			return (false);
		}
		rasterLength = ival;

		ival = pdsLabel.getInteger("IMAGE.LINE_SAMPLES");
		if (ival == null) {
			ival = pdsLabel.getInteger("UNCOMPRESSED_FILE.IMAGE.LINE_SAMPLES");
		}
		if (ival == null) {
			System.err.println("LINE_SAMPLES parameter missing");
			return (false);
		}
		rasterWidth = ival;

		ival = pdsLabel.getInteger("IMAGE.BANDS");
		if (ival == null) {
			ival = pdsLabel.getInteger("UNCOMPRESSED_FILE.IMAGE.BANDS");
		}
		if (ival == null)
			ival = new Integer(1);
		samplesPerPixel = ival;

		ival = pdsLabel.getInteger("IMAGE.SAMPLE_BITS");
		if (ival == null) {
			ival = pdsLabel.getInteger("UNCOMPRESSED_FILE.IMAGE.SAMPLE_BITS");
		}
		if (ival == null) {
			System.err.println("Unable to determine value of SAMPLE_BITS parameter.");
			return (false);
		}

		byteOrder = ByteOrder.nativeOrder();
		String type = pdsLabel.getSymbol("IMAGE.SAMPLE_TYPE");
		if (type == null) {
			type = pdsLabel.getSymbol("UNCOMPRESSED_FILE.IMAGE.SAMPLE_TYPE");
		}
		if (type == null) {
			System.err.println("Unable to determine value of SAMPLE_TYPE parameter.");
			return (false);
		}
		if (type.contains("_REAL")) {
			dataType = DataType.Float;
			bytesPerSample = 4;
		} else {
			boolean unsigned = type.contains("UNSIGNED");
			if (type.startsWith("LSB_")) {
				byteOrder = ByteOrder.LITTLE_ENDIAN;
			} else if (type.startsWith("MSB_") || type.startsWith("UNSIGNED_")) {
				byteOrder = ByteOrder.BIG_ENDIAN;
			}
			if (ival == 32) {
				if (unsigned) {
					dataType = DataType.UnsignedInteger;
				} else {
					dataType = DataType.Integer;
				}
				bytesPerSample = 4;
			} else if (ival == 16) {
				if (unsigned) {
					dataType = DataType.UnsignedShort;
				} else {
					dataType = DataType.Short;
				}
				bytesPerSample = 2;
			} else if (ival == 8) {
				if (unsigned) {
					dataType = DataType.UnsignedByte;
				} else {
					dataType = DataType.Byte;
				}
				bytesPerSample = 1;
			} else {
				System.err.println("Cannot determine data type from SAMPLE_TYPE=" + type + ", SAMPLE_BITS=" + ival
					+ ".");
				dataType = DataType.Unknown;
			}
		}

		bandStorageType = pdsLabel.getSymbol("IMAGE.BAND_STORAGE_TYPE");
		if (bandStorageType == null) {
			bandStorageType = pdsLabel.getSymbol("UNCOMPRESSED_FILE.IMAGE.BAND_STORAGE_TYPE");
		}

		// get the list of possible minimum value keys from the properties file
		String str = properties.getProperty("PDS.MinimumValueKey", "IMAGE.VALID_MINIMUM");
		String[] minList = str.split(",");
		// find the minimum value
		for (int i = 0; i < minList.length; ++i) {
			if (dataType == DataType.Float) {
				Double val = pdsLabel.getDouble(minList[i]);
				if (val != null) {
					minimum = new double[samplesPerPixel];
					for (int j = 0; j < samplesPerPixel; ++j) {
						minimum[j] = val;
					}
					break;
				}
			}
			else {
				Integer val = pdsLabel.getInteger(minList[i]);
				if (val != null) {
					minimum = new double[samplesPerPixel];
					for (int j = 0; j < samplesPerPixel; ++j)
						minimum[j] = val;
					break;
				}
			}
		}

		// get the list of possible maximum value keys from the properties file
		str = properties.getProperty("PDS.MaximumValueKey", "IMAGE.VALID_MAXIMUM");
		String[] maxList = str.split(",");
		// find the maximum value
		for (int i = 0; i < maxList.length; ++i) {
			if (dataType == DataType.Float) {
				Double val = pdsLabel.getDouble(maxList[i]);
				if (val != null) {
					maximum = new double[samplesPerPixel];
					for (int j = 0; j < samplesPerPixel; ++j) {
						maximum[j] = val;
					}
					break;
				}
			}
			else {
				Integer val = pdsLabel.getInteger(maxList[i]);
				if (val != null) {
					maximum = new double[samplesPerPixel];
					for (int j = 0; j < samplesPerPixel; ++j)
						maximum[j] = val;
					break;
				}
			}
		}

		// get the list of possible missing value keys from the properties file
		str = properties.getProperty("PDS.MissingValueKey", "IMAGE.MISSING_CONSTANT");
		String[] missList = str.split(",");
		// find the missing value
		for (int i = 0; i < missList.length; ++i) {
			if (dataType == DataType.Float) {
				Float val = pdsLabel.getFloat(missList[i]);
				if (val != null) {
					missing = val.floatValue();
					break;
				}
			}
			else {
				Integer val = pdsLabel.getInteger(missList[i]);
				if (val != null) {
					missing = val;
					break;
				}
			}
		}

		// find the image start position in the file
		Object[] ptr = pdsLabel.getPointer("^IMAGE");
		if (ptr == null)
			ptr = pdsLabel.getPointer("UNCOMPRESSED_FILE.^IMAGE");
		if (ptr == null) {
			System.err.println("^IMAGE parameter missing.");
			return (false);
		}
		String imgPtr = (String)ptr[0];
		imageStart = (Long)ptr[1];
		if (imgPtr == null)
			dataFilePath = filePath;
		else
			dataFilePath = new File(new File(filePath).getParent(), imgPtr).getAbsolutePath();
		// if RECORD_BYTES is present, IMAGE is in records, not bytes
		Integer recordBytes = pdsLabel.getInteger("RECORD_BYTES");
		if (recordBytes != null)
			imageStart = recordBytes * (imageStart - 1);

		Double sF = pdsLabel.getDouble("IMAGE.SCALING_FACTOR");
		if (sF == null)
			sF = pdsLabel.getDouble("UNCOMPRESSED_FILE.IMAGE.SCALING_FACTOR");
		if (sF != null)
			scalingFactor = sF.floatValue();

		getProjectionInfo();

		System.out.println("Initialized " + filePath);
		System.out.println("Width = " + rasterWidth + ", Length = " + rasterLength + ", SamplesPerPixel = "
			+ samplesPerPixel + ", Data Type = " + dataType + ", Scaling Factor = " + scalingFactor);
		System.out
			.println("Minimum Sample Value = " + (minimum == null ? "Unknown" : minimum[0])
				+ ", Maximum Sample Value = " + (maximum == null ? "Unknown" : maximum[0]) + ", Missing Value = "
				+ missing);
		
		if (projInfo != null)
			System.out.println(projInfo);

		if (dataType == DataType.Unknown) {
			System.err.println("Unknown data type.");
			return (false);
		}

		initialized = true;
		return (true);
	}

	/**
	 * Get projection information for this file.
	 * 
	 * @return the projection info object
	 */
	@Override
	public ProjectionInfo getProjectionInfo() {
		if (projInfo != null) {
			return (projInfo);
		}
		
		String projType = pdsLabel.getString("IMAGE_MAP_PROJECTION.MAP_PROJECTION_TYPE");
		if (projType == null)
			return(null);

		projInfo = new ProjectionInfo();

		// Coordinate Transform
		projType = projType.toLowerCase();
		projType = projType.replace(" ", "");
		if (projType.equals("simplecylindrical")) {
			projType = "equirectangular";
		}
		projInfo.coordTransformCode = projInfo.findCoordTransformCode(projType);
		projInfo.projected = true;

		// Pixel Scale
		double mapScale = getMetadataDouble("IMAGE_MAP_PROJECTION.MAP_SCALE");
		double[] scale = new double[] { mapScale, mapScale, 1 };
		projInfo.scale = scale;

		// // Map Resolution
		// double mRes =
		// getMetadataDouble("IMAGE_MAP_PROJECTION.MAP_RESOLUTION");
		// projInfo.mapResolution = new double[] {1/mRes, 1/mRes, 1};
		//
		// // Lon/Lat bounds
		// double[] bounds = new double[]
		// getMetadataDouble("IMAGE_MAP_PROJECTION.WESTERNMOST_LONGITUDE"),
		// getMetadataDouble("IMAGE_MAP_PROJECTION.MINIMUM_LATITUDE"),
		// getMetadataDouble("IMAGE_MAP_PROJECTION.EASTERNMOST_LONGITUDE"),
		// getMetadataDouble("IMAGE_MAP_PROJECTION.MAXIMUM_LATITUDE")};
		// MathUtil.clipLonLat(bounds);
		// projInfo.lonLatBounds = bounds;

		// Tiepoint
		double sampleProjOffset = getMetadataDouble("IMAGE_MAP_PROJECTION.SAMPLE_PROJECTION_OFFSET");
		double xT = (0.5 - sampleProjOffset) * scale[0];
		double lineProjOffset = getMetadataDouble("IMAGE_MAP_PROJECTION.LINE_PROJECTION_OFFSET");
		double yT = (lineProjOffset - 0.5) * scale[1];
		projInfo.tiePoint = new double[] { xT, yT, 0 };

		projInfo.semiMajorAxis = getMetadataDouble("IMAGE_MAP_PROJECTION.A_AXIS_RADIUS", Double.NaN);
		projInfo.semiMinorAxis = getMetadataDouble("IMAGE_MAP_PROJECTION.C_AXIS_RADIUS", Double.NaN);
		projInfo.centerLat = getMetadataDouble("IMAGE_MAP_PROJECTION.CENTER_LATITUDE", Double.NaN);
		projInfo.centerLon = getMetadataDouble("IMAGE_MAP_PROJECTION.CENTER_LONGITUDE", Double.NaN);
		projInfo.rasterWidth = rasterWidth;
		projInfo.rasterLength = rasterLength;
		projInfo.falseEasting = 0;
		projInfo.falseNorthing = 0;
		projInfo.projLinearUnits = "meter";

		// Globe.
		String target = getMetadataString("TARGET_NAME", "");
		if (target.isEmpty()) {
			String g = properties.getProperty("DefaultGlobe");
			if (g != null) {
				target = g;
			} else {
				target = "Earth";
			}
			System.err.println("Could not determine globe ... setting to " + target);
		} else if (target.length() == 1) {
			target = target.toUpperCase();
		} else {
			target = target.substring(0, 1).toUpperCase() + target.substring(1).toLowerCase();
		}
		projInfo.globe = target;

		return (projInfo);
	}

	/**
	 * Get a double value from the metadata hashmap and use a default value if
	 * not available.
	 * 
	 * @param key
	 *            the metadata key
	 * @param defaultValue
	 * @return the value
	 */
	protected final double getMetadataDouble(String key, double defaultValue) {
		Double d = pdsLabel.getDouble(key);
		if (d == null) {
			return (defaultValue);
		}
		return (d);
	}

	/**
	 * Get a double value from the metadata hashmap and throw an exception if it
	 * isn't available.
	 * 
	 * @param key
	 *            the metadata key
	 * @param defaultValue
	 * @return the value
	 */
	protected final double getMetadataDouble(String key) {
		Double d = pdsLabel.getDouble(key);
		if (d == null) {
			throw new IllegalArgumentException("Unable to find " + key + ".");
		}
		return (d);
	}

	/**
	 * Get a String value from the metadata hashmap and use a default value if
	 * not available.
	 * 
	 * @param key
	 *            the metadata key
	 * @param defaultValue
	 * @return the value
	 */
	protected final String getMetadataString(String key, String defaultValue) {
		String d = pdsLabel.getString(key);
		if (d == null) {
			return (defaultValue);
		}
		return (d);
	}

	/**
	 * Get a String value from the metadata hashmap and throw an exception if it
	 * isn't available.
	 * 
	 * @param key
	 *            the metadata key
	 * @param defaultValue
	 * @return the value
	 */
	protected final String getMetadataString(String key) {
		String d = pdsLabel.getString(key);
		if (d == null) {
			throw new IllegalArgumentException("Unable to find " + key + ".");
		}
		return (d);
	}

	/**
	 * Close the PDS file. If the file was opened for writing or appending, keys
	 * and data will be flushed.
	 */
	@Override
	public void close() throws IOException {
		if (!access.equals("r")) {
			if (dataStream != null) {
				dataStream.close();
				dataStream = null;
			}
			if (outputStream != null) {
				outputStream.close();
				outputStream = null;
			}
		} else if (iStream != null) {
			iStream.close();
		}
	}

	/**
	 * Load data from file into a raster, converting data to float.
	 * 
	 * @param raster
	 */
	@Override
	public void load(Raster raster) throws IOException {

		int stripHeight = Math.min(STRIP_ROWS, rasterLength);
		int stripWidth = rasterWidth * samplesPerPixel;
		int stripSize = stripWidth * stripHeight * bytesPerSample;
		int numStrips = (int) Math.ceil((double) rasterLength / stripHeight);

		// Java limits ByteBuffer sizes
		if (stripSize > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Cannot load PDS file with strip size > " + Integer.MAX_VALUE + ".");
		}

		if ((minimum == null) || (maximum == null)) {
			computeMinMaxFromStrip(dataType, numStrips, stripSize, stripWidth, stripHeight);
		}

		loadFromStrip(dataType, numStrips, stripSize, stripWidth, stripHeight, raster, false);
	}

	/**
	 * Load data from height map file into a raster, converting data to float.
	 * 
	 * @param raster
	 */
	@Override
	public void loadHeightMap(Raster raster) throws IOException {

		int stripHeight = Math.min(STRIP_ROWS, rasterLength);
		int stripWidth = rasterWidth * samplesPerPixel;
		int stripSize = stripWidth * stripHeight * bytesPerSample;
		int numStrips = (int) Math.ceil((double) rasterLength / stripHeight);

		// Java limits ByteBuffer sizes
		if (stripSize > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Cannot load PDS file with strip size > " + Integer.MAX_VALUE + ".");
		}

		if ((minimum == null) || (maximum == null)) {
			computeMinMaxFromStrip(dataType, numStrips, stripSize, stripWidth, stripHeight);
		}

		loadFromStrip(dataType, numStrips, stripSize, stripWidth, stripHeight, raster, false);
	}

	/**
	 * Load data from file into a raster, converting data to unsigned byte gray scale.
	 * 
	 * @param raster
	 */
	@Override
	public void loadGray(Raster raster) throws IOException {

		int stripHeight = Math.min(STRIP_ROWS, rasterLength);
		int stripWidth = rasterWidth * samplesPerPixel;
		int stripSize = stripWidth * stripHeight * bytesPerSample;
		int numStrips = (int) Math.ceil((double) rasterLength / stripHeight);

		// Java limits ByteBuffer sizes
		if (stripSize > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Cannot load PDS file with strip size > " + Integer.MAX_VALUE + ".");
		}

		if ((minimum == null) || (maximum == null)) {
			computeMinMaxFromStrip(dataType, numStrips, stripSize, stripWidth, stripHeight);
		}

		loadFromStrip(dataType, numStrips, stripSize, stripWidth, stripHeight, raster, true);

		minimum = new double[] { 0 };
		maximum = new double[] { 255 };
	}

	/**
	 * Load data from a strip into a raster with no conversion.
	 * 
	 * @param dataType
	 *            the data type of the raster.
	 * @param numStrips
	 *            number of strips to read from the file.
	 * @param size
	 *            size of a strip
	 * @param width
	 *            width of a strip
	 * @param height
	 *            height of a strip
	 * @param raster
	 *            the raster
	 * @throws IOException
	 */
	protected final void loadFromStrip(int numStrips, int size, int width, int height, Raster raster)
		throws IOException {
		if (iStream != null) {
			iStream.close();
		}
		iStream = new FileImageInputStream(new File(dataFilePath));
		iStream.seek(imageStart);

		// Allocate memory
		byte[] bbArray = new byte[size];
		ByteBuffer bbuf = ByteBuffer.wrap(bbArray);
		bbuf.order(byteOrder);

		// Read each strip and place it in the full size raster.
		int r = 0; // row pixel of upper left corner of strip
		for (int i = 0; i < numStrips; ++i) {
			// determine the height of each strip in case it is short
			bbuf.rewind();
			int h = Math.min(rasterLength - height * i, height); // strip height
			int len = h * width * bytesPerSample;
			len = iStream.read(bbArray, 0, len);
			bbuf.rewind();
			raster.set(r, h, bbArray);
			r += h;
		}
	}

	/**
	 * Load data from a strip file into a raster, converting data.
	 * 
	 * @param dataType
	 *            the data type of the raster.
	 * @param numStrips
	 *            number of strips to read from the file.
	 * @param size
	 *            size of a strip
	 * @param width
	 *            width of a strip
	 * @param height
	 *            height of a strip
	 * @param raster
	 *            the raster
	 * @param gray
	 *            convert to gray scale unsigned byte
	 * @throws IOException
	 */
	protected final void loadFromStrip(DataType dataType, int numStrips, int size, int width, int height,
		Raster raster, boolean gray) throws IOException {
		if (iStream != null) {
			iStream.close();
		}
		iStream = new FileImageInputStream(new File(dataFilePath));
		iStream.seek(imageStart);

		// Allocate memory
		byte[] bbArray = new byte[size];
		ByteBuffer bbuf = ByteBuffer.wrap(bbArray);
		bbuf.order(byteOrder);
		bbuf.rewind();

		// Read each strip and place it in the full size raster.
		int r = 0; // row pixel of upper left corner of strip
		for (int i = 0; i < numStrips; ++i) {
			// determine the height of each strip in case it is short
			int h = Math.min(rasterLength - height * i, height); // strip height
			int len = h * width * bytesPerSample;
			iStream.read(bbArray, 0, len);
			bbuf.rewind();
			bbuf.limit(len);
			if (gray) {
				raster.setAsGray(r, 0, rasterWidth, h, bbuf, dataType, minimum, maximum, missing);
			} else {
				raster.setAsFloat(r, 0, rasterWidth, h, bbuf, dataType, scalingFactor, minimum, maximum, missing);
			}
			r += h;
		}
	}

	/**
	 * Compute minimum and maximum.
	 * 
	 * @param dataType
	 *            the data type of the raster.
	 * @param numStrips
	 *            number of strips to read from the file.
	 * @param size
	 *            size of a strip
	 * @param width
	 *            width of a strip
	 * @param height
	 *            height of a strip
	 * @throws IOException
	 */
	protected final void computeMinMaxFromStrip(DataType dataType, int numStrips, int size, int width, int height)
		throws IOException {
		if (iStream != null) {
			iStream.close();
		}
		iStream = new FileImageInputStream(new File(dataFilePath));
		iStream.seek(imageStart);

		minimum = new double[samplesPerPixel];
		Arrays.fill(minimum, Double.MAX_VALUE);
		maximum = new double[samplesPerPixel];
		Arrays.fill(maximum, -Double.MAX_VALUE);

		// Allocate memory
		byte[] bbArray = new byte[size];
		ByteBuffer bbuf = ByteBuffer.wrap(bbArray);
		bbuf.order(byteOrder);
		bbuf.rewind();

		// Read each strip and place it in the full size raster.
		for (int i = 0; i < numStrips; ++i) {
			// determine the height of each strip in case it is short
			int h = Math.min(rasterLength - height * i, height); // strip height
			int len = h * width * bytesPerSample;
			iStream.read(bbArray, 0, len);
			bbuf.rewind();
			computeMinMax(bbuf);
		}
	}

	/**
	 * Load data from a file as an RGBA image, to be used as a texture map.
	 * 
	 * @param raster
	 */
	@Override
	public void loadRGBA(Raster raster) throws IOException {
		if (bandStorageType == null) {
			throw new IllegalArgumentException("Unable to find BAND_STORAGE_TYPE.");
		}
		if (samplesPerPixel < 3) {
			throw new IllegalArgumentException("Cannot load RGBA with less than 3 color components.");
		}
		if (iStream != null) {
			iStream.close();
		}
		iStream = new FileImageInputStream(new File(dataFilePath));
		iStream.seek(imageStart);

		int stripHeight = Math.min(STRIP_ROWS, rasterLength);
		int stripWidth = rasterWidth * samplesPerPixel;
		int numStrips = (int) Math.ceil((double) rasterLength / stripHeight);

		// samples are grouped together for each pixel
		if (bandStorageType.equals("SAMPLE_INTERLEAVED")) {
			int stripSize = stripWidth * stripHeight * bytesPerSample;

			// Java limits ByteBuffer sizes
			if (stripSize > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Cannot load PDS file with strip size > " + Integer.MAX_VALUE + ".");
			}

			// Allocate memory directly
			byte[] bbArray = new byte[stripSize];
			ByteBuffer bbuf = ByteBuffer.allocate(4 * rasterWidth * stripHeight);
			iStream.seek(imageStart);

			// Read each strip and place it in the full size raster.
			int r = 0; // row pixel of upper left corner of strip
			for (int i = 0; i < numStrips; ++i) {
				// determine the height of each strip in case it is short
				int h = Math.min(rasterLength - stripHeight * i, stripHeight); // compute
																				// the
																				// height
																				// of
																				// the
																				// strip
				int len = h * stripWidth;
				len = iStream.read(bbArray, 0, len);
				for (int j = 0; j < h; ++j) {
					for (int k = 0; k < stripWidth; k += samplesPerPixel) {
						for (int l = 0; l < samplesPerPixel; ++l) {
							bbuf.put(bbArray[k + l]);
						}
						if (samplesPerPixel == 3) {
							bbuf.put((byte) 255);
						}
					}
					r++;
				}
				bbuf.rewind();
				// raster.set(r, rasterWidth, h, bbuf);
				raster.set(r, h, bbArray);
			}
		}
		// samples are separated into bands
		else if (bandStorageType.equals("BAND_SEQUENTIAL")) {

			int stripSize = rasterWidth * stripHeight;

			// Java limits ByteBuffer sizes
			if (stripSize > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Cannot load PDS file with strip size > " + Integer.MAX_VALUE + ".");
			}

			raster.set(255);
			byte[] bArray = new byte[4 * rasterWidth * stripHeight];

			// Allocate memory
			byte[] bbArray = new byte[stripSize];
			iStream.seek(imageStart);

			for (int k = 0; k < samplesPerPixel; ++k) {
				// Read each strip and place it in the full size raster.
				int r = 0; // row pixel of upper left corner of strip
				for (int i = 0; i < numStrips; ++i) {
					// determine the height of each strip in case it is short
					int h = Math.min(rasterLength - stripHeight * i, stripHeight); // compute
																					// the
																					// height
																					// of
																					// the
																					// strip
					int len = h * rasterWidth;
					len = iStream.read(bbArray, 0, len);
					for (int j = 0; j < h; ++j) {
						raster.get(r, bArray);
						for (int c = 0; c < rasterWidth; ++c) {
							bArray[c * 4 + k] = bbArray[c];
						}
						raster.set(r, 1, bArray);
						r++;
					}
				}
			}
		}
		if (minimum == null) {
			minimum = new double[] { 0, 0, 0, 1 };
		}
		if (maximum == null) {
			maximum = new double[] { 255, 255, 255, 255 };
		}

	}

	/**
	 * Write a raster to a file.
	 * @param metadata
	 * @param imageStart
	 * @param raster
	 * @param dataType
	 * @throws IOException
	 */
	public void write(ArrayList<KeyValue> metadata, long imageStart, Raster raster, DataType dataType)
		throws IOException {
		OutputStream oStream = new FileOutputStream(filePath);
		if (oStream != null) {
			outputStream = new BufferedOutputStream(oStream);
		}

		// Write header (label)
		PdsLabel pdsLabel = new PdsLabel();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
		long size = pdsLabel.writeObject(writer, metadata);
		long pad = imageStart - size;
		for (int i = 0; i < pad; ++i) {
			writer.write(' ');
		}
		writer.flush();

		// Write data
		dataStream = new DataOutputStream(outputStream);
		writeRaster(raster, dataType);
	}

	/**
	 * Write the image data.
	 * 
	 * @param raster
	 *            the data array
	 * @param dataType
	 *            the data type
	 * @throws IOException
	 */
	protected final void writeRaster(Raster raster, DataType dataType) throws IOException {
		int rasterWidth = raster.getWidth();
		int rasterLength = raster.getLength();
		int numBytes = raster.getBytesPerSample();
		byte[] bData = new byte[rasterWidth * numBytes];
		for (int i = 0; i < rasterLength; ++i) {
			raster.get(i, bData);
			dataStream.write(bData[i]);
		}
	}

}
