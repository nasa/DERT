package gov.nasa.arc.dert.view.world;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.viewpoint.Viewpoint;
import gov.nasa.arc.dert.viewpoint.ViewpointStore;

import javax.swing.undo.AbstractUndoableEdit;

/**
 * Provides an undo edit for seeking a map element.
 *
 */
public class SeekEdit extends AbstractUndoableEdit {

	private MapElement mapElement;
	private ViewpointStore vpStore;

	/**
	 * Constructor
	 * 
	 * @param mapElement
	 */
	public SeekEdit(MapElement mapElement) {
		this.mapElement = mapElement;
		WorldView wv = Dert.getWorldView();
		Viewpoint cameraControl = ((WorldScene) wv.getScenePanel().getScene()).getViewpoint();
		vpStore = cameraControl.get("");
	}

	@Override
	public String getPresentationName() {
		return ("Seek " + mapElement.getName());
	}

	@Override
	public void undo() {
		super.undo();
		WorldView wv = Dert.getWorldView();
		Viewpoint cameraControl = ((WorldScene) wv.getScenePanel().getScene()).getViewpoint();
		cameraControl.set(vpStore, false);
	}

	@Override
	public void redo() {
		super.redo();
		WorldView wv = Dert.getWorldView();
		Viewpoint cameraControl = ((WorldScene) wv.getScenePanel().getScene()).getViewpoint();
		cameraControl.seek(mapElement);
	}

}
