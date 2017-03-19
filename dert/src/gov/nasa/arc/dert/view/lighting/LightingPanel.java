package gov.nasa.arc.dert.view.lighting;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.edit.CoordAction;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.ui.CoordTextField;
import gov.nasa.arc.dert.ui.DateTextField;
import gov.nasa.arc.dert.ui.DoubleSpinner;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.ui.FieldPanel;
import gov.nasa.arc.dert.ui.GroupPanel;
import gov.nasa.arc.dert.ui.VerticalPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
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
		ArrayList<Component> vCompList = new ArrayList<Component>();
		
		setLayout(new BorderLayout());

		// Main Light section
		GroupPanel mainPanel = new GroupPanel("Main Light");
		mainPanel.setLayout(new BorderLayout());
		ArrayList<Component> compList = new ArrayList<Component>();
		
		// Light mode
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		modeLabel = new JLabel("Mode", SwingConstants.RIGHT);
		if (World.getInstance().getLighting().isLampMode()) {
			modeLabel.setIcon(Icons.getImageIcon("luxo.png"));
		} else {
			modeLabel.setIcon(Icons.getImageIcon("sun.png"));
		}
		compList.add(modeLabel);
		ButtonGroup group = new ButtonGroup();
		solButton = new JRadioButton("Solar");
		solButton.setToolTipText("light is positioned by time");
		solButton.setSelected(!World.getInstance().getLighting().isLampMode());
		group.add(solButton);
		panel.add(solButton);
		lampButton = new JRadioButton("Artificial");
		lampButton.setToolTipText("light is positioned by azimuth and elevation");
		lampButton.setSelected(World.getInstance().getLighting().isLampMode());
		group.add(lampButton);
		panel.add(lampButton);
		compList.add(panel);
		
		// Light attributes
		compList.add(new JLabel("Diffuse Intensity", SwingConstants.RIGHT));
		mainDiffuseSpinner = new DoubleSpinner(World.getInstance().getLighting().getDiffuseIntensity(), 0, 1, 0.05,
			false) {
			@Override
			public void stateChanged(ChangeEvent event) {
				double value = (Double) getValue();
				World.getInstance().getLighting().setDiffuseIntensity(value);
			}
		};
		compList.add(mainDiffuseSpinner);
		compList.add(new JLabel("Ambient Intensity", SwingConstants.RIGHT));
		mainAmbientSpinner = new DoubleSpinner(World.getInstance().getLighting().getAmbientIntensity(), 0, 1, 0.05,
			false) {
			@Override
			public void stateChanged(ChangeEvent event) {
				double value = (Double) getValue();
				World.getInstance().getLighting().setAmbientIntensity(value);
			}
		};
		compList.add(mainAmbientSpinner);
		compList.add(new JLabel("Global Ambient Intensity", SwingConstants.RIGHT));
		globalAmbientSpinner = new DoubleSpinner(World.getInstance().getLighting().getGlobalIntensity(), 0, 1, 0.05,
			false) {
			@Override
			public void stateChanged(ChangeEvent event) {
				double value = (Double) getValue();
				World.getInstance().getLighting().setGlobalIntensity(value);
			}
		};
		compList.add(globalAmbientSpinner);
		
		// Time epoch for determing Local Mars Solar Time
		compList.add(new JLabel("LMST Epoch", SwingConstants.RIGHT));
		lmstEpoch = new DateTextField(10, World.getInstance().getLighting().getEpoch(), Lighting.DATE_FORMAT) {
			@Override
			public void handleChange(Date value) {
				World.getInstance().getLighting().setEpoch(value);
			}
		};
		compList.add(lmstEpoch);
		
		FieldPanel fPanel = new FieldPanel(compList);
		mainPanel.add(fPanel, BorderLayout.CENTER);
		vCompList.add(mainPanel);

		// Headlight section
		GroupPanel headPanel = new GroupPanel("Headlight");
		headPanel.setLayout(new BorderLayout());
		compList = new ArrayList<Component>();
		
		compList.add(new JLabel("Headlight", SwingConstants.RIGHT));
		headlightButton = new JCheckBox("enable");
		headlightButton.setSelected(World.getInstance().getLighting().isHeadlightEnabled());
		compList.add(headlightButton);
		
		compList.add(new JLabel("Diffuse Intensity", SwingConstants.RIGHT));
		headDiffuseSpinner = new DoubleSpinner(World.getInstance().getLighting().getHeadlightIntensity(), 0, 1, 0.05,
			false) {
			@Override
			public void stateChanged(ChangeEvent event) {
				double value = (Double) getValue();
				World.getInstance().getLighting().setHeadlightIntensity(value);
			}
		};
		compList.add(headDiffuseSpinner);
		
		fPanel = new FieldPanel(compList);
		headPanel.add(fPanel, BorderLayout.CENTER);
		vCompList.add(headPanel);

		// Shadows section
		GroupPanel shadowPanel = new GroupPanel("Shadows");
		shadowPanel.setLayout(new BorderLayout());
		compList = new ArrayList<Component>();
		
		compList.add(new JLabel("Shadow Map", SwingConstants.RIGHT));
		shadowButton = new JCheckBox("enable");
		shadowButton.setToolTipText("display shadows");
		shadowButton.setSelected(World.getInstance().getLighting().isShadowEnabled());
		shadowButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				World.getInstance().getLighting().enableShadow(shadowButton.isSelected());
			}
		});
		compList.add(shadowButton);

		// Shadow sphere
		compList.add(new JLabel("Shadow Center", SwingConstants.RIGHT));
		shadowCenterText = new CoordTextField(10, "coordinates of shadow sphere center", Landscape.format, false) {
			@Override
			public void doChange(ReadOnlyVector3 result) {
				coord.set(result);
				Lighting lighting = World.getInstance().getLighting();
				lighting.getShadowMap().setCenter(coord);
				Landscape.getInstance().localToWorldCoordinate(coord);
				Landscape.getInstance().worldToSphericalCoordinate(coord);
				lighting.setRefLoc(coord);
			}
		};
		CoordAction.listenerList.add(shadowCenterText);
		coord.set(World.getInstance().getLighting().getRefLoc());
		Landscape.getInstance().sphericalToLocalCoordinate(coord);
		shadowCenterText.setLocalValue(coord);
		compList.add(shadowCenterText);
		compList.add(new JLabel("Shadow Radius", SwingConstants.RIGHT));
		shadowRadiusText = new DoubleTextField(10, World.getInstance().getLighting().getShadowMap().getRadius(), true, Landscape.format) {
			@Override
			public void handleChange(double radius) {
				Lighting lighting = World.getInstance().getLighting();
				lighting.getShadowMap().setRadius(radius);
			}
		};
		compList.add(shadowRadiusText);

		defaultSphereButton = new JButton("Default");
		defaultSphereButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Lighting lighting = World.getInstance().getLighting();
				BoundingVolume bv = World.getInstance().getContents().getWorldBound();
				shadowRadiusText.setValue((float) bv.getRadius());
				coord.set(Landscape.getInstance().getCenter());
				shadowCenterText.setLocalValue(coord);
				World.getInstance().getLighting().setRefLoc(Landscape.getInstance().getCenterLonLat());
				lighting.getShadowMap().setCenter(coord);
				lighting.getShadowMap().setRadius((float) bv.getRadius());
			}
		});
		compList.add(defaultSphereButton);
		advancedShadow = new JButton("Advanced");
		advancedShadow.setToolTipText("edit advanced shadow settings");
		advancedShadow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ShadowSettingsDialog dialog = new ShadowSettingsDialog();
				dialog.open();
			}
		});
		compList.add(advancedShadow);
		
		fPanel = new FieldPanel(compList);
		shadowPanel.add(fPanel, BorderLayout.CENTER);
		vCompList.add(shadowPanel);
		
		VerticalPanel vPanel = new VerticalPanel(vCompList, 0);
		add(vPanel, BorderLayout.CENTER);

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
	
	public void dispose() {
		CoordAction.listenerList.remove(shadowCenterText);
	}

}
