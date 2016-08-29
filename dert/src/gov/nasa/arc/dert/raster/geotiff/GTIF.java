package gov.nasa.arc.dert.raster.geotiff;

import gov.nasa.arc.dert.raster.ProjectionInfo;
import gov.nasa.arc.dert.raster.Raster;
import gov.nasa.arc.dert.raster.RasterFileImpl;
import gov.nasa.arc.dert.raster.geotiff.GeoKey.KeyID;
import gov.nasa.arc.dert.util.ImageUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

/**
 * Implementation of RasterFile interface for a GeoTIFF file. Includes native
 * methods for reading and writing files that wrap libtiff.
 *
 */
public class GTIF extends RasterFileImpl {

	public static final int TIFFTAG_IMAGEWIDTH = 256;
	public static final int TIFFTAG_IMAGELENGTH = 257;
	public static final int TIFFTAG_BITSPERSAMPLE = 258;
	public static final int TIFFTAG_TILEWIDTH = 322;
	public static final int TIFFTAG_TILELENGTH = 323;
	public static final int TIFFTAG_MINSAMPLEVALUE = 280;
	public static final int TIFFTAG_MAXSAMPLEVALUE = 281;
	public static final int TIFFTAG_SAMPLESPERPIXEL = 277;
	public static final int TIFFTAG_ROWSPERSTRIP = 278;
	public static final int TIFFTAG_STRIPBYTECOUNTS = 279;
	public static final int TIFFTAG_PLANARCONFIG = 284;
	public static final int TIFFTAG_SAMPLEFORMAT = 339;
	public static final int TIFFTAG_SMINSAMPLEVALUE = 340;
	public static final int TIFFTAG_SMAXSAMPLEVALUE = 341;
	public static final int TIFFTAG_FILLORDER = 266;
	public static final int TIFFTAG_PHOTOMETRIC = 262;

	public static final int PHOTOMETRIC_RGB = 2;

	public static final int GDAL_METADATA_TAG = 42112;
	public static final int GDAL_NODATA_TAG = 42113;
	public static final int GEO_KEY_DIRECTORY_TAG = 34735;
	public static final int GEO_DOUBLE_PARAMS_TAG = 34736;
	public static final int GEO_ASCII_PARAMS_TAG = 34737;

	public static final int MODEL_TIEPOINT_TAG = 33922;
	public static final int MODEL_PIXEL_SCALE_TAG = 33550;
	public static final int MODEL_TRANSFORMATION_TAG = 34264;

	public static final int PLANARCONFIG_CONTIG = 1;
	public static final int PLANARCONFIG_SEPARATE = 2;

	public static final int SAMPLEFORMAT_UINT = 1;
	public static final int SAMPLEFORMAT_INT = 2;
	public static final int SAMPLEFORMAT_IEEEFP = 3;
	public static final int SAMPLEFORMAT_VOID = 4;
	public static final int SAMPLEFORMAT_COMPLEXINT = 5;
	public static final int SAMPLEFORMAT_COMPLEXIEEEFP = 6;

	protected long handle;
	protected short[] shortCodeValue = new short[1];
	protected double[] doubleCodeValue = new double[1];
	protected String[] asciiCodeValue = new String[1];
	protected ProjectionInfo projInfo;
	protected HashMap<KeyID, Object> geoKeyMap;
	protected boolean isTiled;
	protected int tileWidth, tileLength, tileCount, rowsPerStrip, stripCount, planarConfiguration;
	protected long stripSize, tileSize;
	protected short sampleFormat;

	/**
	 * Given a TIFF file path, open the file. The returned TIFF access handle
	 * can be used to read or write TIFF tags using the various GTIF functions.
	 * The handle should be destroyed using closeTIFF() to close the file.
	 * 
	 * @param filePath
	 *            the file location
	 * @param access
	 *            the file access, one of "r", "w", "a"
	 * @return the access handle
	 */
	protected native long openTIFF(String filePath, String access);

	/**
	 * This function deallocates an existing TIFF access handle previously
	 * created with openTIFF(). and closes the file. Any data and/or tags will
	 * be flushed prior to close.
	 * 
	 * @param handle
	 *            the access handle
	 */
	protected native void closeTIFF(long handle);

	/**
	 * This function reads the value of a single TIFF tag from a TIFF file.
	 * Value array should be large enough to hold the tag contents.
	 * 
	 * @param handle
	 *            the access handle
	 * @param tag
	 *            The TIFF tag.
	 * @param value
	 *            pointer to the variable into which the value should be read.
	 * @return number of values in array (0 if unsuccessful)
	 */
	protected native int getTIFFFieldString(long handle, int tag, String[] value);

	protected native int getTIFFFieldInt(long handle, int tag, int[] value);

	protected native int getTIFFFieldShort(long handle, int tag, short[] value);

	protected native int getTIFFFieldDouble(long handle, int tag, double[] value);

	protected native int getTIFFFieldFloat(long handle, int tag, float[] value);

	/**
	 * This function writes the value of a single TIFF tag to a TIFF file.
	 * 
	 * @param handle
	 *            the access handle
	 * @param tag
	 *            The TIFF tag.
	 * @param value
	 *            to be written.
	 * @return success or failure
	 */
	protected native boolean setTIFFFieldString(long handle, int tag, String value);

	protected native boolean setTIFFFieldInt(long handle, int tag, int value);

	protected native boolean setTIFFFieldShort(long handle, int tag, short value);

	protected native boolean setTIFFFieldDouble(long handle, int tag, double value);

	protected native boolean setTIFFFieldFloat(long handle, int tag, float value);

	/**
	 * This function writes the array of values of a single TIFF tag to a TIFF
	 * file.
	 * 
	 * @param handle
	 *            the access handle
	 * @param tag
	 *            The TIFF tag.
	 * @param value
	 *            array to be written.
	 * @return success or failure
	 */
	protected native boolean setTIFFFieldIntArray(long handle, int tag, int[] value);

	protected native boolean setTIFFFieldShortArray(long handle, int tag, short[] value);

	protected native boolean setTIFFFieldDoubleArray(long handle, int tag, double[] value);

	protected native boolean setTIFFFieldFloatArray(long handle, int tag, float[] value);

	/**
	 * Is this TIFF tiled.
	 * 
	 * @param handle
	 *            the access handle
	 * @return true or false
	 */
	protected native boolean isTiled(long handle);

	/**
	 * Get the number of strips in this TIFF.
	 * 
	 * @param handle
	 *            the access handle
	 * @return number of strips (unsigned int)
	 */
	protected native int getNumberOfStrips(long handle);

	/**
	 * Get the size of the strips.
	 * 
	 * @param handle
	 *            the access handle
	 * @return the strip size (signed long)
	 */
	protected native long getStripSize(long handle);

	/**
	 * Read a strip from the file.
	 * 
	 * @param handle
	 *            the access handle
	 * @param stripNumber
	 *            the strip number
	 * @param buffer
	 *            a buffer to hold the data
	 * @param size
	 *            the amount of data to load (-1 to load entire strip)
	 * @return bytes read (signed long) or -1 for error
	 */
	protected native long readStrip(long handle, int stripNumber, Buffer buffer, long size);

	/**
	 * Read a strip in RGBA format from the file.
	 * 
	 * @param handle
	 *            the access handle
	 * @param row
	 *            the row in the image
	 * @param buffer
	 *            a buffer to hold the data, should have an int for each pixel
	 * @return success or failure
	 */
	protected native boolean readRGBAStrip(long handle, int row, Buffer buffer);

	/**
	 * Write a strip to the file.
	 * 
	 * @param handle
	 *            the access handle
	 * @param stripNumber
	 *            the strip number
	 * @param buffer
	 *            a buffer containing the data
	 * @param size
	 *            the amount of data to write
	 * @return bytes written (signed long) or -1 for error
	 */
	protected native long writeStrip(long handle, int stripNumber, Buffer buffer, long size);

