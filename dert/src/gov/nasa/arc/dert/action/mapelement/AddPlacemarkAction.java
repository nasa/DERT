package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.PlacemarkState;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Context menu item for adding a placemark at a point on the landscape.
 *
 */
public class AddPlacemarkAction extends MenuItemAction {

	private ReadOnlyVector3 position;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public AddPlacemarkAction(ReadOnlyVector3 position) {
		super("Placemark");
		this.position = new Vector3(position);
	}

	@Override
	protected void run() {
		PlacemarkState pState = new PlacemarkState(position);
		ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(pState, null);
	}

}
