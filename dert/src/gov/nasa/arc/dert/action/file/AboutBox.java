package gov.nasa.arc.dert.action.file;

import gov.nasa.arc.dert.ui.TextDialog;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Dialog that displays DERT About text.
 *
 */
public class AboutBox extends TextDialog {
	
	private String dertVersion;

	/**
	 * Constructor
	 * 
	 * @param vrsn
	 * @param del
	 */
	public AboutBox(Frame parent, String name, String dertVersion) {
		super(parent, "About "+name, 650, 450, false, false, true);
		this.dertVersion = dertVersion;
	}
	
	@Override
	public Object open() {
		super.open();
		appendDefaultText();
		refresh();
		return(result);
	}
	
	protected void appendDefaultText() {
		try {
			URL url = AboutAction.class.getResource("About.txt");
			String heading = "Desktop Exploration of Remote Terrain (DERT), version "+dertVersion+"\nIntelligent Systems Division, NASA Ames Research Center\n\n";
			appendText(heading+loadText(url));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void appendText(URL url) {
		appendText(loadText(url));
	}
	
	protected String loadText(URL url) {
		try {
			InputStream is = url.openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			char[] text = new char[8192];
			int n = reader.read(text);
			is.close();
			return(new String(text, 0, n));
		} catch (Exception e) {
			e.printStackTrace();
			return("");
		}
	}
}