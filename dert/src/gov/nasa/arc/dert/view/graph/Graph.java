package gov.nasa.arc.dert.view.graph;

import gov.nasa.arc.dert.io.CsvWriter;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.Console;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

/**
 * Provides the content for the GraphView
 *
 */
public class Graph {

	// Axes
	private Axes axes;

	// Dimensions
	private int width, height;

	// Vertices for graph line
	private float[] vertex;
	private int vertexCount;

	// Range
	private float xMin, yMin, xMax, yMax;

	// Colors
	private Color color, pickColor = Color.RED;

	// Buffer size and current portion used
	private int length, numPoints;

	// Buffer
	private int[] lineX, lineY;

	// Pick line
	private int[] pickX, pickY;
	private double valueX;

	// Java2D
	private Stroke stroke, defaultStroke;
	private Graphics2D oldG2d;

	/**
	 * Constructor
	 * 
	 * @param n
	 * @param color
	 */
	public Graph(int n, Color color) {
		valueX = -1;
		this.color = color;
		length = n;
		stroke = new BasicStroke(1.5f);
		axes = new Axes();
		createLine(length);
		createPick();
		axes.setRange(xMin, xMax, yMin, yMax);
	}

	/**
	 * Set the graph line color
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Render the graph
	 * 
	 * @param g2d
	 */
	public void render(Graphics2D g2d) {
		// changed graphics context
		if (g2d != oldG2d) {
			defaultStroke = g2d.getStroke();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			oldG2d = g2d;
		}
		// render axes
		axes.render(g2d);
		// render graph line
		g2d.setColor(color);
		g2d.setStroke(stroke);
		g2d.drawPolyline(lineX, lineY, numPoints);
		g2d.setStroke(defaultStroke);
		// render pick line
		if ((pickX[0] > Axes.LEFT_MARGIN) && (pickX[0] < (width - Axes.RIGHT_MARGIN))) {
			g2d.setColor(pickColor);
			g2d.drawPolyline(pickX, pickY, 2);
		}
	}

	/**
	 * Get the graph coordinates at the mouse X/Y.
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @return
	 */
	public float[] getValueAt(int mouseX, int mouseY) {
		valueX = (mouseX - Axes.LEFT_MARGIN) / axes.getXScale() + xMin;
		double y = Double.NaN;
		if ((valueX >= xMin) && (vertex != null)) {
			for (int i = 3; i < vertex.length; i += 3) {
				if (valueX < vertex[i]) {
					double w = (valueX - vertex[i - 3]) / (vertex[i] - vertex[i - 3]);
					y = w * (vertex[i + 1] - vertex[i - 2]) + vertex[i - 2];
					break;
				}
			}
		}
		pickX[0] = mouseX;
		pickX[1] = mouseX;
		if ((valueX > xMin) && (valueX < xMax)) {
			return (new float[] { (float) valueX, (float) y });
		} else {
			return (null);
		}
	}

	/**
	 * Set the graph line data
	 * 
	 * @param vertex
	 * @param vertexCount
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 */
	public void setData(float[] vertex, int vertexCount, float xMin, float xMax, float yMin, float yMax) {
		if ((xMax - xMin) == 0) {
			xMax += 0.0001;
		}
		if ((yMax - yMin) == 0) {
			yMax += 0.0001;
		}
		this.vertex = vertex;
		this.vertexCount = vertexCount;
		this.xMin = xMin;
		this.yMin = yMin;
		this.xMax = xMax;
		this.yMax = yMax;
		axes.setRange(xMin, xMax, yMin, yMax);
		setData();
		pickX[0] = 0;
		pickX[1] = 0;
		valueX = -1;
	}

	private void setData() {
		buildLine(vertex, vertexCount, xMin, yMin, axes.getXScale(), axes.getYScale());
		pickY[0] = height - Axes.BOTTOM_MARGIN;
		pickY[1] = height - (axes.getHeight() + Axes.BOTTOM_MARGIN);
	}

	/**
	 * The view was resized
	 * 
	 * @param width
	 * @param height
	 */
	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		axes.resize(width, height);
		setData();
		pickX[0] = (int) ((valueX - xMin) * axes.getXScale()) + Axes.LEFT_MARGIN;
		pickX[1] = pickX[0];
	}

	protected void createLine(int n) {
		lineX = new int[n];
		lineY = new int[n];
		lineX[0] = Axes.LEFT_MARGIN;
		lineX[1] = Axes.LEFT_MARGIN;
		lineY[0] = height - Axes.BOTTOM_MARGIN;
		lineY[1] = height - Axes.BOTTOM_MARGIN;
	}

	protected void createPick() {
		pickX = new int[2];
		pickY = new int[2];
		pickX[0] = 0;
		pickX[1] = 0;
		pickY[0] = height - Axes.BOTTOM_MARGIN;
		pickY[1] = height - (axes.getHeight() + Axes.BOTTOM_MARGIN);
	}

	protected void buildLine(float[] vertex, int n, double xMin, double yMin, double xScale, double yScale) {
		// increase buffer size if necessary
		if (n > length) {
			lineX = new int[n];
			lineY = new int[n];
			length = n;
		}
		// fill buffers
		numPoints = n;
		for (int i = 0; i < n; ++i) {
			int ii = i * 3;
			lineX[i] = (int) ((vertex[ii] - xMin) * xScale) + Axes.LEFT_MARGIN;
			lineY[i] = height - (int) ((vertex[ii + 1] - yMin) * yScale + Axes.BOTTOM_MARGIN);
		}
	}

	/**
	 * Save graph line to comma separated value formatted file
	 * 
	 * @param filename
	 * @param column
	 */
	public void saveAsCsv(String filename, String[] column) {
		CsvWriter csvWriter = null;
		try {
			csvWriter = new CsvWriter(filename, column);
			csvWriter.open();
			String[] value = new String[column.length];
			if (vertex != null) {
				for (int i = 0; i < vertexCount; ++i) {
					value[0] = StringUtil.format(vertex[i * 3]);
					value[1] = StringUtil.format(vertex[i * 3 + 1]);
					csvWriter.writeLine(value);
				}
			}
			csvWriter.close();
			Console.getInstance().println(vertexCount + " records saved to " + filename);
		} catch (Exception e) {
			e.printStackTrace();
			if (csvWriter != null) {
				try {
					csvWriter.close();
				} catch (Exception e2) {
					e.printStackTrace();
				}
			}
		}
	}

}
