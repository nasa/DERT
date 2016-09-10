package gov.nasa.arc.dert.action.edit;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.ui.AbstractDialog;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.view.world.WorldScenePanel;

import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Provides a dialog for selecting stereo mode and setting stereo parameters.
 *
 */
public class StereoDialog extends AbstractDialog {

	private DoubleTextField fdText, esText;
	private JCheckBox stereoCheck;

	/**
	 * Constructor
	 */
	public StereoDialog() {
		super(Dert.getMainWindow(), "Stereo Selection", true, false);
	}

	@Override
	protected void build() {
		super.build();
		contentArea.setLayout(new GridLayout(2, 1));
		stereoCheck = new JCheckBox("Enable Stereo");
		WorldScenePanel wsp = Dert.getWorldView().getScenePanel();
		stereoCheck.setSelected(wsp.isStereo());
		contentArea.add(stereoCheck);

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 4));
		panel.add(new JLabel("Focal Distance:", SwingConstants.RIGHT));
		fdText = new DoubleTextField(8, World.getInstance().stereoFocalDistance, true, Landscape.format);
		panel.add(fdText);
		panel.add(new JLabel("Eye Separation:", SwingConstants.RIGHT));
		esText = new DoubleTextField(8, World.getInstance().stereoEyeSeparation, true, "0.000");
		panel.add(esText);
		contentArea.add(panel);
	}

	/**
	 * Set stereo mode.
	 */
	@Override
	public boolean okPressed() {
		double val = fdText.getValue();
		if (Double.isNaN(val)) {
			return (false);
		}
		World.getInstance().stereoFocalDistance = val;
		val = esText.getValue();
		if (Double.isNaN(val)) {
			return (false);
		}
		World.getInstance().stereoEyeSeparation = val;
		WorldScenePanel wsp = Dert.getWorldView().getScenePanel();
		wsp.setStereo(stereoCheck.isSelected(), World.getInstance().stereoFocalDistance, World.getInstance().stereoEyeSeparation);
		return (true);
	}

}
