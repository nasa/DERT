package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.tool.RadialGrid;
import gov.nasa.arc.dert.ui.DoubleTextField;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Provides controls for setting options for radial grid tools.
 *
 */
public class RadialGridPanel extends MapElementBasePanel {

	// Controls
	private DoubleTextField sizeText;
	private JCheckBox compassRoseCheckBox;
	private JCheckBox actualCoordButton;
	private JSpinner ringsSpinner;
	private JLabel radius;
	private DoubleTextField lineWidthText;

	// The grid
	private RadialGrid grid;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public RadialGridPanel(MapElement mapElement) {
		super(mapElement);
	}

	@Override
	protected void addFields(ArrayList<Component> compList) {
		super.addFields(compList);

		compList.add(new JLabel("Radius", SwingConstants.RIGHT));
		radius = new JLabel();
		compList.add(radius);

		compList.add(new JLabel("Number of Rings", SwingConstants.RIGHT));
		ringsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000, 1));
		compList.add(ringsSpinner);
		ringsSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				setRadius();
				int rings = (Integer) ringsSpinner.getValue();
				grid.setRings(rings);
			}
		});
		
		compList.add(new JLabel("Gap Between Rings", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, RadialGrid.defaultCellSize, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				grid.setSize(value);
				setRadius();
			}
		};
		sizeText.setToolTipText("distance to next ring");
		compList.add(sizeText);
		
		compList.add(new JLabel("Line Width", SwingConstants.RIGHT));
		lineWidthText = new DoubleTextField(8, RadialGrid.defaultLineWidth, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				grid.setLineWidth((float) value);
			}
		};
		compList.add(lineWidthText);

		compList.add(new JLabel("Absolute Coordinates", SwingConstants.RIGHT));
		actualCoordButton = new JCheckBox("enabled");
		actualCoordButton.setToolTipText("label shows absolute landscape coordinates or coordinates relative to grid origin");
		actualCoordButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				grid.setActualCoordinates(actualCoordButton.isSelected());
			}
		});
		compList.add(actualCoordButton);

		compList.add(new JLabel("Compass Rose", SwingConstants.RIGHT));
		compassRoseCheckBox = new JCheckBox("enabled");
		compassRoseCheckBox.setToolTipText("label with N, S, E, and W");
		compassRoseCheckBox.setSelected(false);
		compList.add(compassRoseCheckBox);
		compassRoseCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				grid.setCompassRose(compassRoseCheckBox.isSelected());
			}
		});
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		super.setMapElement(mapElement);
		grid = (RadialGrid) mapElement;
		sizeText.setValue(grid.getSize());
		lineWidthText.setValue(grid.getLineWidth());

		setLocation(locationText, grid.getTranslation());
		ringsSpinner.setValue(grid.getRings());
		compassRoseCheckBox.setSelected(grid.isCompassRose());
		actualCoordButton.setSelected(grid.isActualCoordinates());
		noteText.setText(grid.getState().getAnnotation());
	}

	private void setRadius() {
		String str = sizeText.getText();
		double size = Double.parseDouble(str);
		str = formatter.format((Integer) ringsSpinner.getValue() * size);
		radius.setText(str);
	}

}
