package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.scene.featureset.FeatureSet;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.FeatureSetState;
import gov.nasa.arc.dert.ui.AbstractDialog;
import gov.nasa.arc.dert.util.FileHelper;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FeatureSetDialog
	extends AbstractDialog {
	
	private JTextField fileText;
	private JButton browseButton;
	private JCheckBox isProjected;
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
		
		contentArea.setLayout(new GridLayout(3, 1));

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("File"), BorderLayout.WEST);
		fileText = new JTextField();
		fileText.setToolTipText("path to GeoJSON file");
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
		contentArea.add(panel);
		
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		isProjected = new JCheckBox("Is Projected");
		panel.add(isProjected);
		contentArea.add(panel);
		
		panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("Property to use as label: "), BorderLayout.WEST);
		labelText = new JTextField();
		panel.add(labelText, BorderLayout.CENTER);
		contentArea.add(panel);
		
		width = 400;
		height = 200;
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
		FeatureSetState lsState = new FeatureSetState(label, filePath, FeatureSet.defaultColor, null, isProjected.isSelected(), labelProp);
		if (ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(lsState, messageText) == null)
			return(false);
		return(true);
	}

}
