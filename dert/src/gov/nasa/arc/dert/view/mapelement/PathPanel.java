package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scene.tool.Path.BodyType;
import gov.nasa.arc.dert.scene.tool.Path.LabelType;
import gov.nasa.arc.dert.scene.tool.Waypoint;
import gov.nasa.arc.dert.ui.DoubleTextField;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Provides controls for setting options for paths and waypoints.
 *
 */
public class PathPanel extends MapElementBasePanel {

	// Controls
	private JComboBox typeCombo;
	private JComboBox labelCombo;
	private JButton addPoints;
	private JCheckBox showWaypoints;
	private DoubleTextField lineWidthText;
	private DoubleTextField sizeText;

	// Current map element
	private Path path;
	private Waypoint waypoint;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public PathPanel(MapElement mapElement) {
		super(mapElement);
	}

	@Override
	protected void addFields(ArrayList<Component> compList) {
		super.addFields(compList);

		compList.add(new JLabel("Type", SwingConstants.RIGHT));
		typeCombo = new JComboBox(Path.BodyType.values());
		typeCombo.setToolTipText("select path body type");
		typeCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				path.setBodyType((BodyType) typeCombo.getSelectedItem());
			}
		});
		compList.add(typeCombo);

		compList.add(new JLabel("Waypoints", SwingConstants.RIGHT));
		showWaypoints = new JCheckBox("visible");
		showWaypoints.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				path.setWaypointsVisible(showWaypoints.isSelected());
			}
		});
		compList.add(showWaypoints);

		compList.add(new JLabel("Label", SwingConstants.RIGHT));
		labelCombo = new JComboBox(Path.LabelType.values());
		labelCombo.setToolTipText("select type of label");
		labelCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				path.setLabelType((LabelType) labelCombo.getSelectedItem());
			}
		});
		compList.add(labelCombo);

		compList.add(new JLabel("Line Width", SwingConstants.RIGHT));
		lineWidthText = new DoubleTextField(8, Path.defaultLineWidth, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				path.setLineWidth((float) value);
			}
		};
		compList.add(lineWidthText);

		compList.add(new JLabel("Point Size", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, Path.defaultSize, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				path.setPointSize(value);
			}
		};
		compList.add(sizeText);

		addPoints = new JButton("Add Points");
		addPoints.setToolTipText("add points to the path");;
		addPoints.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				path.setCurrentWaypoint(waypoint);
				Dert.getWorldView().getScenePanel().getInputHandler().setPath(path);
			}
		});
		compList.add(addPoints);
		compList.add(new JLabel(""));
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		super.setMapElement(mapElement);
		// map element is a Path
		if (mapElement instanceof Path) {
			path = (Path) mapElement;
			waypoint = null;
			labelCombo.setSelectedItem(path.getLabelType());
			typeCombo.setSelectedItem(path.getBodyType());
			showWaypoints.setSelected(path.areWaypointsVisible());
			lineWidthText.setValue(path.getLineWidth());
			sizeText.setValue(path.getSize());
			locLabel.setText("Location");
			locationText.setText("");
			locationText.setEnabled(false);			
			path.setCurrentWaypoint(null);
		}
		// map element is a Waypoint
		else {
			waypoint = (Waypoint) mapElement;
			locLabel.setText(waypoint.getName());
			path = waypoint.getPath();
			setLocation(locationText, locLabel, waypoint.getTranslation());
			locationText.setEnabled(true);
			path.setCurrentWaypoint(waypoint);
		}
	}

}
