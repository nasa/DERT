package gov.nasa.arc.dert.scene.tool;

import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.scene.MapElement;

import javax.swing.Icon;

/**
 * Interface for map elements that are tools
 *
 */
public interface Tool extends MapElement {

	public Icon getIcon();

	public void update(BasicCamera camera);
	
	public void setHiddenDashed(boolean hiddenDashed);

}
