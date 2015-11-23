package gov.nasa.arc.dert.view.world;

import gov.nasa.arc.dert.scene.MapElement;

import javax.swing.undo.AbstractUndoableEdit;

/**
 * Provides an undo edit for showing a map element.
 *
 */
public class ShowEdit extends AbstractUndoableEdit {

	private MapElement mapElement;

	/**
	 * Constructor
	 * 
	 * @param mapElement
	 */
	public ShowEdit(MapElement mapElement) {
		this.mapElement = mapElement;
		mapElement.setVisible(true);
	}

	@Override
	public String getPresentationName() {
		return ("Show " + mapElement.getName());
	}

	@Override
	public void undo() {
		super.undo();
		mapElement.setVisible(false);
	}

	@Override
	public void redo() {
		super.redo();
		mapElement.setVisible(true);
	}

}
