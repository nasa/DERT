package gov.nasa.arc.dert.ui;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A JSpinner with a model that handles ints
 *
 */
public class IntSpinner extends JSpinner implements ChangeListener {

	protected int lastValue;
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
	public IntSpinner(int value, int min, int max, int step, boolean wrap, String format) {
		super();
		try {
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
					int val = (Integer) obj;
					int maximum = (Integer) super.getMaximum();
					int minimum = (Integer) super.getMinimum();
					if (val < minimum) {
						val = maximum - (minimum-val)+(Integer)getStepSize();
					} else if (val > maximum) {
						val = val-maximum-(Integer)getStepSize();
					}
					super.setValue(new Integer(val));
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
		lastValue = (Integer) getValue();
	}

	/**
	 * Set the last value without triggering the change listener
	 * 
	 * @param val
	 */
	public void setValueNoChange(int val) {
		model.removeChangeListener(this);
		super.setValue(new Integer(val));
		lastValue = val;
		model.addChangeListener(this);
	}

	/**
	 * Set the spinner maximum without triggering the change listener
	 * 
	 * @param max
	 */
	public void setMaximum(int max) {
		model.removeChangeListener(this);
		model.setMaximum(new Integer(max));
		model.addChangeListener(this);
		if (lastValue > max)
			model.setValue(max);
	}

	/**
	 * Set the spinner minimum without triggering the change listener
	 * 
	 * @param min
	 */
	public void setMinimum(int min) {
		model.removeChangeListener(this);
		model.setMinimum(new Integer(min));
		model.addChangeListener(this);
		if (lastValue < min)
			model.setValue(min);
	}

}
