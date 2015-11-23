package gov.nasa.arc.dert.view;

import gov.nasa.arc.dert.state.State;

import java.awt.BorderLayout;
import java.awt.Panel;

/**
 * Abstract base class for heavy weight Views.
 *
 */
public abstract class PanelView extends Panel implements View {

	protected State state;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public PanelView(State state) {
		super();
		this.state = state;
		setLayout(new BorderLayout());
	}

	/**
	 * Get the state
	 */
	@Override
	public State getState() {
		return (state);
	}

	/**
	 * Close the view
	 */
	@Override
	public void close() {
		// nothing here
	}
}
