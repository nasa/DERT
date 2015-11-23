package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.tool.CartesianGrid;
import gov.nasa.arc.dert.scene.tool.Grid;
import gov.nasa.arc.dert.scene.tool.RadialGrid;

import java.awt.Color;

import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides a state object for a CartesianGrid or RadialGrid.
 *
 */
public class GridState extends ToolState {

	// Number of rings for radial grid
	public int rings;

	// Draw compass rose on radial grid
	public boolean compassRose;

	// Number of columns and rows in Cartesian grid
	public int columns, rows;

	/**
	 * Create a CartesianGrid state
	 * 
	 * @param position
	 * @return
	 */
	public static GridState createCartesianGridState(ReadOnlyVector3 position) {
		GridState state = new GridState(MapElementState.Type.CartesianGrid, "CartesianGrid", Grid.defaultCellSize,
			CartesianGrid.defaultColor, CartesianGrid.defaultLabelVisible, CartesianGrid.defaultPinned, position);
		state.columns = CartesianGrid.defaultColumns;
		state.rows = CartesianGrid.defaultRows;
		return (state);
	}

	/**
	 * Create a RadialGrid state
	 * 
	 * @param position
	 * @return
	 */
	public static GridState createRadialGridState(ReadOnlyVector3 position) {
		GridState state = new GridState(MapElementState.Type.RadialGrid, "RadialGrid", Grid.defaultCellSize,
			RadialGrid.defaultColor, RadialGrid.defaultLabelVisible, RadialGrid.defaultPinned, position);
		state.rings = RadialGrid.defaultRings;
		state.compassRose = RadialGrid.defaultCompassRose;
		return (state);
	}

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param prefix
	 * @param size
	 * @param color
	 * @param labelVisible
	 * @param pinned
	 * @param position
	 */
	protected GridState(MapElementState.Type type, String prefix, double size, Color color, boolean labelVisible,
		boolean pinned, ReadOnlyVector3 position) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration().incrementMapElementCount(type), type,
			prefix, size, color, labelVisible, pinned, position);
	}

	@Override
	public void save() {
		super.save();
		if (mapElement != null) {
			if (mapElementType == MapElementState.Type.RadialGrid) {
				rings = ((RadialGrid) mapElement).getRings();
				compassRose = ((RadialGrid) mapElement).isCompassRose();
			} else {
				columns = ((CartesianGrid) mapElement).getColumns();
				rows = ((CartesianGrid) mapElement).getRows();
			}
		}
	}
}
