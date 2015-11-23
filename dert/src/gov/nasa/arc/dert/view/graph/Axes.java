package gov.nasa.arc.dert.view.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.ardor3d.scenegraph.Node;

/**
 * Provides a set of axes for the GraphView.
 *
 */
public class Axes extends Node {

	/**
	 * Data structure for tick mark text.
	 *
	 */
	protected class Text {
		public String str;
		public float x, y;

		public Text(String str) {
			this.str = str;
		}

		public void update(float x, float y) {
			this.x = x;
			this.y = y;
		}

		public void update(float x, float y, String str) {
			this.x = x;
			this.y = y;
			this.str = str;
		}
	}

	/**
	 * Data structure for axis lines
	 *
	 */
	protected class LineSegment {
		public int x0, y0, x1, y1;

		public LineSegment(int x0, int y0, int x1, int y1) {
			this.x0 = x0;
			this.y0 = y0;
			this.x1 = x1;
			this.y1 = y1;
		}
	}

	// Border margins
	public final static int LEFT_MARGIN = 90;
	public final static int RIGHT_MARGIN = 10;
	public final static int TOP_MARGIN = 40;
	public final static int BOTTOM_MARGIN = 30;

	// Pixels per tick
	protected static int PIXEL_X = 75;
	protected static int PIXEL_Y = 30;

	// Tick mark formatters
	private DecimalFormat yFormatter = new DecimalFormat("0.00");
	private DecimalFormat xFormatter = new DecimalFormat("0.00");

	// Range
	private double xMin = 0, xMax = 100, yMin = 0, yMax = 50, deltaX = 10, deltaY = 10;
	private double xScale, yScale, xStart, yStart;

	// Distance in pixels between ticks
	private int width = 100, height = 50, graphHeight = 50;

	// Tick marks and labels
	private ArrayList<Text> bottomText;
	private ArrayList<Text> leftText;
	private Font font;
	private FontMetrics fontMetrics;
	private Text leftLabel, rightLabel;

	// Axis lines
	private ArrayList<LineSegment> grid;
	private Stroke stroke, defaultStroke;

	private Graphics2D oldG2d;

