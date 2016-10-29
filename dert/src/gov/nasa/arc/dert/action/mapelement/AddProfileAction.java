package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.ProfileState;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Context menu item for adding a profile at a point on the landscape.
 *
 */
public class AddProfileAction extends MenuItemAction {

	private Vector3 position;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public AddProfileAction(ReadOnlyVector3 position) {
		super("Profile");
		this.position = new Vector3(position);
	}

	@Override
	protected void run() {
		ProfileState state = new ProfileState(position);
		ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state, null);
	}
}
