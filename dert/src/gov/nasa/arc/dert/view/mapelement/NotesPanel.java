package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

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
		topPanel = new JPanel();
		buildLocation(topPanel);
		contents.add(topPanel, BorderLayout.NORTH);
		JPanel notesPanel = new JPanel(new BorderLayout());
		notesPanel.add(new JLabel("Notes", SwingConstants.LEFT), BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		scrollPane.getViewport().setView(textArea);
		notesPanel.add(scrollPane, BorderLayout.CENTER);
		contents.add(notesPanel, BorderLayout.CENTER);
		update();
	}
	
	protected void buildLocation(JPanel panel) {
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		if (iconLabel == null)
			panel.add(new JLabel(mapElement.getIcon()));
		panel.add(new JLabel("Location", SwingConstants.LEFT));
		location = new JLabel();
		panel.add(location);
	}
	
	protected void updateLocation() {
		if (mapElement.isPinned()) {
			if (locked.getParent() == null)
				topPanel.add(locked);
		}
		else {
			if (locked.getParent() != null)
				topPanel.remove(locked);
		}
		location.setText(StringUtil.format(mapElement.getLocationInWorld()));
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
		updateLocation();
		textArea.setText(mapElement.getState().getAnnotation());
		textArea.setCaretPosition(0);
		revalidate();
	}
	
	public Insets getInsets() {
		return(new Insets(5, 5, 5, 5));
	}

}
