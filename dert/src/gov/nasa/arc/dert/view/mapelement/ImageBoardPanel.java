package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.landmark.ImageBoard;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.ui.FieldPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Provides controls for setting options for image billboards.
 *
 */
public class ImageBoardPanel extends MapElementBasePanel {

	// Controls
	private JTextField imageText;
	private DoubleTextField sizeText;

	// Image billboard
	private ImageBoard imageBoard;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public ImageBoardPanel() {
		super();
		icon = ImageBoard.icon;
		type = "ImageBoard";
		build(true, true);
	}

	@Override
	protected void build(boolean addNotes, boolean addLoc) {
		super.build(addNotes, addLoc);
		
		ArrayList<Component> compList = new ArrayList<Component>();

		compList.add(new JLabel("File", SwingConstants.RIGHT));
		imageText = new JTextField();
		imageText.setEditable(false);
		imageText.setBorder(BorderFactory.createEmptyBorder());
		imageText.setToolTipText("image file path");
		compList.add(imageText);

		compList.add(new JLabel("Size", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, ImageBoard.defaultSize, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				imageBoard.setSize(value);
			}
		};
		compList.add(sizeText);
		
		contents.add(new FieldPanel(compList), BorderLayout.CENTER);
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		this.mapElement = mapElement;
		imageBoard = (ImageBoard) mapElement;
		setLocation(locationText, imageBoard.getTranslation());
		nameLabel.setText(imageBoard.getName());
		sizeText.setValue(imageBoard.getSize());
		imageText.setText(imageBoard.getImagePath());
		noteText.setText(imageBoard.getState().getAnnotation());
	}

}
