package gov.nasa.arc.dert.viewpoint;

/**
 * Tags objects that need to be updated when the worldview viewpoint changes.
 *
 */
public interface ViewDependent {

	static public int PIXEL_SIZE = 20;

	/**
	 * Update this object
	 * 
	 * @param camera
	 */
	public void update(BasicCamera camera);

}
