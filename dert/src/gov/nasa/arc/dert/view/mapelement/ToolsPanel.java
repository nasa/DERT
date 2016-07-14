package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.CartesianGrid;
import gov.nasa.arc.dert.scene.tool.Grid;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scene.tool.Path.BodyType;
import gov.nasa.arc.dert.scene.tool.Plane;
import gov.nasa.arc.dert.scene.tool.Profile;
import gov.nasa.arc.dert.scene.tool.RadialGrid;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCameraInfoManager;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.FieldCameraState;
import gov.nasa.arc.dert.state.GridState;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.PathState;
import gov.nasa.arc.dert.state.PlaneState;
import gov.nasa.arc.dert.state.ProfileState;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.ui.GroupPanel;
import gov.nasa.arc.dert.util.ColorMap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides controls for setting Tool preferences and adding Tools.
 *
 */
public class ToolsPanel extends JPanel {

	/**
	 * Constructor
	 */
	public ToolsPanel() {
		super();
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel(new GridLayout(3, 1));

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Add:"));

		JButton newButton = new JButton(Icons.getImageIcon("path.png"));
		newButton.setToolTipText("Path");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				PathState state = new PathState(position);
				Path path = (Path) ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state);
				newMapElement(MapElementState.Type.Path, path);
				Dert.getWorldView().getScenePanel().getInputHandler().setPath(path);
			}
		});
		panel.add(newButton);

		newButton = new JButton(Icons.getImageIcon("plane.png"));
		newButton.setToolTipText("Plane");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				PlaneState state = new PlaneState(position);
				Plane plane = (Plane) ConfigurationManager.getInstance().getCurrentConfiguration()
					.addMapElementState(state);
				newMapElement(MapElementState.Type.Plane, plane);
			}
		});
		panel.add(newButton);

		newButton = new JButton(Icons.getImageIcon("cartesiangrid.png"));
		newButton.setToolTipText("Cartesian Grid");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				GridState state = GridState.createCartesianGridState(position);
				CartesianGrid grid = (CartesianGrid) ConfigurationManager.getInstance().getCurrentConfiguration()
					.addMapElementState(state);
				newMapElement(MapElementState.Type.CartesianGrid, grid);
			}
		});
		panel.add(newButton);

		newButton = new JButton(Icons.getImageIcon("radialgrid.png"));
		newButton.setToolTipText("Radial Grid");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				GridState state = GridState.createRadialGridState(position);
				RadialGrid grid = (RadialGrid) ConfigurationManager.getInstance().getCurrentConfiguration()
					.addMapElementState(state);
				newMapElement(MapElementState.Type.RadialGrid, grid);
			}
		});
		panel.add(newButton);

		newButton = new JButton(Icons.getImageIcon("fieldcamera.png"));
		newButton.setToolTipText("Camera");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				FieldCameraState state = new FieldCameraState(position);
				FieldCamera fieldCamera = (FieldCamera) ConfigurationManager.getInstance().getCurrentConfiguration()
					.addMapElementState(state);
				newMapElement(MapElementState.Type.FieldCamera, fieldCamera);
				Landscape.getInstance().getLayerManager().addFieldCamera(fieldCamera);
			}
		});
		panel.add(newButton);

		newButton = new JButton(Icons.getImageIcon("profile.png"));
		newButton.setToolTipText("Profile");
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ReadOnlyVector3 position = World.getInstance().getMarble().getTranslation();
				ProfileState state = new ProfileState(position);
				Profile profile = (Profile) ConfigurationManager.getInstance().getCurrentConfiguration()
					.addMapElementState(state);
				newMapElement(MapElementState.Type.Profile, profile);
			}
		});
		panel.add(newButton);
		topPanel.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("All Tools:"));
		JButton hideAllButton = new JButton("Hide");
		hideAllButton.setToolTipText("hide all tools");
		hideAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setAllVisible(false);
			}
		});
		panel.add(hideAllButton);

		JButton showAllButton = new JButton("Show");
		showAllButton.setToolTipText("show all tools");
		showAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setAllVisible(true);
			}
		});
		panel.add(showAllButton);
		topPanel.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Tools Preferences"));
		topPanel.add(panel);

		add(topPanel, BorderLayout.NORTH);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

		// Path Preferences
		GroupPanel gPanel = new GroupPanel("Path");
		gPanel.setLayout(new GridLayout(5, 2));

		gPanel.add(new JLabel("Label", SwingConstants.RIGHT));
		JCheckBox checkBox = new JCheckBox("visible");
		checkBox.setSelected(Path.defaultLabelVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Path.defaultLabelVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		gPanel.add(checkBox);

		gPanel.add(new JLabel("Waypoints", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(Path.defaultWaypointsVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Path.defaultWaypointsVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		gPanel.add(checkBox);

		gPanel.add(new JLabel("Type", SwingConstants.RIGHT));
		JComboBox comboBox = new JComboBox(BodyType.values());
		comboBox.setSelectedItem(Path.defaultBodyType);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Path.defaultBodyType = (BodyType) ((JComboBox) event.getSource()).getSelectedItem();
			}
		});
		gPanel.add(comboBox);

		gPanel.add(new JLabel("Color", SwingConstants.RIGHT));
		ColorSelectionPanel colorList = new ColorSelectionPanel(Path.defaultColor) {
			@Override
			public void doColor(Color color) {
				Path.defaultColor = color;
			}
		};
		gPanel.add(colorList);

		gPanel.add(new JLabel("Linewidth", SwingConstants.RIGHT));
		DoubleTextField ptlwText = new DoubleTextField(8, Path.defaultLineWidth, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				Path.defaultLineWidth = (float)value;
			}
		};
		gPanel.add(ptlwText);

		bottomPanel.add(gPanel);

		// Plane Preferences
		gPanel = new GroupPanel("Plane");
		gPanel.setLayout(new GridLayout(4, 2));

		gPanel.add(new JLabel("Label", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(Plane.defaultLabelVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Plane.defaultLabelVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		gPanel.add(checkBox);

		gPanel.add(new JLabel("Color Map", SwingConstants.RIGHT));
		comboBox = new JComboBox(ColorMap.getColorMapNames());
		comboBox.setSelectedItem(Plane.defaultColorMap);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Plane.defaultColorMap = (String) ((JComboBox) event.getSource()).getSelectedItem();
			}
		});
		gPanel.add(comboBox);

		gPanel.add(new JLabel("Color", SwingConstants.RIGHT));
		colorList = new ColorSelectionPanel(Plane.defaultColor) {
			@Override
			public void doColor(Color color) {
				Plane.defaultColor = color;
			}
		};
		gPanel.add(colorList);

		gPanel.add(new JLabel("Strike Format", SwingConstants.RIGHT));
		checkBox = new JCheckBox("compass bearing");
		checkBox.setSelected(Plane.strikeAsCompassBearing);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Plane.strikeAsCompassBearing = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		gPanel.add(checkBox);

		bottomPanel.add(gPanel);

		// Cartesian Grid Preferences
		gPanel = new GroupPanel("Cartesian Grid");
		gPanel.setLayout(new GridLayout(6, 2));

		gPanel.add(new JLabel("Label", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(CartesianGrid.defaultLabelVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				CartesianGrid.defaultLabelVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		gPanel.add(checkBox);

		gPanel.add(new JLabel("Color", SwingConstants.RIGHT));
		colorList = new ColorSelectionPanel(CartesianGrid.defaultColor) {
			@Override
			public void doColor(Color color) {
				CartesianGrid.defaultColor = color;
			}
		};
		gPanel.add(colorList);

		gPanel.add(new JLabel("Columns", SwingConstants.RIGHT));
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(CartesianGrid.defaultColumns, 1, 1000000, 1));
		gPanel.add(spinner);
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				CartesianGrid.defaultColumns = (Integer) ((JSpinner) event.getSource()).getValue();
			}
		});

		gPanel.add(new JLabel("Rows", SwingConstants.RIGHT));
		spinner = new JSpinner(new SpinnerNumberModel(CartesianGrid.defaultRows, 1, 1000000, 1));
		gPanel.add(spinner);
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				CartesianGrid.defaultRows = (Integer) ((JSpinner) event.getSource()).getValue();
			}
		});

		gPanel.add(new JLabel("Cell Size", SwingConstants.RIGHT));
		DoubleTextField sizeText = new DoubleTextField(8, Grid.defaultCellSize, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				Grid.defaultCellSize = value;
			}
		};
		gPanel.add(sizeText);

		gPanel.add(new JLabel("Linewidth", SwingConstants.RIGHT));
		DoubleTextField clwText = new DoubleTextField(8, CartesianGrid.defaultLineWidth, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				CartesianGrid.defaultLineWidth = (float)value;
			}
		};
		gPanel.add(clwText);

		bottomPanel.add(gPanel);

		// Radial Grid Preferences
		gPanel = new GroupPanel("Radial Grid");
		gPanel.setLayout(new GridLayout(6, 2));

		gPanel.add(new JLabel("Label", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(RadialGrid.defaultLabelVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				RadialGrid.defaultLabelVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		gPanel.add(checkBox);

		gPanel.add(new JLabel("Color", SwingConstants.RIGHT));
		colorList = new ColorSelectionPanel(RadialGrid.defaultColor) {
			@Override
			public void doColor(Color color) {
				RadialGrid.defaultColor = color;
			}
		};
		gPanel.add(colorList);

		gPanel.add(new JLabel("Rings", SwingConstants.RIGHT));
		spinner = new JSpinner(new SpinnerNumberModel(RadialGrid.defaultRings, 1, 1000000, 1));
		gPanel.add(spinner);
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				RadialGrid.defaultRings = (Integer) ((JSpinner) event.getSource()).getValue();
			}
		});

		gPanel.add(new JLabel("Distance", SwingConstants.RIGHT));
		sizeText = new DoubleTextField(8, RadialGrid.defaultRadius, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				RadialGrid.defaultRadius = value;
			}
		};
		gPanel.add(sizeText);

		gPanel.add(new JLabel("Linewidth", SwingConstants.RIGHT));
		DoubleTextField rlwText = new DoubleTextField(8, RadialGrid.defaultLineWidth, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				RadialGrid.defaultLineWidth = (float)value;
			}
		};
		gPanel.add(rlwText);

		gPanel.add(new JLabel("Rose", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(RadialGrid.defaultCompassRose);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				RadialGrid.defaultCompassRose = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		gPanel.add(checkBox);

		bottomPanel.add(gPanel);

		// FieldCamera Preferences
		gPanel = new GroupPanel("Camera");
		gPanel.setLayout(new GridLayout(5, 2));

		gPanel.add(new JLabel("Label", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(FieldCamera.defaultLabelVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				FieldCamera.defaultLabelVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		gPanel.add(checkBox);

		gPanel.add(new JLabel("Color", SwingConstants.RIGHT));
		colorList = new ColorSelectionPanel(FieldCamera.defaultColor) {
			@Override
			public void doColor(Color color) {
				FieldCamera.defaultColor = color;
			}
		};
		gPanel.add(colorList);

		gPanel.add(new JLabel("FOV", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(FieldCamera.defaultFovVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				FieldCamera.defaultFovVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		gPanel.add(checkBox);

		gPanel.add(new JLabel("LookAt Line", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(FieldCamera.defaultLineVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				FieldCamera.defaultLineVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		gPanel.add(checkBox);

		gPanel.add(new JLabel("Definition", SwingConstants.RIGHT));
		comboBox = new JComboBox(FieldCameraInfoManager.getInstance().getFieldCameraNames());
		comboBox.setSelectedItem(FieldCamera.defaultDefinition);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				FieldCamera.defaultDefinition = (String) ((JComboBox) event.getSource()).getSelectedItem();
			}
		});
		gPanel.add(comboBox);
		bottomPanel.add(gPanel);

		// Profile Preferences
		gPanel = new GroupPanel("Profile");
		gPanel.setLayout(new GridLayout(3, 2));

		gPanel.add(new JLabel("Label", SwingConstants.RIGHT));
		checkBox = new JCheckBox("visible");
		checkBox.setSelected(Profile.defaultLabelVisible);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Profile.defaultLabelVisible = ((JCheckBox) event.getSource()).isSelected();
			}
		});
		gPanel.add(checkBox);

		gPanel.add(new JLabel("Color", SwingConstants.RIGHT));
		colorList = new ColorSelectionPanel(Profile.defaultColor) {
			@Override
			public void doColor(Color color) {
				Profile.defaultColor = color;
			}
		};
		gPanel.add(colorList);

		gPanel.add(new JLabel("Linewidth", SwingConstants.RIGHT));
		DoubleTextField plwText = new DoubleTextField(8, Profile.defaultLineWidth, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				Profile.defaultLineWidth = (float)value;
			}
		};
		gPanel.add(plwText);
		bottomPanel.add(gPanel);

		add(new JScrollPane(bottomPanel), BorderLayout.CENTER);
	}

	/**
	 * Create a new Tool. Overridden by implementing class.
	 * 
	 * @param type
	 * @param mapElement
	 */
	public void newMapElement(MapElementState.Type type, MapElement mapElement) {
		// nothing here
	}

	/**
	 * Set all Tools visibility. Overridden by implementing class.
	 * 
	 * @param visible
	 */
	public void setAllVisible(boolean visible) {
		// nothing here
	}
}
