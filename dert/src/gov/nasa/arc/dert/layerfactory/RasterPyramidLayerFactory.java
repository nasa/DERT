/**

DERT is a viewer for digital terrain models created from data collected during NASA missions.

DERT is Released in under the NASA Open Source Agreement (NOSA) found in the “LICENSE” folder where you
downloaded DERT.

DERT includes 3rd Party software. The complete copyright notice listing for DERT is:

Copyright © 2015 United States Government as represented by the Administrator of the National Aeronautics and
Space Administration.  No copyright is claimed in the United States under Title 17, U.S.Code. All Other Rights
Reserved.

Desktop Exploration of Remote Terrain (DERT) could not have been written without the aid of a number of free,
open source libraries. These libraries and their notices are listed below. Find the complete third party license
listings in the separate “DERT Third Party Licenses” pdf document found where you downloaded DERT in the
LICENSE folder.
 
JogAmp Ardor3D Continuation
Copyright © 2008-2012 Ardor Labs, Inc.
 
JogAmp
Copyright 2010 JogAmp Community. All rights reserved.
 
JOGL Portions Sun Microsystems
Copyright © 2003-2009 Sun Microsystems, Inc. All Rights Reserved.
 
JOGL Portions Silicon Graphics
Copyright © 1991-2000 Silicon Graphics, Inc.
 
Light Weight Java Gaming Library Project (LWJGL)
Copyright © 2002-2004 LWJGL Project All rights reserved.
 
Tile Rendering Library - Brian Paul 
Copyright © 1997-2005 Brian Paul. All Rights Reserved.
 
OpenKODE, EGL, OpenGL , OpenGL ES1 & ES2
Copyright © 2007-2010 The Khronos Group Inc.
 
Cg
Copyright © 2002, NVIDIA Corporation
 
Typecast - David Schweinsberg 
Copyright © 1999-2003 The Apache Software Foundation. All rights reserved.
 
PNGJ - Herman J. Gonzalez and Shawn Hartsock
Copyright © 2004 The Apache Software Foundation. All rights reserved.
 
Apache Harmony - Open Source Java SE
Copyright © 2006, 2010 The Apache Software Foundation.
 
Guava
Copyright © 2010 The Guava Authors
 
GlueGen Portions
Copyright © 2010 JogAmp Community. All rights reserved.
 
GlueGen Portions - Sun Microsystems
Copyright © 2003-2005 Sun Microsystems, Inc. All Rights Reserved.
 
SPICE
Copyright © 2003, California Institute of Technology.
U.S. Government sponsorship acknowledged.
 
LibTIFF
Copyright © 1988-1997 Sam Leffler
Copyright © 1991-1997 Silicon Graphics, Inc.
 
PROJ.4
Copyright © 2000, Frank Warmerdam

LibJPEG - Independent JPEG Group
Copyright © 1991-2018, Thomas G. Lane, Guido Vollbeding
 

Disclaimers

No Warranty: THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY KIND,
EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
THAT THE SUBJECT SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY
WARRANTY THAT THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE. THIS AGREEMENT
DOES NOT, IN ANY MANNER, CONSTITUTE AN ENDORSEMENT BY GOVERNMENT AGENCY OR ANY
PRIOR RECIPIENT OF ANY RESULTS, RESULTING DESIGNS, HARDWARE, SOFTWARE PRODUCTS OR
ANY OTHER APPLICATIONS RESULTING FROM USE OF THE SUBJECT SOFTWARE.  FURTHER,
GOVERNMENT AGENCY DISCLAIMS ALL WARRANTIES AND LIABILITIES REGARDING THIRD-PARTY
SOFTWARE, IF PRESENT IN THE ORIGINAL SOFTWARE, AND DISTRIBUTES IT "AS IS."

Waiver and Indemnity:  RECIPIENT AGREES TO WAIVE ANY AND ALL CLAIMS AGAINST THE UNITED
STATES GOVERNMENT, ITS CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR
RECIPIENT.  IF RECIPIENT'S USE OF THE SUBJECT SOFTWARE RESULTS IN ANY LIABILITIES,
DEMANDS, DAMAGES, EXPENSES OR LOSSES ARISING FROM SUCH USE, INCLUDING ANY DAMAGES
FROM PRODUCTS BASED ON, OR RESULTING FROM, RECIPIENT'S USE OF THE SUBJECT SOFTWARE,
RECIPIENT SHALL INDEMNIFY AND HOLD HARMLESS THE UNITED STATES GOVERNMENT, ITS
CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR RECIPIENT, TO THE EXTENT
PERMITTED BY LAW.  RECIPIENT'S SOLE REMEDY FOR ANY SUCH MATTER SHALL BE THE IMMEDIATE,
UNILATERAL TERMINATION OF THIS AGREEMENT.

**/

