package gov.nasa.arc.dert.view.viewpoint;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.ui.GroupPanel;
import gov.nasa.arc.dert.viewpoint.FlyThroughParameters;
import gov.nasa.arc.dert.viewpoint.ViewpointController;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

/**
 * Provides a dialog for setting fly through animation options.
 *
 */
public class FlyThroughDialog extends JDialog {

	private ViewpointController controller;

	// Path to fly through
	private Path path;

	// Controls
	private DoubleTextField heightText;
	private JButton playButton, pauseButton, stopButton;
	private JButton closeButton, applyButton;
	private JSpinner inbetweenSpinner, millisSpinner;
	private JCheckBox loop;
	private JLabel flyStatus;

	/**
	 * Constructor
	 * 
	 * @param cntlr
	 * @param p
	 */
	public FlyThroughDialog(Dialog owner, ViewpointController cntlr) {
		super(owner, "Fly Through Viewpoints");
		controller = cntlr;
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				controller.closeFlyThrough();
			}
		});
		getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		getRootPane().setLayout(new BorderLayout());

		JPanel controlBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
		playButton = new JButton(Icons.getImageIcon("play.png"));
		playButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				applyButton.setEnabled(false);
				controller.startFlyThrough();
			}
		});
		controlBar.add(playButton);
		pauseButton = new JButton(Icons.getImageIcon("pause.png"));
		pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.pauseFlyThrough();
			}
		});
		controlBar.add(pauseButton);
		stopButton = new JButton(Icons.getImageIcon("stop.png"));
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				applyButton.setEnabled(true);
				controller.stopFlyThrough();
			}
		});
		controlBar.add(stopButton);

		flyStatus = new JLabel("          ", SwingConstants.LEFT);
		controlBar.add(flyStatus);
		getRootPane().add(controlBar, BorderLayout.NORTH);

		JPanel content = new GroupPanel("Parameters");
		content.setLayout(new GridLayout(5, 1));

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Number of Inbetween Frames"));
		inbetweenSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
		panel.add(inbetweenSpinner);
		content.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Milliseconds Per Frame"));
		millisSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 100000, 10));
		millisSpinner.setEditor(new JSpinner.NumberEditor(millisSpinner, "###0.###"));
		panel.add(millisSpinner);
		content.add(panel);
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Height above waypoint (Path only)"));
		heightText = new DoubleTextField(8, 5, false, "0.000");
		panel.add(heightText);
		content.add(panel);

		loop = new JCheckBox("Loop");
		loop.setSelected(false);
		content.add(loop);

		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		applyButton = new JButton("Apply");
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int numInbetweens = (Integer) inbetweenSpinner.getValue();
				int millis = (Integer) millisSpinner.getValue();
				double height = heightText.getValue();
				if (Double.isNaN(height)) {
					return;
				}
				if (path == null) {
					controller.flyViewpoints(numInbetweens, millis, loop.isSelected());
				} else {
					controller.flyPath(path, numInbetweens, millis, loop.isSelected(), height);
				}
			}
		});
		panel.add(applyButton);
		content.add(panel);
		getRootPane().add(content, BorderLayout.CENTER);

		JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.closeFlyThrough();
			}
		});
		buttonBar.add(closeButton);
		getRootPane().add(buttonBar, BorderLayout.SOUTH);
	}
	
	public void setPath(Path p) {
		setTitle("Fly Through" + ((p != null) ? " " + p.getName() : " Viewpoints"));
		path = p;
		FlyThroughParameters flyParams = controller.getFlyThroughParameters();
		inbetweenSpinner.getModel().setValue(flyParams.numInbetweens);
		millisSpinner.getModel().setValue(flyParams.millisPerFrame);
		heightText.setValue(flyParams.pathHeight);
		loop.setSelected(flyParams.loop);

		if (path == null) {
			controller.flyViewpoints(flyParams.numInbetweens, flyParams.millisPerFrame, flyParams.loop);
		} else {
			controller.flyPath(path, flyParams.numInbetweens, flyParams.millisPerFrame, flyParams.loop,
				flyParams.pathHeight);
		}
		pack();
	}

	/**
	 * Show the status of the flight
	 * 
	 * @param status
	 */
	public void setStatus(String status) {
		flyStatus.setText(status);
	}

	/**
	 * Make the apply button usable.
	 * 
	 * @param enable
	 */
	public void enableApply(boolean enable) {
		applyButton.setEnabled(enable);
	}

}
