package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.scene.featureset.FeatureSet;
import gov.nasa.arc.dert.scene.landmark.Landmark;
import gov.nasa.arc.dert.scene.tool.Tool;
import gov.nasa.arc.dert.scene.tool.Waypoint;

import java.awt.Component;
import java.awt.Font;

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

	/**
	 * Constructor
	 */
	public MapElementTreeCellRenderer() {
		super();
		setOpenIcon(null);
		setClosedIcon(null);
		setLeafIcon(null);
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
		} else if (value instanceof Landmark) {
			Landmark landmark = (Landmark) value;
			if (!landmark.isVisible()) {
				setFont(italicFont);
			} else {
				setFont(defaultFont);
			}
			setIcon(landmark.getIcon());
			setText(landmark.getName());
		} else if (value instanceof Tool) {
			Tool tool = (Tool) value;
			if (!tool.isVisible()) {
				setFont(italicFont);
			} else {
				setFont(defaultFont);
			}
			setIcon(tool.getIcon());
			setText(tool.getName());
		} else if (value instanceof Waypoint) {
			Waypoint waypoint = (Waypoint) value;
			if (!waypoint.isVisible()) {
				setFont(italicFont);
			} else {
				setFont(defaultFont);
			}
			setIcon(null);
			setText(waypoint.getName());
		} else if (value instanceof FeatureSet) {
			FeatureSet vectorGroup = (FeatureSet) value;
			setIcon(vectorGroup.getIcon());
			setText(vectorGroup.getName());
		}
		return (this);
	}

}
