package gov.nasa.arc.dert.view.viewpoint;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.ButtonAction;
import gov.nasa.arc.dert.action.edit.CoordAction;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.state.ViewpointState;
import gov.nasa.arc.dert.ui.CoordTextField;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import com.ardor3d.math.type.ReadOnlyVector3;

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
	private CoordTextField locationField;
	private Vector3TextField directionField;
//	private DoubleTextField distanceField;
	private DoubleArrayTextField azElField;
//	private DoubleTextField altitudeField;
	private DoubleTextField magnificationField;
	private CoordTextField corField;
	private ButtonAction prevAction;
	private ButtonAction nextAction;
	private JCheckBox hike;
	private JButton current, save;

	// Fields
	private Vector3 coord;
	private double[] azEl;

	// Viewpoint
	private ViewpointController controller;
	private ViewpointStore currentVPS, tempVPS;
	private ViewpointListCellRenderer cellRenderer;
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
		list.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				currentVPS = (ViewpointStore) list.getSelectedValue();
				deleteButton.setEnabled(currentVPS != null);
				if (currentVPS != null)
					controller.gotoViewpoint(currentVPS);
				updateData(true);
				setEditing(false);
			}
		});
		cellRenderer = new ViewpointListCellRenderer();
		list.setCellRenderer(cellRenderer);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().setView(list);
		scrollPane.setMinimumSize(new Dimension(128, 128));
		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.add(scrollPane, BorderLayout.CENTER);
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		prevAction = new ButtonAction("Go to the previous viewpoint", null, "prev_16.png") {
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
		nextAction = new ButtonAction("Go to the next viewpoint", null, "next_16.png") {
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
				int[] vpList = list.getSelectedIndices();
				String str = currentVPS.toString();
				if (vpList.length > 1)
					str += ". . . ";
				int answer = JOptionPane.showConfirmDialog(Dert.getMainWindow(), "Delete " + str + "?",
					"Confirm Delete", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
					Icons.getImageIcon("delete.png"));
				if (answer == JOptionPane.OK_OPTION) {
					setEditing(false);
					int index = controller.removeViewpoints(vpList);
					list.setListData(viewpointList);
					list.setSelectedIndex(index);
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
				ArrayList<Path> pathList = World.getInstance().getTools().getFlyablePaths();
				if (pathList.size() == 0) {
					JOptionPane.showMessageDialog(state.getViewData().getViewWindow(), "No flyable Paths available.", "Fly Path",
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
		tipText = "toggle hike mode";
		label = new JLabel("On Foot", SwingConstants.RIGHT);
		label.setToolTipText(tipText);
		panel.add(label);
		hike = new JCheckBox("");
		hike.setSelected(controller.getViewpointNode().isHikeMode());
		hike.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (!controller.getViewpointNode().setHikeMode(hike.isSelected())) {
					Toolkit.getDefaultToolkit().beep();
					hike.setSelected(!hike.isSelected());
				}
				else {
					setEditing(true);
					controller.updateLookAt();
				}
			}
		});
		panel.add(hike);
		current = new JButton("Current");
		current.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setEditing(true);
				updateData(true);
			}
		});
		panel.add(current);
		save = new JButton("Save");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (currentVPS != null) {
					controller.getViewpointNode().getViewpoint(currentVPS);
					updateData(true);
					setEditing(false);
				}
			}
		});
		panel.add(save);
		dataPanel.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		tipText = "location of viewpoint";
		label = new JLabel("Location", SwingConstants.RIGHT);
		label.setToolTipText(tipText);
		panel.add(label);
		locationField = new CoordTextField(20, tipText, "0.000", true) {
			@Override
			public void doChange(ReadOnlyVector3 loc) {
				if (!controller.getViewpointNode().changeLocation(loc)) {
					Toolkit.getDefaultToolkit().beep();
				}
				else
					setEditing(true);
			}
		};
		CoordAction.listenerList.add(locationField);
		panel.add(locationField);
		dataPanel.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		tipText = "Vector pointing to center of rotation from viewpoint (X,Y,Z)";
		label = new JLabel("Direction Vector", SwingConstants.RIGHT);
		label.setToolTipText(tipText);
		panel.add(label);
		directionField = new Vector3TextField(20, new Vector3(), "0.000", true) {
			@Override
			public void handleChange(Vector3 dir) {
				controller.getViewpointNode().changeDirection(dir);
				setEditing(true);
				updateData(true);
			}
		};
		directionField.setToolTipText(tipText);
		panel.add(directionField);
		dataPanel.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		tipText = "Direction in degrees from N, degrees from hort";
		label = new JLabel("Direction Az,El", SwingConstants.RIGHT);
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
				setEditing(true);
				updateData(true);
			}
		};
		azElField.setToolTipText(tipText);
		panel.add(azElField);
		dataPanel.add(panel);

//		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//		tipText = "Distance from viewpoint to center of rotation";
//		label = new JLabel("Distance to CoR", SwingConstants.RIGHT);
//		label.setToolTipText(tipText);
//		panel.add(label);
//		distanceField = new DoubleTextField(20, 0, false, "0.000") {
//			@Override
//			protected void handleChange(double value) {
//				if (Double.isNaN(value)) {
//					return;
//				}
//				if (!controller.getViewpointNode().changeDistance(value)) {
//					Toolkit.getDefaultToolkit().beep();
//				}
//				else
//					setEditing(true);
//			}
//		};
//		distanceField.setToolTipText(tipText);
//		panel.add(distanceField);
//		dataPanel.add(panel);

//		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//		tipText = "Viewpoint height above terrain";
//		label = new JLabel("Height from Ground", SwingConstants.RIGHT);
//		label.setToolTipText(tipText);
//		panel.add(label);
//		altitudeField = new DoubleTextField(20, 0, false, "0.000") {
//			@Override
//			protected void handleChange(double value) {
//				if (Double.isNaN(value)) {
//					return;
//				}
//				if (!controller.getViewpointNode().changeAltitude(value)) {
//					Toolkit.getDefaultToolkit().beep();
//				}
//				else {
//					setEditing(true);
//					controller.updateLookAt();
//				}
//			}
//		};
//		altitudeField.setToolTipText(tipText);
//		panel.add(altitudeField);
//		dataPanel.add(panel);

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
				setEditing(true);
			}
		};
		magnificationField.setToolTipText(tipText);
		panel.add(magnificationField);
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
		coord.set(tempVPS.location);
		locationField.setLocalValue(coord);
		directionField.setValue(tempVPS.direction);
//		distanceField.setValue(tempVPS.distance);
		azEl[0] = Math.toDegrees(tempVPS.azimuth);
		azEl[1] = Math.toDegrees(tempVPS.elevation);
		azElField.setValue(azEl);
		magnificationField.setValue(BasicCamera.magFactor[tempVPS.magIndex]);
//		double alt = viewpointNode.getAltitude();
//		if (Double.isNaN(alt)) {
//			altitudeField.setText("N/A");
//		} else {
//			altitudeField.setValue(alt);
//		}
		hike.setSelected(tempVPS.hikeMode);
	}
	
	public void clearSelection() {
		list.clearSelection();
	}
	
	public void dispose() {
		if (locationField != null)
			CoordAction.listenerList.remove(locationField);
		if (corField != null)
			CoordAction.listenerList.remove(corField);
	}
	
	private void setEditing(boolean isEditing) {
		cellRenderer.setEditing(isEditing);
		list.repaint();
	}
}
