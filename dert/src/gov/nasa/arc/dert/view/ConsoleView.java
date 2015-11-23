package gov.nasa.arc.dert.view;

import gov.nasa.arc.dert.state.PanelState;

import java.awt.BorderLayout;

/**
 * View for the console.
 *
 */
public class ConsoleView extends JPanelView {

	private Console console;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public ConsoleView(PanelState state) {
		super(state);
		console = Console.createInstance();
		add(console, BorderLayout.CENTER);
	}

	/**
	 * Set the console state
	 * 
	 * @param state
	 */
	public void setState(PanelState state) {
		this.state = state;
	}

}
