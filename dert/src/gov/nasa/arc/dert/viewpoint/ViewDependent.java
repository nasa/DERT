package gov.nasa.arc.dert.viewpoint;

import gov.nasa.arc.dert.camera.BasicCamera;

/**
 * Tags objects that need to be updated when the worldview viewpoint changes.
 *
 */
public interface ViewDependent {

	/**
	 * Update this object
	 * 
	 * @param camera
	 */
	public void update(BasicCamera camera);

}
