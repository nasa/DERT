package gov.nasa.arc.dert.view.viewpoint;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.ButtonAction;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.state.ViewpointState;
import gov.nasa.arc.dert.ui.DoubleArrayTextField;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.ui.Vector3TextField;
import gov.nasa.arc.dert.viewpoint.BasicCamera;
import gov.nasa.arc.dert.viewpoint.ViewpointController;
import gov.nasa.arc.dert.viewpoint.ViewpointNode;
import gov.nasa.arc.dert.viewpoint.ViewpointStore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.ardor3d.math.Vector3;

/**
 * Provides content for the ViewpointView.
 *
 */
public class ViewpointPanel extends JPanel {

	// Controls
	private JList list;
	private ButtonAction addButton, deleteButton, flyListButton, flyPathButton;
	private JSplitPane splitPane;
	private JPanel buttonBar;
	private Vector3TextField locationField;
	private Vector3TextField directionField;
	private DoubleTextField distanceField;
	private DoubleArrayTextField azElField;
	private DoubleTextField altitudeField;
	private DoubleTextField magnificationField;
	private DoubleArrayTextField corField;
	private ButtonAction prevAction;
	private ButtonAction nextAction;

	// Fields
	private Vector3 coord;
	private double[] azEl;

	// Viewpoint
	private ViewpointController controller;
	private ViewpointStore currentVPS, tempVPS;
	private Vector<ViewpointStore> viewpointList;
	
