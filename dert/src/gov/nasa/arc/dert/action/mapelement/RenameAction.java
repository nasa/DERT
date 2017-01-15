package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.tool.Waypoint;

/**
 * Context menu item for renaming a map element.
 *
 */
public class RenameAction extends MenuItemAction {

	protected MapElement mapElement;

	/**
	 * Constructor
	 * 
	 * @param mapElement
	 */
	public RenameAction(MapElement mapElement) {
		super("Rename " + mapElement.getName());
		this.mapElement = mapElement;
	}

	@Override
	protected void run() {
		String nameStr = NameDialog.getName(Dert.getMainWindow(), mapElement.getName());
		if (nameStr == null) {
			return;
		}
		if (mapElement instanceof Waypoint) {
			mapElement.getState().setAnnotation(nameStr);
		} else {
			mapElement.setName(nameStr);
		}
	}

}
