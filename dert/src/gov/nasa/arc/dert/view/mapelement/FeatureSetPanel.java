package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.featureset.Feature;
import gov.nasa.arc.dert.scene.featureset.FeatureSet;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.FeatureSetState;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.GroupPanel;
import gov.nasa.arc.dert.util.FileHelper;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Provides controls for setting options for FeatureSets.
 *
 */
public class FeatureSetPanel extends MapElementBasePanel {

	// Controls
	private ColorSelectionPanel colorList;
	private JTextField fileText;
	private JTextArea propText;
	private JButton browseButton;
	private Color currentColor = FeatureSet.defaultColor;

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
		build(true, false, false);
	}

	@Override
	protected void build(boolean addNotes, boolean addLoc, boolean addCBs) {
		super.build(addNotes, addLoc, addCBs);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("File"), BorderLayout.WEST);
		fileText = new JTextField();
		fileText.setToolTipText("path to GeoJSON file");
		fileText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				loadFile();
			}
		});
		panel.add(fileText, BorderLayout.CENTER);
		panel.setMaximumSize(new Dimension(1000, -1));
		browseButton = new JButton("Browse");
		browseButton.setToolTipText("browse to GeoJSON file");
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setFile();
			}
		});
		panel.add(browseButton, BorderLayout.EAST);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Color"));
		colorList = new ColorSelectionPanel(FeatureSet.defaultColor) {
			@Override
			public void doColor(Color color) {
				currentColor = color;
				if (featureSet != null) {
					featureSet.setColor(currentColor);
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
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		this.mapElement = mapElement;
		featureSet = null;
		if (mapElement == null) {
			nameLabel.setText("");
			colorList.setEnabled(true);
			colorList.setColor(FeatureSet.defaultColor);
			fileText.setEnabled(true);
			browseButton.setEnabled(true);
			noteText.setText("");
			propText.setText("");
		}
		else if (mapElement instanceof FeatureSet) {
			featureSet = (FeatureSet) mapElement;
			propText.setText("");
			colorList.setEnabled(true);
			nameLabel.setText(featureSet.getName());
			colorList.setColor(featureSet.getColor());
			fileText.setText(featureSet.getFilePath());
			fileText.setEnabled(false);
			browseButton.setEnabled(false);
			noteText.setText(featureSet.getState().getAnnotation());
			currentColor = featureSet.getColor();
		}
		else if (mapElement instanceof Feature) {
			fileText.setEnabled(false);
			browseButton.setEnabled(false);
			colorList.setEnabled(false);
			Feature feature = (Feature)mapElement;
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

	protected void setFile() {
		String path = FileHelper.getFilePathForOpen("Select GeoJSON File", "GeoJSON Files", "json");
		if (path != null) {
			File file = new File(path);
			fileText.setText(file.getAbsolutePath());
			loadFile();
		}
	}

	private void loadFile() {

		// get the file path
		String filePath = fileText.getText().trim();
		if (filePath.isEmpty()) {
			return;
		}

		String label = StringUtil.getLabelFromFilePath(filePath);
		nameLabel.setText(label);
		FeatureSetState lsState = new FeatureSetState(label, filePath, currentColor, noteText.getText());
		ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(lsState);
	}

}
