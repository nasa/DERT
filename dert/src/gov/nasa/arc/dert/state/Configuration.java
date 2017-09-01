package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scene.tool.Waypoint;
import gov.nasa.arc.dert.state.StateFactory.DefaultState;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.mapelement.MapElementsView;
import gov.nasa.arc.dert.view.surfaceandlayers.SurfaceAndLayersView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JTextField;

/**
 * A collection of State objects that represents a snapshot of a DERT session.
 *
 */
public class Configuration {

	// Default states
	public final ConsoleState consoleState;
	public final WorldState worldState;

	// States for MapElements
	public ArrayList<MapElementState> mapElementStateList;

	// A name for this configuration
	protected String label;

	// Current map element counts (for automatic labeling)
	protected int[] mapElementCount;

	// The path to the landscape where this configuration is stored
	protected String landscapePath;
	
	// General maps
	protected ConcurrentHashMap<String, State> stateMap;

	/**
	 * Constructor
	 * 
	 * @param label
	 */
	public Configuration(String label) {
		this.label = label;		
		
		stateMap = new ConcurrentHashMap<String, State>();
		
		StateFactory stateFactory = ConfigurationManager.getInstance().getStateFactory();
		worldState = (WorldState)stateFactory.createState(DefaultState.WorldState);
		stateMap.put("WorldState", worldState);
		consoleState = (ConsoleState)stateFactory.createState(DefaultState.ConsoleState);
		consoleState.viewData.setVisible(true);
		stateMap.put("ConsoleState", consoleState);
		stateMap.put("HelpState", stateFactory.createState(DefaultState.HelpState));
		stateMap.put("MapElementsState", stateFactory.createState(DefaultState.MapElementsState));
		stateMap.put("SurfaceAndLayerState", stateFactory.createState(DefaultState.SurfaceAndLayersState));
		stateMap.put("LightingState", stateFactory.createState(DefaultState.LightingState));
		stateMap.put("ViewpointState", stateFactory.createState(DefaultState.ViewpointState));
		stateMap.put("AnimationState", stateFactory.createState(DefaultState.AnimationState));
		stateMap.put("LightPositionState", stateFactory.createState(DefaultState.LightPositionState));
		stateMap.put("ColorBarsState", stateFactory.createState(DefaultState.ColorBarsState));
		stateMap.put("MarbleState", stateFactory.createState(DefaultState.MarbleState));
		
		mapElementStateList = new ArrayList<MapElementState>();
		mapElementCount = new int[MapElementState.Type.values().length];
	}
	
	public Configuration(HashMap<String,Object> map) {
		
		label = StateUtil.getString(map, "Label", null);				
		
		stateMap = new ConcurrentHashMap<String, State>();
		
		DefaultState[] dState = DefaultState.values();
		
		StateFactory stateFactory = ConfigurationManager.getInstance().getStateFactory();
		for (int i=0; i<dState.length; ++i) {
			State s = stateFactory.createState(dState[i].toString(), (HashMap<String,Object>)map.get(dState[i].toString()));
			if (s == null)
				throw new IllegalStateException("Missing "+dState[i]);
			stateMap.put(dState[i].toString(), s);
		}
		worldState = (WorldState)stateMap.get("WorldState");
		consoleState = (ConsoleState)stateMap.get("ConsoleState");

		mapElementCount = (int[])map.get("MapElementCount");
		int n = StateUtil.getInteger(map, "MapElementStateCount", 0);
		mapElementStateList = new ArrayList<MapElementState>();
		for (int i = 0; i < n; ++i) {
			HashMap<String,Object> meMap = (HashMap<String,Object>)map.get("MapElementState"+i);
			MapElementState meState = stateFactory.createMapElementState(meMap);
			if (meState != null)
				mapElementStateList.add(meState);
		}
		
	}

	/**
	 * Get the current count for the given map element type
	 * 
	 * @param type
	 * @return
	 */
	public long getMapElementCount(MapElementState.Type type) {
		return (mapElementCount[type.ordinal()]);
	}

	/**
	 * Increment the current count for the given map element type
	 * 
	 * @param type
	 * @return
	 */
	public int incrementMapElementCount(MapElementState.Type type) {
		mapElementCount[type.ordinal()]++;
		return (mapElementCount[type.ordinal()]);
	}

	@Override
	public String toString() {
		return (label);
	}

	/**
	 * Set the label
	 * 
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Get the path to the landscape
	 * 
	 * @return
	 */
	public String getLandscapePath() {
		return (landscapePath);
	}

	/**
	 * Set the path to the landscape
	 * 
	 * @param landscapePath
	 */
	public void setLandscapePath(String landscapePath) {
		this.landscapePath = landscapePath;
		worldState.setName(StringUtil.getLabelFromFilePath(landscapePath));
	}

	/**
	 * Add a map element state
	 * 
	 * @param state
	 * @return
	 */
	public MapElement addMapElementState(MapElementState state, JTextField msgField) {
		MapElement mapElement = null;
		if (state instanceof WaypointState) {
			Path path = (Path) ((WaypointState) state).parent.getMapElement();
			mapElement = path.addWaypoint((WaypointState) state);
		} else {
			mapElementStateList.add(state);
			if (state instanceof LandmarkState) {
				mapElement = World.getInstance().getLandmarks().addLandmark((LandmarkState) state, true);
			} else if (state instanceof ToolState) {
				mapElement = World.getInstance().getTools().addTool((ToolState) state, true);
			} else if (state instanceof FeatureSetState) {
				mapElement = World.getInstance().getFeatureSets().addFeatureSet((FeatureSetState) state, true, msgField);
			}
		}
		World world = World.getInstance();
		if (world != null) {
			world.setVerticalExaggeration(mapElement);
		}
		return (mapElement);
	}

