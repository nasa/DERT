package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.featureset.FeatureSet;
import gov.nasa.arc.dert.ui.DoubleTextField;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Provides controls for setting options for FeatureSets.
 *
 */
public class FeatureSetPanel extends MapElementBasePanel {

	// Controls
	private JTextField fileText;
	private DoubleTextField lineWidthText;
	private DoubleTextField sizeText;

	// FeatureSet
	private FeatureSet featureSet;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public FeatureSetPanel(MapElement mapElement) {
		super(mapElement);
		featureSet = (FeatureSet)mapElement;
	}

	@Override
	protected void addFields(ArrayList<Component> compList) {
		
		compList.add(new JLabel("File", SwingConstants.RIGHT));
		fileText = new JTextField();
		fileText.setEditable(false);
		fileText.setBorder(BorderFactory.createEmptyBorder());
		fileText.setToolTipText("path to GeoJSON file");
		compList.add(fileText);
		
		compList.add(new JLabel("Line Width", SwingConstants.RIGHT));
		lineWidthText = new DoubleTextField(8, FeatureSet.defaultLineWidth, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				featureSet.setLineWidth((float) value);
			}
		};
		compList.add(lineWidthText);

		compList.add(new JLabel("Point Size", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, FeatureSet.defaultSize, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				featureSet.setPointSize((float)value);
			}
		};
		compList.add(sizeText);
		
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		super.setMapElement(mapElement);
		if (mapElement instanceof FeatureSet) {
			featureSet = (FeatureSet) mapElement;
			fileText.setText("File: "+featureSet.getFilePath());
			noteText.setText(featureSet.getState().getAnnotation());
			lineWidthText.setValue(featureSet.getLineWidth());
			lineWidthText.setEnabled(true);
			sizeText.setValue(featureSet.getSize());
			sizeText.setEnabled(true);
		}
	}
}
