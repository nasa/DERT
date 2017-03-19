package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.state.MapElementsState;
import gov.nasa.arc.dert.view.JPanelView;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;

/**
 * Provides a view that allows map elements to be added, removed, viewed, and
 * edited. Map element preference options are also available here.
 *
 */
public class MapElementsView extends JPanelView {

	private MapElementsPanel panel;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public MapElementsView(MapElementsState state) {
		super(state);

		setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
		
		panel = new MapElementsPanel(state);
		add(panel, BorderLayout.CENTER);
	}

	/**
	 * Select a map element to display
	 * 
	 * @param mapElement
	 */
	public void selectMapElement(MapElement mapElement) {
		panel.selectMapElement(mapElement);
	}

}
