package gov.nasa.arc.dert.view.viewpoint;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.state.ViewpointState;
import gov.nasa.arc.dert.view.JPanelView;

import java.awt.BorderLayout;

/**
 * Provides controls for setting view point attributes. Shows a list of saved
 * viewpoints.
 *
 */
public class ViewpointView extends JPanelView {

	private ViewpointPanel panel;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public ViewpointView(ViewpointState state) {
		super(state);
		panel = new ViewpointPanel(state);
		add(panel, BorderLayout.CENTER);
		Dert.getWorldView().getViewpointNode().setViewpointPanel(panel);
	}
	
	public ViewpointPanel getPanel() {
		return(panel);
	}

	/**
	 * Close this view
	 */
	@Override
	public void close() {
		panel.dispose();
	}

}
