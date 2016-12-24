package gov.nasa.arc.dert.action;

import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Abstract for menu actions.
 *
 */
public abstract class MenuItemAction extends MenuItem {

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
	
	public void setText(String str) {
		setLabel(str);
	}

	protected abstract void run();
}
