package gov.nasa.arc.dert.ui;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * JPanel with an etched border and a title in the upper left corner.
 *
 */
public class GroupPanel extends JPanel {

	public GroupPanel(String title) {
		this(title, 10, 0, 10, 0, 0);
	}

	public GroupPanel(String title, int top, int left, int bottom, int right) {
		this(title, top, left, bottom, right, 0);
	}

	public GroupPanel(String title, int top, int left, int bottom, int right, int innerTop) {
		super();

		if (title != null) {
			Border outerBorder = BorderFactory.createEmptyBorder(top, left, bottom, right);
			Border innerBorder = BorderFactory.createTitledBorder(title);
			outerBorder = BorderFactory.createCompoundBorder(outerBorder, innerBorder);
			innerBorder = BorderFactory.createEmptyBorder(innerTop, 10, 10, 10);
			setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
		}
	}
}
