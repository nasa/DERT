package gov.nasa.arc.dert.ui;

import gov.nasa.arc.dert.util.ColorMap;
import gov.nasa.arc.dert.view.Console;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.text.DecimalFormat;

/**
 * Provides a Canvas with a palette of colors in a vertical or horizontal bar
 * drawn with Java 2D.
 */
public class Palette extends Canvas {

	// Palette will be vertical
	private boolean vertical;

	// Formatter for tick marks
	private DecimalFormat formatter;

	// Color bars
	private Rectangle colorRect;

	// Colors
	private Color[] color;

	// Values associated with the colors
	private double[] value;

	// Tick mark text
	private String[] tickStr;

	// Dimensions
	private int width, height;
	private String bigTick;

	// Draw as gradient
	private boolean isGradient;

	/**
	 * Constructor
	 * 
	 * @param colorMap
	 * @param vertical
	 */
	public Palette(ColorMap colorMap, boolean vertical) {
		super();
		this.vertical = vertical;
		build(colorMap);
	}

	/**
	 * Build the palette based on the given color map
	 * 
	 * @param colorMap
	 */
	public void build(ColorMap colorMap) {
		value = colorMap.getValues();
		isGradient = colorMap.isGradient();
		
		// determine fractional digits for tick marks
		double dMin = Double.MAX_VALUE;
		for (int i = 1; i < value.length; ++i) {
			dMin = Math.min(dMin, value[i] - value[i - 1]);
		}
		double fracDigits = 0;
		if (dMin != 0)
			fracDigits = Math.log10(dMin);
		else
			Console.println("One or more color map intervals is 0. Labels will have no fractional digits.");
		String str = "0";
		if (fracDigits > 0) {
			fracDigits = 0;
		} else {
			str += ".";
			fracDigits = Math.ceil(Math.abs(fracDigits));
			for (int i = 0; i < fracDigits; ++i) {
				str += "0";
			}
		}
		formatter = new DecimalFormat(str);
		
		
		color = colorMap.getColors();
		tickStr = new String[value.length];
		bigTick = "";
		for (int i = 0; i < value.length; ++i) {
			tickStr[i] = formatter.format(value[i]);
			if (tickStr[i].length() > bigTick.length()) {
				bigTick = tickStr[i];
			}
		}
		if (vertical) {
			colorRect = new Rectangle(0, 0, 40, 20);
			width = 100;
			height = color.length * colorRect.height;
		} else {
			colorRect = new Rectangle(0, 0, 80, 40);
			width = (color.length+1) * colorRect.width;
			height = 80;
		}
		setPreferredSize(new Dimension(width, height));
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		doPaint(g);
	}

	protected void doPaint(Graphics g) {
		if (getWidth() == 0) {
			return;
		}
		FontMetrics fm = g.getFontMetrics();
		if (fm == null) {
			return;
		}
		Graphics2D g2d = (Graphics2D) g;
		Color textBackground = g2d.getBackground();
		Color textForeground = Color.black;
		int x0 = 0, x1 = 0, y0 = 0, y1 = 0;
		int wid = (int) Math.round(fm.stringWidth(bigTick) * 1.5);
		int hh = fm.getAscent();
		int hv = fm.getAscent() / 2;
		int hgt = colorRect.height;

		if (vertical) {
			x1 = 60 + fm.stringWidth(bigTick);
			colorRect.height ++;
		} else {
			x0 = wid / 2;
			y0 = 0;
			y1 = 30;
			colorRect = new Rectangle(x0, y0, wid+1, 20);
		}

		// draw tick marks
		g2d.setPaint(textForeground);
		g2d.setBackground(textBackground);
		for (int i = 0; i < tickStr.length; ++i) {
			if (vertical) {
				y0 = (color.length - i) * hgt;
				int x = x1 - fm.stringWidth(tickStr[i]);
				g2d.drawLine(x0, y0, 50, y0);
				g2d.drawString(tickStr[i], x, y0 + hv);
			} else {
				x1 = x0;
				g2d.drawLine(x0, y0, x1, y1);
				int x = x1 - fm.stringWidth(tickStr[i]) / 2;
				g2d.drawString(tickStr[i], x, y1 + hh);
				x0 += wid;
			}

		}

		// draw color bars
		for (int i = 0; i < color.length-1; ++i) {
			if (vertical) {
				colorRect.y = (color.length-1 - i) * hgt;
				if (isGradient) {
					GradientPaint gp = new GradientPaint(colorRect.x, colorRect.y, color[i+1], colorRect.x,
						colorRect.y + colorRect.height, color[i]);
					g2d.setPaint(gp);
					g2d.fill(colorRect);
				} else {
					g2d.setBackground(color[i]);
					g2d.setPaint(color[i]);
					g2d.fill(colorRect);
					g2d.draw(colorRect);
				}
			} else {
				if (isGradient) {
					GradientPaint gp = new GradientPaint(colorRect.x, colorRect.y, color[i], colorRect.x
						+ colorRect.width, colorRect.y, color[i+1]);
					g2d.setPaint(gp);
					g2d.fill(colorRect);
					colorRect.x += wid;
				} else {
					g2d.setBackground(color[i]);
					g2d.setPaint(color[i]);
					g2d.fill(colorRect);
					// g2d.draw(colorRect);
					colorRect.x += wid;
				}
			}
		}
		if (!vertical) {
			width = colorRect.x + 2*wid;
		}
		g2d.setPaint(textForeground);
		g2d.setBackground(textBackground);
		setPreferredSize(new Dimension(width, height));
	}
}
