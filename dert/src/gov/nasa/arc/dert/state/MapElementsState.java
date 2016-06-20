package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.util.StateUtil;

import java.util.HashMap;

/**
 * State object for MapElementsView.
 *
 */
public class MapElementsState extends PanelState {

	// Name of last map element presented
	public String lastMapElementName;

	// Type of last map element presented
	public MapElementState.Type lastMapElementType;

	// The last map element presented
	public transient MapElement lastMapElement;

	/**
	 * Constructor
	 */
	public MapElementsState() {
		super(PanelType.MapElements, "Map Elements", new ViewData(-1, -1, 600, ViewData.DEFAULT_WINDOW_HEIGHT, false));
	}
	
	/**
	 * Constructor for hash map.
	 */
	public MapElementsState(HashMap<String,Object> map) {
		super(map);
		lastMapElementName = StateUtil.getString(map, "LastMapElementName", null);
		String str = StateUtil.getString(map, "LastMapElementType", null);
		if (str != null)
			lastMapElementType = MapElementState.Type.valueOf(str);
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof MapElementsState)) 
			return(false);
		MapElementsState that = (MapElementsState)state;
		if (!super.isEqualTo(that)) 
			return(false);
		if (!this.lastMapElementName.equals(that.lastMapElementName)) 
			return(false);
		if (this.lastMapElementType != that.lastMapElementType) 
			return(false);
		return(true);
	}

	@Override
	public HashMap<String,Object> save() {
		HashMap<String,Object> map = super.save();
		map.put("LastMapElementName", lastMapElementName);
		if (lastMapElementType != null)
			map.put("LastMapElementType", lastMapElementType.toString());
		return(map);
	}

	/**
	 * Get the last map element
	 * 
	 * @return
	 */
	public MapElement getLastMapElement() {
		if (lastMapElement != null) {
			return (lastMapElement);
		}
		if (lastMapElementName == null) {
			return (null);
		}
		if (lastMapElementType == null) {
			return (null);
		}
		MapElementState meState = ConfigurationManager.getInstance().getCurrentConfiguration()
			.findMapElementState(lastMapElementType, lastMapElementName);
		if (meState != null) {
			lastMapElement = meState.getMapElement();
		}
		return (lastMapElement);
	}

	/**
	 * Set the last map element
	 * 
	 * @param mapElement
	 */
	public void setLastMapElement(MapElement mapElement) {
		lastMapElement = mapElement;
		if (mapElement == null) {
			return;
		}
		lastMapElementType = mapElement.getType();
		lastMapElementName = mapElement.getName();
	}
	
	@Override
	public String toString() {
		String str = super.toString();
		str += " LastMapElement="+lastMapElementName+","+lastMapElementType;
		return(str);
	}

}
