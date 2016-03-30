package gov.nasa.arc.dert.view.lighting;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.ui.CoordTextField;
import gov.nasa.arc.dert.ui.DateTextField;
import gov.nasa.arc.dert.ui.DoubleSpinner;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.ui.GBCHelper;
import gov.nasa.arc.dert.ui.GroupPanel;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Provides content for LightingView.
 *
 */
public class LightingPanel extends JPanel {

	private JRadioButton solButton, lampButton;
	private JCheckBox headlightButton, shadowButton;
	private DoubleSpinner mainDiffuseSpinner, mainAmbientSpinner, globalAmbientSpinner, headDiffuseSpinner;
	private JButton advancedShadow, defaultSphereButton;
	private CoordTextField shadowCenterText;
	private DoubleTextField shadowRadiusText;
	private DateTextField lmstEpoch;
	private JLabel modeLabel;

	private Vector3 coord = new Vector3();

	public LightingPanel() {
		final Lighting lighting = World.getInstance().getLighting();
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		GroupPanel mainPanel = new GroupPanel("Main Light");
		mainPanel.setLayout(new GridLayout(4, 1, 0, 0));
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		modeLabel = new JLabel("Mode");
		if (lighting.isLampMode()) {
			modeLabel.setIcon(Icons.getImageIcon("luxo.png"));
		} else {
			modeLabel.setIcon(Icons.getImageIcon("sun.png"));
		}
		panel.add(modeLabel);
		ButtonGroup group = new ButtonGroup();
		solButton = new JRadioButton("Solar");
		solButton.setToolTipText("light is positioned by time");
		solButton.setSelected(!lighting.isLampMode());
		group.add(solButton);
		panel.add(solButton);
		lampButton = new JRadioButton("Artificial");
		lampButton.setToolTipText("light is positioned by azimuth and elevation");
		lampButton.setSelected(lighting.isLampMode());
		group.add(lampButton);
		panel.add(lampButton);
		mainPanel.add(panel);
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Diffuse Intensity"));
		mainDiffuseSpinner = new DoubleSpinner(World.getInstance().getLighting().getDiffuseIntensity(), 0, 1, 0.05,
			false) {
			@Override
			public void stateChanged(ChangeEvent event) {
				double value = (Double) getValue();
				World.getInstance().getLighting().setDiffuseIntensity(value);
			}
		};
		panel.add(mainDiffuseSpinner);
		mainPanel.add(panel);
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Ambient Intensity"));
		mainAmbientSpinner = new DoubleSpinner(World.getInstance().getLighting().getAmbientIntensity(), 0, 1, 0.05,
			false) {
			@Override
			public void stateChanged(ChangeEvent event) {
				double value = (Double) getValue();
				World.getInstance().getLighting().setAmbientIntensity(value);
			}
		};
		panel.add(mainAmbientSpinner);
		mainPanel.add(panel);
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Global Ambient Intensity"));
		globalAmbientSpinner = new DoubleSpinner(World.getInstance().getLighting().getGlobalIntensity(), 0, 1, 0.05,
			false) {
			@Override
			public void stateChanged(ChangeEvent event) {
				double value = (Double) getValue();
				World.getInstance().getLighting().setGlobalIntensity(value);
			}
		};
		panel.add(globalAmbientSpinner);
		mainPanel.add(panel);
		add(mainPanel, GBCHelper.getGBC(0, 0, 1, 4, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0));

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("LMST Epoch"));
		lmstEpoch = new DateTextField(30, lighting.getEpoch(), Lighting.DATE_FORMAT) {
			@Override
			public void handleChange(Date value) {
				lighting.setEpoch(value);
			}
		};
		panel.add(lmstEpoch);
		add(panel, GBCHelper.getGBC(0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0));

