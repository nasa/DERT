package gov.nasa.arc.dert.view.world;

import gov.nasa.arc.dert.scenegraph.Movable;

import javax.swing.undo.AbstractUndoableEdit;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides an undo edit for moving a map element.
 *
 */
public class MoveEdit extends AbstractUndoableEdit {

	private Movable movable;
	private ReadOnlyVector3 oldPosition, position;
	private boolean oldStrictZ, strictZ;

	/**
	 * Constructor
	 * 
	 * @param spatial
	 * @param oldPosition
	 */
	public MoveEdit(Movable movable, ReadOnlyVector3 oldPosition, boolean oldStrictZ) {
		this.movable = movable;
		this.oldPosition = oldPosition;
		this.oldStrictZ = oldStrictZ;
	}

	@Override
	public String getPresentationName() {
		return ("Move " + movable.getName());
	}

	@Override
	public void undo() {
		super.undo();
		position = new Vector3(movable.getTranslation());
		oldStrictZ = movable.isStrictZ();
		movable.setLocation(oldPosition.getX(), oldPosition.getY(), oldPosition.getZ(), false, false);
		movable.setStrictZ(oldStrictZ);
	}

	@Override
	public void redo() {
		super.redo();
		movable.setLocation(position.getX(), position.getY(), position.getZ(), false, false);
		movable.setStrictZ(strictZ);
	}

}
