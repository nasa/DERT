package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.ImageBoardState;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Context menu item for adding a billboard at a point on the landscape.
 *
 */
public class AddBillboardAction extends MenuItemAction {

	private Vector3 position;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public AddBillboardAction(ReadOnlyVector3 position) {
		super("Billboard");
		this.position = new Vector3(position);
	}

	@Override
	protected void run() {
		ImageBoardState iState = new ImageBoardState(position);
		ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(iState);
	}

}
