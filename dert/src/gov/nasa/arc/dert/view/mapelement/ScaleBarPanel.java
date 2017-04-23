package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.tool.ScaleBar;
import gov.nasa.arc.dert.ui.DoubleSpinner;
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
 * Provides controls for setting options for scales.
 *
 */
public class ScaleBarPanel extends MapElementBasePanel {

	// Controls
	private DoubleTextField sizeText, radiusText;
	private DoubleSpinner azSpinner, tiltSpinner;
	private JSpinner cellsSpinner;
	private JCheckBox autoLabelButton;
	private JLabel dimensions;

	private ScaleBar scale;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public ScaleBarPanel(MapElement mapElement) {
		super(mapElement);
	}

	@Override
	protected void addFields(ArrayList<Component> compList) {
		super.addFields(compList);

		compList.add(new JLabel("Dimensions", SwingConstants.RIGHT));
		dimensions = new JLabel();
		compList.add(dimensions);

		compList.add(new JLabel("Cell Count", SwingConstants.RIGHT));
		cellsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000, 1));
		compList.add(cellsSpinner);
		cellsSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				int cells = (Integer) cellsSpinner.getValue();
				scale.setCellCount(cells);
				setDimensions();
			}
		});

		compList.add(new JLabel("Cell Size", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, ScaleBar.defaultCellSize, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				scale.setSize(value);
			}
		};
		compList.add(sizeText);

		compList.add(new JLabel("Radius", SwingConstants.RIGHT));
		radiusText = new DoubleTextField(8, ScaleBar.defaultRadius, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				scale.setCellRadius(value);
			}
		};
		compList.add(radiusText);

		compList.add(new JLabel("AutoLabel", SwingConstants.RIGHT));		
		autoLabelButton = new JCheckBox("enabled");
		autoLabelButton.setToolTipText("set label automatically");
		autoLabelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				scale.setAutoLabel(autoLabelButton.isSelected());
			}
		});
		compList.add(autoLabelButton);

		compList.add(new JLabel("Azimuth", SwingConstants.RIGHT));
		azSpinner = new DoubleSpinner(0, 0, 359, 1, true) {
			@Override
			public void stateChanged(ChangeEvent event) {
				super.stateChanged(event);
				double azimuth = ((Double) azSpinner.getValue());
				scale.setAzimuth(azimuth);
			}
		};
		azSpinner.setToolTipText("rotate scale around vertical axis");
		compList.add(azSpinner);

		compList.add(new JLabel("Tilt", SwingConstants.RIGHT));
		tiltSpinner = new DoubleSpinner(0, -90, 90, 1, true) {
			@Override
			public void stateChanged(ChangeEvent event) {
				super.stateChanged(event);
				double tilt = ((Double) tiltSpinner.getValue());
				scale.setTilt(tilt);
			}
		};
		tiltSpinner.setToolTipText("rotate scale around horizontal axis");
		compList.add(tiltSpinner);
	}

	private void setDimensions() {
		double size = scale.getSize();
		int cells = scale.getCellCount();
		String str = "Length=" + formatter.format(cells * size);
		dimensions.setText(str);
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		super.setMapElement(mapElement);
		scale = (ScaleBar) mapElement;
		setLocation(locationText, locLabel, scale.getTranslation());
		sizeText.setValue(scale.getSize());
		radiusText.setValue(scale.getCellRadius());
		autoLabelButton.setSelected(scale.isAutoLabel());
		cellsSpinner.setValue(scale.getCellCount());
		azSpinner.setValue(scale.getAzimuth());
		tiltSpinner.setValue(scale.getTilt());
	}

}
