package gov.nasa.arc.dert.view;

import gov.nasa.arc.dert.state.State;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JPanel;

/**
 * Abstract base class for lightweight Views.
 *
 */
public abstract class JPanelView extends JPanel implements View {

	protected State state;
	private Insets insets = new Insets(5, 5, 5, 5);

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
	
	@Override
	public Insets getInsets() {
		return(insets);
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
