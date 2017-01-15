package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.action.edit.CoordAction;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.ui.CoordTextField;
import gov.nasa.arc.dert.ui.GroupPanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Provides an abstract base class for all map element panels.
 *
 */
public abstract class MapElementBasePanel extends JPanel {
	
	// Lock icon
	private ImageIcon lockedIcon = Icons.getImageIcon("locked.png");
	private ImageIcon unlockedIcon = Icons.getImageIcon("unlocked.png");

	// Common controls
	protected JPanel contents;
	protected JTextArea noteText;
	protected JButton saveButton, groundButton;
	protected JCheckBox pinnedCheckBox, labelCheckBox;
	protected JLabel typeLabel, nameLabel;
	protected CoordTextField locationText;
	protected JPanel container;

	// Map element icon and type
	protected Icon icon;
	protected String type;

	// Helpers
	protected NumberFormat formatter;
	protected Vector3 coord;

	// Parent panel
	protected MapElementsPanel parent;

	// MapElement being edited
	protected MapElement mapElement;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public MapElementBasePanel(MapElementsPanel parent) {
		this.parent = parent;
		setLayout(new BorderLayout());
		coord = new Vector3();
		formatter = new DecimalFormat(Landscape.format);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	}

	protected void build(boolean addNotes, boolean addLoc, boolean addCBs) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		typeLabel = new JLabel(icon);
		panel.add(typeLabel);
		nameLabel = new JLabel("              ");
		Font font = nameLabel.getFont();
		font = font.deriveFont(Font.BOLD);
		nameLabel.setFont(font);
		panel.add(nameLabel);
		add(panel, BorderLayout.NORTH);

		container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		if (addLoc) {
			panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(new JLabel("Location"));
			locationText = new CoordTextField(22, "location of map element", Landscape.format, true) {
				@Override
				public void handleChange(Vector3 store) {
					if (mapElement instanceof Path)
						return;
					super.handleChange(store);
				}
				@Override
				public void doChange(ReadOnlyVector3 result) {
					Movable movable = (Movable)mapElement;
					double z = Landscape.getInstance().getZ(result.getX(), result.getY());
					if (Double.isNaN(z)) {
						Toolkit.getDefaultToolkit().beep();
						return;
					}
					if (Double.isNaN(result.getZ())) {
						movable.setLocation(result.getX(), result.getY(), z, true);
					}
					else {
						movable.setZOffset(result.getZ()-z, false);
						movable.setLocation(result.getX(), result.getY(), z, true);
					}
				}
			};
			CoordAction.listenerList.add(locationText);
			panel.add(locationText);
			container.add(panel);
		}

		if (addCBs) {
			panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			pinnedCheckBox = new JCheckBox("Lock in Place");
			pinnedCheckBox.setIcon(unlockedIcon);
			pinnedCheckBox.setSelectedIcon(lockedIcon);
			pinnedCheckBox.setToolTipText("lock map element at current location");
			pinnedCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					mapElement.setPinned(pinnedCheckBox.isSelected());
				}
			});
			panel.add(pinnedCheckBox);
			labelCheckBox = new JCheckBox("Show Label");
			labelCheckBox.setToolTipText("display map element label");
			labelCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					mapElement.setLabelVisible(labelCheckBox.isSelected());
					((Spatial) mapElement).markDirty(DirtyType.RenderState);
				}
			});
			panel.add(labelCheckBox);
			groundButton = new JButton("Ground");
			groundButton.setToolTipText("put map element on terrain surface");
			groundButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					mapElement.ground();
				}
			});
			panel.add(groundButton);

			container.add(panel);
		}

		container.add(new JLabel("  "));

		contents = new JPanel();
		contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));
		container.add(contents);

		if (addNotes) {
			GroupPanel notePanel = new GroupPanel("Notes");
			notePanel.setLayout(new BorderLayout());
			noteText = new JTextArea();
			noteText.setEditable(true);
			noteText.getDocument().addDocumentListener(new DocumentListener() {
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
			notePanel.add(new JScrollPane(noteText), BorderLayout.CENTER);
			saveButton = new JButton("Save Notes");
			saveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					mapElement.getState().setAnnotation(noteText.getText());
					saveButton.setEnabled(false);
				}
			});
			JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			buttonBar.add(saveButton);
			notePanel.add(buttonBar, BorderLayout.SOUTH);
			container.add(notePanel);
		}

		add(container, BorderLayout.CENTER);
	}

	protected void setLocation(CoordTextField locationText, ReadOnlyVector3 position) {
		if (position == null) {
			position = World.getInstance().getMarble().getTranslation();
		}
		locationText.setLocalValue(position);
	}

	/**
	 * Map element was moved
	 * 
	 * @param mapElement
	 */
	public void updateLocation(MapElement mapElement) {
		if (mapElement instanceof Path) {
			return;
		}
		if (locationText != null) {
			setLocation(locationText, ((Spatial) mapElement).getTranslation());
		}
	}

	/**
	 * Map element was renamed
	 * 
	 * @param mapElement
	 */
	public void updateData(MapElement mapElement) {
		nameLabel.setText(mapElement.getName());
		if (pinnedCheckBox != null)
			pinnedCheckBox.setSelected(mapElement.isPinned());
	}

	/**
	 * Set the map element to be viewed or edited
	 * 
	 * @param mapElement
	 */
	public abstract void setMapElement(MapElement mapElement);

	public void dispose() {
		if (locationText != null)
			CoordAction.listenerList.remove(locationText);
	}
}
