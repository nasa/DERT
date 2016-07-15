package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.landmark.Figure;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.DoubleSpinner;
import gov.nasa.arc.dert.ui.DoubleTextField;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

/**
 * Provides controls for setting options for figures.
 *
 */
public class FigurePanel extends MapElementBasePanel {

	// Controls
	private ColorSelectionPanel colorList;
	private DoubleTextField sizeText;
	private DoubleSpinner azSpinner, tiltSpinner;

	// Figure
	private Figure figure;

	// surface normal
	private JCheckBox surfaceButton, autoScaleButton;

	// select shape
	private JComboBox shapeCombo;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public FigurePanel(MapElementsPanel parent) {
		super(parent);
		icon = Figure.icon;
		type = "Figure";
		build(true, true, true);
	}

	@Override
	protected void build(boolean addNotes, boolean addLoc, boolean addCBs) {
		super.build(addNotes, addLoc, addCBs);

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Shape"));
		shapeCombo = new JComboBox(ShapeType.values());
		shapeCombo.setToolTipText("select figure shape");
		shapeCombo.setSelectedIndex(3);
		shapeCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ShapeType shape = (ShapeType) shapeCombo.getSelectedItem();
				figure.setShape(shape);
			}
		});
		panel.add(shapeCombo);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Color"));
		colorList = new ColorSelectionPanel(Figure.defaultColor) {
			@Override
			public void doColor(Color color) {
				figure.setColor(color);
			}
		};
		panel.add(colorList);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Size", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, Figure.defaultSize, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				figure.setSize(value);
			}
		};
		panel.add(sizeText);
		autoScaleButton = new JCheckBox("Autoscale");
		autoScaleButton.setToolTipText("maintain size with change in viewpoint");
		autoScaleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				figure.setAutoScale(autoScaleButton.isSelected());
			}
		});
		panel.add(autoScaleButton);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Azimuth", SwingConstants.RIGHT));
		azSpinner = new DoubleSpinner(0, 0, 359, 1, true) {
			@Override
			public void stateChanged(ChangeEvent event) {
				super.stateChanged(event);
				double azimuth = ((Double) azSpinner.getValue());
				figure.setAzimuth(azimuth);
			}
		};
		azSpinner.setToolTipText("rotate figure around vertical axis");
		panel.add(azSpinner);

		panel.add(new JLabel("        "));

		panel.add(new JLabel("Tilt", SwingConstants.RIGHT));
		tiltSpinner = new DoubleSpinner(0, -90, 90, 1, true) {
			@Override
			public void stateChanged(ChangeEvent event) {
				super.stateChanged(event);
				double tilt = ((Double) tiltSpinner.getValue());
				figure.setTilt(tilt);
			}
		};
		tiltSpinner.setToolTipText("rotate figure around horizontal axis");
		panel.add(tiltSpinner);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		surfaceButton = new JCheckBox("Show Surface Normal");
		surfaceButton.setToolTipText("display the vector for the surface normal");
		surfaceButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				figure.setSurfaceNormalVisible(surfaceButton.isSelected());
			}
		});
		panel.add(surfaceButton);
		contents.add(panel);
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		this.mapElement = mapElement;
		figure = (Figure) mapElement;
		setLocation(locationText, elevLabel, figure.getTranslation());
		pinnedCheckBox.setSelected(figure.isPinned());
		nameLabel.setText(figure.getLabel());
		colorList.setColor(figure.getColor());
		sizeText.setValue(figure.getSize());
		shapeCombo.setSelectedItem(figure.getShapeType());
		autoScaleButton.setSelected(figure.isAutoScale());
		surfaceButton.setSelected(figure.isSurfaceNormalVisible());
		azSpinner.setValue(figure.getAzimuth());
		tiltSpinner.setValue(figure.getTilt());
		labelCheckBox.setSelected(figure.isLabelVisible());
		noteText.setText(figure.getState().getAnnotation());
	}

}
