package gov.nasa.arc.dert.view;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.state.State;

import java.awt.BorderLayout;
import java.io.File;

/**
 * Provides a view for Help.
 */

public class HelpView extends JPanelView {

	protected BasicHelpBrowser browser;

	/**
	 * The constructor.
	 */
	public HelpView(State state) {
		super(state);
		browser = new BasicHelpBrowser();
		File file = new File(Dert.getPath(), "html");
		file = new File(file, "Help.html");
		browser.setUrl(file.toURI().toString());
		add(browser, BorderLayout.CENTER);
	}

}
