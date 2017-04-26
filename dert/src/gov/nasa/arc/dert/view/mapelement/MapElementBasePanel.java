package gov.nasa.arc.dert.view.mapelement;

import gov.nasa.arc.dert.action.edit.CoordAction;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.ui.CoordTextField;
import gov.nasa.arc.dert.ui.FieldPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Spatial;

/**
 * Provides an abstract base class for all map element panels.
 *
 */
public abstract class MapElementBasePanel extends JPanel {
	
//	protected static ImageIcon locked = Icons.getImageIcon("locked.png");
	
	// Common controls
	protected JPanel topPanel;
	protected JLabel locLabel;
	protected CoordTextField locationText;

	// Helpers
	protected NumberFormat formatter;
	protected Vector3 coord;

	// MapElement being edited
	protected MapElement mapElement;
	protected MapElement parentElement;

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public MapElementBasePanel(MapElement mapElement) {
		setLayout(new BorderLayout());
		coord = new Vector3();
		formatter = new DecimalFormat(Landscape.format);
		this.parentElement = mapElement;
		this.mapElement = mapElement;
		build();
	}

	protected void build() {
		ArrayList<Component> compList = new ArrayList<Component>();
		
		addFields(compList);
		
		add(new FieldPanel(compList), BorderLayout.CENTER);
	}
	
	protected void addFields(ArrayList<Component> compList) {
		locLabel = new JLabel("Location", SwingConstants.RIGHT);
		compList.add(locLabel);
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
		compList.add(locationText);
	}

	protected void setLocation(CoordTextField locationText, JLabel label, ReadOnlyVector3 position) {
		if (position == null) {
			position = World.getInstance().getMarble().getTranslation();
		}
		if (locationText != null) {
			locationText.setLocalValue(position);
//			if (mapElement.isLocked())
//				label.setIcon(locked);
//			else
//				label.setIcon(null);
		}
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
			setLocation(locationText, locLabel, ((Spatial) mapElement).getTranslation());
		}
	}

	/**
	 * Map element was renamed
	 * 
	 * @param mapElement
	 */
	public void updateData(MapElement mapElement) {
	}

	/**
	 * Set the map element to be viewed or edited
	 * 
	 * @param mapElement
	 */
	public void setMapElement(MapElement mapElement) {
		this.mapElement = mapElement;
	}

	public void dispose() {
		if (locationText != null)
			CoordAction.listenerList.remove(locationText);
	}
	
	public void update() {
		updateLocation(mapElement);
	}
}
