package gov.nasa.arc.dert.view;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.ui.ColorBar;
import gov.nasa.arc.dert.util.ColorMap;

import java.awt.GridLayout;
import java.awt.Label;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Provides a JPanel that contains a color bar for each color map in use.
 * Displayed in the ColorBarView.
 *
 */
public class ColorBarPanel extends JPanel {

	// Singleton
	protected static ColorBarPanel instance;

	// List of color bars to be displayed
	protected ArrayList<ColorBar> colorBar;

	// Container of color bars
	protected JPanel container;

	/**
	 * Get the color bar panel
	 * 
	 * @return
	 */
	public static ColorBarPanel getInstance() {
		if (instance == null) {
			instance = new ColorBarPanel();
		}
		return (instance);
	}

	/**
	 * Reset all the color bars
	 */
	public static void resetColorBars() {
		if (instance == null) {
			return;
		}
		instance.reset();
	}

	/**
	 * Constructor
	 */
	protected ColorBarPanel() {
		colorBar = new ArrayList<ColorBar>();
		setLayout(new GridLayout(1, 1));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		container = new JPanel();
		add(container);
		reset();
	}

	/**
	 * Reset all of the color bars
	 */
	public void reset() {
		container.removeAll();
		colorBar.clear();
		ArrayList<ColorMap> colorMapList = Landscape.getInstance().getLayerManager().getColorMaps();
		for (int i = 0; i < colorMapList.size(); ++i) {
			colorBar.add(new ColorBar(colorMapList.get(i), false));
		}
		if (colorBar.size() == 0) {
			container.setLayout(new GridLayout(1, 1));
			container.add(new Label("No color bars."));
		} else {
			container.setLayout(new GridLayout(colorBar.size(), 1));
			for (int i = 0; i < colorBar.size(); ++i) {
				container.add(colorBar.get(i));
			}
		}
		validate();
		repaint();
	}
}
