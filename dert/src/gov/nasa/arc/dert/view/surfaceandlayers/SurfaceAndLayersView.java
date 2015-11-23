package gov.nasa.arc.dert.view.surfaceandlayers;

import gov.nasa.arc.dert.state.State;
import gov.nasa.arc.dert.view.JPanelView;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;

/**
 * Provides controls to set options for layers and landscape surface.
 *
 */
public class SurfaceAndLayersView extends JPanelView {

	private LayersPanel layersPanel;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public SurfaceAndLayersView(State state) {
		super(state);

		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		layersPanel = new LayersPanel(state);

		add(new SurfacePanel(state), BorderLayout.NORTH);
		add(layersPanel, BorderLayout.CENTER);
	}

	/**
	 * Available layers have changed
	 */
	public void updateSelectedLayers() {
		layersPanel.updateSelectedLayers();
	}

}