	/**
	 * Get the number of tiles in this GeoTIFF.
	 * 
	 * @param handle
	 *            the access handle
	 * @return number of tiles (unsigned int)
	 */
	protected native int getNumberOfTiles(long handle);

	/**
	 * Get the size of the tiles.
	 * 
	 * @param handle
	 *            the access handle
	 * @return the tile size (signed long)
	 */
	protected native long getTileSize(long handle);

	/**
	 * Read a tile from the file.
	 * 
	 * @param handle
	 *            the access handle
	 * @param tileNumber
	 *            the tile number
	 * @param buffer
	 *            a buffer to hold the data
	 * @param size
	 *            the amount of data to load (-1 to load the entire tile)
	 * @return bytes read (signed long) or -1 for error
	 */
	protected native long readTile(long handle, int tileNumber, Buffer buffer, long size);

	/**
	 * Read a tile from the file in RGBA format.
	 * 
	 * @param handle
	 *            the access handle
	 * @param x
	 *            the column of the image for the upper left corner of the tile
	 * @param y
	 *            the row of the image for the upper left corner of the tile
	 * @param buffer
	 *            a buffer to hold the data, should have an int for each pixel
	 * @return success or failure
	 */
	protected native boolean readRGBATile(long handle, int x, int y, Buffer buffer);

	/**
	 * Write a tile to the file.
	 * 
	 * @param handle
	 *            the access handle
	 * @param tileNumber
	 *            the tile number
	 * @param buffer
	 *            a buffer containing the data
	 * @param size
	 *            the amount of data to write
	 * @return bytes written (signed long) or -1 for error
	 */
	protected native long writeTile(long handle, int tileNumber, Buffer buffer, long size);

	/**
	 * Get the TIFF error message.
	 * 
	 * @return message
	 */
	protected native String getTIFFError();

	/**
	 * Load native library.
	 */