	/**
	 * Add a list of map element states
	 * 
	 * @param state
	 */
	public void addMapElementState(MapElementState[] state) {
		for (int i = 0; i < state.length; ++i) {
			addMapElementState(state[i], null);
		}
	}

	/**
	 * Remove a map element state
	 * 
	 * @param state
	 */
	public void removeMapElementState(MapElementState state) {
		if (state instanceof WaypointState) {
			Waypoint wp = (Waypoint) ((WaypointState) state).getMapElement();
			Path path = wp.getPath();
			path.removeWaypoint(wp);
		} else {
			ViewData viewData = state.viewData;
			if (viewData != null) {
				viewData.close();
			}
			mapElementStateList.remove(state);
			state.dispose();
		}
	}

	/**
	 * Remove a list of map element states
	 * 
	 * @param state
	 */
	public void removeMapElementState(MapElementState[] state) {
		for (int i = 0; i < state.length; ++i) {
			removeMapElementState(state[i]);
		}
	}

	/**
	 * Save all states
	 */
	public HashMap<String,Object> save() {
		HashMap<String,Object> map = new HashMap<String,Object>();
		
		map.put("Label", label);
		map.put("LandscapePath", landscapePath);
		
		Object[] key = stateMap.keySet().toArray();
		for (int i=0; i<key.length; ++i) {
			map.put((String)key[i], stateMap.get(key[i]).save());
		}

		map.put("MapElementCount", mapElementCount);
		map.put("MapElementStateCount", new Integer(mapElementStateList.size()));
		for (int i = 0; i < mapElementStateList.size(); ++i)
			map.put("MapElementState"+i, mapElementStateList.get(i).save());		
		
		return(map);
	}

	/**
	 * Find a map element state with the given type and name
	 * 
	 * @param type
	 * @param name
	 * @return
	 */
	public MapElementState findMapElementState(MapElementState.Type type, String name) {
		for (int i = 0; i < mapElementStateList.size(); ++i) {
			MapElementState state = mapElementStateList.get(i);
			if (state.mapElementType == type) {
				if (name == null) {
					return (state);
				} else if (state.name.equals(name)) {
					return (state);
				}
			}
		}
		return (null);
	}

	/**
	 * Get all the Landmark states
	 * 
	 * @return
	 */
	public ArrayList<LandmarkState> getLandmarkStates() {
		ArrayList<LandmarkState> states = new ArrayList<LandmarkState>();
		for (int i = 0; i < mapElementStateList.size(); ++i) {
			State state = mapElementStateList.get(i);
			if (state instanceof LandmarkState) {
				states.add((LandmarkState) state);
			}
		}
		return (states);
	}

	/**
	 * Get all the FeatureSet states
	 * 
	 * @return
	 */
	public ArrayList<FeatureSetState> getFeatureSetStates() {
		ArrayList<FeatureSetState> states = new ArrayList<FeatureSetState>();
		for (int i = 0; i < mapElementStateList.size(); ++i) {
			State state = mapElementStateList.get(i);
			if (state instanceof FeatureSetState) {
				states.add((FeatureSetState) state);
			}
		}
		return (states);
	}

	/**
	 * Get all the tool states
	 * 
	 * @return
	 */
	public ArrayList<ToolState> getToolStates() {
		ArrayList<ToolState> list = new ArrayList<ToolState>();
		for (int i = 0; i < mapElementStateList.size(); ++i) {
			State state = mapElementStateList.get(i);
			if (state instanceof ToolState) {
				list.add((ToolState) state);
			}
		}
		return (list);
	}

	/**
	 * Close all views
	 */
	public void closeViews() {
		
		Object[] key = stateMap.keySet().toArray();
		for (int i=0; i<key.length; ++i) {
			if (key[i].equals("WorldState"))
				continue;
			ViewData viewData = stateMap.get(key[i]).viewData;
			if (viewData != null)
				viewData.close();
		}
		
		for (int i = 0; i < mapElementStateList.size(); ++i) {
			State state = mapElementStateList.get(i);
			ViewData viewData = state.viewData;
			if (viewData != null) {
				viewData.close();
			}
		}
	}

	/**
	 * Open all views
	 */
	public void openViews() {
		
		Object[] key = stateMap.keySet().toArray();
		for (int i=0; i<key.length; ++i) {
			State state = stateMap.get(key[i]);
			if ((state.viewData != null) && state.viewData.isVisible())
				state.open(false);
		}
		
		for (int i = 0; i < mapElementStateList.size(); ++i) {
			MapElementState state = mapElementStateList.get(i);
			state.open(false);
		}
	}

	/**
	 * Get the surface and layers view
	 * 
	 * @return
	 */
	public SurfaceAndLayersView getSurfaceAndLayersView() {
		SurfaceAndLayersState state = (SurfaceAndLayersState)getState(DefaultState.SurfaceAndLayersState.toString());
		return ((SurfaceAndLayersView) state.viewData.view);
	}

	/**
	 * Get the map elements view
	 * 
	 * @return
	 */
	public MapElementsView getMapElementsView() {
		MapElementsState state = (MapElementsState)getState(DefaultState.MapElementsState.toString());
		return ((MapElementsView) state.viewData.view);
	}
	
	public void addState(String key, State state) {
		stateMap.put(key, state);
	}
	
	public void removeState(String key) {
		stateMap.remove(key);
	}
	
	public State getState(String key) {
		return(stateMap.get(key));
	}
}
