package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.landmark.Figure;
import gov.nasa.arc.dert.scene.tool.Scale;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.DoubleSpinner;
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
 * Provides controls for setting options for scales.
 *
 */
public class ScalePanel extends MapElementBasePanel {

	// Controls
	private ColorSelectionPanel colorList;
	private DoubleTextField sizeText, radiusText;
	private DoubleSpinner azSpinner, tiltSpinner;
	private JSpinner cellsSpinner;
	private JCheckBox autoLabelButton;
	private JLabel dimensions;

	private Scale scale;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public ScalePanel(MapElementsPanel parent) {
		super(parent);
		icon = Figure.icon;
		type = "Figure";
		build(true, true, true);
	}

	@Override
	protected void build(boolean addNotes, boolean addLoc, boolean addCBs) {
		super.build(addNotes, addLoc, addCBs);

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		dimensions = new JLabel();
		panel.add(dimensions);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Cell Count", SwingConstants.RIGHT));
		cellsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000000, 1));
		panel.add(cellsSpinner);
		cellsSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				int cells = (Integer) cellsSpinner.getValue();
				scale.setCellCount(cells);
				setDimensions();
			}
		});

		panel.add(new JLabel("    Cell Size", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, Scale.defaultCellSize, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				scale.setSize(value);
			}
		};
		panel.add(sizeText);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Radius", SwingConstants.RIGHT));
		radiusText = new DoubleTextField(8, Scale.defaultRadius, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				scale.setCellRadius(value);
			}
		};
		panel.add(radiusText);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Color"));
		colorList = new ColorSelectionPanel(Scale.defaultColor) {
			@Override
			public void doColor(Color color) {
				scale.setColor(color);
			}
		};
		panel.add(colorList);

		panel.add(new JLabel("        "));
		
		autoLabelButton = new JCheckBox("AutoLabel");
		autoLabelButton.setToolTipText("set label automatically");
		autoLabelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				scale.setAutoLabel(autoLabelButton.isSelected());
			}
		});
		panel.add(autoLabelButton);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Azimuth", SwingConstants.RIGHT));
		azSpinner = new DoubleSpinner(0, 0, 359, 1, true) {
			@Override
			public void stateChanged(ChangeEvent event) {
				super.stateChanged(event);
				double azimuth = ((Double) azSpinner.getValue());
				scale.setAzimuth(azimuth);
			}
		};
		azSpinner.setToolTipText("rotate scale around vertical axis");
		panel.add(azSpinner);

		panel.add(new JLabel("        "));

		panel.add(new JLabel("Tilt", SwingConstants.RIGHT));
		tiltSpinner = new DoubleSpinner(0, -90, 90, 1, true) {
			@Override
			public void stateChanged(ChangeEvent event) {
				super.stateChanged(event);
				double tilt = ((Double) tiltSpinner.getValue());
				scale.setTilt(tilt);
			}
		};
		tiltSpinner.setToolTipText("rotate scale around horizontal axis");
		panel.add(tiltSpinner);
		contents.add(panel);
	}

	private void setDimensions() {
		double size = scale.getSize();
		int cells = scale.getCellCount();
		String str = "Length=" + formatter.format(cells * size);
		dimensions.setText(str);
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		this.mapElement = mapElement;
		scale = (Scale) mapElement;
		setLocation(locationText, scale.getTranslation());
		pinnedCheckBox.setSelected(scale.isPinned());
		nameLabel.setText(scale.getLabel());
		colorList.setColor(scale.getColor());
		sizeText.setValue(scale.getSize());
		radiusText.setValue(scale.getCellRadius());
		autoLabelButton.setSelected(scale.isAutoLabel());
		cellsSpinner.setValue(scale.getCellCount());
		azSpinner.setValue(scale.getAzimuth());
		tiltSpinner.setValue(scale.getTilt());
		labelCheckBox.setSelected(scale.isLabelVisible());
		noteText.setText(scale.getState().getAnnotation());
	}

}
