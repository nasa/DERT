package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.io.FileSystemTileSource;
import gov.nasa.arc.dert.io.TileSource;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.featureset.FeatureSets;
import gov.nasa.arc.dert.scene.landmark.Landmarks;
import gov.nasa.arc.dert.scene.tool.Tools;
import gov.nasa.arc.dert.terrain.LayerManager;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.Console;
import gov.nasa.arc.dert.view.world.WorldView;
import gov.nasa.arc.dert.viewpoint.ViewpointStore;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a state object for the World.
 *
 */
public class WorldState extends State {

	// Color
	public Color surfaceColor;

	// Vertical exaggeration value
	public double verticalExaggeration = 1;
	public boolean hiddenDashed;

	// The time
	public long time;

	// Viewpoint
	public ViewpointStore currentViewpoint;
	
	// Coordinate display
	public boolean useLonLat;

	// Persisted components
	protected Lighting lighting;
	protected LayerManager layerManager;

	// Transient components
	protected transient World world;
	protected transient TileSource tileSource;
	protected transient String username, password;

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public WorldState() {
		super(null, StateType.World, new ViewData(20, 20, 960, 600, false));
		surfaceColor = Color.WHITE;
		time = System.currentTimeMillis();
		viewData.setVisible(true);
		lighting = new Lighting();
		layerManager = new LayerManager();
		tileSource = new FileSystemTileSource();
		username = "dert";
		password = "dert";
	}
	
	public WorldState(Map<String,Object> map) {
		super(map);
		useLonLat = StateUtil.getBoolean(map, "UseLonLat", false);
		surfaceColor = StateUtil.getColor(map, "SurfaceColor", Color.WHITE);
		verticalExaggeration = StateUtil.getDouble(map, "VerticalExaggeration", verticalExaggeration);
		time = StateUtil.getLong(map, "Time", System.currentTimeMillis());
		currentViewpoint = ViewpointStore.fromHashMap((HashMap<String,Object>)map.get("CurrentViewpoint"));		
		lighting = new Lighting((HashMap<String,Object>)map.get("Lighting"));
		layerManager = new LayerManager((HashMap<String,Object>)map.get("LayerManager"));
		hiddenDashed = StateUtil.getBoolean(map, "HiddenDashed", World.defaultHiddenDashed);
		tileSource = new FileSystemTileSource();
		username = "dert";
		password = "dert";
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof WorldState)) 
			return(false);
		WorldState that = (WorldState)state;
		if (!super.isEqualTo(that)) 
			return(false);
		if (this.useLonLat != that.useLonLat)
			return(false);
		if (this.time != that.time)
			return(false);
		if (this.verticalExaggeration != that.verticalExaggeration)
			return(false);
		if (this.hiddenDashed != that.hiddenDashed)
			return(false);
		if (!surfaceColor.equals(that.surfaceColor)) 
			return(false);
		// the same viewdata objects or both are null
		if (this.currentViewpoint == that.currentViewpoint)
			return(true);
		// this view data is null but the other isn't
		if (this.currentViewpoint == null)
			return(false);
		// the other view data is null but this one isn't
		if (that.currentViewpoint == null)
			return(false);
		// see if the view datas are equal
		return(this.currentViewpoint.isEqualTo(that.currentViewpoint));
	}
	
	@Override
	public String toString() {
		String str = super.toString();
		str += " time="+time+" surfaceColor="+surfaceColor+" vertexag="+verticalExaggeration+" VP="+currentViewpoint;
		return(str);
	}

	/**
	 * Create the world.
	 * 
	 * @param landscape
	 * @return the world
	 */
	public World createWorld(String landscapeName, Configuration config) {
		if (!tileSource.connect(landscapeName, username, password)) {
			return (null);
		}
		String[][] layerInfo = tileSource.getLayerInfo();

		if (layerInfo.length == 0) {
			Console.println("No valid layers.");
			return (null);
		}
		lighting.initialize();
		if (!layerManager.initialize(tileSource)) {
			return (null);
		}
		// create Landscape before world
		Landscape.createInstance(tileSource, layerManager, surfaceColor);
		world = World.createInstance(name, new Landmarks(config.getLandmarkStates()),
			new Tools(config.getToolStates()), new FeatureSets(config.getFeatureSetStates()), lighting, time);
		world.setUseLonLat(useLonLat);
		if (verticalExaggeration != 1) {
			world.setVerticalExaggeration(verticalExaggeration);
		}
		return (world);
	}

	@Override
	public void dispose() {
		if (world != null)
			world.dispose();
		world = null;
		tileSource = null;
		System.gc();
	}

	@Override
	public Map<String,Object> save() {
		Map<String,Object> map = super.save();
		
		map.put("Lighting", lighting.saveAsHashMap());
		map.put("LayerManager", layerManager.saveAsHashMap());
		if (viewData != null) {
			WorldView wv = (WorldView)viewData.getView();
			if (wv != null)
				currentViewpoint = wv.getViewpoint().get(currentViewpoint);
		}
		map.put("CurrentViewpoint", currentViewpoint.toHashMap());
		verticalExaggeration = world.getVerticalExaggeration();
		map.put("VerticalExaggeration", new Double(verticalExaggeration));
		surfaceColor = Landscape.getInstance().getSurfaceColor();
		map.put("SurfaceColor", surfaceColor);
		time = world.getTime();
		map.put("Time", new Long(time));
		useLonLat = world.getUseLonLat();
		map.put("UseLonLat", new Boolean(useLonLat));
		map.put("HiddenDashed", new Boolean(world.isHiddenDashed()));
		
		return(map);
	}
	
	public void initWorld() {
		// nothing here
	}

}
