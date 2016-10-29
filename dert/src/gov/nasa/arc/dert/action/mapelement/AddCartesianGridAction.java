package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.GridState;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Context menu item for adding a Cartesian grid at a point on the landscape.
 *
 */
public class AddCartesianGridAction extends MenuItemAction {

	private Vector3 position;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public AddCartesianGridAction(ReadOnlyVector3 position) {
		super("Cartesian Grid");
		this.position = new Vector3(position);
	}

	@Override
	protected void run() {
		GridState state = GridState.createCartesianGridState(position);
		ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state, null);
	}

}
