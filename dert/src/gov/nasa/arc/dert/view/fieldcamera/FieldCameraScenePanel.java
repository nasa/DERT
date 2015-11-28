package gov.nasa.arc.dert.view.fieldcamera;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.render.SceneCanvasPanel;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCameraInfo;
import gov.nasa.arc.dert.state.FieldCameraState;
import gov.nasa.arc.dert.state.State;
import gov.nasa.arc.dert.ui.DoubleArrayTextField;
import gov.nasa.arc.dert.ui.DoubleSpinner;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.ui.Vector3TextField;
import gov.nasa.arc.dert.viewpoint.BasicCamera;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.util.ReadOnlyTimer;

/**
 * SceneCanvasPanel for the FieldCameraView. Provides camera pointing controls.
 *
 */
public class FieldCameraScenePanel extends SceneCanvasPanel {

	// Ardor3D Scene
	private FieldCameraScene fieldCameraScene;

	// FieldCamera map element
	private FieldCamera fieldCamera;

	// Camera pointing controls
	private DoubleSpinner azSpinner, tiltSpinner, heightSpinner;
	private DoubleArrayTextField fovLocationText, fovDirectionText;
	private Vector3TextField seekText;
	private DoubleTextField distanceText;

	// Cross hair visibility
	private JCheckBox crosshair;

	private Vector3 seekPoint = new Vector3();
	private Vector3 coord = new Vector3();

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public FieldCameraScenePanel(FieldCameraState state) {
		super(state.getViewData().getWidth(), state.getViewData().getHeight(), new FieldCameraScene(state), false);
		FieldCameraInfo instInfo = state.getInfo();
		fieldCamera = (FieldCamera) state.getMapElement();
		fieldCameraScene = (FieldCameraScene) scene;
		setState(state);

		JPanel controlPanel = new JPanel(new GridLayout(3, 1));

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 7, 10, 0));

		crosshair = new JCheckBox("Xhair");
		crosshair.setSelected(fieldCameraScene.isCrosshairVisible());
		crosshair.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				fieldCameraScene.setCrosshairVisible(crosshair.isSelected());
			}
		});
		panel.add(crosshair);

		panel.add(new JLabel("Pan", SwingConstants.RIGHT));
		azSpinner = new DoubleSpinner(instInfo.tripodPan, instInfo.panRange[0], instInfo.panRange[1], 1, true) {
			@Override
			public void stateChanged(ChangeEvent event) {
				Double val = (Double) getValue();
				if (val == null) {
					Toolkit.getDefaultToolkit().beep();
				} else if (Double.isNaN(val.doubleValue())) {
					Toolkit.getDefaultToolkit().beep();
				} else {
					fieldCamera.setAzimuth(-val.doubleValue());
				}
			}
		};
		panel.add(azSpinner);

		panel.add(new JLabel("Tilt", SwingConstants.RIGHT));
		tiltSpinner = new DoubleSpinner(instInfo.tripodTilt, instInfo.tiltRange[0], instInfo.tiltRange[1], 1, false) {
			@Override
			public void stateChanged(ChangeEvent event) {
				Double val = (Double) getValue();
				if (val == null) {
					Toolkit.getDefaultToolkit().beep();
				} else if (Double.isNaN(val.doubleValue())) {
					Toolkit.getDefaultToolkit().beep();
				} else {
					fieldCamera.setElevation(val.doubleValue());
				}
			}
		};
		panel.add(tiltSpinner);

		panel.add(new JLabel("Height", SwingConstants.RIGHT));
		heightSpinner = new DoubleSpinner(instInfo.tripodHeight, instInfo.heightRange[0], instInfo.heightRange[1], 1,
			false) {
			@Override
			public void stateChanged(ChangeEvent event) {
				Double val = (Double) getValue();
				if (val == null) {
					Toolkit.getDefaultToolkit().beep();
				} else if (Double.isNaN(val.doubleValue())) {
					Toolkit.getDefaultToolkit().beep();
				} else {
					fieldCamera.setHeight(val.doubleValue());
				}
			}
		};
		panel.add(heightSpinner);

		controlPanel.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton seekButton = new JButton("Point At");
		seekButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 value = seekText.getValue();
				if (value != null) {
					seekPoint.set(value);
					World.getInstance().getLandscape().worldToLocalCoordinate(seekPoint);
					double[] angle = fieldCamera.seek(seekPoint);
					azSpinner.setValue(Math.toDegrees(angle[0]));
					tiltSpinner.setValue(Math.toDegrees(angle[1]));
				}
			}
		});
		panel.add(seekButton);
		seekText = new Vector3TextField(20, seekPoint, Landscape.format, true);
		panel.add(seekText);
		JButton distanceButton = new JButton("Distance");
		distanceButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				double dist = fieldCamera.getDistanceToSurface();
				distanceText.setValue(dist);
			}
		});
		panel.add(distanceButton);
		distanceText = new DoubleTextField(8, 0, false, Landscape.format);
		distanceText.setEditable(false);
		panel.add(distanceText);
		controlPanel.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("FOV   Location:"));
		fovLocationText = new DoubleArrayTextField(20, new double[3], Landscape.format);
		fovLocationText.setBackground(panel.getBackground());
		fovLocationText.setEditable(false);
		panel.add(fovLocationText);
		panel.add(new JLabel("  Direction:"));
		fovDirectionText = new DoubleArrayTextField(12, new double[3], "0.000");
		fovDirectionText.setBackground(panel.getBackground());
		fovDirectionText.setEditable(false);
		panel.add(fovDirectionText);
		controlPanel.add(panel);

		add(controlPanel, BorderLayout.SOUTH);

		azSpinner.setValue(fieldCamera.getAzimuth());
		tiltSpinner.setValue(fieldCamera.getElevation());
		heightSpinner.setValue(fieldCamera.getHeight());
	}

	@Override
	public void setState(State state) {
		super.setState(state);
		canvasRenderer.setCamera(fieldCameraScene.getCamera());
		Dimension size = canvas.getSize();
		resizeCanvas(size.width, size.height);
	}

	@Override
	public void update(ReadOnlyTimer timer) {
		super.update(timer);
		if (fieldCamera.changed.get()) {
			updateFOV();
		}
		fieldCamera.changed.set(false);
	}

	/**
	 * Update the FOV text fields.
	 */
	public void updateFOV() {
		BasicCamera cam = fieldCamera.getCamera();
		coord.set(cam.getLocation());
		World.getInstance().getLandscape().localToWorldCoordinate(coord);
		fovLocationText.setValue(coord);
		coord.set(cam.getDirection());
		fovDirectionText.setValue(coord);
	}

}
