package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.featureset.Feature;
import gov.nasa.arc.dert.scene.featureset.FeatureSet;
import gov.nasa.arc.dert.view.View;
import gov.nasa.arc.dert.view.mapelement.FeatureSetView;

import java.awt.Color;

/**
 * Base class for map element state objects.
 *
 */
public class FeatureState extends MapElementState {

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param name
	 * @param mapElementType
	 * @param prefix
	 * @param color
	 */
	public FeatureState(int id, String name, Type mapElementType, String prefix, Color color) {
		super(id, mapElementType, prefix, 1, color, false);
		if (name != null)
			this.name = name;
	}

	/**
	 * Open the view
	 */
	@Override
	public View open(boolean doIt) {
		if (mapElement == null)
			return(null);
		FeatureSet parent = ((Feature)mapElement).getFeatureSet();
		if (parent == null)
			return(null);
		FeatureSetView fsv = (FeatureSetView)parent.getState().open(doIt);
		if (fsv != null) {
			fsv.setMapElement(mapElement);
		}
		return(fsv);
	}

}
