package gov.nasa.arc.dert.action;

import gov.nasa.arc.dert.icon.Icons;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;

/**
 * Helper class for building UI buttons.
 *
 */
public abstract class ButtonAction extends JButton {
	
	protected boolean checked;

	public ButtonAction(String toolTipText, String label, String iconFileName) {
		this(toolTipText, label, iconFileName, false);
	}

	public ButtonAction(String toolTipText, String label, String iconFileName, boolean border) {
		super(label);
//		System.err.println("ButtonAction "+getFont());
		setFont(getFont().deriveFont(Font.BOLD));
		if (iconFileName == null) {
			border = true;
		}
		if (!border) {
			setBorder(BorderFactory.createEmptyBorder());
		}
		setBorderPainted(border);
		setToolTipText(toolTipText);
		setIcon(Icons.getImageIcon(iconFileName));
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				run();
			}
		});
	}
	
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	
	public boolean isChecked() {
		return(checked);
	}

	protected abstract void run();
}
