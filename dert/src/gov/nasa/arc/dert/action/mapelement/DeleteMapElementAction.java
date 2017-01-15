package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.ui.OptionDialog;
import gov.nasa.arc.dert.view.world.DeleteEdit;

/**
 * Context menu item for deleting a map element.
 *
 */
public class DeleteMapElementAction extends MenuItemAction {

	protected MapElement mapElement;

	/**
	 * Constructor
	 * 
	 * @param mapElement
	 */
	public DeleteMapElementAction(MapElement mapElement) {
		super("Delete " + mapElement.getName());
		this.mapElement = mapElement;
	}

	@Override
	protected void run() {
		// Get a confirmation from the user
		boolean yes = OptionDialog.showDeleteConfirmDialog(Dert.getMainWindow(), "Delete " + mapElement.getName() + "?");
		// Save the state and give it to the undo handler.
		if (yes) {
			MapElementState state = mapElement.getState();
			state.save();
			ConfigurationManager.getInstance().getCurrentConfiguration().removeMapElementState(state);
			Dert.getMainWindow().getUndoHandler().addEdit(new DeleteEdit(state));
		}

	}

}
