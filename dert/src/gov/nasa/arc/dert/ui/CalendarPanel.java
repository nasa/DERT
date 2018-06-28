/**

DERT is a viewer for digital terrain models created from data collected during NASA missions.

DERT is Released in under the NASA Open Source Agreement (NOSA) found in the “LICENSE” folder where you
downloaded DERT.

DERT includes 3rd Party software. The complete copyright notice listing for DERT is:

Copyright © 2015 United States Government as represented by the Administrator of the National Aeronautics and
Space Administration.  No copyright is claimed in the United States under Title 17, U.S.Code. All Other Rights
Reserved.

Desktop Exploration of Remote Terrain (DERT) could not have been written without the aid of a number of free,
open source libraries. These libraries and their notices are listed below. Find the complete third party license
listings in the separate “DERT Third Party Licenses” pdf document found where you downloaded DERT in the
LICENSE folder.
 
JogAmp Ardor3D Continuation
Copyright © 2008-2012 Ardor Labs, Inc.
 
JogAmp
Copyright 2010 JogAmp Community. All rights reserved.
 
JOGL Portions Sun Microsystems
Copyright © 2003-2009 Sun Microsystems, Inc. All Rights Reserved.
 
JOGL Portions Silicon Graphics
Copyright © 1991-2000 Silicon Graphics, Inc.
 
Light Weight Java Gaming Library Project (LWJGL)
Copyright © 2002-2004 LWJGL Project All rights reserved.
 
Tile Rendering Library - Brain Paul 
Copyright © 1997-2005 Brian Paul. All Rights Reserved.
 
OpenKODE, EGL, OpenGL , OpenGL ES1 & ES2
Copyright © 2007-2010 The Khronos Group Inc.
 
Cg
Copyright © 2002, NVIDIA Corporation
 
Typecast - David Schweinsberg 
Copyright © 1999-2003 The Apache Software Foundation. All rights reserved.
 
PNGJ - Herman J. Gonzalez and Shawn Hartsock
Copyright © 2004 The Apache Software Foundation. All rights reserved.
 
Apache Harmony - Open Source Java SE
Copyright © 2006, 2010 The Apache Software Foundation.
 
Guava
Copyright © 2010 The Guava Authors
 
GlueGen Portions
Copyright © 2010 JogAmp Community. All rights reserved.
 
GlueGen Portions - Sun Microsystems
Copyright © 2003-2005 Sun Microsystems, Inc. All Rights Reserved.
 
SPICE
Copyright © 2003, California Institute of Technology.
U.S. Government sponsorship acknowledged.
 
LibTIFF
Copyright © 1988-1997 Sam Leffler
Copyright © 1991-1997 Silicon Graphics, Inc.
 
PROJ.4
Copyright © 2000, Frank Warmerdam

LibJPEG - Independent JPEG Group
Copyright © 1991-2018, Thomas G. Lane, Guido Vollbeding
 

Disclaimers

No Warranty: THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY KIND,
EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
THAT THE SUBJECT SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY
WARRANTY THAT THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE. THIS AGREEMENT
DOES NOT, IN ANY MANNER, CONSTITUTE AN ENDORSEMENT BY GOVERNMENT AGENCY OR ANY
PRIOR RECIPIENT OF ANY RESULTS, RESULTING DESIGNS, HARDWARE, SOFTWARE PRODUCTS OR
ANY OTHER APPLICATIONS RESULTING FROM USE OF THE SUBJECT SOFTWARE.  FURTHER,
GOVERNMENT AGENCY DISCLAIMS ALL WARRANTIES AND LIABILITIES REGARDING THIRD-PARTY
SOFTWARE, IF PRESENT IN THE ORIGINAL SOFTWARE, AND DISTRIBUTES IT "AS IS."

Waiver and Indemnity:  RECIPIENT AGREES TO WAIVE ANY AND ALL CLAIMS AGAINST THE UNITED
STATES GOVERNMENT, ITS CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR
RECIPIENT.  IF RECIPIENT'S USE OF THE SUBJECT SOFTWARE RESULTS IN ANY LIABILITIES,
DEMANDS, DAMAGES, EXPENSES OR LOSSES ARISING FROM SUCH USE, INCLUDING ANY DAMAGES
FROM PRODUCTS BASED ON, OR RESULTING FROM, RECIPIENT'S USE OF THE SUBJECT SOFTWARE,
RECIPIENT SHALL INDEMNIFY AND HOLD HARMLESS THE UNITED STATES GOVERNMENT, ITS
CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR RECIPIENT, TO THE EXTENT
PERMITTED BY LAW.  RECIPIENT'S SOLE REMEDY FOR ANY SUCH MATTER SHALL BE THE IMMEDIATE,
UNILATERAL TERMINATION OF THIS AGREEMENT.

**/

