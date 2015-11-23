package gov.nasa.arc.dert.ui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JTextField;

/**
 * A JTextField formatted for doubles.
 *
 */
public class DoubleTextField extends JTextField {

	private DecimalFormat formatter;

	// Only allow positive values
	private boolean posOnly;

	/**
	 * Constructor
	 * 
	 * @param columns
	 * @param value
	 * @param posOnly
	 * @param format
	 */
	public DoubleTextField(int columns, double value, boolean posOnly, String format) {
		super(columns);
		this.posOnly = posOnly;
		setEditable(true);
		formatter = new DecimalFormat(format);
		setValue(value);

		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				double value = getValue();
				if (!Double.isNaN(value)) {
					handleChange(value);
				}
			}
		});
	}

	/**
	 * Set the value
	 * 
	 * @param value
	 */
	public void setValue(double value) {
		setText(formatter.format(value));
	}

	/**
	 * Get the value
	 * 
	 * @return
	 */
	public double getValue() {
		String str = getText();
		double s = stringToDouble(str, Double.NaN);
		if (Double.isNaN(s)) {
			Toolkit.getDefaultToolkit().beep();
		} else if (posOnly) {
			if (s <= 0) {
				Toolkit.getDefaultToolkit().beep();
			}
		}
		return (s);
	}

	/**
	 * Get the value as a float
	 * 
	 * @return
	 */
	public float getFloatValue() {
		return ((float) getValue());
	}

	protected void handleChange(double value) {
		// nothing here
	}

	private double stringToDouble(String str, double defaultValue) {
		if (str == null) {
			return (defaultValue);
		}
		if (str.isEmpty()) {
			return (defaultValue);
		}
		try {
			return (Double.parseDouble(str));
		} catch (Exception e) {
			return (defaultValue);
		}
	}

}
