package gov.nasa.arc.dert.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

/**
 * Provides a basic web browser for browsing Help pages.
 *
 */
public class BasicHelpBrowser extends JPanel {

	protected JEditorPane editorPane;
	protected JTextField searchText;

	/**
	 * Constructor
	 */
	public BasicHelpBrowser() {
		super();
		setLayout(new BorderLayout());
		JPanel topPanel = new JPanel(new BorderLayout());
		searchText = new JTextField();
		searchText.setEditable(true);
		topPanel.add(searchText, BorderLayout.CENTER);
		JButton searchButton = new JButton("Find");
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					String st = searchText.getText().toLowerCase();
					Document doc = editorPane.getDocument();
					DefaultHighlightPainter hlP = new DefaultHighlightPainter(Color.yellow);
					Highlighter highlighter = editorPane.getHighlighter();
					highlighter.removeAllHighlights();
					String str = doc.getText(0, doc.getLength()).toLowerCase();
					int p = str.indexOf(st, 0);
					while (p >= 0) {
						highlighter.addHighlight(p, p+st.length(), hlP);
						p = str.indexOf(st, p+st.length());
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		topPanel.add(searchButton, BorderLayout.EAST);
		add(topPanel, BorderLayout.NORTH);
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
