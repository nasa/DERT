package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.PlaneState;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Context menu item for adding a plane at a point on the landscape.
 *
 */
public class AddPlaneAction extends MenuItemAction {

	private Vector3 position;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public AddPlaneAction(ReadOnlyVector3 position) {
		super("Plane");
		this.position = new Vector3(position);
	}

	@Override
	protected void run() {
		PlaneState state = new PlaneState(position);
		ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state);
	}
}
