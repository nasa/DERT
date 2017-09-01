package gov.nasa.arc.dert.scene.tool.fieldcamera;

import gov.nasa.arc.dert.util.StringUtil;

import java.util.Properties;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Data structure that contains the information to define a camera.
 *
 */
public class FieldCameraInfo {

	protected static double[] defaultMountingOffset = { 0, 0, 0 };
	protected static double[] defaultHeightRange = { 0, 10 };
	protected static double[] defaultPanRange = { -180, 180 };
	protected static double[] defaultTiltRange = { -90, 90 };

	public double fovX, fovY;
	public int pixelWidth, pixelHeight;
	public double tripodHeight, tripodPan, tripodTilt;
	public ReadOnlyVector3 mountingOffset;
	public double[] panRange;
	public double[] tiltRange;
	public double[] heightRange;
	public boolean frustumVisible, footprintVisible;
	public String name;

	/**
	 * Constructor
	 * 
	 * @param properties
	 */
	public FieldCameraInfo(String name, Properties properties) {
		this.name = name;
		fovX = StringUtil.getDoubleValue(properties, "FovDegreesWidth", true, 45, false);
		fovY = StringUtil.getDoubleValue(properties, "FovDegreesHeight", true, 45, false);
		pixelWidth = StringUtil.getIntegerValue(properties, "FovPixelWidth", true, 1024, false);
		pixelHeight = StringUtil.getIntegerValue(properties, "FovPixelHeight", true, 1024, false);
		double[] darray = StringUtil.getDoubleArray(properties, "MountingOffset", defaultMountingOffset, false);
		mountingOffset = new Vector3(darray[0], darray[1], darray[2]);

		tripodHeight = StringUtil.getDoubleValue(properties, "TripodHeight", true, 1, false);
		heightRange = StringUtil.getDoubleArray(properties, "TripodHeightRange", defaultHeightRange, false);
		tripodPan = StringUtil.getDoubleValue(properties, "TripodPan", true, 0, false);
		panRange = StringUtil.getDoubleArray(properties, "TripodPanRange", defaultPanRange, false);
		tripodTilt = StringUtil.getDoubleValue(properties, "TripodTilt", true, 0, false);
		tiltRange = StringUtil.getDoubleArray(properties, "TripodTiltRange", defaultTiltRange, false);
	}

}
