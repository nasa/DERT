package gov.nasa.arc.dert.view.world;

import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.MapElementState;

import javax.swing.undo.AbstractUndoableEdit;

/**
 * Provides an undo edit for deleting a map element.
 *
 */
public class DeleteEdit extends AbstractUndoableEdit {

	private MapElementState state;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public DeleteEdit(MapElementState state) {
		this.state = state;
		state.save();
		ConfigurationManager.getInstance().getCurrentConfiguration().removeMapElementState(state);
	}

	@Override
	public String getPresentationName() {
		return ("Delete " + state.name);
	}

	@Override
	public void undo() {
		super.undo();
		ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state);
	}

	@Override
	public void redo() {
		super.redo();
		ConfigurationManager.getInstance().getCurrentConfiguration().removeMapElementState(state);
	}

}
