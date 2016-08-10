package gov.nasa.arc.dert.landscape.factory;

import gov.nasa.arc.dert.landscape.LayerInfo.LayerType;
import gov.nasa.arc.dert.raster.ProjectionInfo;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 * Base class for factories that create a multi-resolution tiled pyramid. The
 * structure of the pyramid is a quad that is divided into 4 child quads for
 * each new level. Each quad (called a tile) has the same number of pixels but
 * covers a smaller physical area. The lowest resolution (a single quad) is
 * level 0 and each subsequent level increases in index up to the highest
 * raster.
 * 
 * Each tile has an edge length of a power of 2 plus an extension depending on
 * the type of layer. An elevation tile adds a single pixel to the right and
 * bottom edges to extend to the neighbor tile for stitching purposes. In
 * addition to extending its bottom and right edges, a color or gray image tile
 * extends an addition pixel on all sides for the OpenGL feature of bilinear
 * interpolation for texture borders.
 * 
 * @author lkeelyme
 *
 */
public abstract class PyramidLayerFactory {

	// Projection information from source file or destination landscape
	protected ProjectionInfo projInfo;

	// Path to source raster or vector file
	protected String sourceFilePath;

	// Type of layer to produce
	protected LayerType layerType;

	// Sample value extremes
	protected double[] minimumSampleValue, maximumSampleValue;

	// Value to use to fill to the edge of mesh.
	protected float edgeFillValue;

	// flag for cancellation
	protected boolean doIt;

	/**
	 * Constructor
	 * 
	 * @param sourceFilePath
	 */
	public PyramidLayerFactory(String sourceFilePath) {
		this.sourceFilePath = sourceFilePath;
	}

	protected void writeProperties(String path, int numLevels, int tileWidth, int tileLength, int numTiles)
			throws IOException, FileNotFoundException {
		Properties properties = new Properties();
		properties.setProperty("LayerType", layerType.toString());
		properties.setProperty("NumberOfLevels", Integer.toString(numLevels));
		properties.setProperty("NumberOfTiles", Integer.toString(numTiles));
		properties.setProperty("TileWidth", Integer.toString(tileWidth));
		properties.setProperty("TileLength", Integer.toString(tileLength));
		properties.setProperty("Source", sourceFilePath);
		properties.setProperty("MinimumValue", Double.toString(minimumSampleValue[0]));
		properties.setProperty("MaximumValue", Double.toString(maximumSampleValue[0]));
		properties.setProperty("TileFormat", "PNG");
		projInfo.saveToProperties(properties);
		if (layerType == LayerType.elevation) {
			properties.setProperty("EdgeFillValue", Float.toString(edgeFillValue));
		}

		properties.store(new FileOutputStream(path), LayerFactory.VERSION);
	}

	/**
	 * Determine if a tile is empty
	 * 
	 * @param bbuf
	 * @return
	 */
	protected boolean isEmpty(ByteBuffer bbuf) {
		switch (layerType) {
		case none:
		case footprint:
		case viewshed:
		case derivative:
			break;
		case elevation:
		case field:
			FloatBuffer fbuf = bbuf.asFloatBuffer();
			int fl = fbuf.limit();
			for (int i = 0; i < fl; ++i) {
				if (!Float.isNaN(fbuf.get(i))) {
					return (false);
				}
			}
			break;
		case colorimage:
			IntBuffer ibuf = bbuf.asIntBuffer();
			int il = ibuf.limit();
			for (int i = 0; i < il; ++i) {
				if (ibuf.get(i) != 0) {
					return (false);
				}
			}
			break;
		case grayimage:
			int bl = bbuf.limit();
			for (int i = 0; i < bl; ++i) {
				if (bbuf.get(i) != 0) {
					return (false);
				}
			}
			break;
		}
		return (true);
	}

	/**
	 * Determine the path for a tile.
	 * 
	 * @param column
	 *            the tile column
	 * @param row
	 *            the tile row
	 * @param numTiles
	 *            the number of tiles on a side
	 * @param level
	 *            the pyramid level
	 * @param dirPath
	 *            the directory for the tiles
	 * @return
	 */
	protected String getTileFilePath(int column, int row, int numTiles, int level, String dirPath) {
		numTiles /= 2;
		int xLine = numTiles;
		int yLine = numTiles;
		byte[] id = new byte[level + 1];
		int l = 0;
		while (numTiles > 0) {
			if ((column < xLine) && (row < yLine)) {
				id[l] = 1;
				numTiles /= 2;
				xLine -= numTiles;
				yLine -= numTiles;
			} else if ((column >= xLine) && (row < yLine)) {
				id[l] = 2;
				numTiles /= 2;
				xLine += numTiles;
				yLine -= numTiles;
			} else if ((column < xLine) && (row >= yLine)) {
				id[l] = 3;
				numTiles /= 2;
				xLine -= numTiles;
				yLine += numTiles;
			} else if ((column >= xLine) && (row >= yLine)) {
				id[l] = 4;
				numTiles /= 2;
				xLine += numTiles;
				yLine += numTiles;
			}
			l++;
		}
		id[l] = 0;
		String fileName = "";
		for (int i = 0; i < id.length; ++i) {
			fileName += File.separator + id[i];
		}
		File file = new File(dirPath + fileName);
		return (file.getAbsolutePath());
	}

	/**
	 * User pressed the cancel button
	 */
	public void cancel() {
		doIt = false;
	}

	/**
	 * Write a tile out to the pyramid
	 * 
	 * @param filePath
	 * @param bbArray
	 * @param width
	 * @param height
	 * @param layerType
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected void writeTile(String filePath, byte[] bbArray, int width, int height, LayerType layerType)
		throws FileNotFoundException, IOException {

		if (bbArray.length != 0) {
			BufferedImage bImage = null;
			switch (layerType) {
			case none:
			case footprint:
			case viewshed:
			case derivative:
				break;
			case elevation:
			case field:
				// core PNG does not support 32 bit or floating point
				// write it as a 4 byte color
				bImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
				byte[] fData = ((DataBufferByte) bImage.getRaster().getDataBuffer()).getData();
				System.arraycopy(bbArray, 0, fData, 0, bbArray.length);
				break;
			case colorimage:
				bImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
				byte[] iData = ((DataBufferByte) bImage.getRaster().getDataBuffer()).getData();
				System.arraycopy(bbArray, 0, iData, 0, bbArray.length);
				break;
			case grayimage:
				bImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
				byte[] bData = ((DataBufferByte) bImage.getRaster().getDataBuffer()).getData();
				System.arraycopy(bbArray, 0, bData, 0, bbArray.length);
				break;
			}
			filePath += ".png";
			File file = new File(filePath);
			file.getParentFile().mkdirs();
			ImageOutputStream oStream = new FileImageOutputStream(file);
			ImageIO.write(bImage, "PNG", oStream);
			oStream.flush();
			oStream.close();
		}
		// Empty tile
		else {
			filePath += ".png";
			File file = new File(filePath);
			file.getParentFile().mkdirs();
			ImageOutputStream oStream = new FileImageOutputStream(file);
			oStream.flush();
			oStream.close();
		}
	}

}
