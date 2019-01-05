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

package gov.nasa.arc.dert.render;

import gov.nasa.arc.dert.util.ImageUtil;

import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture.DepthTextureCompareFunc;
import com.ardor3d.image.Texture.DepthTextureCompareMode;
import com.ardor3d.image.Texture.DepthTextureMode;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix4;
import com.ardor3d.math.type.ReadOnlyMatrix4;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.ClipState;
import com.ardor3d.renderer.state.ColorMaskState;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.OffsetState;
import com.ardor3d.renderer.state.OffsetState.OffsetType;
import com.ardor3d.renderer.state.ShadingState;
import com.ardor3d.renderer.state.ShadingState.ShadingMode;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.TextureKey;

/**
 * Provides a class that handles a depth texture with a projection matrix.
 *
 */
public class ProjectedDepthTexture {

	// defaults for polygon offset
	public final static float DEFAULT_POLYGON_OFFSET_FACTOR = 3f, DEFAULT_POLYGON_OFFSET_UNITS = 4f;

	// bias matrix
	public static final ReadOnlyMatrix4 BIAS = new Matrix4(0.5, 0.0, 0.0, 0.0, 0.0, 0.5, 0.0, 0.0, 0.0, 0.0, 0.5, 0.0,
		0.5, 0.5, 0.5, 1.0);

	// target of projected texture
	protected Spatial target;

	// occluding object
	protected Spatial occluder;

	// texture renderer
	protected BasicTextureRenderer textureRenderer;

	// texture
	protected Texture2D texture;

	// The state applying the depth offset
	private OffsetState offsetState;

	// Light -> Camera transformation matrix
	protected Matrix4 projectionMatrix = new Matrix4();

	// depth compare function
	protected DepthTextureCompareFunc func;

	// the camera projection is parallel
	protected boolean isParallel;

	// the texture should have a white border
	protected boolean borderWhite;

	/**
	 * Constructor
	 * 
	 * @param occluder
	 * @param target
	 * @param func
	 * @param isParallel
	 * @param borderWhite
	 */
	public ProjectedDepthTexture(Spatial occluder, Spatial target, DepthTextureCompareFunc func, boolean isParallel,
		boolean borderWhite) {
		this.occluder = occluder;
		this.target = target;
		this.func = func;
		this.isParallel = isParallel;
		this.borderWhite = borderWhite;

		offsetState = new OffsetState();
		offsetState.setEnabled(true);
		offsetState.setTypeEnabled(OffsetType.Fill, true);
		offsetState.setFactor(DEFAULT_POLYGON_OFFSET_FACTOR);
		offsetState.setUnits(DEFAULT_POLYGON_OFFSET_UNITS);
	}

	public void doPrerender(Renderer renderer) {
		init(renderer);
		update(renderer);
	}

	/**
	 * Get the polygon offset factor value
	 * 
	 * @return
	 */
	public float getPolygonOffsetFactor() {
		return (offsetState.getFactor());
	}

	/**
	 * Get the polygon offset units value
	 * 
	 * @return
	 */
	public float getPolygonOffsetUnits() {
		return (offsetState.getUnits());
	}

	/**
	 * Set the polygon offset factor value
	 * 
	 * @param factor
	 */
	public void setPolygonOffsetFactor(float factor) {
		offsetState.setFactor(factor);
	}

	/**
	 * Set the polygon offset units value
	 * 
	 * @param units
	 */
	public void setPolygonOffsetUnits(float units) {
		offsetState.setUnits(units);
	}

