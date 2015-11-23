package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scene.tool.Waypoint;
import gov.nasa.arc.dert.state.PanelState.PanelType;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.surfaceandlayers.SurfaceAndLayersView;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A collection of State objects that represents a snapshot of a DERT session.
 *
 */
public class Configuration implements Serializable {

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
	protected ArrayList<MapElementState> mapElementStateList;

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
		worldState = new WorldState(null);
		consoleState = new PanelState(PanelType.Console, "DERT Console", new ViewData(-1, 624, 900, 250, false));
		consoleState.viewData.setVisible(true);
		helpState = new PanelState(PanelType.Help, "DERT Help", new ViewData());
		mapElementsState = new MapElementsState();
		surfAndLayerState = new PanelState(PanelType.SurfaceAndLayers, "DERT Surface and Layers", new ViewData(-1, -1,
			false));
		lightingState = new PanelState(PanelType.Lighting, "DERT Lighting and Shadows", new ViewData(-1, -1, false));
		viewPtState = new ViewpointState();
		lightPosState = new PanelState(PanelType.LightPosition, "DERT Light Position", new ViewData(-1, -1, false));
		colorBarsState = new PanelState(PanelType.ColorBars, "DERT Color Bars", new ViewData(-1, -1, 700, 200, false));
		marbleState = new MarbleState();
		mapElementStateList = new ArrayList<MapElementState>();
		mapElementCount = new int[MapElementState.Type.values().length];
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
	public void saveStates() {
		worldState.save();

		helpState.save();
		surfAndLayerState.save();
		mapElementsState.save();
		colorBarsState.save();
		lightingState.save();
		lightPosState.save();
		viewPtState.save();
		consoleState.save();
		marbleState.save();

		for (int i = 0; i < mapElementStateList.size(); ++i) {
			mapElementStateList.get(i).save();
		}
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
