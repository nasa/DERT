package gov.nasa.arc.dert.view.lighting;

import gov.nasa.arc.dert.ui.CalendarPanel;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Provides calendar and time controls for positioning the solar light in the
 * LightPositionView.
 *
 */
public class TimePanel extends JPanel {

	private CalendarPanel calendar;
	private LMSTPanel lmstPanel;

	/**
	 * Constructor
	 */
	public TimePanel() {
		setLayout(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		calendar = new CalendarPanel();
		tabbedPane.addTab("UTC", calendar);
		lmstPanel = new LMSTPanel();
		tabbedPane.addTab("LMST", lmstPanel);
		add(tabbedPane, BorderLayout.CENTER);
	}

}
