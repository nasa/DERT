package gov.nasa.arc.dert.scene.landmark;

import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.scene.MapElement;

import javax.swing.Icon;

/**
 * Interface for MapElements that are Landmarks
 *
 */
public interface Landmark extends MapElement {

	public Icon getIcon();

	public void update(BasicCamera camera);

}
