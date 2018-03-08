package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.tool.CartesianGrid;
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
 * Provides controls for setting options for Cartesian grids.
 *
 */
public class CartesianGridPanel extends MapElementBasePanel {

	// Controls
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
	public CartesianGridPanel(MapElement mapElement) {
		super(mapElement);
	}

	@Override
	protected void addFields(ArrayList<Component> compList) {
		super.addFields(compList);
		
		compList.add(new JLabel("Dimensions", SwingConstants.RIGHT));
		dimensions = new JLabel(" ");
		compList.add(dimensions);
		
		compList.add(new JLabel("Columns", SwingConstants.RIGHT));
		columnsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000, 1));
		compList.add(columnsSpinner);
		columnsSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				int columns = (Integer) columnsSpinner.getValue();
				grid.setColumns(columns);
				setDimensions();
			}
		});
		
		compList.add(new JLabel("Rows", SwingConstants.RIGHT));
		rowsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000, 1));
		compList.add(rowsSpinner);
		rowsSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				int rows = (Integer) rowsSpinner.getValue();
				grid.setRows(rows);
				setDimensions();
			}
		});

		compList.add(new JLabel("Cell Size", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, Landscape.defaultCellSize, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				grid.setSize(value);
				setDimensions();
			}
		};
		compList.add(sizeText);

		compList.add(new JLabel("Line Width", SwingConstants.RIGHT));
		lineWidthText = new DoubleTextField(8, CartesianGrid.defaultLineWidth, true, Landscape.format) {
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
		actualCoordButton.setToolTipText("label shows absolute landscape coordinates instead of coordinates relative to grid origin");
		actualCoordButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				grid.setActualCoordinates(actualCoordButton.isSelected());
			}
		});
		compList.add(actualCoordButton);
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		super.setMapElement(mapElement);
		grid = (CartesianGrid) mapElement;
		sizeText.setValue(grid.getSize());
		lineWidthText.setValue(grid.getLineWidth());
		setLocation(locationText, locLabel, grid.getTranslation());
		columnsSpinner.setValue(grid.getColumns());
		rowsSpinner.setValue(grid.getRows());
		actualCoordButton.setSelected(grid.isActualCoordinates());
	}

	private void setDimensions() {
		double size = grid.getSize();
		int rows = grid.getRows();
		int cols = grid.getColumns();
		dimensions.setText("Width="+formatter.format(cols * size)+", Height="+formatter.format(rows * size));
	}

}
