package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.scene.MapElement;

/**
 * Context menu item for hiding a map element.
 *
 */
public class PinMapElementAction extends MenuItemAction {

	protected MapElement mapElement;

	/**
	 * Constructor
	 * 
	 * @param mapElement
	 */
	public PinMapElementAction(MapElement mapElement) {
		super((mapElement.isPinned() ? "Unlock " : "Lock ") + mapElement.getName());
		this.mapElement = mapElement;
	}

	@Override
	protected void run() {
		mapElement.setPinned(!mapElement.isPinned());
	}

}
