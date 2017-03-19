package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.featureset.Feature;
import gov.nasa.arc.dert.scene.featureset.FeatureSet;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.mapelement.FeatureSetView;

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
	public int currentFeature;

	/**
	 * Constructor for FeatureSetState.
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
		viewData = new ViewData(-1, -1, 400, 350, false);
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
		super(ConfigurationManager.getInstance().getCurrentConfiguration().incrementMapElementCount(MapElementState.Type.FeatureSet),
			MapElementState.Type.FeatureSet, "", FeatureSet.defaultSize, color, false);
		this.name = name;
		this.filePath = filePath;
		this.isProjected = isProjected;
		this.labelProp = labelProp;
		this.ground = ground;
		this.lineWidth = FeatureSet.defaultLineWidth;
		this.annotation = notes;
		viewData = new ViewData(-1, -1, 550, 400, false);
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
		currentFeature = StateUtil.getInteger(map, "CurrentFeature", 0);
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
		if (this.labelProp == null) {
			if (that.labelProp != null)
				return(false);
		}
		else if (that.labelProp == null)
			return(false);
		else if (!this.labelProp.equals(that.labelProp))
			return(false);
		return(true);
	}

	@Override
	public HashMap<String,Object> save() {
		HashMap<String,Object> map = super.save();
		if (mapElement != null) {
			FeatureSet featureSet = (FeatureSet) mapElement;
			filePath = featureSet.getFilePath();
			lineWidth = featureSet.getLineWidth();			
		}
		map.put("FilePath", filePath);
		map.put("IsProjected", new Boolean(isProjected));
		map.put("Ground", new Boolean(ground));
		map.put("LineWidth", new Double(lineWidth));
		map.put("CurrentFeature", new Long(currentFeature));
		if (labelProp != null)
			map.put("LabelProperty", labelProp);
		return(map);
	}
	
	@Override
	public String toString() {
		String str = isProjected+" "+filePath+" "+labelProp+" "+super.toString();
		return(str);
	}

	/**
	 * Set the MapElement
	 * 
	 * @param mapElement
	 */
	@Override
	public void setMapElement(MapElement mapElement) {
		super.setMapElement(mapElement);
		if (viewData != null) {
			FeatureSetView fsv = (FeatureSetView)viewData.getView();
			if (fsv != null)
				fsv.setMapElement(mapElement);
		}
		if (mapElement instanceof Feature)
			currentFeature = mapElement.getState().id;
	}

	/**
	 * Create the view
	 */
	@Override
	protected void createView() {
		FeatureSetView fsv = new FeatureSetView(this);
		if (currentFeature != 0) {
			FeatureSet fs = (FeatureSet)mapElement;
			Feature f = fs.getFeature((int)currentFeature);
			System.err.println("FeatureSetState.createView "+currentFeature+" "+f);
			fsv.setMapElement(f);
		}
		setView(fsv);
		viewData.createWindow(Dert.getMainWindow(), name+" View", X_OFFSET, Y_OFFSET);
	}
}
