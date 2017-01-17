package gov.nasa.arc.dert.view.lighting;

import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.state.State;
import gov.nasa.arc.dert.view.JPanelView;

import java.awt.BorderLayout;
import java.awt.Window;

/**
 * Provides controls for setting light position with modes for Azimuth and
 * Elevation, and Local Mean Solar Time.
 *
 */
public class LightPositionView extends JPanelView {

	private TimePanel timePanel;
	private AzElPanel azElPanel;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public LightPositionView(State state) {
		super(state);
		setMode();
	}

	/**
	 * Set the mode for light positioning. Az/el positioning for artificial
	 * light and LMST for solar.
	 */
	public void setMode() {
		Lighting lighting = World.getInstance().getLighting();
		if (lighting.isLampMode()) {
			if (timePanel != null) {
				remove(timePanel);
				timePanel = null;
			}
			if (azElPanel == null) {
				azElPanel = new AzElPanel();
			}
			add(azElPanel, BorderLayout.CENTER);
		} else {
			if (azElPanel != null) {
				remove(azElPanel);
				azElPanel = null;
			}
			if (timePanel == null) {
				timePanel = new TimePanel();
			}
			add(timePanel, BorderLayout.CENTER);
		}
		Window window = (Window)getTopLevelAncestor();
		if (window != null)
			window.pack();
		else
			revalidate();
	}

}
