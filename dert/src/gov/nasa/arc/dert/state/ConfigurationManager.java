package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.edit.CoordAction;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCameraInfoManager;
import gov.nasa.arc.dert.ui.OptionDialog;
import gov.nasa.arc.dert.util.ColorMap;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.Console;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;

import javax.swing.JOptionPane;

/**
 * Manages Configuration objects.
 *
 */
public class ConfigurationManager {

	// This is a singleton
	private static ConfigurationManager INSTANCE;

	// The current configuration in use
	private Configuration currentConfig;

	// A list of recently used configurations
	private LinkedHashMap<String, String> recentConfigMap;

	// The maximum number of recent configurations to keep
	private int maxRecent = 10;

	// A thread for loading a configuration
	private Thread configThread;
	
	private StateFactory stateFactory;
	
	private String currentConfigHome;
	private boolean saveConfigToStash;

	/**
	 * Create the ConfigurationManager
	 * 
	 * @param properties
	 * @return
	 */
	public static ConfigurationManager createInstance(Properties properties, StateFactory stateFactory) {
		if (INSTANCE == null) {
			INSTANCE = new ConfigurationManager(properties, stateFactory);
			INSTANCE.currentConfig = new Configuration((String) null);
		}
		return (INSTANCE);
	}

	/**
	 * Get the ConfigurationManager
	 * 
	 * @return
	 */
	public static ConfigurationManager getInstance() {
		return (INSTANCE);
	}

	/**
	 * Constructor
	 * 
	 * @param properties
	 */
	protected ConfigurationManager(Properties properties, StateFactory stateFactory) {
		this.stateFactory = stateFactory;
		saveConfigToStash = StringUtil.getBooleanValue(properties, "SaveToStashOnly", false, false);
		loadRecent(properties);
	}

	/**
	 * Save the current configuration
	 * 
	 * @return
	 */
	public boolean saveCurrentConfiguration() {
		// we haven't actually created a configuration
		// just return
		if (currentConfig.toString() == null) {
			return (true);
		}
		int answer = OptionDialog.showConfirmDialog(Dert.getMainWindow(), "Save current configuration?", JOptionPane.YES_NO_CANCEL_OPTION);
		// cancelled
		if (answer == JOptionPane.CANCEL_OPTION) {
			return (false);
		} else if (answer == JOptionPane.NO_OPTION) {
			return (true);
		}
		// save
		return (saveCurrentConfigurationAs(false));
	}

	/**
	 * Check if label is Untitled or user requests new configuration (force).
	 * Then save the current configuration.
	 * 
	 * @param force
	 *            ask to change label even if there is one
	 */
	public boolean saveCurrentConfigurationAs(boolean force) {
		if (currentConfig.toString().equals("Untitled") || force) {
			String[] configName = getConfigList(currentConfigHome);
			String label = OptionDialog.showSingleInputDialog(Dert.getMainWindow(), "Please enter a name for the current configuration.", "");
			if (label == null) {
				return (false);
			}

			for (int i = 0; i < configName.length; ++i) {
				if (configName[i].equals(label)) {
					int answer = OptionDialog.showConfirmDialog(Dert.getMainWindow(),
						"There is already a configuration with the name of " + label + ".  Overwrite?", 
						JOptionPane.OK_CANCEL_OPTION);
					if (answer == JOptionPane.CANCEL_OPTION) {
						return (false);
					}
					break;
				}
			}
			currentConfig.setLabel(label);
		}

		saveConfiguration(currentConfig);
		return (true);
	}

	/**
	 * Save the current configuration.
	 */
	public void saveConfiguration(Configuration dertConfig) {
		File file = new File(currentConfigHome, "config");
		HashMap<String, Object> savedState = dertConfig.save();
		try {
			file = new File(file, dertConfig.label);
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(savedState);
			oos.flush();
			oos.close();
			addRecent(file.getAbsolutePath());
		} catch (Exception e) {
			Console.println("Error writing configuration.  See log.");
			e.printStackTrace();
		}
		Console.println("Saved configuration to "+dertConfig.label);
	}
	
