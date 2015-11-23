package gov.nasa.arc.dert.action.edit;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.ui.AbstractDialog;
import gov.nasa.arc.dert.ui.ColorSelectionPanel;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.util.UIUtil;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

/**
 * Dialog to edit the background color of the worldview. The dert.properties
 * file provides a list of predefined colors. A color chooser is also available.
 *
 */
public class BackgroundColorDialog extends AbstractDialog {

	/**
	 * Data structure to hold color information.
	 *
	 */
	public static class ColorEntry {
		public String name;
		public ReadOnlyColorRGBA color;

		public ColorEntry(String name, ReadOnlyColorRGBA color) {
			this.name = name;
			this.color = color;
		}

		@Override
		public String toString() {
			return (name);
		}
	}

	// Predefined colors from dert.properties
	protected static ColorEntry[] predefined;
	protected ColorSelectionPanel csp;
	protected ReadOnlyColorRGBA bgCol;

	/**
	 * Constructor
	 */
	public BackgroundColorDialog() {
		super(Dert.getMainWindow(), "Background Color", true, false);
	}

	@Override
	protected void build() {
		super.build();		
		contentArea.setLayout(new FlowLayout());
		bgCol = World.getInstance().getBackgroundColor();
		csp = new ColorSelectionPanel(UIUtil.colorRGBAToColor(bgCol)) {
			public void doColor(Color color) {
				bgCol = UIUtil.colorToColorRGBA(color);
			}
		};
		contentArea.add(csp);
		contentArea.add(new JLabel("  Predefined", SwingConstants.RIGHT));
		JComboBox combo = new JComboBox(predefined);
		contentArea.add(combo);
		combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				JComboBox combo = (JComboBox) event.getSource();
				ColorEntry ce = (ColorEntry) combo.getSelectedItem();
				if (ce.color == null) {
					return;
				}
				csp.setColor(UIUtil.colorRGBAToColor(ce.color));
				bgCol = ce.color;
			}
		});
	}

	/**
	 * Set the background color.
	 */
	@Override
	public boolean okPressed() {
		World.getInstance().setBackgroundColor(bgCol);
		setVisible(false);
		return (true);
	}

	/**
	 * Set predefined colors from dert.properties file.
	 * 
	 * @param dertProperties
	 *            data from dert.properties file
	 */
	public static void setPredefinedBackgroundColors(Properties dertProperties) {
		Object[] key = dertProperties.keySet().toArray();
		ArrayList<ColorEntry> list = new ArrayList<ColorEntry>();
		for (int i = 0; i < key.length; ++i) {
			if (((String) key[i]).startsWith("Background.")) {
				String name = (String) key[i];
				Color color = StringUtil.stringToColor((String) dertProperties.get(name));
				if (color != null) {
					ReadOnlyColorRGBA colorRGBA = UIUtil.colorToColorRGBA(color);
					int p = name.indexOf('.');
					if (p < 0) {
						throw new IllegalArgumentException("Background property " + name
							+ " from dert.properties is invalid.");
					}
					name = name.substring(p + 1);
					if (name.toLowerCase().equals("default")) {
						World.defaultBackgroundColor = colorRGBA;
					} else {
						ColorEntry ce = new ColorEntry(name, colorRGBA);
						list.add(ce);
					}
				}
			}
		}
		list.add(0, new ColorEntry("Default", World.defaultBackgroundColor));
		list.add(0, new ColorEntry("Select", null));
		predefined = new ColorEntry[list.size()];
		list.toArray(predefined);
	}

}
