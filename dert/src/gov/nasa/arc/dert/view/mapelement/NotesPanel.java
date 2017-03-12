package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class NotesPanel
	extends JPanel {
	
	private MapElement mapElement;
	private JPanel topPanel;
	private JTextArea textArea;
	private JLabel location;
	private JLabel locked;
	private JLabel iconLabel;
	private JLabel titleLabel;
	
	public NotesPanel(MapElement mapElement, boolean title) {
		this.mapElement = mapElement;
		locked = new JLabel(Icons.getImageIcon("locked.png"));
		setLayout(new BorderLayout());
		JPanel contents = null;
		if (title) {
			contents = new JPanel(new BorderLayout());
			iconLabel = new JLabel();
			titleLabel = new JLabel("");
			JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			titlePanel.add(iconLabel);
			titlePanel.add(titleLabel);
			add(titlePanel, BorderLayout.NORTH);
			add(contents, BorderLayout.CENTER);
		}
		else
			contents = this;
		topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		if (!title)
			topPanel.add(new JLabel(mapElement.getIcon()));
		topPanel.add(new JLabel("Location"));
		location = new JLabel();
		topPanel.add(location);
		contents.add(topPanel, BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane();
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		scrollPane.getViewport().setView(textArea);
		contents.add(scrollPane, BorderLayout.CENTER);
		update();
	}
	
	public void setMapElement(MapElement mapElement) {
		this.mapElement = mapElement;
		if (titleLabel != null)
			titleLabel.setText(mapElement.getName());
		if (iconLabel != null)
			iconLabel.setIcon(mapElement.getIcon());
		update();
	}
	
	public void update() {
		if (mapElement == null)
			return;
		if (mapElement.isPinned()) {
			if (locked.getParent() == null)
				topPanel.add(locked);
		}
		else {
			if (locked.getParent() != null)
				topPanel.remove(locked);
		}
		location.setText(StringUtil.format(mapElement.getLocationInWorld()));
		textArea.setText(mapElement.getState().getAnnotation());
		textArea.setCaretPosition(0);
		revalidate();
	}

}
