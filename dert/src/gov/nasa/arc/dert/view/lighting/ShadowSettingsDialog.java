package gov.nasa.arc.dert.view.lighting;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.ui.AbstractDialog;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.ui.GroupPanel;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Provides controls for setting advanced shadow options.
 *
 */
public class ShadowSettingsDialog extends AbstractDialog {

	// Polygon offset control fields
	private DoubleTextField scaleText, unitsText;

	/**
	 * Constructor
	 */
	public ShadowSettingsDialog() {
		super(Dert.getMainWindow(), "Advanced Shadow Settings", true, false);
	}

	@Override
	protected void build() {
		super.build();
		Lighting lighting = World.getInstance().getLighting();
		contentArea.setLayout(new GridLayout(1, 1));

		JPanel panel = new GroupPanel("Polygon Offset");
		panel.setLayout(new GridLayout(1, 4));
		panel.add(new JLabel("Scale:", SwingConstants.RIGHT));
		scaleText = new DoubleTextField(8, lighting.getShadowMap().getPolygonOffsetFactor(), true, "0.000");
		panel.add(scaleText);
		panel.add(new JLabel("Units:", SwingConstants.RIGHT));
		unitsText = new DoubleTextField(8, lighting.getShadowMap().getPolygonOffsetUnits(), true, "0.000");
		panel.add(unitsText);
		contentArea.add(panel);
	}

	/**
	 * Make changes.
	 */
	@Override
	public boolean okPressed() {
		float scale = scaleText.getFloatValue();
		if (Float.isNaN(scale)) {
			return (false);
		}
		float units = unitsText.getFloatValue();
		if (Float.isNaN(units)) {
			return (false);
		}
		Lighting lighting = World.getInstance().getLighting();
		lighting.getShadowMap().setPolygonOffsetFactor(scale);
		lighting.getShadowMap().setPolygonOffsetUnits(units);
		setVisible(false);
		return (true);
	}

}
