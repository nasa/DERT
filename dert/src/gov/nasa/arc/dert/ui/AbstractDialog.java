package gov.nasa.arc.dert.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Provides an abstract base class for dialogs.
 *
 */
public abstract class AbstractDialog extends JDialog {

	// Returned result (if any)
	protected boolean result;

	// The area where extending classes can put widgets
	protected JPanel contentArea;

	// Panel for buttons at bottom of dialog
	protected JPanel buttonsPanel;

	// Message field at the top of the dialog
	protected JTextField messageText;

	// Default buttons
	protected JButton okButton, cancelButton;

	// Boolean argument and flag to add message text
	protected boolean boolArg, addMessage;

	// Dimensions
	protected int width, height;

	/**
	 * Constuctor
	 * 
	 * @param parent
	 * @param title
	 * @param modal
	 * @param addMessage
	 */
	public AbstractDialog(Frame parent, String title, boolean modal, boolean addMessage) {
		this(parent, title, modal, false, addMessage);
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param title
	 * @param modal
	 * @param boolArg
	 * @param addMessage
	 */
	public AbstractDialog(Frame parent, String title, boolean modal, boolean boolArg, boolean addMessage) {
		super(parent, title, modal);
		setLocationRelativeTo(parent);
		this.boolArg = boolArg;
		this.addMessage = addMessage;
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param title
	 * @param modal
	 * @param addMessage
	 */
	public AbstractDialog(Dialog parent, String title, boolean modal, boolean addMessage) {
		this(parent, title, modal, false, addMessage);
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param title
	 * @param modal
	 * @param closeOnly
	 * @param addMessage
	 */
	public AbstractDialog(Dialog parent, String title, boolean modal, boolean boolArg, boolean addMessage) {
		super(parent, title, modal);
		setLocationRelativeTo(parent);
		this.boolArg = boolArg;
		this.addMessage = addMessage;
	}

	protected void build() {
		getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		getRootPane().setLayout(new BorderLayout());
		if (addMessage) {
			messageText = new JTextField();
			messageText.setEditable(false);
			messageText.setBackground(getBackground());
			messageText.setForeground(Color.blue);
			messageText.setBorder(null);
			getRootPane().add(messageText, BorderLayout.NORTH);
		}
		contentArea = new JPanel();
		getRootPane().add(contentArea, BorderLayout.CENTER);
		buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		if (boolArg) {
			okButton = new JButton("Close");
//			okButton.addActionListener(new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent event) {
//					if (okPressed()) {
//						close();
//					}
//				}
//			});
//			buttonsPanel.add(okButton);
		} else {
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					if (cancelPressed()) {
						close();
					}
				}
			});
			buttonsPanel.add(cancelButton);

			okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					if (okPressed()) {
						close();
					}
				}
			});
			buttonsPanel.add(okButton);
		}
		getRootPane().add(buttonsPanel, BorderLayout.SOUTH);
		if (okButton != null) {
			getRootPane().setDefaultButton(okButton);
		} else if (cancelButton != null) {
			getRootPane().setDefaultButton(cancelButton);
		}
	}

	protected abstract boolean okPressed();

	protected boolean cancelPressed() {
		result = false;
		return (true);
	}

	/**
	 * Open the dialog
	 */
	public boolean open() {
		result = false;
		if (contentArea == null) {
			build();
			if ((width == 0) || (height == 0)) {
				pack();
			} else {
				setSize(width, height);
			}
		}
		setVisible(true);
		return(result);
	}

	/**
	 * Close the dialog
	 */
	public void close() {
		setVisible(false);
	}
}
