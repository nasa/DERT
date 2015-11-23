package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCameraInfo;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCameraInfoManager;
import gov.nasa.arc.dert.view.fieldcamera.FieldCameraView;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides a state object for the FieldCamera
 *
 */
public class FieldCameraState extends ToolState {

	// The FieldCamera definition name
	public String fieldCameraDef;

	// Visibility flags
	public boolean fovVisible, lineVisible;
	public boolean crosshairVisible;

	// Camera viewpoint parameters
	public double azimuth, tilt, height;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public FieldCameraState(ReadOnlyVector3 position) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration()
			.incrementMapElementCount(MapElementState.Type.FieldCamera), MapElementState.Type.FieldCamera, "Camera", 1,
			FieldCamera.defaultColor, FieldCamera.defaultLabelVisible, FieldCamera.defaultPinned, position);
		viewData = new ViewData(-1, -1, 600, 500, false);
		viewData.setVisible(true);
		fieldCameraDef = FieldCamera.defaultDefinition;
		fovVisible = FieldCamera.defaultFovVisible;
		lineVisible = FieldCamera.defaultLineVisible;
		azimuth = FieldCamera.defaultAzimuth;
		tilt = FieldCamera.defaultTilt;
		height = FieldCamera.defaultHeight;
	}

	@Override
	public void dispose() {
		if (mapElement != null) {
			FieldCamera fieldCamera = (FieldCamera) mapElement;
			World.getInstance().getLandscape().removeFieldCamera(fieldCamera);
		}
		super.dispose();
	}

	@Override
	public void save() {
		super.save();
		if (viewData != null) {
			FieldCameraView fcv = (FieldCameraView) viewData.getView();
			crosshairVisible = fcv.isCrosshairVisible();
		}
		if (mapElement != null) {
			FieldCamera fieldCamera = (FieldCamera) mapElement;
			position = new Vector3(fieldCamera.getTranslation());
			fieldCameraDef = fieldCamera.getFieldCameraDefinition();
			fovVisible = fieldCamera.isFovVisible();
			lineVisible = fieldCamera.isLookAtLineVisible();
			azimuth = fieldCamera.getAzimuth();
			tilt = fieldCamera.getElevation();
			height = fieldCamera.getHeight();
		}
	}

	/**
	 * Get the field camera definition information
	 * 
	 * @return
	 */
	public FieldCameraInfo getInfo() {
		try {
			return (FieldCameraInfoManager.getInstance().getFieldCameraInfo(fieldCameraDef));
		} catch (Exception e) {
			System.out.println("Unable to get camera definition " + fieldCameraDef + ", see log.");
			e.printStackTrace();
			return (null);
		}
	}
	
	protected void createView() {
		setView(new FieldCameraView((FieldCameraState) this));
		viewData.createWindow(Dert.getMainWindow(), name, X_OFFSET, Y_OFFSET);
	}

}
