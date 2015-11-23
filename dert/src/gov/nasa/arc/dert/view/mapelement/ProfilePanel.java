package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.Profile;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.Vector3TextField;
import gov.nasa.arc.dert.util.FileHelper;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.ardor3d.math.Vector3;

/**
 * Provides controls for setting options for profile tools.
 *
 */
public class ProfilePanel extends MapElementBasePanel {

	// Controls
	private ColorSelectionPanel colorList;
	private Vector3TextField pALocation, pBLocation;
	private JLabel aElevLabel, bElevLabel;
	private JButton saveAsCSV, openButton;

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
		pALocation = new Vector3TextField(20, new Vector3(), Landscape.format, false) {
			@Override
			protected void handleChange(Vector3 coord) {
				Landscape landscape = World.getInstance().getLandscape();
				landscape.worldToLocalCoordinate(coord);
				coord.setZ(landscape.getZ(coord.getX(), coord.getY()));
				if (!coord.equals(profile.getEndpointA())) {
					profile.setEndpointA(coord);
				}
			}
		};
		panel.add(pALocation);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Elevation A"));
		aElevLabel = new JLabel("            ");
		panel.add(aElevLabel);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Location B"));
		pBLocation = new Vector3TextField(20, new Vector3(), Landscape.format, false) {
			@Override
			protected void handleChange(Vector3 coord) {
				Landscape landscape = World.getInstance().getLandscape();
				landscape.worldToLocalCoordinate(coord);
				coord.setZ(landscape.getZ(coord.getX(), coord.getY()));
				if (!coord.equals(profile.getEndpointB())) {
					profile.setEndpointB(coord);
				}
			}
		};
		panel.add(pBLocation);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Elevation B"));
		bElevLabel = new JLabel("            ");
		panel.add(bElevLabel);
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
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		saveAsCSV = new JButton("Save As CSV");
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
		setLocation(pALocation, aElevLabel, profile.getEndpointA());
		setLocation(pBLocation, bElevLabel, profile.getEndpointB());
		pinnedCheckBox.setSelected(profile.isPinned());
		nameLabel.setText(profile.getName());
		colorList.setColor(profile.getColor());
		noteText.setText(profile.getState().getAnnotation());
		labelCheckBox.setSelected(profile.isLabelVisible());
		saveAsCSV.setEnabled(true);
		openButton.setEnabled(true);
	}

	@Override
	public void updateLocation(MapElement mapElement) {
		setLocation(pALocation, aElevLabel, profile.getEndpointA());
		setLocation(pBLocation, bElevLabel, profile.getEndpointB());
	}

}
