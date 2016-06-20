package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scene.tool.Path.BodyType;
import gov.nasa.arc.dert.scene.tool.Path.LabelType;
import gov.nasa.arc.dert.scene.tool.Waypoint;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.util.FileHelper;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Provides controls for setting options for paths and waypoints.
 *
 */
public class PathPanel extends MapElementBasePanel {

	// Controls
	private ColorSelectionPanel colorList;
	private JComboBox typeCombo;
	private JComboBox labelCombo;
	private JButton saveAsCSV, addPoints, statistics;
	private JCheckBox showWaypoints;
	private DoubleTextField lineWidthText;

	// Current map element
	private Path path;
	private Waypoint waypoint;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public PathPanel(MapElementsPanel parent) {
		super(parent);
		icon = Path.icon;
		type = "Path";
		build(true, true, true);
	}

	@Override
	protected void build(boolean addNotes, boolean addLoc, boolean addCBs) {
		super.build(addNotes, addLoc, addCBs);

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Type"));
		typeCombo = new JComboBox(Path.BodyType.values());
		typeCombo.setToolTipText("select path body type");
		typeCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				path.setBodyType((BodyType) typeCombo.getSelectedItem());
			}
		});
		panel.add(typeCombo);

		showWaypoints = new JCheckBox("Show Waypoints");
		showWaypoints.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				path.setWaypointsVisible(showWaypoints.isSelected());
			}
		});
		panel.add(showWaypoints);

		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Label"));
		labelCombo = new JComboBox(Path.LabelType.values());
		labelCombo.setToolTipText("select type of label");
		labelCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				path.setLabelType((LabelType) labelCombo.getSelectedItem());
			}
		});
		panel.add(labelCombo);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Color"));
		colorList = new ColorSelectionPanel(Path.defaultColor) {
			@Override
			public void doColor(Color color) {
				path.setColor(color);
			}
		};
		panel.add(colorList);

		panel.add(new JLabel("        "));

		panel.add(new JLabel("Line Width", SwingConstants.RIGHT));
		lineWidthText = new DoubleTextField(8, Path.defaultLineWidth, true, Landscape.format) {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				path.setLineWidth((float) value);
			}
		};
		panel.add(lineWidthText);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		saveAsCSV = new JButton("Save As CSV");
		saveAsCSV.setToolTipText("save waypoints formatted as comma separated values");
		saveAsCSV.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String fileName = FileHelper.getCSVFile();
				if (fileName == null) {
					return;
				}
				path.saveAsCsv(fileName);
			}
		});
		panel.add(saveAsCSV);
		addPoints = new JButton("Add Points");
		addPoints.setToolTipText("add points to the path");;
		addPoints.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				path.setCurrentWaypoint(waypoint);
				Dert.getWorldView().getScenePanel().getInputHandler().setPath(path);
			}
		});
		panel.add(addPoints);
		statistics = new JButton("Open Info");
		statistics.setToolTipText("display path statistics in a separate window");
		statistics.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				path.getState().getViewData().setVisible(true);
				path.getState().open();
			}
		});
		panel.add(statistics);
		contents.add(panel);
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		this.mapElement = mapElement;
		// map element is a Path
		if (mapElement instanceof Path) {
			typeLabel.setIcon(icon);
			path = (Path) mapElement;
			waypoint = null;
			labelCombo.setSelectedItem(path.getLabelType());
			pinnedCheckBox.setSelected(path.isPinned());
			nameLabel.setText(path.getName());
			colorList.setColor(path.getColor());
			noteText.setText(path.getState().getAnnotation());
			typeCombo.setSelectedItem(path.getBodyType());
			labelCheckBox.setSelected(path.isLabelVisible());
			showWaypoints.setSelected(path.areWaypointsVisible());
			lineWidthText.setValue(path.getLineWidth());

			locationText.setEnabled(false);
			labelCombo.setEnabled(true);
			pinnedCheckBox.setEnabled(true);
			colorList.setEnabled(true);
			noteText.setEnabled(true);
			labelCheckBox.setEnabled(true);
			typeCombo.setEnabled(true);
			saveAsCSV.setEnabled(true);
			addPoints.setEnabled(true);
			statistics.setEnabled(true);
			showWaypoints.setEnabled(true);
			lineWidthText.setEnabled(true);
		}
		// map element is a Waypoint
		else {
			typeLabel.setIcon(Waypoint.icon);
			waypoint = (Waypoint) mapElement;
			path = waypoint.getPath();
			nameLabel.setText(waypoint.getName());
			setLocation(locationText, elevLabel, waypoint.getTranslation());
			labelCombo.setSelectedItem(path.getLabelType());
			pinnedCheckBox.setSelected(path.isPinned());
			colorList.setColor(path.getColor());
			noteText.setText(waypoint.getState().getAnnotation());
			typeCombo.setSelectedItem(path.getBodyType());
			labelCheckBox.setSelected(path.isLabelVisible());
			showWaypoints.setSelected(path.areWaypointsVisible());
			lineWidthText.setValue(path.getLineWidth());

			locationText.setEnabled(true);
			labelCombo.setEnabled(false);
			pinnedCheckBox.setEnabled(false);
			colorList.setEnabled(false);
			noteText.setEnabled(true);
			labelCheckBox.setEnabled(false);
			typeCombo.setEnabled(false);
			saveAsCSV.setEnabled(false);
			addPoints.setEnabled(true);
			statistics.setEnabled(false);
			showWaypoints.setEnabled(false);
			lineWidthText.setEnabled(false);
		}
	}

}
