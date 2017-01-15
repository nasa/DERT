package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.edit.CoordAction;
import gov.nasa.arc.dert.icon.Icons;
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

	/**
	 * Create the ConfigurationManager
	 * 
	 * @param properties
	 * @return
	 */
	public static ConfigurationManager createInstance(Properties properties) {
		if (INSTANCE == null) {
			INSTANCE = new ConfigurationManager(properties);
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
	 * Get the configuration file path given the configuration name
	 * 
	 * @param configStr
	 * @return
	 */
	public String getConfigFilePath(String configStr) {
		int p = configStr.indexOf(':');
		if (p > 0) {
			File f = new File(configStr.substring(0, p), "dert" + File.separator + "config" + File.separator
				+ configStr.substring(p + 1) + ".xml");
			return (f.getAbsolutePath());
		}
		return (null);

	}

	/**
	 * Constructor
	 * 
	 * @param properties
	 */
	protected ConfigurationManager(Properties properties) {
		loadRecent(properties);
		currentConfig = new Configuration((String) null);
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
			String[] configName = getConfigList(currentConfig.getLandscapePath());
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
		File file = new File(dertConfig.getLandscapePath(), "dert");
		file = new File(file, "config");
		if (!file.exists())
			file.mkdirs();
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
			int p = configPath.lastIndexOf("/dert/");
			String landPath = configPath.substring(0, p);
			File file = new File(configPath).getCanonicalFile();
			if (file.exists()) {
				Console.println("Loading configuration from " + file.getAbsolutePath());
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
				Object obj = ois.readObject();
				ois.close();
				if (obj instanceof HashMap<?,?>) {
					config = new Configuration((HashMap<String,Object>)obj);
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
		String landscapePath = config.getLandscapePath();
		File dertFile = new File(landscapePath, "dert");
		String configLocation = dertFile.getAbsolutePath();
		File f = new File(dertFile, "colormap");
		if (!f.exists()) {
			f.mkdirs();
		}
		f = new File(dertFile, "camera");
		if (!f.exists()) {
			f.mkdirs();
		}
		ColorMap.setConfigLocation(configLocation);
		FieldCameraInfoManager.getInstance().setConfigLocation(configLocation);

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
					currentConfig.worldState.viewData.setViewWindow(Dert.getMainWindow(), true, 20, 20);
				}
			});
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					// set up the console
					Dert.getConsoleView().setState(currentConfig.consoleState);
					currentConfig.consoleState.setView(Dert.getConsoleView());
					currentConfig.consoleState.viewData.setViewWindow(Dert.getConsoleWindow(), true, 20, 600);
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
	 * Get the list of Configurations for the given landscape.
	 * 
	 * @param landscapePath
	 * @return
	 */
	public String[] getConfigList(String landscapePath) {
		File dertFile = new File(landscapePath, "dert");
		File file = new File(dertFile, "config");
		if (!file.exists()) {
			return (new String[0]);
		}
		if (!file.isDirectory()) {
			return (new String[0]);
		}
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
				if (!configPath.contains("dert/config")) {
					continue;
				}
				File file = new File(configPath);
				if (!file.exists()) {
					continue;
				}
				int p = configPath.indexOf("dert");
				String name = configPath.substring(0, p);
				name = StringUtil.getLabelFromFilePath(name);
				String config = StringUtil.getLabelFromFilePath(configPath);
				name += ":" + config;
				recentConfigMap.put(name, configPath);
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
				saveCurrentConfiguration();
				Configuration config = loadConfiguration(configPath);
				if (config != null) {
					setCurrentConfiguration(config);
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

}
