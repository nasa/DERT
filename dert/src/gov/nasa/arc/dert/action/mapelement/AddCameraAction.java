package gov.nasa.arc.dert.action.mapelement;

import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.FieldCameraState;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Context menu item for adding a camera at a point on the landscape.
 *
 */
public class AddCameraAction extends MenuItemAction {

	private Vector3 position;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public AddCameraAction(ReadOnlyVector3 position) {
		super("Camera");
		this.position = new Vector3(position);
	}

	@Override
	protected void run() {
		FieldCameraState state = new FieldCameraState(position);
		ConfigurationManager.getInstance().getCurrentConfiguration().addMapElementState(state);
		World.getInstance().getLandscape().getLayerManager().addFieldCamera((FieldCamera) state.getMapElement());
	}

}
