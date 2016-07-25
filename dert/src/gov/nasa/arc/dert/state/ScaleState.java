package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.tool.Scale;
import gov.nasa.arc.dert.util.StateUtil;

import java.util.HashMap;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides a state object for a scale bar.
 *
 */
public class ScaleState extends ToolState {

	// Number of cells in each dimension
	public int cellCount;
	
	// Location of center of grid
	public Vector3 location;
	
	// Use the default label (X:xsize, Y:ysize, Z:zsize)
	public boolean autoLabel;

	// Orientation
	public double azimuth, tilt;
	
	public double radius;

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param prefix
	 * @param size
	 * @param color
	 * @param labelVisible
	 * @param pinned
	 * @param position
	 */
	public ScaleState(ReadOnlyVector3 position) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration().incrementMapElementCount(MapElementState.Type.Scale), MapElementState.Type.Scale, "Scale",
			Scale.defaultCellSize, Scale.defaultColor, Scale.defaultLabelVisible);
		location = new Vector3(position);
		this.radius = Scale.defaultRadius;
		this.cellCount = Scale.defaultCellCount;
		this.autoLabel = Scale.defaultAutoLabel;
		azimuth = Scale.defaultAzimuth;
		tilt = Scale.defaultTilt;
	}
	
	/**
	 * Constructor for hash map.
	 */
	public ScaleState(HashMap<String,Object> map) {
		super(map);
		cellCount = StateUtil.getInteger(map, "CellCount", 0);
		radius = StateUtil.getDouble(map, "Radius", 1);
		location = StateUtil.getVector3(map, "Location", Vector3.ZERO);
		autoLabel = StateUtil.getBoolean(map, "AutoLabel", true);
		azimuth = StateUtil.getDouble(map, "Azimuth", Scale.defaultAzimuth);
		tilt = StateUtil.getDouble(map, "Tilt", Scale.defaultTilt);
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof ScaleState))
			return(false);
		ScaleState that = (ScaleState)state;
		if (!super.isEqualTo(that))
			return(false);
		if (this.cellCount != that.cellCount) 
			return(false);
		if (this.radius != that.radius) 
			return(false);
		if (this.autoLabel != that.autoLabel) 
			return(false);
		if (this.azimuth != that.azimuth) 
			return(false);
		if (this.tilt != that.tilt)
			return(false);
		if (!this.location.equals(that.location)) 
			return(false);
		return(true);
	}

	@Override
	public HashMap<String,Object> save() {
		HashMap<String,Object> map = super.save();
		if (mapElement != null) {
			Scale scale = (Scale)mapElement;
			cellCount = scale.getCellCount();
			autoLabel = scale.isAutoLabel();
			radius = scale.getCellRadius();
			location = new Vector3(((Scale)mapElement).getTranslation());
			azimuth = scale.getAzimuth();
			tilt = scale.getTilt();
		}
		map.put("CellCount", new Integer(cellCount));
		map.put("Radius", new Double(radius));
		map.put("AutoLabel", new Boolean(autoLabel));
		map.put("Azimuth", new Double(azimuth));
		map.put("Tilt", new Double(tilt));
		StateUtil.putVector3(map, "Location", location);
		return(map);
	}
	
	@Override
	public String toString() {
		String str = "["+cellCount+","+radius+","+location+"] "+super.toString();
		return(str);
	}
}
