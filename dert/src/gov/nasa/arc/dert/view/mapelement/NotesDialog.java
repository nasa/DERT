package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.ui.AbstractDialog;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
	protected JLabel locLabel;
	protected JButton saveButton;
	protected boolean showLocation;

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
		JPanel notesPanel = new JPanel(new BorderLayout());
		buildLocation(notesPanel);
		JScrollPane scrollPane = new JScrollPane();
		textArea = new JTextArea();
		textArea.setEditable(true);
		textArea.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent event) {
				saveButton.setEnabled(true);
			}

			@Override
			public void insertUpdate(DocumentEvent event) {
				saveButton.setEnabled(true);
			}

			@Override
			public void removeUpdate(DocumentEvent event) {
				saveButton.setEnabled(true);
			}
		});
		textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		scrollPane.getViewport().setView(textArea);
		notesPanel.add(scrollPane, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				mapElement.getState().setAnnotation(textArea.getText());
				saveButton.setEnabled(false);
			}
		});
		bottomPanel.add(saveButton, BorderLayout.EAST);
		notesPanel.add(bottomPanel, BorderLayout.SOUTH);	
		
		contentArea.add(notesPanel, BorderLayout.CENTER);
		update();
	}
	
	protected void buildLocation(JPanel panel) {
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		topPanel.add(new JLabel("Location: ", SwingConstants.RIGHT), BorderLayout.WEST);
		location = new JLabel();
		topPanel.add(location, BorderLayout.CENTER);
		lockLabel = new JLabel(new ImageIcon());
		topPanel.add(lockLabel, BorderLayout.EAST);
		panel.add(topPanel, BorderLayout.NORTH);		
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
		saveButton.setEnabled(false);
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
		update();
	}
}
