package gov.nasa.arc.dert.ui;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.util.ColorMap;
import gov.nasa.arc.dert.view.surfaceandlayers.SurfaceAndLayersView;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

/**
 * Provides a dialog for changing color map options.
 *
 */
public class ColorMapDialog extends AbstractDialog {

	// Range spinners
	private DoubleSpinner minSpinner, maxSpinner;

	// Range limits and defaults
	private double defaultMin, defaultMax, lowerLimit, upperLimit;

	// List of available color maps
	private JComboBox colorMapName;

	// Gradient selection
	private JCheckBox gradient;

	// The selected color map
	private ColorMap colorMap;

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param title
	 * @param lowerLimit
	 * @param upperLimit
	 * @param cMap
	 */
	public ColorMapDialog(Dialog parent, String title, double lowerLimit, double upperLimit, ColorMap cMap) {
		super(parent, title, false, false);
		this.defaultMin = cMap.getMinimum();
		this.defaultMax = cMap.getMaximum();
		this.colorMap = cMap;
		this.upperLimit = upperLimit;
		this.lowerLimit = lowerLimit;
	}

	@Override
	protected void build() {
		super.build();
		cancelButton.setText("Default");
		okButton.setText("Close");
		
		ArrayList<Component> compList = new ArrayList<Component>();
		
		compList.add(new JLabel("Color Map", SwingConstants.RIGHT));
		colorMapName = new JComboBox(ColorMap.getColorMapNames());
		colorMapName.setEditable(false);
		colorMapName.setSelectedItem(colorMap.getName());
		colorMapName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				colorMap.setName((String) colorMapName.getSelectedItem());
				defaultMin = colorMap.getMinimum();
				defaultMax = colorMap.getMaximum();
				setRange(defaultMin, defaultMax);
				SurfaceAndLayersView slv = (SurfaceAndLayersView)ConfigurationManager.getInstance().getCurrentConfiguration().surfAndLayerState.getViewData().getView();
				if ((slv != null) && slv.isVisible())
					slv.updateVisibleLayers();
			}
		});
		compList.add(colorMapName);
		compList.add(new JLabel("Gradient", SwingConstants.RIGHT));
		gradient = new JCheckBox("");
		gradient.setSelected(colorMap.isGradient());
		gradient.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				colorMap.setGradient(gradient.isSelected());
			}
		});
		compList.add(gradient);
		compList.add(new JLabel("Maximum", SwingConstants.RIGHT));
		maxSpinner = new DoubleSpinner(defaultMax, defaultMin, upperLimit, Landscape.defaultCellSize / 100.0, false,
			Landscape.format) {
			@Override
			public void stateChanged(ChangeEvent event) {
				super.stateChanged(event);
				double max = ((Double) maxSpinner.getValue());
				maximumChanged(max);
				minSpinner.setMaximum(max);
			}
		};
		compList.add(maxSpinner);

		compList.add(new JLabel("Minimum", SwingConstants.RIGHT));
		minSpinner = new DoubleSpinner(defaultMin, lowerLimit, defaultMax, Landscape.defaultCellSize / 100.0, false,
			Landscape.format) {
			@Override
			public void stateChanged(ChangeEvent event) {
				super.stateChanged(event);
				double min = ((Double) minSpinner.getValue());
				minimumChanged(min);
				maxSpinner.setMinimum(min);
			}
		};
		compList.add(minSpinner);
		
		contentArea.setLayout(new BorderLayout());
		contentArea.add(new FieldPanel(compList), BorderLayout.CENTER);

		getRootPane().setDefaultButton(null);
	}

	@Override
	protected boolean okPressed() {
		return (true);
	}

	@Override
	protected boolean cancelPressed() {
		minSpinner.setValue(defaultMin);
		maxSpinner.setValue(defaultMax);
		return (false);
	}

	/**
	 * Set the range of the color bar
	 * 
	 * @param min
	 * @param max
	 */
	public void setRange(double min, double max) {
		minSpinner.setValueNoChange(min);
		maxSpinner.setValueNoChange(max);
	}

	/**
	 * Notify the color map that the minimum range value changed.
	 * 
	 * @param value
	 */
	public void minimumChanged(double value) {
		colorMap.setRange(value, colorMap.getMaximum());
	}

	/**
	 * Notify the color map that the maximum range value changed.
	 * 
	 * @param value
	 */
	public void maximumChanged(double value) {
		colorMap.setRange(colorMap.getMinimum(), value);
	}
}
