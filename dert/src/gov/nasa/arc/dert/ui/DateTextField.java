package gov.nasa.arc.dert.ui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTextField;

/**
 * JTextField formatted for a Date.
 *
 */
public class DateTextField extends JTextField {

	private SimpleDateFormat formatter;

	/**
	 * Constructor
	 * 
	 * @param columns
	 * @param value
	 * @param format
	 */
	public DateTextField(int columns, Date value, String format) {
		super(columns);
		setEditable(true);
		formatter = new SimpleDateFormat(format);
		setValue(value);

		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Date value = getValue();
				if (value != null) {
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
	public void setValue(Date value) {
		setText(formatter.format(value));
	}

	/**
	 * Get the value
	 * 
	 * @return
	 */
	public Date getValue() {
		String str = getText();
		Date date = null;
		try {
			date = formatter.parse(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (date == null) {
			Toolkit.getDefaultToolkit().beep();
		}
		return (date);
	}

	protected void handleChange(Date value) {
		// nothing here
	}

}
