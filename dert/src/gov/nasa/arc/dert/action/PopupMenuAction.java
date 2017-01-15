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

	public PopupMenuAction(String toolTipText, String label, ImageIcon icon) {
		super();
		setFont(getFont().deriveFont(Font.BOLD, 14));
		setBorderPainted(false);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		if (icon == null) {
			setText(label);
		} else {
			setIcon(icon);
		}
		setToolTipText(toolTipText);
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				final PopupMenu menu = new PopupMenu();
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