	private boolean setConfigHome(String landscapePath) {
		if (saveConfigToStash) {
			currentConfigHome = Dert.getUserPath();
			return(true);
		}		
		boolean saveLocal = false;
		File file = new File(landscapePath);
		// Landscape is on a server
		if (landscapePath.toLowerCase().startsWith("http"))
			saveLocal = true;
		// Landscape is on local host
		else {
			// Is there an existing dert subdirectory?
			if (!isWritable(file, "dert"))
				saveLocal = true;
			else {
				file = new File(file, "dert");
				if (!isWritable(file, "config"))
					saveLocal = true;
				else if (!isWritable(file, "colormap"))
					saveLocal = true;
				else if (!isWritable(file, "camera"))
					saveLocal = true;
			}
		}
		
		// Landscape dert subdirectories are writable.
		if (!saveLocal) {
			currentConfigHome = file.getAbsolutePath();
			return(true);
		}
		
		file = new File(Dert.getUserPath());
		if (!isWritable(file, "config")) {
			Console.println("Unable to write to stash config subdirectory.");
			return(false);
		}
		if (!isWritable(file, "colormap")) {
			Console.println("Unable to write to stash colormap subdirectory.");
			return(false);
		}
		if (!isWritable(file, "camera")) {
			Console.println("Unable to write to stash camera subdirectory.");
			return(false);
		}
		currentConfigHome = Dert.getUserPath();
		return(true);
	}
	
	private boolean isWritable(File parent, String child) {
		File c = new File(parent, child);
		if (!c.exists()) {
			if (!c.mkdirs())
				return(false);
		}
		else if (!c.canWrite())
			return(false);
		return(true);
	}


	/**
	 * Remove a configuration
	 * 
	 * @param configPath
	 */
	public void removeConfiguration(String configPath) {
		File file = new File(configPath);
		if (file.exists()) {
			file.delete();
		}
		removeRecent(configPath);
	}

	/**
	 * Load a Configuration file
	 * 
	 * @param configPath
	 * @return
	 */
	public Configuration loadConfiguration(String configPath) {
		Configuration config = null;
		try {
			String landPath = null;
			// Configuration is in landscape, derive landscape path from config path.
			// This way we can move/copy a configuration to a different landscape.
			if (!configPath.startsWith(Dert.getUserPath())) {
				int p = configPath.lastIndexOf("/dert/");
				landPath = configPath.substring(0, p);
			}
			
			File file = new File(configPath).getCanonicalFile();
			if (file.exists()) {
				Console.println("Loading configuration from " + file.getAbsolutePath());
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
				Object obj = ois.readObject();
				ois.close();
				if (obj instanceof HashMap<?,?>) {
					config = new Configuration((HashMap<String,Object>)obj);
					if (landPath != null)
						config.setLandscapePath(landPath);
					addRecent(configPath);
				} else {
					OptionDialog.showErrorMessageDialog(Dert.getMainWindow(), "Configuration for " + landPath + " is invalid.");
				}
			} else {
				OptionDialog.showErrorMessageDialog(Dert.getMainWindow(), "Configuration for " + landPath + " does not exist.");
			}
		} catch (Exception e) {
			Console.println("Unable to load configuration " + configPath + ", see log.");
			e.printStackTrace();
		}
		return (config);
	}

