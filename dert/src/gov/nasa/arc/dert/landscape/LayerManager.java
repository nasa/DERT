package gov.nasa.arc.dert.landscape;

import gov.nasa.arc.dert.io.TileSource;
import gov.nasa.arc.dert.landscape.DerivativeLayer.DerivativeType;
import gov.nasa.arc.dert.landscape.LayerInfo.LayerType;
import gov.nasa.arc.dert.render.LayerEffects;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.util.ColorMap;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.ColorBarPanel;
import gov.nasa.arc.dert.view.Console;
import gov.nasa.arc.dert.view.surfaceandlayers.SurfaceAndLayersView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.Renderer;

/**
 * Helper class to aid Landscape with layers.
 *
 */
public class LayerManager implements Serializable {

	// number of texture units available for image layers
	public static int NUM_LAYERS = 7;
	public static final ReadOnlyColorRGBA DEFAULT_GRID_COLOR = ColorRGBA.WHITE;

	// Flags accessed by Landscape
	protected boolean layersEnabled;
	protected boolean shadingFromSurface;
	protected boolean autoAdjustBlendFactor;

	// Surface grid layer fields
	private ReadOnlyColorRGBA gridColor;
	private double gridCellSize;
	private boolean gridEnabled;

	// Layer information
	private ArrayList<LayerInfo> layerInfoList;

	// Array of image layers currently displayed (element set to null if not
	// used) corresponding to texture unit.
	protected transient LayerInfo[] selectedLayerInfo;

	// Object to do special layer effects in shader
	protected transient LayerEffects layerEffects;

	// Source of data for layers
	protected transient TileSource source;

	// Base layer
	protected transient RasterLayer baseLayer;

	// Array of layers by texture unit (element set to null if not used).
	protected transient Layer[] layers;

	/**
	 * Constructor
	 */
	public LayerManager() {
		shadingFromSurface = false;
		autoAdjustBlendFactor = true;
		layersEnabled = true;
		gridColor = DEFAULT_GRID_COLOR;
		gridCellSize = 0;
	}

