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

	/**
	 * Constructor
	 * 
	 * @param vrsn
	 * @param del
	 */
	public AboutBox(Frame parent, String version, String title) {
		super(parent, "About "+title, 650, 450, false, false, true);
		String aboutStr = "Desktop Exploration of Remote Terrain (DERT), version "+version+"\nIntelligent Systems Division, NASA Ames Research Center\n\n";
		try {
			URL url = AboutAction.class.getResource("About.txt");
			InputStream is = url.openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			char[] text = new char[8192];
			int n = reader.read(text);
			is.close();
			aboutStr += new String(text, 0, n);
		} catch (Exception e) {
			e.printStackTrace();
		}
		setText(aboutStr);
	}
	
	@Override
	public boolean open() {
		super.open();
		refresh();
		return(true);
	}
}