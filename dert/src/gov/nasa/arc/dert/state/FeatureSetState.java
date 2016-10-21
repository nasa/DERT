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

	/**
	 * Constructor for LayerFactory.
	 * 
	 * @param name
	 * @param filePath
	 * @param color
	 */
	public FeatureSetState(String name, String filePath, Color color) {
		super(0, MapElementState.Type.FeatureSet, "", 1.0, color, false);
		this.name = name;
		this.filePath = filePath;
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
	public FeatureSetState(String name, String filePath, Color color, String notes) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration()
			.incrementMapElementCount(MapElementState.Type.FeatureSet), MapElementState.Type.FeatureSet, "", 1.0, color,
			false);
		this.name = name;
		this.filePath = filePath;
		this.annotation = notes;
		pinned = true;
	}
	
	/**
	 * Constructor for hash map
	 */
	public FeatureSetState(HashMap<String,Object> map) {
		super(map);
		filePath = StateUtil.getString(map, "FilePath", null);
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof FeatureSetState)) 
			return(false);
		FeatureSetState that = (FeatureSetState)state;
		if (!super.isEqualTo(that)) 
			return(false);
		if (!this.filePath.equals(that.filePath)) 
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
		return(map);
	}
	
	@Override
	public String toString() {
		String str = filePath+" "+super.toString();
		return(str);
	}
}
