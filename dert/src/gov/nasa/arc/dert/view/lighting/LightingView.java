package gov.nasa.arc.dert.view.lighting;

import gov.nasa.arc.dert.state.State;
import gov.nasa.arc.dert.view.JPanelView;

import java.awt.BorderLayout;

/**
 * Provides controls for setting lighting options.
 *
 */
public class LightingView extends JPanelView {

	private LightingPanel panel;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public LightingView(State state) {
		super(state);
		panel = new LightingPanel();
		add(panel, BorderLayout.CENTER);
	}
	
	@Override
	public void close() {
		panel.dispose();
	}

}
