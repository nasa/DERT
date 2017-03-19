package gov.nasa.arc.dert.view;

import gov.nasa.arc.dert.state.State;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

/**
 * View for the color maps currently in use.
 *
 */
public class ColorBarView extends JPanelView {

	private ColorBarPanel colorBarPanel;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public ColorBarView(State state) {
		super(state);
		colorBarPanel = ColorBarPanel.getInstance();
		JScrollPane scrollPane = new JScrollPane(colorBarPanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		add(scrollPane, BorderLayout.CENTER);
	}
}
