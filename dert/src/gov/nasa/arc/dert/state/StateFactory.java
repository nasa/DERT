package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.util.StateUtil;

import java.util.Map;

public class StateFactory {
	
	public static enum DefaultState {AnimationState, ColorBarsState, ConsoleState, HelpState, LightingState, LightPositionState, MapElementsState, MarbleState, SurfaceAndLayersState, ViewpointState, WorldState}
	
	public StateFactory() {
	}
	
	public State createState(DefaultState key) {
		return(createState(key.toString(), null));
	}
	
	public State createState(String key, Map<String,Object> map) {
		
		DefaultState sName = DefaultState.valueOf(key);
		
		if (sName != null) {
			switch (sName) {
			case AnimationState:
				if (map == null)
					return(new AnimationState());
				return(new AnimationState(map));
			case ColorBarsState:
				if (map == null)
					return(new ColorBarsState());
				return(new ColorBarsState(map));
			case ConsoleState:
				if (map == null)
					return(new ConsoleState());
				return(new ConsoleState(map));
			case HelpState:
				if (map == null)
					return(new HelpState());
				return(new HelpState(map));
			case LightingState:
				if (map == null)
					return(new LightingState());
				return(new LightingState(map));
			case LightPositionState:
				if (map == null)
					return(new LightPositionState());
				return(new LightPositionState(map));
			case MapElementsState:
				if (map == null)
					return(new MapElementsState());
				return(new MapElementsState(map));
			case MarbleState:
				if (map == null)
					return(new MarbleState());
				return(new MarbleState(map));
			case SurfaceAndLayersState:
				if (map == null)
					return(new SurfaceAndLayersState());
				return(new SurfaceAndLayersState(map));
			case ViewpointState:
				if (map == null)
					return(new ViewpointState());
				return(new ViewpointState(map));
			case WorldState:
				if (map == null)
					return(new WorldState());
				return(new WorldState(map));
			}
		}
		
		return(null);
	}		

	public MapElementState createMapElementState(Map<String,Object> map) {
		if (map == null)
			return(null);
		String str = StateUtil.getString(map, "MapElementType", null);
		if (str != null) {
			try {
				MapElementState.Type type = MapElementState.Type.valueOf(str);
				if (type != null)
					switch (type) {
					case Placemark:
						return(new PlacemarkState(map));
					case Figure:
						return(new FigureState(map));
					case Billboard:
						return(new ImageBoardState(map));
					case FeatureSet:
						return(new FeatureSetState(map));
					case Path:
						return(new PathState(map));
					case Plane:
						return(new PlaneState(map));
					case CartesianGrid:
						return(new GridState(map));
					case RadialGrid:
						return(new GridState(map));
					case Profile:
						return(new ProfileState(map));
					case FieldCamera:
						return(new FieldCameraState(map));
					case Waypoint:
						return(new WaypointState(map));
					case Marble:
						return(new MarbleState(map));
					case Scale:
						return(new ScaleBarState(map));
					case Feature:
						break;
					}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return(null);
	}

}
