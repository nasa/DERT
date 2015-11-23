package gov.nasa.arc.dert.view;

import gov.nasa.arc.dert.state.State;

import java.awt.BorderLayout;

import javax.swing.JPanel;

/**
 * Abstract base class for lightweight Views.
 *
 */
public abstract class JPanelView extends JPanel implements View {

	protected State state;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public JPanelView(State state) {
		super();
		this.state = state;
		setLayout(new BorderLayout());
	}

	/**
	 * Get the view state
	 */
	@Override
	public State getState() {
		return (state);
	}

	/**
	 * Close this view
	 */
	@Override
	public void close() {
		// nothing here
	}
}
