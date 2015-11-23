package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.LineSet;

import java.awt.Color;

/**
 * Provides a state object for a LineSet.
 *
 */
public class LineSetState extends MapElementState {

	// Path to the LineSet file
	public String filePath;

	/**
	 * Constructor for LayerFactory.
	 * 
	 * @param name
	 * @param filePath
	 * @param color
	 */
	public LineSetState(String name, String filePath, Color color) {
		super(0, MapElementState.Type.LineSet, "", 1.0, color, false, true);
		this.name = name;
		this.filePath = filePath;
	}

	/**
	 * Constructor for DERT.
	 * 
	 * @param name
	 * @param filePath
	 * @param color
	 * @param notes
	 */
	public LineSetState(String name, String filePath, Color color, String notes) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration()
			.incrementMapElementCount(MapElementState.Type.LineSet), MapElementState.Type.LineSet, "", 1.0, color,
			false, true);
		this.name = name;
		this.filePath = filePath;
		this.annotation = notes;
	}

	@Override
	public void save() {
		super.save();
		if (mapElement != null) {
			LineSet lineSet = (LineSet) mapElement;
			filePath = lineSet.getFilePath();
		}
	}
}
