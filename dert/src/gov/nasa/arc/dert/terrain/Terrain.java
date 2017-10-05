package gov.nasa.arc.dert.terrain;

import gov.nasa.arc.dert.io.TileSource;
import gov.nasa.arc.dert.raster.ProjectionInfo;
import gov.nasa.arc.dert.render.LayerEffects;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.viewpoint.BasicCamera;

import java.awt.Color;

import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.MaterialFace;
import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.LightCombineMode;

/**
 * Provides a class for handling the Landscape.
 *
 */
public abstract class Terrain extends Node {

	public static int MAX_LEVELS = 50;

	// terrain tile source
	protected TileSource source;

	// base layer projection info
	protected ProjectionInfo projInfo;

	// the quad tree
	protected QuadTree quadTree;

	// the height map
	protected RasterLayer baseLayer;

	// list of image and other layers
	protected Layer[] layerList;

	// base layer tile dimensions
	protected int tileWidth, tileLength;

	// full terrain dimensions
	protected double terrainWidth, terrainLength;

	// pixel dimensions
	protected double pixelWidth, pixelLength;

	// factory to create quad trees (tiles)
	protected QuadTreeFactory factory;

	// the terrain vertical exaggeration
	protected double vertExaggeration = 1;

	// the maximum level of the height map
	protected int baseMapLevel;

	// a scene graph node to hold a translated quad tree so it can be scaled
	protected Node contents;

	// minimum Z value, maximum Z value
	protected double minZ, maxZ;

	// surface mesh color
	protected MaterialState materialState;
	protected Color surfaceColor;

	// texture state for field camera footprints and view sheds
	protected TextureState textureState;

	// object to manage layers
	protected LayerManager layerManager;

	// scale factor for millimeter scale terrains
	protected float pixelScale = 1;
	
	// bounds of the terrain
	protected double[] bounds;

	/**
	 * Constructor
	 * 
	 * @param source
	 *            source of tiles
	 * @param layerManager
	 *            manager of layers
	 * @param surfaceColor
	 *            surface color
	 */
	public Terrain(String label, TileSource source, LayerManager layerManager, Color surfaceColor) {
		super(label);
		this.source = source;
		this.layerManager = layerManager;
		this.surfaceColor = surfaceColor;
		layerList = layerManager.getLayers();
		baseLayer = layerManager.getBaseLayer();
		projInfo = baseLayer.getProjectionInfo();
		pixelWidth = projInfo.scale[0];
		pixelLength = projInfo.scale[1];
		// if we have a millimeter scale terrain we need to scale it up
		// so we can calculate normals
		if ((pixelWidth < 0.0001) || (pixelLength < 0.0001))
			pixelScale = 100;
		pixelWidth *= pixelScale;
		pixelLength *= pixelScale;
		minZ = baseLayer.getMinimumValue()[0];
		maxZ = baseLayer.getMaximumValue()[0];
		tileWidth = baseLayer.getTileWidth();
		tileLength = baseLayer.getTileLength();
		baseMapLevel = baseLayer.getNumberOfLevels() - 1;
		terrainWidth = baseLayer.getRasterWidth() * pixelWidth;
		terrainLength = baseLayer.getRasterLength() * pixelLength;
		bounds = new double[6];
		bounds[0] = -terrainWidth/2;
		bounds[3] = terrainWidth/2;
		bounds[1] = -terrainLength/2;
		bounds[4] = terrainLength/2;
		bounds[2] = minZ;
		bounds[5] = maxZ;
		textureState = new TextureState();
		textureState.setEnabled(true);
		setRenderState(textureState);
	}

	/**
	 * Get the factory for quad tree tiles
	 * 
	 * @return
	 */
	public QuadTreeFactory getFactory() {
		return (factory);
	}

	/**
	 * Get the landscape texture state
	 * 
	 * @return
	 */
	public TextureState getTextureState() {
		return (textureState);
	}

	/**
	 * Get the layer manager
	 * 
	 * @return
	 */
	public LayerManager getLayerManager() {
		return (layerManager);
	}

	/**
	 * Get the pixel width
	 * 
	 * @return
	 */
	public double getPixelWidth() {
		return (pixelWidth);
	}

	/**
	 * Get the pixel length
	 * 
	 * @return
	 */
	public double getPixelLength() {
		return (pixelLength);
	}

	/**
	 * User changed the landscape layers. Reinitialize the landscape contents.
	 */
	public void resetLayers() {
		// free up existing tiles and run the Java garbage collector
		detachChild(contents);
		contents = null;
		quadTree = null;
		factory.dispose();
		factory = null;
		System.gc();
		// get the new layer configuration
		if (!layerManager.initialize(source)) {
			return;
		}
		layerList = layerManager.getLayers();
		initialize();
	}

