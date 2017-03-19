package gov.nasa.arc.dert.action;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Abstract for menu actions.
 *
 */
public abstract class CheckBoxMenuItemAction extends CheckboxMenuItem {

	protected Object arg;

	public CheckBoxMenuItemAction(String name) {
		this(name, null);
	}

	public CheckBoxMenuItemAction(String name, Object obj) {
		super(name);
		arg = obj;
		addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED)
					run();
			}
		});
	}
	
	public void setText(String str) {
		setLabel(str);
	}

	protected abstract void run();
}
