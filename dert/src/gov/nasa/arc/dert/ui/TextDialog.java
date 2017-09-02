package gov.nasa.arc.dert.ui;

import gov.nasa.arc.dert.icon.Icons;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * A dialog that displays text in a JTextArea.
 *
 */
public class TextDialog extends AbstractDialog {

	protected JTextArea textArea;
	protected String theText, theMessage;
	protected Color theColor;
	protected boolean scrolled;

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param title
	 * @param width
	 * @param height
	 * @param addMessage
	 * @param addRefresh
	 */
	public TextDialog(Frame parent, String title, int width, int height, boolean addMessage, boolean addRefresh, boolean scrolled) {
		super(parent, title, false, addRefresh, addMessage);
		this.width = width;
		this.height = height;
		this.scrolled = scrolled;
	}

	@Override
	protected void build() {
		setBackground(Color.white);
		getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		getRootPane().setLayout(new BorderLayout());
		if (boolArg) {
			JPanel panel = new JPanel(new BorderLayout());
			JButton refreshButton = new JButton(Icons.getImageIcon("refresh.png"));
			refreshButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					refresh();
				}
			});
			panel.add(refreshButton, BorderLayout.WEST);
			messageText = new JTextField();
			messageText.setEditable(false);
			messageText.setBackground(getBackground());
			messageText.setForeground(Color.blue);
			messageText.setBorder(null);
			panel.add(messageText, BorderLayout.CENTER);
			getRootPane().add(panel, BorderLayout.NORTH);

		} else if (addMessage) {
			messageText = new JTextField();
			messageText.setEditable(false);
			messageText.setBackground(getBackground());
			messageText.setForeground(Color.blue);
			messageText.setBorder(null);
			getRootPane().add(messageText, BorderLayout.NORTH);
		}
		
		contentArea = new JPanel(new BorderLayout());
		textArea = new JTextArea();
		textArea.setEditable(false);
		if (scrolled) {
			textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.getViewport().setView(textArea);
			contentArea.add(scrollPane, BorderLayout.CENTER);
		}
		else
			contentArea.add(textArea, BorderLayout.CENTER);
		getRootPane().add(contentArea, BorderLayout.CENTER);
		if (theMessage != null) {
			messageText.setText(theMessage);
		}
		if (theColor != null) {
			textArea.setForeground(theColor);
		}
		if (theText != null) {
			textArea.setText(theText);
		}
	}

	/**
	 * Set the text to display
	 * 
	 * @param text
	 */
	public void setText(String text) {
		theText = text;
		if (textArea != null) {
			textArea.setText(theText);
			textArea.setCaretPosition(0);
		}
	}

	/**
	 * Append the text to the display
	 * 
	 * @param text
	 */
	public void appendText(String text) {
		if (theText == null)
			theText = "";
		theText += text;
		setText(theText);
	}

	/**
	 * Set the message at the top of the display
	 * 
	 * @param msg
	 */
	public void setMessage(String msg) {
		theMessage = msg;
		if (messageText != null) {
			messageText.setText(theMessage);
		}
	}

	/**
	 * Set the text color
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		theColor = color;
		if (textArea != null) {
			textArea.setForeground(color);
		}
	}

	/**
	 * Close the dialog
	 */
	@Override
	public boolean okPressed() {
		return (true);
	}

	/**
	 * Refresh the display (implemented by subclass)
	 */
	public void refresh() {
		if (textArea != null)
			textArea.setCaretPosition(0);
	}
}
