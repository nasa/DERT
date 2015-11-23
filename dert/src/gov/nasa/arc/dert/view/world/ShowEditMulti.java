package gov.nasa.arc.dert.view.world;

import gov.nasa.arc.dert.scene.MapElement;

import javax.swing.undo.AbstractUndoableEdit;

/**
 * Provides an undo edit for showing multiple map elements.
 *
 */
public class ShowEditMulti extends AbstractUndoableEdit {

	private MapElement[] mapElement;

	/**
	 * Constructor
	 * 
	 * @param mapElement
	 */
	public ShowEditMulti(MapElement[] mapElement) {
		this.mapElement = mapElement;
		for (int i = 0; i < mapElement.length; ++i) {
			mapElement[i].setVisible(true);
		}
	}

	@Override
	public String getPresentationName() {
		return ("Show " + mapElement[0].getName() + "...");
	}

	@Override
	public void undo() {
		super.undo();
		for (int i = 0; i < mapElement.length; ++i) {
			mapElement[i].setVisible(false);
		}
	}

	@Override
	public void redo() {
		super.redo();
		for (int i = 0; i < mapElement.length; ++i) {
			mapElement[i].setVisible(true);
		}
	}

}
