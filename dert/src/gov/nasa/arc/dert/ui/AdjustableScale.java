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
 
Tile Rendering Library - Brian Paul 
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

package gov.nasa.arc.dert.ui;

import gov.nasa.arc.dert.action.ButtonAction;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Provides a scale with an adjustable range. The scale can be zoomed by
 * scrolling the mouse wheel.
 *
 */
public class AdjustableScale extends JPanel {

	private final static int XOFFSET = 4, YOFFSET = 4;

	// Text formatters
	private NumberFormat formatter, curFormatter;

	// Values and defaults
	private double currentValue;
	private double minValue, maxValue, defaultMin, defaultMax;

	// Arrow buttons
	private JButton left, right;

	// Canvas for Java2D rendering
	private ScaleCanvas scaleCanvas;

	/**
	 * Constructor
	 * 
	 * @param min
	 * @param max
	 * @param current
	 * @param delta
	 */
	public AdjustableScale(double min, double max, double current, double delta) {
		super();
		defaultMin = minValue = min;
		defaultMax = maxValue = max;
		currentValue = current;
		formatter = new DecimalFormat("0");
		curFormatter = new DecimalFormat("0.00");
		setLayout(new GridBagLayout());

		GridBagConstraints gbc = GBCHelper.getGBC(0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
			0, 0);
		left = new ButtonAction("move left one pixel", null, "prev_15.png", true) {
			@Override
			protected void run() {
				scaleCanvas.left();
			}
		};
		add(left, gbc);

		scaleCanvas = new ScaleCanvas(delta);
		gbc = GBCHelper.getGBC(1, 0, 4, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
		add(scaleCanvas, gbc);

		gbc = GBCHelper.getGBC(5, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 0);
		right = new ButtonAction("move right one pixel", null, "next_15.png", true) {
			@Override
			protected void run() {
				scaleCanvas.right();
			}
		};
		add(right, gbc);

	}

	/**
	 * Apply the current value (subclasses must implement)
	 * 
	 * @param value
	 */
	public void apply(double value) {
		// nothing here
	}

	/**
	 * Class to draw the scale.
	 *
	 */
	public class ScaleCanvas extends JPanel {

		// Puck and background rectangles
		protected Rectangle puck, background;

		// User has the puck
		protected boolean grabbed;

		// Ticks
		protected int index, tickSize, numTicks, lastTick;

		// Scaling
		protected double unitsPerPixel, delta;

		// Text rendering
		protected int xOffset, fontHgt;

		public ScaleCanvas(double delta) {
			super();
			setBorder(BorderFactory.createEtchedBorder());
			setBackground(Color.white);
			background = new Rectangle(0, 0, 0, 0);
			setPreferredSize(new Dimension(200, 24));
			puck = new Rectangle(0, 0, 2 * XOFFSET + 1, 2 * YOFFSET + 1);
			index = 0;
			this.delta = delta;
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent event) {
					grabbed = withIn(event.getX(), event.getY(), puck);
				}

				@Override
				public void mouseReleased(MouseEvent event) {
					grabbed = false;
				}
			});
			addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseDragged(MouseEvent event) {
					if (grabbed && withIn(event.getX(), background.y, background)) {
						movePuck(event.getX());
					}
				}
			});
			addMouseWheelListener(new MouseWheelListener() {
				@Override
				public void mouseWheelMoved(MouseWheelEvent event) {
					zoom(event.getWheelRotation());
				}
			});
			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent event) {
				}
			});

		}

		/**
		 * Zoom the scale so that the ends are closer or farther apart. This
		 * changes the range.
		 * 
		 * @param wheel
		 */
		public void zoom(int wheel) {
			double x = currentValue;
			if ((x < minValue) || (x > maxValue)) {
				x = (maxValue + minValue) / 2;
			}
			double wid = Math.abs(maxValue - minValue);
			double xE = (maxValue - x) / wid;
			double xW = (x - minValue) / wid;
			double w = minValue + wheel * xW;
			double e = maxValue - wheel * xE;
			if (w >= e) {
				return;
			}
			minValue = Math.max(defaultMin, w);
			maxValue = Math.min(defaultMax, e);
			scaleCanvas.repaint();
		}

		protected void movePuck(int x) {
			int minX = background.x + xOffset;
			int maxX = lastTick;
			if (x < minX) {
				x = minX;
			} else if (x > maxX) {
				x = maxX;
			}
			currentValue = minValue + (x - minX) * unitsPerPixel;
			puck.x = x - XOFFSET;
			scaleCanvas.repaint();
			apply(currentValue);
		}

		@Override
		protected void paintComponent(Graphics g) {
			background = new Rectangle(0, 0, scaleCanvas.getWidth(), scaleCanvas.getHeight());
			if (background.width == 0) {
				return;
			}
			if (xOffset == 0) {
				String minText = formatter.format(defaultMin);
				String maxText = formatter.format(defaultMax);
				Graphics2D gc = (Graphics2D) g;
				gc.setFont(gc.getFont().deriveFont(10.0f)); // arg must be float
															// to adjust size
				fontHgt = gc.getFontMetrics().getHeight();
				xOffset = (int) Math.ceil(Math.max(gc.getFontMetrics().stringWidth(maxText), gc.getFontMetrics()
					.stringWidth(minText)) / 2.0);
				numTicks = 8;
				delta = (maxValue - minValue) / numTicks;
				tickSize = (background.width - 2 * xOffset) / numTicks;
				unitsPerPixel = delta / tickSize;
				lastTick = background.x + xOffset + numTicks * tickSize;
				puck.x = (int) ((currentValue - minValue) / unitsPerPixel) + background.x + xOffset - XOFFSET;
			}
			Graphics2D gc = (Graphics2D) g;
			gc.setFont(gc.getFont().deriveFont(10.0f)); // arg must be float to
														// adjust size
			numTicks = 8;
			delta = (maxValue - minValue) / numTicks;
			tickSize = (background.width - 2 * xOffset) / numTicks;
			unitsPerPixel = delta / tickSize;
			lastTick = background.x + xOffset + numTicks * tickSize;
			gc.setBackground(Color.white);
			gc.setPaint(Color.white);
			gc.fill(background);
			gc.setPaint(Color.gray);
			for (int i = 0; i <= numTicks; ++i) {
				gc.drawLine(i * tickSize + background.x + xOffset, background.y, i * tickSize + background.x + xOffset,
					background.y + 2 * YOFFSET + 1);
			}
			if (currentValue != minValue) {
				String minText = formatter.format(minValue);
				int minWid = gc.getFontMetrics().stringWidth(minText);
				gc.drawString(minText, background.x + xOffset - minWid / 2, puck.y + fontHgt + 2 * YOFFSET);
			}
			if (currentValue != maxValue) {
				String maxText = formatter.format(maxValue);
				int maxWid = gc.getFontMetrics().stringWidth(maxText);
				gc.drawString(maxText, lastTick - maxWid / 2, puck.y + fontHgt + 2 * YOFFSET);
			}
			gc.setBackground(Color.black);
			if ((puck.x + puck.width > background.x) && (puck.x < background.x + background.width)) {
				gc.fill(puck);
				gc.setPaint(Color.black);
				gc.drawLine(puck.x + XOFFSET, puck.y, puck.x + XOFFSET, puck.y + 10);
				String currentText = formatter.format(currentValue);
				int curWid = gc.getFontMetrics().stringWidth(currentText);
				currentText = curFormatter.format(currentValue);
				gc.drawString(currentText, puck.x + XOFFSET - curWid / 2, puck.y + fontHgt + 2 * YOFFSET);
			}
		}

		protected boolean withIn(int x, int y, Rectangle rect) {
			if (x < rect.x) {
				return (false);
			}
			if (x >= (rect.x + rect.width)) {
				return (false);
			}
			if (y < rect.y) {
				return (false);
			}
			if (y >= (rect.y + rect.height)) {
				return (false);
			}
			return (true);
		}

		/**
		 * Set the puck position from the current scale value
		 */
		public void setFromCurrentValue() {
			if (unitsPerPixel == 0) {
				return;
			}
			puck.x = (int) ((currentValue - minValue) / unitsPerPixel) + background.x + xOffset - XOFFSET;
		}

		/**
		 * Move the puck to the right one pixel
		 */
		public void right() {
			movePuck(puck.x + XOFFSET + 1);
		}

		/**
		 * Move the puck to the left one pixel
		 */
		public void left() {
			movePuck(puck.x + XOFFSET - 1);
		}

	}

	/**
	 * Get the current scale value
	 * 
	 * @return
	 */
	public double getCurrentValue() {
		return (currentValue);
	}

	/**
	 * Set the current scale value
	 * 
	 * @param current
	 * @param notify
	 */
	public void setCurrentValue(double current, boolean notify) {
		currentValue = current;
		scaleCanvas.setFromCurrentValue();
		scaleCanvas.repaint();
		if (notify) {
			apply(currentValue);
		}
	}

}
