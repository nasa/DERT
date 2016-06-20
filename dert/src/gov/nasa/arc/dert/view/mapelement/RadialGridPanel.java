package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.tool.Grid;
import gov.nasa.arc.dert.scene.tool.RadialGrid;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.DoubleTextField;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
	private ColorSelectionPanel colorList;
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
	public RadialGridPanel(MapElementsPanel parent) {
		super(parent);
		icon = RadialGrid.icon;
		type = "Radial Grid";
		build(true, true, true);
	}

	@Override
	protected void build(boolean addNotes, boolean addLoc, boolean addCBs) {
		super.build(addNotes, addLoc, addCBs);

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		radius = new JLabel();
		panel.add(radius);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Rings"));
		ringsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000, 1));
		panel.add(ringsSpinner);
		ringsSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				setRadius();
				int rings = (Integer) ringsSpinner.getValue();
				grid.setRings(rings);
			}
		});
		panel.add(new JLabel("Gap Between", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, Grid.defaultCellSize, true, Landscape.format) {
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
		panel.add(sizeText);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Color"));
		colorList = new ColorSelectionPanel(RadialGrid.defaultColor) {
			@Override
			public void doColor(Color color) {
				grid.setColor(color);
			}
		};
		panel.add(colorList);
		panel.add(new JLabel("Line Width", SwingConstants.RIGHT));
		lineWidthText = new DoubleTextField(8, RadialGrid.defaultLineWidth, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				grid.setLineWidth((float) value);
			}
		};
		panel.add(lineWidthText);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		actualCoordButton = new JCheckBox("Absolute Landscape Coordinates");
		actualCoordButton.setToolTipText("label shows absolute landscape coordinates or coordinates relative to grid origin");
		actualCoordButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				grid.setActualCoordinates(actualCoordButton.isSelected());
			}
		});
		panel.add(actualCoordButton);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		compassRoseCheckBox = new JCheckBox("Compass Rose");
		compassRoseCheckBox.setToolTipText("label with N, S, E, and W");
		compassRoseCheckBox.setSelected(false);
		panel.add(compassRoseCheckBox);
		compassRoseCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				grid.setCompassRose(compassRoseCheckBox.isSelected());
			}
		});

		contents.add(panel);
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		this.mapElement = mapElement;
		grid = (RadialGrid) mapElement;
		nameLabel.setText(grid.getName());
		pinnedCheckBox.setSelected(grid.isPinned());
		colorList.setColor(grid.getColor());
		sizeText.setValue(grid.getSize());
		lineWidthText.setValue(grid.getLineWidth());

		setLocation(locationText, elevLabel, grid.getTranslation());
		ringsSpinner.setValue(grid.getRings());
		colorList.setColor(grid.getColor());
		labelCheckBox.setSelected(grid.isLabelVisible());
		compassRoseCheckBox.setSelected(grid.isCompassRose());
		actualCoordButton.setSelected(grid.isActualCoordinates());
		noteText.setText(grid.getState().getAnnotation());
	}

	private void setRadius() {
		String str = sizeText.getText();
		double size = Double.parseDouble(str);
		str = "Radius: " + formatter.format((Integer) ringsSpinner.getValue() * size);
		radius.setText(str);
	}

}
