package gov.nasa.arc.dert.io;

import gov.nasa.arc.dert.landscape.QuadTreeTile;
import gov.nasa.arc.dert.raster.RasterFile.DataType;
import gov.nasa.arc.dert.util.ImageUtil;
import gov.nasa.arc.dert.view.Console;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import javax.imageio.ImageIO;

import com.ardor3d.image.Image;

/**
 * Provides an implementation of the TileSource interface for landscapes that
 * reside on the local file system.
 *
 */
public class FileSystemTileSource implements TileSource {

	// Quad tree structure of existing tile ids
	private DepthTree depthTree;

	// Landscape directory
	private String dirName;

	// Map of properties from layer.properties files throughout the landscape
	private HashMap<String, Properties> propertiesMap;

	/**
	 * Constructor
	 * 
	 * @param dirName
	 */
	public FileSystemTileSource() {
	}

	/**
	 * Use this method to see if the directory exists.
	 */
	@Override
	public boolean connect(String location, String userName, String password) {
		dirName = location;
		File file = new File(dirName);
		try {
			file = file.getCanonicalFile();
			return (file.exists());
		} catch (Exception e) {
			e.printStackTrace();
			return (false);
		}
	}

	/**
	 * Get information about each layer.
	 */
	@Override
	public String[][] getLayerInfo() {
		propertiesMap = new HashMap<String, Properties>();
		File dir = new File(dirName);
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("TerrainModel location " + dirName + " is not a directory.");
		}
		File[] file = dir.listFiles();
		if (file == null) {
			file = new File[0];
		}

		// Alphabetical order
		Arrays.sort(file);

