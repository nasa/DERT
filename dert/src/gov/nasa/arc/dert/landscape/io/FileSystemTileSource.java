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

package gov.nasa.arc.dert.landscape.io;

import gov.nasa.arc.dert.landscape.io.QuadTreeTile.DataType;
import gov.nasa.arc.dert.landscape.quadtree.QuadKey;
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
public class FileSystemTileSource
	extends AbstractTileSource {

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
	public String getLandscapePath() {
		return (dirName);
	}

	private boolean tileExists(String layerName, String key) {
		if (depthTree != null) {
			return (tileExists(key, depthTree));
		}
		String fileName = layerPath(layerName) + key + "/0.png";
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
	public boolean tileExists(String key) {
		return (tileExists(key, depthTree));
	}

	private boolean tileExists(String key, DepthTree dTree) {
		if (key.equals(dTree.key)) {
			return (true);
		}
		if (!key.startsWith(dTree.key)) {
			return (false);
		}
		if (dTree.child == null) {
			return (false);
		}
		for (int i = 0; i < dTree.child.length; ++i) {
			if (tileExists(key, dTree.child[i])) {
				return (true);
			}
		}
		return (false);
	}

	@Override
	public QuadTreeTile getTile(String layerName, QuadKey qKey, DataType dataType) {
		if (tileExists(layerName, qKey.toString())) {
			return (getTilePng(layerName, qKey, dataType));
		}
		return (null);
	}

	/**
	 * Given a layer and an id, load the contents of the tile.
	 */
	public QuadTreeTile getTilePng(String layerName, QuadKey qKey, DataType dataType) {
		try {
			String fileName = layerPath(layerName) + qKey + "/0.png";
			File file = new File(fileName).getCanonicalFile();
			BufferedImage bImage = ImageIO.read(file);
			if (bImage != null) {
				// System.err.println("FileSystemTileSource.getTilePng "+layerName+" "+dataType+" "+bImage.getWidth()+" "+bImage.getHeight());
				int numBands = bImage.getData().getNumBands();
				if (dataType == DataType.Float) {
					DataBufferByte dBuf = (DataBufferByte) bImage.getData().getDataBuffer();
					byte[] bytes = dBuf.getData();
					ByteBuffer bBuf = ByteBuffer.wrap(bytes);
					QuadTreeTile tile = new QuadTreeTile(bBuf, qKey, bImage.getWidth(), bImage.getHeight(), dataType,
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
				QuadTreeTile tile = new QuadTreeTile(image, qKey, dataType);
				return (tile);
			}
		} catch (Exception e) {
			System.out.println("Unable to read tile " + qKey + " for layer "+layerName+", see log.");
			e.printStackTrace();
		}
		return (null);
	}

	protected synchronized void fillDepthTree(DepthTree dTree, String path, String layerName) {
		String key = path + "/";
		if (tileExists(layerName, key + "1")) {
			// if one child exists they should all exist
			dTree.child = new DepthTree[] { new DepthTree(), new DepthTree(), new DepthTree(), new DepthTree() };
			for (int i = 0; i < dTree.child.length; ++i) {
				dTree.child[i].key = key + (i + 1);
				fillDepthTree(dTree.child[i], key + (i + 1), layerName);
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
				dTree.key = "";
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