	/**
	 * Initialize this LayerManager with a layer source.
	 * 
	 * @param source
	 * @return
	 */
	public boolean initialize(TileSource source) {
		this.source = source;
		LayerInfo baseLayerInfo = null;

		boolean firstTime = false;
		if (layerInfoList == null) {
			layerInfoList = new ArrayList<LayerInfo>();
			firstTime = true;
		}

		// create a list of available layers
		String[][] sourceLayerInfo = source.getLayerInfo();
		ArrayList<LayerInfo> newInfoList = new ArrayList<LayerInfo>();
		for (int i = 0; i < sourceLayerInfo.length; ++i) {
			LayerInfo li = new LayerInfo(sourceLayerInfo[i][0], sourceLayerInfo[i][1], 0, -1);
			// elevation is always the base layer
			if (li.type == LayerType.elevation) {
				baseLayerInfo = li;
				baseLayerInfo.layerNumber = 0;
			} else {
				LayerInfo lInfo = findLayerInfo(li.name, li.type);
				if (lInfo != null) {
					li = lInfo;
				}
				Properties properties = source.getProperties(li.name);
				li.isOverlay = StringUtil.getBooleanValue(properties, "Overlay", false, false);
				if (li.isOverlay) {
					li.blendFactor = 0.75;
					li.autoBlend = false;
				}
				newInfoList.add(li);
			}
		}

		// Elevation and derivatives
		if (baseLayerInfo == null) {
			Console.getInstance().println("Elevation layer not found.");
			return (false);
		}
		Properties properties = source.getProperties("elevation");
		if (properties == null) {
			Console.getInstance().println("Elevation layer properties not found.");
			return (false);
		}
		// elevation min and max are single values, not arrays
		baseLayerInfo.minimum = StringUtil.getDoubleValue(properties, "MinimumValue", false, 0, true);
		baseLayerInfo.maximum = StringUtil.getDoubleValue(properties, "MaximumValue", false, 0, true);
		LayerInfo lInfo = findLayerInfo("Slope Map", LayerType.floatfield);
		if (lInfo == null) {
			lInfo = new LayerInfo("Slope Map", "floatfield", DerivativeLayer.defaultColorMapName, 0, 90, false);
		}
		newInfoList.add(lInfo);
		lInfo = findLayerInfo("Aspect Map", LayerType.floatfield);
		if (lInfo == null) {
			lInfo = new LayerInfo("Aspect Map", "floatfield", DerivativeLayer.defaultColorMapName, 0, 90, false);
		}
		newInfoList.add(lInfo);
		lInfo = findLayerInfo("Elevation Map", LayerType.floatfield);
		if (lInfo == null) {
			lInfo = new LayerInfo("Elevation Map", "floatfield", DerivativeLayer.defaultColorMapName,
				baseLayerInfo.minimum, baseLayerInfo.maximum, false);
		} else {
			lInfo.minimum = baseLayerInfo.minimum;
			lInfo.maximum = baseLayerInfo.maximum;
		}
		newInfoList.add(lInfo);

		// FieldCamera footprints and viewsheds
		for (int i = 0; i < layerInfoList.size(); ++i) {
			lInfo = layerInfoList.get(i);
			if ((lInfo.type == LayerType.footprint) || (lInfo.type == LayerType.viewshed)) {
				newInfoList.add(lInfo);
			}
		}

		layerInfoList = newInfoList;
		Collections.sort(layerInfoList);

		// set up the displayed image layers
		if (selectedLayerInfo == null) {
			selectedLayerInfo = new LayerInfo[NUM_LAYERS];
			for (int i = 0; i < layerInfoList.size(); ++i) {
				LayerInfo li = layerInfoList.get(i);
				if (li.layerNumber > 0) {
					selectedLayerInfo[li.layerNumber - 1] = li;
				}
			}
			if (firstTime && (selectedLayerInfo[0] == null)) {
				// first time choose the first image layer found as the default
				// image layer
				for (int i = 0; i < layerInfoList.size(); ++i) {
					if ((layerInfoList.get(i).type == LayerType.colorimage)
						|| (layerInfoList.get(i).type == LayerType.grayimage)) {
						selectedLayerInfo[0] = layerInfoList.get(i);
						selectedLayerInfo[0].blendFactor = 1;
						selectedLayerInfo[0].layerNumber = 1;
						break;
					}
				}
				firstTime = false;
			}
			// no image layers available for the default, we are showing the
			// surface
			if (selectedLayerInfo[0] == null) {
				selectedLayerInfo[0] = new LayerInfo("None", "none", 0, 1);
			}
			// set all non-assigned layers to "none"
			for (int i = 1; i < selectedLayerInfo.length; ++i) {
				if (selectedLayerInfo[i] == null) {
					selectedLayerInfo[i] = new LayerInfo("None", "none", 0, i + 1);
				}
			}
		}

		// create the layers
		if (layers == null) {
			layers = new Layer[NUM_LAYERS];
		}
		if (!createBaseLayer(baseLayerInfo)) {
			return (false);
		}
		createLayers();

		return (true);
	}

	private LayerInfo findLayerInfo(String name, LayerType type) {
		for (int i = 0; i < layerInfoList.size(); ++i) {
			LayerInfo li = layerInfoList.get(i);
			if ((li.type == type) && li.name.equals(name)) {
				return (li);
			}
		}
		return (null);
	}

	private boolean createBaseLayer(LayerInfo baseLayerInfo) {
		// already been here
		if (baseLayer != null) {
			return (true);
		}

		// create the base layer
		baseLayer = (RasterLayer) createLayer(baseLayerInfo, source, -1);
		if (baseLayer == null) {
			Console.getInstance().println("Unable to create base layer");
			return (false);
		}
		return (true);
	}

	/**
	 * Get a copy of the layer information list
	 * 
	 * @return
	 */
	public LayerInfo[] getLayerInfoList() {
		LayerInfo[] layerInfo = new LayerInfo[layerInfoList.size()];
		layerInfoList.toArray(layerInfo);
		return (layerInfo);
	}

	/**
	 * Get the list of selected layers.
	 * 
	 * @return
	 */
	public LayerInfo[] getLayerSelection() {
		return (selectedLayerInfo);
	}

