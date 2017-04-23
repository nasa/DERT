package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.scene.MapElement;

/**
 * Context menu item for opening the annotation of a map element.
 *
 */
public class OpenAnnotationAction extends MenuItemAction {

	protected MapElement mapElement;

	/**
	 * Constructor
	 * 
	 * @param mapElement
	 */
	public OpenAnnotationAction(MapElement mapElement) {
		super("Show Notes for " + mapElement.getName());
		this.mapElement = mapElement;
	}

	@Override
	protected void run() {
		mapElement.getState().save();
		mapElement.getState().openAnnotation();
	}

}
