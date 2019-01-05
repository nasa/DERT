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
 
Tile Rendering Library - Brian Paul 
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

package gov.nasa.arc.dert;

import gov.nasa.arc.dert.action.edit.BackgroundColorDialog;
import gov.nasa.arc.dert.ephemeris.Ephemeris;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.layer.DerivativeLayer;
import gov.nasa.arc.dert.landscape.layer.FieldLayer;
import gov.nasa.arc.dert.landscape.quadtree.QuadTree;
import gov.nasa.arc.dert.landscape.quadtree.QuadTreeCache;
import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.proj.Proj4;
import gov.nasa.arc.dert.render.BasicScene;
import gov.nasa.arc.dert.render.SceneCanvas;
import gov.nasa.arc.dert.render.SceneFramework;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.featureset.FeatureSet;
import gov.nasa.arc.dert.scene.featureset.FeatureSets;
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
import gov.nasa.arc.dert.scenegraph.Marker;
import gov.nasa.arc.dert.scenegraph.text.BitmapFont;
import gov.nasa.arc.dert.scenegraph.text.BitmapText;
import gov.nasa.arc.dert.scenegraph.text.Text;
import gov.nasa.arc.dert.state.Configuration;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.StateFactory;
import gov.nasa.arc.dert.util.ColorMap;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.Console;
import gov.nasa.arc.dert.view.ConsoleView;
import gov.nasa.arc.dert.view.graph.Axes;
import gov.nasa.arc.dert.view.world.WorldView;
import gov.nasa.arc.dert.viewpoint.ViewpointController;

