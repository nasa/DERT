package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.scene.featureset.FeatureSet;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.FeatureSetState;
import gov.nasa.arc.dert.ui.AbstractDialog;
import gov.nasa.arc.dert.ui.GBCHelper;
import gov.nasa.arc.dert.util.FileHelper;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class FeatureSetDialog
	extends AbstractDialog {
	
	private JTextField fileText;
	private JButton browseButton;
	private JCheckBox isProjected;
	private JCheckBox ground;
	private JTextField labelText;

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
		
		contentArea.setMaximumSize(new Dimension(1000, -1));
		contentArea.setLayout(new GridBagLayout());
		
		JLabel label = new JLabel("File");
		contentArea.add(label, GBCHelper.getGBC(0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0));
		fileText = new JTextField();
		fileText.setToolTipText("path to GeoJSON file");
		contentArea.add(fileText, GBCHelper.getGBC(1, 0, 3, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0));
		
		browseButton = new JButton("Browse");
		browseButton.setToolTipText("browse to GeoJSON file");
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setFile();
			}
		});
		contentArea.add(browseButton, GBCHelper.getGBC(4, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 0));
		
		label = new JLabel("Is projected but contains no CRS: ");
		contentArea.add(label, GBCHelper.getGBC(0, 1, 3, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0));
		isProjected = new JCheckBox();
		contentArea.add(isProjected, GBCHelper.getGBC(3, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0));
		
		label = new JLabel("Use elevation for Z coordinate: ");
		contentArea.add(label, GBCHelper.getGBC(0, 2, 3, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0));
		ground = new JCheckBox();
		contentArea.add(ground, GBCHelper.getGBC(3, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0));
		
		label = new JLabel("Property to use as label: ");
		contentArea.add(label, GBCHelper.getGBC(0, 3, 2, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0));
		labelText = new JTextField();
		contentArea.add(labelText, GBCHelper.getGBC(2, 3, 3, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0));
		
		width = 400;
		height = 220;
	}

	@Override
	public boolean okPressed() {
		return (loadFile());
	}

	protected void setFile() {
		String path = FileHelper.getFilePathForOpen("Select GeoJSON File", "GeoJSON Files", "json");
		if (path != null) {
			File file = new File(path);
			fileText.setText(file.getAbsolutePath());
		}
	}

	private boolean loadFile() {

		// get the file path
		String filePath = fileText.getText().trim();
		if (filePath.isEmpty()) {
			messageText.setText("Invalid file path.");
			return(false);
		}

		String labelProp = labelText.getText();
		if (labelProp.isEmpty())
			labelProp = null;
		String label = StringUtil.getLabelFromFilePath(filePath);
		FeatureSetState lsState = new FeatureSetState(label, filePath, FeatureSet.defaultColor, null, isProjected.isSelected(), ground.isSelected(), labelProp);
		if (ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(lsState, messageText) == null)
			return(false);
		return(true);
	}

}
