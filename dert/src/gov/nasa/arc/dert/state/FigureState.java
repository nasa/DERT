package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.landmark.Figure;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.util.StateUtil;

import java.util.HashMap;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides a state object for the Figure.
 *
 */
public class FigureState extends LandmarkState {

	// Surface normal
	public Vector3 normal;

	// Orientation
	public double azimuth, tilt;

	// Type of shape
	public ShapeType shape;

	// Options
	public boolean showNormal, autoScale;

	/**
	 * Constructor
	 * 
	 * @param position
	 * @param normal
	 */
	public FigureState(ReadOnlyVector3 position, ReadOnlyVector3 normal) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration()
			.incrementMapElementCount(MapElementState.Type.Figure), MapElementState.Type.Figure, "Figure",
			Figure.defaultSize, Figure.defaultColor, Figure.defaultLabelVisible, Figure.defaultPinned, position);
		this.normal = new Vector3(normal);
		azimuth = Figure.defaultAzimuth;
		tilt = Figure.defaultTilt;
		shape = Figure.defaultShapeType;
		showNormal = Figure.defaultSurfaceNormalVisible;
		autoScale = Figure.defaultAutoScale;
	}
	
	/**
	 * Constructor for hash map.
	 */
	public FigureState(HashMap<String,Object> map) {
		super(map);
		normal = StateUtil.getVector3(map, "Normal", Vector3.ZERO);
		azimuth = StateUtil.getDouble(map, "Azimuth", Figure.defaultAzimuth);
		tilt = StateUtil.getDouble(map, "Tilt", Figure.defaultTilt);
		String str = StateUtil.getString(map, "Shape", Figure.defaultShapeType.toString());
		try {
			shape = ShapeType.valueOf(str);
		}
		catch (Exception e) {
			shape = Figure.defaultShapeType;
		}
		showNormal = StateUtil.getBoolean(map, "ShowNormal", Figure.defaultSurfaceNormalVisible);
		autoScale = StateUtil.getBoolean(map, "AutoScale", Figure.defaultAutoScale);
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof FigureState)) 
			return(false);
		FigureState that = (FigureState)state;
		if (!super.isEqualTo(that)) 
			return(false);
		if (!this.normal.equals(that.normal)) 
			return(false);
		if (this.azimuth != that.azimuth) 
			return(false);
		if (this.tilt != that.tilt)
			return(false);
		if (this.shape != that.shape) 
			return(false);
		if (this.showNormal != that.showNormal) 
			return(false);
		if (this.autoScale != that.autoScale) 
			return(false);
		return(true);
	}

	@Override
	public HashMap<String,Object> save() {
		HashMap<String,Object> map = super.save();
		if (mapElement != null) {
			Figure figure = (Figure) mapElement;
			normal = new Vector3(figure.getNormal());
			azimuth = figure.getAzimuth();
			tilt = figure.getTilt();
			shape = figure.getShapeType();
			showNormal = figure.isSurfaceNormalVisible();
			autoScale = figure.isAutoScale();
		}
		StateUtil.putVector3(map, "Normal", normal);
		map.put("Azimuth", new Double(azimuth));
		map.put("Tilt", new Double(tilt));
		map.put("Shape", shape.toString());
		map.put("ShowNormal", new Boolean(showNormal));
		map.put("AutoScale", new Boolean(autoScale));
		return(map);
	}
	
	@Override
	public String toString() {
		String str = super.toString();
		str = "["+azimuth+","+tilt+","+normal+","+shape+","+showNormal+","+autoScale+"] "+str;
		return(str);
	}
}
