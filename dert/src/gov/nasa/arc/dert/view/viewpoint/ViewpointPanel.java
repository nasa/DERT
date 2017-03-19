package gov.nasa.arc.dert.view.viewpoint;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.ButtonAction;
import gov.nasa.arc.dert.action.edit.CoordAction;
import gov.nasa.arc.dert.state.ViewpointState;
import gov.nasa.arc.dert.ui.CoordTextField;
import gov.nasa.arc.dert.ui.DoubleArrayTextField;
import gov.nasa.arc.dert.ui.DoubleTextField;
import gov.nasa.arc.dert.ui.FieldPanel;
import gov.nasa.arc.dert.ui.OptionDialog;
import gov.nasa.arc.dert.ui.Vector3TextField;
import gov.nasa.arc.dert.viewpoint.BasicCamera;
import gov.nasa.arc.dert.viewpoint.ViewpointController;
import gov.nasa.arc.dert.viewpoint.ViewpointStore;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides content for the ViewpointView.
 *
 */
public class ViewpointPanel extends JPanel {

	// Controls
	private JList list;
	private ButtonAction addButton, deleteButton;
	private JSplitPane splitPane;
	private CoordTextField locationField;
	private Vector3TextField directionField;
	private DoubleArrayTextField azElField;
	private DoubleTextField magnificationField;
	private ButtonAction prevAction;
	private ButtonAction nextAction;
	private JButton current, save;

	// Fields
	private Vector3 coord;
	private double[] azEl;