	static {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("mac")) {
			loadNativeLibrary("/libgeo.jnilib");
		} else if (os.contains("lin")) {
			loadNativeLibrary("/libgeo.so");
		}
	}

	protected static void loadNativeLibrary(String libName) {
		try {
			final InputStream in = GTIF.class.getResource(libName).openStream();
			int p0 = libName.lastIndexOf('/');
			int p1 = libName.lastIndexOf('.');
			String tempName = libName.substring(p0, p1) + '_' + System.currentTimeMillis();
			final File libFile = File.createTempFile(tempName, ".jni");
			libFile.deleteOnExit();
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(libFile));
			int len = 0;
			byte[] buffer = new byte[32768];
			while ((len = in.read(buffer)) > -1) {
				out.write(buffer, 0, len);
			}
			out.close();
			in.close();
			System.load(libFile.getAbsolutePath());
			libFile.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor
	 */
	public GTIF(String filePath, Properties properties) {
		super(filePath, properties);
	}

	/**
	 * Open the GeoTIFF file.
	 * 
	 * @param filePath
	 *            the path to the file
	 * @param access
	 *            the access mode, "r" (reading), "w" (writing), or "a"
	 *            (appending)
	 */
	@Override
	public boolean open(String access) {
		handle = openTIFF(filePath, access);
		if (handle == 0) {
			String str = getTIFFError();
			System.err.println("Unable to open " + filePath + " for " + access + " access.");
			System.err.println("Cause: " + str);
			return (false);
		}
		return (super.open(access));
	}

	/**
	 * Close the GeoTIFF file. If the file was opened for writing or appending,
	 * keys and data will be flushed.
	 */
	@Override
	public void close() throws IOException {
		closeTIFF(handle);
		handle = 0;
	}

	/**
	 * Initialize by loading image width and length, projection information,
	 * minimum and maximum sample values, and determine the data type.
	 */
	@Override
	protected boolean initialize() {
		if (handle == 0) {
			return (false);
		}
		rasterWidth = getTIFFFieldInt(TIFFTAG_IMAGEWIDTH, true, 0);
		rasterLength = getTIFFFieldInt(TIFFTAG_IMAGELENGTH, true, 0);
		isTiled = isTiled(handle);
		if (isTiled) {
			tileWidth = getTIFFFieldInt(TIFFTAG_TILEWIDTH, true, 0);
			tileLength = getTIFFFieldInt(TIFFTAG_TILELENGTH, true, 0);
			tileSize = getTileSize(handle);
			tileCount = getNumberOfTiles(handle);
		} else {
			rowsPerStrip = getTIFFFieldInt(TIFFTAG_ROWSPERSTRIP, true, 0);
			stripCount = getNumberOfStrips(handle);
			stripSize = getStripSize(handle);
		}
		sampleFormat = getTIFFFieldShort(TIFFTAG_SAMPLEFORMAT, true, (short) 0);
		planarConfiguration = getTIFFFieldShort(TIFFTAG_PLANARCONFIG, true, (short) 0);

		boolean missingUnknown = true;
		missing = Float.NaN;
		String str = getTIFFFieldString(GDAL_NODATA_TAG, false, null);
		if (str != null) {
			missing = Float.valueOf(str.trim());
			missingUnknown = false;
		}
		str = getTIFFFieldString(GDAL_METADATA_TAG, false, null);
		if (str != null)
			System.out.println(str);
		
		short[] geoKeyDir = getTIFFFieldShort(GEO_KEY_DIRECTORY_TAG, true, null);
		double[] geoKeyDouble = getTIFFFieldDouble(GEO_DOUBLE_PARAMS_TAG, false, null);
		String geoKeyString = getTIFFFieldString(GEO_ASCII_PARAMS_TAG, false, null);
		geoKeyMap = GeoKey.mapKeys(geoKeyDir, geoKeyDouble, geoKeyString);

		dataType = findDataType();

		getProjectionInfo();

		samplesPerPixel = getTIFFFieldShort(TIFFTAG_SAMPLESPERPIXEL, true, (short) 0);
		minimum = findMinimumSampleValue();
		maximum = findMaximumSampleValue();

		System.out.println("Loaded tags from " + filePath);
		System.out.println("Width = " + rasterWidth + ", Length = " + rasterLength + ", SamplesPerPixel = "
			+ samplesPerPixel + ", Data Type = " + dataType);
		if (isTiled) {
			System.out.println("Tile width = " + tileWidth + ", Tile Length = " + tileLength + ", Tile Count = "
				+ tileCount + ", Tile Size = " + tileSize);
		} else {
			System.out.println("Strip Size = " + stripSize + ", Strip Count = " + stripCount + ", Rows per Strip = "
				+ rowsPerStrip);
		}
		System.out
			.println("Minimum Sample Value = " + (minimum == null ? "Unknown" : minimum[0])
				+ ", Maximum Sample Value = " + (maximum == null ? "Unknown" : maximum[0])
				+ ", Missing Value = " + (missingUnknown ? "Unknown" : missing));
		if (dataType == DataType.Unknown) {
			System.err.println("Unknown data type.");
			return (false);
		}

		System.out.println();
		initialized = true;
		return (true);
	}

	/**
	 * Get the projection information from the TIFF and GeoTIFF tags.
	 * 
	 * @return projection info object
	 */
	@Override
	public ProjectionInfo getProjectionInfo() {
		if (projInfo != null) {
			return (projInfo);
		}

		projInfo = new ProjectionInfo();
		boolean isGeographic = false;
		boolean isDegrees = false;

		Integer aU = (Integer)geoKeyMap.get(KeyID.GeogAngularUnits);
		if (aU != null) {
			if (!(aU == GeoKey.Code_Angular_Degree) && !(aU == GeoKey.Code_Angular_Radian)) {
				throw new UnsupportedOperationException("Angular units of " + aU + " is not supported.");
			}
		} else {
			aU = GeoKey.Code_Angular_Degree;
		}
		isDegrees = (aU == GeoKey.Code_Angular_Degree);
		Integer lU = (Integer) geoKeyMap.get(KeyID.GeogLinearUnits);
		if (lU != null) {
			if (lU != GeoKey.Code_Linear_Meter) {
				throw new UnsupportedOperationException("Geographic Linear units of " + lU + " is not supported.");
			}
		}
		lU = (Integer) geoKeyMap.get(KeyID.ProjLinearUnits);
		if (lU != null) {
			if (lU != GeoKey.Code_Linear_Meter) {
				throw new UnsupportedOperationException("Projected Linear units of " + lU + " not supported.");
			}
		}
		projInfo.projLinearUnits = "meter";

		// Coordinate Transform
		Integer mt = (Integer)geoKeyMap.get(KeyID.ModelType);
		if (mt == null)
			throw new IllegalArgumentException("Model type not supported.");
		if (mt == GeoKey.Code_ModelTypeGeocentric) {
			throw new UnsupportedOperationException("ModelType Geocentric is not supported.");
		}
		isGeographic = (mt == GeoKey.Code_ModelTypeGeographic);

		projInfo.projected = !isGeographic;
		projInfo.rasterWidth = rasterWidth;
		projInfo.rasterLength = rasterLength;
		projInfo.gcsCode = getGTIFKeyInt(KeyID.GeographicType, 0);
		projInfo.datumCode = getGTIFKeyInt(KeyID.GeogGeodeticDatum, 0);
		projInfo.ellipsoidCode = getGTIFKeyInt(KeyID.GeogEllipsoid, 0);
		projInfo.primeMeridianCode = getGTIFKeyInt(KeyID.GeogPrimeMeridian, 0);
		projInfo.semiMajorAxis = getGTIFKeyDouble(KeyID.GeogSemiMajorAxis, Double.NaN);
		projInfo.semiMinorAxis = getGTIFKeyDouble(KeyID.GeogSemiMinorAxis, Double.NaN);
		projInfo.inverseFlattening = getGTIFKeyDouble(KeyID.GeogInvFlattening, Double.NaN);
		projInfo.gcsPrimeMeridianLon = getGTIFKeyDouble(KeyID.GeogPrimeMeridianLong, Double.NaN);
		if (!isDegrees) {
			projInfo.gcsPrimeMeridianLon = Math.toDegrees(projInfo.gcsPrimeMeridianLon);
		}

		projInfo.pcsCode = getGTIFKeyInt(KeyID.ProjectedCSType, 0);
		projInfo.projCode = getGTIFKeyInt(KeyID.Projection, 0);
		projInfo.coordTransformCode = getGTIFKeyInt(KeyID.ProjCoordTrans, 0);
		projInfo.centerLon = getGTIFKeyDouble(KeyID.ProjCenterLong, Double.NaN);
		if (!isDegrees) {
			projInfo.centerLon = Math.toDegrees(projInfo.centerLon);
		}
		projInfo.centerLat = getGTIFKeyDouble(KeyID.ProjCenterLat, Double.NaN);
		if (!isDegrees) {
			projInfo.centerLat = Math.toDegrees(projInfo.centerLat);
		}
		projInfo.stdParallel1 = getGTIFKeyDouble(KeyID.ProjStdParallel1, Double.NaN);
		if (!isDegrees) {
			projInfo.stdParallel1 = Math.toDegrees(projInfo.stdParallel1);
		}
		projInfo.stdParallel2 = getGTIFKeyDouble(KeyID.ProjStdParallel2, Double.NaN);
		if (!isDegrees) {
			projInfo.stdParallel2 = Math.toDegrees(projInfo.stdParallel2);
		}
		projInfo.naturalOriginLon = getGTIFKeyDouble(KeyID.ProjNatOriginLong, Double.NaN);
		if (!isDegrees) {
			projInfo.naturalOriginLon = Math.toDegrees(projInfo.naturalOriginLon);
		}
		projInfo.naturalOriginLat = getGTIFKeyDouble(KeyID.ProjNatOriginLat, Double.NaN);
		if (!isDegrees) {
			projInfo.naturalOriginLat = Math.toDegrees(projInfo.naturalOriginLat);
		}
		projInfo.falseEasting = getGTIFKeyDouble(KeyID.ProjFalseEasting, Double.NaN);
		projInfo.falseNorthing = getGTIFKeyDouble(KeyID.ProjFalseNorthing, Double.NaN);
		projInfo.centerEasting = getGTIFKeyDouble(KeyID.ProjCenterEasting, Double.NaN);
		projInfo.centerNorthing = getGTIFKeyDouble(KeyID.ProjCenterNorthing, Double.NaN);
		projInfo.scaleAtNaturalOrigin = getGTIFKeyDouble(KeyID.ProjScaleAtNatOrigin, Double.NaN);
		projInfo.azimuth = getGTIFKeyDouble(KeyID.ProjAzimuthAngle, Double.NaN);
		if (!isDegrees) {
			projInfo.azimuth = Math.toDegrees(projInfo.azimuth);
		}
		projInfo.straightVertPoleLon = getGTIFKeyDouble(KeyID.ProjStraightVertPoleLong, Double.NaN);
		if (!isDegrees) {
			projInfo.straightVertPoleLon = Math.toDegrees(projInfo.straightVertPoleLon);
		}
		projInfo.scaleAtCenter = getGTIFKeyDouble(KeyID.ProjScaleAtCenter, Double.NaN);
		projInfo.falseOriginLon = getGTIFKeyDouble(KeyID.ProjFalseOriginLong, Double.NaN);
		if (!isDegrees) {
			projInfo.falseOriginLon = Math.toDegrees(projInfo.falseOriginLon);
		}
		projInfo.falseOriginLat = getGTIFKeyDouble(KeyID.ProjFalseOriginLat, Double.NaN);
		if (!isDegrees) {
			projInfo.falseOriginLat = Math.toDegrees(projInfo.falseOriginLat);
		}

		// Globe.
		String citation = getGTIFKeyASCII(KeyID.Citation, "");
		String geogCitation = getGTIFKeyASCII(KeyID.GeogCitation, "");
		if (citation.toLowerCase().contains("mars") || geogCitation.toLowerCase().contains("mars")) {
			projInfo.globe = "Mars";
		} else if (citation.toLowerCase().contains("moon") || geogCitation.toLowerCase().contains("moon")) {
			projInfo.globe = "Moon";
		} else if (citation.toLowerCase().contains("wgs84") || geogCitation.toLowerCase().contains("wgs84")) {
			projInfo.globe = "Earth";
		} else if (citation.toLowerCase().contains("wgs 84") || geogCitation.toLowerCase().contains("wgs 84")) {
			projInfo.globe = "Earth";
		} else if (citation.toLowerCase().contains("wgs 1984") || geogCitation.toLowerCase().contains("wgs 1984")) {
			projInfo.globe = "Earth";
		} else if (citation.toLowerCase().contains("wgs_84") || geogCitation.toLowerCase().contains("wgs_84")) {
			projInfo.globe = "Earth";
		} else if (citation.toLowerCase().contains("wgs_1984") || geogCitation.toLowerCase().contains("wgs_1984")) {
			projInfo.globe = "Earth";
		} else if (citation.toLowerCase().contains("nad83") || geogCitation.toLowerCase().contains("nad83")) {
			projInfo.globe = "Earth";
		} else if (citation.toLowerCase().contains("nad 83") || geogCitation.toLowerCase().contains("nad 83")) {
			projInfo.globe = "Earth";
		} else if ((projInfo.gcsCode == GeoKey.Code_GCS_WGS_84) || (projInfo.gcsCode == GeoKey.Code_GCS_NAD_83)) {
			projInfo.globe = "Earth";
		} else {
			String g = properties.getProperty("DefaultGlobe");
			if (g != null) {
				projInfo.globe = g;
			} else {
				projInfo.globe = "Earth";
			}
		}
		projInfo.gcsCitation = geogCitation;
		projInfo.pcsCitation = citation;

		double[] tp = getTIFFFieldDouble(MODEL_TIEPOINT_TAG, false, null);
		double[] scale = getTIFFFieldDouble(MODEL_PIXEL_SCALE_TAG, false, null);
		double[] trans = getTIFFFieldDouble(MODEL_TRANSFORMATION_TAG, false, null);
		if ((tp == null) && (trans == null)) {
			throw new IllegalArgumentException(
				"Model tiepoint and transformation tags are missing.  Cannot determine coordinate transformation.");
		}

		// Use Tie Point
		if (tp != null) {
			if (tp.length < 6) {
				throw new IllegalArgumentException("Tiepoint has < 6 elements.");
			}
			projInfo.tiePoint = new double[] { tp[3], tp[4], tp[5] };

			// Use Pixel Scale
			if (scale == null) {
				scale = new double[] { 1, 1, 1 };
			}
			if (scale.length < 3) {
				throw new IllegalArgumentException("Model pixel scale has < 3 elements.");
			}
			if (scale[2] == 0) {
				scale[2] = 1;
			}
			if (isGeographic) {
				if (!isDegrees) {
					radianToDegree(scale);
				}
			}
			projInfo.scale = scale;
		}
		// Use Transformation Matrix
		else {
			projInfo.tiePoint = new double[] { trans[3], trans[7], trans[11] };
			projInfo.scale = new double[] { trans[0], trans[5], trans[10] };
			if (projInfo.scale[2] == 0) {
				projInfo.scale[2] = 1;
			}
		}

		// Raster Type (see section 2.5.2.2 of GeoTIFF spec)
		// DERT and LayerFactory work with "pixel is point" because OpenGL renders each
		// pixel at a vertex for both the DEM and the orthoimage texture.
		// Convert "pixel is area" to "pixel is point" by shifting the
		// tie point right and down by 1/2 pixel
		Integer rasterType = (Integer) geoKeyMap.get(KeyID.RasterType);
		if (rasterType == GeoKey.Code_RasterPixelIsArea) {
			projInfo.tiePoint[0] += 0.5 * projInfo.scale[0];
			projInfo.tiePoint[1] -= 0.5 * projInfo.scale[1];
		}

		if (isGeographic) {
			if (!isDegrees) {
				radianToDegree(projInfo.tiePoint);
			}
			clipLonLat(projInfo.tiePoint);
		}
		return (projInfo);
	}

	/**
	 * Set the projection information from the ProjectionInfo object.
	 * 
	 * @param projInfo projection info object
	 */
	public void setProjectionInfo(ProjectionInfo projInfo) {
		geoKeyMap = new HashMap<KeyID,Object>();
		
		// use "pixel is area" as it is the GDAL default
		geoKeyMap.put(KeyID.RasterType, new Integer(GeoKey.Code_RasterPixelIsArea));
		geoKeyMap.put(KeyID.GeogAngularUnits, new Integer(GeoKey.Code_Angular_Degree));
		geoKeyMap.put(KeyID.GeogLinearUnits, new Integer(GeoKey.Code_Linear_Meter));
		geoKeyMap.put(KeyID.ProjLinearUnits, new Integer(GeoKey.Code_Linear_Meter));

		// Coordinate Transform
		if (projInfo.projected)
			geoKeyMap.put(KeyID.ModelType, new Integer(GeoKey.Code_ModelTypeProjected));
		else
			geoKeyMap.put(KeyID.ModelType, new Integer(GeoKey.Code_ModelTypeGeographic));

		rasterWidth = projInfo.rasterWidth;
		rasterLength = projInfo.rasterLength;
		if (!projInfo.projected) {
			geoKeyMap.put(KeyID.GeographicType, new Integer(projInfo.gcsCode));
			geoKeyMap.put(KeyID.GeogGeodeticDatum, new Integer(projInfo.datumCode));
			geoKeyMap.put(KeyID.GeogEllipsoid, new Integer(projInfo.ellipsoidCode));
			geoKeyMap.put(KeyID.GeogPrimeMeridian, new Integer(projInfo.primeMeridianCode));
			geoKeyMap.put(KeyID.GeogSemiMajorAxis, new Double(projInfo.semiMajorAxis));
			geoKeyMap.put(KeyID.GeogSemiMinorAxis, new Double(projInfo.semiMinorAxis));
			geoKeyMap.put(KeyID.GeogInvFlattening, new Double(projInfo.inverseFlattening));
			geoKeyMap.put(KeyID.GeogPrimeMeridianLong, new Double(projInfo.gcsPrimeMeridianLon));
		}
		else {
			if (projInfo.gcsCode > 0)
				geoKeyMap.put(KeyID.GeographicType, new Integer(projInfo.gcsCode));
			if (projInfo.pcsCode > 0)
				geoKeyMap.put(KeyID.ProjectedCSType, new Integer(projInfo.pcsCode));
			geoKeyMap.put(KeyID.Projection, new Integer(projInfo.projCode));
			geoKeyMap.put(KeyID.ProjCoordTrans, new Integer(projInfo.coordTransformCode));
			if (!Double.isNaN(projInfo.centerLon))
				geoKeyMap.put(KeyID.ProjCenterLong, new Double(projInfo.centerLon));
			if (!Double.isNaN(projInfo.centerLat))
				geoKeyMap.put(KeyID.ProjCenterLat, new Double(projInfo.centerLat));
			if (!Double.isNaN(projInfo.stdParallel1))
				geoKeyMap.put(KeyID.ProjStdParallel1, new Double(projInfo.stdParallel1));
			if (!Double.isNaN(projInfo.stdParallel2))
				geoKeyMap.put(KeyID.ProjStdParallel2, new Double(projInfo.stdParallel2));
			if (!Double.isNaN(projInfo.naturalOriginLon))
				geoKeyMap.put(KeyID.ProjNatOriginLong, new Double(projInfo.naturalOriginLon));
			if (!Double.isNaN(projInfo.naturalOriginLat))
				geoKeyMap.put(KeyID.ProjNatOriginLat, new Double(projInfo.naturalOriginLat));
			if (!Double.isNaN(projInfo.falseEasting))
				geoKeyMap.put(KeyID.ProjFalseEasting, new Double(projInfo.falseEasting));
			if (!Double.isNaN(projInfo.falseNorthing))
				geoKeyMap.put(KeyID.ProjFalseNorthing, new Double(projInfo.falseNorthing));
			if (!Double.isNaN(projInfo.centerEasting))
				geoKeyMap.put(KeyID.ProjCenterEasting, new Double(projInfo.centerEasting));
			if (!Double.isNaN(projInfo.centerNorthing))
				geoKeyMap.put(KeyID.ProjCenterNorthing, new Double(projInfo.centerNorthing));
			if (!Double.isNaN(projInfo.scaleAtNaturalOrigin))
				geoKeyMap.put(KeyID.ProjScaleAtNatOrigin, new Double(projInfo.scaleAtNaturalOrigin));
			if (!Double.isNaN(projInfo.azimuth))
				geoKeyMap.put(KeyID.ProjAzimuthAngle, new Double(projInfo.azimuth));
			if (!Double.isNaN(projInfo.straightVertPoleLon))
				geoKeyMap.put(KeyID.ProjStraightVertPoleLong, new Double(projInfo.straightVertPoleLon));
			if (!Double.isNaN(projInfo.scaleAtCenter))
				geoKeyMap.put(KeyID.ProjScaleAtCenter, new Double(projInfo.scaleAtCenter));
			if (!Double.isNaN(projInfo.falseOriginLon))
				geoKeyMap.put(KeyID.ProjFalseOriginLong, new Double(projInfo.falseOriginLon));
			if (!Double.isNaN(projInfo.falseOriginLat))
				geoKeyMap.put(KeyID.ProjFalseOriginLat, new Double(projInfo.falseOriginLat));			
		}

		if (projInfo.pcsCitation != null)
			geoKeyMap.put(KeyID.Citation, projInfo.pcsCitation);			
		if (projInfo.gcsCitation != null)
			geoKeyMap.put(KeyID.GeogCitation, projInfo.gcsCitation);				
		
		// convert to "pixel is area" by shifting tie point left and up by 1/2 pixel
		double[] tp = new double[] {0,0,0, projInfo.tiePoint[0]-(0.5*projInfo.scale[0]),projInfo.tiePoint[1]+(0.5*projInfo.scale[1]),projInfo.tiePoint[2]};  
		double[] scale = new double[] {projInfo.scale[0], projInfo.scale[1], projInfo.scale[2]};  

		setTIFFFieldDouble(MODEL_TIEPOINT_TAG, tp);
		setTIFFFieldDouble(MODEL_PIXEL_SCALE_TAG, scale);
		
		Object[] result = GeoKey.unmapKeys(geoKeyMap);
		setTIFFFieldShort(GEO_KEY_DIRECTORY_TAG, (short[])(result[0]));
		setTIFFFieldDouble(GEO_DOUBLE_PARAMS_TAG, (double[])(result[1]));
		setTIFFFieldString(GEO_ASCII_PARAMS_TAG, (String)(result[2]));		
	}

	/**
	 * Get the value of a string TIFF tag field.
	 * 
	 * @return the value
	 */
	public String getTIFFFieldString(int tag, boolean strict, String defVal) {
		String[] val = new String[1];
		int n = getTIFFFieldString(handle, tag, val);
		if (n > 0) {
			return (val[0]);
		} else if (strict) {
			throw new IllegalStateException("TIFF Tag " + tag + " not found.");
		} else {
			return (defVal);
		}
	}

	/**
	 * Set the value of a string TIFF tag field.
	 * 
	 * @return success
	 */
	public boolean setTIFFFieldString(int tag, String value) {
		return (setTIFFFieldString(handle, tag, value));
	}

	/**
	 * Get the value of a short TIFF tag field.
	 * 
	 * @return the value
	 */
	public short getTIFFFieldShort(int tag, boolean strict, short defVal) {
		short[] val = new short[1];
		int n = getTIFFFieldShort(handle, tag, val);
		if (n > 0) {
			return (val[0]);
		} else if (strict) {
			throw new IllegalStateException("TIFF Tag " + tag + " not found.");
		} else {
			return (defVal);
		}
	}

	/**
	 * Get the value of a short array TIFF tag field.
	 * 
	 * @return the value
	 */
	public short[] getTIFFFieldShort(int tag, boolean strict, short[] defVal) {
		short[] val = new short[65536];
		int n = getTIFFFieldShort(handle, tag, val);
		if (n > 0) {
			short[] value = new short[n];
			System.arraycopy(val, 0, value, 0, n);
			return (value);
		} else if (strict) {
			throw new IllegalStateException("TIFF Tag " + tag + " not found.");
		} else {
			return (defVal);
		}
	}

	/**
	 * Set the value of a short TIFF tag field.
	 * 
	 * @return success
	 */
	public boolean setTIFFFieldShort(int tag, short value) {
		return (setTIFFFieldShort(handle, tag, value));
	}

	/**
	 * Set the value of a short array TIFF tag field.
	 * 
	 * @return success
	 */
	public boolean setTIFFFieldShort(int tag, short[] value) {
		return (setTIFFFieldShortArray(handle, tag, value));
	}

	/**
	 * Get the value of an int TIFF tag field.
	 * 
	 * @return the value
	 */
	public int getTIFFFieldInt(int tag, boolean strict, int defVal) {
		int[] val = new int[1];
		if (getTIFFFieldInt(handle, tag, val) > 0) {
			return (val[0]);
		} else if (strict) {
			throw new IllegalStateException("TIFF Tag " + tag + " not found.");
		} else {
			return (defVal);
		}
	}

	/**
	 * Get the value of an int array TIFF tag field.
	 * 
	 * @return the value
	 */
	public int[] getTIFFFieldInt(int tag, boolean strict, int[] defVal) {
		int[] val = new int[65536];
		int n = getTIFFFieldInt(handle, tag, val);
		if (n > 0) {
			int[] value = new int[n];
			System.arraycopy(val, 0, value, 0, n);
			return (value);
		} else if (strict) {
			throw new IllegalStateException("TIFF Tag " + tag + " not found.");
		} else {
			return (defVal);
		}
	}

	/**
	 * Set the value of a int TIFF tag field.
	 * 
	 * @return success
	 */
	public boolean setTIFFFieldInt(int tag, int value) {
		return (setTIFFFieldInt(handle, tag, value));
	}

	/**
	 * Set the value of a int array TIFF tag field.
	 * 
	 * @return success
	 */
	public boolean setTIFFFieldInt(int tag, int[] value) {
		return (setTIFFFieldIntArray(handle, tag, value));
	}

	/**
	 * Get the value of a double TIFF tag field.
	 * 
	 * @return the value
	 */
	public double getTIFFFieldDouble(int tag, boolean strict, double defVal) {
		double[] val = new double[1];
		if (getTIFFFieldDouble(handle, tag, val) > 0) {
			return (val[0]);
		} else if (strict) {
			throw new IllegalStateException("TIFF Tag " + tag + " not found.");
		} else {
			return (defVal);
		}
	}

	/**
	 * Get the value of a double array TIFF tag field.
	 * 
	 * @return the value
	 */
	public double[] getTIFFFieldDouble(int tag, boolean strict, double[] defVal) {
		double[] val = new double[65536];
		int n = getTIFFFieldDouble(handle, tag, val);
		if (n > 0) {
			double[] value = new double[n];
			System.arraycopy(val, 0, value, 0, n);
			return (value);
		} else if (strict) {
			throw new IllegalStateException("TIFF Tag " + tag + " not found.");
		} else {
			return (defVal);
		}
	}

	/**
	 * Set the value of a double TIFF tag field.
	 * 
	 * @return success
	 */
	public boolean setTIFFFieldDouble(int tag, double value) {
		return (setTIFFFieldDouble(handle, tag, value));
	}

	/**
	 * Set the value of a double array TIFF tag field.
	 * 
	 * @return success
	 */
	public boolean setTIFFFieldDouble(int tag, double[] value) {
		return (setTIFFFieldDoubleArray(handle, tag, value));
	}

	/**
	 * Get the value of a float TIFF tag field.
	 * 
	 * @return the value
	 */
	public float getTIFFFieldFloat(int tag, boolean strict, float defVal) {
		float[] val = new float[1];
		if (getTIFFFieldFloat(handle, tag, val) > 0) {
			return (val[0]);
		} else if (strict) {
			throw new IllegalStateException("TIFF Tag " + tag + " not found.");
		} else {
			return (defVal);
		}
	}

	/**
	 * Get the value of a float array TIFF tag field.
	 * 
	 * @return the value
	 */
	public float[] getTIFFFieldFloat(int tag, boolean strict, float[] defVal) {
		float[] val = new float[65536];
		int n = getTIFFFieldFloat(handle, tag, val);
		if (n > 0) {
			float[] value = new float[n];
			System.arraycopy(val, 0, value, 0, n);
			return (value);
		} else if (strict) {
			throw new IllegalStateException("TIFF Tag " + tag + " not found.");
		} else {
			return (defVal);
		}
	}

	/**
	 * Set the value of a float TIFF tag field.
	 * 
	 * @return success
	 */
	public boolean setTIFFFieldFloat(int tag, float value) {
		return (setTIFFFieldFloat(handle, tag, value));
	}

	/**
	 * Set the value of a float array TIFF tag field.
	 * 
	 * @return success
	 */
	public boolean setTIFFFieldFloat(int tag, float[] value) {
		return (setTIFFFieldFloatArray(handle, tag, value));
	}

	/**
	 * Get the value of a GeoTIFF tag, given the code.
	 * 
	 * @param code
	 * @return the value
	 */
	protected final int getGTIFKeyInt(KeyID code, int defaultValue) {
		Integer val = (Integer) geoKeyMap.get(code);
		if (val != null) {
			return (val);
		}
		return (defaultValue);
	}

	protected final double getGTIFKeyDouble(KeyID code, double defaultValue) {
		Double val = (Double) geoKeyMap.get(code);
		if (val != null) {
			return (val);
		}
		return (defaultValue);
	}

	protected final String getGTIFKeyASCII(KeyID code, String defaultValue) {
		String val = (String) geoKeyMap.get(code);
		if (val != null) {
			return (val);
		}
		return (defaultValue);
	}

	/**
	 * Get the image tile width.
	 * 
	 * @return the tile width
	 */
	public int getTileWidth() {
		return (tileWidth);
	}

	/**
	 * Get the image tile length.
	 * 
	 * @return the tile length
	 */
	public int getTileLength() {
		return (tileLength);
	}

	/**
	 * Get the number of rows per strip.
	 * 
	 * @return the tile length
	 */
	public int getRowsPerStrip() {
		return (rowsPerStrip);
	}

	/**
	 * Is the GeoTIFF tiled?
	 * 
	 * @return true or false
	 */
	public boolean isTiled() {
		return (isTiled);
	}

	/**
	 * Is the GeoTIFF floating point?
	 * 
	 * @return true or false
	 */
	public boolean isFloatingPoint() {
		return ((sampleFormat == SAMPLEFORMAT_IEEEFP));
	}

	/**
	 * Get the minimum sample value from the file and convert it to double.
	 * 
	 * @return the minimum sample value
	 */
	protected double[] findMinimumSampleValue() {
		// Although the TIFF documentation claims that smaxsamplevalue and sminsamplevalue handle samplesPerPixel
		// values (one for each sample) and all datatypes, libtiff actually only stores and retrieves a
		// single double. See http://maptools-org.996276.n3.nabble.com/TIFFTAG-SMINSAMPLEVALUE-and-TIFFTAG-SMAXSAMPLEVALUE-td171.html.
		double min = getTIFFFieldDouble(TIFFTAG_SMINSAMPLEVALUE, false, Double.NaN);
		if (Double.isNaN(min))
			return(null);
		double[] d = new double[samplesPerPixel];
		for (int i=0; i<samplesPerPixel; ++i)
			d[i] = min;
		return(d);
	}

	/**
	 * Get the maximum sample value from the file and convert it to double.
	 * 
	 * @return the maximum sample value
	 */
	protected double[] findMaximumSampleValue() {
		// Although the TIFF documentation claims that smaxsamplevalue and sminsamplevalue handle samplesPerPixel
		// values (one for each sample) and all datatypes, libtiff actually only stores and retrieves a
		// single double. See http://maptools-org.996276.n3.nabble.com/TIFFTAG-SMINSAMPLEVALUE-and-TIFFTAG-SMAXSAMPLEVALUE-td171.html.
		double max = getTIFFFieldDouble(TIFFTAG_SMAXSAMPLEVALUE, false, Double.NaN);
		if (Double.isNaN(max))
			return(null);
		double[] d = new double[samplesPerPixel];
		for (int i=0; i<samplesPerPixel; ++i)
			d[i] = max;
		return(d);
	}

	/**
	 * Get the number of strips.
	 * 
	 * @return number of strips
	 */
	public int getStripCount() {
		return (stripCount);
	}

	/**
	 * Get the strip size.
	 * 
	 * @return strip size in bytes
	 */
	public long getStripSize() {
		return (stripSize);
	}

	/**
	 * Read a strip from the file.
	 * 
	 * @param stripNumber
	 *            the raw strip number
	 * @param buffer
	 *            a buffer to contain the data
	 * @param size
	 *            the size of the data to load
	 * @return the actual bytes read or -1 for error
	 */
	public long readStrip(int stripNumber, ByteBuffer buffer, long size) {
		if (handle == 0) {
			throw new IllegalStateException("No open file.");
		}
		return (readStrip(handle, stripNumber, buffer, size));
	}

	/**
	 * Write a strip to the file.
	 * 
	 * @param stripNumber
	 *            the raw strip number
	 * @param buffer
	 *            a buffer that contains the data
	 * @param size
	 *            the size of the data to write
	 * @return the actual bytes written or -1 for error
	 */
	public long writeStrip(int stripNumber, ByteBuffer buffer, long size) {
		if (handle == 0) {
			throw new IllegalStateException("No open file.");
		}
		return (writeStrip(handle, stripNumber, buffer, size));
	}

	/**
	 * Get the number of tiles.
	 * 
	 * @return number of tiles
	 */
	public int getTileCount() {
		return (tileCount);
	}

	/**
	 * Get the tile size.
	 * 
	 * @return tile size in bytes
	 */
	public long getTileSize() {
		return (tileSize);
	}

	/**
	 * Read a tile from the file.
	 * 
	 * @param tileNumber
	 *            the raw tile number
	 * @param buffer
	 *            a buffer to contain the data
	 * @param size
	 *            the size of the data to load
	 * @return the actual bytes read or -1 for error
	 */
	public long readTile(int tileNumber, ByteBuffer buffer, long size) {
		if (handle == 0) {
			throw new IllegalStateException("No open file.");
		}
		return (readTile(handle, tileNumber, buffer, size));
	}

	/**
	 * Write a tile to the file.
	 * 
	 * @param tileNumber
	 *            the raw strip number
	 * @param buffer
	 *            a buffer that contains the data
	 * @param size
	 *            the size of the data to write
	 * @return the actual bytes written or -1 for error
	 */
	public long writeTile(int tileNumber, ByteBuffer buffer, long size) {
		if (handle == 0) {
			throw new IllegalStateException("No open file.");
		}
		return (writeTile(handle, tileNumber, buffer, size));
	}

	/**
	 * Find the data type of the file.
	 * 
	 * @return
	 */
	protected DataType findDataType() {

		// get bits per sample
		int bitsPerSample = getTIFFFieldInt(TIFFTAG_BITSPERSAMPLE, true, 0);

		bytesPerSample = bitsPerSample / 8;
		if (bytesPerSample == 0) {
			bytesPerSample = 1;
		}

		// look at the bitsPerSample and sampleFormat to determine data type
		switch (bitsPerSample) {
		case 4:
		case 8:
			switch (sampleFormat) {
			case SAMPLEFORMAT_UINT:
				return (DataType.UnsignedByte);
			case SAMPLEFORMAT_INT:
				return (DataType.Byte);
			default:
				return (DataType.UnsignedByte);
			}
		case 16:
			switch (sampleFormat) {
			case SAMPLEFORMAT_UINT:
				return (DataType.UnsignedShort);
			case SAMPLEFORMAT_INT:
				return (DataType.Short);
			default:
				return (DataType.UnsignedShort);
			}
		case 24:
			switch (sampleFormat) {
			case SAMPLEFORMAT_UINT:
				return (DataType.UnsignedInteger);
			case SAMPLEFORMAT_INT:
				return (DataType.Integer);
			default:
				return (DataType.UnsignedInteger);
			}
		case 32:
			switch (sampleFormat) {
			case SAMPLEFORMAT_UINT:
				return (DataType.UnsignedInteger);
			case SAMPLEFORMAT_INT:
				return (DataType.Integer);
			case SAMPLEFORMAT_IEEEFP:
				return (DataType.Float);
			default:
				return (DataType.UnsignedInteger);
			}
		case 64:
			switch (sampleFormat) {
			case SAMPLEFORMAT_UINT:
				return (DataType.Unknown);
			case SAMPLEFORMAT_INT:
				return (DataType.Long);
			case SAMPLEFORMAT_IEEEFP:
				return (DataType.Double);
			default:
				return (DataType.Unknown);
			}
		default:
			System.err.println("Unsupported data type: bits per sample = " + bitsPerSample + ", sample format code = "
				+ sampleFormat);
			return (DataType.Unknown);
		}
	}

	/**
	 * Get the planar configuration from the TIFF tag.
	 * 
	 * @return the planar configuration
	 */
	public int getPlanarConfiguration() {
		return (planarConfiguration);
	}

	/**
	 * Load entire file into a raster.
	 * 
	 * @param raster
	 */
	@Override
	public void load(Raster raster) {

		// TIFF is organized in tiles.
		if (isTiled()) {
			int n = getTileCount();
			int w = getTileWidth() * samplesPerPixel; // multiply by samples per
														// pixel to get true
														// scanline width
			int h = getTileLength();
			long s = getTileSize(); // size of tile in bytes

			// Java limits ByteBuffer sizes
			if (s > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Cannot load floating point TIFF file with tile size > "
					+ Integer.MAX_VALUE + ".");
			}

			if ((minimum == null) || (maximum == null)) {
				computeMinMaxFromTile(dataType, n, (int) s);
			}

			loadFromTile(dataType, n, (int) s, w, h, raster, false);
		}

		// TIFF is organized in strips.
		else {
			int n = getStripCount();
			int w = rasterWidth * samplesPerPixel; // multiply by samples
													// per pixel to get
													// true scanline
													// width
			long s = getStripSize(); // size of strip in bytes

			// Java limits ByteBuffer sizes
			if (s > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Cannot load floating point TIFF file with strip size > "
					+ Integer.MAX_VALUE + ".");
			}

			if ((minimum == null) || (maximum == null)) {
				computeMinMaxFromStrip(dataType, n, (int) s);
			}

			loadFromStrip(dataType, n, (int) s, w, raster, false);
		}
	}

	/**
	 * Load entire height map file into a raster.
	 * 
	 * @param raster
	 */
	@Override
	public void loadHeightMap(Raster raster) {
		load(raster);
//		// TIFF is organized in tiles.
//		if (isTiled()) {
//			int n = getTileCount();
//			int w = getTileWidth() * samplesPerPixel; // multiply by samples per
//														// pixel to get true
//														// scanline width
//			int h = getTileLength();
//			long s = getTileSize(); // size of tile in bytes
//
//			// Java limits ByteBuffer sizes
//			if (s > Integer.MAX_VALUE) {
//				throw new IllegalArgumentException("Cannot load floating point TIFF file with tile size > "
//					+ Integer.MAX_VALUE + ".");
//			}
//
//			if ((minimum == null) || (maximum == null)) {
//				computeMinMaxFromTile(dataType, n, (int) s);
//			}
//
//			loadFromTile(dataType, n, (int) s, w, h, raster, false);
//		}
//
//		// TIFF is organized in strips.
//		else {
//			int n = getStripCount();
//			int w = rasterWidth * samplesPerPixel; // multiply by samples
//													// per pixel to get
//													// true scanline
//													// width
//			long s = getStripSize(); // size of strip in bytes
//
//			// Java limits ByteBuffer sizes
//			if (s > Integer.MAX_VALUE) {
//				throw new IllegalArgumentException("Cannot load floating point TIFF file with strip size > "
//					+ Integer.MAX_VALUE + ".");
//			}
//
//			if ((minimum == null) || (maximum == null)) {
//				computeMinMaxFromStrip(dataType, n, (int) s);
//			}
//
//			loadFromStrip(dataType, n, (int) s, w, raster, false);
//		}
	}

	/**
	 * Load entire file contents into byte array. File data type must be short,
	 * unsigned short, byte, or unsigned byte.
	 */
	@Override
	public void loadRGBA(Raster raster) {

		// TIFF is organized in tiles.
		if (isTiled()) {
			int n = getTileCount();
			int w = getTileWidth();
			int h = getTileLength();
			long s = getTileSize(); // size of tile in bytes

			// Java limits ByteBuffer sizes
			if (s > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Cannot load TIFF file with tile size > " + Integer.MAX_VALUE + ".");
			}

			// Allocate memory directly
			ByteBuffer bbuf = ByteBuffer.allocateDirect((int) s);
			bbuf.order(byteOrder);
			byte[] bArray = new byte[(int) s];
			// ByteBuffer bbuf = ByteBuffer.wrap(bArray);

			// Read each tile and place it in the full size raster.
			int r = 0, c = 0; // row and column pixels of the upper left corner
								// of the tile
			for (int i = 0; i < n; ++i) {
				boolean success = readRGBATile(handle, r, c, bbuf);
				if (!success) {
					throw new IllegalStateException(getTIFFError());
				}
				bbuf.rewind();
				bbuf.get(bArray);
				int wid = Math.min(w, rasterWidth - c);
				int hgt = Math.min(h, rasterLength - r);
				// raster.set(r, c, wid, hgt, bbuf);
				raster.set(r, c, wid, hgt, bArray);
				c += w;
				if (c >= rasterWidth) {
					c = 0;
					r += h;
				}
				Thread.yield();
			}
		}

		// TIFF is organized in strips.
		else {
			int n = getStripCount();
			int h = getRowsPerStrip(); // number of rows in a strip
			int s = rasterWidth * h * 4; // size of strip in bytes

			// Java limits ByteBuffer sizes
			if (s > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Cannot load TIFF file with strip size > " + Integer.MAX_VALUE + ".");
			}

			// Allocate memory
			ByteBuffer bbuf = ByteBuffer.allocateDirect(s);
			bbuf.order(byteOrder);
			byte[] bArray = new byte[s];
			// ByteBuffer bbuf = ByteBuffer.wrap(bArray);
			bbuf.rewind();

			// Read each strip and place it in the full size raster.
			int r = 0; // row pixel of upper left corner of strip
			for (int i = 0; i < n; ++i) {
				boolean success = readRGBAStrip(handle, r, bbuf);
				if (!success) {
					throw new IllegalStateException(getTIFFError());
				}
				bbuf.rewind();
				// libtiff doesn't flip the RGBA image (only RGB) so we need to
				if (samplesPerPixel == 4)
					ImageUtil.doFlip(bbuf, rasterWidth*4, h);
				bbuf.get(bArray);
				int hgt = Math.min(h, rasterLength - r);
				// raster.set(r, rasterWidth, hgt, bbuf);
				raster.set(r, hgt, bArray);
				r += h;
				Thread.yield();
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
	 * Load entire file into a raster of unsigned byte.
	 * 
	 * @param raster
	 */
	@Override
	public void loadGray(Raster raster) {

		// TIFF is organized in tiles.
		if (isTiled()) {
			int n = getTileCount();
			int w = getTileWidth() * samplesPerPixel; // multiply by samples per
														// pixel to get true
														// scanline width
			int h = getTileLength();
			long s = getTileSize(); // size of tile in bytes

			// Java limits ByteBuffer sizes
			if (s > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Cannot load floating point TIFF file with tile size > "
					+ Integer.MAX_VALUE + ".");
			}

			if ((minimum == null) || (maximum == null)) {
				computeMinMaxFromTile(dataType, n, (int) s);
			}

			loadFromTile(dataType, n, (int) s, w, h, raster, true);
		}

		// TIFF is organized in strips.
		else {
			int n = getStripCount();
			int w = rasterWidth * samplesPerPixel; // multiply by samples
													// per pixel to get
													// true scanline
													// width
			long s = getStripSize(); // size of strip in bytes

			// Java limits ByteBuffer sizes
			if (s > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Cannot load floating point TIFF file with strip size > "
					+ Integer.MAX_VALUE + ".");
			}

			if ((minimum == null) || (maximum == null)) {
				computeMinMaxFromStrip(dataType, n, (int) s);
			}

			loadFromStrip(dataType, n, (int) s, w, raster, true);
		}
		minimum = new double[] { 1 };
		maximum = new double[] { 255 };
	}

	/**
	 * Load bytes from a strip into a raster.
	 * 
	 * @param n
	 *            the number of strips
	 * @param size
	 *            the strip size
	 * @param w
	 *            the strip width
	 * @param raster
	 *            the raster
	 */
	protected final void loadFromStrip(int n, int size, int w, Raster raster) {

		// Allocate memory
		byte[] bArray = new byte[size];
		ByteBuffer bbuf = ByteBuffer.allocateDirect(size);
		// ByteBuffer bbuf = ByteBuffer.wrap(bArray);
		bbuf.order(byteOrder);

		// Read each strip and place it in the full size raster.
		int row = 0; // row pixel of upper left corner of strip
		int h = 0;
		for (int i = 0; i < n; ++i) {
			bbuf.rewind();
			long as = readStrip(i, bbuf, size);
			if (as == -1) {
				throw new IllegalStateException(getTIFFError());
			}
			bbuf.rewind();
			bbuf.get(bArray);
			h = (int) (as / (w * bytesPerSample)); // compute the height of the
													// strip
			// raster.set(row, rasterWidth, h, bbuf);
			raster.set(row, h, bArray);
			row += h;
			Thread.yield();
		}
	}

	/**
	 * Load data from a strip into a raster. This method converts to float or unsigned
	 * byte depending on the value of 'gray'.
	 * 
	 * @param dataType
	 *            the strip data type
	 * @param n
	 *            the number of strips
	 * @param size
	 *            the strip size
	 * @param w
	 *            the strip width
	 * @param raster
	 *            the raster
	 * @param gray
	 * 			  these are gray scale pixels (0-255), convert to unsigned byte
	 */
	protected final void loadFromStrip(DataType dataType, int n, int size, int w, Raster raster, boolean gray) {

		// Allocate memory directly
		ByteBuffer bbuf = ByteBuffer.allocateDirect(size);
		bbuf.order(byteOrder);

		// Read each strip and place it in the raster.
		int row = 0; // row pixel of upper left corner of strip
		int h = 0;
		for (int i = 0; i < n; ++i) {
			bbuf.rewind();
			long as = readStrip(i, bbuf, size);
			if (as == -1) {
				throw new IllegalStateException(getTIFFError());
			}
			bbuf.rewind();
			h = (int) (as / (w * bytesPerSample)); // compute the height of the
													// strip
			if (gray) {
				raster.setAsGray(row, 0, rasterWidth, h, bbuf, dataType, minimum, maximum, missing);
			} else {
				raster.setAsFloat(row, 0, rasterWidth, h, bbuf, dataType, 1f, minimum, maximum, missing);
			}
			row += h;
			Thread.yield();
		}
	}

	/**
	 * Load data from a tile into a raster.
	 * 
	 * @param n
	 *            the number of tiles
	 * @param size
	 *            the tile size
	 * @param w
	 *            the tile width
	 * @param h
	 *            the tile height
	 * @param raster
	 *            the raster
	 */
	protected final void loadFromTile(int n, int size, int w, int h, Raster raster) {

		// Allocate memory
		ByteBuffer bbuf = ByteBuffer.allocateDirect(size);
		byte[] bArray = new byte[size];
		// ByteBuffer bbuf = ByteBuffer.wrap(bArray);
		bbuf.order(byteOrder);

		// Read each tile and place it in the full size raster.
		int row = 0, left = 0, wid = 0, hgt = 0; // row and column pixels of the
													// upper left corner of the
													// tile
		for (int i = 0; i < n; ++i) {
			bbuf.rewind();
			long len = readTile(i, bbuf, size);
			if (len == -1) {
				throw new IllegalStateException(getTIFFError());
			}
			bbuf.rewind();
			bbuf.get(bArray);
			wid = Math.min(w, rasterWidth - left);
			hgt = Math.min(h, rasterLength - row);
			// raster.set(row, left, wid, hgt, bbuf);
			raster.set(row, left, wid, hgt, bArray);
			left += w;
			if (left >= rasterWidth) {
				left = 0;
				row += h;
			}
			Thread.yield();
		}

	}

	/**
	 * Load data from a tile into a raster.
	 * 
	 * @param dataType
	 *            the tile data type
	 * @param n
	 *            the number of tiles
	 * @param size
	 *            the tile size
	 * @param w
	 *            the tile width
	 * @param h
	 *            the tile height
	 * @param raster
	 *            the raster
	 * @param gray
	 *            convert pixels to gray scale unsigned bytes
	 */
	protected final void loadFromTile(DataType dataType, int n, int size, int w, int h, Raster raster, boolean gray) {
		// System.err.println("GTIF.loadFromTile "+dataType+" "+gray+" "+byteOrder+" "+ByteOrder.nativeOrder()+" "+missing);

		// Allocate memory
		ByteBuffer bbuf = ByteBuffer.allocateDirect(size);
		bbuf.order(byteOrder);

		// Read each tile and place it in the full size raster.
		int row = 0, left = 0, wid = 0, hgt = 0; // row and column pixels of the
													// upper left corner of the
													// tile
		for (int i = 0; i < n; ++i) {
			bbuf.rewind();
			long len = readTile(i, bbuf, size);
			if (len == -1) {
				throw new IllegalStateException(getTIFFError());
			}
			bbuf.rewind();
			wid = Math.min(w, rasterWidth - left);
			hgt = Math.min(h, rasterLength - row);
			if (gray) {
				raster.setAsGray(row, left, wid, hgt, bbuf, dataType, minimum, maximum, missing);
			} else {
				raster.setAsFloat(row, left, wid, hgt, bbuf, dataType, 1f, minimum, maximum, missing);
			}
			left += w;
			if (left >= rasterWidth) {
				left = 0;
				row += h;
			}
			Thread.yield();
		}

	}

	/**
	 * Compute the minimum and maximum of an entire file.
	 * 
	 * @param dataType
	 *            the strip data type
	 * @param n
	 *            the number of strips
	 * @param size
	 *            the strip size
	 */
	protected final void computeMinMaxFromStrip(DataType dataType, int n, int size) {

		minimum = new double[samplesPerPixel];
		maximum = new double[samplesPerPixel];
		Arrays.fill(minimum, Double.MAX_VALUE);
		Arrays.fill(maximum, -Double.MAX_VALUE);

		// Allocate memory directly
		ByteBuffer bbuf = ByteBuffer.allocateDirect(size);
		bbuf.order(byteOrder);

		// Read each strip and place it in the full size raster.
		for (int i = 0; i < n; ++i) {
			bbuf.rewind();
			long as = readStrip(i, bbuf, size);
			if (as == -1) {
				throw new IllegalStateException(getTIFFError());
			}
			bbuf.rewind();
			computeMinMax(bbuf);
			Thread.yield();
		}
	}

	/**
	 * Compute the minimum and maximum of an entire file.
	 * 
	 * @param dataType
	 *            the tile data type
	 * @param n
	 *            the number of tiles
	 * @param size
	 *            the tile size
	 */
	protected final void computeMinMaxFromTile(DataType dataType, int n, int size) {

		minimum = new double[samplesPerPixel];
		maximum = new double[samplesPerPixel];
		Arrays.fill(minimum, Double.MAX_VALUE);
		Arrays.fill(maximum, -Double.MAX_VALUE);

		// Allocate memory
		ByteBuffer bbuf = ByteBuffer.allocateDirect(size);
		bbuf.order(byteOrder);

		// Read each tile and compute min and max values.
		for (int i = 0; i < n; ++i) {
			bbuf.rewind();
			long len = readTile(i, bbuf, size);
			if (len == -1) {
				throw new IllegalStateException(getTIFFError());
			}
			bbuf.rewind();
			computeMinMax(bbuf);
			Thread.yield();
		}

	}

	private void radianToDegree(double[] ll) {
		if (ll == null) {
			return;
		}
		for (int i = 0; i < ll.length; ++i) {
			ll[i] = Math.toDegrees(ll[i]);
		}
	}

	/**
	 * Clip the angle down to within a range of -180 to 180.
	 * 
	 * @param llb
	 */
	private void clipLonLat(double[] llb) {
		for (int i = 0; i < llb.length; i += 2) {
			if (llb[i] > 180) {
				System.out.print("Found longitude of " + llb[i] + " degrees ...");
				llb[i] -= 360;
				System.out.println(" setting to " + llb[i] + " degrees.");
			}
		}
	}

}
