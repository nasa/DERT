package gov.nasa.arc.dert.view.viewpoint;

import gov.nasa.arc.dert.state.AnimationState;
import gov.nasa.arc.dert.view.JPanelView;
import gov.nasa.arc.dert.viewpoint.FlyThroughParameters;

import java.awt.BorderLayout;

/**
 * Provides a dialog for setting fly through animation options.
 *
 */
public class AnimationView extends JPanelView {
	
	private AnimationPanel animationPanel;

	/**
	 * Constructor
	 * 
	 * @param cntlr
	 * @param p
	 */
	public AnimationView(AnimationState state) {
		super(state);
		setLayout(new BorderLayout());
		animationPanel = new AnimationPanel(state);
		add(animationPanel, BorderLayout.CENTER);
	}
	
	@Override
	public void close() {
		animationPanel.close();
	}
	
	public FlyThroughParameters getViewpointFlyParams() {
		return(animationPanel.getViewpointFlyParams());
	}
	
	public Object getSubject() {
		return(animationPanel.getSubject());
	}

}
