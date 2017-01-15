package gov.nasa.arc.dert.view.viewpoint;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.state.PathState;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.ui.GBCHelper;
import gov.nasa.arc.dert.ui.GroupPanel;
import gov.nasa.arc.dert.ui.OptionDialog;
import gov.nasa.arc.dert.util.FileHelper;
import gov.nasa.arc.dert.viewpoint.FlyThroughParameters;
import gov.nasa.arc.dert.viewpoint.ViewpointController;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
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
import javax.swing.JTextField;
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
	private JSpinner framesSpinner, millisSpinner;
	private JCheckBox loop, grab;
	private JLabel flyStatus;
	private JTextField fileText;
	
	// Parameters for fly through animation
	private FlyThroughParameters flyParams;
	private boolean paused;

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
				if (!paused) {
					if (!setParameters())
						return;
				}
				paused = false;
				enableParameters(false);
				controller.startFlyThrough();
			}
		});
		controlBar.add(playButton);
		pauseButton = new JButton(Icons.getImageIcon("pause.png"));
		pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.pauseFlyThrough();
				paused = !paused;
			}
		});
		controlBar.add(pauseButton);
		stopButton = new JButton(Icons.getImageIcon("stop.png"));
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				enableParameters(true);
				paused = false;
				controller.stopFlyThrough();
			}
		});
		controlBar.add(stopButton);

		flyStatus = new JLabel("          ", SwingConstants.LEFT);
		controlBar.add(flyStatus);
		getRootPane().add(controlBar, BorderLayout.NORTH);

		JPanel content = new GroupPanel("Parameters");
		content.setLayout(new GridLayout(6, 1));

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Number of Frames"));
		framesSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 10000, 1));
		panel.add(framesSpinner);
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

		grab = new JCheckBox("Create Image Sequence");
		grab.setSelected(false);
		content.add(grab);
		
		panel = new JPanel(new GridBagLayout());
		panel.add(new JLabel("Images "), GBCHelper.getGBC(0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0));
		fileText = new JTextField();
		panel.add(fileText, GBCHelper.getGBC(1, 0, 3, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0));
		JButton browseButton = new JButton("Browse");
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String sequencePath = FileHelper.getDirectoryPathForSave("Image Sequence Directory");
				if (sequencePath != null)
					fileText.setText(sequencePath);
			}
		});
		panel.add(browseButton, GBCHelper.getGBC(4, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 0));
		content.add(panel);

		getRootPane().add(content, BorderLayout.CENTER);
	}
	
	public void setPath(Path p) {
		paused = false;
		setTitle("Fly Through" + ((p != null) ? " " + p.getName() : " Waypoints"));
		path = p;
		if (path != null)
			flyParams = ((PathState)path.getState()).flyParams;
		else
			flyParams = controller.getFlyThroughParameters();
		framesSpinner.getModel().setValue(flyParams.numFrames);
		millisSpinner.getModel().setValue(flyParams.millisPerFrame);
		heightText.setValue(flyParams.pathHeight);
		loop.setSelected(flyParams.loop);
		grab.setSelected(flyParams.grab);
		fileText.setText(flyParams.imageSequencePath);
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
	public void enableParameters(boolean enable) {
		heightText.setEnabled(enable);
		framesSpinner.setEnabled(enable);
		millisSpinner.setEnabled(enable);
		grab.setEnabled(enable);
		loop.setEnabled(enable);
		fileText.setEnabled(enable);
	}
	
	private boolean setParameters() {
		int numFrames = (Integer) framesSpinner.getValue();
		int millis = (Integer) millisSpinner.getValue();
		double height = heightText.getValue();
		if (Double.isNaN(height)) {					
			return(false);
		}
		if (grab.isSelected()) {
			if (fileText.getText().isEmpty()) {
				OptionDialog.showErrorMessageDialog((Window)FlyThroughDialog.this, "Please enter a directory for the image sequence.");
				return(false);
			}
		}
		
		flyParams.numFrames = numFrames;
		flyParams.millisPerFrame = millis;
		flyParams.pathHeight = height;
		flyParams.grab = grab.isSelected();
		flyParams.loop = loop.isSelected();
		flyParams.imageSequencePath = fileText.getText();
		if (path == null) {
			controller.flyViewpoints(flyParams);
		} else {
			controller.flyPath(path);
		}
		
		return(true);
		
	}

}
