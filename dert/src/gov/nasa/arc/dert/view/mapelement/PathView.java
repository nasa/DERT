package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.state.State;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.view.JPanelView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Lightweight view for test displays.
 *
 */
public class PathView extends JPanelView {

	protected JTextArea textArea;
	protected JLabel messageLabel;
	protected JCheckBox volumeCheck;
	protected JRadioButton polyMethod, planeMethod;
	protected DoubleTextField volElev;
	protected JButton refreshButton;
	protected boolean isCalculating;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public PathView(State state) {
		super(state);
		JPanel northPanel = new JPanel(new GridLayout(2, 1));
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		northPanel.setBackground(Color.white);
		topPanel.setBackground(Color.white);
		refreshButton = new JButton(Icons.getImageIcon("refresh.png"));
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (isCalculating) {
					doCancel();
				}
				else {
					doRefresh();
				}
			}
		});
		topPanel.add(refreshButton);
		messageLabel = new JLabel("        ");
		messageLabel.setBackground(Color.white);
		topPanel.add(messageLabel);
		northPanel.add(topPanel);
		
		JPanel volPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		volPanel.setBackground(Color.white);
		volumeCheck = new JCheckBox("Volume");
		volumeCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				enableVolume();
			}
		});
		volPanel.add(volumeCheck);
		ButtonGroup volType = new ButtonGroup();
		polyMethod = new JRadioButton("Above/Below Polygon");
		polyMethod.setSelected(true);
		polyMethod.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				enableVolume();
			}			
		});
		volType.add(polyMethod);
		volPanel.add(polyMethod);
		planeMethod = new JRadioButton("Above/Below Elevation");
		planeMethod.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				enableVolume();
			}			
		});
		volType.add(planeMethod);
		volPanel.add(planeMethod);
		volElev = new DoubleTextField(8, Double.NaN, false, Landscape.format) {
			@Override
			public void handleChange(double value) {
				if (!isCalculating)
					doRefresh();
			}
		};
		volPanel.add(volElev);
		enableVolume();
		northPanel.add(volPanel);
		add(northPanel, BorderLayout.NORTH);
		
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setBackground(Color.white);
		textArea = new JTextArea();
		textArea.setEditable(false);
		add(new JScrollPane(textArea), BorderLayout.CENTER);
	}

	/**
	 * Set the text.
	 * 
	 * @param text
	 */
	public void setText(String text) {
		textArea.setText(text);
		isCalculating = false;
		refreshButton.setIcon(Icons.getImageIcon("refresh.png"));
	}
	
	public void setMessage(String msg) {
		messageLabel.setText(msg);
	}
	
	public void doRefresh() {
		isCalculating = true;
		refreshButton.setIcon(Icons.getImageIcon("cancel.png"));
	}
	
	public void doCancel() {
		isCalculating = false;
		setMessage("Cancelled");
	}
	
	private void enableVolume() {
		boolean vol = volumeCheck.isSelected();
		polyMethod.setEnabled(vol);
		planeMethod.setEnabled(vol);
		volElev.setEnabled(planeMethod.isSelected() && vol);
	}
	
	public boolean isVolume() {
		return(volumeCheck.isSelected());
	}
	
	public boolean isPlaneMethod() {
		return(planeMethod.isSelected());
	}
	
	public double getVolElevation() {
		if (!isPlaneMethod())
			return(Double.NaN);
		return(volElev.getValue());
	}
}
