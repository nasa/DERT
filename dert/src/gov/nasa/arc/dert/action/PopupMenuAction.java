package gov.nasa.arc.dert.action;

import gov.nasa.arc.dert.icon.Icons;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPopupMenu;

/**
 * Abstract class for tool bar pop up menus.
 *
 */
public abstract class PopupMenuAction extends JButton {

	public PopupMenuAction(String toolTipText, String label, String iconFileName) {
		super();
		setFont(getFont().deriveFont(Font.BOLD, 14));
		setBorderPainted(false);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		if (iconFileName == null) {
			setText(label);
		} else {
			setIcon(Icons.getImageIcon(iconFileName));
		}
		setToolTipText(toolTipText);
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				final JPopupMenu menu = new JPopupMenu();
				fillMenu(menu);
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						menu.show(PopupMenuAction.this, 0, PopupMenuAction.this.getHeight());
					}
				});
			}
		});
	}

	protected abstract void fillMenu(JPopupMenu menu);

}
