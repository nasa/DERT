package gov.nasa.arc.dert.ui;

import gov.nasa.arc.dert.action.edit.CoordListener;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.view.Console;

import java.awt.Toolkit;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

public abstract class CoordTextField
	extends Vector3TextField
	implements CoordListener {
	
	private Vector3 valueVec, coord;
	
	public CoordTextField(int size, String toolTip, Vector3 point, String format, boolean displayZ) {
		super(size, point, format, displayZ);

		coord = new Vector3();
		valueVec = new Vector3();
		setToolTipText(toolTip);
	}
	
	public CoordTextField(int size, String toolTip, String format, boolean displayZ) {
		this(size, toolTip, new Vector3(), format, displayZ);
	}

	// User hit return
	@Override
	public void handleChange(Vector3 store) {
		Landscape landscape = Landscape.getInstance();
		coord.set(store);
		// convert to OpenGL coordinates
		if (World.getInstance().getUseLonLat())
			landscape.sphericalToLocalCoordinate(store);
		else
			landscape.worldToLocalCoordinate(store);
		// get the actual elevation at the point
		if (Double.isNaN(store.getZ()))
			store.setZ(landscape.getZ(store.getX(), store.getY()));
		// coordinate is out of bounds or in error, beep the user
		if (Double.isNaN(store.getZ())) {
			Toolkit.getDefaultToolkit().beep();
			setError();
			Console.getInstance().println("Coordinate [" + coord.getXf()+", "+coord.getYf() + "] is outside of landscape.");
		}
		// Add the elevation to the field
		else {
			doChange(store);
			landscape.localToWorldCoordinate(store);
			if (World.getInstance().getUseLonLat())
				landscape.worldToSphericalCoordinate(store);
			setValue(store);
		}
	}
	
	public float setLocalValue(ReadOnlyVector3 value) {
		coord.set(value);
		valueVec.set(value);
		// Convert from OpenGL to World coordinates
		Landscape.getInstance().localToWorldCoordinate(valueVec);
		if (World.getInstance().getUseLonLat())
			Landscape.getInstance().worldToSphericalCoordinate(valueVec);
		setValue(valueVec);
		return(valueVec.getZf());
	}
	
	public ReadOnlyVector3 getLocalValue() {
		valueVec.set(getValue());
		if (World.getInstance().getUseLonLat())
			Landscape.getInstance().sphericalToWorldCoordinate(valueVec);
		// Convert from OpenGL to World coordinates
		Landscape.getInstance().worldToLocalCoordinate(valueVec);
		return(valueVec);
	}
	
	public void coordDisplayChanged() {
		setLocalValue(coord);
	}
	
	public abstract void doChange(ReadOnlyVector3 result);

}
