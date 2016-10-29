package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.PathState;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Context menu item for adding a path at a point on the landscape.
 *
 */
public class AddPathAction extends MenuItemAction {

	private Vector3 position;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public AddPathAction(ReadOnlyVector3 position) {
		super("Path");
		this.position = new Vector3(position);
	}

	@Override
	protected void run() {
		PathState state = new PathState(position);
		Path path = (Path) ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state, null);
		Dert.getWorldView().getScenePanel().getInputHandler().setPath(path);
	}

}
