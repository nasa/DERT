package gov.nasa.arc.dert.util;

import gov.nasa.arc.dert.io.CsvReader;
import gov.nasa.arc.dert.view.Console;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.ardor3d.image.Image;
import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.image.PixelDataType;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix4;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.util.TextureKey;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Maps a range of elevations in a color map text file to colors.
 * 
 * The color map file typically contains 4 columns per line: the elevation value
 * and the corresponding Red, Green, Blue component (between 0 and 255). The
 * elevation value may be any floating point value, or "nv" keyword for the
 * nodata value. The elevation may also be expressed as a percentage: 0% being
 * the minimum value found in the raster, 100% the maximum value. Elevations
 * specified as percentage will be translated as absolute values. An extra
 * column may be optionally added for the alpha component. If it is not
 * specified, full opacity (255) is assumed. Various field separators are
 * accepted: comma, tabulation, spaces, ':'. Common colors used by GRASS may
 * also be specified by using their name, instead of the RGB triplet. The
 * supported list is : white, black, red, green, blue, yellow, magenta, cyan,
 * aqua, grey/gray, orange, brown, purple/violet and indigo.
 * 
 * See the colormap directory for examples.
 * 
 */
public class ColorMap {

	// Colors for color maps by name
	public static enum ColorType {
		white, black, blue, green, red, yellow, magenta, cyan, orange, purple, brown, violet, aqua, olive, gray, lightgray, periwinkle, rose, chartreuse, greenbean, tan, indigo, darkgray
	}

	public final static float[][] COLOR_VALUE = { { 1, 1, 1, 1 }, { 0, 0, 0, 1 }, { 0, 0, 1, 1 }, { 0, 1, 0, 1 },
		{ 1, 0, 0, 1 }, { 1, 1, 0, 1 }, { 1, 0, 1, 1 }, { 0, 1, 1, 1 }, { 1, 0.5f, 0, 1 }, { 0.5f, 0, 0.5f, 1 },
		{ 0.6f, 0.4f, 0.2f, 1 }, { 0.5f, 0, 1, 1 }, { 0, 0.5f, 0.5f, 1 }, { 0.5f, 0.5f, 0, 1 },
		{ 0.5f, 0.5f, 0.5f, 1 }, { 0.75f, 0.75f, 0.75f, 1 }, { 0, 0.5f, 1, 1 }, { 1, 0, 0.5f, 1 }, { 0.75f, 1, 0, 1 },
		{ 0, 0.75f, 0.5f, 1 }, { 0.75f, 0.5f, 0, 1 }, { 0.29f, 0, 0.51f, 1 }, { 0.25f, 0.25f, 0.25f, 1 } };

	// Location of color map directory
	public static String location;

	// Size of color map texture
	public static final int textureSize = 1024;

	// Color map directory name
	private static String COLOR_MAP_HOME = "colormap";

	// Configuration location
	private static String configLocation;

	// Color map list
	private static HashMap<String, String> mapMap;

	// Default color map
	protected static final String[][] grayScale = { { "0%", "5", "5", "5", "255" }, { "10%", "30", "30", "30", "255" },
		{ "20%", "55", "55", "55", "255" }, { "30%", "80", "80", "80", "255" }, { "40%", "105", "105", "105", "255" },
		{ "50%", "130", "130", "130", "255" }, { "60%", "155", "155", "155", "255" },
		{ "70%", "180", "180", "180", "255" }, { "80%", "205", "205", "205", "255" },
		{ "90%", "230", "230", "230", "255" }, { "100%", "255", "255", "255", "255" } };

	// Name for the color map and layer it applies to
	private String name, layer;

	// Flags for percentage and gradient
	private boolean isPercent, gradient;

	// Color map range
	private double minimum, maximum, range;

	// Color map base range
	private double baseMinimum, baseMaximum, baseRange;

	// For sorting
	private Comparator<double[]> comparator;

	// Java color array
	private Color[] color;

	// Ardor3D color array
	private ColorRGBA[] colorRGBA;

	// Float array of colors
	private float[][] rgba;

	// Value corresponding to color
	private double[] value;

