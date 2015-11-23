package gov.nasa.arc.dert.view;

import gov.nasa.arc.dert.state.State;

/**
 * Interface for Views
 *
 */
public interface View {

	public State getState();

	public void close();
}
