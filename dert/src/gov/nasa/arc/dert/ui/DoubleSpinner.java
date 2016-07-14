package gov.nasa.arc.dert.ui;

import java.text.DecimalFormat;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A JSpinner with a model that handles doubles
 *
 */
public class DoubleSpinner extends JSpinner implements ChangeListener {

	protected double lastValue;
	protected SpinnerNumberModel model;

	/**
	 * Constructor
	 * 
	 * @param value
	 * @param min
	 * @param max
	 * @param step
	 * @param wrap
	 */
	public DoubleSpinner(double value, double min, double max, double step, boolean wrap) {
		this(value, min, max, step, wrap, "###0.00");
	}

	/**
	 * Constructor
	 * 
	 * @param value
	 * @param min
	 * @param max
	 * @param step
	 * @param wrap
	 * @param format
	 */
	public DoubleSpinner(double value, double min, double max, double step, boolean wrap, String format) {
		super();
		try {
			DecimalFormat formatter = new DecimalFormat(format);
			String str = formatter.format(min);
			min = Double.parseDouble(str);
			str = formatter.format(max);
			max = Double.parseDouble(str);
			if (value < min) {
				value = min;
			} else if (value > max) {
				value = max;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (wrap) {
			model = new SpinnerNumberModel(value, min, max, step) {
				@Override
				public Object getNextValue() {
					Object obj = super.getNextValue();
					if (obj == null) {
						return (getMinimum());
					}
					return (obj);
				}

				@Override
				public Object getPreviousValue() {
					Object obj = super.getPreviousValue();
					if (obj == null) {
						return (getMaximum());
					}
					return (obj);
				}

				@Override
				public void setValue(Object obj) {
					double val = (Double) obj;
					double maximum = (Double) super.getMaximum();
					double minimum = (Double) super.getMinimum();
					if (val < minimum) {
						val = maximum - (minimum-val)+(Double)getStepSize();
					} else if (val > maximum) {
						val = val-maximum-(Double)getStepSize();
					}
					super.setValue(new Double(val));
				}
			};
		} else {
			model = new SpinnerNumberModel(value, min, max, step);
		}
		setModel(model);
		model.addChangeListener(this);
		setEditor(new JSpinner.NumberEditor(this, format));
		lastValue = value;
	}

	/**
	 * The spinner changed, store the last value
	 */
	@Override
	public void stateChanged(ChangeEvent event) {
		lastValue = (Double) getValue();
	}

	/**
	 * Set the last value without triggering the change listener
	 * 
	 * @param val
	 */
	public void setValueNoChange(double val) {
		model.removeChangeListener(this);
		super.setValue(new Double(val));
		lastValue = val;
		model.addChangeListener(this);
	}

	/**
	 * Set the spinner maximum without triggering the change listener
	 * 
	 * @param max
	 */
	public void setMaximum(double max) {
		model.removeChangeListener(this);
		model.setMaximum(new Double(max));
		model.addChangeListener(this);
		if (lastValue > max)
			model.setValue(max);
	}

	/**
	 * Set the spinner minimum without triggering the change listener
	 * 
	 * @param min
	 */
	public void setMinimum(double min) {
		model.removeChangeListener(this);
		model.setMinimum(new Double(min));
		model.addChangeListener(this);
		if (lastValue < min)
			model.setValue(min);
	}

}