	// Viewpoint
	private ViewpointController controller;
	private ViewpointStore currentVPS, tempVPS;
	private ViewpointListCellRenderer cellRenderer;
	private Vector<ViewpointStore> viewpointList;
	
	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public ViewpointPanel(ViewpointState vState) {
		super();
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		controller = Dert.getWorldView().getScenePanel().getViewpointController();
		viewpointList = controller.getViewpointList();
		coord = new Vector3();
		azEl = new double[2];
		tempVPS = new ViewpointStore();

		// Add the viewpoint list
		setLayout(new BorderLayout());
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		add(splitPane, BorderLayout.CENTER);
		list = new JList(viewpointList);
		list.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				Point p = new Point(event.getX(), event.getY());
				int index = list.locationToIndex(p);
				if (list.getCellBounds(index, index).contains(p)) {
					viewpointSelected(event.getClickCount());
				}
				else {
					// User clicked outside of list items -- clear selection.
					list.clearSelection();
					list.getSelectionModel().setAnchorSelectionIndex(-1);
					list.getSelectionModel().setLeadSelectionIndex(-1);
				}
			}
		});
		cellRenderer = new ViewpointListCellRenderer();
		list.setCellRenderer(cellRenderer);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().setView(list);
		scrollPane.setMinimumSize(new Dimension(128, 128));
		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.add(scrollPane, BorderLayout.CENTER);
		JToolBar buttonBar = new JToolBar();
		buttonBar.setFloatable(false);
		buttonBar.setBorder(BorderFactory.createEmptyBorder());
		prevAction = new ButtonAction("Go to the previous viewpoint", null, "prev_15.png", true) {
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
				viewpointSelected(2);
			}
		};
		buttonBar.add(prevAction);
		nextAction = new ButtonAction("Go to the next viewpoint", null, "next_15.png", true) {
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
				viewpointSelected(2);
			}
		};
		buttonBar.add(nextAction);
		addButton = new ButtonAction("Add the current viewpoint to the list", "+", null) {
			@Override
			public void run() {
				String answer = OptionDialog.showSingleInputDialog((JDialog)getTopLevelAncestor(), "Please enter a name for this viewpoint.",
						"Viewpoint"+viewpointList.size());
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
					viewpointSelected(1);
				}
			}
		};
		buttonBar.add(addButton);
		deleteButton = new ButtonAction("Delete the selected viewpoint", "-", null) {
			@Override
			public void run() {
				int[] vpList = list.getSelectedIndices();
				String str = currentVPS.toString();
				if (vpList.length > 1)
					str += ". . . ";
				boolean yes = OptionDialog.showDeleteConfirmDialog((JDialog)getTopLevelAncestor(), "Delete " + str + "?");
				if (yes) {
					setEditing(false);
					int index = controller.removeViewpoints(vpList);
					list.setListData(viewpointList);
					list.setSelectedIndex(index);
				}
			}
		};
		deleteButton.setEnabled(false);
		buttonBar.add(deleteButton);
		listPanel.add(buttonBar, BorderLayout.NORTH);
		splitPane.setLeftComponent(listPanel);

		// Add the viewpoint attributes panel
		splitPane.setRightComponent(createDataPanel());
	}

	private JPanel createDataPanel() {
		JPanel panel = null;
		JLabel label = null;
		JPanel dataPanel = new JPanel(new BorderLayout());

		panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		current = new JButton("Current");
		current.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setEditing(true);
				controller.getViewpointNode().getViewpoint(tempVPS);
				updateData(tempVPS);
			}
		});
		panel.add(current);
		save = new JButton("Save");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (currentVPS != null) {
					controller.getViewpointNode().getViewpoint(currentVPS);
					updateData(currentVPS);
					setEditing(false);
				}
			}
		});
		panel.add(save);
		dataPanel.add(panel, BorderLayout.NORTH);
		
		ArrayList<Component> compList = new ArrayList<Component>();
		label = new JLabel("VP Location", SwingConstants.RIGHT);
		label.setToolTipText("location of viewpoint");
		compList.add(label);
		locationField = new CoordTextField(20, "location of viewpoint", "0.000", true) {
			@Override
			public void doChange(ReadOnlyVector3 loc) {
				if (!controller.getViewpointNode().changeLocation(loc)) {
					Toolkit.getDefaultToolkit().beep();
				}
				else {
					setEditing(true);
					controller.getViewpointNode().getViewpoint(tempVPS);
					updateData(tempVPS);
				}
			}
		};
		CoordAction.listenerList.add(locationField);
		compList.add(locationField);

		label = new JLabel("Direction Vector", SwingConstants.RIGHT);
		label.setToolTipText("Vector pointing to center of rotation from viewpoint (X,Y,Z)");
		compList.add(label);
		directionField = new Vector3TextField(20, new Vector3(), "0.000", true) {
			@Override
			public void handleChange(Vector3 dir) {
				controller.getViewpointNode().changeDirection(dir);
				setEditing(true);
				controller.getViewpointNode().getViewpoint(tempVPS);
				updateData(tempVPS);
			}
		};
		directionField.setToolTipText("Vector pointing to center of rotation from viewpoint (X,Y,Z)");
		compList.add(directionField);

		label = new JLabel("Direction Az,El", SwingConstants.RIGHT);
		label.setToolTipText("Direction in degrees from N, degrees from hort");
		compList.add(label);
		azElField = new DoubleArrayTextField(20, new double[2], "0.00") {
			@Override
			protected void handleChange(double[] azel) {
				if (azel == null) {
					return;
				}
				controller.getViewpointNode().changeAzimuthAndElevation(Math.toRadians(azel[0]),
					Math.toRadians(azel[1]));
				setEditing(true);
				controller.getViewpointNode().getViewpoint(tempVPS);
				updateData(tempVPS);
			}
		};
		azElField.setToolTipText("Direction in degrees from N, degrees from hort");
		compList.add(azElField);

		label = new JLabel("Magnification", SwingConstants.RIGHT);
		label.setToolTipText("Magnification scale factor");
		compList.add(label);
		magnificationField = new DoubleTextField(20, 0, false, "0.0") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				controller.getViewpointNode().changeMagnification(value);
				setEditing(true);
				controller.getViewpointNode().getViewpoint(tempVPS);
				updateData(tempVPS);
			}
		};
		magnificationField.setToolTipText("Magnification scale factor");
		compList.add(magnificationField);
		
		dataPanel.add(new FieldPanel(compList), BorderLayout.CENTER);

		return (dataPanel);
	}

	/**
	 * Viewpoint moved. Update the attributes panel.
	 */
	public void updateData(ViewpointStore vps) {
		if (vps == null)
			return;
		coord.set(vps.location);
		locationField.setLocalValue(coord);
		directionField.setValue(vps.direction);
		azEl[0] = Math.toDegrees(vps.azimuth);
		azEl[1] = Math.toDegrees(vps.elevation);
		azElField.setValue(azEl);
		magnificationField.setValue(BasicCamera.magFactor[vps.magIndex]);
	}
	
	public void clearSelection() {
		list.clearSelection();
	}
	
	public void dispose() {
		if (locationField != null)
			CoordAction.listenerList.remove(locationField);
	}
	
	private void setEditing(boolean isEditing) {
		cellRenderer.setEditing(isEditing);
		list.repaint();
	}
	
	private void viewpointSelected(int clickCount) {
		currentVPS = (ViewpointStore) list.getSelectedValue();
		deleteButton.setEnabled(currentVPS != null);
		updateData(currentVPS);
		if ((currentVPS != null) && (clickCount == 2))
			controller.gotoViewpoint(currentVPS);
		setEditing(false);
	}
}
