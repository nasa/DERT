package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.tool.CartesianGrid;
import gov.nasa.arc.dert.scene.tool.Grid;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.DoubleTextField;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
 * Provides controls for setting options for Cartesian grids.
 *
 */
public class CartesianGridPanel extends MapElementBasePanel {

	// Controls
	private ColorSelectionPanel colorList;
	private DoubleTextField sizeText;
	private JCheckBox actualCoordButton;
	private JSpinner rowsSpinner;
	private JSpinner columnsSpinner;
	private JLabel dimensions;
	private DoubleTextField lineWidthText;

	// Grid
	private CartesianGrid grid;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public CartesianGridPanel(MapElementsPanel parent) {
		super(parent);
		icon = CartesianGrid.icon;
		type = "Cartesian Grid";
		build(true, true, true);
	}

	@Override
	protected void build(boolean addNotes, boolean addLoc, boolean addCBs) {
		super.build(addNotes, addLoc, addCBs);

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		dimensions = new JLabel();
		panel.add(dimensions);
		contents.add(panel);

		panel = new JPanel(new GridLayout(2, 4));
		panel.add(new JLabel("Columns", SwingConstants.RIGHT));
		columnsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000, 1));
		panel.add(columnsSpinner);
		columnsSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				int columns = (Integer) columnsSpinner.getValue();
				grid.setColumns(columns);
				setDimensions();
			}
		});
		panel.add(new JLabel("Rows", SwingConstants.RIGHT));
		rowsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000, 1));
		panel.add(rowsSpinner);
		rowsSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				int rows = (Integer) rowsSpinner.getValue();
				grid.setRows(rows);
				setDimensions();
			}
		});

		panel.add(new JLabel("Cell Size", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, Grid.defaultCellSize, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				grid.setSize(value);
				setDimensions();
			}
		};
		panel.add(sizeText);
		panel.add(new JLabel(" "));
		panel.add(new JLabel(" "));
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Color", SwingConstants.RIGHT));
		colorList = new ColorSelectionPanel(CartesianGrid.defaultColor) {
			@Override
			public void doColor(Color color) {
				grid.setColor(color);
			}
		};
		panel.add(colorList);

		panel.add(new JLabel("Line Width", SwingConstants.RIGHT));
		lineWidthText = new DoubleTextField(8, CartesianGrid.defaultLineWidth, true, Landscape.format) {
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
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		this.mapElement = mapElement;
		grid = (CartesianGrid) mapElement;
		nameLabel.setText(grid.getName());
		pinnedCheckBox.setSelected(grid.isPinned());
		colorList.setColor(grid.getColor());
		sizeText.setValue(grid.getSize());
		lineWidthText.setValue(grid.getLineWidth());
		labelCheckBox.setSelected(grid.isLabelVisible());
		setLocation(locationText, elevLabel, grid.getTranslation());
		columnsSpinner.setValue(grid.getColumns());
		rowsSpinner.setValue(grid.getRows());
		colorList.setColor(grid.getColor());
		labelCheckBox.setSelected(grid.isLabelVisible());
		actualCoordButton.setSelected(grid.isActualCoordinates());
		noteText.setText(grid.getState().getAnnotation());
	}

	private void setDimensions() {
		double size = grid.getSize();
		int rows = grid.getRows();
		int cols = grid.getColumns();
		String str = "Width=" + formatter.format(cols * size) + ", Height=" + formatter.format(rows * size);
		dimensions.setText(str);
	}

}
