package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.ImageBoardState;
import gov.nasa.arc.dert.ui.AbstractDialog;
import gov.nasa.arc.dert.ui.GBCHelper;
import gov.nasa.arc.dert.util.FileHelper;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.ardor3d.math.type.ReadOnlyVector3;

public class ImageBoardDialog
	extends AbstractDialog {
	
	private JTextField fileText;
	private JButton browseButton;
	private ReadOnlyVector3 position;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public ImageBoardDialog(Dialog parent, ReadOnlyVector3 position) {
		super(parent, "New Billboard", true, true);
		this.position = position;
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public ImageBoardDialog(Frame parent, ReadOnlyVector3 position) {
		super(parent, "New Billboard", true, true);
		this.position = position;
	}
	

	@Override
	protected void build() {
		super.build();
		
		contentArea.setMaximumSize(new Dimension(1000, -1));
		contentArea.setLayout(new GridBagLayout());
		
		JLabel label = new JLabel("File");
		contentArea.add(label, GBCHelper.getGBC(0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0));
		fileText = new JTextField();
		fileText.setToolTipText("path to Billboard image file");
		contentArea.add(fileText, GBCHelper.getGBC(1, 0, 3, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0));
		
		browseButton = new JButton("Browse");
		browseButton.setToolTipText("browse to Billboard image file");
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setFile();
			}
		});
		contentArea.add(browseButton, GBCHelper.getGBC(4, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 0));
		
		width = 500;
		height = 150;
	}

	@Override
	public boolean okPressed() {
		return (loadFile());
	}

	protected void setFile() {
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Image File", "jpg", "jpeg", "png", "gif");
		String path = FileHelper.getFilePathForOpen("Image File Selection", filter);
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

		ImageBoardState ibState = new ImageBoardState(position, filePath);
		if (ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(ibState, messageText) == null)
			return(false);
		return(true);
	}

}
