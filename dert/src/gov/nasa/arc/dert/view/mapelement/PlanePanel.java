package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.action.edit.CoordAction;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.tool.Plane;
import gov.nasa.arc.dert.scene.tool.Profile;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.CoordTextField;
import gov.nasa.arc.dert.ui.DoubleSpinner;
import gov.nasa.arc.dert.ui.GroupPanel;
import gov.nasa.arc.dert.util.StringUtil;

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
import javax.swing.event.ChangeEvent;

import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Provides controls for setting options for plane tools.
 *
 */
public class PlanePanel extends MapElementBasePanel {

	// Controls
	private ColorSelectionPanel colorList;
	private CoordTextField p0Location, p1Location, p2Location;
	private JButton openButton;
	private DoubleSpinner lengthSpinner, widthSpinner;
	private JCheckBox triangleCheckBox;
	private JLabel strikeAndDip;

	// The plane
	private Plane plane;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public PlanePanel(MapElementsPanel parent) {
		super(parent);
		icon = Plane.icon;
		type = "Plane";
		build(true, false, true);
	}

	@Override
	protected void build(boolean addNotes, boolean addLoc, boolean addCBs) {
		super.build(addNotes, addLoc, addCBs);

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Point 0"));
		p0Location = new CoordTextField(22, "location of first point of triangle", Landscape.format, true) {
			@Override
			public void doChange(ReadOnlyVector3 coord) {
				if (Double.isNaN(coord.getZ())) {
					double z = Landscape.getInstance().getZ(coord.getX(), coord.getY());
					if (Double.isNaN(z)) {
						Toolkit.getDefaultToolkit().beep();
						return;
					}
					plane.setPoint(0, coord);
					plane.getMarker(0).setStrictZ(true);
				}
				else {
					plane.setPoint(0, coord);
					plane.getMarker(0).setStrictZ(false);
				}
			}			
		};
		CoordAction.listenerList.add(p0Location);
		panel.add(p0Location);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Point 1"));
		p1Location = new CoordTextField(22, "location of second point of triangle", Landscape.format, true) {
			@Override
			public void doChange(ReadOnlyVector3 coord) {
				if (Double.isNaN(coord.getZ())) {
					double z = Landscape.getInstance().getZ(coord.getX(), coord.getY());
					if (Double.isNaN(z)) {
						Toolkit.getDefaultToolkit().beep();
						return;
					}
					plane.setPoint(1, coord);
					plane.getMarker(1).setStrictZ(true);
				}
				else {
					plane.setPoint(1, coord);
					plane.getMarker(1).setStrictZ(false);
				}
			}			
		};
		CoordAction.listenerList.add(p1Location);
		panel.add(p1Location);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Point 2"));
		p2Location = new CoordTextField(22, "location of third point of triangle", Landscape.format, true) {
			@Override
			public void doChange(ReadOnlyVector3 coord) {
				if (Double.isNaN(coord.getZ())) {
					double z = Landscape.getInstance().getZ(coord.getX(), coord.getY());
					if (Double.isNaN(z)) {
						Toolkit.getDefaultToolkit().beep();
						return;
					}
					plane.setPoint(2, coord);
					plane.getMarker(2).setStrictZ(true);
				}
				else {
					plane.setPoint(2, coord);
					plane.getMarker(2).setStrictZ(false);
				}
			}			
		};
		CoordAction.listenerList.add(p2Location);
		panel.add(p2Location);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Color"));
		colorList = new ColorSelectionPanel(Profile.defaultColor) {
			@Override
			public void doColor(Color color) {
				plane.setColor(color);
			}
		};
		panel.add(colorList);
		triangleCheckBox = new JCheckBox("Show Triangle");
		triangleCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				plane.setTriangleVisible(triangleCheckBox.isSelected());
				plane.markDirty(DirtyType.RenderState);
			}
		});
		panel.add(triangleCheckBox);
		contents.add(panel);

		panel = new GroupPanel("Change Scale Along Axis");
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Dip", SwingConstants.RIGHT));
		lengthSpinner = new DoubleSpinner(Plane.defaultSize, 1, 10000, 1, false) {
			@Override
			public void stateChanged(ChangeEvent event) {
				double lengthScale = ((Double) lengthSpinner.getValue());
				plane.setLengthScale(lengthScale);
			}
		};
		panel.add(lengthSpinner);

		panel.add(new JLabel("  Strike", SwingConstants.RIGHT));
		widthSpinner = new DoubleSpinner(Plane.defaultSize, 1, 10000, 1, false) {
			@Override
			public void stateChanged(ChangeEvent event) {
				double widthScale = ((Double) widthSpinner.getValue());
				plane.setWidthScale(widthScale);
			}
		};
		panel.add(widthSpinner);
		contents.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		openButton = new JButton("Diff Map");
		openButton.setToolTipText("display map of difference of plane and elevation in separate window");
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				plane.getState().getViewData().setVisible(true);
				plane.getState().open();
			}
		});
		openButton.setEnabled(false);
		panel.add(openButton);
		strikeAndDip = new JLabel("                            ");
		panel.add(strikeAndDip);
		contents.add(panel);
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		this.mapElement = mapElement;
		plane = (Plane) mapElement;
		lengthSpinner.setValue(plane.getLengthScale());
		widthSpinner.setValue(plane.getWidthScale());
		pinnedCheckBox.setSelected(plane.isPinned());
		nameLabel.setText(plane.getName());
		colorList.setColor(plane.getColor());
		noteText.setText(plane.getState().getAnnotation());
		labelCheckBox.setSelected(plane.isLabelVisible());
		triangleCheckBox.setSelected(plane.isTriangleVisible());
		openButton.setEnabled(true);
		updateLocation(plane);
	}

	@Override
	public void updateLocation(MapElement mapElement) {
		setLocation(p0Location, plane.getPoint(0));
		setLocation(p1Location, plane.getPoint(1));
		setLocation(p2Location, plane.getPoint(2));
		String str = "Strike: ";
		if (Plane.strikeAsCompassBearing) {
			str += StringUtil.azimuthToCompassBearing(plane.getStrike());
		} else {
			str += StringUtil.format(plane.getStrike()) + StringUtil.DEGREE;
		}
		str += "   Dip: " + StringUtil.format(plane.getDip()) + StringUtil.DEGREE;
		strikeAndDip.setText(str);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		if (p0Location != null)
			CoordAction.listenerList.remove(p0Location);
		if (p1Location != null)
			CoordAction.listenerList.remove(p1Location);
		if (p2Location != null)
			CoordAction.listenerList.remove(p2Location);
	}

}
