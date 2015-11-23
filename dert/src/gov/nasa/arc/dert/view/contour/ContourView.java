package gov.nasa.arc.dert.view.contour;

import gov.nasa.arc.dert.state.PlaneState;
import gov.nasa.arc.dert.view.PanelView;

import java.awt.BorderLayout;

/**
 * A view for the elevation difference map produced by a Plane tool.
 *
 */
public class ContourView extends PanelView {

	private ContourScenePanel panel;

	/**
	 * Constructor
	 * 
	 * @param viewState
	 */
	public ContourView(PlaneState viewState) {
		super(viewState);
		panel = new ContourScenePanel(viewState);
		add(panel, BorderLayout.CENTER);
	}

	public ContourScenePanel getContourScenePanel() {
		return (panel);
	}

}