	/**
	 * Initialize the landscape. Create the factory, quad tree, and layers.
	 */
	public void initialize() {
		factory = new QuadTreeFactory(getName(), source, baseLayer, layerList, pixelScale);
		factory.setSurfaceColor(surfaceColor);
//		factory.enableLayers(layerManager.layersEnabled);

		// create the top level quad tree tile
		quadTree = factory.getQuadTree("", null, new Vector3(0, 0, 0), terrainWidth / tileWidth,
				terrainLength / tileLength, 0, -1, false);
		if (quadTree == null)
			throw new IllegalStateException("Root quadTree for "+getName()+" is empty or invalid.");
		quadTree.inUse = true;
		quadTree.updateWorldBound(true);

		// material state for surface color and shading
		materialState = new MaterialState();
		materialState.setColorMaterial(MaterialState.ColorMaterial.AmbientAndDiffuse);
		materialState.setEnabled(true);
		materialState.setColorMaterialFace(MaterialFace.Front);
		quadTree.setRenderState(materialState);
		if (layerManager.shadingFromSurface) {
			quadTree.getSceneHints().setLightCombineMode(LightCombineMode.Inherit);
		} else {
			quadTree.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		}

		// layer effects shader
		LayerEffects layerEffects = layerManager.getLayerEffects();
		quadTree.setRenderState(layerEffects);

		// make sure changes are realized
		quadTree.markDirty(DirtyType.RenderState);

		// translate to minimum elevation for terrain exaggeration
		contents = new Node("_landscape_contents");
		contents.setTranslation(0, 0, -minZ * pixelScale);
		contents.attachChild(quadTree);

		// attach the contents of this landscape
		attachChild(contents);
		updateGeometricState(0, true);
	}

	/**
	 * Remove layers for a field camera.
	 * 
	 * @param fieldCamera
	 */
	public void removeFieldCamera(FieldCamera fieldCamera) {
		if (layerManager.removeFieldCamera(fieldCamera)) {
			LayerEffects layerEffects = layerManager.getLayerEffects();
			layerEffects.setEnabled(true);
			quadTree.setRenderState(layerEffects);
			quadTree.markDirty(DirtyType.RenderState);
		}
	}

	/**
	 * Dispose of landscape resources.
	 */
	public void dispose() {
		quadTree = null;
		factory.dispose();
		for (int i = 0; i < layerList.length; ++i) {
			if (layerList[i] != null) {
				layerList[i].dispose();
			}
		}
	}

	/**
	 * Set the surface color.
	 * 
	 * @param surfaceColor
	 */
	public void setSurfaceColor(Color surfaceColor) {
		this.surfaceColor = surfaceColor;
		factory.setSurfaceColor(surfaceColor);
	}

	/**
	 * Get the surface color.
	 * 
	 * @return
	 */
	public Color getSurfaceColor() {
		return (surfaceColor);
	}

	/**
	 * Get the tile data source.
	 * 
	 * @return
	 */
	public TileSource getSource() {
		return (source);
	}

	/**
	 * Change the vertical exaggeration of the Z coordinate by scaling.
	 * 
	 * @param val
	 */
	public void setVerticalExaggeration(double val) {
		setScale(1, 1, val);
		vertExaggeration = val;
	}

	/**
	 * Get the vertical exaggeration scale factor.
	 * 
	 * @return
	 */
	public double getVerticalExaggeration() {
		return (vertExaggeration);
	}

	/**
	 * Get the width of the original raster used for the base layer.
	 * 
	 * @return
	 */
	public int getRasterWidth() {
		return (baseLayer.getRasterWidth());
	}

	/**
	 * Get the length of the original raster used for the base layer.
	 * 
	 * @return
	 */
	public int getRasterLength() {
		return (baseLayer.getRasterLength());
	}

	/**
	 * Get the level that is at the original raster resolution. This should be
	 * the highest level unless a subpyramid has been added.
	 * 
	 * @return
	 */
	public int getBaseMapLevel() {
		return (baseMapLevel);
	}

	/**
	 * Get the pixel scale factor
	 * 
	 * @return
	 */
	public float getPixelScale() {
		return (pixelScale);
	}

	/**
	 * Update the resolution of the tiles in the landscape.
	 */
	public boolean update(BasicCamera camera) {
		boolean qtChanged = false;
		if (quadTree != null) {
			qtChanged = quadTree.update(camera);
			if (qtChanged)
				for (int i = 0; i <= baseMapLevel; ++i) {
					quadTree.stitch(i);
				}
		}
		return(qtChanged);
	}

	/**
	 * Show the image and derivative layers
	 * 
	 * @param enable
	 */
	public void enableLayers(boolean enable) {
		layerManager.enableLayers(enable);
		quadTree.markDirty(DirtyType.RenderState);
	}

	/**
	 * Are the layers showing.
	 * 
	 * @return
	 */
	public boolean isLayersEnabled() {
		return (layerManager.layersEnabled);
	}

	/**
	 * Is the blend factor auto-adjusting
	 * 
	 * @return
	 */
	public boolean isAutoAdjustBlendFactor() {
		return (layerManager.autoAdjustOpacity);
	}

	/**
	 * Is shading provided by surface normals.
	 * 
	 * @return
	 */
	public boolean isShadingFromSurface() {
		return (layerManager.shadingFromSurface);
	}
	
	public boolean isWireFrame() {
		WireframeState wfs = (WireframeState) getLocalRenderState(RenderState.StateType.Wireframe);
		if (wfs == null) {
			return (false);
		}
		if (!wfs.isEnabled()) {
			return (false);
		}
		return (true);
	}


	/**
	 * Set the shading.
	 * 
	 * @param shading
	 */
	public void setShadingFromSurface(boolean shading) {
		layerManager.shadingFromSurface = shading;
		if (shading) {
			quadTree.getSceneHints().setLightCombineMode(LightCombineMode.Inherit);
			quadTree.markDirty(DirtyType.RenderState);
		} else {
			quadTree.getSceneHints().setLightCombineMode(LightCombineMode.Off);
			quadTree.markDirty(DirtyType.RenderState);
		}
	}
	
	public double[] getBounds() {
		return(bounds);
	}
}
