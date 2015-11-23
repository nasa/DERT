package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.LineSet;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.GroupPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Provides controls for setting LineSet preferences and adding LineSets.
 *
 */
public class LineSetsPanel extends JPanel {

	/**
	 * Constructor
	 */
	public LineSetsPanel() {
		super();
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel(new GridLayout(2, 1));

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Add:"));

		JButton addButton = new JButton(Icons.getImageIcon("lineset.png"));
		addButton.setToolTipText("LineSet");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				addLineSet(MapElementState.Type.LineSet);
			}
		});
		panel.add(addButton);
		topPanel.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("All LineSets:"));
		JButton hideAllButton = new JButton("Hide");
		hideAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setAllVisible(false);
			}
		});
		panel.add(hideAllButton);

		JButton showAllButton = new JButton("Show");
		showAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setAllVisible(true);
			}
		});
		panel.add(showAllButton);
		topPanel.add(panel);
		add(topPanel, BorderLayout.NORTH);

		// LineSet Preferences
		GroupPanel gPanel = new GroupPanel("Preferences");
		gPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		gPanel.add(new JLabel("Color", SwingConstants.RIGHT));
		ColorSelectionPanel colorList = new ColorSelectionPanel(LineSet.defaultColor) {
			@Override
			public void doColor(Color color) {
				LineSet.defaultColor = color;
			}
		};
		gPanel.add(colorList);
		add(gPanel, BorderLayout.CENTER);
	}

	/**
	 * Add a new LineSet. Overridden by implementing class.
	 * 
	 * @param type
	 */
	public void addLineSet(MapElementState.Type type) {
		// nothing here
	}

	/**
	 * Set visibility of all LineSets. Overridden by implementing class.
	 * 
	 * @param visible
	 */
	public void setAllVisible(boolean visible) {
		// nothing here
	}
}
