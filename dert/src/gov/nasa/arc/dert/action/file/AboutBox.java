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
	public AboutBox(Frame parent, String name, String heading) {
		super(parent, "About "+name, 650, 450, false, false, true);
		try {
			URL url = AboutAction.class.getResource("About.txt");
			InputStream is = url.openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			char[] text = new char[8192];
			int n = reader.read(text);
			is.close();
			heading += new String(text, 0, n);
		} catch (Exception e) {
			e.printStackTrace();
		}
		setText(heading);
	}
	
	@Override
	public Object open() {
		super.open();
		refresh();
		return(result);
	}
}