package gov.nasa.arc.dert.scene.tool;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.state.GridState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.Properties;

import javax.swing.Icon;

import com.ardor3d.math.MathUtils;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Provides a radial grid map element.
 */
public class RadialGrid extends Grid {

	public static final Icon icon = Icons.getImageIcon("radialgrid.png");

	// Defaults
	public static Color defaultColor = Color.white;
	public static int defaultRings = 10;
	public static boolean defaultLabelVisible = false;
	public static boolean defaultPinned = false;
	public static double defaultRadius = 10;
	public static boolean defaultActualCoordinates = false;
	public static boolean defaultCompassRose = false;

	// Number of rings
	private int rings;

	// Show a compass rose
	private boolean compassRose;

	// Dimensions
	private int numSegments = 30, bodySegments = 120;
	private double radius;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public RadialGrid(GridState state) {
		super(state);
		this.rings = state.rings;
		this.compassRose = state.compassRose;
		buildGrid();
	}

	public void setRings(int rings) {
		if (this.rings != rings) {
			this.rings = rings;
			buildGrid();
		}
	}

	/**
	 * Set the number of rings
	 * 
	 * @param compassRose
	 */
	public void setCompassRose(boolean compassRose) {
		if (this.compassRose == compassRose) {
			return;
		}
		this.compassRose = compassRose;
		buildGrid();
	}

	/**
	 * Determine if compass rose
	 * 
	 * @return
	 */
	public boolean isCompassRose() {
		return (compassRose);
	}

	@Override
	protected void buildGrid() {

		radius = rings * cellSize;

		detachChild(body);
		FloatBuffer vertices = buildBodyVertices();
		body.getMeshData().setVertexBuffer(vertices);
		body.updateModelBound();
		attachChild(body);

		vertices = buildLatticeVertices(rings);
		lattice.getMeshData().setVertexBuffer(vertices);
		lattice.updateModelBound();

		text.detachAllChildren();
		buildText();

		updateGeometricState(0, true);
		updateWorldTransform(true);
		updateWorldBound(true);
	}

	protected FloatBuffer buildBodyVertices() {
		FloatBuffer vertex = BufferUtils.createFloatBuffer(bodySegments * 3 * 3);
		double xOrg = 0;
		double yOrg = 0;
		double step = 360 / bodySegments;
		for (int i = 0; i < bodySegments; ++i) {
			vertex.put((float) xOrg).put((float) yOrg).put(0);
			double angle = Math.toRadians(i * step);
			float x = (float) (Math.cos(angle) * radius + xOrg);
			float y = (float) (Math.sin(angle) * radius + yOrg);
			vertex.put(x).put(y).put(0);
			angle = Math.toRadians((i + 1) * step);
			x = (float) (Math.cos(angle) * radius + xOrg);
			y = (float) (Math.sin(angle) * radius + yOrg);
			vertex.put(x).put(y).put(0);
		}
		vertex.flip();
		return (vertex);
	}

	protected FloatBuffer buildLatticeVertices(int circles) {
		int n = circles * (circles + 1) / 2;
		int numVertices = (n * numSegments + 2) * 2;
		if (compassRose) {
			numVertices += 4;
		}
		FloatBuffer vertex = BufferUtils.createFloatBuffer(numVertices * 3);

		// add X axis line
		vertex.put(0).put(-(float) radius).put(0);
		vertex.put(0).put((float) radius).put(0);

		// add Y axis line
		vertex.put(-(float) radius).put(0).put(0);
		vertex.put((float) radius).put(0).put(0);

		if (compassRose) {
			float r = (float) (Math.cos(Math.PI / 4) * radius);
			vertex.put(-r).put(-r).put(0);
			vertex.put(r).put(r).put(0);
			vertex.put(r).put(-r).put(0);
			vertex.put(-r).put(r).put(0);
		}

		for (int c = 1; c <= circles; ++c) {
			addCircle(cellSize * c, c * numSegments, vertex);
		}

		vertex.flip();
		return (vertex);
	}