		GroupPanel headPanel = new GroupPanel("Headlight");
		headPanel.setLayout(new GridLayout(2, 1, 0, 0));
		headlightButton = new JCheckBox("Enable");
		headlightButton.setSelected(lighting.isHeadlightEnabled());
		headPanel.add(headlightButton);
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Diffuse Intensity"));
		headDiffuseSpinner = new DoubleSpinner(World.getInstance().getLighting().getHeadlightIntensity(), 0, 1, 0.05,
			false) {
			@Override
			public void stateChanged(ChangeEvent event) {
				double value = (Double) getValue();
				World.getInstance().getLighting().setHeadlightIntensity(value);
			}
		};
		panel.add(headDiffuseSpinner);
		headPanel.add(panel);
		add(headPanel, GBCHelper.getGBC(0, 5, 1, 2, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0));

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		shadowButton = new JCheckBox("Enable Shadows");
		shadowButton.setToolTipText("display shadows");
		shadowButton.setSelected(lighting.isShadowEnabled());
		shadowButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				World.getInstance().getLighting().enableShadow(shadowButton.isSelected());
			}
		});
		panel.add(shadowButton);
		advancedShadow = new JButton("Advanced");
		advancedShadow.setToolTipText("edit advanced shadow settings");
		advancedShadow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ShadowSettingsDialog dialog = new ShadowSettingsDialog();
				dialog.open();
			}
		});
		panel.add(advancedShadow);
		add(panel, GBCHelper.getGBC(0, 7, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0));

		GroupPanel shadowPanel = new GroupPanel("Shadow Sphere");
		shadowPanel.setLayout(new GridLayout(3, 1));

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Center"));
//		coord.set(lighting.getShadowMap().getCenter());
//		Landscape.getInstance().localToWorldCoordinate(coord);
		shadowCenterText = new CoordTextField(30, "coordinates of shadow sphere center", Landscape.format, false) {
			@Override
			public void doChange(ReadOnlyVector3 result) {
				coord.set(result);
//				Landscape.getInstance().worldToLocalCoordinate(coord);
				Lighting lighting = World.getInstance().getLighting();
//				lighting.getShadowMap().setCenter(coord);
				Landscape.getInstance().localToWorldCoordinate(coord);
				Landscape.getInstance().worldToSphericalCoordinate(coord);
				lighting.setRefLoc(coord);
			}
		};
		coord.set(lighting.getRefLoc());
		Landscape.getInstance().sphericalToLocalCoordinate(coord);
		shadowCenterText.setLocalValue(coord);
		panel.add(shadowCenterText);
		shadowPanel.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Radius"));
		shadowRadiusText = new DoubleTextField(10, lighting.getShadowMap().getRadius(), true, Landscape.format) {
			@Override
			public void handleChange(double radius) {
				Lighting lighting = World.getInstance().getLighting();
				lighting.getShadowMap().setRadius(radius);
			}
		};
		panel.add(shadowRadiusText);
		shadowPanel.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		defaultSphereButton = new JButton("Default");
		defaultSphereButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				BoundingVolume bv = World.getInstance().getContents().getWorldBound();
				shadowRadiusText.setValue((float) bv.getRadius());
//				coord.set(bv.getCenter());
				coord.set(Landscape.getInstance().getCenter());
//				Landscape.getInstance().localToWorldCoordinate(coord);
				shadowCenterText.setLocalValue(coord);
				lighting.setRefLoc(Landscape.getInstance().getCenterLonLat());
			}
		});
		panel.add(defaultSphereButton);
		shadowPanel.add(panel);
		add(shadowPanel, GBCHelper.getGBC(0, 8, 1, 2, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0));

		solButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (solButton.isSelected()) {
					lightSelected();
				}
			}
		});
		lampButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (lampButton.isSelected()) {
					lightSelected();
				}
			}
		});
		headlightButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				lightSelected();
			}
		});
	}

	private void lightSelected() {
		Lighting lighting = World.getInstance().getLighting();
		if (lampButton.isSelected()) {
			modeLabel.setIcon(Icons.getImageIcon("luxo.png"));
		} else {
			modeLabel.setIcon(Icons.getImageIcon("sun.png"));
		}
		lighting.enableHeadlight(headlightButton.isSelected());
		lighting.setLampMode(lampButton.isSelected());
		Dert.getMainWindow().updateLightIcon();
		World.getInstance().getMarble().setSolarDirection(lighting.getLightDirection());
		World.getInstance().markDirty(DirtyType.RenderState);
	}

}
