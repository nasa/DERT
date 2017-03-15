package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.featureset.Feature;
import gov.nasa.arc.dert.state.State;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.JPanelView;

import java.awt.BorderLayout;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class FeatureSetView
	extends JPanelView {
	
	private JTextArea propText;
	private JLabel location, locLabel;
	
	public FeatureSetView(State state) {
		super(state);
		JPanel locPanel = new JPanel(new BorderLayout(5, 5));
		locPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		locLabel = new JLabel("Location:", SwingConstants.RIGHT);
		locPanel.add(locLabel, BorderLayout.WEST);
		location = new JLabel();
		location.setToolTipText("location in landscape");
		locPanel.add(location, BorderLayout.CENTER);
		add(locPanel, BorderLayout.NORTH);
		
		JPanel propPanel = new JPanel(new BorderLayout());
		propPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		propPanel.add(new JLabel("Properties", SwingConstants.LEFT), BorderLayout.NORTH);
		propText = new JTextArea();
		propText.setEditable(false);
		propText.setRows(4);
		propPanel.add(new JScrollPane(propText), BorderLayout.CENTER);
		add(propPanel, BorderLayout.CENTER);
	}
	
	public void setMapElement(MapElement mapElement) {
		if (mapElement instanceof Feature) {
			locLabel.setText(mapElement.getName()+":");
			location.setText(StringUtil.format(mapElement.getLocationInWorld()));
			HashMap<String,Object> properties = ((Feature)mapElement).getProperties();
			Object[] key = properties.keySet().toArray();
			String str = "";
			for (int i=0; i<key.length; ++i) {
				str += key[i]+" = "+properties.get((String)key[i])+"\n";
			}
			propText.setText(str);
			propText.setCaretPosition(0);
		}
		else {
			locLabel.setText("Location");
			location.setText("");
			propText.setText("");
			propText.setCaretPosition(0);
		}
	}

}
