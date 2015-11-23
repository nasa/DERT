package gov.nasa.arc.dert.scene.tool;

import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.viewpoint.BasicCamera;

import javax.swing.Icon;

/**
 * Interface for map elements that are tools
 *
 */
public interface Tool extends MapElement {

	public Icon getIcon();

	public void update(BasicCamera camera);

}