package gov.nasa.arc.dert.ui;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.World;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Provides a calendar for selecting date/time.
 */
public class CalendarPanel extends JPanel implements MouseListener {

	/**
	 * Spinner model class
	 */
	protected class CalSpinnerNumberModel extends SpinnerNumberModel {
		public CalSpinnerNumberModel(Integer value, Integer min, Integer max, Integer step) {
			super(value, min, max, step);
		}

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
				val = maximum - (minimum-val)+1;
			} else if (val > maximum) {
				val = val-maximum-1;
			}
			super.setValue(new Integer(val));
		}
	};

	// UI layout
	private GridLayout gridLayout;

	// Day labels
	private JLabel sunday;
	private JLabel monday;
	private JLabel tuesday;
	private JLabel wednesday;
	private JLabel thursday;
	private JLabel friday;
	private JLabel saturday;

	// Year and mond labels
	private JButton yearUp;
	private JButton yearNext;
	private JButton monthUp;
	private JButton monthNext;
	private JButton current;
	private JLabel selectedMonth;
	private JLabel selectedYear;

	// Days for a month page
	private JLabel[] days = new JLabel[42];
	private JLabel selectedDay;
	private Color labelBackground;

	// Time spinners
	private JSpinner hour, minute, second, doy;
	private int lastSecond, lastMinute;

	// Formatters
	private SimpleDateFormat dateFormatter, formatterMonth, formatterYear;

	// Date and Time
	private Date selectedDate;
	private long daysFromEpochToNow;
	private Calendar cal;
	private long millisPerDay = 86400000;
	private boolean doyCommit;
	private boolean doSetTime;

	/**
	 * Constructor
	 */
	public CalendarPanel() {
		super();
		daysFromEpochToNow = getDay(new Date().getTime());
		cal = Calendar.getInstance();
		cal.setTime(new Date(World.getInstance().getTime()));
		dateFormatter = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		formatterYear = new SimpleDateFormat("yyyy");
		formatterMonth = new SimpleDateFormat("MMM");
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		initialize();
	}

	/**
	 * Initialize the display
	 */
	private void initialize() {
		setSize(230, 240);

		setLayout(new BorderLayout());

		JPanel timePart = new JPanel();
		gridLayout = new GridLayout(1, 5);
		timePart.setLayout(new FlowLayout());

		JLabel label = new JLabel("H:M:S ");
		timePart.add(label);
		hour = new JSpinner(new CalSpinnerNumberModel(new Integer(0), new Integer(0), new Integer(23), new Integer(1)));
		timePart.add(hour);
		hour.setToolTipText("hours (0-23)");
		hour.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				if (doSetTime)
					World.getInstance().setTime(getSelectedDate().getTime());
			}
		});
		minute = new JSpinner(
			new CalSpinnerNumberModel(new Integer(0), new Integer(0), new Integer(59), new Integer(1)));
		timePart.add(minute);
		minute.setToolTipText("minutes (0-59)");
		minute.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				if ((Integer) minute.getValue() == 0) {
					if (lastMinute == 59) {
						hour.setValue(new Integer((Integer)hour.getValue()+1));
					}
				} else if ((Integer) minute.getValue() == 59) {
					if (lastMinute == 0) {
						hour.setValue(new Integer((Integer)hour.getValue()-1));
					}
				}
				lastMinute = (Integer) minute.getValue();
				if (doSetTime)
					World.getInstance().setTime(getSelectedDate().getTime());
			}
		});
		second = new JSpinner(
			new CalSpinnerNumberModel(new Integer(0), new Integer(0), new Integer(59), new Integer(1)));
		timePart.add(second);
		second.setToolTipText("seconds (0-59)");
		second.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				if ((Integer) second.getValue() == 0) {
					if (lastSecond == 59) {
						minute.setValue(new Integer((Integer)minute.getValue()+1));
					}
				} else if ((Integer) second.getValue() == 59) {
					if (lastSecond == 0) {
						minute.setValue(new Integer((Integer) minute.getValue() - 1));
					}
				}
				lastSecond = (Integer) second.getValue();
				if (doSetTime)
					World.getInstance().setTime(getSelectedDate().getTime());
			}
		});
		current = new JButton();
		timePart.add(current);
		current.setText("Now");
		current.setToolTipText("set calendar to current date and time");
		current.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				doSetTime = false;
				setCurrentDate(new Date());
				doSetTime = true;
				World.getInstance().setTime(getSelectedDate().getTime());
			}
		});

		add(timePart, BorderLayout.NORTH);

		JPanel calendarPart = new JPanel();
		gridLayout = new GridLayout(8, 7);
		calendarPart.setLayout(gridLayout);

		yearUp = new JButton(Icons.getImageIcon("doubleprev_16.png"));
		calendarPart.add(yearUp);
		yearUp.setToolTipText("previous year");
		yearUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				shiftDate(Calendar.YEAR, -1);
				if (doSetTime)
					World.getInstance().setTime(getSelectedDate().getTime());
			}
		});

		monthUp = new JButton(Icons.getImageIcon("prev_16.png"));
		calendarPart.add(monthUp);
		monthUp.setToolTipText("previous month");
		monthUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				shiftDate(Calendar.MONTH, -1);
				if (doSetTime)
					World.getInstance().setTime(getSelectedDate().getTime());
			}
		});

		selectedYear = new JLabel(formatterYear.format(new Date()), SwingConstants.RIGHT);
		calendarPart.add(selectedYear);
		calendarPart.add(new JLabel("-", SwingConstants.CENTER));
		selectedMonth = new JLabel(formatterMonth.format(new Date()), SwingConstants.LEFT);
		calendarPart.add(selectedMonth);

		monthNext = new JButton(Icons.getImageIcon("next_16.png"));
		calendarPart.add(monthNext);
		monthNext.setToolTipText("next month");
		monthNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				shiftDate(Calendar.MONTH, 1);
				if (doSetTime)
					World.getInstance().setTime(getSelectedDate().getTime());
			}
		});

		yearNext = new JButton(Icons.getImageIcon("doublenext_16.png"));
		calendarPart.add(yearNext);
		yearNext.setToolTipText("next year");
		yearNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				shiftDate(Calendar.YEAR, 1);
				if (doSetTime)
					World.getInstance().setTime(getSelectedDate().getTime());
			}
		});

		sunday = new JLabel("Sun");
		calendarPart.add(sunday);

		monday = new JLabel("Mon");
		calendarPart.add(monday);

		tuesday = new JLabel("Tue");
		calendarPart.add(tuesday);

		wednesday = new JLabel("Wed");
		calendarPart.add(wednesday);

		thursday = new JLabel("Thu");
		calendarPart.add(thursday);

		friday = new JLabel("Fri");
		calendarPart.add(friday);

		saturday = new JLabel("Sat");
		calendarPart.add(saturday);

		for (int i = 0; i < 42; i++) {
			days[i] = new JLabel(" ", SwingConstants.CENTER);
			days[i].setOpaque(true);
			calendarPart.add(days[i]);
			days[i].addMouseListener(this);
		}
		labelBackground = days[0].getBackground();

		add(calendarPart, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new FlowLayout());
		bottomPanel.add(new JLabel("Day of Year:"));
		doy = new JSpinner(new CalSpinnerNumberModel(new Integer(1), new Integer(1), new Integer(366), new Integer(1)));
		doy.setToolTipText("day of year (1-366)");
		doy.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				setDateFromDOY();
				if (doSetTime)
					World.getInstance().setTime(getSelectedDate().getTime());
			}
		});
		bottomPanel.add(doy);

		add(bottomPanel, BorderLayout.SOUTH);

		selectedDate = new Date(cal.getTimeInMillis());
		setCurrentDate(selectedDate);
		doSetTime = true;

	}

	private long getDay(long time) {
		return (time / millisPerDay);
	}

	private int getLastDayOfMonth(int year, int month) {
		switch (month) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			return (31);
		case 4:
		case 6:
		case 9:
		case 11:
			return (30);
		case 2:
			if (isLeapYear(year)) {
				return (29);
			} else {
				return (28);
			}
		}
		return 0;
	}

	private boolean isLeapYear(int year) {
		return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
	}

	private void shiftDate(int type, int value) {
		cal.setTime(selectedDate); // set current date
		cal.add(type, value); // add to spec time.
		selectedDate = new Date(cal.getTimeInMillis()); // result
		selectedMonth.setText(formatterMonth.format(selectedDate)); // set to
																	// label
		selectedYear.setText(formatterYear.format(selectedDate)); // set to
																	// label
		setDOY(cal.get(Calendar.DAY_OF_YEAR));
		setDayForDisplay(cal);
	}

	private void setDayForDisplay(Calendar calendar) {
		int day = calendar.get(Calendar.DATE);
		calendar.add(Calendar.DAY_OF_MONTH, -(calendar.get(Calendar.DATE) - 1));
		int startIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int lastDay = this.getLastDayOfMonth(year, month);
		int endIndex = startIndex + lastDay - 1;
		int dayIndex = 1;
		calendar.set(Calendar.DATE, dayIndex);
		long d = getDay(calendar.getTimeInMillis());
		for (int i = 0; i < 42; i++) {
			Color temp = days[i].getBackground();
			if (temp.equals(Color.yellow) || temp.equals(Color.green)) {
				days[i].setBackground(labelBackground);
			}
		}
		for (int i = 0; i < 42; i++) {
			if (i >= startIndex && i <= endIndex) {
				days[i].setText(Integer.toString(dayIndex));
				if (dayIndex == day) {
					days[i].setBackground(Color.yellow);
					selectedDay = days[i];
				} else if (d == daysFromEpochToNow) {
					days[i].setBackground(Color.green);
				}
				d++;
				dayIndex++;
			} else {
				days[i].setText("");
			}
		}

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// nothing here
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// nothing here
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// nothing here
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// nothing here
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		JLabel day = (JLabel) e.getSource();
		if (!day.getText().equals("")) {
			if (selectedDay != null) {
				selectedDay.setBackground(labelBackground);
			}
			selectedDay = day;
			selectedDate = getSelectedDate();
			cal.setTime(selectedDate);
			setDOY(cal.get(Calendar.DAY_OF_YEAR));
			setDayForDisplay(cal);
			if (doSetTime)
				World.getInstance().setTime(getSelectedDate().getTime());
		}
	}

	/**
	 * Get the date specified by the user
	 * 
	 * @return
	 */
	public Date getSelectedDate() {
		try {
			String str = selectedYear.getText() + "-" + selectedMonth.getText() + "-" + selectedDay.getText() + " "
				+ hour.getValue() + ":" + minute.getValue() + ":" + second.getValue();
			return (dateFormatter.parse(str));
		} catch (Exception e) {
			return (new Date());
		}
	}

	private void setDOY(int val) {
		doyCommit = false;
		doy.setValue(val);
		doyCommit = true;
	}

	/**
	 * Set the current date
	 * 
	 * @param currentDate
	 */
	public void setCurrentDate(Date currentDate) {
		selectedDate.setTime(currentDate.getTime());
		selectedMonth.setText(formatterMonth.format(currentDate));
		selectedYear.setText(formatterYear.format(currentDate));
		cal.setTime(currentDate);
		setDOY(cal.get(Calendar.DAY_OF_YEAR));
		setDayForDisplay(cal);
		lastSecond = cal.get(Calendar.SECOND);
		second.setValue(lastSecond);
		lastMinute = cal.get(Calendar.MINUTE);
		minute.setValue(lastMinute);
		int hr = cal.get(Calendar.HOUR_OF_DAY);
		hour.setValue(hr);
	}

	/**
	 * Set the current data from DOY
	 */
	public void setDateFromDOY() {
		if (!doyCommit) {
			return;
		}
		int doyValue = (Integer) doy.getValue();
		cal.setTime(selectedDate); // set current date
		cal.set(Calendar.DAY_OF_YEAR, doyValue);
		selectedDate = new Date(cal.getTimeInMillis()); // result
		selectedMonth.setText(formatterMonth.format(selectedDate)); // set to
																	// label
		selectedYear.setText(formatterYear.format(selectedDate)); // set to
																	// label
		setDayForDisplay(cal);
	}

}