	// Percent value corresponding to color
	private double[] percent;

	// Matrix used to map colors to vertices
	private Matrix4 textureMatrix = new Matrix4();

	// Texture holding color map
	private Texture2D texture;

	// Listeners for color map range changes
	private ArrayList<ColorMapListener> listeners;

	// Image for texture
	private Image image;

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param layer
	 * @param baseMin
	 * @param baseMax
	 * @param min
	 * @param max
	 * @param gradient
	 */
	public ColorMap(String name, String layer, double baseMin, double baseMax, double min, double max, boolean gradient) {
		this.name = name;
		this.layer = layer;
		comparator = new Comparator<double[]>() {
			@Override
			public int compare(double[] d0, double[] d1) {
				if (d0[0] < d1[0]) {
					return (-1);
				}
				if (d0[0] > d1[0]) {
					return (1);
				}
				return (0);
			}
		};
		baseMinimum = Math.floor(baseMin);
		baseMaximum = Math.ceil(baseMax);
		baseRange = baseMaximum - baseMinimum;
		minimum = min;
		maximum = max;
		range = max - min;
		this.gradient = gradient;
		listeners = new ArrayList<ColorMapListener>();
		loadFromFile();
	}

	/**
	 * Set the location of the colormap directory
	 * 
	 * @param loc
	 */
	public static void setConfigLocation(String loc) {
		configLocation = loc;
		mapMap = new HashMap<String, String>();
		File colorMapDir = new File(location, COLOR_MAP_HOME);
		String[] colorMapFiles = colorMapDir.list();
		if (colorMapFiles == null) {
			colorMapFiles = new String[0];
		}
		for (int i = 0; i < colorMapFiles.length; ++i) {
			String cMapPath = new File(colorMapDir.getAbsolutePath(), colorMapFiles[i]).getAbsolutePath();
			if (!cMapPath.endsWith(".txt")) {
				continue;
			}
			mapMap.put(StringUtil.getLabelFromFilePath(cMapPath), cMapPath);
		}
		colorMapDir = new File(configLocation, COLOR_MAP_HOME);
		colorMapFiles = colorMapDir.list();
		if (colorMapFiles == null) {
			colorMapFiles = new String[0];
		}
		for (int i = 0; i < colorMapFiles.length; ++i) {
			String cMapPath = new File(colorMapDir.getAbsolutePath(), colorMapFiles[i]).getAbsolutePath();
			if (!cMapPath.endsWith(".txt")) {
				continue;
			}
			mapMap.put(StringUtil.getLabelFromFilePath(cMapPath), cMapPath);
		}
	}

	/**
	 * Get the list of color maps
	 * 
	 * @return
	 */
	public static String[] getColorMapNames() {
		String[] names = new String[mapMap.size()];
		mapMap.keySet().toArray(names);
		return (names);
	}

	/**
	 * Get the values for tick marks
	 * 
	 * @return
	 */
	public double[] getValues() {
		if (value == null) {
			loadFromFile();
		}
		double[] v = Arrays.copyOf(value, value.length);
		return (v);
	}

