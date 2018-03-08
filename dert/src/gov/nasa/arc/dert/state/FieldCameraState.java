package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCameraInfo;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCameraInfoManager;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.fieldcamera.FieldCameraView;

import java.util.Map;

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
	
	// Location of camera
	public Vector3 location;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public FieldCameraState(ReadOnlyVector3 position) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration()
			.incrementMapElementCount(MapElementState.Type.FieldCamera), MapElementState.Type.FieldCamera, "Camera", 1,
			FieldCamera.defaultColor, FieldCamera.defaultLabelVisible);
		viewData = new ViewData(600, 500, false);
		viewData.setVisible(true);
		fieldCameraDef = FieldCamera.defaultDefinition;
		fovVisible = FieldCamera.defaultFovVisible;
		lineVisible = FieldCamera.defaultLineVisible;
		azimuth = Double.NaN;
		tilt = Double.NaN;
		height = Double.NaN;
		location = new Vector3(position);
	}
	
	/**
	 * Constructor for hash map.
	 */
	public FieldCameraState(Map<String,Object> map) {
		super(map);
		crosshairVisible = StateUtil.getBoolean(map, "CrosshairVisible", false);
		location = StateUtil.getVector3(map, "Location", Vector3.ZERO);
		fieldCameraDef = StateUtil.getString(map, "FieldCameraDefinition", FieldCamera.defaultDefinition);
		fovVisible = StateUtil.getBoolean(map, "FovVisible", FieldCamera.defaultFovVisible);
		lineVisible = StateUtil.getBoolean(map, "LineVisible", FieldCamera.defaultLineVisible);
		azimuth = StateUtil.getDouble(map, "Azimuth", Double.NaN);
		tilt = StateUtil.getDouble(map, "Tilt", Double.NaN);
		height = StateUtil.getDouble(map, "Height", Double.NaN);
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof FieldCameraState))
			return(false);
		FieldCameraState that = (FieldCameraState)state;
		if (!super.isEqualTo(that))
			return(false);
		if (!this.fieldCameraDef.equals(that.fieldCameraDef))
			return(false);
		if (this.fovVisible != that.fovVisible) 
			return(false);
		if (this.lineVisible != that.lineVisible) 
			return(false);
		if (this.crosshairVisible != that.crosshairVisible) 
			return(false);
		if (this.azimuth != that.azimuth) 
			return(false);
		if (this.tilt != that.tilt) 
			return(false);
		if (this.height != that.height) 
			return(false);
		if (!this.location.equals(that.location)) 
			return(false);
		return(true);		
	}

	@Override
	public void dispose() {
		if (mapElement != null) {
			FieldCamera fieldCamera = (FieldCamera) mapElement;
			Landscape.getInstance().removeFieldCamera(fieldCamera.getName());
		}
		super.dispose();
	}

	@Override
	public Map<String,Object> save() {
		Map<String,Object> map = super.save();
		if (mapElement != null) {
			FieldCamera fieldCamera = (FieldCamera) mapElement;
			location = new Vector3(fieldCamera.getTranslation());
			fieldCameraDef = fieldCamera.getFieldCameraDefinition();
			fovVisible = fieldCamera.getSyntheticCameraNode().isFovVisible();
			lineVisible = fieldCamera.getSyntheticCameraNode().isSiteLineVisible();
			crosshairVisible = fieldCamera.getSyntheticCameraNode().isCrosshairVisible();
			azimuth = fieldCamera.getAzimuth();
			tilt = fieldCamera.getElevation();
			height = fieldCamera.getHeight();
		}
		map.put("CrosshairVisible", new Boolean(crosshairVisible));
		StateUtil.putVector3(map, "Location", location);
		map.put("FieldCameraDefinition", fieldCameraDef);
		map.put("FovVisible", new Boolean(fovVisible));
		map.put("LineVisible", new Boolean(lineVisible));
		map.put("Azimuth", new Double(azimuth));
		map.put("Tilt", new Double(tilt));
		map.put("Height", new Double(height));
		return(map);
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
//		viewData.createWindow(Dert.getMainWindow(), "DERT "+name, X_OFFSET, Y_OFFSET);
		viewData.createWindow(Dert.getMainWindow(), "DERT "+name);
	}
	
	@Override
	public String toString() {
		String str = super.toString();
		str = "["+fieldCameraDef+","+fovVisible+","+lineVisible+","+crosshairVisible+","+azimuth+","+tilt+","+height+","+location+"] "+str;
		return(str);
	}

}