	/**
	 * Set the list of selected layers.
	 * 
	 * @param selectedLayerInfo
	 */
	public void setLayerSelection(LayerInfo[] selectedLayerInfo) {
		this.selectedLayerInfo = selectedLayerInfo;
	}

	/**
	 * Get the base (elevation) layer
	 * 
	 * @return
	 */
	public RasterLayer getBaseLayer() {
		return (baseLayer);
	}

	/**
	 * Get the non-base layers
	 * 
	 * @return
	 */
	public Layer[] getLayers() {
		return (layers);
	}

	/**
	 * Remove field camera layers.
	 * 
	 * @param fieldCamera
	 * @return
	 */
	public boolean removeFieldCamera(FieldCamera fieldCamera) {
		boolean found = false;
		String iName = fieldCamera.getName();
		for (int i = 0; i < layers.length; ++i) {
			if (layers[i] instanceof FieldCameraLayer) {
				FieldCameraLayer layer = (FieldCameraLayer) layers[i];
				if (iName.equals(layer.getLayerName())) {
					layer.dispose();
					layers[i] = null;
					selectedLayerInfo[i] = new LayerInfo("None", "none", 0, i + 1);
					layerEffects = new LayerEffects(layers, layerEffects);
					layerEffects.setEnabled(true);
					found = true;
				}
			}
		}
		Iterator<LayerInfo> iterator = layerInfoList.iterator();
		while (iterator.hasNext()) {
			LayerInfo li = iterator.next();
			if (li.name.equals(iName)) {
				iterator.remove();
			}
		}
		if (found) {
			SurfaceAndLayersView view = ConfigurationManager.getInstance().getCurrentConfiguration()
				.getSurfaceAndLayersView();
			if (view != null) {
				view.updateSelectedLayers();
			}
		}
		return (found);
	}

	/**
	 * Add field camera layers
	 * 
	 * @param fieldCamera
	 */
	public void addFieldCamera(FieldCamera fieldCamera) {
		layerInfoList.add(new LayerInfo(fieldCamera.getName(), "footprint", 0, -1));
		layerInfoList.add(new LayerInfo(fieldCamera.getName(), "viewshed", 0, -1));
	}

	private void createLayers() {
		Layer[] newList = new Layer[NUM_LAYERS];
		for (int i = 0; i < selectedLayerInfo.length; ++i) {
			if (selectedLayerInfo[i].type == LayerType.none) {
				newList[i] = null;
			} else {
				// see if we have this layer already and move it to the new slot
				boolean found = false;
				for (int j = 0; j < layers.length; ++j) {
					if (layers[j] == null) {
						continue;
					}
					if (selectedLayerInfo[i].name.equals(layers[j].toString()) && (selectedLayerInfo[i].type == layers[j].getLayerType())) {
						layers[j].blendFactor = selectedLayerInfo[i].blendFactor;
						newList[i] = layers[j];
						layers[j] = null;
						found = true;
						break;
					}
				}
				// if not create the layer
				if (!found) {
					newList[i] = createLayer(selectedLayerInfo[i], source, i);
				}
			}
		}
		// dispose of the layers we didn't reuse
		for (int i = 0; i < layers.length; ++i) {
			if (layers[i] != null) {
				layers[i].dispose();
				layers[i] = null;
			}
		}
		layers = newList;

		// create the layer effects shaders
		layerEffects = new LayerEffects(layers, layerEffects);
		enableGrid(gridEnabled);
		layerEffects.setEnabled(true);
		ColorBarPanel.resetColorBars();
	}

	private Layer createLayer(LayerInfo layerInfo, TileSource source, int textureUnit) {
		try {
			if (layerInfo.type == LayerType.none) {
				return (null);
			} else if (layerInfo.type == LayerType.floatfield) {
				if (layerInfo.name.contains("Elevation")) {
					return (new DerivativeLayer(DerivativeType.Elevation, layerInfo, baseLayer));
				} else if (layerInfo.name.contains("Slope")) {
					return (new DerivativeLayer(DerivativeType.Slope, layerInfo, baseLayer));
				} else if (layerInfo.name.contains("Aspect")) {
					return (new DerivativeLayer(DerivativeType.Aspect, layerInfo, baseLayer));
				} else {
					return (null);
				}
			} else if ((layerInfo.type == LayerType.footprint) || (layerInfo.type == LayerType.viewshed)) {
				return (new FieldCameraLayer(layerInfo, textureUnit));
			} else {
				return (new RasterLayer(layerInfo, source));
			}
		} catch (Exception e) {
			System.out.println("Unable to create layer " + layerInfo.name + ", see log.");
			e.printStackTrace();
			return (null);
		}
	}

