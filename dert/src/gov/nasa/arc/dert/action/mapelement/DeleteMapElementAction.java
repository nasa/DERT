package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.view.world.DeleteEdit;

import javax.swing.JOptionPane;

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
		int answer = JOptionPane.showConfirmDialog(Dert.getMainWindow(), "Delete " + mapElement.getName() + "?",
			"Confirm Delete", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
			Icons.getImageIcon("delete.png"));
		// Save the state and give it to the undo handler.
		if (answer == JOptionPane.OK_OPTION) {
			MapElementState state = mapElement.getState();
			state.save();
			ConfigurationManager.getInstance().getCurrentConfiguration().removeMapElementState(state);
			Dert.getMainWindow().getUndoHandler().addEdit(new DeleteEdit(state));
		}

	}

}
