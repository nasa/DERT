package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.landmark.ImageBoard;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.util.FileHelper;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.Console;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Provides controls for setting options for image billboards.
 *
 */
public class ImageBoardPanel extends MapElementBasePanel {

	// Controls
	private JTextField imageText;
	private DoubleTextField sizeText;
	private JButton browseButton;
	private JButton openButton;

	// Image billboard
	private ImageBoard imageBoard;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public ImageBoardPanel(MapElementsPanel parent) {
		super(parent);
		icon = ImageBoard.icon;
		type = "ImageBoard";
		build(true, true, true);
	}

	@Override
	protected void build(boolean addNotes, boolean addLoc, boolean addCBs) {
		super.build(addNotes, addLoc, addCBs);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("File"), BorderLayout.WEST);
		imageText = new JTextField();
		imageText.setToolTipText("image file path");
		panel.add(imageText, BorderLayout.CENTER);
		browseButton = new JButton("Browse");
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setImageFile();
			}
		});
		browseButton.setToolTipText("browse to image file");
		panel.add(browseButton, BorderLayout.EAST);
		panel.setMaximumSize(new Dimension(1000, -1));
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Size", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, ImageBoard.defaultSize, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				imageBoard.setSize(value);
			}
		};
		panel.add(sizeText);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		openButton = new JButton("Open Image");
		openButton.setToolTipText("view the image in full size");
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					Desktop.getDesktop().open(new File(imageBoard.getImagePath()));
				} catch (Exception e) {
					e.printStackTrace();
					Console.getInstance().println("Unable to open image, see log.");
					JOptionPane.showMessageDialog(ImageBoardPanel.this, "Unable to open image, see log.", "Error",
						JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		panel.add(openButton);
		contents.add(panel);
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		this.mapElement = mapElement;
		imageBoard = (ImageBoard) mapElement;
		setLocation(locationText, elevLabel, imageBoard.getTranslation());
		pinnedCheckBox.setSelected(imageBoard.isPinned());
		nameLabel.setText(imageBoard.getName());
		sizeText.setValue(imageBoard.getSize());
		imageText.setText(imageBoard.getImagePath());
		labelCheckBox.setSelected(imageBoard.isLabelVisible());
		noteText.setText(imageBoard.getState().getAnnotation());
	}

	protected void setImageFile() {
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Image File", "jpg", "jpeg", "tif", "tiff", "png");
		String path = FileHelper.getFilePathForOpen("Image File Selection", filter);
		if (path != null) {
			File file = new File(path);
			path = file.getAbsolutePath();
			imageText.setText(path);
			nameLabel.setText(StringUtil.getLabelFromFilePath(path));
			if (!path.equals(imageBoard.getImagePath())) {
				imageBoard.setImagePath(path);
			}
		}
	}

}
