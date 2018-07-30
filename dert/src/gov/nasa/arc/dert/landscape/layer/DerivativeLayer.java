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
 
Tile Rendering Library - Brain Paul 
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
import gov.nasa.arc.dert.landscape.quadtree.QuadKey;
import gov.nasa.arc.dert.landscape.quadtree.QuadTreeMesh;
import gov.nasa.arc.dert.util.ColorMap;
import gov.nasa.arc.dert.util.ColorMapListener;
import gov.nasa.arc.dert.util.MathUtil;

import java.nio.FloatBuffer;
import java.util.Properties;

import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
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
public class DerivativeLayer extends Layer implements ColorMapListener {

	public static String defaultColorMapName;

	public static enum DerivativeType {
		Elevation, Slope, Aspect, Distance
	}

	// Type of derivative
	private DerivativeType type;

	// Source of height map data
	private RasterLayer dataSource;

	// The texture holding the color map
	private Texture2D colorMapTexture;

	// The color map
	private ColorMap colorMap;
	
	// The location of the green marble (for distance map)
	private ReadOnlyVector3 origin;
	
	// Maximum distance from origin;
	private double maxDist;
	private double terrainWidth, terrainLength;

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param layerInfo
	 * @param source
	 */
	public DerivativeLayer(DerivativeType type, LayerInfo layerInfo, RasterLayer source) {
		super(layerInfo);
		this.type = type;
		colorMap = layerInfo.colorMap;
		dataSource = source;
		numLevels = dataSource.getNumberOfLevels();
		numTiles = dataSource.getNumberOfTiles();
		bytesPerTile = (dataSource.getTileWidth() + 1) * (dataSource.getTileLength() + 1) * 8;
		terrainWidth = dataSource.getRasterWidth()*dataSource.getPixelWidth()*dataSource.getPixelScale();
		terrainLength = dataSource.getRasterLength()*dataSource.getPixelLength()*dataSource.getPixelScale();
		if (type == DerivativeType.Distance) {
			origin = layerInfo.gmLoc;
			findMaximumDistance();
			// set the green marble location
			if (Double.isNaN(layerInfo.minimum))
				layerInfo.minimum = 0;
			if (Double.isNaN(layerInfo.maximum))
				layerInfo.maximum = maxDist;
		}
	}
	
	private double findMaximumDistance() {
		double x = terrainWidth/2;
		double y = terrainLength/2;
		double z = origin.getZ();
		maxDist = origin.distance(new Vector3(-x, -y, z));
		maxDist = Math.max(maxDist, origin.distance(new Vector3(x, -y, z)));
		maxDist = Math.max(maxDist, origin.distance(new Vector3(x, y, z)));
		maxDist = Math.max(maxDist, origin.distance(new Vector3(-x, y, z)));
		return(maxDist);
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
		
		Vector3 center = key.getTileCenter(terrainWidth, terrainLength);
		createColorMapTextureCoords(mesh, layerInfo.layerNumber, center);
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

	/**
	 * Get the type of this derivative layer
	 * 
	 * @return
	 */
	public DerivativeType getDerivativeType() {
		return (type);
	}

	private void initColormap() {
		if (colorMap == null) {
			switch (type) {
			case Elevation:
				colorMap = new ColorMap(layerInfo.colorMapName, layerName, dataSource.getMinimumValue()[0],
					dataSource.getMaximumValue()[0], layerInfo.minimum, layerInfo.maximum, layerInfo.gradient);
				colorMap.addListener(this);
				break;
			case Slope:
				colorMap = new ColorMap(layerInfo.colorMapName, layerName, 0, 90, layerInfo.minimum, layerInfo.maximum,
					layerInfo.gradient);
				colorMap.addListener(this);
				break;
			case Aspect:
				colorMap = new ColorMap(layerInfo.colorMapName, layerName, 0, 360, layerInfo.minimum, layerInfo.maximum,
					layerInfo.gradient);
				colorMap.addListener(this);
				break;
			case Distance:
				colorMap = new ColorMap(layerInfo.colorMapName, layerName, 0, maxDist, layerInfo.minimum, layerInfo.maximum,
						layerInfo.gradient);
				colorMap.addListener(this);
				break;
			}
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

	/**
	 * Create the color map texture coordinates for a tile mesh.
	 * 
	 * @param mesh
	 * @param textureUnit
	 */
	private void createColorMapTextureCoords(QuadTreeMesh mesh, int textureUnit, ReadOnlyVector3 center) {
		FloatBuffer vertex = mesh.getMeshData().getVertexBuffer();
		FloatBuffer normals = mesh.getMeshData().getNormalBuffer();
		FloatBuffer colors = mesh.getMeshData().getColorBuffer();
		FloatBuffer texCoords = mesh.getMeshData().getTextureBuffer(textureUnit);
		if (texCoords == null) {
			texCoords = BufferUtils.createFloatBuffer(colors.limit() / 2);
		}
		int dataSize = colors.limit() / 4;
		Vector2 coord = new Vector2();
		Vector3 vec = new Vector3();
		int k = 0;
		float pixelScale = Landscape.getInstance().getPixelScale();
		for (int i = 0; i < dataSize; ++i) {
			k = i * 2;
			float alpha = colors.get(i * 4 + 3);
			float z = 0;
			if (alpha != 0) {
				switch (type) {
				case Elevation:
					z = vertex.get(i * 3 + 2);
					colorMap.getTextureCoordinate(z/pixelScale, coord);
					texCoords.put(k, coord.getXf()).put(k + 1, coord.getYf());
					break;
				case Slope:
					vec.set(normals.get(i * 3), normals.get(i * 3 + 1), normals.get(i * 3 + 2));
					z = (float) MathUtil.getSlopeFromNormal(vec);
					colorMap.getTextureCoordinate(z/pixelScale, coord);
					texCoords.put(k, coord.getXf()).put(k + 1, coord.getYf());
					break;
				case Aspect:
					vec.set(normals.get(i * 3), normals.get(i * 3 + 1), normals.get(i * 3 + 2));
					z = (float) MathUtil.getAspectFromNormal(vec);
					colorMap.getTextureCoordinate(z/pixelScale, coord);
					texCoords.put(k, coord.getXf()).put(k + 1, coord.getYf());
					break;
				case Distance:
					vec.set(vertex.get(i * 3), vertex.get(i * 3 + 1), vertex.get(i * 3 + 2));
					vec.addLocal(center);
					z = (float) origin.distance(vec);
					colorMap.getTextureCoordinate(z/pixelScale, coord);
					texCoords.put(k, coord.getXf()).put(k + 1, coord.getYf());
					break;
				}
			} else {
				texCoords.put(k, 0).put(k + 1, -1);
			}
		}
		k += 2;
		texCoords.limit(k);
		texCoords.rewind();
		mesh.getMeshData().setTextureBuffer(texCoords, textureUnit);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (colorMap != null)
			colorMap.removeListener(this);
	}

}
