package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.landmark.Placemark;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.ui.IconComboBox;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Provides controls for setting options for placemarks.
 *
 */
public class PlacemarkPanel extends MapElementBasePanel {

	// Controls
	private IconComboBox iconCombo;
	private DoubleTextField sizeText;

	// Placemark
	private Placemark placemark;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public PlacemarkPanel(MapElement mapElement) {
		super(mapElement);
	}

	@Override
	protected void addFields(ArrayList<Component> compList) {
		super.addFields(compList);

		compList.add(new JLabel("Icon", SwingConstants.RIGHT));
		
		iconCombo = new IconComboBox(Placemark.ICON_LABEL, Placemark.icons);
		iconCombo.setToolTipText("select placemark icon");
		iconCombo.setSelectedIndex(0);
		iconCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int index = iconCombo.getSelectedIndex();
				placemark.setTexture(index);
			}
		});
		compList.add(iconCombo);

		compList.add(new JLabel("Size", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, Placemark.defaultSize, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				placemark.setSize(value);
			}
		};
		compList.add(sizeText);
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		super.setMapElement(mapElement);
		placemark = (Placemark) mapElement;
		setLocation(locationText, locLabel, placemark.getTranslation());
		sizeText.setValue(placemark.getSize());
		iconCombo.setSelectedIndex(placemark.getTextureIndex());
		noteText.setText(placemark.getState().getAnnotation());
	}

}
