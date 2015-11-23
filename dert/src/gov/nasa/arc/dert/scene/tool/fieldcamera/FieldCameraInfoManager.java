package gov.nasa.arc.dert.scene.tool.fieldcamera;

import gov.nasa.arc.dert.util.FileHelper;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.Console;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

/**
 * Manages the FieldCameraInfo objects.
 *
 */
public class FieldCameraInfoManager {

	private static FieldCameraInfoManager INSTANCE;

	private static String fieldCameraDirectory = "camera";
	private String location, configLocation;
	private HashMap<String, String> infoMap;

	public static void createInstance(String loc) {
		INSTANCE = new FieldCameraInfoManager(loc);
	}

	public static FieldCameraInfoManager getInstance() {
		return (INSTANCE);
	}

	protected FieldCameraInfoManager(String location) {
		this.location = location;
	}

	/**
	 * Set the location of the directory containing the camera definitions
	 * 
	 * @param loc
	 */
	public void setConfigLocation(String loc) {
		configLocation = loc;
		getFieldCameraNames();
	}

	/**
	 * Get the names of the camera definition files
	 * 
	 * @return
	 */
	public String[] getFieldCameraNames() {
		infoMap = new HashMap<String, String>();
		File fieldCameraDir = new File(location, fieldCameraDirectory);
		String[] fieldCameraFiles = fieldCameraDir.list();
		if (fieldCameraFiles == null) {
			fieldCameraFiles = new String[0];
		}
		for (int i = 0; i < fieldCameraFiles.length; ++i) {
			String instPath = new File(fieldCameraDir.getAbsolutePath(), fieldCameraFiles[i]).getAbsolutePath();
			if (!instPath.endsWith(".properties")) {
				continue;
			}
			infoMap.put(StringUtil.getLabelFromFilePath(instPath), instPath);
		}
		fieldCameraDir = new File(configLocation, fieldCameraDirectory);
		fieldCameraFiles = fieldCameraDir.list();
		if (fieldCameraFiles == null) {
			fieldCameraFiles = new String[0];
		}
		for (int i = 0; i < fieldCameraFiles.length; ++i) {
			String instPath = new File(fieldCameraDir.getAbsolutePath(), fieldCameraFiles[i]).getAbsolutePath();
			if (!instPath.endsWith(".properties")) {
				continue;
			}
			infoMap.put(StringUtil.getLabelFromFilePath(instPath), instPath);
		}

		String[] names = new String[infoMap.size()];
		infoMap.keySet().toArray(names);
		Arrays.sort(names);
		return (names);
	}

	/**
	 * Get a camera info object by name
	 * 
	 * @param name
	 * @return
	 */
	public FieldCameraInfo getFieldCameraInfo(String name) {
		try {
			String filename = infoMap.get(name);
			Properties properties = new Properties();
			properties.load(new FileInputStream(filename));
			return (new FieldCameraInfo(properties));
		} catch (Exception e) {
			e.printStackTrace();
			Console.getInstance().println("Error loading camera " + name + ".  See log.");
			return (null);
		}
	}

	/**
	 * Copy all of the camera definition files
	 * 
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	public void copyFieldCameras(String src, String dest) throws IOException {
		File inDir = new File(src, fieldCameraDirectory);
		File outDir = new File(dest, fieldCameraDirectory);
		if (!outDir.exists()) {
			outDir.mkdir();
		}
		String[] files = inDir.list();
		if (files == null) {
			files = new String[0];
		}
		for (int i = 0; i < files.length; ++i) {
			if (files[i].endsWith(".properties")) {
				File inFile = new File(inDir, files[i]);
				File outFile = new File(outDir, files[i]);
				FileHelper.copyFile(inFile, outFile);
			}
		}
	}

}
