package gov.nasa.arc.dert.scene.tool.fieldcamera;

import gov.nasa.arc.dert.util.StringUtil;

import java.util.Properties;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Data structure that contains the information to define a camera.
 *
 */
public class FieldCameraInfo {

	protected static float[] defaultColor = { 1, 1, 1, 1 };
	protected static double[] defaultMountingOffset = { 0, 0, 0 };
	protected static int[] defaultHeightRange = { 0, 10 };
	protected static int[] defaultPanRange = { -180, 180 };
	protected static int[] defaultTiltRange = { -90, 90 };

	public double fovX, fovY;
	public int pixelWidth, pixelHeight;
	public double tripodHeight, tripodPan, tripodTilt;
	public ReadOnlyColorRGBA color;
	public ReadOnlyVector3 mountingOffset;
	public int[] panRange;
	public int[] tiltRange;
	public int[] heightRange;
	public boolean frustumVisible, footprintVisible;

	/**
	 * Constructor
	 * 
	 * @param properties
	 */
	public FieldCameraInfo(Properties properties) {
		fovX = StringUtil.getDoubleValue(properties, "FovDegreesWidth", true, 45, false);
		fovY = StringUtil.getDoubleValue(properties, "FovDegreesHeight", true, 45, false);
		pixelWidth = StringUtil.getIntegerValue(properties, "FovPixelWidth", true, 1024, false);
		pixelHeight = StringUtil.getIntegerValue(properties, "FovPixelHeight", true, 1024, false);
		float[] farray = StringUtil.getFloatArray(properties, "Color", defaultColor, false);
		color = new ColorRGBA(farray[0], farray[1], farray[2], farray[3]);
		double[] darray = StringUtil.getDoubleArray(properties, "MountingOffset", defaultMountingOffset, false);
		mountingOffset = new Vector3(darray[0], darray[1], darray[2]);

		tripodHeight = StringUtil.getDoubleValue(properties, "TripodHeight", true, 1, false);
		heightRange = StringUtil.getIntegerArray(properties, "TripodHeightRange", defaultHeightRange, false);
		tripodPan = StringUtil.getDoubleValue(properties, "TripodPan", true, 0, false);
		panRange = StringUtil.getIntegerArray(properties, "TripodPanRange", defaultPanRange, false);
		tripodTilt = StringUtil.getDoubleValue(properties, "TripodTilt", true, 0, false);
		tiltRange = StringUtil.getIntegerArray(properties, "TripodTiltRange", defaultTiltRange, false);
	}

}
