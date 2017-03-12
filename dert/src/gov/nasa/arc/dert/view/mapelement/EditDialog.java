package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.ui.AbstractDialog;

import java.awt.BorderLayout;
import java.awt.Frame;

/**
 * A dialog that displays text in a JTextArea.
 *
 */
public class EditDialog extends AbstractDialog {

	private MapElement mapElement;
	private MapElementBasePanel basePanel;

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param title
	 * @param width
	 * @param height
	 * @param addMessage
	 * @param addRefresh
	 */
	public EditDialog(Frame parent, String title, MapElement mapElement) {
		super(parent, title, false, true, false);
		this.mapElement = mapElement;
	}

	@Override
	protected void build() {
		super.build();
		contentArea.setLayout(new BorderLayout());
		switch (mapElement.getType()) {
		case Placemark:
			basePanel = new PlacemarkPanel();
			break;
		case Figure:
			basePanel = new FigurePanel();
			break;
		case Billboard:
			basePanel = new ImageBoardPanel();
			break;
		case Feature:
		case FeatureSet:
			basePanel = new FeatureSetPanel();
			break;
		case Waypoint:
		case Path:
			basePanel = new PathPanel();
			break;
		case Plane:
			basePanel = new PlanePanel();
			break;
		case RadialGrid:
			basePanel = new RadialGridPanel();
			break;
		case CartesianGrid:
			basePanel = new CartesianGridPanel();
			break;
		case FieldCamera:
			basePanel = new FieldCameraPanel();
			break;
		case Profile:
			basePanel = new ProfilePanel();
			break;
		case Scale:
			basePanel = new ScaleBarPanel();
			break;
		default:
			break;
		}
		if (basePanel != null) {
			contentArea.add(basePanel, BorderLayout.CENTER);
			basePanel.setMapElement(mapElement);
		}
	}

	/**
	 * Set the text to display
	 * 
	 * @param text
	 */
	public void update() {
		basePanel.update();
	}

	/**
	 * Close the dialog
	 */
	@Override
	public boolean okPressed() {
		return (true);
	}
	
	public void dispose() {
		basePanel.dispose();
	}
}
