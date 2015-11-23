package gov.nasa.arc.dert.ui;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * Helper class for GridBagConstraints.
 *
 */
public class GBCHelper {

	public static GridBagConstraints getGBC(int width, int height, int anchor, int fill) {
		Insets insets = new Insets(0, 0, 0, 0);
		GridBagConstraints gbc = new GridBagConstraints(0, 0, width, height, 0, 0, anchor, fill, insets, 0, 0);
		return (gbc);
	}

	public static GridBagConstraints getGBC(int x, int y, int width, int height, int anchor, int fill) {
		Insets insets = new Insets(0, 0, 0, 0);
		GridBagConstraints gbc = new GridBagConstraints(x, y, width, height, 0, 0, anchor, fill, insets, 0, 0);
		return (gbc);
	}

	public static GridBagConstraints getGBC(int x, int y, int width, int height, int anchor, int fill, double weightX) {
		Insets insets = new Insets(0, 0, 0, 0);
		GridBagConstraints gbc = new GridBagConstraints(x, y, width, height, weightX, 0, anchor, fill, insets, 0, 0);
		return (gbc);
	}

	public static GridBagConstraints getGBC(int x, int y, int width, int height, int anchor, int fill, double weightX,
		double weightY) {
		Insets insets = new Insets(0, 0, 0, 0);
		GridBagConstraints gbc = new GridBagConstraints(x, y, width, height, weightX, weightY, anchor, fill, insets, 0,
			0);
		return (gbc);
	}

}
