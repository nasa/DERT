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
		panel.add(new JLabel("Distance", SwingConstants.RIGHT));
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
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		actualCoordButton = new JCheckBox("Show World Coordinates in Label");
		actualCoordButton.setSelected(true);
		actualCoordButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				grid.setActualCoordinates(actualCoordButton.isSelected());
			}
		});
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		compassRoseCheckBox = new JCheckBox("Compass Rose");
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
