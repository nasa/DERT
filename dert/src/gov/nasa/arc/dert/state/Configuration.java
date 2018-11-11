/**

DERT is a viewer for digital terrain models created from data collected during NASA missions.

DERT is Released in under the NASA Open Source Agreement (NOSA) found in the “LICENSE” folder where you
downloaded DERT.

DERT includes 3rd Party software. The complete copyright notice listing for DERT is:

Copyright © 2015 United States Government as represented by the Administrator of the National Aeronautics and
Space Administration.  No copyright is claimed in the United States under Title 17, U.S.Code. All Other Rights
Reserved.

Desktop Exploration of Remote Terrain (DERT) could not have been written without the aid of a number of free,
open source libraries. These libraries and their notices are listed below. Find the complete third party license
listings in the separate “DERT Third Party Licenses” pdf document found where you downloaded DERT in the
LICENSE folder.
 
JogAmp Ardor3D Continuation
Copyright © 2008-2012 Ardor Labs, Inc.
 
JogAmp
Copyright 2010 JogAmp Community. All rights reserved.
 
JOGL Portions Sun Microsystems
Copyright © 2003-2009 Sun Microsystems, Inc. All Rights Reserved.
 
JOGL Portions Silicon Graphics
Copyright © 1991-2000 Silicon Graphics, Inc.
 
Light Weight Java Gaming Library Project (LWJGL)
Copyright © 2002-2004 LWJGL Project All rights reserved.
 
Tile Rendering Library - Brain Paul 
Copyright © 1997-2005 Brian Paul. All Rights Reserved.
 
OpenKODE, EGL, OpenGL , OpenGL ES1 & ES2
Copyright © 2007-2010 The Khronos Group Inc.
 
Cg
Copyright © 2002, NVIDIA Corporation
 
Typecast - David Schweinsberg 
Copyright © 1999-2003 The Apache Software Foundation. All rights reserved.
 
PNGJ - Herman J. Gonzalez and Shawn Hartsock
Copyright © 2004 The Apache Software Foundation. All rights reserved.
 
Apache Harmony - Open Source Java SE
Copyright © 2006, 2010 The Apache Software Foundation.
 
Guava
Copyright © 2010 The Guava Authors
 
GlueGen Portions
Copyright © 2010 JogAmp Community. All rights reserved.
 
GlueGen Portions - Sun Microsystems
Copyright © 2003-2005 Sun Microsystems, Inc. All Rights Reserved.
 
SPICE
Copyright © 2003, California Institute of Technology.
U.S. Government sponsorship acknowledged.
 
LibTIFF
Copyright © 1988-1997 Sam Leffler
Copyright © 1991-1997 Silicon Graphics, Inc.
 
PROJ.4
Copyright © 2000, Frank Warmerdam

LibJPEG - Independent JPEG Group
Copyright © 1991-2018, Thomas G. Lane, Guido Vollbeding
 

Disclaimers

No Warranty: THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY KIND,
EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
THAT THE SUBJECT SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY
WARRANTY THAT THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE. THIS AGREEMENT
DOES NOT, IN ANY MANNER, CONSTITUTE AN ENDORSEMENT BY GOVERNMENT AGENCY OR ANY
PRIOR RECIPIENT OF ANY RESULTS, RESULTING DESIGNS, HARDWARE, SOFTWARE PRODUCTS OR
ANY OTHER APPLICATIONS RESULTING FROM USE OF THE SUBJECT SOFTWARE.  FURTHER,
GOVERNMENT AGENCY DISCLAIMS ALL WARRANTIES AND LIABILITIES REGARDING THIRD-PARTY
SOFTWARE, IF PRESENT IN THE ORIGINAL SOFTWARE, AND DISTRIBUTES IT "AS IS."

Waiver and Indemnity:  RECIPIENT AGREES TO WAIVE ANY AND ALL CLAIMS AGAINST THE UNITED
STATES GOVERNMENT, ITS CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR
RECIPIENT.  IF RECIPIENT'S USE OF THE SUBJECT SOFTWARE RESULTS IN ANY LIABILITIES,
DEMANDS, DAMAGES, EXPENSES OR LOSSES ARISING FROM SUCH USE, INCLUDING ANY DAMAGES
FROM PRODUCTS BASED ON, OR RESULTING FROM, RECIPIENT'S USE OF THE SUBJECT SOFTWARE,
RECIPIENT SHALL INDEMNIFY AND HOLD HARMLESS THE UNITED STATES GOVERNMENT, ITS
CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR RECIPIENT, TO THE EXTENT
PERMITTED BY LAW.  RECIPIENT'S SOLE REMEDY FOR ANY SUCH MATTER SHALL BE THE IMMEDIATE,
UNILATERAL TERMINATION OF THIS AGREEMENT.

**/

package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scene.tool.Waypoint;
import gov.nasa.arc.dert.state.State.StateType;
import gov.nasa.arc.dert.state.StateFactory.DefaultState;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.mapelement.MapElementsView;
import gov.nasa.arc.dert.view.surfaceandlayers.SurfaceAndLayersView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JTextField;

/**
 * A collection of State objects that represents a snapshot of a DERT session.
 *
 */
public class Configuration {

	// Default states
	public ConsoleState consoleState;
	public WorldState worldState;

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
		stateMap.put("SurfaceAndLayersState", stateFactory.createState(DefaultState.SurfaceAndLayersState));
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
		if (mapElementCount.length < MapElementState.Type.values().length)
			mapElementCount = Arrays.copyOf(mapElementCount, MapElementState.Type.values().length);
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
			if (state instanceof LandmarkState) {
				mapElement = World.getInstance().getLandmarks().addLandmark((LandmarkState) state, true);
			} else if (state instanceof ToolState) {
				mapElement = World.getInstance().getTools().addTool((ToolState) state, true);
			} else if (state instanceof FeatureSetState) {
				mapElement = World.getInstance().getFeatureSets().addFeatureSet((FeatureSetState) state, true, msgField);
			}
			if (mapElement != null)
				mapElementStateList.add(state);
		}
		World world = World.getInstance();
		if ((world != null) && (mapElement != null)) {
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
			if (key[i].equals("ConsoleState"))
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
	
	public void initializeActors() {
		Object[] key = stateMap.keySet().toArray();
		for (int i=0; i<key.length; ++i) {
			State state = stateMap.get((String)key[i]);
			if (state.type == StateType.Actor) {
				ActorState aState = (ActorState)state;
				aState.initialize();
			}
		}
	}
	
	public void dispose() {
		Object[] key = stateMap.keySet().toArray();
		for (int i=0; i<key.length; ++i) {
			State state = stateMap.get((String)key[i]);
			state.dispose();
		}
	}
}
