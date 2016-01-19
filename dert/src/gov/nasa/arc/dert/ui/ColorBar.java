package gov.nasa.arc.dert.ui;

import gov.nasa.arc.dert.util.ColorMap;
import gov.nasa.arc.dert.util.ColorMapListener;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Provides a color bar widget to display a color map.
 *
 */
public class ColorBar extends JPanel implements ColorMapListener {

	// The palette of colors
	private Palette palette;

	// A pop-up dialog to change the range of the color map
	private ColorMapDialog dialog;

	// The color map
	private ColorMap colorMap;

	/**
	 * Constructor
	 * 
	 * @param cMap
	 * @param vertical
	 */
	public ColorBar(ColorMap cMap, boolean vertical) {
		this.colorMap = cMap;
		cMap.addListener(this);
		setLayout(new BorderLayout());
		palette = new Palette(colorMap, vertical);
		JButton button = new JButton("Color Map");
		button.setToolTipText("edit color map settings");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (dialog == null) {
					double range = colorMap.getBaseMaximum() - colorMap.getBaseMinimum();
					dialog = new ColorMapDialog(null, "Color Map Settings", colorMap.getMinimum() - 10 * range,
						colorMap.getMaximum() + 10 * range, colorMap);
				}
				dialog.open();
				dialog.setRange(colorMap.getMinimum(), colorMap.getMaximum());
			}
		});
		if (vertical) {
			add(button, BorderLayout.NORTH);
			add(palette, BorderLayout.CENTER);
		} else {
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(new JLabel("Layer: " + colorMap.getLayer()));
			panel.add(button);
			add(panel, BorderLayout.NORTH);
			panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(palette);
			add(panel, BorderLayout.CENTER);
		}
	}

	/**
	 * Build the palette with the given color map.
	 * 
	 * @param colorMap
	 */
	public void buildPalette(ColorMap colorMap) {
		this.colorMap = colorMap;
		palette.build(colorMap);
		palette.repaint();
		palette.invalidate();
		doLayout();
	}

	/**
	 * The color map colors changed
	 */
	@Override
	public void mapChanged(ColorMap cMap) {
		palette.build(cMap);
		palette.repaint();
		palette.invalidate();
		doLayout();
	}

	/**
	 * The color map range changed
	 */
	@Override
	public void rangeChanged(ColorMap cMap) {
		palette.build(cMap);
		palette.repaint();
		palette.invalidate();
		doLayout();
	}
}
