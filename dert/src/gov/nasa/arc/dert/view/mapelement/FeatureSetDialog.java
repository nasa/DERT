package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.scene.featureset.FeatureSet;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.FeatureSetState;
import gov.nasa.arc.dert.ui.AbstractDialog;
import gov.nasa.arc.dert.ui.FieldPanel;
import gov.nasa.arc.dert.ui.FileInputField;
import gov.nasa.arc.dert.util.FileHelper;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class FeatureSetDialog
	extends AbstractDialog {
	
	private JCheckBox ground;
	private JTextField labelText;
	private FileInputField fif;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public FeatureSetDialog(Dialog parent) {
		super(parent, "New Feature Set", true, true);
	}
	

	@Override
	protected void build() {
		super.build();
		
		ArrayList<Component> compList = new ArrayList<Component>();
		compList.add(new JLabel("File", SwingConstants.RIGHT));
		fif = new FileInputField("", "enter path to GeoJSON file") {
			@Override
			public void setFile() {
				String path = FileHelper.getFilePathForOpen("Select GeoJSON File", "GeoJSON Files", "json");
				if (path != null) {
					File file = new File(path);
					fileText.setText(file.getAbsolutePath());
				}
			}
		};
		compList.add(fif);
		
		ground = new JCheckBox();
		compList.add(ground);
		compList.add(new JLabel("Use landscape elevation for Z coordinate.", SwingConstants.LEFT));
		
		JLabel label = new JLabel("Label Property", SwingConstants.RIGHT);
		label.setToolTipText("use this property for Point labels");
		compList.add(label);
		labelText = new JTextField();
		labelText.setToolTipText("use this property for Point labels");
		compList.add(labelText);
		
		contentArea.setLayout(new BorderLayout());
		contentArea.add(new FieldPanel(compList), BorderLayout.CENTER);
		
		width = 400;
		height = 220;
		
		messageText.setText("Enter the path to a GeoJSON file.");
	}

	@Override
	public boolean okPressed() {
		return (loadFile());
	}

	private boolean loadFile() {

		// get the file path
		String filePath = fif.getFilePath();
		if (filePath.isEmpty()) {
			messageText.setText("Invalid file path.");
			return(false);
		}

		String labelProp = labelText.getText();
		if (labelProp.isEmpty())
			labelProp = null;
		String label = StringUtil.getLabelFromFilePath(filePath);
		FeatureSetState lsState = new FeatureSetState(label, filePath, FeatureSet.defaultColor, null, ground.isSelected(), labelProp);
		if (ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(lsState, messageText) == null)
			return(false);
		return(true);
	}

}
