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

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.io.QuadTreeTile;
import gov.nasa.arc.dert.landscape.io.TileSource;
import gov.nasa.arc.dert.landscape.quadtree.QuadKey;
import gov.nasa.arc.dert.landscape.quadtree.QuadTreeMesh;
import gov.nasa.arc.dert.util.ColorMap;
import gov.nasa.arc.dert.util.ColorMapListener;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Properties;

import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.math.Vector2;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.util.geom.BufferUtils;

/**
 * A layer that provides derivatives of the height map as color maps.
 * Derivatives include elevation contour map, slope map, and aspect map. Colors
 * are applied via a texture map color map. Texture coordinates determine which
 * part of the color map texture is applied at a given vertex in the landscape
 * mesh.
 *
 */
public class FieldLayer extends Layer implements ColorMapListener {

	public static String defaultColorMapName;

	// Source of height map data
	private RasterLayer dataSource;

	// The texture holding the color map
	private Texture2D colorMapTexture;

	// The color map
	private ColorMap colorMap;
	
	// Tile dimensions
	private int tileWidth, tileLength;

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param layerInfo
	 * @param source
	 */
	public FieldLayer(LayerInfo layerInfo, TileSource source) throws IOException {
		super(layerInfo);
		dataSource = new RasterLayer(layerInfo, source);
		tileWidth = dataSource.getTileWidth();
		tileLength = dataSource.getTileLength();
		this.layerInfo = layerInfo;
		numLevels = dataSource.getNumberOfLevels();
		numTiles = dataSource.getNumberOfTiles();
		bytesPerTile = (dataSource.getTileWidth() + 1) * (dataSource.getTileLength() + 1) * 8;
		colorMap = layerInfo.colorMap;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (colorMap != null)
			colorMap.removeListener(this);
		dataSource.dispose();
	}

	@Override
	public QuadTreeTile getTile(QuadKey key) {
		return (dataSource.getTile(key));
	}

	@Override
	public Properties getProperties() {
		return (dataSource.getProperties());
	}

	@Override
	public Texture getTexture(QuadKey key, QuadTreeMesh mesh, Texture store) {
		if (colorMapTexture == null) {
			initColormap();
		}
		createColorMapTextureCoords(key, mesh, layerInfo.layerNumber);
		return (colorMapTexture);
	}

	/**
	 * Get the color map.
	 * 
	 * @return
	 */
	public ColorMap getColorMap() {
		if (colorMapTexture == null) {
			initColormap();
		}
		return (colorMap);
	}

	private void initColormap() {
		if (colorMap == null) {
			colorMap = new ColorMap(layerInfo.colorMapName, layerName, dataSource.getMinimumValue()[0],
				dataSource.getMaximumValue()[0], layerInfo.minimum, layerInfo.maximum, layerInfo.gradient);
			colorMap.addListener(this);
			if (colorMap == null) {
				throw new IllegalStateException("Error loading color map " + layerInfo.colorMapName + ".");
			}
			layerInfo.colorMap = colorMap;
		}
		colorMapTexture = colorMap.getTexture();
	}

	/**
	 * The color map changed.
	 */
	@Override
	public void mapChanged(ColorMap cMap) {
		Landscape.getInstance().markDirty(DirtyType.RenderState);
	}

	/**
	 * The range of the color map changed.
	 */
	@Override
	public void rangeChanged(ColorMap cMap) {
		Landscape.getInstance().markDirty(DirtyType.RenderState);
	}
	
	private void createColorMapTextureCoords(QuadKey key, Mesh mesh, int tUnit) {
		FloatBuffer texCoords = getFloatTexCoords(key);
		if (texCoords != null)
			mesh.getMeshData().setTextureBuffer(texCoords, tUnit);
	}
	
	private FloatBuffer getFloatTexCoords(QuadKey key) {
		Vector2 coord = new Vector2();
		QuadTreeTile tile = dataSource.getTile(key);
		FloatBuffer data = tile.raster.asFloatBuffer();
		int tWidth = tileWidth + 1;
		int tLength = tileLength + 1;
		int size = tWidth * tLength * 2;
		FloatBuffer texCoords = BufferUtils.createFloatBuffer(size);
		for (int r = 0; r < tLength; ++r) {
			for (int c = 0; c < tWidth; ++c) {
				double val = data.get();
				colorMap.getTextureCoordinate(val, coord);
				texCoords.put(coord.getXf()).put(coord.getYf());
			}
		}
		texCoords.limit(size);
		texCoords.rewind();
		return (texCoords);
	}


}
