package gov.nasa.arc.dert.view;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Provides a basic web browser for browsing Help pages.
 *
 */
public class BasicHelpBrowser extends JPanel {

	protected JEditorPane editorPane;

	/**
	 * Constructor
	 */
	public BasicHelpBrowser() {
		super();
		setLayout(new BorderLayout());
		editorPane = new JEditorPane();
		editorPane.setEditable(false);
		editorPane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent event) {
				try {
					editorPane.setPage(event.getURL());
				} catch (Exception e) {
					System.out.println("Error loading page.  See log.");
					e.printStackTrace();
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(editorPane);
		add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * Set the page to be viewed.
	 * 
	 * @param url
	 */
	public void setUrl(String url) {
		try {
			editorPane.setPage(url);
		} catch (Exception e) {
			System.out.println("Unable to display page for " + url + ".  See log.");
			e.printStackTrace();
		}
	}
}