import java.awt.Font;
import java.awt.Toolkit;
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
	
	public static String version;

	// Main application window
	protected static MainWindow mainWindow;

	// Console window
	protected static JDialog consoleWindow;

	// Console view
	protected static ConsoleView consoleView;

	// Paths to DERT executable and user directory
	protected static String path, userPath;

	// Application properties
	protected static Properties dertProperties;
	
	protected String[] args;

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
		Dert dert = new Dert(args);
		BasicScene.imagePath = path + SPLASH_SCREEN;
		dert.initialize();

		// If a configuration has been passed in the command line, open it.
		String configPath = null;
		for (int i = 0; i < args.length - 1; ++i) {
			if (args[i].toLowerCase().equals("-config")) {
				configPath = args[i + 1];
				break;
			}
		}
		if (configPath != null) {
			Configuration config = ConfigurationManager.getInstance().loadConfiguration(configPath);
			if (config != null)
				ConfigurationManager.getInstance().openConfiguration(config);
		}
	}

	/**
	 * Constructor
	 * 
	 * @param args
	 */
	public Dert(String[] args) {
		this.args = args;

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

		path = pathStr;
		
	}
	
	protected void initialize() {
		ColorMap.location = path;
		ImageBoard.defaultImagePath = path + "html/images/defaultimage.png";
		Proj4.setProjPath(path + "proj");
		
		// Make it possible to use multiple OpenGL contexts in Ardor3D.
		System.setProperty("ardor3d.useMultipleContexts", "true");

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
				File file = new File(userPath, LOG_NAME);
				String logFilename = file.getAbsolutePath();
				PrintStream pStream = new PrintStream(new FileOutputStream(logFilename, true), true);
				System.setErr(pStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Create singletons.
		ConfigurationManager.createInstance(dertProperties, new StateFactory());
		FieldCameraInfoManager.createInstance(path);

		// Always create heavy weight menus (this must be set before creating
		// any windows).
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		// Initialize main window and console.
		createMainWindows(args);
		createFont();
		
		Console.println("OpenGL Vendor: " + SceneCanvas.openGLVendor);
		Console.println("OpenGL Renderer: " + SceneCanvas.openGLRenderer);
		Console.println("OpenGL Version: " + SceneCanvas.openGLVersion);

		Console.println("Date: " + dateStr);
		Console.println("DERT Version: " + dertProperties.getProperty("Dert.Version"));
		Console.println("Path to executable: " + path);
		Console.println("Path to user files: " + userPath);
		Console.println("OS Name: " + System.getProperty("os.name"));
		Console.println("OS Version: " + System.getProperty("os.version"));
		Console.println("OS Arch: " + System.getProperty("os.arch"));
		Console.println("Java Version: " + System.getProperty("java.version"));
		Console.println();
		
		// Load SPICE libraries and kernels
		Ephemeris.createInstance(path, dertProperties);
	}

	/**
	 * Initialize the DERT session.
	 * 
	 * @param pathStr
	 *            path to the DERT executable
	 * @param args
	 *            the command line arguments
	 */
	protected void createMainWindows(String[] args) {

		// Get the default configuration.
		Configuration currentConfig = ConfigurationManager.getInstance().getCurrentConfiguration();

		// Create the main and console windows.
		SceneFramework.createInstance();
		mainWindow = new MainWindow(MAIN_TITLE, path, args, dertProperties);
		mainWindow.setToolPanel(createToolPanel());
		mainWindow.setVisible(true);
		consoleView = (ConsoleView) currentConfig.consoleState.open(false);
		consoleWindow = (JDialog) currentConfig.consoleState.getViewData().getViewWindow();
		mainWindow.requestFocus();

		// Sleep a second to let the main view be completely realized so its
		// glContext may be shared.
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected ToolPanel createToolPanel() {
		ToolPanel toolPanel = new ToolPanel();
		toolPanel.populate();
		return(toolPanel);
	}
	
	protected void createFont() {	
		// Create our own OpenGL bitmap font from a Java font.
		// First determine font size.
		int fontSize = StringUtil.getIntegerValue(dertProperties, "RasterText.FontSize", true, 0, false);
		// Calculate from screen resolution.
		double hgt = mainWindow.getWorldView().getScenePanel().getHeightScale()*Toolkit.getDefaultToolkit().getScreenResolution();
		if (fontSize == 0) {
			fontSize = (int)Math.abs(Math.ceil(hgt/10));
			if (fontSize%2 == 1)
				fontSize ++;
		}
		Text.FONT_SIZE = fontSize;
		Marker.PIXEL_SIZE = hgt/7.5;
		String fName = StringUtil.getStringValue(dertProperties, "RasterText.Font", "Courier New", false);
		Console.println("Building font: "+fName+" "+fontSize);
		BitmapText.DEFAULT_FONT = new BitmapFont(fName, Font.BOLD, fontSize);
	}

	private void installDertProperties() {
		dertProperties = new Properties();
		try {
			// Load properties from file with executable.
			File file = new File(path, "dert.properties");
			FileInputStream propStream = new FileInputStream(file);
			dertProperties.load(propStream);
			propStream.close();

			// Create the user's DERT_HOME directory if it doesn't exist.
			userPath = dertProperties.getProperty("StashPath", "$user.home");
			if (userPath.startsWith("$"))
				userPath = System.getProperty(userPath.substring(1));
			if (userPath.endsWith(DERT_HOME))
				userPath = userPath.substring(0, userPath.length()-(DERT_HOME.length()+1));
			file = new File(userPath, DERT_HOME);
			try {
				if (!file.exists()) {
					file.mkdirs();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			userPath = file.getAbsolutePath();
			// Add preferences from user's dertstash directory.
			file = new File(userPath, "prefs.properties");
			if (file.exists()) {
				propStream = new FileInputStream(file);
				dertProperties.load(propStream);
				propStream.close();
			}
			// Add recent configs from user's dertstash directory.
			file = new File(userPath, "recents.properties");
			if (file.exists()) {
				propStream = new FileInputStream(file);
				dertProperties.load(propStream);
				propStream.close();
			}

			version = dertProperties.getProperty("Dert.Version");
			SceneFramework.millisBetweenFrames = StringUtil.getIntegerValue(dertProperties, "MillisBetweenFrames", true, 33, false);
			World.defaultStereoEyeSeparation = StringUtil.getDoubleValue(dertProperties, "Stereo.eyeSeparation", false, World.defaultStereoEyeSeparation, false);
			World.defaultStereoFocalDistance = StringUtil.getDoubleValue(dertProperties, "Stereo.focalDistance", false, World.defaultStereoFocalDistance, false);
			//RasterText.setFont(StringUtil.getIntegerValue(dertProperties, "RasterText.Font", true, 18, false));
			Lighting.loadProperties(dertProperties);
			QuadTreeCache.MAX_CACHE_MEMORY = (long)(Runtime.getRuntime().maxMemory()*0.75);
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
			FeatureSet.setDefaultsFromProperties(dertProperties);
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
	 * Get the path to the user's directory.
	 * 
	 * @return the path
	 */
	public static String getUserPath() {
		return (userPath);
	}
	
	public static Properties getProperties() {
		return(dertProperties);
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
			
			// Save preferences.
			Properties properties = new Properties();
			Landmarks.saveDefaultsToProperties(properties);
			Tools.saveDefaultsToProperties(properties);
			FeatureSets.saveDefaultsToProperties(properties);

			// Write file to dertstash.
			File f = new File(userPath, "prefs.properties");
			properties.store(new FileOutputStream(f), "DERT Preferences");
			
			// Save recents.
			properties = new Properties();
			ConfigurationManager.getInstance().saveRecent(properties);
			f = new File(userPath, "recents.properties");
			properties.store(new FileOutputStream(f), "DERT Recent Configurations");

			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
