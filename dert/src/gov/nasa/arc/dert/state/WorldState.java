package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.io.FileSystemTileSource;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.LayerManager;
import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.scene.LineSets;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.landmark.Landmarks;
import gov.nasa.arc.dert.scene.tool.Tools;
import gov.nasa.arc.dert.view.Console;
import gov.nasa.arc.dert.view.world.WorldView;
import gov.nasa.arc.dert.viewpoint.ViewpointStore;

import java.awt.Color;

import com.ardor3d.math.type.ReadOnlyColorRGBA;

/**
 * Provides a state object for the World.
 *
 */
public class WorldState extends State {

	// Stereo parameters
	public double stereoFocalDistance = 1;
	public double stereoEyeSeparation = stereoFocalDistance / 30;

	// Colors
	private ReadOnlyColorRGBA background;
	private Color surfaceColor;

	// Vertical exaggeration value
	private double verticalExaggeration = 1;

	// The time
	private long time;

	// Viewpoint
	private ViewpointStore currentViewpoint;

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
		super(name, StateType.World, new ViewData());
		surfaceColor = Color.WHITE;

		viewData.setVisible(true);
	}

	/**
	 * Get the world
	 * 
	 * @return the world
	 */
	public World getWorld() {
		return (world);
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
		if (background == null) {
			background = World.defaultBackgroundColor;
		}
		if (lighting == null) {
			lighting = new Lighting();
		}
		lighting.initialize();
		if (layerManager == null) {
			layerManager = new LayerManager();
		}
		if (!layerManager.initialize(tileSource)) {
			return (null);
		}
		Landscape landscape = new Landscape(tileSource, layerManager, surfaceColor);
		if (time == 0) {
			time = System.currentTimeMillis();
		}
		world = World.createNewWorld(name, landscape, new Landmarks(config.getLandmarkStates()),
			new Tools(config.getToolStates()), new LineSets(config.getLineSetStates()), lighting, background, time);
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
	public void save() {
		super.save();
		lighting.save();
		layerManager.save();
		if (viewData != null) {
			currentViewpoint = ((WorldView) viewData.getView()).getViewpointNode().getViewpoint("");
		}
		background = world.getBackgroundColor();
		verticalExaggeration = world.getVerticalExaggeration();
		surfaceColor = world.getLandscape().getSurfaceColor();
		time = world.getTime();
	}

	/**
	 * Get the current viewpoint
	 * 
	 * @return
	 */
	public ViewpointStore getCurrentViewpoint() {
		return (currentViewpoint);
	}

}
