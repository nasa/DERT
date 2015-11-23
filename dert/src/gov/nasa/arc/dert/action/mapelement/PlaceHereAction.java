package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scene.tool.Profile;
import gov.nasa.arc.dert.view.world.MoveEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JOptionPane;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Spatial;

/**
 * Context menu item for placing a map element at a point in the landscape. User
 * is prompted with a list of map elements.
 *
 */
public class PlaceHereAction extends MenuItemAction {

	private Vector3 position;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public PlaceHereAction(ReadOnlyVector3 position) {
		super("Place Here");
		this.position = new Vector3(position);
	}

	@Override
	protected void run() {
		// Get a list of movable map elements
		List<Spatial> landmarks = World.getInstance().getLandmarks().getChildren();
		List<Spatial> tools = World.getInstance().getTools().getChildren();
		ArrayList<Spatial> list = new ArrayList<Spatial>();
		for (int i = 0; i < landmarks.size(); ++i) {
			list.add(landmarks.get(i));
		}
		for (int i = 0; i < tools.size(); ++i) {
			Spatial spat = tools.get(i);
			if (!((spat instanceof Path) || (spat instanceof Profile))) {
				list.add(spat);
			}
		}
		if (list.size() == 0) {
			return;
		}

		// Sort the list alphabetically
		Collections.sort(list, new Comparator<Spatial>() {
			@Override
			public int compare(Spatial me1, Spatial me2) {
				return (me1.getName().compareTo(me2.getName()));
			}

			@Override
			public boolean equals(Object obj) {
				return (this == obj);
			}
		});
		Spatial[] spatials = new Spatial[list.size()];
		list.toArray(spatials);

		// ask user to select one
		Spatial spatial = (Spatial) JOptionPane.showInputDialog(Dert.getMainWindow(), "Select a Map Element",
			"Place Here", JOptionPane.PLAIN_MESSAGE, Icons.getImageIcon("dert_24.png"), spatials, spatials[0]);

		// move the map element and hand it to the undo handler
		if (spatial != null) {
			Vector3 trans = new Vector3(spatial.getTranslation());
			spatial.setTranslation(position);
			Dert.getMainWindow().getUndoHandler().addEdit(new MoveEdit(spatial, trans));
		}
	}

}
