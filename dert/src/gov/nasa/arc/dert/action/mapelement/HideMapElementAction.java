package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.action.UndoHandler;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.view.world.HideEdit;

/**
 * Context menu item for hiding a map element.
 *
 */
public class HideMapElementAction extends MenuItemAction {

	protected MapElement mapElement;

	/**
	 * Constructor
	 * 
	 * @param mapElement
	 */
	public HideMapElementAction(MapElement mapElement) {
		super("Hide " + mapElement.getName());
		this.mapElement = mapElement;
	}

	@Override
	protected void run() {
		// hide map element and notify the undo handler
		mapElement.setVisible(false);
		UndoHandler.getInstance().addEdit(new HideEdit(mapElement));
	}

}
