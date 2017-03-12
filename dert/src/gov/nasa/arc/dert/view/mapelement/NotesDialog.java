package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.ui.AbstractDialog;

import java.awt.BorderLayout;
import java.awt.Frame;

/**
 * A dialog that displays text in a JTextArea.
 *
 */
public class NotesDialog extends AbstractDialog {

	private MapElement mapElement;
	private NotesPanel notesPanel;

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
		notesPanel = new NotesPanel(mapElement, true);
		contentArea.setLayout(new BorderLayout());
		contentArea.add(notesPanel, BorderLayout.CENTER);
	}

	/**
	 * Set the text to display
	 * 
	 * @param text
	 */
	public void update() {
		notesPanel.update();
	}

	/**
	 * Close the dialog
	 */
	@Override
	public boolean okPressed() {
		return (true);
	}
}
