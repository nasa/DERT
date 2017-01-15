package gov.nasa.arc.dert.scene.tool;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.state.GridState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.util.UIUtil;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.Properties;

import javax.swing.Icon;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Provides a labeled cartesian grid object.
 */
public class CartesianGrid extends Grid {

	public static final Icon icon = Icons.getImageIcon("cartesiangrid.png");

	// Defaults
	public static double defaultCellSize = 1;
	public static Color defaultColor = Color.white;
	public static int defaultRows = 5;
	public static int defaultColumns = 5;
	public static boolean defaultLabelVisible = false;
	public static boolean defaultActualCoordinates = false;
	public static float defaultLineWidth = 2;

	// Dimensions
	private double min[], max[];
	private int rows, columns;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public CartesianGrid(GridState state) {
		super(state);
		this.rows = state.rows;
		this.columns = state.columns;
		min = new double[2];
		max = new double[2];
		buildGrid();
	}

	/**
	 * Set the number of columns
	 * 
	 * @param columns
	 */
	public void setColumns(int columns) {
		if (this.columns == columns) {
			return;
		}
		this.columns = columns;
		buildGrid();
	}

	/**
	 * Set the number of rows
	 * 
	 * @param rows
	 */
	public void setRows(int rows) {
		if (this.rows == rows) {
			return;
		}
		this.rows = rows;
		buildGrid();
	}

	/**
	 * Get the number of rows
	 * 
	 * @return
	 */
	public int getRows() {
		return (rows);
	}

	/**
	 * get the number of columns
	 * 
	 * @return
	 */
	public int getColumns() {
		return (columns);
	}

	/**
	 * Build the grid
	 */
	@Override
	protected void buildGrid() {
		min[0] = 0;
		min[1] = 0;
		max[0] = columns * cellSize;
		max[1] = rows * cellSize;

		FloatBuffer vertices = buildLatticeVertices(rows, columns);
		lattice.setVertexBuffer(vertices);
		lattice.updateModelBound();
		lattice.setLineWidth(lineWidth);

		buildText();

		updateGeometricState(0, true);
		updateWorldTransform(true);
		updateWorldBound(true);
	}

	private FloatBuffer buildLatticeVertices(int rows, int columns) {

		int numVertices = (rows + 1) * 2 + (columns + 1) * 2;
		FloatBuffer vertex = BufferUtils.createFloatBuffer(numVertices * 3);

		// add Y axis line
		vertex.put((float) min[0]).put((float) min[1]).put(0);
		vertex.put((float) min[0]).put((float) max[1]).put(0);

		// add X axis line
		vertex.put((float) min[0]).put((float) min[1]).put(0);
		vertex.put((float) max[0]).put((float) min[1]).put(0);

		// add columns
		for (int c = 1; c <= columns; ++c) {
			vertex.put((float) (min[0] + c * cellSize)).put((float) min[1]).put(0);
			vertex.put((float) (min[0] + c * cellSize)).put((float) max[1]).put(0);
		}

		// add rows
		for (int r = 1; r <= rows; ++r) {
			vertex.put((float) min[0]).put((float) (min[1] + r * cellSize)).put(0);
			vertex.put((float) max[0]).put((float) (min[1] + r * cellSize)).put(0);
		}

		vertex.flip();
		return (vertex);
	}

	@Override
	protected void buildText() {
		text.detachAllChildren();
		int k = 0;
		ColorRGBA colorRGBA = UIUtil.colorToColorRGBA(color);

		// add Y axis line
		text.attachChild(createColumnText("_cb0", 0, 0, min[1], colorRGBA));
		text.attachChild(createColumnText("_ct0", 0, 0, max[1], colorRGBA));

		// add X axis line
		text.attachChild(createRowText("_rl0", 0, min[0], 0, colorRGBA));
		text.attachChild(createRowText("_rr0", 0, max[0], 0, colorRGBA));

		// add columns
		k = 1;
		for (int c = 1; c <= columns; ++c) {
			double x = c * cellSize;
			text.attachChild(createRowText("_cb" + k, x, x, min[1], colorRGBA));
			text.attachChild(createRowText("_ct" + k, x, x, max[1], colorRGBA));
			k++;
		}

		// add rows
		k = 1;
		for (int r = 1; r <= rows; ++r) {
			double y = r * cellSize;
			text.attachChild(createRowText("_rl" + k, y, min[0], y, colorRGBA));
			text.attachChild(createRowText("_rr" + k, y, max[0], y, colorRGBA));
			k++;
		}
	}

	/**
	 * Get the map element type
	 */
	@Override
	public Type getType() {
		return (Type.CartesianGrid);
	}

	@Override
	public Icon getIcon() {
		return (icon);
	}

	/**
	 * Set defaults
	 * 
	 * @param properties
	 */
	public static void setDefaultsFromProperties(Properties properties) {
		defaultColor = StringUtil.getColorValue(properties, "MapElement.CartesianGrid.defaultColor", defaultColor,
			false);
		defaultRows = StringUtil.getIntegerValue(properties, "MapElement.CartesianGrid.defaultRows", true, defaultRows,
			false);
		defaultColumns = StringUtil.getIntegerValue(properties, "MapElement.CartesianGrid.defaultColumns", true,
			defaultColumns, false);
		defaultLineWidth = (float)StringUtil.getDoubleValue(properties, "MapElement.CartesianGrid.defaultLineWidth", true,
				defaultLineWidth, false);
		defaultLabelVisible = StringUtil.getBooleanValue(properties, "MapElement.CartesianGrid.defaultLabelVisible",
			defaultLabelVisible, false);
		defaultActualCoordinates = StringUtil.getBooleanValue(properties,
			"MapElement.CartesianGrid.defaultActualCoordinates", defaultActualCoordinates, false);
	}

	/**
	 * Save defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		properties.setProperty("MapElement.CartesianGrid.defaultColor", StringUtil.colorToString(defaultColor));
		properties.setProperty("MapElement.CartesianGrid.defaultRows", Integer.toString(defaultRows));
		properties.setProperty("MapElement.CartesianGrid.defaultColumns", Integer.toString(defaultColumns));
		properties.setProperty("MapElement.CartesianGrid.defaultLineWidth", Double.toString(defaultLineWidth));
		properties.setProperty("MapElement.CartesianGrid.defaultLabelVisible", Boolean.toString(defaultLabelVisible));
		properties.setProperty("MapElement.CartesianGrid.defaultActualCoordinates",
			Boolean.toString(defaultActualCoordinates));
	}

}