	/**
	 * Add a color map listener
	 * 
	 * @param listener
	 */
	public void addListener(ColorMapListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a color map listener
	 * 
	 * @param listener
	 */
	public void removeListener(ColorMapListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Get the colors for rendering the palette.
	 * 
	 * @return
	 */
	public Color[] getColors() {
		if (colorRGBA == null) {
			return (null);
		}
		color = new Color[colorRGBA.length];
		for (int i = 0; i < color.length; ++i) {
			color[i] = new Color(colorRGBA[i].getRed(), colorRGBA[i].getGreen(), colorRGBA[i].getBlue(),
				colorRGBA[i].getAlpha());
		}
		return (color);
	}

	/**
	 * Get the name of this ColorMap
	 * 
	 * @return
	 */
	public String getName() {
		return (name);
	}

	/**
	 * Set the name, loading the colors from the file.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
		loadFromFile();
		if (texture != null) {
			fillColorMapTexture();
		}
		for (int i = 0; i < listeners.size(); ++i) {
			listeners.get(i).mapChanged(this);
		}
	}

	/**
	 * Get the layer this ColorMap is used for
	 * 
	 * @return
	 */
	public String getLayer() {
		return (layer);
	}

	public void setRange(double min, double max) {
		minimum = min;
		maximum = max;
		range = max - min;

		// set the texture coordinate matrix
		textureMatrix.setIdentity();
		textureMatrix.setM11(baseRange / range);
		textureMatrix.setM31(-(min - baseMinimum) / range);
		if (texture != null) {
			texture.setTextureMatrix(textureMatrix);
		}
		for (int i = 0; i < percent.length; ++i) {
			value[i] = range * percent[i] + minimum;
		}
		for (int i = 0; i < listeners.size(); ++i) {
			listeners.get(i).rangeChanged(this);
		}
	}

	/**
	 * Get current range minimum
	 * 
	 * @return
	 */
	public double getMinimum() {
		return (minimum);
	}

	/**
	 * Get current range maximum
	 * 
	 * @return
	 */
	public double getMaximum() {
		return (maximum);
	}

	/**
	 * Get the base range minimum
	 * 
	 * @return
	 */
	public double getBaseMinimum() {
		return (baseMinimum);
	}

	/**
	 * Get the base range maximum
	 * 
	 * @return
	 */
	public double getBaseMaximum() {
		return (baseMaximum);
	}

	/**
	 * Set the base range minimum
	 * 
	 * @param baseMinimum
	 */
	public void setBaseMinimum(double baseMinimum) {
		this.baseMinimum = baseMinimum;
		baseRange = baseMaximum - baseMinimum;
	}

	/**
	 * Set the base range maximum
	 * 
	 * @param baseMaximum
	 */
	public void setBaseMaximum(double baseMaximum) {
		this.baseMaximum = baseMaximum;
		baseRange = baseMaximum - baseMinimum;
	}

	/**
	 * Set the gradient flag and rebuild the colors.
	 * 
	 * @param gradient
	 */
	public void setGradient(boolean gradient) {
		this.gradient = gradient;
		if (texture != null) {
			fillColorMapTexture();
		}
		for (int i = 0; i < listeners.size(); ++i) {
			listeners.get(i).mapChanged(this);
		}
	}

	/**
	 * Determine if gradient
	 * 
	 * @return
	 */
	public boolean isGradient() {
		return (gradient);
	}

	/**
	 * Given a value, return a color
	 * 
	 * @param val
	 * @param cRGBA
	 * @return
	 */
	public ColorRGBA getColorRGBA(double val, ColorRGBA cRGBA) {
		if (cRGBA == null) {
			cRGBA = new ColorRGBA();
		}
		if (Double.isNaN(val)) {
			cRGBA.set(ColorRGBA.BLACK_NO_ALPHA);
			return (cRGBA);
		}
		if (val < value[0]) {
			cRGBA.set(ColorRGBA.BLACK_NO_ALPHA);
			return (cRGBA);
		}
		if (val > value[value.length - 1]) {
			cRGBA.set(ColorRGBA.BLACK_NO_ALPHA);
			return (cRGBA);
		}
		int index1 = 0;
		for (int i = 1; i < value.length; ++i) {
			index1 = i;
			if (val < value[i]) {
				break;
			}
		}
		int index0 = index1 - 1;
		if (gradient) {
			double f1 = (val - value[index0]) / (value[index1] - value[index0]);
			double f0 = 1 - f1;
			cRGBA.setRed((float) (colorRGBA[index0].getRed() * f0 + colorRGBA[index1].getRed() * f1));
			cRGBA.setGreen((float) (colorRGBA[index0].getGreen() * f0 + colorRGBA[index1].getGreen() * f1));
			cRGBA.setBlue((float) (colorRGBA[index0].getBlue() * f0 + colorRGBA[index1].getBlue() * f1));
			cRGBA.setAlpha((float) (colorRGBA[index0].getAlpha() * f0 + colorRGBA[index1].getAlpha() * f1));
			return (cRGBA);
		}
		// nearest neighbor
		else {
			double d1 = value[index1] - val;
			double d0 = val - value[index0];
			if (d1 < d0) {
				cRGBA.set(colorRGBA[index1]);
			} else {
				cRGBA.set(colorRGBA[index0]);
			}
			return (cRGBA);
		}
	}

	/**
	 * Given a value, return a texture coordinate
	 * 
	 * @param val
	 * @param result
	 * @return
	 */
	public Vector2 getTextureCoordinate(double val, Vector2 result) {
		if (result == null) {
			result = new Vector2();
		}
		if (Double.isNaN(val)) {
			result.set(0, -1);
			return (result);
		}
		result.set(0, (val - baseMinimum) / baseRange);
		return (result);
	}

	/**
	 * Get the color map texture
	 * 
	 * @return
	 */
	public Texture2D getTexture() {
		if (texture == null) {
			texture = new Texture2D();
			texture.setWrap(Texture.WrapMode.BorderClamp);
			texture.setTextureStoreFormat(TextureStoreFormat.RGBA8);
			texture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
			texture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
			texture.setTextureKey(TextureKey.getRTTKey(Texture.MinificationFilter.NearestNeighborNoMipMaps));
			texture.setApply(Texture2D.ApplyMode.Modulate);
			texture.setBorderColor(ColorRGBA.BLACK_NO_ALPHA);
			if (value == null) {
				loadFromFile();
			}
		}
		fillColorMapTexture();
		return (texture);
	}

	/**
	 * Get the color lookup table.
	 * 
	 * @param size
	 * @param table
	 */
	public void getColorTable(int size, ByteBuffer table) {
		table.rewind();
		table.clear();
		int[] c = getBaseColorMapAsIntArray(size);
		for (int i = 0; i < c.length; ++i) {
			table.putInt(c[i]);
		}
		table.rewind();
	}

	/**
	 * Get the color map as integers
	 * 
	 * @param size
	 * @return
	 */
	public int[] getColorMapAsIntArray(int size) {
		int[] result = new int[size];
		ColorRGBA colorRGBA = new ColorRGBA();
		for (int i = 0; i < size; ++i) {
			colorRGBA = getColorRGBA(minimum + i / (size - 1.0) * range, colorRGBA);
			result[i] = colorRGBAToInt(colorRGBA);
		}
		return (result);
	}

	/**
	 * Get the base color map as integers
	 * 
	 * @param size
	 * @return
	 */
	public int[] getBaseColorMapAsIntArray(int size) {
		int[] result = new int[size];
		ColorRGBA colorRGBA = new ColorRGBA();
		for (int i = 0; i < size; ++i) {
			colorRGBA = getColorRGBA(baseMinimum + i / (size - 1.0) * baseRange, colorRGBA);
			result[i] = colorRGBAToInt(colorRGBA);
		}
		return (result);
	}

	private void fillColorMapTexture() {
		int[] color = getColorMapAsIntArray(textureSize);
		int width = 4; // 4 is minimum dimension
		int height = color.length;
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
		for (int i = 0; i < color.length; ++i) {
			for (int j = 0; j < width; ++j) {
				buffer.putInt(color[i]);
			}
		}
		buffer.limit(width * height * 4);
		buffer.rewind();
		ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>(1);
		list.add(buffer);
		image = new Image(ImageDataFormat.RGBA, PixelDataType.UnsignedByte, width, height, list, null);
		texture.setImage(image);
		texture.setTextureMatrix(textureMatrix);
	}

	private int colorRGBAToInt(ColorRGBA col) {
		int color = MathUtil.bytes2Int((byte) (col.getAlpha() * 255), (byte) (col.getBlue() * 255),
			(byte) (col.getGreen() * 255), (byte) (col.getRed() * 255));
		return (color);
	}

	private void loadFromFile() {
		String filename = mapMap.get(name);
		CsvReader reader = new CsvReader(filename, false, "[, :\t]");
		ArrayList<String[]> tokenList = new ArrayList<String[]>();
		String[] token = null;
		try {
			reader.open();
			token = reader.readLine();
			while (token != null) {
				if (token.length < 2) {
					throw new IllegalArgumentException("Invalid colormap entry in " + filename);
				}
				tokenList.add(token);
				token = reader.readLine();
			}
			if (tokenList.size() < 2) {
				throw new IllegalStateException("Less than two entries in color map " + name + ".");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Console.getInstance().println("Error loading color map. See log. Using default gray scale.");
			tokenList.clear();
			for (int i = 0; i < grayScale.length; ++i) {
				tokenList.add(grayScale[i]);
			}
		}

		ArrayList<double[]> dataList = new ArrayList<double[]>();
		isPercent = false;

		for (int i = 0; i < tokenList.size(); ++i) {
			token = tokenList.get(i);
			double[] data = new double[5];
			int p = token[0].indexOf('%');
			if (p >= 0) {
				data[0] = Double.valueOf(token[0].substring(0, p)) / 100.0;
				isPercent = true;
			} else {
				data[0] = Double.valueOf(token[0]);
			}
			if (token.length < 3) {
				getRgbFromColorName(token[1], data);
			} else {
				data[1] = Double.valueOf(token[1]) / 255.0f;
				data[2] = Double.valueOf(token[2]) / 255.0f;
				data[3] = Double.valueOf(token[3]) / 255.0f;
				if (token.length > 4) {
					data[4] = Double.valueOf(token[4]) / 255.0f;
				} else {
					data[4] = 1;
				}
			}
			dataList.add(data);
		}
		Collections.sort(dataList, comparator);
		rgba = new float[dataList.size()][4];
		percent = new double[dataList.size()];
		for (int i = 0; i < dataList.size(); ++i) {
			double[] data = dataList.get(i);
			percent[i] = data[0];
			rgba[i][0] = (float) data[1];
			rgba[i][1] = (float) data[2];
			rgba[i][2] = (float) data[3];
			rgba[i][3] = (float) data[4];
		}
		value = new double[percent.length];
		colorRGBA = new ColorRGBA[percent.length];
		for (int i = 0; i < percent.length; ++i) {
			colorRGBA[i] = new ColorRGBA(rgba[i][0], rgba[i][1], rgba[i][2], rgba[i][3]);
		}
		if (!isPercent) {
			minimum = Math.floor(percent[0]);
			maximum = Math.ceil(percent[percent.length - 1]);
			range = maximum - minimum;
			for (int i = 0; i < percent.length; ++i) {
				percent[i] = (percent[i] - minimum) / range;
			}
		} else {
			minimum = baseMinimum;
			maximum = baseMaximum;
		}
		setRange(minimum, maximum);
	}

	private void getRgbFromColorName(String name, double[] data) {
		if (name.toLowerCase().equals("grey")) {
			name = "Gray";
		} else if (name.toLowerCase().equals("lightgrey")) {
			name = "LightGray";
		}
		ReadOnlyColorRGBA color = getColor(name);
		data[1] = color.getRed();
		data[2] = color.getGreen();
		data[3] = color.getBlue();
		data[4] = 1;
	}

	/**
	 * Copy the color map files from installation directory to configuration.
	 * 
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	public static void copyColorMaps(String src, String dest) throws IOException {
		File inDir = new File(src, COLOR_MAP_HOME);
		File outDir = new File(dest, COLOR_MAP_HOME);
		if (!outDir.exists()) {
			outDir.mkdir();
		}
		String[] colorMapFiles = inDir.list();
		if (colorMapFiles == null) {
			colorMapFiles = new String[0];
		}
		for (int i = 0; i < colorMapFiles.length; ++i) {
			if (colorMapFiles[i].endsWith(".txt")) {
				File inFile = new File(inDir, colorMapFiles[i]);
				File outFile = new File(outDir, colorMapFiles[i]);
				FileHelper.copyFile(inFile, outFile);
			}
		}
	}

	private ReadOnlyColorRGBA getColor(String name) {
		ColorType type = ColorType.valueOf(name.toLowerCase());
		float[] col = COLOR_VALUE[type.ordinal()];
		return (new ColorRGBA(col[0], col[1], col[2], col[3]));
	}
}
