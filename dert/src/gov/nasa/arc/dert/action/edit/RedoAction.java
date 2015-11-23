package gov.nasa.arc.dert.action.edit;

import gov.nasa.arc.dert.UndoHandler;
import gov.nasa.arc.dert.action.MenuItemAction;

/**
 * Provides a Redo menu entry for the Edit menu.
 *
 */
public class RedoAction extends MenuItemAction {

	protected UndoHandler undoHandler;

	/**
	 * Constructor
	 * 
	 * @param undoHandler
	 */
	public RedoAction(UndoHandler undoHandler) {
		super("Redo");
		this.undoHandler = undoHandler;
		setEnabled(false);
	}

	@Override
	public void run() {
		undoHandler.redo();
	}

}