	private ViewpointState state;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public ViewpointPanel(ViewpointState vState) {
		super();
		state = vState;
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		controller = Dert.getWorldView().getScenePanel().getViewpointController();
		viewpointList = state.getViewpointList();
		controller.setViewpointList(viewpointList);
		controller.setFlyParams(state.getFlyParams());
		coord = new Vector3();
		azEl = new double[2];
		tempVPS = new ViewpointStore();

		// Add the viewpoint list
		setLayout(new BorderLayout());
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		add(splitPane, BorderLayout.CENTER);
		list = new JList(viewpointList);
		list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				currentVPS = (ViewpointStore) list.getSelectedValue();
				deleteButton.setEnabled(currentVPS != null);
				if (currentVPS != null) {
					controller.gotoViewpoint(currentVPS);
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().setView(list);
		scrollPane.setMinimumSize(new Dimension(128, 128));
		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.add(scrollPane, BorderLayout.CENTER);
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		prevAction = new ButtonAction("Go to the previous viewpoint", null, "prev_20.png") {
			@Override
			protected void run() {
				int index = list.getSelectedIndex();
				if (index < 0) {
					index = 0;
				} else if (index == 0) {
					index = viewpointList.size() - 1;
				} else {
					index--;
				}
				list.setSelectedIndex(index);
			}
		};
		topPanel.add(prevAction);
		nextAction = new ButtonAction("Go to the next viewpoint", null, "next_20.png") {
			@Override
			protected void run() {
				int index = list.getSelectedIndex();
				if (index < 0) {
					index = 0;
				} else if (index == viewpointList.size() - 1) {
					index = 0;
				} else {
					index++;
				}
				list.setSelectedIndex(index);
			}
		};
		topPanel.add(nextAction);
		listPanel.add(topPanel, BorderLayout.NORTH);
		splitPane.setLeftComponent(listPanel);

		// Add the viewpoint attributes panel
		splitPane.setRightComponent(createDataPanel());

		// Add buttons panel
		buttonBar = new JPanel();
		buttonBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
		addButton = new ButtonAction("Add the current viewpoint to the list", "Add", null) {
			@Override
			public void run() {
				String answer = JOptionPane.showInputDialog(null, "Please enter a name for this viewpoint.",
					"Viewpoint" + viewpointList.size());
				if (answer != null) {
					int index = list.getSelectedIndex();
					if (index < 0) {
						index = viewpointList.size();
					} else {
						index++;
					}
					controller.addViewpoint(index, answer);
					list.setListData(viewpointList);
					list.setSelectedIndex(index);
					flyListButton.setEnabled(viewpointList.size() > 1);
				}
			}
		};
		buttonBar.add(addButton);
		deleteButton = new ButtonAction("Delete the selected viewpoint", "Delete", null) {
			@Override
			public void run() {
				int answer = JOptionPane.showConfirmDialog(Dert.getMainWindow(), "Delete " + currentVPS + "?",
					"Confirm Delete", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
					Icons.getImageIcon("delete.png"));
				if (answer == JOptionPane.OK_OPTION) {
					int index = list.getSelectedIndex();
					controller.removeViewpoint(currentVPS);
					list.setListData(viewpointList);
					if (index >= viewpointList.size()) {
						list.setSelectedIndex(-1);
					} else {
						list.setSelectedIndex(index);
					}
					flyListButton.setEnabled(viewpointList.size() > 1);
				}
			}
		};
		deleteButton.setEnabled(false);
		buttonBar.add(deleteButton);
		flyListButton = new ButtonAction("Fly through the viewpoint list", "Fly List", null) {
			@Override
			public void run() {
				controller.flyThrough(null, (JDialog)state.getViewData().getViewWindow());
			}
		};
		flyListButton.setEnabled(viewpointList.size() > 1);
		buttonBar.add(flyListButton);
		flyPathButton = new ButtonAction("Fly through the waypoints on a path", "Fly Path", null) {
			@Override
			public void run() {
				ArrayList<Path> pathList = World.getInstance().getTools().getPaths();
				if (pathList.size() == 0) {
					JOptionPane.showMessageDialog(state.getViewData().getViewWindow(), "No Paths available.", "Fly Path",
						JOptionPane.INFORMATION_MESSAGE, Icons.getImageIcon("path.png"));
					return;
				}
				Path[] paths = new Path[pathList.size()];
				pathList.toArray(paths);
				Path path = (Path) JOptionPane.showInputDialog(state.getViewData().getViewWindow(), "Select Path", "Fly Path",
					JOptionPane.QUESTION_MESSAGE, Icons.getImageIcon("path.png"), paths, paths[0]);
				if (path != null) {
					controller.flyThrough(path, (JDialog)state.getViewData().getViewWindow());
				}
			}
		};
		buttonBar.add(flyPathButton);
		add(buttonBar, BorderLayout.NORTH);
	}

	private JPanel createDataPanel() {
		JPanel panel = null;
		String tipText = null;
		JLabel label = null;
		JPanel dataPanel = new JPanel();
		BoxLayout boxLayout = new BoxLayout(dataPanel, BoxLayout.Y_AXIS);
		dataPanel.setLayout(boxLayout);

		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		tipText = "Location of viewpoint in East(+X), North(+Y), Elevation(+Z) coordinates";
		label = new JLabel("Location", SwingConstants.RIGHT);
		label.setToolTipText(tipText);
		panel.add(label);
		locationField = new Vector3TextField(20, new Vector3(), "0.000", true) {
			@Override
			protected void handleChange(Vector3 loc) {
				Landscape landscape = World.getInstance().getLandscape();
				landscape.worldToLocalCoordinate(loc);
				if (!controller.getViewpointNode().changeLocation(loc)) {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		};
		locationField.setToolTipText(tipText);
		panel.add(locationField);
		dataPanel.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		tipText = "Vector pointing to center of rotation from viewpoint (X,Y,Z)";
		label = new JLabel("Direction", SwingConstants.RIGHT);
		label.setToolTipText(tipText);
		panel.add(label);
		directionField = new Vector3TextField(20, new Vector3(), "0.000", true) {
			@Override
			protected void handleChange(Vector3 dir) {
				controller.getViewpointNode().changeDirection(dir);
			}
		};
		directionField.setToolTipText(tipText);
		panel.add(directionField);
		dataPanel.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		tipText = "Direction in degress from N, degrees from hort";
		label = new JLabel("Az,El", SwingConstants.RIGHT);
		label.setToolTipText(tipText);
		panel.add(label);
		azElField = new DoubleArrayTextField(20, new double[2], "0.00") {
			@Override
			protected void handleChange(double[] azel) {
				if (azel == null) {
					return;
				}
				controller.getViewpointNode().changeAzimuthAndElevation(Math.toRadians(azel[0]),
					Math.toRadians(90 + azel[1]));
			}
		};
		azElField.setToolTipText(tipText);
		panel.add(azElField);
		dataPanel.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		tipText = "Distance from viewpoint to center of rotation";
		label = new JLabel("Distance", SwingConstants.RIGHT);
		label.setToolTipText(tipText);
		panel.add(label);
		distanceField = new DoubleTextField(20, 0, false, "0.000") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				if (!controller.getViewpointNode().changeDistance(value)) {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		};
		distanceField.setToolTipText(tipText);
		panel.add(distanceField);
		dataPanel.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		tipText = "Viewpoint altitude above ground level";
		label = new JLabel("Altitude", SwingConstants.RIGHT);
		label.setToolTipText(tipText);
		panel.add(label);
		altitudeField = new DoubleTextField(20, 0, false, "0.000") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				if (!controller.getViewpointNode().changeAltitude(value)) {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		};
		altitudeField.setToolTipText(tipText);
		panel.add(altitudeField);
		dataPanel.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		tipText = "Magnification scale factor";
		label = new JLabel("Magnification", SwingConstants.RIGHT);
		label.setToolTipText(tipText);
		panel.add(label);
		magnificationField = new DoubleTextField(20, 0, false, "0.0") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				controller.getViewpointNode().changeMagnification(value);
			}
		};
		magnificationField.setToolTipText(tipText);
		panel.add(magnificationField);
		dataPanel.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		tipText = "Location of center of rotation in East(+X), North(+Y), Elevation(+Z) coordinates";
		label = new JLabel("Cntr of Rot", SwingConstants.RIGHT);
		label.setToolTipText(tipText);
		panel.add(label);
		corField = new DoubleArrayTextField(20, new double[3], "0.000");
		corField.setToolTipText(tipText);
		corField.setEditable(false);
		corField.setBackground(panel.getBackground());
		panel.add(corField);
		dataPanel.add(panel);

		updateData(false);
		return (dataPanel);
	}

	/**
	 * Viewpoint moved. Update the attributes panel.
	 */
	public void updateData(boolean vpSelected) {
		if (!vpSelected && (currentVPS != null))
			list.clearSelection();
		ViewpointNode viewpointNode = controller.getViewpointNode();
		viewpointNode.getViewpoint(tempVPS);
		Landscape landscape = World.getInstance().getLandscape();
		coord.set(tempVPS.location);
		landscape.localToWorldCoordinate(coord);
		locationField.setValue(coord);
		directionField.setValue(tempVPS.direction);
		distanceField.setValue(tempVPS.distance);
		azEl[0] = Math.toDegrees(tempVPS.azimuth);
		azEl[1] = Math.toDegrees(tempVPS.elevation);
		azElField.setValue(azEl);
		magnificationField.setValue(BasicCamera.magFactor[tempVPS.magIndex]);
		double alt = viewpointNode.getAltitude();
		if (Double.isNaN(alt)) {
			altitudeField.setText("N/A");
		} else {
			altitudeField.setValue(alt);
		}
		coord.set(tempVPS.lookAt);
		landscape.localToWorldCoordinate(coord);
		corField.setValue(coord);
	}
	
	public void clearSelection() {
		list.clearSelection();
	}
}
