package gov.nasa.arc.dert.view.viewpoint;

import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class ViewpointListCellRenderer
	extends DefaultListCellRenderer {

	private Font defaultFont;
	private Font italicFont;
	
	private boolean isEditing;
	
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (defaultFont == null) {
			// can't use deriveFont here
			defaultFont = getFont();
			italicFont = new Font(defaultFont.getFontName(), Font.ITALIC, defaultFont.getSize());
		}
		setText(value.toString());
		if (isSelected) {
			if (isEditing)
				setFont(italicFont);
			else
				setFont(defaultFont);				
		}
		else
			setFont(defaultFont);
		return(this);
	}
	
	public void setEditing(boolean isEditing) {
		this.isEditing = isEditing;
	}

}
