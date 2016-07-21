package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scene.tool.Waypoint;
import gov.nasa.arc.dert.state.PanelState.PanelType;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.surfaceandlayers.SurfaceAndLayersView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A collection of State objects that represents a snapshot of a DERT session.
 *
 */
public class Configuration {

	// States for views
	public final PanelState helpState;
	public final PanelState surfAndLayerState;
	public final MapElementsState mapElementsState;
	public final PanelState colorBarsState;
	public final PanelState lightingState;
	public final PanelState lightPosState;
	public final ViewpointState viewPtState;
	public final PanelState consoleState;

	// The world state
	public final WorldState worldState;

	// The marble state
	public final MarbleState marbleState;

	// States for MapElements
	public ArrayList<MapElementState> mapElementStateList;

	// A name for this configuration
	protected String label;

	// Current map element counts (for automatic labeling)
	protected int[] mapElementCount;

	// The path to the landscape where this configuration is stored
	protected transient String landscapePath;

	/**
	 * Constructor
	 * 
	 * @param label
	 */
	public Configuration(String label) {
		this.label = label;
		worldState = new WorldState((String)null);
		consoleState = new PanelState(PanelType.Console, "DERT Console", new ViewData(-1, 624, 960, 250, false));
		consoleState.viewData.setVisible(true);
		helpState = new PanelState(PanelType.Help, "DERT Help", new ViewData(-1, -1, ViewData.DEFAULT_WINDOW_WIDTH, ViewData.DEFAULT_WINDOW_HEIGHT, false));
		mapElementsState = new MapElementsState();
		surfAndLayerState = new PanelState(PanelType.SurfaceAndLayers, "DERT Surface and Layers", new ViewData(-1, -1, -1, -1, false));
		lightingState = new PanelState(PanelType.Lighting, "DERT Lighting and Shadows", new ViewData(-1, -1, -1, -1, false));
		viewPtState = new ViewpointState();
		lightPosState = new PanelState(PanelType.LightPosition, "DERT Light Position", new ViewData(-1, -1, -1, -1, false));
		colorBarsState = new PanelState(PanelType.ColorBars, "DERT Color Bars", new ViewData(-1, -1, 700, 200, false));
		marbleState = new MarbleState();
		mapElementStateList = new ArrayList<MapElementState>();
		mapElementCount = new int[MapElementState.Type.values().length];
	}
	
