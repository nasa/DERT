package gov.nasa.arc.dert.ui;

import gov.nasa.arc.dert.util.StringUtil;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JTextField;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * A JTextField formatted for a Vector3.
 *
 */
public class Vector3TextField extends JTextField {

	// Helpers
	private DecimalFormat formatter;
	private Vector3 store;

	// Flag to display the Z coordinate
	private boolean displayZ;

	// The field is in error, set the color red
	private boolean isError;

	public Vector3TextField(int columns, Vector3 value, String format, boolean displayZ) {
		super(columns);
		store = new Vector3(value);
		this.displayZ = displayZ;
		setEditable(true);
		formatter = new DecimalFormat(format);
		setValue(value);
		setToolTipText("press return to enter a value");

		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (getValue() != null) {
					handleChange(store);
				}
			}
		});
	}

	/**
	 * Set to the error state.
	 */
	public void setError() {
		isError = true;
		setForeground(Color.red);
	}

	/**
	 * Set the format
	 * 
	 * @param format
	 */
	public void setFormat(String format) {
		formatter.applyPattern(format);
	}

	/**
	 * Set the value
	 * 
	 * @param value
	 */
	public void setValue(ReadOnlyVector3 value) {
		if (isError) {
			setForeground(Color.black);
			isError = false;
		}
		if (displayZ) {
			setText(formatter.format(value.getX()) + "," + formatter.format(value.getY()) + ","
				+ formatter.format(value.getZ()));
		} else {
			setText(formatter.format(value.getX()) + "," + formatter.format(value.getY()));
		}
	}

	/**
	 * Get the value
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getValue() {
		String str = getText();
		if (stringToVector3(str, store, displayZ) == null) {
			Toolkit.getDefaultToolkit().beep();
			return (null);
		}
		return (store);
	}

	protected void handleChange(Vector3 store) {
		// nothing here
	}

	private Vector3 stringToVector3(String string, Vector3 store, boolean getZ) {
		if (store == null) {
			store = new Vector3();
		}
		double[] array = null;
		try {
			array = StringUtil.stringToDoubleArray(string);
		} catch (Exception e) {
			array = null;
		}
		if (array == null) {
			return (null);
		}
		if (array.length > 3) {
			return (null);
		} else if (array.length < 2) {
			return (null);
		}
		store.setX(array[0]);
		store.setY(array[1]);
		if (getZ && (array.length > 2)) {
			store.setZ(array[2]);
		} else {
			store.setZ(Double.NaN);
		}

		return (store);
	}

}
