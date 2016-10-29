package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.featureset.Feature;
import gov.nasa.arc.dert.scene.featureset.FeatureSet;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.GroupPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Provides controls for setting options for FeatureSets.
 *
 */
public class FeatureSetPanel extends MapElementBasePanel {

	// Controls
	private ColorSelectionPanel colorList;
	private JLabel fileLabel;
	private JTextArea propText;

	// FeatureSet
	private FeatureSet featureSet;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public FeatureSetPanel(MapElementsPanel parent) {
		super(parent);
		icon = FeatureSet.icon;
		type = "FeatureSet";
		build(true, false, true);
	}

	@Override
	protected void build(boolean addNotes, boolean addLoc, boolean addCBs) {
		super.build(addNotes, addLoc, addCBs);

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fileLabel = new JLabel("File: ");
		fileLabel.setToolTipText("path to GeoJSON file");
		panel.add(fileLabel);
		panel.setMaximumSize(new Dimension(1000, -1));
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Color"));
		colorList = new ColorSelectionPanel(FeatureSet.defaultColor) {
			@Override
			public void doColor(Color color) {
				if (featureSet != null) {
					featureSet.setColor(color);
				}
			}
		};
		panel.add(colorList);
		contents.add(panel);
		
		GroupPanel groupPanel = new GroupPanel("Properties");
		groupPanel.setLayout(new BorderLayout());
		propText = new JTextArea();
		propText.setEditable(false);
		propText.setRows(4);
		groupPanel.add(new JScrollPane(propText), BorderLayout.CENTER);
		contents.add(groupPanel);
		
		pinnedCheckBox.setSelected(true);
		pinnedCheckBox.setEnabled(false);
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		this.mapElement = mapElement;
		featureSet = null;
		if (mapElement instanceof FeatureSet) {
			featureSet = (FeatureSet) mapElement;
			propText.setText("");
			colorList.setEnabled(true);
			nameLabel.setText(featureSet.getName());
			colorList.setColor(featureSet.getColor());
			fileLabel.setText("File: "+featureSet.getFilePath());
			noteText.setText(featureSet.getState().getAnnotation());
			labelCheckBox.setSelected(featureSet.isLabelVisible());
			labelCheckBox.setEnabled(true);
		}
		else if (mapElement instanceof Feature) {
			colorList.setEnabled(false);
			Feature feature = (Feature)mapElement;
			FeatureSet fs = (FeatureSet)feature.getParent();
			if (fs != null)
				fileLabel.setText("File: "+fs.getFilePath());
			labelCheckBox.setEnabled(false);
			nameLabel.setText(feature.getName());
			HashMap<String,Object> properties = feature.getProperties();
			Object[] key = properties.keySet().toArray();
			String str = "";
			for (int i=0; i<key.length; ++i) {
				str += key[i]+" = "+properties.get((String)key[i])+"\n";
			}
			propText.setText(str);
			propText.setCaretPosition(0);
		}
	}
}
