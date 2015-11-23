package gov.nasa.arc.dert.view.world;

import javax.swing.undo.AbstractUndoableEdit;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Spatial;

/**
 * Provides an undo edit for moving a map element.
 *
 */
public class MoveEdit extends AbstractUndoableEdit {

	private Spatial spatial;
	private ReadOnlyVector3 oldPosition, position;

	/**
	 * Constructor
	 * 
	 * @param spatial
	 * @param oldPosition
	 */
	public MoveEdit(Spatial spatial, ReadOnlyVector3 oldPosition) {
		this.spatial = spatial;
		this.oldPosition = oldPosition;
	}

	@Override
	public String getPresentationName() {
		return ("Move " + spatial.getName());
	}

	@Override
	public void undo() {
		super.undo();
		position = new Vector3(spatial.getTranslation());
		spatial.setTranslation(oldPosition);
	}

	@Override
	public void redo() {
		super.redo();
		spatial.setTranslation(position);
	}

}
