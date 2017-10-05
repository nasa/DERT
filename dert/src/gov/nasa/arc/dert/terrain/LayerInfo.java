package gov.nasa.arc.dert.terrain;

import gov.nasa.arc.dert.util.ColorMap;
import gov.nasa.arc.dert.util.StateUtil;

import java.util.HashMap;

/**
 * Data structure for information about a landscape layer.
 *
 */
public class LayerInfo implements Comparable<LayerInfo> {

	public static enum LayerType {
		none, elevation, colorimage, grayimage, field, footprint, viewshed, derivative
	}

	// Name of the layer, presented in the UI
	public String name;

	// Layer type
	public LayerType type;

	// The percent that the layer contributes to the overall color of the
	// landscape
	public double opacity = 1;

	// The texture index for the layer
	public int layerNumber = -1;

	// The name of a color map used with this layer (for derivatives and fields only)
	public String colorMapName;

	// Flag to use a gradient with the color map
	public boolean gradient = false;

	// The minimum value of this layer
	public double minimum = Double.NaN;

	// The maximum value of this layer
	public double maximum = Double.NaN;
	
	// Auto blending enabled for this layer
	public boolean autoblend = false;
	
	// This layer, when added to the visible list, is showing.
	public int show = 1;

	// The color map object
	public transient ColorMap colorMap;

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param type
	 * @param colorMapName
	 * @param minimum
	 * @param maximum
	 * @param gradient
	 */
	public LayerInfo(String name, String type, String colorMapName, double minimum, double maximum, boolean gradient) {
		this(name, type, -1);
		this.colorMapName = colorMapName;
		this.minimum = minimum;
		this.maximum = maximum;
		this.gradient = gradient;
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param type
	 * @param blendFactor
	 * @param isOverlay
	 * @param layerNumber
	 */
	public LayerInfo(String name, String type, int layerNumber) {
		this.name = name;
		this.type = LayerType.valueOf(type);
		this.layerNumber = layerNumber;
		autoblend = !(this.type == LayerType.none);
	}
	
	/**
	 * Constructor from hash map.
	 */
	public LayerInfo(HashMap<String,Object> map) {
		name = StateUtil.getString(map, "Name", null);
		if (name == null)
			throw new NullPointerException("Name for LayerInfo is null.");
		String str = StateUtil.getString(map, "Type", "none");
		type = LayerType.valueOf(str);
		opacity = StateUtil.getDouble(map, "Opacity", opacity);
		layerNumber = StateUtil.getInteger(map, "LayerNumber", layerNumber);
		autoblend = StateUtil.getBoolean(map, "Autoblend", autoblend);
		show = StateUtil.getInteger(map, "Show", show);
		str = StateUtil.getString(map, "ColorMap.name", null);
		if (str != null) {
			colorMapName = str;
			gradient = StateUtil.getBoolean(map, "ColorMap.gradient", gradient);
			minimum = StateUtil.getDouble(map, "ColorMap.minimum", minimum);
			maximum = StateUtil.getDouble(map, "ColorMap.maximum", maximum);
		}
	}

	/**
	 * Constructor
	 * 
	 * @param that
	 */
	public LayerInfo(LayerInfo that) {
		this.name = that.name;
		this.type = that.type;
		this.opacity = that.opacity;
		this.layerNumber = that.layerNumber;
		this.gradient = that.gradient;
		this.colorMapName = that.colorMapName;
		this.minimum = that.minimum;
		this.maximum = that.maximum;
		this.colorMap = that.colorMap;
		this.autoblend = that.autoblend;
		this.show = that.show;
	}

	/**
	 * Compare two LayerInfo objects
	 */
	@Override
	public int compareTo(LayerInfo that) {
		return (this.name.compareTo(that.name));
	}

	@Override
	public String toString() {
		String str = name;
		if ((type == LayerType.footprint) || (type == LayerType.viewshed)) {
			str += " " + type;
		}
		return (str);
	}

	/**
	 * Prepare this LayerInfo object to be persisted.
	 */
	public HashMap<String,Object> getAsHashMap() {
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("Name", name);
		map.put("Type", type.toString());
		map.put("Opacity", new Double(opacity));
		map.put("LayerNumber", new Integer(layerNumber));
		map.put("Autoblend", new Boolean(autoblend));
		map.put("Show", new Integer(show));
		if (colorMap != null) {
			map.put("ColorMap.name", colorMap.getName());
			map.put("ColorMap.gradient", new Boolean(colorMap.isGradient()));
			map.put("ColorMap.minimum", new Double(colorMap.getMinimum()));
			map.put("ColorMap.maximum", new Double(colorMap.getMaximum()));
		}
		else if (colorMapName != null) {
			map.put("ColorMap.name", colorMapName);
			map.put("ColorMap.gradient", new Boolean(gradient));
			map.put("ColorMap.minimum", new Double(minimum));
			map.put("ColorMap.maximum", new Double(maximum));
		}
		return(map);
	}

}
