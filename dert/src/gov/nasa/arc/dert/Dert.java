package gov.nasa.arc.dert;

import gov.nasa.arc.dert.action.edit.BackgroundColorDialog;
import gov.nasa.arc.dert.ephemeris.Ephemeris;
import gov.nasa.arc.dert.landscape.DerivativeLayer;
import gov.nasa.arc.dert.landscape.FieldLayer;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.QuadTree;
import gov.nasa.arc.dert.landscape.QuadTreeCache;
import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.raster.proj.Proj4;
import gov.nasa.arc.dert.render.BasicScene;
import gov.nasa.arc.dert.render.SceneCanvas;
import gov.nasa.arc.dert.render.SceneFramework;
import gov.nasa.arc.dert.scene.LineSet;
import gov.nasa.arc.dert.scene.LineSets;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.landmark.Figure;
import gov.nasa.arc.dert.scene.landmark.ImageBoard;
import gov.nasa.arc.dert.scene.landmark.Landmarks;
import gov.nasa.arc.dert.scene.landmark.Placemark;
import gov.nasa.arc.dert.scene.tool.CartesianGrid;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scene.tool.Plane;
import gov.nasa.arc.dert.scene.tool.Profile;
import gov.nasa.arc.dert.scene.tool.RadialGrid;
import gov.nasa.arc.dert.scene.tool.Tools;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCameraInfoManager;
import gov.nasa.arc.dert.scenegraph.RasterText;
import gov.nasa.arc.dert.state.Configuration;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.util.ColorMap;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.Console;
import gov.nasa.arc.dert.view.ConsoleView;
import gov.nasa.arc.dert.view.graph.Axes;
import gov.nasa.arc.dert.view.world.WorldView;
import gov.nasa.arc.dert.viewpoint.ViewpointController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import javax.swing.JDialog;
import javax.swing.JPopupMenu;

/**
 * Main class for Desktop Exploration of Remote Terrain. This class loads
 * properties, creates the MainWindow and console, and initializes the
 * application components.
 *
 */
public class Dert {

	public static final boolean isMac, isLinux, is64;
	public static String DERT_HOME = "dertstash";
	public static String SPLASH_SCREEN = "html/images/dert.png";
	public static String LOG_NAME = "dert.log";
	public static String MAIN_TITLE = "Desktop Exploration of Remote Terrain";

	// Main application window
	protected static MainWindow mainWindow;

	// Console window
	protected static JDialog consoleWindow;

	// Console view
	protected static ConsoleView consoleView;

	// Paths to DERT executable and user current working directory
	protected static String path, userPath;

	// Application properties
	protected static Properties dertProperties;

	// Operating system flags
	static {
		String os = System.getProperty("os.name").toLowerCase();
		isMac = os.contains("mac");
		isLinux = os.contains("lin");
		String arch = System.getProperty("os.arch").toLowerCase();
		is64 = arch.contains("64");
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Start DERT.
		new Dert(args);
	}

