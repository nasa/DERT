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
