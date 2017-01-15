package gov.nasa.arc.dert.view.world;

import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scenegraph.Movable;

import javax.swing.undo.AbstractUndoableEdit;

/**
 * Provides an undo edit for moving a map element.
 *
 */
public class GroundEdit extends AbstractUndoableEdit {

	private Movable movable;
	private double oldZOffset;
	private MapElement mapElement;
	private GroundEdit[] ge;

	/**
	 * Constructor
	 * 
	 * @param spatial
	 * @param oldPosition
	 */
	public GroundEdit(Movable movable, double oldZOffset) {
		this.movable = movable;
		this.oldZOffset = oldZOffset;
	}

	/**
	 * Constructor
	 * 
	 * @param spatial
	 * @param oldPosition
	 */
	public GroundEdit(MapElement mapElement, GroundEdit[] ge) {
		this.mapElement = mapElement;
		this.ge = ge;
	}

	@Override
	public String getPresentationName() {
		if (movable != null)
			return ("Ground " + movable.getName());
		else
			return("Ground "+mapElement.getName());
	}

	@Override
	public void undo() {
		super.undo();
		if (movable != null)
			movable.setZOffset(oldZOffset, true);
		else {
			for (int i=0; i<ge.length; ++i)
				ge[i].undo();
		}
	}

	@Override
	public void redo() {
		super.redo();
		if (movable != null)
			movable.setZOffset(0, true);
		else
			for (int i=0; i<ge.length; ++i)
				ge[i].redo();
	}

}
