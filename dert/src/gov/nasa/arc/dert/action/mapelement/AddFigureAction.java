package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.FigureState;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Context menu item for adding a 3D figure at a point on the landscape.
 *
 */
public class AddFigureAction extends MenuItemAction {

	private Vector3 position, normal;

	/**
	 * Constructor
	 * 
	 * @param position
	 * @param normal
	 */
	public AddFigureAction(ReadOnlyVector3 position, ReadOnlyVector3 normal) {
		super("3D Figure");
		this.position = new Vector3(position);
		this.normal = new Vector3(normal);
	}

	@Override
	protected void run() {
		FigureState fState = new FigureState(position, normal);
		ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(fState);
	}

}
