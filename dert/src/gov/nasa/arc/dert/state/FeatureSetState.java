package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.featureset.FeatureSet;
import gov.nasa.arc.dert.util.StateUtil;

import java.awt.Color;
import java.util.HashMap;

/**
 * Provides a state object for a FeatureSet.
 *
 */
public class FeatureSetState extends MapElementState {

	// Path to the FeatureSet file
	public String filePath;
	public boolean isProjected;
	public String labelProp;
	public boolean ground;
	public float lineWidth;

	/**
	 * Constructor for LayerFactory.
	 * 
	 * @param name
	 * @param filePath
	 * @param color
	 */
	public FeatureSetState(String name, String filePath, Color color, boolean isProjected, boolean ground, String labelProp) {
		super(0, MapElementState.Type.FeatureSet, "", FeatureSet.defaultSize, color, false);
		this.name = name;
		this.filePath = filePath;
		this.isProjected = isProjected;
		this.labelProp = labelProp;
		this.ground = ground;
		this.lineWidth = FeatureSet.defaultLineWidth;
		pinned = true;
	}

	/**
	 * Constructor for DERT.
	 * 
	 * @param name
	 * @param filePath
	 * @param color
	 * @param notes
	 */
	public FeatureSetState(String name, String filePath, Color color, String notes, boolean isProjected, boolean ground, String labelProp) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration()
			.incrementMapElementCount(MapElementState.Type.FeatureSet), MapElementState.Type.FeatureSet, "", FeatureSet.defaultSize, color,
			false);
		this.name = name;
		this.filePath = filePath;
		this.isProjected = isProjected;
		this.labelProp = labelProp;
		this.ground = ground;
		this.lineWidth = FeatureSet.defaultLineWidth;
		this.annotation = notes;
		pinned = true;
	}
	
	/**
	 * Constructor for hash map
	 */
	public FeatureSetState(HashMap<String,Object> map) {
		super(map);
		filePath = StateUtil.getString(map, "FilePath", null);
		isProjected = StateUtil.getBoolean(map, "IsProjected", false);
		labelProp = StateUtil.getString(map, "LabelProperty", null);
		ground = StateUtil.getBoolean(map, "Ground", false);
		lineWidth = (float)StateUtil.getDouble(map, "LineWidth", FeatureSet.defaultLineWidth);
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof FeatureSetState)) 
			return(false);
		FeatureSetState that = (FeatureSetState)state;
		if (!super.isEqualTo(that)) 
			return(false);
		if (this.isProjected != that.isProjected)
			return(false);
		if (this.ground != that.ground)
			return(false);
		if (this.lineWidth != that.lineWidth)
			return(false);
		if (!this.filePath.equals(that.filePath)) 
			return(false);
		if (!this.labelProp.equals(that.labelProp))
			return(false);
		return(true);
	}

	@Override
	public HashMap<String,Object> save() {
		HashMap<String,Object> map = super.save();
		if (mapElement != null) {
			FeatureSet featureSet = (FeatureSet) mapElement;
			filePath = featureSet.getFilePath();
		}
		map.put("FilePath", filePath);
		map.put("IsProjected", new Boolean(isProjected));
		map.put("Ground", new Boolean(ground));
		map.put("LineWidth", new Float(lineWidth));
		if (labelProp != null)
			map.put("LabelProperty", labelProp);
		return(map);
	}
	
	@Override
	public String toString() {
		String str = isProjected+" "+filePath+" "+labelProp+" "+super.toString();
		return(str);
	}
}
