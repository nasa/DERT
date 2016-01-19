package gov.nasa.arc.dert.viewpoint;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JPanel;

/**
 * A compass displaying the current viewpoint heading. Drawn with Java2D.
 *
 */
public class Compass extends JPanel {

	private Rectangle background;
	private int textX, textY, bearing;
	private Color backColor;

	/**
	 * Constructor
	 */
	public Compass() {
		super();
		setPreferredSize(new Dimension(100, 24));
		textX = 50;
		textY = 24;
		backColor = getBackground();
		setToolTipText("viewpoint heading");
	}

	/**
	 * Set the azimuth value
	 * 
	 * @param azimuth
	 */
	public void setValue(double azimuth) {
		bearing = (int) Math.toDegrees(azimuth);
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		background = new Rectangle(0, 0, getWidth(), getHeight());
		if (background.width == 0) {
			return;
		}
		Graphics2D gc = (Graphics2D) g;
		gc.setFont(gc.getFont().deriveFont(10.0f)); // arg must be float to
													// adjust size
		gc.setPaint(backColor);
		gc.fill(background);
		gc.setPaint(Color.black);
		String text = Integer.toString(bearing);
		int textWid = gc.getFontMetrics().stringWidth(text);
		gc.drawString(text, textX - textWid / 2, textY);

		int xb = (bearing % 5) * 2;
		int b = bearing - (bearing % 5) - 25;
		int dirWid = 0;
		int x = 0;
		for (int i = 0; i < 11; ++i) {
			x = i * 10 - xb;
			String dir = getDirection(b);
			gc.drawLine(x, 9, x, 13);
			if (dir != null) {
				dirWid = gc.getFontMetrics().stringWidth(dir);
				gc.drawString(dir, x - dirWid / 2, 10);
			}
			b += 5;
		}
	}

	private String getDirection(int b) {
		switch (b) {
		case 0:
		case 360:
			return ("N");
		case 45:
			return ("NE");
		case 90:
			return ("E");
		case 135:
			return ("SE");
		case 180:
			return ("S");
		case 225:
			return ("SW");
		case 270:
			return ("W");
		case 315:
			return ("NW");
		}
		return (null);
	}

}
