package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.MapElement;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Renders an entry in the JTree used to list the MapElements in the
 * MapElementsView. Hidden map elements are displayed in italics.
 *
 */
public class MapElementTreeCellRenderer extends DefaultTreeCellRenderer {

	private Font defaultFont;
	private Font boldFont;
	private Font italicFont;
	private JLabel lockedLabel;
	private ImageIcon lockedIcon;
	private JPanel panel;

	/**
	 * Constructor
	 */
	public MapElementTreeCellRenderer() {
		super();
		setOpenIcon(null);
		setClosedIcon(null);
		setLeafIcon(null);
		lockedIcon = Icons.getImageIcon("locked.png");
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		panel.setBorder(BorderFactory.createEmptyBorder());
		lockedLabel = new JLabel(lockedIcon);
		panel.setOpaque(false);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
		boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		value = ((DefaultMutableTreeNode) value).getUserObject();
		if (defaultFont == null) {
			// can't use deriveFont here
			defaultFont = getFont();
			boldFont = new Font(defaultFont.getFontName(), Font.BOLD, defaultFont.getSize());
			italicFont = new Font(defaultFont.getFontName(), Font.ITALIC, defaultFont.getSize());
		}
		if (value instanceof String) {
			setFont(boldFont);
			return(this);
		}
		else if (value instanceof MapElement) {
			MapElement mapElement = (MapElement)value;
			if (!mapElement.isVisible()) {
				setFont(italicFont);
			} else {
				setFont(defaultFont);
			}
			setIcon(mapElement.getIcon());
			setText(mapElement.getName());
			panel.removeAll();
			panel.add(this);
			if (mapElement.isPinned())
				panel.add(lockedLabel);
			return(panel);
		}
		return(null);
	}

}