	public Configuration(HashMap<String,Object> map) {
		label = StateUtil.getString(map, "Label", null);
		mapElementCount = (int[])map.get("MapElementCount");
		consoleState = new PanelState((HashMap<String,Object>)map.get("ConsoleState"));
		marbleState = new MarbleState((HashMap<String,Object>)map.get("MarbleState"));
		worldState = new WorldState((HashMap<String,Object>)map.get("WorldState"));
		helpState = new PanelState((HashMap<String,Object>)map.get("HelpState"));
		surfAndLayerState = new PanelState((HashMap<String,Object>)map.get("SurfaceAndLayerState"));
		mapElementsState = new MapElementsState((HashMap<String,Object>)map.get("MapElementsState"));
		colorBarsState = new PanelState((HashMap<String,Object>)map.get("ColorBarsState"));
		lightingState = new PanelState((HashMap<String,Object>)map.get("LightingState"));
		lightPosState = new PanelState((HashMap<String,Object>)map.get("LightPositionState"));
		viewPtState = new ViewpointState((HashMap<String,Object>)map.get("ViewpointState"));

		int n = StateUtil.getInteger(map, "MapElementStateCount", 0);
		mapElementStateList = new ArrayList<MapElementState>();
		for (int i = 0; i < n; ++i) {
			HashMap<String,Object> meMap = (HashMap<String,Object>)map.get("MapElementState"+i);
			String str = StateUtil.getString(meMap, "MapElementType", null);
			if (str != null) {
				try {
					MapElementState.Type type = MapElementState.Type.valueOf(str);
					if (type != null)
						switch (type) {
						case Placemark:
							mapElementStateList.add(new PlacemarkState(meMap));
							break;
						case Figure:
							mapElementStateList.add(new FigureState(meMap));
							break;
						case Billboard:
							mapElementStateList.add(new ImageBoardState(meMap));
							break;
						case LineSet:
							mapElementStateList.add(new LineSetState(meMap));
							break;
						case Path:
							mapElementStateList.add(new PathState(meMap));
							break;
						case Plane:
							mapElementStateList.add(new PlaneState(meMap));
							break;
						case CartesianGrid:
							mapElementStateList.add(new GridState(meMap));
							break;
						case RadialGrid:
							mapElementStateList.add(new GridState(meMap));
							break;
						case Profile:
							mapElementStateList.add(new ProfileState(meMap));
							break;
						case FieldCamera:
							mapElementStateList.add(new FieldCameraState(meMap));
							break;
						case Waypoint:
							mapElementStateList.add(new WaypointState(meMap));
							break;
						case Marble:
							mapElementStateList.add(new MarbleState(meMap));
							break;
						case Scale:
							mapElementStateList.add(new ScaleState(meMap));
							break;
						}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public boolean isEqualTo(Configuration that) {
		if (!this.label.equals(that.label)) {
			System.err.println("Configuration labels not eaual ("+this.label+","+that.label+")");
			return(false);
		}
		if (this.mapElementCount.length != that.mapElementCount.length) {
			System.err.println("Configuration map element counts not equal ("+this.mapElementCount.length+","+that.mapElementCount.length+")");
			return(false);
		}
		for (int i=0; i<mapElementCount.length; ++i)
			if (this.mapElementCount[i] != that.mapElementCount[i]) {
				System.err.println("Configuration map element count "+i+" not equal ("+this.mapElementCount[i]+","+that.mapElementCount[i]+")");
				return(false);
			}
		if (!this.consoleState.isEqualTo(that.consoleState)) { 
			System.err.println("Configuration console state not equal");
			return(false);
		}
		if (!this.marbleState.isEqualTo(that.marbleState)) {
			System.err.println("Configuration marble state not equal");
			return(false);
		}
		if (!this.worldState.isEqualTo(that.worldState)) {
			System.err.println("Configuration world state not equal");
			return(false);
		}
		if (!this.helpState.isEqualTo(that.helpState)) {
			System.err.println("Configuration world state not equal");
			return(false);
		}
		if (!this.surfAndLayerState.isEqualTo(that.surfAndLayerState)) {
			System.err.println("Configuration surface and layer state not equal");
			return(false);
		}
		if (!this.mapElementsState.isEqualTo(that.mapElementsState)) {
			System.err.println("Configuration map elements state not equal");
			return(false);
		}
		if (!this.colorBarsState.isEqualTo(that.colorBarsState)) {
			System.err.println("Configuration color bars state not equal");
			return(false);
		}
		if (!this.lightingState.isEqualTo(that.lightingState)) {
			System.err.println("Configuration lighting state not equal");
			return(false);
		}
		if (!this.lightPosState.isEqualTo(that.lightPosState)) {
			System.err.println("Configuration light position state not equal");
			return(false);
		}
		if (!this.viewPtState.isEqualTo(that.viewPtState)) {
			System.err.println("Configuration viewpoint state not equal");
			return(false);
		}
		if (this.mapElementStateList.size() != that.mapElementStateList.size()) {
			System.err.println("Configuration map elements state list size not equal");
			return(false);
		}
		for (int i=0; i<this.mapElementStateList.size(); ++i) {
			MapElementState mps0 = this.mapElementStateList.get(i);
			MapElementState mps1 = that.mapElementStateList.get(i);
			if (!mps0.isEqualTo(mps1)) {
				System.err.println("Configuration map element state "+i+" not equal");
				return(false);
			}
		}
		return(true);
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
	public long incrementMapElementCount(MapElementState.Type type) {
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
	public MapElement addMapElementState(MapElementState state) {
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
			} else if (state instanceof LineSetState) {
				mapElement = World.getInstance().getLineSets().addLineSet((LineSetState) state, true);
			}
		}
		World world = World.getInstance();
		if (world != null) {
			world.setVerticalExaggeration(world.getVerticalExaggeration());
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
			addMapElementState(state[i]);
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
		map.put("MapElementCount", mapElementCount);
		
		map.put("WorldState", worldState.save());
		map.put("HelpState", helpState.save());
		map.put("SurfaceAndLayerState", surfAndLayerState.save());
		map.put("MapElementsState", mapElementsState.save());
		map.put("ColorBarsState", colorBarsState.save());
		map.put("LightingState", lightingState.save());
		map.put("LightPositionState", lightPosState.save());
		map.put("ViewpointState", viewPtState.save());
		map.put("ConsoleState", consoleState.save());
		map.put("MarbleState", marbleState.save());

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
	 * Get all the LineSet states
	 * 
	 * @return
	 */
	public ArrayList<LineSetState> getLineSetStates() {
		ArrayList<LineSetState> states = new ArrayList<LineSetState>();
		for (int i = 0; i < mapElementStateList.size(); ++i) {
			State state = mapElementStateList.get(i);
			if (state instanceof LineSetState) {
				states.add((LineSetState) state);
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
		helpState.viewData.close();
		surfAndLayerState.viewData.close();
		mapElementsState.viewData.close();
		colorBarsState.viewData.close();
		lightingState.viewData.close();
		lightPosState.viewData.close();
		viewPtState.viewData.close();
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
		if (helpState.getViewData().isVisible()) {
			helpState.open();
		}
		if (surfAndLayerState.getViewData().isVisible()) {
			surfAndLayerState.open();
		}
		if (mapElementsState.getViewData().isVisible()) {
			mapElementsState.open();
		}
		if (colorBarsState.getViewData().isVisible()) {
			colorBarsState.open();
		}
		if (lightingState.getViewData().isVisible()) {
			lightingState.open();
		}
		if (lightPosState.getViewData().isVisible()) {
			lightPosState.open();
		}
		if (viewPtState.getViewData().isVisible()) {
			viewPtState.open();
		}
		for (int i = 0; i < mapElementStateList.size(); ++i) {
			MapElementState state = mapElementStateList.get(i);
			state.open();
		}
	}

	/**
	 * Get the surface and layers view
	 * 
	 * @return
	 */
	public SurfaceAndLayersView getSurfaceAndLayersView() {
		return ((SurfaceAndLayersView) surfAndLayerState.viewData.view);
	}
}
