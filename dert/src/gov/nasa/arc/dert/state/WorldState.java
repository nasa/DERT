package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.io.FileSystemTileSource;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.LayerManager;
import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.scene.LineSets;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.landmark.Landmarks;
import gov.nasa.arc.dert.scene.tool.Tools;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.Console;
import gov.nasa.arc.dert.view.world.WorldView;
import gov.nasa.arc.dert.viewpoint.ViewpointStore;

import java.awt.Color;
import java.util.HashMap;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

/**
 * Provides a state object for the World.
 *
 */
public class WorldState extends State {

	// Colors
	public ReadOnlyColorRGBA background;
	public Color surfaceColor;

	// Vertical exaggeration value
	public double verticalExaggeration = 1;

	// The time
	public long time;

	// Viewpoint
	public ViewpointStore currentViewpoint;
	
	// Coordinate display
	public boolean useLonLat;

	// Persisted components
	private Lighting lighting;
	private LayerManager layerManager;

	// Transient components
	private transient World world;
	private transient FileSystemTileSource tileSource;

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public WorldState(String name) {
		super(name, StateType.World, new ViewData(-1, -1, 900, 600, false));
		surfaceColor = Color.WHITE;
		background = World.defaultBackgroundColor;
		time = System.currentTimeMillis();
		viewData.setVisible(true);
		lighting = new Lighting();
		layerManager = new LayerManager();
	}
	
	public WorldState(HashMap<String,Object> map) {
		super(map);
		useLonLat = StateUtil.getBoolean(map, "UseLonLat", false);
		surfaceColor = StateUtil.getColor(map, "SurfaceColor", Color.WHITE);
		background = StateUtil.getColorRGBA(map, "Background", World.defaultBackgroundColor);
		verticalExaggeration = StateUtil.getDouble(map, "VerticalExaggeration", verticalExaggeration);
		time = StateUtil.getLong(map, "Time", System.currentTimeMillis());
		currentViewpoint = ViewpointStore.fromHashMap((HashMap<String,Object>)map.get("CurrentViewpoint"));		
		lighting = new Lighting((HashMap<String,Object>)map.get("Lighting"));
		layerManager = new LayerManager((HashMap<String,Object>)map.get("LayerManager"));
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
		if (!surfaceColor.equals(that.surfaceColor)) 
			return(false);
		if (!background.equals(that.background)) 
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
		str += " time="+time+" background="+background+" surfaceColor="+surfaceColor+" vertexag="+verticalExaggeration+" VP="+currentViewpoint;
		return(str);
	}

	/**
	 * Create the world.
	 * 
	 * @param landscape
	 * @return the world
	 */
	public World createWorld(String landscapeName, Configuration config) {
		tileSource = new FileSystemTileSource(landscapeName);
		if (!tileSource.connect("dert", "dert")) {
			return (null);
		}
		String[][] layerInfo = tileSource.getLayerInfo();

		if (layerInfo.length == 0) {
			Console.getInstance().println("No valid layers.");
			return (null);
		}
		lighting.initialize();
		if (!layerManager.initialize(tileSource)) {
			return (null);
		}
		// create Landscape before world
		Landscape.createInstance(tileSource, layerManager, surfaceColor);
		world = World.createInstance(name, new Landmarks(config.getLandmarkStates()),
			new Tools(config.getToolStates()), new LineSets(config.getLineSetStates()), lighting, background, time);
		world.setUseLonLat(useLonLat);
		if (verticalExaggeration != 1) {
			world.setVerticalExaggeration(verticalExaggeration);
		}
		return (world);
	}

	@Override
	public void dispose() {
		world.dispose();
		world = null;
		tileSource = null;
		System.gc();
	}

	@Override
	public HashMap<String,Object> save() {
		HashMap<String,Object> map = super.save();
		
		map.put("Lighting", lighting.saveAsHashMap());
		map.put("LayerManager", layerManager.saveAsHashMap());
		if (viewData != null) {
			WorldView wv = (WorldView)viewData.getView();
			if (wv != null)
				currentViewpoint = wv.getViewpointNode().getViewpoint(currentViewpoint);
		}
		map.put("CurrentViewpoint", currentViewpoint.toHashMap());
		background = world.getBackgroundColor();
		StateUtil.putColorRGBA(map, "Background", background);
		verticalExaggeration = world.getVerticalExaggeration();
		map.put("VerticalExaggeration", new Double(verticalExaggeration));
		surfaceColor = Landscape.getInstance().getSurfaceColor();
		map.put("SurfaceColor", surfaceColor);
		time = world.getTime();
		map.put("Time", new Long(time));
		useLonLat = world.getUseLonLat();
		map.put("UseLonLat", new Boolean(useLonLat));
		
		return(map);
	}

}
