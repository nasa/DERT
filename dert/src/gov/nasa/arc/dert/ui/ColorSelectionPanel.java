package gov.nasa.arc.dert.ui;

import gov.nasa.arc.dert.icon.Icons;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Provides a panel for displaying and selecting a color.
 *
 */
public class ColorSelectionPanel extends JPanel {

	// Button to open the color chooser
	private JButton colorButton;

	// Label to display the current color
	private JLabel colorLabel;

	// Colors
	private Color color;
	private Color background;

	/**
	 * Constructor
	 * 
	 * @param col
	 */
	public ColorSelectionPanel(Color col) {
		color = col;
		background = getBackground();

		setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		colorLabel = new JLabel("            ");
		colorLabel.setOpaque(true);
		colorLabel.setBackground(color);
		add(colorLabel);

		colorButton = new JButton(Icons.getImageIcon("colors.png"));
		colorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Color col = JColorChooser.showDialog(ColorSelectionPanel.this, "Select Color", color);
				if (col != null) {
					color = col;
					colorLabel.setBackground(color);
					doColor(color);
				}
			}
		});
		add(colorButton);
	}

	/**
	 * Get the selected color
	 * 
	 * @return
	 */
	public Color getColor() {
		return (color);
	}

	/**
	 * Set the selected color
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
		colorLabel.setBackground(color);
	}

	@Override
	public void setEnabled(boolean enabled) {
		colorButton.setEnabled(enabled);
		colorLabel.setEnabled(enabled);
		if (enabled) {
			colorLabel.setBackground(color);
		} else {
			colorLabel.setBackground(background);
		}
		super.setEnabled(enabled);
	}

	/**
	 * Apply the color
	 * 
	 * @param color
	 */
	public void doColor(Color color) {
		// nothing here
	}

}
