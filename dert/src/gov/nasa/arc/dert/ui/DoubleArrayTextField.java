package gov.nasa.arc.dert.ui;

import gov.nasa.arc.dert.util.StringUtil;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JTextField;

import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * A JTextField formatted for an array of double.
 *
 */
public class DoubleArrayTextField extends JTextField {

	private DecimalFormat formatter;
	private int length;

	/**
	 * Constructor
	 * 
	 * @param columns
	 * @param value
	 * @param format
	 */
	public DoubleArrayTextField(int columns, double[] value, String format) {
		super(columns);
		length = value.length;
		setEditable(true);
		formatter = new DecimalFormat(format);
		setValue(value);

		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				double[] value = getValue();
				handleChange(value);
			}
		});
	}

	/**
	 * Constructor for float array
	 * 
	 * @param columns
	 * @param value
	 * @param format
	 */
	public DoubleArrayTextField(int columns, float[] value, String format) {
		super(columns);
		length = value.length;
		setEditable(true);
		formatter = new DecimalFormat(format);
		setValue(value);

		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				double[] value = getValue();
				handleChange(value);
			}
		});
	}

	/**
	 * Set the value with a double array
	 * 
	 * @param value
	 */
	public void setValue(double[] value) {
		String str = "";
		if (value != null) {
			str = formatter.format(value[0]);
			for (int i = 1; i < length; ++i) {
				str += "," + formatter.format(value[i]);
			}
		}
		setText(str);
	}

	/**
	 * Set the value with a float array
	 * 
	 * @param value
	 */
	public void setValue(float[] value) {
		String str = "";
		if (value != null) {
			str = formatter.format(value[0]);
			for (int i = 1; i < length; ++i) {
				str += "," + formatter.format(value[i]);
			}
		}
		setText(str);
	}

	/**
	 * Set the value with a Vector3
	 * 
	 * @param value
	 */
	public void setValue(ReadOnlyVector3 value) {
		String str = "";
		if (value != null) {
			str = formatter.format(value.getX()) + "," + formatter.format(value.getY()) + ","
				+ formatter.format(value.getZ());
		}
		setText(str);
	}

	/**
	 * Get the value as a double array
	 * 
	 * @return
	 */
	public double[] getValue() {
		String str = getText();
		double[] value = null;
		try {
			value = StringUtil.stringToDoubleArray(str);
			if ((value == null) || (value.length != length)) {
				Toolkit.getDefaultToolkit().beep();
			}
		} catch (Exception e) {
			Toolkit.getDefaultToolkit().beep();
		}
		return (value);
	}

	/**
	 * Get the value as a float array
	 * 
	 * @return
	 */
	public float[] getFloatValue() {
		String str = getText();
		float[] value = null;
		try {
			value = StringUtil.stringToFloatArray(str);
			if ((value == null) || (value.length != length)) {
				Toolkit.getDefaultToolkit().beep();
			}
		} catch (Exception e) {
			Toolkit.getDefaultToolkit().beep();
		}
		return (value);
	}

	protected void handleChange(double[] value) {
		// nothing here
	}

}
