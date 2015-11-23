package gov.nasa.arc.dert.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

/**
 * Abstract for menu actions.
 *
 */
public abstract class MenuItemAction extends JMenuItem {

	protected Object arg;

	public MenuItemAction(String name) {
		this(name, null);
	}

	public MenuItemAction(String name, Object obj) {
		super(name);
		arg = obj;
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				run();
			}
		});
	}

	protected abstract void run();
}
