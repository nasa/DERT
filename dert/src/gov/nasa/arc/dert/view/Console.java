package gov.nasa.arc.dert.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Provides a console for displaying messages.
 *
 */
public class Console extends JPanel {

	// This is a singleton
	protected static Console instance;

	private JTextArea textArea;

	/**
	 * Create the console instance
	 * 
	 * @return
	 */
	public static Console createInstance() {
		instance = new Console();
		return (instance);
	}

	/**
	 * Get the console instance
	 * 
	 * @return
	 */
	public static Console getInstance() {
		return (instance);
	}

	/**
	 * Constructor
	 * 
	 * @return
	 */
	protected Console() {
		textArea = new JTextArea();
		setLayout(new BorderLayout());
		textArea.setEditable(false);
		textArea.setRows(20);
		textArea.setColumns(50);
		add(new JScrollPane(textArea), BorderLayout.CENTER);
	}

	/**
	 * Print text to the console
	 * 
	 * @param str
	 */
	public static void print(final String str) {
		if ((str != null) && (str.length() > 0)) {
			if (instance == null)
				System.err.print(str);
			else
				instance._append(str);
		}
	}

	/**
	 * Print a line to the console
	 * 
	 * @param str
	 */
	public static void println(final String str) {
		if ((str != null) && (str.length() > 0)) {
			if (instance == null)
				System.err.println(str);
			else
				instance._append(str + "\n");
		}
	}

	/**
	 * Add a new line to the console
	 */
	public static void println() {
		if (instance == null)
			System.err.println();
		else
			instance._append("\n");
	}

	/**
	 * Append text to the console
	 * 
	 * @param str
	 */
	public void _append(final String str) {
		if (!SwingUtilities.isEventDispatchThread()) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					textArea.append(str);

					// Make sure the last line is always visible
					textArea.setCaretPosition(textArea.getDocument().getLength());
				}
			});
			Thread.yield();
		} else {
			textArea.append(str);

			// Make sure the last line is always visible
			textArea.setCaretPosition(textArea.getDocument().getLength());
		}
		System.err.print(str);
	}

}
