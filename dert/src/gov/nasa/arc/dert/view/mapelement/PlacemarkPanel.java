package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.landmark.Placemark;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.DoubleTextField;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
	private ColorSelectionPanel colorList;
	private DoubleTextField sizeText;

	// Placemark
	private Placemark placemark;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public PlacemarkPanel(MapElementsPanel parent) {
		super(parent);
		icon = Placemark.icon;
		type = "Placemark";
		icons = new Icon[Placemark.ICON_NAME.length];
		for (int i = 0; i < icons.length; ++i) {
			icons[i] = Icons.getImageIcon(Placemark.ICON_NAME[i] + "_24.png");
		}
		build(true, true, true);
	}

	@Override
	protected void build(boolean addNotes, boolean addLocation, boolean addCBs) {
		super.build(addNotes, addLocation, addCBs);

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Icon"));
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
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Color"));
		colorList = new ColorSelectionPanel(Placemark.defaultColor) {
			@Override
			public void doColor(Color color) {
				placemark.setColor(color);
			}
		};
		panel.add(colorList);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Size", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, Placemark.defaultSize, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				placemark.setSize(value);
			}
		};
		panel.add(sizeText);
		contents.add(panel);
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		this.mapElement = mapElement;
		placemark = (Placemark) mapElement;
		setLocation(locationText, elevLabel, placemark.getTranslation());
		pinnedCheckBox.setSelected(placemark.isPinned());
		nameLabel.setText(placemark.getName());
		colorList.setColor(placemark.getColor());
		sizeText.setValue(placemark.getSize());
		iconCombo.setSelectedIndex(placemark.getTextureIndex());
		labelCheckBox.setSelected(placemark.isLabelVisible());
		noteText.setText(placemark.getState().getAnnotation());
	}

}