package gov.nasa.arc.dert.layerfactory;

import gov.nasa.arc.dert.landscape.io.QuadTreeTile.DataType;
import gov.nasa.arc.dert.landscape.layer.LayerInfo.LayerType;
import gov.nasa.arc.dert.raster.Raster;
import gov.nasa.arc.dert.raster.RasterFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Properties;

import javax.swing.JTextField;

/**
 * Convert a raster layer file such as a DEM or ortho-image to a
 * multi-resolution tiled pyramid. Pixels for new levels are subsampled through
 * averaging. The raster is padded first to extend its size to a power of 2 on
 * each side.
 *
 */
public class RasterPyramidLayerFactory extends PyramidLayerFactory {

	// Dimensions
	protected int rasterWidth, rasterLength;
	protected int tileWidth, tileLength;

	// File we will be reading
	protected RasterFile rasterFile;

	// File to hold the raster with padding on all edges.
	protected File paddedFile;

	// Size of padded raster in pixels
	protected int paddedWidth, paddedLength, leftMargin, topMargin;
	protected int leftInset, topInset;

	// Number of tiles on a side at the highest layer resolution
	protected int numberOfTiles;

	// Number of samples for one pixel.
	protected int bytesPerPixel;

	// Raster file data type
	protected DataType dataType;
	
	// Location of temporary files
	protected String tmpPath;

	/**
	 * Constructor
	 * 
	 * @param rasterFile
	 *            the raster file to be used for the pyramid
	 */
	public RasterPyramidLayerFactory(RasterFile rasterFile, String tmpPath) {
		super(rasterFile.getFilePath());
		this.rasterFile = rasterFile;
		this.tmpPath = tmpPath;
	}

