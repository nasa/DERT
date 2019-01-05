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

package gov.nasa.arc.dert.landscape.layer;

import gov.nasa.arc.dert.landscape.io.QuadTreeTile;
import gov.nasa.arc.dert.landscape.io.TileSource;
import gov.nasa.arc.dert.landscape.io.QuadTreeTile.DataType;
import gov.nasa.arc.dert.landscape.quadtree.QuadKey;
import gov.nasa.arc.dert.landscape.quadtree.QuadTreeMesh;
import gov.nasa.arc.dert.landscape.srs.ProjectionInfo;
import gov.nasa.arc.dert.render.SharedTexture2D;
import gov.nasa.arc.dert.util.ImageUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.Console;

import java.io.IOException;
import java.util.Properties;

import com.ardor3d.image.Image;
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
	
	protected float pixelScale = 1;

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
		try {
			tileLength = StringUtil.getIntegerValue(properties, "TileLength", true, 0, true);
		}
		catch (Exception e) {
			tileLength = StringUtil.getIntegerValue(properties, "TileHeight", true, 0, true);
		}
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
		Console.println("\nProperties for " + layerName + ":");
		Console.println("Layer Type = " + layerType);
		Console.println("Number of Levels = " + numLevels);
		Console.println("Tile Width = " + tileWidth);
		Console.println("Tile Length = " + tileLength);
		Console.println("Minimum Value = " + minimumValue[0]);
		Console.println("Maximum Value = " + maximumValue[0]);
		Console.println("Edge Fill Z-Value = " + fillValue);
		Console.println(projInfo.toString());
		// if we have a millimeter scale terrain we need to scale it up
		// so we can calculate normals
		if ((projInfo.scale[0] < 0.0001) || (projInfo.scale[1] < 0.0001))
			pixelScale = 100;
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
	 * Get pixel width.
	 * @return
	 */
	public double getPixelWidth() {
		return(projInfo.scale[0]);
	}
	
	/**
	 * Get pixel length.
	 * @return
	 */
	public double getPixelLength() {
		return(projInfo.scale[1]);
	}
	
	/**
	 * Get the pixel scale.
	 */
	public float getPixelScale() {
		return(pixelScale);
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

	protected QuadTreeTile readTile(QuadKey key) {
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
	public QuadTreeTile getTile(QuadKey key) {
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

	protected Image getTextureImage(QuadKey key) {
		QuadTreeTile t = getTile(key);
		if (t == null) {
			return (null);
		}
		if (t.getImage() == null) {
			t.setImage(imageUtil.convertToArdor3DImage(t.raster, numBytes * 8, t.dataType, t.columns, t.rows));
		}
		return (t.getImage());
	}

	/**
	 * Given the tile key, get a tile as a texture for this layer
	 */
	@Override
	public Texture getTexture(QuadKey key, QuadTreeMesh mesh, Texture store) {
		Image image = getTextureImage(key);
		if (image == null) {
			return (null);
		}
//		if (image.getDataFormat() == ImageDataFormat.RGBA) {
//			ByteBuffer byteBuffer = image.getData(0);
//			int n = byteBuffer.limit();
//			for (int i = 3; i < n; i += 4) {
//				if (byteBuffer.get(i) == 0) {
//					byteBuffer.put(i, (byte) 255);
//				}
//			}
//		}
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
