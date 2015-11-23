package gov.nasa.arc.dert.viewpoint;

import java.io.Serializable;

/**
 * Data structure that provides fly through parameters and can be persisted.
 *
 */
public class FlyThroughParameters implements Serializable {

	public int numInbetweens = 10;
	public int millisPerFrame = 100;
	public double pathHeight = 5;
	public boolean loop = false;

}