	/**
	 * Initialize this ProjectedDepthTexture with the renderer
	 * 
	 * @param r
	 */
	public void init(final Renderer r) {
		// already initialized ?
		if (textureRenderer != null) {
			return;
		}

		textureRenderer = ImageUtil.createTextureRenderer(0, 0, r, true);
		if (isParallel) {
			textureRenderer.getCamera().setProjectionMode(Camera.ProjectionMode.Parallel);
		}

		// Enforce performance enhancing states on the renderer.
		// No textures or colors are required since we're only
		// interested in recording depth.
		// Also only need front faces when rendering the shadow maps

		// turn off clipping
		ClipState noClip = new ClipState();
		noClip.setEnabled(false);
		textureRenderer.enforceState(noClip);

		// turn off texturing
		TextureState noTexture = new TextureState();
		noTexture.setEnabled(false);
		textureRenderer.enforceState(noTexture);

		// turn off colors
		ColorMaskState colorDisabled = new ColorMaskState();
		colorDisabled.setAll(false);
		textureRenderer.enforceState(colorDisabled);

		// cull back faces
		CullState cullFace = new CullState();
		cullFace.setEnabled(true);
		cullFace.setCullFace(CullState.Face.Back);
		textureRenderer.enforceState(cullFace);

		// turn off lights
		LightState noLights = new LightState();
		noLights.setEnabled(false);
		textureRenderer.enforceState(noLights);

		// use flat shading
		ShadingState flat = new ShadingState();
		flat.setShadingMode(ShadingMode.Flat);
		textureRenderer.enforceState(flat);

		// disable GLSLShaderObjectsState
		GLSLShaderObjectsState glsl = new GLSLShaderObjectsState();
		glsl.setEnabled(false);
		textureRenderer.enforceState(glsl);

		// enforce the shadow offset parameters
		textureRenderer.enforceState(offsetState);

		if (texture == null) {
			createTexture();
		}
		textureRenderer.setupTexture(texture);
	}

	private void createTexture() {

		texture = new SharedTexture2D();
		texture.setWrap(Texture.WrapMode.BorderClamp);
		texture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
		texture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
		texture.setHasBorder(true);
		if (borderWhite) {
			texture.setBorderColor(ColorRGBA.WHITE);
		} else {
			texture.setBorderColor(ColorRGBA.BLACK_NO_ALPHA);
		}

		texture.setEnvironmentalMapMode(Texture.EnvironmentalMapMode.EyeLinear);
		texture.setTextureStoreFormat(TextureStoreFormat.Depth32);
		texture.setDepthCompareMode(DepthTextureCompareMode.RtoTexture);
		texture.setDepthCompareFunc(func);
		texture.setDepthMode(DepthTextureMode.Intensity);
		texture.setTextureKey(TextureKey.getRTTKey(Texture.MinificationFilter.BilinearNoMipMaps));
	}

	protected void updateProjection() {
		// nothing here
	}

	// Render the texture
	public void update(final Renderer r) {

		updateProjection();

		// Render only vertices, nothing else
		setRenderVertexOnly(true);

		// render
		textureRenderer.render(occluder, texture, Renderer.BUFFER_COLOR_AND_DEPTH);

		// restore states
		setRenderVertexOnly(false);

		// set the texture coordinate matrix
		texture.setTextureMatrix(projectionMatrix);
	}

	// Render the texture
	public void clear(final Renderer r) {

		updateProjection();

		// Render only vertices, nothing else
		setRenderVertexOnly(true);

		// render
		textureRenderer.clear(texture, Renderer.BUFFER_COLOR_AND_DEPTH);

		// restore states
		setRenderVertexOnly(false);

		// set the texture coordinate matrix
		texture.setTextureMatrix(projectionMatrix);
	}

	private static void setRenderVertexOnly(boolean val) {
		Mesh.RENDER_VERTEX_ONLY = val;
	}

	/**
	 * Copy the texture to another
	 * 
	 * @param tex
	 */
	public void copyToTexture(Texture tex) {
		textureRenderer.copyToTexture(tex, 0, 0, textureRenderer.getWidth(), textureRenderer.getHeight(), 0, 0);
	}

	/**
	 * Clean up.
	 * 
	 */
	public void dispose() {
		if (textureRenderer != null) {
			try {
				textureRenderer.cleanup();
				textureRenderer = null;
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	/**
	 * Get the texture
	 * 
	 * @return
	 */
	public Texture getTexture() {
		if (texture == null) {
			createTexture();
		}
		return (texture);
	}

}