		ArrayList<String[]> layers = new ArrayList<String[]>();
		for (int i = 0; i < file.length; ++i) {
			String filename = file[i].getName();
			if (filename.startsWith(".")) {
				continue;
			}
			if (filename.toLowerCase().equals("subpyramid")) {
				continue;
			}
			if (filename.toLowerCase().equals("dert")) {
				continue;
			}
			if (file[i].isDirectory()) {
				Properties prop = loadPropertiesFile(file[i], filename);
				if (prop == null) {
					Console.println("No properties found for layer "+filename+", skipping.");
					continue;
				}
				propertiesMap.put(filename, prop);
				String type = prop.getProperty("LayerType");
				if (type != null) {
					layers.add(new String[] { filename, type, null });
					if (type.equals("elevation"))
						getDepthTree();
				}
			}
		}
		String[][] layerInfo = new String[layers.size()][];
		layers.toArray(layerInfo);
		return (layerInfo);
	}

	private Properties loadPropertiesFile(File dir, String name) {
		File file = new File(dir, "layer.properties");
		if (!file.exists()) {
			return(null);
		}
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(file));
			return (prop);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Get the contents of a layer.properties file.
	 */
	@Override
	public Properties getProperties(String layerName) {
		return (propertiesMap.get(layerName));
	}

	/**
	 * Get the path to the landscape
	 */
	@Override
	public String getPath() {
		return (dirName);
	}

	private boolean tileExists(String layerName, String id) {
		if (depthTree != null) {
			return (tileExists(id, depthTree));
		}
		String fileName = layerPath(layerName) + id + "/0.png";
		File file = new File(fileName);
		try {
			if (file.getCanonicalFile().exists()) {
				return (true);
			}
			return (false);
		} catch (Exception e) {
			return (false);
		}
	}

	/**
	 * Determine if a tile exists.
	 * 
	 * @param id
	 *            the tile id
	 */
	@Override
	public boolean tileExists(String id) {
		return (tileExists(id, depthTree));
	}

	private boolean tileExists(String id, DepthTree dTree) {
		if (id.equals(dTree.id)) {
			return (true);
		}
		if (!id.startsWith(dTree.id)) {
			return (false);
		}
		if (dTree.child == null) {
			return (false);
		}
		for (int i = 0; i < dTree.child.length; ++i) {
			if (tileExists(id, dTree.child[i])) {
				return (true);
			}
		}
		return (false);
	}

	@Override
	public QuadTreeTile getTile(String layerName, String id, DataType dataType) {
		if (tileExists(layerName, id)) {
			return (getTilePng(layerName, id, dataType));
		}
		return (null);
	}

	/**
	 * Given a layer and an id, load the contents of the tile.
	 */
	public QuadTreeTile getTilePng(String layerName, String id, DataType dataType) {
		try {
			String fileName = layerPath(layerName) + id + "/0.png";
			File file = new File(fileName).getCanonicalFile();
			BufferedImage bImage = ImageIO.read(file);
			if (bImage != null) {
				// System.err.println("FileSystemTileSource.getTilePng "+layerName+" "+dataType+" "+bImage.getWidth()+" "+bImage.getHeight());
				int numBands = bImage.getData().getNumBands();
				if (dataType == DataType.Float) {
					DataBufferByte dBuf = (DataBufferByte) bImage.getData().getDataBuffer();
					byte[] bytes = dBuf.getData();
					ByteBuffer bBuf = ByteBuffer.wrap(bytes);
					QuadTreeTile tile = new QuadTreeTile(bBuf, id, bImage.getWidth(), bImage.getHeight(), dataType,
						numBands);
					return (tile);
				}
				// Spurious gray tile in the midst of a color landscape.
				if ((dataType == DataType.UnsignedInteger) && (numBands < 4)) {
					dataType = DataType.UnsignedByte;
				} else if ((dataType == DataType.UnsignedByte) && (numBands > 1)) {
					dataType = DataType.UnsignedInteger;
				}
				Image image = ImageUtil.convertToArdor3DImage(bImage, false);
				QuadTreeTile tile = new QuadTreeTile(image, id, dataType);
				return (tile);
			}
		} catch (Exception e) {
			System.out.println("Unable to read tile " + id + ", see log.");
			e.printStackTrace();
		}
		return (null);
	}

	protected synchronized void fillDepthTree(DepthTree dTree, String path, String layerName) {
		String id = path + "/";
		if (tileExists(layerName, id + "1")) {
			// if one child exists they should all exist
			dTree.child = new DepthTree[] { new DepthTree(), new DepthTree(), new DepthTree(), new DepthTree() };
			for (int i = 0; i < dTree.child.length; ++i) {
				dTree.child[i].id = id + (i + 1);
				fillDepthTree(dTree.child[i], id + (i + 1), layerName);
			}
		}
	}

	public synchronized String getMaxLevel(String id) {
		String[] token = id.split("/");
		DepthTree dTree = depthTree;
		String newId = "";
		for (int i = 0; i < token.length; ++i) {
			if (!token[i].isEmpty()) {
				if (dTree.child == null) {
					return (newId);
				}
				int index = Integer.valueOf(token[i]) - 1;
				dTree = dTree.child[index];
				newId += "/" + token[i];
			}
		}
		return (newId);
	}

	/**
	 * Given an X,Y coordinate, find the highest level tile key that contains
	 * that coordinate.
	 * 
	 * @param x
	 *            , y the coordinate
	 * @param worldWidth
	 *            , worldHeight the physical dimensions of the source
	 * @return the key string
	 */
	@Override
	public synchronized String getKey(double x, double y, double worldWidth, double worldLength) {
		String key = "";
		return (getKey(depthTree, x, y, key, worldWidth / 2, worldLength / 2, -1));
	}

	/**
	 * Given an X,Y coordinate and level, find the key of the tile that contains
	 * that coordinate.
	 * 
	 * @param x
	 *            , y the coordinate
	 * @param worldWidth
	 *            , worldHeight the physical dimensions of the source
	 * @return the key string
	 */
	@Override
	public synchronized String getKey(double x, double y, double worldWidth, double worldLength, int lvl) {
		String key = "";
		return (getKey(depthTree, x, y, key, worldWidth / 2, worldLength / 2, lvl));
	}

	private String getKey(DepthTree depthTree, double x, double y, String key, double width, double length, int lvl) {
		if (depthTree.child == null) {
			if (lvl > 0)
				return(null);
			else
				return (key);
		}
		if (lvl == 0)
			return(key);	
		double w = width / 2;
		double l = length / 2;
		if (x < 0) {
			if (y >= 0) {
				depthTree = depthTree.child[0];
				return (getKey(depthTree, x + w, y - l, key + "/" + 1, w, l, lvl-1));
			} else {
				depthTree = depthTree.child[2];
				return (getKey(depthTree, x + w, y + l, key + "/" + 3, w, l, lvl-1));
			}
		} else {
			if (y >= 0) {
				depthTree = depthTree.child[1];
				return (getKey(depthTree, x - w, y - l, key + "/" + 2, w, l, lvl-1));
			} else {
				depthTree = depthTree.child[3];
				return (getKey(depthTree, x - w, y + l, key + "/" + 4, w, l, lvl-1));
			}
		}
	}

	protected final String layerPath(String layerName) {
		return (dirName + "/" + layerName);
	}

	protected DepthTree getDepthTree() {
		if (depthTree != null) {
			return (depthTree);
		}
//		String depthFileName = dirName + "/dert/depth.obj";
//		final File depthFile = new File(depthFileName);
//		if (depthFile.exists()) {
//			try {
//				ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(depthFile));
//				depthTree = (DepthTree) inStream.readObject();
//				inStream.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//				System.out.println("Error reading depth tree file.");
//				depthTree = null;
//			}
//		}
		final String depthFileName = dirName + "/dert/depthtree.txt";
		File depthFile = new File(depthFileName);
		if (depthFile.exists()) {
			try {
				depthTree = DepthTree.load(depthFileName);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error reading depth tree file.");
				depthTree = null;
			}
		}
		if (depthTree == null) {
			// assign after filling so tileExists method will work
			if (tileExists("elevation", "")) {
				Console.print("Filling depth tree. This may take a bit for large landscapes . . .");
				DepthTree dTree = new DepthTree();
				dTree.id = "";
				fillDepthTree(dTree, "", "elevation");
				depthTree = dTree;
				Console.println(" complete.");
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						Thread.yield();
						try {
							DepthTree.store(depthTree, depthFileName);
						} catch (Exception e) {
							e.printStackTrace();
							Console.println("Error writing depth tree file.");
							depthTree = null;
						}
					}
				});
				thread.start();
			}
		}
		return (depthTree);
	}

}
