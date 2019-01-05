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
import gov.nasa.arc.dert.landscape.layer.LayerInfo.LayerType;
import gov.nasa.arc.dert.landscape.quadtree.QuadKey;
import gov.nasa.arc.dert.landscape.quadtree.QuadTreeMesh;

import java.util.Properties;

import com.ardor3d.image.Texture;
import com.ardor3d.renderer.Renderer;

/**
 * Abstract base class for landscape layers.
 *
 */
public abstract class Layer {

	// Flag to signify use of maximum level of layer
	public static int MAX_LEVEL = -1;

	// number of levels from the layer's properties file
	// NOTE: this value is for the original pyramid and doesn't include added
	// subpyramids
	// NOTE: subclasses must set this field
	protected int numLevels;

	// the number of bytes of memory used for a tile for this layer
	// NOTE: subclasses must set this field
	protected int bytesPerTile;

	// number of tiles
	// NOTE: subclasses must set this field
	protected int numTiles;

	// the layer name
	protected String layerName;

	// the type of layer
	protected LayerType layerType;

	// how much this layer contributes to the landscape color
	protected double blendFactor;

	// Information about this layer
	protected LayerInfo layerInfo;

	/**
	 * Constructor
	 * 
	 * @param layerInfo
	 */
	public Layer(LayerInfo layerInfo) {
		this.layerInfo = layerInfo;
		layerName = layerInfo.name;
		layerType = layerInfo.type;
		blendFactor = layerInfo.opacity;
	}

	/**
	 * Dispose of this layers resources
	 */
	public void dispose() {
		if (layerInfo.colorMap != null)
			layerInfo.colorMapName = layerInfo.colorMap.getName();
	}

	/**
	 * Given its id, get a tile.
	 * 
	 * @param id
	 * @return
	 */
	public abstract QuadTreeTile getTile(QuadKey qKey);

	/**
	 * Get the properties for this layer
	 * 
	 * @return
	 */
	public abstract Properties getProperties();

	/**
	 * Get a texture for this layer.
	 * 
	 * @param key
	 * @param store
	 * @return
	 */
	public abstract Texture getTexture(QuadKey key, QuadTreeMesh mesh, Texture store);

	/**
	 * Get number of tiles
	 */
	public int getNumberOfTiles() {
		return (numTiles);
	}

	/**
	 * Get the amount of memory required for a tile.
	 * 
	 * @return
	 */
	public int getBytesPerTile() {
		return (bytesPerTile);
	}

	/**
	 * Get the blend factor for this layer
	 * 
	 * @return
	 */
	public double getBlendFactor() {
		return (blendFactor);
	}

	/**
	 * Set the blend factor for this layer
	 * 
	 * @return
	 */
	public void setBlendFactor(double factor) {
		blendFactor = factor;
	}

	/**
	 * Determine if this layer consists of image pixels.
	 * 
	 * @return
	 */
	public boolean isImage() {
		switch (layerType) {
		case none:
		case footprint:
		case viewshed:
		case derivative:
			return (false);
		case elevation:
		case field:
			return (false);
		case colorimage:
		case grayimage:
			return (true);
		}
		return (false);
	}

	/**
	 * Determine if this layer consists of image pixels.
	 * 
	 * @return
	 */
	public boolean hasColorMap() {
		switch (layerType) {
		case none:
		case footprint:
		case viewshed:
		case elevation:
		case colorimage:
		case grayimage:
			return (false);
		case derivative:
		case field:
			return (true);
		}
		return (false);
	}

	/**
	 * Get the type of layer
	 * 
	 * @return
	 */
	public LayerType getLayerType() {
		return (layerType);
	}

	/**
	 * Get the layer name
	 * 
	 * @return
	 */
	public String getLayerName() {
		return (layerName);
	}

	@Override
	public String toString() {
		return (layerName);
	}

	/**
	 * Get the number of levels for this layer
	 * 
	 * @return
	 */
	public int getNumberOfLevels() {
		return (numLevels);
	}

	/**
	 * Pre-render the layer.
	 * 
	 * @param renderer
	 */
	public void prerender(final Renderer renderer) {
		// nothing here
	}
}
