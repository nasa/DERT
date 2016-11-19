package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.featureset.FeatureSet;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.ui.DoubleTextField;

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
 * Provides controls for setting FeatureSet preferences and adding FeatureSets.
 *
 */
public class FeatureSetsPanel extends JPanel {

	/**
	 * Constructor
	 */
	public FeatureSetsPanel() {
		super();
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel(new GridLayout(3, 1));

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Add:"));

		JButton addButton = new JButton(Icons.getImageIcon("lineset.png"));
		addButton.setToolTipText("FeatureSet");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				addFeatureSet(MapElementState.Type.FeatureSet);
			}
		});
		panel.add(addButton);
		topPanel.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("All FeatureSets:"));
		JButton hideAllButton = new JButton("Hide");
		hideAllButton.setToolTipText("hide all FeatureSets");
		hideAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setAllVisible(false);
			}
		});
		panel.add(hideAllButton);

		JButton showAllButton = new JButton("Show");
		showAllButton.setToolTipText("show all FeatureSets");
		showAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				setAllVisible(true);
			}
		});
		panel.add(showAllButton);
		topPanel.add(panel);
		topPanel.add(new JLabel("FeatureSet Preferences", SwingConstants.LEFT));
		add(topPanel, BorderLayout.NORTH);
		JPanel centerPanel = new JPanel(new GridLayout(3, 1));

		// FeatureSet Preferences
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Color:", SwingConstants.RIGHT));
		ColorSelectionPanel colorList = new ColorSelectionPanel(FeatureSet.defaultColor) {
			@Override
			public void doColor(Color color) {
				FeatureSet.defaultColor = color;
			}
		};
		panel.add(colorList);
		centerPanel.add(panel);

		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Linewidth:", SwingConstants.RIGHT));
		DoubleTextField lwText = new DoubleTextField(8, FeatureSet.defaultLineWidth, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				FeatureSet.defaultLineWidth = (float)value;
			}
		};
		panel.add(lwText);
		centerPanel.add(panel);

		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Point Size:", SwingConstants.RIGHT));
		DoubleTextField ptSzText = new DoubleTextField(8, FeatureSet.defaultSize, true, "0.00") {
			@Override
			protected void handleChange(double value) {
				if (Double.isNaN(value)) {
					return;
				}
				FeatureSet.defaultSize = (float)value;
			}
		};
		panel.add(ptSzText);
		centerPanel.add(panel);
		
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(centerPanel);
		
		add(panel, BorderLayout.CENTER);
	}

	/**
	 * Add a new FeatureSet. Overridden by implementing class.
	 * 
	 * @param type
	 */
	public void addFeatureSet(MapElementState.Type type) {
		// nothing here
	}

	/**
	 * Set visibility of all FeatureSets. Overridden by implementing class.
	 * 
	 * @param visible
	 */
	public void setAllVisible(boolean visible) {
		// nothing here
	}
}