	/**
	 * Build a multi-resolution tiled pyramid to be used in a landscape.
	 * 
	 * @param path
	 *            the pyramid directory
	 * @param globe
	 *            the body this landscape is located on
	 * @param lType
	 *            the type of layer for the pyramid
	 * @param layerName
	 *            the layer name, will be name of subdirectory
	 * @param tileSize
	 *            the size of the pyramid tiles
	 * @param missing
	 *            the missing value argument from the commandline/UI
	 * @param margin
	 *            user specified raster margins
	 * @param messageText
	 *            UI text field for messages (null if headless)
	 */
	public void buildPyramid(String path, String globe, LayerType lType, String layerName, int tileSize,
		String missing, int[] margin, JTextField messageText) throws IOException {

		long t = System.currentTimeMillis();

		doIt = true;

		boolean opened = rasterFile.open("r");
		if (!opened) {
			System.exit(0);
		}
		rasterWidth = rasterFile.getRasterWidth();
		rasterLength = rasterFile.getRasterLength();

		layerType = lType;

		switch (layerType) {
		case none:
		case footprint:
		case viewshed:
		case derivative:
			break;
		case elevation:
			bytesPerPixel = 4;
			break;
		case field:
			bytesPerPixel = 4;
			break;
		case colorimage:
			bytesPerPixel = 4;
			break;
		case grayimage:
			bytesPerPixel = 1;
			break;
		}

		// Determine if we can handle this raster file.
		int samplesPerPixel = rasterFile.getSamplesPerPixel();
		if ((samplesPerPixel > 1) && (layerType != LayerType.colorimage)) {
			throw new IllegalStateException("Cannot handle " + layerType + " layer with multiple samples per pixel.");
		}

		// Padded file size must be a power of 2 x samples per pixel
		findDimensions(margin, tileSize);

		if (tileWidth > paddedWidth) {
			throw new IllegalStateException("Tile width greater than raster width.");
		}

		if (tileLength > paddedLength) {
			throw new IllegalStateException("Tile width greater than raster width.");
		}

		// Get the number of tiles at highest resolution and maximum level
		int numTiles = numberOfTiles;
		int maxLevel = (int) (Math.log(numTiles) / Math.log(2) + 0.5);

		// Build the pyramid
		try {

			// First save the raster file in a quad with a size that is a power
			// of 2
			// Pad and center the raster
			if (messageText != null) {
				messageText.setText("Writing temporary "+paddedWidth+" x "+paddedLength+" file ");
				Thread.yield();
			}
			else
				System.out.println("Writing temporary "+paddedWidth+" x "+paddedLength+" file ");
			Raster raster = createPaddedRaster(path, samplesPerPixel, missing);
			rasterFile.close();
			System.gc();

			// Create a sub-directory for the layer
			File dirFile = new File(path, layerName);
			dirFile.mkdirs();
			String dirPath = dirFile.getAbsolutePath();

			// Write tiles for each level starting at highest resolution
			int columnStep = tileWidth;
			int rowStep = tileLength;
			for (int level = maxLevel; level >= 0; level--) {
				if (!doIt) {
					break;
				}
				int kernelSize = (int) Math.pow(2, (maxLevel - level));
				int rcnt = 0;
				if (messageText == null)
					System.out.println("Writing "+numTiles+" rows for level "+(level+1)+" of "+(maxLevel+1));
				for (int r = 0; r < numTiles; ++r) {
					if (messageText == null) {
						if (rcnt%10 == 0)
							System.out.print(rcnt);
						else 
							System.out.print(".");
						rcnt ++;
					}
					for (int c = 0; c < numTiles; ++c) {
						if (!doIt) {
							break;
						}
						if (messageText != null) {
							messageText.setText("Writing " + layerName + " level " + (level + 1) + " of "
								+ (maxLevel + 1) + ", tile row " + (r + 1) + " of " + numTiles + " . . .");
							Thread.yield();
						}
						String filePath = getTileFilePath(c, r, numTiles, level, dirPath);
						writeTile(raster, c * columnStep, r * rowStep, kernelSize, filePath, layerType);
					}
				}
				if (messageText == null)
					System.out.println();
				numTiles /= 2;
				columnStep *= 2;
				rowStep *= 2;
			}
			raster = null;
			System.gc();

			System.out.println();
			System.out.println("Writing projection info for " + layerName);
			projInfo.rasterWidth = tileWidth * numberOfTiles;
			projInfo.rasterLength = tileLength * numberOfTiles;
			projInfo.tiePoint[0] -= (leftMargin - leftInset) * projInfo.scale[0];
			projInfo.tiePoint[1] += (topMargin - topInset) * projInfo.scale[1];
			if (globe != null) {
				projInfo.globe = globe;
			}
			System.out.println(projInfo);

			if (doIt) {
				writeProperties(new File(dirPath, "layer.properties").getAbsolutePath(), maxLevel + 1, tileWidth,
					tileLength, numberOfTiles);
			}
			System.out.println("Number of levels for " + layerName + " = " + (maxLevel + 1) + " with " + numberOfTiles
				+ " tiles per side at the highest resolution level.");
			int nt = 0;
			int n = 1;
			for (int i = 0; i <= maxLevel; ++i) {
				nt += n;
				n *= 4;
			}

			// add landscape identifier
			Properties landscapeProperties = new Properties();
			File propFile = new File(path, ".landscape");
			if (propFile.exists()) {
				landscapeProperties.load(new FileInputStream(propFile));
			}
			landscapeProperties.setProperty("LastWrite", System.getProperty("user.name"));
			landscapeProperties.store(new FileOutputStream(propFile), null);

			// report
			System.out.println("Total number of tiles for " + layerName + " = " + nt + " using "
				+ ((double)nt*tileWidth*tileLength*bytesPerPixel / 1073741824.0) + " GB.");
			System.out.println("Total time for building " + layerName + " = "
				+ (float) ((System.currentTimeMillis() - t) / 60000.0) + " minutes.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void findDimensions(int[] margin, int tileSize) {
		int rightInset = 0, bottomInset = 0;
		// no adjacent edges
		if (margin == null) {
			// raster width and height must be power of 2
			paddedWidth = (int)nextPowerOf2(rasterWidth);
			paddedLength = (int)nextPowerOf2(rasterLength);
			// User specified tile size is used for longest edge of tile (tiles
			// are rectangular).
			if (paddedLength > paddedWidth) {
				tileLength = tileSize;
				tileWidth = (int) (tileSize * ((double) paddedWidth / paddedLength));
			} else {
				tileWidth = tileSize;
				tileLength = (int) (tileSize * ((double) paddedLength / paddedWidth));
			}
			if ((layerType == LayerType.grayimage) || (layerType == LayerType.colorimage)) {
				leftInset = paddedWidth / tileWidth;
				rightInset = -2 * leftInset;
				topInset = paddedLength / tileLength;
				bottomInset = -2 * topInset;
			} else {
				leftInset = 0;
				rightInset = -paddedWidth / tileWidth;
				topInset = 0;
				bottomInset = -paddedLength / tileLength;
			}
			leftMargin = leftInset + (paddedWidth - rasterWidth) / 2;
			topMargin = topInset + (paddedLength - rasterLength) / 2;
		} else {
			paddedLength = (int)nextPowerOf2(rasterLength + margin[2] + margin[3]) / 2;
			paddedWidth = (int)nextPowerOf2(rasterWidth + margin[0] + margin[1]) / 2;
			topMargin = margin[3];
			leftMargin = margin[0];

			if (paddedLength >= paddedWidth) {
				tileLength = tileSize;
				tileWidth = (int) (tileSize * ((double) paddedWidth / paddedLength));
			} else {
				tileWidth = tileSize;
				tileLength = (int) (tileSize * ((double) paddedLength / paddedWidth));
			}
			if ((layerType == LayerType.grayimage) || (layerType == LayerType.colorimage)) {
				leftInset = paddedWidth / tileWidth;
				rightInset = -2 * leftInset;
				topInset = paddedLength / tileLength;
				bottomInset = -2 * topInset;
			} else {
				leftInset = 0;
				rightInset = -paddedWidth / tileWidth;
				topInset = 0;
				bottomInset = -paddedLength / tileLength;
			}
		}
		numberOfTiles = paddedWidth / tileWidth;

		// padded dimension
		paddedWidth += (leftInset - rightInset);
		paddedLength += (topInset - bottomInset);
	}

	protected Raster createPaddedRaster(String path, int samplesPerPixel, String missing) throws IOException {

		if (!doIt) {
			return (null);
		}

		projInfo = rasterFile.getProjectionInfo();
		float missingValue = Float.NaN;
		if (missing == null) {
			missingValue = rasterFile.getMissingValue();
		} else {
			missingValue = new Float(missing);
			rasterFile.setMissingValue(missingValue);
		}

		// Get the entire raster file contents
		dataType = rasterFile.getDataType();
		if (tmpPath == null)
			tmpPath = path;
		Raster raster = loadRasterFile(tmpPath);
		minimumSampleValue = rasterFile.getMinimumSampleValue();
		maximumSampleValue = rasterFile.getMaximumSampleValue();

		Raster padded = new Raster(paddedWidth, paddedLength, bytesPerPixel, dataType, tmpPath);
		byte[] bbArray = new byte[rasterWidth * bytesPerPixel];

		// add in the raster data
		switch (layerType) {
		case none:
		case footprint:
		case viewshed:
		case derivative:
			break;
		case elevation:
		case field:
			raster.setMissingValuesToNaN(missingValue, minimumSampleValue, maximumSampleValue);
			padded.set(Float.NaN);
			break;
		case colorimage:
		case grayimage:
			padded.set(0);
			break;
		}
		raster.flush();
		padded.flush();

		// transfer raster to padded one row at a time
		for (int i = 0; i < rasterLength; ++i) {
			// don't overwrite the left padding
			raster.get(i, bbArray);
			padded.set(topMargin + i, leftMargin, rasterWidth, 1, bbArray);
		}

		if (layerType == LayerType.elevation) {
			// If the terrain dips below the edge (that is the minimum value < edgeFillValue),
			// the shadows don't work correctly (this is the case with the Victoria Crater landscape).
			// So here we use the minimum value by default.
			// The user can change this in the layer.properties file for the elevation.
//			edgeFillValue = padded.computeEdgeFill();
			edgeFillValue = (float)minimumSampleValue[0];
		}

		raster.dispose();
		return (padded);
	}

	/**
	 * Load a raster file converting to the data type appropriate for the layer.
	 * 
	 * @param path
	 *            the path of the raster file
	 * @return the raster array
	 * @throws IOException
	 */
	protected Raster loadRasterFile(String path) throws IOException {
		Raster raster = new Raster(rasterWidth, rasterLength, bytesPerPixel, dataType, path);
		switch (layerType) {
		case none:
		case footprint:
		case viewshed:
		case derivative:
			break;
		case elevation:
			rasterFile.loadHeightMap(raster);
			this.dataType = DataType.Float;
			return (raster);
		case colorimage:
			rasterFile.loadRGBA(raster);
			this.dataType = DataType.Integer;
			return (raster);
		case field:
			rasterFile.load(raster);
			this.dataType = DataType.Float;
			return (raster);
		case grayimage:
			rasterFile.loadGray(raster);
			this.dataType = DataType.UnsignedByte;
			return (raster);
		}
		raster.flush();
		return (null);
	}

	/**
	 * Write out a tile.
	 * 
	 * @param raster
	 *            the raster array from which the tile comes
	 * @param column
	 *            the column start in the array
	 * @param row
	 *            the row start in the array
	 * @param size
	 *            the tile size (width and height)
	 * @param filePath
	 *            the path for the file
	 * @param layerType
	 *            the type of layer
	 * @throws IOException
	 */
	protected void writeTile(Raster raster, int column, int row, int kernelSize, String filePath, LayerType layerType)
		throws IOException {

		int tWidth = tileWidth + 1;
		int tLength = tileLength + 1;
		int tSize = tLength * tWidth * bytesPerPixel;
		int tileTop = 0;
		int tileLeft = 0;

		int rasterLeft = leftInset + column;
		int rasterTop = topInset + row;
		int rasterTWidth = tWidth * kernelSize;
		int rasterTLength = tLength * kernelSize;

		// allocate buffer for writing the file
		byte[] bbArray = new byte[tSize];
		ByteBuffer bbuf = ByteBuffer.wrap(bbArray);

		// fill the tile
		switch (layerType) {
		case none:
		case footprint:
		case viewshed:
		case derivative:
			break;
		case elevation:
		case field:
		case grayimage:
			raster.get(rasterTop, rasterLeft, rasterTWidth, rasterTLength, bbuf, tileTop, tileLeft, tWidth, kernelSize);
			break;
		case colorimage:
			bbuf.order(ByteOrder.nativeOrder());
			bbuf.rewind();
			raster.getRGBA(rasterTop, rasterLeft, rasterTWidth, rasterTLength, bbuf, tileTop, tileLeft, tWidth,
				kernelSize);
			break;
		}

		// zero the file if it is only missing values
		if (isEmpty(bbuf)) {
			bbuf.limit(0);
		}

		writeTile(filePath, bbArray, tWidth, tLength, layerType);
	}

	/**
	 * Read the padded raster file.
	 * 
	 * @param raster
	 *            the array to put the data in
	 * @param file
	 *            the file path
	 * @param layerType
	 *            the type of layer
	 * @throws IOException
	 */
	protected void readFile(Object raster, File file, LayerType layerType) throws IOException {
		if (!doIt) {
			return;
		}
		InputStream iStream = new FileInputStream(file);
		ByteBuffer bbuf = null;
		byte[] bbArray = null;
		switch (layerType) {
		case none:
		case footprint:
		case viewshed:
		case derivative:
			break;
		case elevation:
		case field:
			float[][] fArray = (float[][]) raster;
			bbArray = new byte[fArray[0].length * 4];
			bbuf = ByteBuffer.wrap(bbArray);
			FloatBuffer fbuf = bbuf.asFloatBuffer();
			for (int i = 0; i < fArray.length; ++i) {
				iStream.read(bbArray);
				bbuf.rewind();
				fbuf.get(fArray[i]);
				fbuf.rewind();
			}
			break;
		case colorimage:
			int[][] iArray = (int[][]) raster;
			bbArray = new byte[iArray[0].length * 4];
			bbuf = ByteBuffer.wrap(bbArray);
			IntBuffer ibuf = bbuf.asIntBuffer();
			for (int i = 0; i < iArray.length; ++i) {
				iStream.read(bbArray);
				bbuf.rewind();
				ibuf.get(iArray[i]);
				ibuf.rewind();
			}
			break;
		case grayimage:
			byte[][] bArray = (byte[][]) raster;
			for (int i = 0; i < bArray.length; ++i) {
				iStream.read(bArray[i]);
			}
			break;
		}
		iStream.close();
	}

	/**
	 * Fill a byte buffer with a padding value.
	 * 
	 * @param bbuf
	 * @param len
	 * @param layerType
	 */
	protected final void fillBufferRow(ByteBuffer bbuf, int wid, int row, LayerType layerType) {
		switch (layerType) {
		case none:
		case footprint:
		case viewshed:
		case derivative:
			break;
		case elevation:
		case field:
			FloatBuffer fbuf = bbuf.asFloatBuffer();
			fbuf.position(row * wid);
			for (int c = 0; c < wid; ++c) {
				fbuf.put(Float.NaN);
			}
			break;
		case colorimage:
			IntBuffer ibuf = bbuf.asIntBuffer();
			ibuf.position(row * wid);
			for (int c = 0; c < wid; ++c) {
				ibuf.put(0);
			}
			break;
		case grayimage:
			bbuf.position(row * wid);
			for (int c = 0; c < wid; ++c) {
				bbuf.put((byte) 0);
			}
			break;
		}
		bbuf.rewind();
	}

	/**
	 * Fill a byte buffer with a padding value.
	 * 
	 * @param bbuf
	 * @param len
	 * @param layerType
	 */
	protected final void fillBufferColumn(ByteBuffer bbuf, int wid, int hgt, int col, LayerType layerType) {
		switch (layerType) {
		case none:
		case footprint:
		case viewshed:
		case derivative:
			break;
		case elevation:
		case field:
			FloatBuffer fbuf = bbuf.asFloatBuffer();
			for (int r = 0; r < hgt; ++r) {
				int c = r * wid + col;
				fbuf.put(c, Float.NaN);
			}
			break;
		case colorimage:
			IntBuffer ibuf = bbuf.asIntBuffer();
			for (int r = 0; r < hgt; ++r) {
				int c = r * wid + col;
				ibuf.put(c, 0);
			}
			break;
		case grayimage:
			for (int r = 0; r < hgt; ++r) {
				int c = r * wid + col;
				bbuf.put(c, (byte) 0);
			}
			break;
		}
		bbuf.rewind();
	}

	/**
	 * Get the next power of two greater than the given value
	 * 
	 * @param val
	 * @return
	 */
	private long nextPowerOf2(long val) {
		long po2 = 1;
		while (po2 < val) {
			po2 *= 2;
		}
		return (po2);
	}

}
