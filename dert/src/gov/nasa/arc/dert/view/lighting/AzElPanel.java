package gov.nasa.arc.dert.view.lighting;

import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.ui.AzElDisk;
import gov.nasa.arc.dert.ui.DoubleArrayTextField;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.ardor3d.math.Vector3;

/**
 * Provides the controls for positioning the artificial light with the
 * LightPositionView.
 *
 */
public class AzElPanel extends JPanel {

	// The Az/El control
	private AzElDisk azElDisk;

	// The displays of current az/el and direction
	private DoubleArrayTextField azElText, dirText;

	// The light fields
	private Vector3 direction;
	private double[] azElArray, dirArray;
	private float[] lastAzEl;

	/**
	 * Constructor
	 */
	public AzElPanel() {
		setLayout(new BorderLayout());
		direction = new Vector3();
		dirArray = direction.toArray(null);
		azElArray = new double[2];
		lastAzEl = new float[2];
		lastAzEl[0] = (float) Math.toDegrees(World.getInstance().getLighting().getLight().getAzimuth());
		lastAzEl[1] = (float) Math.toDegrees(World.getInstance().getLighting().getLight().getElevation());
		azElDisk = new AzElDisk(lastAzEl[0], lastAzEl[1]) {
			@Override
			public void applyAzEl(double az, double el) {
				azElArray[0] = az;
				azElArray[1] = el;
				azElText.setValue(azElArray);
				az = (float) Math.toRadians(az);
				el = (float) Math.toRadians(el);
				MathUtil.azElToPoint(az, el, direction);
				direction.negateLocal();
				direction.toArray(dirArray);
				dirText.setValue(dirArray);
				World.getInstance().getLighting().setLightPosition(az, el);
			}
		};
		add(azElDisk, BorderLayout.CENTER);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(2, 1));
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JLabel label = new JLabel("Az/El:", SwingConstants.LEFT);
		panel.add(label, BorderLayout.WEST);
		azElText = new DoubleArrayTextField(8, lastAzEl, "0.00") {
			@Override
			protected void handleChange(double[] value) {
				if (value == null) {
					return;
				}
				if ((value == null) || (value.length != 2)) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				if (value[0] < 0) {
					value[0] += 360;
				}
				azElDisk.setCurrentAzEl(value[0], value[1]);
			}
		};
		azElText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String str = event.getActionCommand();
				try {
					double[] value = StringUtil.stringToDoubleArray(str);
					if ((value == null) || (value.length != 2)) {
						Toolkit.getDefaultToolkit().beep();
						return;
					}
					if (value[0] < 0) {
						value[0] += 360;
					}
					azElDisk.setCurrentAzEl((float) value[0], (float) value[1]);
				} catch (Exception e) {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		});
		panel.add(azElText, BorderLayout.CENTER);
		topPanel.add(panel);
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		label = new JLabel("Direction:", SwingConstants.LEFT);
		panel.add(label, BorderLayout.WEST);
		dirText = new DoubleArrayTextField(10, new float[3], "0.00");
		dirText.setBackground(panel.getBackground());
		dirText.setEditable(false);
		panel.add(dirText, BorderLayout.CENTER);
		topPanel.add(panel);
		add(topPanel, BorderLayout.NORTH);
	}
}
