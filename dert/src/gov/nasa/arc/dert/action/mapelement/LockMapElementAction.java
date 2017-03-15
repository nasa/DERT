package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.scene.MapElement;

/**
 * Context menu item for hiding a map element.
 *
 */
public class LockMapElementAction extends MenuItemAction {

	protected MapElement mapElement;

	/**
	 * Constructor
	 * 
	 * @param mapElement
	 */
	public LockMapElementAction(MapElement mapElement) {
		super((mapElement.isLocked() ? "Unlock " : "Lock ") + mapElement.getName());
		this.mapElement = mapElement;
	}

	@Override
	protected void run() {
		mapElement.getState().setLocked(!mapElement.isLocked());
	}

}
