package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.landmark.ImageBoard;
import gov.nasa.arc.dert.ui.DoubleTextField;

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
	public ImageBoardPanel(MapElement mapElement) {
		super(mapElement);
	}

	@Override
	protected void addFields(ArrayList<Component> compList) {
		super.addFields(compList);

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
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		super.setMapElement(mapElement);
		imageBoard = (ImageBoard) mapElement;
		setLocation(locationText, locLabel, imageBoard.getTranslation());
		sizeText.setValue(imageBoard.getSize());
		imageText.setText(imageBoard.getImagePath());
		noteText.setText(imageBoard.getState().getAnnotation());
	}

}
