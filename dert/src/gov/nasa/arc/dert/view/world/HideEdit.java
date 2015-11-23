package gov.nasa.arc.dert.view.world;

import gov.nasa.arc.dert.scene.MapElement;

import javax.swing.undo.AbstractUndoableEdit;

/**
 * Provides an undo edit for hiding a map element.
 *
 */
public class HideEdit extends AbstractUndoableEdit {

	private MapElement mapElement;

	/**
	 * Constructor
	 * 
	 * @param mapElement
	 */
	public HideEdit(MapElement mapElement) {
		this.mapElement = mapElement;
		mapElement.setVisible(false);
	}

	@Override
	public String getPresentationName() {
		return ("Hide " + mapElement.getName());
	}

	@Override
	public void undo() {
		super.undo();
		mapElement.setVisible(true);
	}

	@Override
	public void redo() {
		super.redo();
		mapElement.setVisible(false);
	}

}
