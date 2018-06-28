/**

DERT is a viewer for digital terrain models created from data collected during NASA missions.

DERT is Released in under the NASA Open Source Agreement (NOSA) found in the “LICENSE” folder where you
downloaded DERT.

DERT includes 3rd Party software. The complete copyright notice listing for DERT is:

Copyright © 2015 United States Government as represented by the Administrator of the National Aeronautics and
Space Administration.  No copyright is claimed in the United States under Title 17, U.S.Code. All Other Rights
Reserved.

Desktop Exploration of Remote Terrain (DERT) could not have been written without the aid of a number of free,
open source libraries. These libraries and their notices are listed below. Find the complete third party license
listings in the separate “DERT Third Party Licenses” pdf document found where you downloaded DERT in the
LICENSE folder.
 
JogAmp Ardor3D Continuation
Copyright © 2008-2012 Ardor Labs, Inc.
 
JogAmp
Copyright 2010 JogAmp Community. All rights reserved.
 
JOGL Portions Sun Microsystems
Copyright © 2003-2009 Sun Microsystems, Inc. All Rights Reserved.
 
JOGL Portions Silicon Graphics
Copyright © 1991-2000 Silicon Graphics, Inc.
 
Light Weight Java Gaming Library Project (LWJGL)
Copyright © 2002-2004 LWJGL Project All rights reserved.
 
Tile Rendering Library - Brain Paul 
Copyright © 1997-2005 Brian Paul. All Rights Reserved.
 
OpenKODE, EGL, OpenGL , OpenGL ES1 & ES2
Copyright © 2007-2010 The Khronos Group Inc.
 
Cg
Copyright © 2002, NVIDIA Corporation
 
Typecast - David Schweinsberg 
Copyright © 1999-2003 The Apache Software Foundation. All rights reserved.
 
PNGJ - Herman J. Gonzalez and Shawn Hartsock
Copyright © 2004 The Apache Software Foundation. All rights reserved.
 
Apache Harmony - Open Source Java SE
Copyright © 2006, 2010 The Apache Software Foundation.
 
Guava
Copyright © 2010 The Guava Authors
 
GlueGen Portions
Copyright © 2010 JogAmp Community. All rights reserved.
 
GlueGen Portions - Sun Microsystems
Copyright © 2003-2005 Sun Microsystems, Inc. All Rights Reserved.
 
SPICE
Copyright © 2003, California Institute of Technology.
U.S. Government sponsorship acknowledged.
 
LibTIFF
Copyright © 1988-1997 Sam Leffler
Copyright © 1991-1997 Silicon Graphics, Inc.
 
PROJ.4
Copyright © 2000, Frank Warmerdam

LibJPEG - Independent JPEG Group
Copyright © 1991-2018, Thomas G. Lane, Guido Vollbeding
 

Disclaimers

No Warranty: THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY KIND,
EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
THAT THE SUBJECT SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY
WARRANTY THAT THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE. THIS AGREEMENT
DOES NOT, IN ANY MANNER, CONSTITUTE AN ENDORSEMENT BY GOVERNMENT AGENCY OR ANY
PRIOR RECIPIENT OF ANY RESULTS, RESULTING DESIGNS, HARDWARE, SOFTWARE PRODUCTS OR
ANY OTHER APPLICATIONS RESULTING FROM USE OF THE SUBJECT SOFTWARE.  FURTHER,
GOVERNMENT AGENCY DISCLAIMS ALL WARRANTIES AND LIABILITIES REGARDING THIRD-PARTY
SOFTWARE, IF PRESENT IN THE ORIGINAL SOFTWARE, AND DISTRIBUTES IT "AS IS."

Waiver and Indemnity:  RECIPIENT AGREES TO WAIVE ANY AND ALL CLAIMS AGAINST THE UNITED
STATES GOVERNMENT, ITS CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR
RECIPIENT.  IF RECIPIENT'S USE OF THE SUBJECT SOFTWARE RESULTS IN ANY LIABILITIES,
DEMANDS, DAMAGES, EXPENSES OR LOSSES ARISING FROM SUCH USE, INCLUDING ANY DAMAGES
FROM PRODUCTS BASED ON, OR RESULTING FROM, RECIPIENT'S USE OF THE SUBJECT SOFTWARE,
RECIPIENT SHALL INDEMNIFY AND HOLD HARMLESS THE UNITED STATES GOVERNMENT, ITS
CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR RECIPIENT, TO THE EXTENT
PERMITTED BY LAW.  RECIPIENT'S SOLE REMEDY FOR ANY SUCH MATTER SHALL BE THE IMMEDIATE,
UNILATERAL TERMINATION OF THIS AGREEMENT.

**/

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

	// Pixels per tick
	public static int TIC_PIXELS = 60;
	public static double[] TIC_INCREMENT = {200000, 100000, 50000, 25000, 10000, 5000, 2500, 2000, 1000, 500, 250, 200, 100, 50, 25, 10, 5, 2.5, 2, 1, 0.5, 0.1, 0.05, 0.01, 0.005, 0.001, 0.0005, 0.0001, 0.00005, 0.00001, 0.000005, 0.000001};

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
	protected int leftMargin = (int)(TIC_PIXELS*1.5);
	protected int rightMargin = (int)(TIC_PIXELS*0.5);
	protected int topMargin = (int)(TIC_PIXELS*0.5);
	protected int bottomMargin = TIC_PIXELS;
	
	// maintain Y/X = 1
	protected boolean equalScale;

	// Tick mark formatters
	private DecimalFormat yFormatter = new DecimalFormat("0.00");
	private DecimalFormat xFormatter = new DecimalFormat("0.00");

	// Range
	protected double xMin = 0, xMax = 100, yMin = 0, yMax = 50, deltaX = 10, deltaY = 10;
	protected double xScale, yScale;
	private double xStart, yStart;

	// Distance in pixels between ticks
	private int width = 2*TIC_PIXELS, height = 2*TIC_PIXELS, graphHeight = 2*TIC_PIXELS, graphWidth = 2*TIC_PIXELS;
	private int pixelsPerXTic, pixelsPerYTic;

	// Tick marks and labels
	private ArrayList<Text> bottomText;
	private ArrayList<Text> leftText;
	private Font font;
	private FontMetrics fontMetrics;
	private Text leftLabel, rightLabel, yAxisLabel, xAxisLabel;

	// Axis lines
	private ArrayList<LineSegment> grid;
	private Stroke stroke, defaultStroke;
	private int rows, cols;

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
		setRange(xMin, xMax, yMin, yMax);
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
		drawGrid(g2d);
		drawText(g2d);
	}

	protected void createText() {

		bottomText.clear();
		for (int i = 0; i <= width; i += pixelsPerXTic) {
			bottomText.add(new Text(""));
		}

		leftText.clear();
		for (int i = 0; i <= height; i += pixelsPerYTic) {
			leftText.add(new Text(""));
		}

		leftLabel = new Text("Point A");
		rightLabel = new Text("Point B");
		xAxisLabel = new Text("Distance");
		yAxisLabel = new Text("Elevation");

	}

	protected void updateText() {
		if (fontMetrics == null) {
			return;
		}
		double x = xStart;
		for (int i = 0; i <= cols; ++i) {
			Text t = bottomText.get(i);
			String s = xFormatter.format(x);
			t.update(leftMargin + i * pixelsPerXTic - fontMetrics.stringWidth(s) / 2f, graphHeight
				- (bottomMargin + (float) (-1.1 * fontMetrics.getHeight())), s);
			x += deltaX;
		}
		double y = yStart;
		for (int i = 0; i <= rows; ++i) {
			Text t = leftText.get(i);
			String s = yFormatter.format(y);
			t.update((float) (leftMargin - 1.1 * fontMetrics.stringWidth(s)), graphHeight
				- (bottomMargin + (float) (i * pixelsPerYTic - fontMetrics.getHeight() / 2.0)), s);
			y += deltaY;
		}
		leftLabel.update(leftMargin, graphHeight - (bottomMargin + (float) (height + fontMetrics.getHeight())));
		rightLabel.update((int) ((xMax - xMin) * xScale) + leftMargin - fontMetrics.stringWidth(rightLabel.str),
			graphHeight - (bottomMargin + (float) (height + fontMetrics.getHeight())));
		yAxisLabel.update((int)(TIC_PIXELS*0.5), graphHeight/2);
		xAxisLabel.update((graphWidth-fontMetrics.stringWidth(xAxisLabel.str))/2, graphHeight-(int)(TIC_PIXELS*0.25));
	}

	private void drawText(Graphics2D g2d) {
		g2d.setColor(Color.darkGray);
		for (int i = 0; i < bottomText.size(); ++i) {
			Text t = bottomText.get(i);
			g2d.drawString(t.str, t.x, t.y);
		}
		for (int i = 0; i < leftText.size(); ++i) {
			Text t = leftText.get(i);
			g2d.drawString(t.str, t.x, t.y);
		}
		g2d.setColor(Color.black);
		g2d.drawString(leftLabel.str, leftLabel.x, leftLabel.y);
		g2d.drawString(rightLabel.str, rightLabel.x, rightLabel.y);
		g2d.drawString(xAxisLabel.str, xAxisLabel.x, xAxisLabel.y);
		g2d.translate(yAxisLabel.x, yAxisLabel.y);
		g2d.rotate(-Math.PI/2);
		g2d.drawString(yAxisLabel.str, 0, 0);
		g2d.rotate(Math.PI/2);
		g2d.translate(-yAxisLabel.x, -yAxisLabel.y);
	}

	private void drawGrid(Graphics2D g2d) {
		// draw the light tick grid
		g2d.setColor(Color.lightGray);
		for (int i = 0; i < rows; ++i) {
			LineSegment ls = grid.get(i);
			g2d.drawLine(ls.x0, ls.y0-(int)(0.5*pixelsPerYTic), ls.x1, ls.y1-(int)(0.5*pixelsPerYTic));
		}
		for (int i = rows+1; i < rows+1+cols; ++i) {
			LineSegment ls = grid.get(i);
			g2d.drawLine(ls.x0+(int)(0.5*pixelsPerXTic), ls.y0, ls.x1+(int)(0.5*pixelsPerXTic), ls.y1);
		}
		// draw the dark tick grid
		g2d.setColor(Color.gray);
		for (int i = 0; i < grid.size(); ++i) {
			LineSegment ls = grid.get(i);
			g2d.drawLine(ls.x0, ls.y0, ls.x1, ls.y1);
		}

		// draw dashed end point B line
		g2d.setColor(Color.black);
		g2d.setStroke(stroke);
		g2d.drawLine((int) ((xMax - xMin) * xScale) + leftMargin, graphHeight - bottomMargin,
			(int) ((xMax - xMin) * xScale) + leftMargin, graphHeight - (bottomMargin + height));
		g2d.setStroke(defaultStroke);
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
		width = graphWidth - (leftMargin + rightMargin);
		height = graphHeight - (bottomMargin + topMargin);
		if ((width == 0) || (height == 0))
			return;
		
		// Adjust X ticks
		double xRange = xMax - xMin;
		if (!equalScale) {
			deltaX = xRange/10;
			for (int i=1; i<TIC_INCREMENT.length; ++i) {
				int nn = (int)Math.ceil(xRange/TIC_INCREMENT[i]);
				if (nn != 0) {
					pixelsPerXTic = (int)(width/nn);
					if (pixelsPerXTic < TIC_PIXELS) {
						deltaX = TIC_INCREMENT[i-1];
						break;
					}
				}
			}
			pixelsPerXTic = (int)(width/(int)Math.ceil(xRange/deltaX));
		}
		else {
			pixelsPerXTic = TIC_PIXELS;
			int nn = (int)Math.ceil(width/(double)pixelsPerXTic);
			deltaX = xRange/nn;
			for (int i=1; i<TIC_INCREMENT.length; ++i)
				if (TIC_INCREMENT[i] <= deltaX) {
					deltaX = TIC_INCREMENT[i-1];
					break;
				}
		}
		cols = width / pixelsPerXTic;
		width = cols * pixelsPerXTic;
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
		if (!equalScale) {
			deltaY = yRange/10;
			for (int i=1; i<TIC_INCREMENT.length; ++i) {
				int nn = (int)Math.ceil(yRange/TIC_INCREMENT[i]);
				if (nn != 0) {
					pixelsPerYTic = (int)(height/nn);
					if (pixelsPerYTic < TIC_PIXELS) {
						deltaY = TIC_INCREMENT[i-1];
						break;
					}
				}
			}
			pixelsPerYTic = (int)(height/Math.ceil(yRange/deltaY));
		}
		else {
			pixelsPerYTic = TIC_PIXELS;
			deltaY = deltaX;
		}
		rows = height / pixelsPerYTic;
		height = rows * pixelsPerYTic;
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
		createGrid();
		createText();
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
		for (int r = 0; r <= rows; ++r) {
			grid.add(new LineSegment(leftMargin, graphHeight - (bottomMargin + r * pixelsPerYTic), leftMargin + width,
				graphHeight - (bottomMargin + r * pixelsPerYTic)));
		}
		for (int c = 0; c <= cols; ++c) {
			grid.add(new LineSegment(leftMargin + c * pixelsPerXTic, graphHeight - bottomMargin, leftMargin + c * pixelsPerXTic,
				graphHeight - (bottomMargin + height)));
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
		graphWidth = wid;
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
		if (value == 0)
			return(0);
		if (value > 10) {
			return (0);
		}
		if (value >= 1) {
			return (1);
		}
		value = Math.log10(value);
		value = Math.abs(value);
		int n = (int) Math.ceil(value) + 1;
		return (n);
	}

}