	/**
	 * Constructor
	 * 
	 * @param args
	 */
	public Dert(String[] args) {

		// Find the path to the executable.
		String pathStr = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		if (pathStr.toLowerCase().endsWith(".jar")) {
			int p = pathStr.lastIndexOf('/');
			pathStr = pathStr.substring(0, p + 1);
		} else {
			pathStr += "../";
		}
		try {
			pathStr = new File(pathStr).getCanonicalPath();
			if (!pathStr.endsWith("/")) {
				pathStr = pathStr + "/";
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		// Make it possible to use multiple OpenGL contexts in Ardor3D.
		System.setProperty("ardor3d.useMultipleContexts", "true");

		// Initialize global path related fields.
		path = pathStr;
		ColorMap.location = path;
		ImageBoard.defaultImagePath = path + "html/images/defaultimage.png";
		BasicScene.imagePath = path + SPLASH_SCREEN;
		userPath = System.getProperty("user.home");
		Proj4.setProjPath(path + "proj");

		// Create the user's DERT_HOME directory if it doesn't exist.
		File file = new File(userPath, DERT_HOME);
		try {
			if (!file.exists()) {
				file.mkdirs();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		userPath = file.getAbsolutePath();

		// Load default and session properties.
		installDertProperties();

		// Get current local date.
		String dateStr = new Date().toString();

		// Set time zone to UTC.
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		// Setup the log file (System.err)
		// Send stderr to the IDE console if were are in debug mode
		boolean debug = false;
		for (int i = 0; i < args.length; ++i) {
			if (args[i].toLowerCase().equals("-debug")) {
				debug = true;
				break;
			}
		}
		if (!debug) {
			try {
				file = new File(file, LOG_NAME);
				String logFilename = file.getAbsolutePath();
				PrintStream pStream = new PrintStream(new FileOutputStream(logFilename, true), true);
				System.setErr(pStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Create singletons.
		ConfigurationManager.createInstance(dertProperties);
		FieldCameraInfoManager.createInstance(path);

		// Always create heavy weight menus (this must be set before creating
		// any windows).
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		// Initialize main window and console.
		createMainWindows(pathStr, args);
		
		Console.getInstance().println("OpenGL Vendor: " + SceneCanvas.openGLVendor);
		Console.getInstance().println("OpenGL Renderer: " + SceneCanvas.openGLRenderer);
		Console.getInstance().println("OpenGL Version: " + SceneCanvas.openGLVersion);

		Console.getInstance().println("Date: " + dateStr);
		Console.getInstance().println("DERT Version: " + dertProperties.getProperty("Dert.Version"));
		Console.getInstance().println("Path to executable: " + path);
		Console.getInstance().println("Path to user files: " + userPath);
		Console.getInstance().println("OS Name: " + System.getProperty("os.name"));
		Console.getInstance().println("OS Version: " + System.getProperty("os.version"));
		Console.getInstance().println("OS Arch: " + System.getProperty("os.arch"));
		Console.getInstance().println("Java Version: " + System.getProperty("java.version"));
		Console.getInstance().println();
		
		// Load SPICE libraries and kernels
		Ephemeris.createInstance(path, dertProperties);

		// If a configuration has been passed in the command line, open it.
		String configStr = null;
		for (int i = 0; i < args.length - 1; ++i) {
			if (args[i].toLowerCase().equals("-config")) {
				configStr = args[i + 1];
				break;
			}
		}
		if (configStr != null) {
			String configPath = ConfigurationManager.getInstance().getConfigFilePath(configStr);
			if (configPath != null) {
				ConfigurationManager.getInstance().openConfiguration(configPath);
			} else {
				ConfigurationManager.getInstance().createConfiguration(configStr);
			}
		}
	}

	/**
	 * Initialize the DERT session.
	 * 
	 * @param pathStr
	 *            path to the DERT executable
	 * @param args
	 *            the command line arguments
	 */
	protected void createMainWindows(String pathStr, String[] args) {

		// Get the default configuration.
		Configuration currentConfig = ConfigurationManager.getInstance().getCurrentConfiguration();

		// Create the main and console windows.
		mainWindow = new MainWindow(MAIN_TITLE, path, args, dertProperties);
		consoleView = (ConsoleView) currentConfig.consoleState.open();
		consoleWindow = (JDialog) currentConfig.consoleState.getViewData().getViewWindow();

		// Sleep a second to let the main view be completely realized so its
		// glContext may be shared.
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void installDertProperties() {
		dertProperties = new Properties();
		try {
			// Load properties from file with executable.
			File file = new File(path, "dert.properties");
			dertProperties.load(new FileInputStream(file));
			// Add properties from user's dertstash directory.
			file = new File(userPath, "properties");
			if (file.exists()) {
				dertProperties.load(new FileInputStream(file));
			}

			World.defaultStereoEyeSeparation = StringUtil.getDoubleValue(dertProperties, "Stereo.eyeSeparation", false, World.defaultStereoEyeSeparation, false);
			World.defaultStereoFocalDistance = StringUtil.getDoubleValue(dertProperties, "Stereo.focalDistance", false, World.defaultStereoFocalDistance, false);
			RasterText.setFont(StringUtil.getIntegerValue(dertProperties, "RasterText.Font", true, 18, false));
			Lighting.loadProperties(dertProperties);
			QuadTreeCache.MAX_CACHE_MEMORY = StringUtil.getLongValue(dertProperties, "QuadTree.MaxCacheSize", true,
				QuadTreeCache.MAX_CACHE_MEMORY, false);
			DerivativeLayer.defaultColorMapName = dertProperties.getProperty("ColorMap.Default", "default0");
			FieldLayer.defaultColorMapName = DerivativeLayer.defaultColorMapName;
			QuadTree.CELL_SIZE = StringUtil.getIntegerValue(dertProperties, "MeshCellSize", true, QuadTree.CELL_SIZE,
				false);
			ViewpointController.mouseScrollDirection = StringUtil.getIntegerValue(dertProperties,
				"MouseScrollDirection", false, -1, false);
			BackgroundColorDialog.setPredefinedBackgroundColors(dertProperties);
			Landscape.MAX_LEVELS = StringUtil.getIntegerValue(dertProperties, "Landscape.MaximumLevels", true,
				Landscape.MAX_LEVELS, false);
			Axes.TIC_PIXELS = StringUtil.getIntegerValue(dertProperties, "Profile.tickInterval", true, 60, false);

			// Get map element preferences.
			Placemark.setDefaultsFromProperties(dertProperties);
			Figure.setDefaultsFromProperties(dertProperties);
			ImageBoard.setDefaultsFromProperties(dertProperties);
			FieldCamera.setDefaultsFromProperties(dertProperties);
			Path.setDefaultsFromProperties(dertProperties);
			Plane.setDefaultsFromProperties(dertProperties);
			CartesianGrid.setDefaultsFromProperties(dertProperties);
			RadialGrid.setDefaultsFromProperties(dertProperties);
			Profile.setDefaultsFromProperties(dertProperties);
			LineSet.setDefaultsFromProperties(dertProperties);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Get the main window.
	 * 
	 * @return main window
	 */
	public static MainWindow getMainWindow() {
		return (mainWindow);
	}

	/**
	 * Get the console window.
	 * 
	 * @return console window
	 */
	public static JDialog getConsoleWindow() {
		return (consoleWindow);
	}

	/**
	 * Get the worldview.
	 * 
	 * @return worldview
	 */
	public static WorldView getWorldView() {
		return (mainWindow.getWorldView());
	}

	/**
	 * Get the console view.
	 * 
	 * @return console view
	 */
	public static ConsoleView getConsoleView() {
		return (consoleView);
	}

	/**
	 * Get the path to the DERT executable.
	 * 
	 * @return the path
	 */
	public static String getPath() {
		return (path);
	}

	/**
	 * Get the path to the user's current working directory.
	 * 
	 * @return the path
	 */
	public static String getUserPath() {
		return (userPath);
	}

	/**
	 * Exit the application.
	 */
	public static void quit() {
		try {
			// Abort if user cancels configuration save.
			if (!ConfigurationManager.getInstance().saveCurrentConfiguration()) {
				return;
			}

			SceneFramework.getInstance().stopFrameHandlerUpdate();
			Properties properties = new Properties();
			// Save session.
			ConfigurationManager.getInstance().saveRecent(properties);
			// Save preferences.
			Landmarks.saveDefaultsToProperties(properties);
			Tools.saveDefaultsToProperties(properties);
			LineSets.saveDefaultsToProperties(properties);

			// Write file to dertstash.
			File f = new File(userPath, "properties");
			properties.store(new FileOutputStream(f), "Dert Properties");

			// Close down UI.
			mainWindow.dispose();

			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
