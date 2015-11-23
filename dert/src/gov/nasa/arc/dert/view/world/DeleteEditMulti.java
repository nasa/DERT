package gov.nasa.arc.dert.view.world;

import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.MapElementState;

import javax.swing.undo.AbstractUndoableEdit;

/**
 * Provides an undo edit for deleting multiple map elements.
 *
 */
public class DeleteEditMulti extends AbstractUndoableEdit {

	private MapElementState[] state;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public DeleteEditMulti(MapElementState[] state) {
		this.state = state;
		for (int i = 0; i < state.length; ++i) {
			state[i].save();
		}
		ConfigurationManager.getInstance().getCurrentConfiguration().removeMapElementState(state);
	}

	@Override
	public String getPresentationName() {
		return ("Delete " + state[0].name + "...");
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
