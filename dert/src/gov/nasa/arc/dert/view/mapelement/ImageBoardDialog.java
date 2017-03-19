package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.ImageBoardState;
import gov.nasa.arc.dert.ui.AbstractDialog;
import gov.nasa.arc.dert.ui.FieldPanel;
import gov.nasa.arc.dert.ui.FileInputField;
import gov.nasa.arc.dert.util.FileHelper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.ardor3d.math.type.ReadOnlyVector3;

public class ImageBoardDialog
	extends AbstractDialog {
	
	private ReadOnlyVector3 position;
	private FileInputField fif;

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
		
		ArrayList<Component> compList = new ArrayList<Component>();
		compList.add(new JLabel("File", SwingConstants.RIGHT));
		fif = new FileInputField("", "enter path to Billboard image file") {
			@Override
			public void setFile() {
				FileNameExtensionFilter filter = new FileNameExtensionFilter("JPEG, PNG, or GIF", "jpg", "jpeg", "png", "gif");
				String path = FileHelper.getFilePathForOpen("Image File Selection", filter);
				if (path != null) {
					File file = new File(path);
					fileText.setText(file.getAbsolutePath());
				}
			}
			
		};
		compList.add(fif);
		contentArea.setLayout(new BorderLayout());
		contentArea.add(new FieldPanel(compList), BorderLayout.CENTER);
		messageText.setText("Enter path to image file.");
		
		width = 500;
		height = 150;
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

		ImageBoardState ibState = new ImageBoardState(position, filePath);
		if (ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(ibState, messageText) == null)
			return(false);
		return(true);
	}

}
