package gov.nasa.arc.dert.view.world;

import gov.nasa.arc.dert.scene.MapElement;

import javax.swing.undo.AbstractUndoableEdit;

/**
 * Provides an undo edit for hiding multiple map elements.
 *
 */
public class HideEditMulti extends AbstractUndoableEdit {

	private MapElement[] mapElement;

	/**
	 * Constructor
	 * 
	 * @param mapElement
	 */
	public HideEditMulti(MapElement[] mapElement) {
		this.mapElement = mapElement;
		for (int i = 0; i < mapElement.length; ++i) {
			mapElement[i].setVisible(false);
		}
	}

	@Override
	public String getPresentationName() {
		return ("Hide " + mapElement[0].getName() + "...");
	}

	@Override
	public void undo() {
		super.undo();
		for (int i = 0; i < mapElement.length; ++i) {
			mapElement[i].setVisible(true);
		}
	}

	@Override
	public void redo() {
		super.redo();
		for (int i = 0; i < mapElement.length; ++i) {
			mapElement[i].setVisible(false);
		}
	}

}
