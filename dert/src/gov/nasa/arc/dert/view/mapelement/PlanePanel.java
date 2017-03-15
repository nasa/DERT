package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.action.edit.CoordAction;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.tool.Plane;
import gov.nasa.arc.dert.ui.CoordTextField;
import gov.nasa.arc.dert.ui.DoubleSpinner;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
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
	private CoordTextField p0Location, p1Location, p2Location;
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
	public PlanePanel(MapElement mapElement) {
		super(mapElement);
	}

	@Override
	protected void addFields(ArrayList<Component> compList) {

		compList.add(new JLabel("Point 0", SwingConstants.RIGHT));
		p0Location = new CoordTextField(22, "location of first point of triangle", Landscape.format, true) {
			@Override
			public void doChange(ReadOnlyVector3 result) {
				double z = Landscape.getInstance().getZ(result.getX(), result.getY());
				if (Double.isNaN(z)) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				if (Double.isNaN(result.getZ())) {
					plane.setPoint(0, result.getX(), result.getY(), z);
				}
				else {
					plane.getMarker(0).setZOffset(result.getZ()-z, false);
					plane.setPoint(0, result.getX(), result.getY(), z);
				}
			}			
		};
		CoordAction.listenerList.add(p0Location);
		compList.add(p0Location);

		compList.add(new JLabel("Point 1", SwingConstants.RIGHT));
		p1Location = new CoordTextField(22, "location of second point of triangle", Landscape.format, true) {
			@Override
			public void doChange(ReadOnlyVector3 result) {
				double z = Landscape.getInstance().getZ(result.getX(), result.getY());
				if (Double.isNaN(z)) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				if (Double.isNaN(result.getZ())) {
					plane.setPoint(1, result.getX(), result.getY(), z);
				}
				else {
					plane.getMarker(1).setZOffset(result.getZ()-z, false);
					plane.setPoint(1, result.getX(), result.getY(), z);
				}
			}			
		};
		CoordAction.listenerList.add(p1Location);
		compList.add(p1Location);

		compList.add(new JLabel("Point 2", SwingConstants.RIGHT));
		p2Location = new CoordTextField(22, "location of third point of triangle", Landscape.format, true) {
			@Override
			public void doChange(ReadOnlyVector3 result) {
				double z = Landscape.getInstance().getZ(result.getX(), result.getY());
				if (Double.isNaN(z)) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				if (Double.isNaN(result.getZ())) {
					plane.setPoint(2, result.getX(), result.getY(), z);
				}
				else {
					plane.getMarker(2).setZOffset(result.getZ()-z, false);
					plane.setPoint(2, result.getX(), result.getY(), z);
				}
			}			
		};
		CoordAction.listenerList.add(p2Location);
		compList.add(p2Location);
		
		compList.add(new JLabel("Triangle", SwingConstants.RIGHT));
		triangleCheckBox = new JCheckBox("visible");
		triangleCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				plane.setTriangleVisible(triangleCheckBox.isSelected());
				plane.markDirty(DirtyType.RenderState);
			}
		});
		compList.add(triangleCheckBox);

		double step = Landscape.getInstance().getPixelWidth();
		double min = 1;
		double max = 10000;
		double val = Plane.defaultSize;
		String fmt = "###0.00";
		if (step < 1) {
			min *= step;
			max *= step;
			val *= step;
			fmt = Landscape.format;
		}
		else
			step = 1;
		
		compList.add(new JLabel("Scale Dip Axis", SwingConstants.RIGHT));
		lengthSpinner = new DoubleSpinner(val, min, max, step, false, fmt) {
			@Override
			public void stateChanged(ChangeEvent event) {
				double lengthScale = ((Double) lengthSpinner.getValue());
				plane.setLengthScale(lengthScale);
			}
		};
		compList.add(lengthSpinner);

		compList.add(new JLabel("Scale Strike Axis", SwingConstants.RIGHT));
		widthSpinner = new DoubleSpinner(val, min, max, step, false, fmt) {
			@Override
			public void stateChanged(ChangeEvent event) {
				double widthScale = ((Double) widthSpinner.getValue());
				plane.setWidthScale(widthScale);
			}
		};
		compList.add(widthSpinner);
		
		strikeAndDip = new JLabel("                            ");
	}

	@Override
	public void setMapElement(MapElement mapElement) {
		super.setMapElement(mapElement);
		if (this.mapElement != null)
			((Plane)this.mapElement).setPlanePanel(null);
		plane = (Plane) mapElement;
		plane.setPlanePanel(this);
		lengthSpinner.setValue(plane.getLengthScale());
		widthSpinner.setValue(plane.getWidthScale());
		noteText.setText(plane.getState().getAnnotation());
		triangleCheckBox.setSelected(plane.isTriangleVisible());
		updateLocation(plane);
		updateStrikeAndDip(plane.getStrike(), plane.getDip());
	}

	@Override
	public void updateLocation(MapElement mapElement) {
		setLocation(p0Location, plane.getPoint(0));
		setLocation(p1Location, plane.getPoint(1));
		setLocation(p2Location, plane.getPoint(2));
//		System.err.println("PlanePanel.updateLocation "+plane.getStrike()+" "+plane.getDip());
//		String str = "Strike: ";
//		if (Plane.strikeAsCompassBearing) {
//			str += StringUtil.azimuthToCompassBearing(plane.getStrike());
//		} else {
//			str += StringUtil.format(plane.getStrike()) + StringUtil.DEGREE;
//		}
//		str += "   Dip: " + StringUtil.format(plane.getDip()) + StringUtil.DEGREE;
//		strikeAndDip.setText(str);
	}

	/**
	 * Method to update strike and dip values. This must be called after values are calculated
	 * in plane.
	 * @param strike
	 * @param dip
	 */
	public void updateStrikeAndDip(double strike, double dip) {
		String str = "Strike: ";
		if (Plane.strikeAsCompassBearing) {
			str += StringUtil.azimuthToCompassBearing(strike);
		} else {
			str += StringUtil.format(strike) + StringUtil.DEGREE;
		}
		str += "   Dip: " + StringUtil.format(dip) + StringUtil.DEGREE;
		strikeAndDip.setText(str);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		if (mapElement != null)
			((Plane)mapElement).setPlanePanel(null);
		if (p0Location != null)
			CoordAction.listenerList.remove(p0Location);
		if (p1Location != null)
			CoordAction.listenerList.remove(p1Location);
		if (p2Location != null)
			CoordAction.listenerList.remove(p2Location);
	}

}
