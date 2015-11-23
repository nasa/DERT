package gov.nasa.arc.dert.view;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.state.State;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * Lightweight view for test displays.
 *
 */
public class TextView extends JPanelView {

	protected JTextArea textArea;
	protected JLabel messageLabel;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public TextView(State state, boolean addRefresh) {
		super(state);
		if (addRefresh) {
			JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			topPanel.setBackground(Color.white);
			JButton refreshButton = new JButton(Icons.getImageIcon("refresh.png"));
			refreshButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					doRefresh();
				}
			});
			topPanel.add(refreshButton);
			messageLabel = new JLabel("        ");
			messageLabel.setBackground(Color.white);
			topPanel.add(messageLabel);
			add(topPanel, BorderLayout.NORTH);
		}
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setBackground(Color.white);
		textArea = new JTextArea();
		textArea.setEditable(false);
		add(textArea, BorderLayout.CENTER);
	}

	/**
	 * Set the text.
	 * 
	 * @param text
	 */
	public void setText(String text) {
		textArea.setText(text);
	}
	
	public void setMessage(String msg) {
		messageLabel.setText(msg);
	}
	
	public void doRefresh() {
		// nothing here
	}
}
