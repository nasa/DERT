package gov.nasa.arc.dert.io;

import java.io.Serializable;

/**
 * Provides a data structure to record a landscape tile path/id in a quad tree
 * structure.
 *
 */
public class DepthTree implements Serializable {

	public String id;
	public DepthTree[] child;

	@Override
	public String toString() {
		return (id);
	}
}