	/**
	 * Constructor
	 */
	public Axes() {
		super("axes");
		bottomText = new ArrayList<Text>();
		leftText = new ArrayList<Text>();
		grid = new ArrayList<LineSegment>();
		font = new Font("Dialog", Font.BOLD, 12);
		stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 10 }, 0);
		createGrid();
		createText();
	}

	/**
	 * Render these axes
	 * 
	 * @param g2d
	 */
	public void render(Graphics2D g2d) {
		// graphics context changed
		if (g2d != oldG2d) {
			g2d.setFont(font);
			fontMetrics = g2d.getFontMetrics();
			defaultStroke = g2d.getStroke();
			setRange(xMin, xMax, yMin, yMax);
			oldG2d = g2d;
		}
		g2d.setColor(Color.black);
		drawGrid(g2d);
		drawText(g2d);
	}

	protected void createText() {

		bottomText.clear();
		for (int i = 0; i <= width; i += PIXEL_X) {
			bottomText.add(new Text(""));
		}

		leftText.clear();
		for (int i = 0; i <= height; i += PIXEL_Y) {
			leftText.add(new Text(""));
		}

		leftLabel = new Text("Point A");
		rightLabel = new Text("Point B");

	}

	protected void updateText() {
		if (fontMetrics == null) {
			return;
		}
		double x = xStart;
		int cols = width / PIXEL_X;
		for (int i = 0; i <= cols; ++i) {
			Text t = bottomText.get(i);
			String s = xFormatter.format(x);
			t.update(LEFT_MARGIN + i * PIXEL_X - fontMetrics.stringWidth(s) / 2f, graphHeight
				- (BOTTOM_MARGIN + (float) (-1.1 * fontMetrics.getHeight())), s);
			x += deltaX;
		}
		double y = yStart;
		int rows = height / PIXEL_Y;
		for (int i = 0; i <= rows; ++i) {
			Text t = leftText.get(i);
			String s = yFormatter.format(y);
			t.update((float) (LEFT_MARGIN - 1.1 * fontMetrics.stringWidth(s)), graphHeight
				- (BOTTOM_MARGIN + (float) (i * PIXEL_Y - fontMetrics.getHeight() / 2.0)), s);
			y += deltaY;
		}
		leftLabel.update(LEFT_MARGIN, graphHeight - (BOTTOM_MARGIN + (float) (height + fontMetrics.getHeight())));
		rightLabel.update((int) ((xMax - xMin) * xScale) + LEFT_MARGIN - fontMetrics.stringWidth(rightLabel.str),
			graphHeight - (BOTTOM_MARGIN + (float) (height + fontMetrics.getHeight())));
	}

	private void drawText(Graphics2D g2d) {
		for (int i = 0; i < bottomText.size(); ++i) {
			Text t = bottomText.get(i);
			g2d.drawString(t.str, t.x, t.y);
		}
		for (int i = 0; i < leftText.size(); ++i) {
			Text t = leftText.get(i);
			g2d.drawString(t.str, t.x, t.y);
		}
		g2d.drawString(leftLabel.str, leftLabel.x, leftLabel.y);
		g2d.drawString(rightLabel.str, rightLabel.x, rightLabel.y);
	}

	private void drawGrid(Graphics2D g2d) {
		// draw the tick grid
		for (int i = 0; i < grid.size(); ++i) {
			LineSegment ls = grid.get(i);
			g2d.drawLine(ls.x0, ls.y0, ls.x1, ls.y1);
		}

		// draw dashed end point B line
		g2d.setStroke(stroke);
		g2d.drawLine((int) ((xMax - xMin) * xScale) + LEFT_MARGIN, graphHeight - BOTTOM_MARGIN,
			(int) ((xMax - xMin) * xScale) + LEFT_MARGIN, graphHeight - (BOTTOM_MARGIN + height));
		g2d.setStroke(defaultStroke);
	}

	private double computeDelta(double range, int n) {
		double d = Math.pow(10, Math.ceil(Math.log10(range / n)));
		if ((d * n) >= (2 * range)) {
			d /= 2;
		}
		if ((d * n) >= (2 * range)) {
			d /= 2;
		}
		return (d);
	}

	/**
	 * Set the range of the axes
	 * 
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 */
	public void setRange(double xMin, double xMax, double yMin, double yMax) {
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;

		// Adjust X ticks
		double xRange = xMax - xMin;
		int cols = width / PIXEL_X;
		deltaX = computeDelta(xRange, cols);
		xMin = ((int) (this.xMin / deltaX)) * deltaX;
		xMax = xMin + cols * deltaX;
		xRange = xMax - xMin;
		xStart = xMin;
		int n = getDecimalPlaces(deltaX);
		String fStr = "0";
		switch (n) {
		case 0:
			fStr = "0";
			break;
		case 1:
			fStr = "0.0";
			break;
		case 2:
			fStr = "0.00";
			break;
		case 3:
			fStr = "0.000";
			break;
		case 4:
			fStr = "0.0000";
			break;
		default:
			fStr = "0.00000";
			break;
		}
		xFormatter.applyPattern(fStr);

		// Adjust Y ticks
		double yRange = yMax - yMin;
		int rows = height / PIXEL_Y;
		deltaY = computeDelta(yRange, rows);
		yMin = ((int) (this.yMin / deltaY)) * deltaY;
		yMax = yMin + rows * deltaY;
		yRange = yMax - yMin;
		yStart = yMin;
		n = getDecimalPlaces(deltaY);
		fStr = "0";
		switch (n) {
		case 0:
			fStr = "0";
			break;
		case 1:
			fStr = "0.0";
			break;
		case 2:
			fStr = "0.00";
			break;
		case 3:
			fStr = "0.000";
			break;
		case 4:
			fStr = "0.0000";
			break;
		default:
			fStr = "0.00000";
			break;
		}
		yFormatter.applyPattern(fStr);
		xScale = width / xRange;
		yScale = height / yRange;
		updateText();
	}

	/**
	 * Get the scale of the X axis
	 * 
	 * @return
	 */
	public final double getXScale() {
		return (xScale);
	}

	/**
	 * Get the scale of the Y axis
	 * 
	 * @return
	 */
	public final double getYScale() {
		return (yScale);
	}

	/**
	 * Get the pixel distance between y ticks
	 * 
	 * @return
	 */
	public final int getHeight() {
		return (height);
	}

	/**
	 * Create a grid of tick lines
	 */
	protected void createGrid() {
		grid.clear();
		int cols = width / PIXEL_X;
		width = cols * PIXEL_X;
		int rows = height / PIXEL_Y;
		height = rows * PIXEL_Y;
		for (int r = 0; r <= rows; ++r) {
			grid.add(new LineSegment(LEFT_MARGIN, graphHeight - (BOTTOM_MARGIN + r * PIXEL_Y), LEFT_MARGIN + width,
				graphHeight - (BOTTOM_MARGIN + r * PIXEL_Y)));
		}
		for (int c = 0; c <= cols; ++c) {
			grid.add(new LineSegment(LEFT_MARGIN + c * PIXEL_X, graphHeight - BOTTOM_MARGIN, LEFT_MARGIN + c * PIXEL_X,
				graphHeight - (BOTTOM_MARGIN + height)));
		}
	}

	/**
	 * The view was resized
	 * 
	 * @param wid
	 * @param hgt
	 */
	public void resize(int wid, int hgt) {
		graphHeight = hgt;
		width = wid - (LEFT_MARGIN + RIGHT_MARGIN);
		height = hgt - (BOTTOM_MARGIN + TOP_MARGIN);
		createGrid();
		createText();
		setRange(xMin, xMax, yMin, yMax);
	}

	/**
	 * Get the number of decimal places
	 * 
	 * @param value
	 * @return
	 */
	private int getDecimalPlaces(double value) {
		value = Math.abs(value);
		if (value > 10) {
			return (0);
		}
		if (value > 1) {
			return (1);
		}
		value = Math.log10(value);
		value = Math.abs(value);
		int n = (int) Math.ceil(value) + 1;
		return (n);
	}

}
