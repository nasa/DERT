package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.landmark.Figure;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.ui.DoubleSpinner;
import gov.nasa.arc.dert.ui.DoubleTextField;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

/**
 * Provides controls for setting options for figures.
 *
 */
public class FigurePanel extends MapElementBasePanel {

	// Controls
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
	public FigurePanel(MapElement mapElement) {
		super(mapElement);
	}

	@Override
	protected void addFields(ArrayList<Component> compList) {
		super.addFields(compList);

		compList.add(new JLabel("Shape", SwingConstants.RIGHT));
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
		compList.add(shapeCombo);

		compList.add(new JLabel("Size", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, Figure.defaultSize, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				figure.setSize(value);
			}
		};
		compList.add(sizeText);
		compList.add(new JLabel("Autoscale", SwingConstants.RIGHT));
		autoScaleButton = new JCheckBox("enabled");
		autoScaleButton.setToolTipText("maintain size with change in viewpoint");
		autoScaleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				figure.setAutoScale(autoScaleButton.isSelected());
			}
		});
		compList.add(autoScaleButton);

		compList.add(new JLabel("Azimuth", SwingConstants.RIGHT));
		azSpinner = new DoubleSpinner(0, 0, 359, 1, true) {
			@Override
			public void stateChanged(ChangeEvent event) {
				super.stateChanged(event);
				double azimuth = ((Double) azSpinner.getValue());
				figure.setAzimuth(azimuth);
			}
		};
		azSpinner.setToolTipText("rotate figure around vertical axis");
		compList.add(azSpinner);

		compList.add(new JLabel("Tilt", SwingConstants.RIGHT));
		tiltSpinner = new DoubleSpinner(0, -90, 90, 1, true) {
			@Override
			public void stateChanged(ChangeEvent event) {
				super.stateChanged(event);
				double tilt = ((Double) tiltSpinner.getValue());
				figure.setTilt(tilt);
			}
		};
		tiltSpinner.setToolTipText("rotate figure around horizontal axis");
		compList.add(tiltSpinner);

		compList.add(new JLabel("Surface Normal", SwingConstants.RIGHT));
		surfaceButton = new JCheckBox("visible");
		surfaceButton.setToolTipText("display the vector for the surface normal");
		surfaceButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				figure.setSurfaceNormalVisible(surfaceButton.isSelected());
			}
		});
		compList.add(surfaceButton);
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		super.setMapElement(mapElement);
		figure = (Figure) mapElement;
		setLocation(locationText, locLabel, figure.getTranslation());
		sizeText.setValue(figure.getSize());
		shapeCombo.setSelectedItem(figure.getShapeType());
		autoScaleButton.setSelected(figure.isAutoScale());
		surfaceButton.setSelected(figure.isSurfaceNormalVisible());
		azSpinner.setValue(figure.getAzimuth());
		tiltSpinner.setValue(figure.getTilt());
	}

}
