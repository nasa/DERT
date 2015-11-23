package gov.nasa.arc.dert.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Provides an interactive display of azimuth and elevation that can be changed
 * by dragging a puck.
 *
 */
public class AzElDisk extends JPanel {

	private final static int BACK_XOFFSET = 40, BACK_YOFFSET = 20, XOFFSET = 4, YOFFSET = 4;

	// Azimuth and elevation values
	private double azimuth, elevation;

	// Az/El display
	private AzElCanvas canvas;

	// Scales for positioning
	private AdjustableScale azScale, elScale;

	/**
	 * Constructor
	 * 
	 * @param az
	 * @param el
	 */
	public AzElDisk(float az, float el) {
		super();
		azimuth = az;
		elevation = el;
		setLayout(new BorderLayout());

		canvas = new AzElCanvas();
		add(canvas, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(2, 1));

		// azimuth control
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JLabel label = new JLabel("Az:", SwingConstants.LEFT);
		panel.add(label, BorderLayout.WEST);
		azScale = new AdjustableScale(0, 360, azimuth, 60) {
			@Override
			public void apply(double value) {
				azimuth = (float) value;
				canvas.setPuckFromCurrentAzEl();
				canvas.repaint();
				applyAzEl(azimuth, elevation);
			}
		};
		panel.add(azScale, BorderLayout.CENTER);
		bottomPanel.add(panel);

		// elevation control
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		label = new JLabel("El:", SwingConstants.LEFT);
		panel.add(label, BorderLayout.WEST);
		elScale = new AdjustableScale(0, 90, elevation, 10) {
			@Override
			public void apply(double value) {
				elevation = (float) value;
				canvas.setPuckFromCurrentAzEl();
				canvas.repaint();
				applyAzEl(azimuth, elevation);
			}
		};
		panel.add(elScale, BorderLayout.CENTER);
		bottomPanel.add(panel);
		add(bottomPanel, BorderLayout.SOUTH);
	}

	/**
	 * Class for drawing the display
	 *
	 */
	public class AzElCanvas extends JPanel {

		// Drawing rectangles
		protected Rectangle puck, background, limit;

		// User has the puck
		protected boolean grabbed;

		// Display center
		protected int centerX, centerY;

		// Tick marks
		protected int index, tickSize, lastTick, numTicks;

		/**
		 * Constructor
		 */
		public AzElCanvas() {
			super();
			setBorder(BorderFactory.createEtchedBorder());
			background = new Rectangle(0, 0, 0, 0);
			limit = new Rectangle(BACK_XOFFSET, BACK_YOFFSET, 2 * BACK_XOFFSET, 2 * BACK_YOFFSET);
			setPreferredSize(new Dimension(2 * BACK_XOFFSET + 180, 2 * BACK_YOFFSET + 180));
			puck = new Rectangle(-XOFFSET, -YOFFSET, 2 * XOFFSET, 2 * YOFFSET);
			index = 0;
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent event) {
					grabbed = withIn(event.getX(), event.getY(), puck);
					if (grabbed) {
						applyAzEl(azimuth, elevation);
					}
				}

				@Override
				public void mouseReleased(MouseEvent event) {
					grabbed = false;
				}
			});
			addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseDragged(MouseEvent event) {
					if (grabbed && withIn(event.getX(), event.getY(), background)) {
						movePuck(event.getX(), event.getY());
						applyAzEl(azimuth, elevation);
					}
				}
			});

		}

		protected void movePuck(int x, int y) {
			double d = Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY));
			if (d <= limit.width / 2) {
				puck.x = x - XOFFSET;
				puck.y = y - YOFFSET;
			} else {
				puck.x = (int) (centerX + (x - centerX) * (limit.width / 2) / d) - XOFFSET;
				puck.y = (int) (centerY + (y - centerY) * (limit.height / 2) / d) - YOFFSET;
				d = limit.width / 2;
			}
			elevation = (float) (90 - 90 * d / (limit.width / 2));
			d = Math.atan2((puck.y + YOFFSET - centerY), (puck.x + XOFFSET - centerX));
			azimuth = (float) Math.toDegrees(d) + 90;
			if (azimuth < 0) {
				azimuth += 360;
			}
			canvas.repaint();
			azScale.setCurrentValue(azimuth, false);
			elScale.setCurrentValue(elevation, false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			// couldn't get sizing to work with a ComponentListener
			// componentResize method was not always called
			background = getBounds();
			if (background.width == 0) {
				return;
			}
			centerX = background.width / 2;
			centerY = background.height / 2;
			int d = Math.min(background.width - 2 * BACK_XOFFSET, background.height - 2 * BACK_YOFFSET);
			limit = new Rectangle(centerX - d / 2, centerY - d / 2, d, d);
			setPuckFromCurrentAzEl();

			Graphics2D gc = (Graphics2D) g;
			gc.setFont(gc.getFont().deriveFont(10.0f)); // arg must be float to
														// adjust size
			gc.setBackground(Color.white);
			gc.setPaint(Color.white);
			gc.fill(background);
			gc.setPaint(Color.gray);
			for (int i = 1; i < 4; ++i) {
				int x = (int) (centerX - i * 0.125 * limit.width);
				int y = (int) (centerY - i * 0.1255 * limit.height);
				int wid = (int) (limit.width * i * 0.25);
				int hgt = (int) (limit.height * i * 0.25);
				gc.drawOval(x, y, wid, hgt);
			}
			gc.drawLine(limit.x, centerY, limit.x + limit.width, centerY);
			gc.drawLine(centerX, limit.y, centerX, limit.y + limit.height);
			gc.setPaint(Color.black);
			gc.drawOval(limit.x, limit.y, limit.width, limit.height);
			int h = gc.getFontMetrics().getHeight();
			int w = gc.getFontMetrics().stringWidth("West");
			gc.drawString("West", limit.x - w - 2, background.width / 2 + h / 2);
			gc.drawString("East", limit.x + limit.width + 2, background.height / 2 + h / 2);
			w = gc.getFontMetrics().stringWidth("North");
			gc.drawString("North", background.width / 2 - w / 2, limit.y - 2);
			w = gc.getFontMetrics().stringWidth("South");
			gc.drawString("South", background.width / 2 - w / 2, limit.y + limit.height + h);
			gc.draw(puck);
			gc.drawLine(puck.x - XOFFSET, puck.y + YOFFSET, puck.x + puck.width + XOFFSET, puck.y + YOFFSET);
			gc.drawLine(puck.x + XOFFSET, puck.y - YOFFSET, puck.x + XOFFSET, puck.y + puck.height + YOFFSET);
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
		 * Move the puck to the current values
		 */
		public void setPuckFromCurrentAzEl() {
			double az = azimuth - 90;
			double radius = limit.width / 2;
			radius -= radius * elevation / 90;
			puck.x = (int) (Math.cos(Math.toRadians(az)) * radius) + centerX - XOFFSET;
			puck.y = (int) (Math.sin(Math.toRadians(az)) * radius) + centerY - YOFFSET;
		}
	}

	/**
	 * Set the current values
	 * 
	 * @param az
	 * @param el
	 */
	public void setCurrentAzEl(double az, double el) {
		azimuth = az;
		elevation = el;
		canvas.setPuckFromCurrentAzEl();
		canvas.repaint();
		azScale.setCurrentValue(azimuth, false);
		elScale.setCurrentValue(elevation, false);
		applyAzEl(azimuth, elevation);
	}

	/**
	 * Apply the values, must be implemented by subclasses
	 * 
	 * @param azimuth
	 * @param elevation
	 */
	public void applyAzEl(double azimuth, double elevation) {
		// nothing here
	}

}
