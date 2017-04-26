package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.scene.MapElement;

/**
 * Context menu item for editing a map element.
 *
 */
public class EditAction extends MenuItemAction {

	protected MapElement mapElement;

	/**
	 * Constructor
	 * 
	 * @param mapElement
	 */
	public EditAction(MapElement mapElement) {
		super("Edit " + mapElement.getName());
		this.mapElement = mapElement;
	}

	/**
	 * Constructor
	 * 
	 * @param mapElement
	 */
	public EditAction(MapElement mapElement, String label) {
		super("Edit " + label);
		this.mapElement = mapElement;
	}

	@Override
	protected void run() {
		mapElement.getState().openEditor();
	}

}
