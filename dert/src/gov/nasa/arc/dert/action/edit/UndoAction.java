package gov.nasa.arc.dert.action.edit;

import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.action.UndoHandler;

/**
 * Provides an Undo menu entry for the Edit menu.
 *
 */
public class UndoAction extends MenuItemAction {

	protected UndoHandler undoHandler;

	/**
	 * Constructor
	 * 
	 * @param undoHandler
	 */
	public UndoAction(UndoHandler undoHandler) {
		super("Undo");
		this.undoHandler = undoHandler;
		setEnabled(false);
	}

	@Override
	public void run() {
		undoHandler.undo();
	}

}
