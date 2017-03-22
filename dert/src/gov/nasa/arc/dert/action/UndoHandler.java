package gov.nasa.arc.dert.action;

import gov.nasa.arc.dert.action.edit.RedoAction;
import gov.nasa.arc.dert.action.edit.UndoAction;

import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * Handles Undo and Redo requests.
 *
 */
public class UndoHandler {
	
	private static UndoHandler instance;

	private UndoManager undoManager;
	private MenuItemAction undoAction, redoAction;
	
	public static UndoHandler getInstance() {
		if (instance == null)
			instance = new UndoHandler();
		return(instance);
	}

	protected UndoHandler() {
		undoManager = new UndoManager();
		undoAction = new UndoAction(this);
		undoAction.setEnabled(false);
		redoAction = new RedoAction(this);
		redoAction.setEnabled(false);
	}

	public MenuItemAction getUndoAction() {
		return (undoAction);
	}

	public MenuItemAction getRedoAction() {
		return (redoAction);
	}

	public void undo() {
		try {
			undoManager.undo();
		} catch (Exception e) {
			System.out.println("Unable to perform Undo.  See log.");
			e.printStackTrace();
		} finally {
			updateUndoItems();
		}

	}

	public void redo() {
		try {
			undoManager.redo();
		} catch (Exception e) {
			System.out.println("Unable to perform Redo.  See log.");
			e.printStackTrace();
		} finally {
			updateUndoItems();
		}
	}

	public void updateUndoItems() {
		undoAction.setText(undoManager.getUndoPresentationName());
		redoAction.setText(undoManager.getRedoPresentationName());
//		undoAction.validate();
		undoAction.setEnabled(undoManager.canUndo());
		redoAction.setEnabled(undoManager.canRedo());
	}

	public void addEdit(UndoableEdit edit) {
		if (edit == null)
			return;
		undoManager.addEdit(edit);
		updateUndoItems();
	}
}
