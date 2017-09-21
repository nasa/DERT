package gov.nasa.arc.dert.landscape;

import gov.nasa.arc.dert.landscape.LayerInfo.LayerType;

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
	public abstract QuadTreeTile getTile(String id);

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
	public abstract Texture getTexture(String key, Texture store);

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
		case range:
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
		case range:
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