	/**
	 * Get the LayerEffects object
	 * 
	 * @return
	 */
	public LayerEffects getLayerEffects() {
		return (layerEffects);
	}

	/**
	 * Show layers
	 * 
	 * @param enable
	 */
	public void enableLayers(boolean enable) {
		layerEffects.layersEnabled = enable;
		layersEnabled = enable;
	}

	/**
	 * Show shadows
	 * 
	 * @param enable
	 */
	public void enableShadow(boolean enable) {
		layerEffects.shadowEnabled = enable;
	}

	/**
	 * Set the blend factor for a layer indicated by the index
	 * 
	 * @param index
	 * @param value
	 */
	public void setLayerBlendFactor(int index, float value) {
		layerEffects.blendFactor[index] = value;
		selectedLayerInfo[index].blendFactor = value;
	}

	/**
	 * Get the blend factors for all layers
	 * 
	 * @return
	 */
	public float[] getLayerBlendFactors() {
		return (layerEffects.blendFactor);
	}

	/**
	 * Show the surface grid
	 * 
	 * @param enable
	 */
	public void enableGrid(boolean enable) {
		gridEnabled = enable;
		layerEffects.gridCell = gridCellSize;
		layerEffects.gridColor = gridColor.toArray(null);
		layerEffects.gridEnabled = enable;
	}

	/**
	 * Is the surface grid showing?
	 * 
	 * @return
	 */
	public boolean isGridEnabled() {
		return (gridEnabled);
	}

	/**
	 * Set the surface grid color
	 * 
	 * @param color
	 */
	public void setGridColor(ReadOnlyColorRGBA color) {
		gridColor = color;
		layerEffects.gridColor = color.toArray(null);
	}

	/**
	 * Get the surface grid color
	 * 
	 * @return
	 */
	public ReadOnlyColorRGBA getGridColor() {
		return (gridColor);
	}

	/**
	 * Set the surface grid cell size
	 * 
	 * @param size
	 */
	public void setGridCellSize(double size) {
		gridCellSize = size;
		layerEffects.gridCell = size;
	}

	/**
	 * Get the surface grid cell size
	 * 
	 * @return
	 */
	public double getGridCellSize() {
		return (gridCellSize);
	}

	/**
	 * Get a property from the base layer. If not available, try the other
	 * layers
	 * 
	 * @param key
	 * @return
	 */
	public Object getRasterLayerProperty(String key) {
		Object prop = baseLayer.getProperties().getProperty(key);
		if (prop != null) {
			return (prop);
		}
		for (int i = 0; i < layers.length; ++i) {
			if (layers[i] != null) {
				prop = layers[i].getProperties().getProperty(key);
				if (prop != null) {
					return (prop);
				}
			}
		}
		return (null);
	}

	/**
	 * Get the color maps used by the derivative layers
	 * 
	 * @return
	 */
	public ArrayList<ColorMap> getColorMaps() {
		ArrayList<ColorMap> list = new ArrayList<ColorMap>();
		for (int i = 0; i < layers.length; ++i) {
			if ((layers[i] != null) && (layers[i] instanceof DerivativeLayer)) {
				list.add(((DerivativeLayer) layers[i]).getColorMap());
			}
		}
		return (list);
	}

	/**
	 * Prerender layers
	 * 
	 * @param renderer
	 */
	public void renderLayers(Renderer renderer) {
		for (int i = 0; i < layers.length; ++i) {
			if (layers[i] != null) {
				layers[i].prerender(renderer);
			}
		}
	}

	/**
	 * Prepare layer info objects for persistence
	 */
	public void save() {
		for (int i = 0; i < layerInfoList.size(); ++i) {
			layerInfoList.get(i).save();
		}
	}
}
