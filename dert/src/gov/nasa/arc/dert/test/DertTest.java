package gov.nasa.arc.dert.test;

import gov.nasa.arc.dert.action.edit.BackgroundColorDialog;
import gov.nasa.arc.dert.landscape.DerivativeLayer;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.QuadTree;
import gov.nasa.arc.dert.landscape.QuadTreeCache;
import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.scene.LineSet;
import gov.nasa.arc.dert.scene.landmark.Figure;
import gov.nasa.arc.dert.scene.landmark.ImageBoard;
import gov.nasa.arc.dert.scene.landmark.Placemark;
import gov.nasa.arc.dert.scene.tool.CartesianGrid;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scene.tool.Plane;
import gov.nasa.arc.dert.scene.tool.Profile;
import gov.nasa.arc.dert.scene.tool.RadialGrid;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.scenegraph.RasterText;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.Console;
import gov.nasa.arc.dert.viewpoint.ViewpointController;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Main class for running unit tests of Desktop Exploration of Remote Terrain.
 *
 */
public class DertTest {

	public static final boolean isMac, isLinux, is64;
	public static final String DERT_HOME = "dertstash";

	// Paths to DERT executable and user current working directory
	private static String path, userPath;

	// Application properties
	private static Properties dertProperties;

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
		DertTest test = new DertTest(args);
		test.runTests(args);
	}

	/**
	 * Constructor
	 * 
	 * @param args
	 */
	public DertTest(String[] args) {

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

		// Initialize DERT.
		initialize(pathStr, args);
	}

	/**
	 * Initialize the DERT session.
	 * 
	 * @param pathStr
	 *            path to the DERT executable
	 * @param args
	 *            the command line arguments
	 */
	public static void initialize(String pathStr, String[] args) {

		// Initialize global path related fields.
		path = pathStr;

		// Create the user's DERT_HOME directory if it doesn't exist.
		File file = new File(userPath, DERT_HOME);
		try {
			if (!file.exists()) {
				file.mkdirs();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Load default and session properties.
		installDertProperties();

		// Get current local date.
		String dateStr = new Date().toString();

		// Set time zone to UTC.
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		System.err.println("DERT Test");
		System.err.println("Date: " + dateStr);
		System.err.println("DERT Version: " + dertProperties.getProperty("Dert.Version"));
		System.err.println("Path to executable: " + path);
		System.err.println("Path to user files: " + userPath);
		System.err.println("OS Name: " + System.getProperty("os.name"));
		System.err.println("OS Version: " + System.getProperty("os.version"));
		System.err.println("OS Arch: " + System.getProperty("os.arch"));
		System.err.println("Java Version: " + System.getProperty("java.version"));
		System.err.println();
		
		Console.createInstance();
		
		// Load SPICE libraries and kernels
//		Ephemeris.createInstance(path, dertProperties);
	}

	private static void installDertProperties() {
		dertProperties = new Properties();
		try {
			// Load properties from file with executable.
			File file = new File(path, "dert.properties");
			dertProperties.load(new FileInputStream(file));
			// Add properties from user's dertstash directory.
			file = new File(userPath, DERT_HOME);
			file = new File(file, "properties");
			if (file.exists()) {
				dertProperties.load(new FileInputStream(file));
			}

			RasterText.setFont(StringUtil.getIntegerValue(dertProperties, "RasterText.Font", true, 18, false));
			Lighting.loadProperties(dertProperties);
			QuadTreeCache.MAX_CACHE_MEMORY = StringUtil.getLongValue(dertProperties, "QuadTree.MaxCacheSize", true,
				QuadTreeCache.MAX_CACHE_MEMORY, false);
			DerivativeLayer.defaultColorMapName = dertProperties.getProperty("ColorMap.Default", "default0");
			QuadTree.CELL_SIZE = StringUtil.getIntegerValue(dertProperties, "MeshCellSize", true, QuadTree.CELL_SIZE,
				false);
			ViewpointController.mouseScrollDirection = StringUtil.getIntegerValue(dertProperties,
				"MouseScrollDirection", false, -1, false);
			BackgroundColorDialog.setPredefinedBackgroundColors(dertProperties);
			Landscape.MAX_LEVELS = StringUtil.getIntegerValue(dertProperties, "Landscape.MaximumLevels", true,
				Landscape.MAX_LEVELS, false);

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
	
	public void runTests(String[] args) {
		MathUtilTest mut = new MathUtilTest();
		if (!mut.testMathUtil())
			System.exit(1);
		
		LandscapeTest lt = new LandscapeTest();
		if (!lt.testLandscape())
			System.exit(2);
	}
}
