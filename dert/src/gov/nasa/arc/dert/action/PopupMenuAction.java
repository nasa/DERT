package gov.nasa.arc.dert.action;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * Abstract class for tool bar pop up menus.
 *
 */
public abstract class PopupMenuAction extends JButton {
	
	private PopupMenu menu;

	public PopupMenuAction(String toolTipText, String label, ImageIcon icon) {
		super();
		setFont(getFont().deriveFont(Font.BOLD, 14));
		setBorderPainted(false);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setContentAreaFilled(false);
		if (icon == null) {
			setText(label);
		} else {
			setIcon(icon);
		}
		setToolTipText(toolTipText);
		menu = new PopupMenu();
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				menu.removeAll();
				fillMenu(menu);
				add(menu);
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						menu.show(PopupMenuAction.this, 0, PopupMenuAction.this.getHeight());
					}
				});
			}
		});
	}

	protected abstract void fillMenu(PopupMenu menu);

}
