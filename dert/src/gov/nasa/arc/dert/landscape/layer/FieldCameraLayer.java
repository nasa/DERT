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

import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.io.QuadTreeTile;
import gov.nasa.arc.dert.landscape.layer.LayerInfo.LayerType;
import gov.nasa.arc.dert.landscape.quadtree.QuadKey;
import gov.nasa.arc.dert.landscape.quadtree.QuadTreeMesh;
import gov.nasa.arc.dert.render.Viewshed;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.util.ImageUtil;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Properties;

import com.ardor3d.image.Image;
import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.image.PixelDataType;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix4;
import com.ardor3d.math.type.ReadOnlyMatrix4;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

/**
 * A layer that represents the footprint or viewshed of a FieldCamera. The
 * footprint is a projected texture. The viewshed is a projected depth texture
 * and is the opposite of a shadow map.
 *
 */
public class FieldCameraLayer extends Layer {

	// bias matrix for footprint
	private final static ReadOnlyMatrix4 BIAS = new Matrix4(0.5, 0.0, 0.0, 0.0, 0.0, 0.5, 0.0, 0.0, 0.0, 0.0, 0.5, 0.0,
		0.5, 0.5, 0.5, 1.0);

	// The field camera
	private FieldCamera fieldCamera;

	// the texture
	private Texture texture;

	// the texture unit for the layer
	private int textureUnit;

	// flag to indicate this is a viewshed
	private boolean viewshedEnabled;

	// color fields
	private float[] fColor;
	private Color color;

	// projection matrix
	private Matrix4 matrix;

	// Creates the projected texture for the viewshed
	private Viewshed viewshed;

	/**
	 * Constructor
	 * 
	 * @param layerInfo
	 * @param textureUnit
	 */
	public FieldCameraLayer(LayerInfo layerInfo, int textureUnit) {
		super(layerInfo);
		this.textureUnit = textureUnit;
		this.viewshedEnabled = layerInfo.type == LayerType.viewshed;
		color = new Color(0, 0, 0, 0);
		setColor(color);
	}

	/**
	 * Enable/disable the viewshed
	 * 
	 * @param viewshedEnabled
	 */
	public void setViewShed(boolean viewshedEnabled) {
		this.viewshedEnabled = viewshedEnabled;
		texture = null;
		if (!viewshedEnabled && (viewshed != null)) {
			viewshed.dispose();
			viewshed = null;
		}
	}

	/**
	 * Is the viewshed enabled
	 * 
	 * @return
	 */
	public boolean isViewshed() {
		return (viewshedEnabled);
	}

	@Override
	public void dispose() {
		super.dispose();
		Landscape.getInstance().getTextureState().setTexture(null, textureUnit);
		Landscape.getInstance().markDirty(DirtyType.RenderState);
	}

	@Override
	public QuadTreeTile getTile(QuadKey key) {
		return (null);
	}

	@Override
	public Properties getProperties() {
		return (null);
	}

	@Override
	public Texture getTexture(QuadKey key, QuadTreeMesh mesh, Texture store) {
		return (null);
	}

	private Texture createFootprintTexture() {
		// create the texture for the footprint
		int textureSize = ImageUtil.getMaxTextureRendererSize() / 4; // smaller
																		// increases
																		// performance
		int width = textureSize;
		int height = textureSize;
		int s = width * height;
		ByteBuffer buffer = BufferUtils.createByteBuffer(s * 4);
		for (int i = 0; i < s; ++i) {
			buffer.put((byte) color.getRed()).put((byte) color.getGreen()).put((byte) color.getBlue())
				.put((byte) color.getAlpha());
		}
		buffer.flip();
		buffer.rewind();
		ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>(1);
		list.add(buffer);
		Image image = new Image(ImageDataFormat.RGBA, PixelDataType.UnsignedByte, width, height, list, null);

		Texture texture = TextureManager.loadFromImage(image, Texture.MinificationFilter.BilinearNoMipMaps,
			TextureStoreFormat.RGBA8);
		matrix = new Matrix4();
		texture.setTextureMatrix(matrix);
		// texture.setHasBorder(true);
		texture.setWrap(Texture.WrapMode.BorderClamp);
		texture.setEnvironmentalMapMode(Texture2D.EnvironmentalMapMode.EyeLinear);
		texture.setApply(Texture2D.ApplyMode.Combine);
		texture.setBorderColor(ColorRGBA.BLACK_NO_ALPHA);
		return (texture);
	}

	/**
	 * Prerender this layer.
	 */
	@Override
	public void prerender(final Renderer renderer) {
		if (fieldCamera == null) {
			fieldCamera = (FieldCamera) World.getInstance().getTools().getChild(layerInfo.name);
		}
		Color instCol = fieldCamera.getColor();
		// color changed, recreate the texture
		if (!instCol.equals(color)) {
			setColor(instCol);
			if (!viewshedEnabled) {
				texture = null;
			}
		}
		// viewshed
		if (viewshedEnabled) {
			// render the viewshed
			if (texture == null) {
				viewshed = new Viewshed(fieldCamera.getSyntheticCameraNode().getCamera(), textureUnit);
				texture = viewshed.getTexture();
			}
			fieldCamera.prerender(renderer, viewshed);
		}
		// footprint
		else {
			if (texture == null) {
				texture = createFootprintTexture();
			}
			BasicCamera camera = fieldCamera.getSyntheticCameraNode().getCamera();
			matrix.set(camera.getModelViewProjectionMatrix());
			matrix.multiplyLocal(BIAS);
			texture.setTextureMatrix(matrix);
		}
		Landscape.getInstance().getTextureState().setTexture(texture, textureUnit);
		Landscape.getInstance().markDirty(DirtyType.RenderState);
	}

	/**
	 * Get the color for this layer
	 * 
	 * @return
	 */
	public float[] getColor() {
		return (fColor);
	}

	/**
	 * Set the color for this layer.
	 * 
	 * @param instColor
	 */
	public void setColor(Color instColor) {
		color = instColor;
		if (fColor == null) {
			fColor = new float[4];
		}
		color.getComponents(fColor);
	}

}
