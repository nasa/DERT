package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.landmark.Placemark;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.ui.FieldPanel;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Provides controls for setting options for placemarks.
 *
 */
public class PlacemarkPanel extends MapElementBasePanel {

	// Controls
	private JComboBox iconCombo;
	private JLabel iconLabel;
	private Icon[] icons;
	private DoubleTextField sizeText;

	// Placemark
	private Placemark placemark;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public PlacemarkPanel() {
		super();
		icon = Placemark.icon;
		type = "Placemark";
		icons = new Icon[Placemark.ICON_NAME.length];
		for (int i = 0; i < icons.length; ++i) {
			icons[i] = Icons.getImageIcon(Placemark.ICON_NAME[i] + "_24.png");
		}
		build(true, true);
	}

	@Override
	protected void build(boolean addNotes, boolean addLocation) {
		super.build(addNotes, addLocation);
		
		ArrayList<Component> compList = new ArrayList<Component>();

		compList.add(new JLabel("Icon", SwingConstants.RIGHT));
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setBorder(BorderFactory.createEmptyBorder());
		iconCombo = new JComboBox(Placemark.ICON_LABEL);
		iconCombo.setToolTipText("select placemark icon");
		iconCombo.setSelectedIndex(0);
		iconCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int index = iconCombo.getSelectedIndex();
				iconLabel.setIcon(icons[index]);
				placemark.setTexture(index);
			}
		});
		panel.add(iconCombo);
		iconLabel = new JLabel();
		panel.add(iconLabel);
		compList.add(panel);

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
		
		contents.add(new FieldPanel(compList));
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		this.mapElement = mapElement;
		placemark = (Placemark) mapElement;
		setLocation(locationText, placemark.getTranslation());
		nameLabel.setText(placemark.getName());
		sizeText.setValue(placemark.getSize());
		iconCombo.setSelectedIndex(placemark.getTextureIndex());
		noteText.setText(placemark.getState().getAnnotation());
	}

}
