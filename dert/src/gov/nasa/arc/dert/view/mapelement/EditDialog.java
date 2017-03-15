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
			basePanel = new PlacemarkPanel(mapElement);
			break;
		case Figure:
			basePanel = new FigurePanel(mapElement);
			break;
		case Billboard:
			basePanel = new ImageBoardPanel(mapElement);
			break;
		case Feature:
		case FeatureSet:
			basePanel = new FeatureSetPanel(mapElement);
			break;
		case Waypoint:
		case Path:
			basePanel = new PathPanel(mapElement);
			break;
		case Plane:
			basePanel = new PlanePanel(mapElement);
			break;
		case RadialGrid:
			basePanel = new RadialGridPanel(mapElement);
			break;
		case CartesianGrid:
			basePanel = new CartesianGridPanel(mapElement);
			break;
		case FieldCamera:
			basePanel = new FieldCameraPanel(mapElement);
			break;
		case Profile:
			basePanel = new ProfilePanel(mapElement);
			break;
		case Scale:
			basePanel = new ScaleBarPanel(mapElement);
			break;
		default:
			break;
		}
		if (basePanel != null) {
			contentArea.add(basePanel, BorderLayout.CENTER);
			basePanel.setMapElement(mapElement);
		}
	}
	
	public void setMapElement(MapElement me) {
		basePanel.setMapElement(me);
		update();
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