	protected void addCircle(final double r, final int segments, FloatBuffer verts) {
		double angle = 0;
		final double step = MathUtils.PI * 2 / segments;
		for (int i = 0; i < segments; i++) {
			double dx = MathUtils.cos(angle) * r;
			double dy = MathUtils.sin(angle) * r;
			verts.put((float) (dx)).put((float) (dy)).put(0);
			angle += step;
			dx = MathUtils.cos(angle) * r;
			dy = MathUtils.sin(angle) * r;
			verts.put((float) (dx)).put((float) (dy)).put(0);
		}
	}

	protected void buildText() {
		if (compassRose) {
			text.attachChild(createText("_N", "N", 0, radius, colorRGBA));
			text.attachChild(createText("_S", "S", 0, -radius, colorRGBA));
			text.attachChild(createText("_W", "W", -radius, 0, colorRGBA));
			text.attachChild(createText("_E", "E", radius, 0, colorRGBA));
		} else {
			int k = 0;

			// add Y axis line
			text.attachChild(createColumnText("_yax0", 0, 0, -radius, colorRGBA));
			text.attachChild(createColumnText("_yax0", 0, 0, radius, colorRGBA));

			// add X axis line
			text.attachChild(createRowText("_xax0", 0, -radius, 0, colorRGBA));
			text.attachChild(createRowText("_xax0", 0, radius, 0, colorRGBA));

			// add negative Y axis
			k = 1;
			for (double y = -cellSize; y > -radius; y -= cellSize) {
				text.attachChild(createRowText("_yax" + k, y, 0, y, colorRGBA));
				k++;
			}

			// add positive Y axis
			for (double y = cellSize; y < radius; y += cellSize) {
				text.attachChild(createRowText("_yax" + k, y, 0, y, colorRGBA));
				k++;
			}

			// add negative X axis
			k = 1;
			for (double x = -cellSize; x > -radius; x -= cellSize) {
				text.attachChild(createColumnText("_xax" + k, x, x, 0, colorRGBA));
				k++;
			}

			// add positive X axis
			for (double x = cellSize; x < radius; x += cellSize) {
				text.attachChild(createColumnText("_xax" + k, x, x, 0, colorRGBA));
				k++;
			}
		}
	}

	/**
	 * Get the number of rings
	 * 
	 * @return
	 */
	public int getRings() {
		return (rings);
	}

	/**
	 * Get the map element type
	 */
	@Override
	public Type getType() {
		return (Type.RadialGrid);
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
		defaultColor = StringUtil.getColorValue(properties, "MapElement.RadialGrid.defaultColor", defaultColor, false);
		defaultRings = StringUtil.getIntegerValue(properties, "MapElement.RadialGrid.defaultRings", true, defaultRings,
			false);
		defaultLabelVisible = StringUtil.getBooleanValue(properties, "MapElement.RadialGrid.defaultLabelVisible",
			defaultLabelVisible, false);
		defaultPinned = StringUtil.getBooleanValue(properties, "MapElement.RadialGrid.defaultPinned", defaultPinned,
			false);
		defaultActualCoordinates = StringUtil.getBooleanValue(properties,
			"MapElement.RadialGrid.defaultActualCoordinates", defaultActualCoordinates, false);
		defaultCompassRose = StringUtil.getBooleanValue(properties, "MapElement.RadialGrid.defaultCompassRose",
			defaultCompassRose, false);
	}

	/**
	 * Save defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		properties.setProperty("MapElement.RadialGrid.defaultColor", StringUtil.colorToString(defaultColor));
		properties.setProperty("MapElement.RadialGrid.defaultRings", Integer.toString(defaultRings));
		properties.setProperty("MapElement.RadialGrid.defaultLabelVisible", Boolean.toString(defaultLabelVisible));
		properties.setProperty("MapElement.RadialGrid.defaultPinned", Boolean.toString(defaultPinned));
		properties.setProperty("MapElement.RadialGrid.defaultActualCoordinates",
			Boolean.toString(defaultActualCoordinates));
		properties.setProperty("MapElement.RadialGrid.defaultCompassRose", Boolean.toString(defaultCompassRose));
	}
}
