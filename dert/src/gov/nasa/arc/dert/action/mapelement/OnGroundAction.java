package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.action.UndoHandler;
import gov.nasa.arc.dert.scene.MapElement;

/**
 * Context menu item for placing a map element at a point in the landscape. User
 * is prompted with a list of map elements.
 *
 */
public class OnGroundAction extends MenuItemAction {

	protected MapElement mapElement;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public OnGroundAction(MapElement mapElement) {
		super("Ground "+ mapElement.getName());
		this.mapElement = mapElement;
	}

	@Override
	protected void run() {
		UndoHandler.getInstance().addEdit(mapElement.ground());
	}

}
