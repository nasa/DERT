package gov.nasa.arc.dert.ui;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.view.Console;

import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

public abstract class CoordTextField
	extends JPanel {
	
	private JCheckBox useLonLat;
	private Vector3TextField coordField;
	private Vector3 valueVec;
	
	public CoordTextField(int size, String toolTip, String format, boolean displayZ) {
		setBorder(BorderFactory.createEtchedBorder());

		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		valueVec = new Vector3();

		// The marble location field.
		coordField = new Vector3TextField(size, valueVec, format, displayZ) {
			// User hit return
			@Override
			protected void handleChange(Vector3 store) {
				Landscape landscape = Landscape.getInstance();
				// save a copy
				Vector3 old = new Vector3(store);
				// convert to OpenGL coordinates
				if (useLonLat.isSelected())
					landscape.sphericalToLocalCoordinate(store);
				else
					landscape.worldToLocalCoordinate(store);
				// get the actual elevation at the point
				double z = landscape.getZ(store.getX(), store.getY());
				// coordinate is out of bounds or in error, beep the user
				if (Double.isNaN(z)) {
					Toolkit.getDefaultToolkit().beep();
					setError();
					Console.getInstance().println("Coordinate [" + old.getXf()+", "+old.getYf()+", "+old.getZf() + "] is outside of landscape.");
				}
				// Add the elevation to the field
				else {
					store.setZ(z);
					doChange(store);
					landscape.localToWorldCoordinate(store);
					if (useLonLat.isSelected())
						landscape.worldToSphericalCoordinate(store);
					setValue(store);
				}
			}
		};
		coordField.setToolTipText(toolTip);
		add(coordField);
		
		useLonLat = new JCheckBox(Icons.getImageIcon("graticule_16.png"));
		useLonLat.setSelectedIcon(Icons.getImageIcon("graticule_checked_16.png"));
		useLonLat.setToolTipText("Show as longitude, latitude, altitude");
		useLonLat.setBorder(new EmptyBorder(3,0,3,4));
		useLonLat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (useLonLat.isSelected()) {
					ReadOnlyVector3 coord = coordField.getValue();
					if (coord != null) {
						Vector3 lla = new Vector3(coord);
						Landscape.getInstance().worldToSphericalCoordinate(lla);
						coordField.setValue(lla);
					}
				}
				else {
					ReadOnlyVector3 lla = coordField.getValue();
					if (lla != null) {
						Vector3 coord = new Vector3(lla);
						Landscape.getInstance().sphericalToWorldCoordinate(coord);
						coordField.setValue(coord);
					}
					
				}
			}
		});
		add(useLonLat);

	}
	
	public void setLocalValue(ReadOnlyVector3 value) {
		valueVec.set(value);
		// Convert from OpenGL to World coordinates
		Landscape.getInstance().localToWorldCoordinate(valueVec);
		if (useLonLat.isSelected())
			Landscape.getInstance().worldToSphericalCoordinate(valueVec);
		coordField.setValue(valueVec);
	}
	
	public void setFormat(String format) {
		coordField.setFormat(format);
	}
	
	public void setEnabled(boolean enabled) {
		coordField.setEnabled(enabled);
		useLonLat.setEnabled(enabled);
	}
	
	public abstract void doChange(ReadOnlyVector3 result);

}
