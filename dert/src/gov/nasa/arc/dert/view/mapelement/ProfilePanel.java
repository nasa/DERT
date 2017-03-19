package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.action.edit.CoordAction;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.tool.Profile;
import gov.nasa.arc.dert.ui.CoordTextField;
import gov.nasa.arc.dert.ui.DoubleTextField;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Provides controls for setting options for profile tools.
 *
 */
public class ProfilePanel extends MapElementBasePanel {

	// Controls
	private CoordTextField pALocation, pBLocation;
	private DoubleTextField lineWidthText;
	private JCheckBox endpointsCheckBox;
	private JLabel pALabel, pBLabel;

	// The profile
	private Profile profile;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public ProfilePanel(MapElement mapElement) {
		super(mapElement);
	}

	@Override
	protected void addFields(ArrayList<Component> compList) {

		pALabel = new JLabel("Point A", SwingConstants.RIGHT);
		compList.add(pALabel);
		pALocation = new CoordTextField(22, "location of end point A", Landscape.format, true) {
			@Override
			public void doChange(ReadOnlyVector3 result) {
				double z = Landscape.getInstance().getZ(result.getX(), result.getY());
				if (Double.isNaN(z)) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				if (Double.isNaN(coord.getZ())) {
					profile.setEndpointA(result.getX(), result.getY(), z);
				}
				else {
					profile.getMarkerA().setZOffset(result.getZ()-z, false);
					profile.setEndpointA(result.getX(), result.getY(), z);
				}
			}
		};
		CoordAction.listenerList.add(pALocation);
		compList.add(pALocation);

		pBLabel = new JLabel("Point B", SwingConstants.RIGHT);
		compList.add(pBLabel);
		pBLocation = new CoordTextField(22, "location of end point B", Landscape.format, true) {
			@Override
			public void doChange(ReadOnlyVector3 result) {
				double z = Landscape.getInstance().getZ(result.getX(), result.getY());
				if (Double.isNaN(z)) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				if (Double.isNaN(coord.getZ())) {
					profile.setEndpointB(result.getX(), result.getY(), z);
				}
				else {
					profile.getMarkerB().setZOffset(result.getZ()-z, false);
					profile.setEndpointB(result.getX(), result.getY(), z);
				}
			}
		};
		CoordAction.listenerList.add(pBLocation);
		compList.add(pBLocation);

		compList.add(new JLabel("Line Width", SwingConstants.RIGHT));
		lineWidthText = new DoubleTextField(8, Profile.defaultLineWidth, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				profile.setLineWidth((float) value);
			}
		};
		compList.add(lineWidthText);
		
		compList.add(new JLabel("Endpoints", SwingConstants.RIGHT));
		endpointsCheckBox = new JCheckBox("visible");
		endpointsCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				profile.setEndpointsVisible(endpointsCheckBox.isSelected());
				profile.markDirty(DirtyType.RenderState);
			}
		});
		compList.add(endpointsCheckBox);
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		super.setMapElement(mapElement);
		profile = (Profile) mapElement;
		setLocation(pALocation, pALabel, profile.getEndpointA());
		setLocation(pBLocation, pBLabel, profile.getEndpointB());
		endpointsCheckBox.setSelected(profile.isEndpointsVisible());
		lineWidthText.setValue(profile.getLineWidth());
		noteText.setText(profile.getState().getAnnotation());
	}

	@Override
	public void updateLocation(MapElement mapElement) {
		setLocation(pALocation, pALabel, profile.getEndpointA());
		setLocation(pBLocation, pBLabel, profile.getEndpointB());
	}
	
	@Override
	public void dispose() {
		super.dispose();
		if (pALocation != null)
			CoordAction.listenerList.remove(pALocation);
		if (pBLocation != null)
			CoordAction.listenerList.remove(pBLocation);
	}

}
