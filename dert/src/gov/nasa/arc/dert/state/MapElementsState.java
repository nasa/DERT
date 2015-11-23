package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.MapElement;

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
		super(PanelType.MapElements, "Map Elements", new ViewData(-1, -1, 550, -1, false));
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

}
