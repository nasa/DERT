package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.action.edit.CoordAction;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.tool.Profile;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.CoordTextField;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.util.FileHelper;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Provides controls for setting options for profile tools.
 *
 */
public class ProfilePanel extends MapElementBasePanel {

	// Controls
	private ColorSelectionPanel colorList;
	private CoordTextField pALocation, pBLocation;
	private JButton saveAsCSV, openButton;
	private DoubleTextField lineWidthText;
	private JCheckBox endpointsCheckBox;

	// The profile
	private Profile profile;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public ProfilePanel(MapElementsPanel parent) {
		super(parent);
		icon = Profile.icon;
		type = "Profile";
		build(true, false, true);
	}

	@Override
	protected void build(boolean addNotes, boolean addLoc, boolean addCBs) {
		super.build(addNotes, addLoc, addCBs);

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Location A"));
		pALocation = new CoordTextField(22, "location of end point A", Landscape.format, true) {
			@Override
			public void doChange(ReadOnlyVector3 coord) {
				double z = Landscape.getInstance().getZ(coord.getX(), coord.getY());
				if (Double.isNaN(z)) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				if (Double.isNaN(coord.getZ())) {
					profile.setEndpointA(coord.getX(), coord.getY(), z);
				}
				else {
					profile.getMarkerA().setZOffset(z-coord.getZ(), false);
					profile.setEndpointA(coord.getX(), coord.getY(), coord.getZ());
				}
			}
		};
		CoordAction.listenerList.add(pALocation);
		panel.add(pALocation);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Location B"));
		pBLocation = new CoordTextField(22, "location of end point B", Landscape.format, true) {
			@Override
			public void doChange(ReadOnlyVector3 coord) {
				double z = Landscape.getInstance().getZ(coord.getX(), coord.getY());
				if (Double.isNaN(z)) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				if (Double.isNaN(coord.getZ())) {
					profile.setEndpointB(coord.getX(), coord.getY(), z);
				}
				else {
					profile.getMarkerB().setZOffset(z-coord.getZ(), false);
					profile.setEndpointB(coord.getX(), coord.getY(), coord.getZ());
				}
			}
		};
		CoordAction.listenerList.add(pBLocation);
		panel.add(pBLocation);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Color"));
		colorList = new ColorSelectionPanel(Profile.defaultColor) {
			@Override
			public void doColor(Color color) {
				profile.setColor(color);
			}
		};
		panel.add(colorList);

		panel.add(new JLabel("Line Width", SwingConstants.RIGHT));
		lineWidthText = new DoubleTextField(8, Profile.defaultLineWidth, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				profile.setLineWidth((float) value);
			}
		};
		panel.add(lineWidthText);
		
		endpointsCheckBox = new JCheckBox("Show Endpoints");
		endpointsCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				profile.setEndpointsVisible(endpointsCheckBox.isSelected());
				profile.markDirty(DirtyType.RenderState);
			}
		});
		panel.add(endpointsCheckBox);

		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		saveAsCSV = new JButton("Save As CSV");
		saveAsCSV.setToolTipText("save profile data formatted as comma separated values");
		saveAsCSV.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String fileName = FileHelper.getCSVFile();
				if (fileName == null) {
					return;
				}
				profile.saveAsCsv(fileName);
			}
		});
		saveAsCSV.setEnabled(false);
		panel.add(saveAsCSV);
		openButton = new JButton("Open Graph");
		openButton.setToolTipText("show the profile as a graph in a separate window");
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				profile.getState().getViewData().setVisible(true);
				profile.getState().open();
			}
		});
		openButton.setEnabled(false);
		panel.add(openButton);
		contents.add(panel);
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		this.mapElement = mapElement;
		profile = (Profile) mapElement;
		setLocation(pALocation, profile.getEndpointA());
		setLocation(pBLocation, profile.getEndpointB());
		pinnedCheckBox.setSelected(profile.isPinned());
		endpointsCheckBox.setSelected(profile.isEndpointsVisible());
		lineWidthText.setValue(profile.getLineWidth());
		nameLabel.setText(profile.getName());
		colorList.setColor(profile.getColor());
		noteText.setText(profile.getState().getAnnotation());
		labelCheckBox.setSelected(profile.isLabelVisible());
		saveAsCSV.setEnabled(true);
		openButton.setEnabled(true);
	}

	@Override
	public void updateLocation(MapElement mapElement) {
		setLocation(pALocation, profile.getEndpointA());
		setLocation(pBLocation, profile.getEndpointB());
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
