package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.tool.CartesianGrid;
import gov.nasa.arc.dert.scene.tool.Grid;
import gov.nasa.arc.dert.scene.tool.RadialGrid;
import gov.nasa.arc.dert.util.StateUtil;

import java.awt.Color;
import java.util.Map;

import com.ardor3d.math.Vector3;
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
	
	// Location of center of grid
	public Vector3 location;
	
	public float lineWidth;

	/**
	 * Create a CartesianGrid state
	 * 
	 * @param position
	 * @return
	 */
	public static GridState createCartesianGridState(ReadOnlyVector3 position) {
		GridState state = new GridState(MapElementState.Type.CartesianGrid, "CartesianGrid", CartesianGrid.defaultCellSize,
			CartesianGrid.defaultColor, CartesianGrid.defaultLabelVisible, position,
			CartesianGrid.defaultLineWidth);
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
		GridState state = new GridState(MapElementState.Type.RadialGrid, "RadialGrid", RadialGrid.defaultCellSize,
			RadialGrid.defaultColor, RadialGrid.defaultLabelVisible, position,
			RadialGrid.defaultLineWidth);
		state.rings = RadialGrid.defaultRings;
		state.compassRose = RadialGrid.defaultCompassRose;
		return (state);
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof GridState))
			return(false);
		GridState that = (GridState)state;
		if (!super.isEqualTo(that))
			return(false);
		if (this.rings != that.rings) 
			return(false);
		if (this.columns != that.columns) 
			return(false);
		if (this.rows != that.rows) 
			return(false);
		if (this.compassRose != that.compassRose) 
			return(false);
		if (this.lineWidth != that.lineWidth) 
			return(false);
		if (!this.location.equals(that.location)) 
			return(false);
		return(true);
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
		ReadOnlyVector3 position, float lineWidth) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration().incrementMapElementCount(type), type,
			prefix, size, color, labelVisible);
		location = new Vector3(position);
		this.lineWidth = lineWidth;
	}
	
	/**
	 * Constructor for hash map.
	 */
	public GridState(Map<String,Object> map) {
		super(map);
		rings = StateUtil.getInteger(map, "Rings", 0);
		columns = StateUtil.getInteger(map, "Columns", 0);
		rows = StateUtil.getInteger(map, "Rows", 0);
		compassRose = StateUtil.getBoolean(map, "CompassRose", false);
		location = StateUtil.getVector3(map, "Location", Vector3.ZERO);
		lineWidth = (float)StateUtil.getDouble(map, "LineWidth", 1);
	}

	@Override
	public Map<String,Object> save() {
		Map<String,Object> map = super.save();
		if (mapElement != null) {
			if (mapElementType == MapElementState.Type.RadialGrid) {
				rings = ((RadialGrid) mapElement).getRings();
				compassRose = ((RadialGrid) mapElement).isCompassRose();
			} else {
				columns = ((CartesianGrid) mapElement).getColumns();
				rows = ((CartesianGrid) mapElement).getRows();
			}
			location = new Vector3(((Grid)mapElement).getLocation());
		}
		map.put("Rings", new Integer(rings));
		map.put("Columns", new Integer(columns));
		map.put("Rows", new Integer(rows));
		map.put("CompassRose", new Boolean(compassRose));
		map.put("LineWidth", new Double(lineWidth));
		StateUtil.putVector3(map, "Location", location);
		return(map);
	}
	
	@Override
	public String toString() {
		String str = "["+columns+","+rows+","+rings+","+compassRose+","+location+"] "+super.toString();
		return(str);
	}
}