	/**
	 * Set the current Configuration.
	 * 
	 * @param config
	 */
	public void setCurrentConfiguration(Configuration config) {
		String landscapePath = config.getLandscapePath();
		if (!setConfigHome(landscapePath)) {
			Console.println("Unable to set the current configuration to "+config);
			return;
		}
		
		// Perform UI operations on UI event thread
		if (currentConfig != null) {
			try {
				EventQueue.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						currentConfig.closeViews();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		CoordAction.listenerList.clear();
		currentConfig = config;
		ColorMap.setConfigLocation(currentConfigHome);
		FieldCameraInfoManager.getInstance().setConfigLocation(currentConfigHome);

		// create the world
		World world = currentConfig.worldState.createWorld(landscapePath, currentConfig);
		if (world == null) {
			Console.println("Unable to create world for " + currentConfig);
			return;
		}

		try {
			// Perform UI operations on UI event thread
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					// setup the main virtual world view
					Dert.getWorldView().setState(currentConfig.worldState);
					currentConfig.worldState.setView(Dert.getWorldView());
//					currentConfig.worldState.viewData.setViewWindow(Dert.getMainWindow(), true, 20, 20);
					currentConfig.worldState.viewData.setViewWindow(Dert.getMainWindow(), true);
				}
			});
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					// set up the console
					Dert.getConsoleView().setState(currentConfig.consoleState);
					currentConfig.consoleState.setView(Dert.getConsoleView());
//					currentConfig.consoleState.viewData.setViewWindow(Dert.getConsoleWindow(), true, 20, 600);
					currentConfig.consoleState.viewData.setViewWindow(Dert.getConsoleWindow(), true);
				}
			});
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					// add the other views to the current configuration
					currentConfig.openViews();
				}
			});
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					// Update main window
					Dert.getMainWindow().setConfiguration(currentConfig);

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the current Configuration
	 * 
	 * @return
	 */
	public Configuration getCurrentConfiguration() {
		return (currentConfig);
	}

	/**
	 * Get the list of configurations.
	 * 
	 * @return list of configuration names
	 */
	public String[] getConfigList(String configPath) {
		File file = new File(configPath, "config");
		String[] list = file.list();
		if (list == null) {
			return (new String[0]);
		}
		ArrayList<String> name = new ArrayList<String>();
		for (int i = 0; i < list.length; ++i) {
			if (!list[i].startsWith(".")) {
				name.add(list[i]);
			}
		}
		String[] result = new String[name.size()];
		name.toArray(result);
		return (result);
	}

	private void addRecent(String configPath) {
		int p = configPath.indexOf("dert");
		String name = configPath.substring(0, p);
		name = StringUtil.getLabelFromFilePath(name);
		String config = StringUtil.getLabelFromFilePath(configPath);
		name += ":" + config;
		recentConfigMap.put(name, configPath);
		if (recentConfigMap.size() > maxRecent) {
			String[] key = new String[recentConfigMap.size()];
			recentConfigMap.keySet().toArray(key);
			recentConfigMap.remove(key[0]);
		}
	}

	/**
	 * Save the recently accessed Configuration list
	 * 
	 * @param properties
	 */
	public void saveRecent(Properties properties) {
		String[] key = new String[recentConfigMap.size()];
		recentConfigMap.keySet().toArray(key);
		for (int i = 0; i < key.length; ++i) {
			String path = recentConfigMap.get(key[i]);
			properties.put("RecentConfig." + i, path);
		}
	}

	private void loadRecent(Properties properties) {
		recentConfigMap = new LinkedHashMap<String, String>();
		maxRecent = StringUtil.getIntegerValue(properties, "RecentConfigCount", true, maxRecent, false);
		for (int i = 0; i < maxRecent; ++i) {
			String configPath = properties.getProperty("RecentConfig." + i);
			if (configPath != null) {
				if (!configPath.contains("/config/")) {
					continue;
				}
				File file = new File(configPath);
				if (!file.exists()) {
					continue;
				}
				String uPath = Dert.getUserPath();
				if (configPath.startsWith(uPath)) {
					String name = StringUtil.getLabelFromFilePath(configPath);
					recentConfigMap.put(name, configPath);
				}
				else {
					int p = configPath.indexOf("dert");
					String name = configPath.substring(0, p);
					name = StringUtil.getLabelFromFilePath(name);
					String config = StringUtil.getLabelFromFilePath(configPath);
					name += ":" + config;
					recentConfigMap.put(name, configPath);
				}
			}
		}
	}

	private void removeRecent(String configPath) {
		// remove this configuration from the recents map
		String[] key = new String[recentConfigMap.size()];
		recentConfigMap.keySet().toArray(key);
		for (int i = 0; i < key.length; ++i) {
			String path = recentConfigMap.get(key[i]);
			if (path.equals(configPath)) {
				recentConfigMap.remove(key[i]);
			}
		}
	}

	/**
	 * Get the list of recently accessed Configurations
	 * 
	 * @return
	 */
	public String[] getRecentConfigurations() {
		String[] key = new String[recentConfigMap.size()];
		recentConfigMap.keySet().toArray(key);
		return (key);
	}

	/**
	 * Get the path of the given recent Configuration
	 * 
	 * @param key
	 * @return
	 */
	public String getRecentConfigurationPath(String key) {
		return (recentConfigMap.get(key));
	}

	public void openConfiguration(final String configPath) {
		if (configThread != null) {
			return;
		}
		configThread = new Thread(new Runnable() {
			@Override
			public void run() {
				if (saveCurrentConfiguration()) {
					Configuration config = loadConfiguration(configPath);
					if (config != null) {
						setCurrentConfiguration(config);
					}
				}
				configThread = null;
			}
		});
		configThread.start();
	}

	/**
	 * Create a new Configuration given the landscape path.
	 * 
	 * @param landPath
	 * @return
	 */
	public void createConfiguration(final String landPath) {
		if (configThread != null) {
			return;
		}
		configThread = new Thread(new Runnable() {
			@Override
			public void run() {
				saveCurrentConfiguration();
				Configuration newConfig = new Configuration("Untitled");
				newConfig.setLandscapePath(landPath);
				setCurrentConfiguration(newConfig);
				configThread = null;
			}
		});
		configThread.start();
	}
	
	public StateFactory getStateFactory() {
		return(stateFactory);
	}

}
