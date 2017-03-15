package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.ui.AbstractDialog;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * A dialog that displays text in a JTextArea.
 *
 */
public class NotesDialog extends AbstractDialog {
	
	protected static ImageIcon locked = Icons.getImageIcon("locked.png");

	protected MapElement mapElement;
	protected JTextArea textArea;
	protected JLabel location;
	protected JLabel lockLabel;
	protected JLabel titleLabel;

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param title
	 * @param width
	 * @param height
	 * @param addMessage
	 * @param addRefresh
	 */
	public NotesDialog(Frame parent, String title, int width, int height, MapElement mapElement) {
		super(parent, title, false, true, false);
		this.width = width;
		this.height = height;
		this.mapElement = mapElement;
	}

	@Override
	protected void build() {
		super.build();
		contentArea.setLayout(new BorderLayout());
		titleLabel = new JLabel(" ", SwingConstants.LEFT);
		contentArea.add(titleLabel, BorderLayout.NORTH);
		JPanel notesPanel = new JPanel(new BorderLayout());
		buildLocation(notesPanel);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		scrollPane.getViewport().setView(textArea);
		notesPanel.add(scrollPane, BorderLayout.CENTER);
		contentArea.add(notesPanel, BorderLayout.CENTER);
		update();
	}
	
	protected void buildLocation(JPanel panel) {		
		JPanel locPanel = new JPanel(new BorderLayout());
		locPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		locPanel.add(new JLabel("Location: ", SwingConstants.RIGHT), BorderLayout.WEST);
		location = new JLabel();
		locPanel.add(location, BorderLayout.CENTER);
		lockLabel = new JLabel(new ImageIcon());
		locPanel.add(lockLabel, BorderLayout.EAST);
		panel.add(locPanel, BorderLayout.NORTH);
	}
	
	protected void updateLocation() {
		if (mapElement.isLocked())
			lockLabel.setIcon(locked);
		else
			lockLabel.setIcon(null);
		location.setText(StringUtil.format(mapElement.getLocationInWorld()));
	}
	
	protected void updateText() {
		textArea.setText(mapElement.getState().getAnnotation());
		textArea.setCaretPosition(0);
	}
	
	public void update() {
		if (mapElement == null)
			return;
		updateLocation();
		updateText();
		revalidate();
	}

	/**
	 * Close the dialog
	 */
	@Override
	public boolean okPressed() {
		return (true);
	}
	
	public void setMapElement(MapElement me) {
		mapElement = me;
		titleLabel.setIcon(mapElement.getIcon());
		titleLabel.setText(mapElement.getName());
		update();
	}
}
