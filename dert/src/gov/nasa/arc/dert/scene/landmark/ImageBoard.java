package gov.nasa.arc.dert.scene.landmark;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scenegraph.BillboardMarker;
import gov.nasa.arc.dert.state.ImageBoardState;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.util.ImageUtil;
import gov.nasa.arc.dert.util.StringUtil;

import java.util.Properties;

import javax.swing.Icon;

import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture.ApplyMode;
import com.ardor3d.math.Vector3;

/**
 * Provides a Billboard landmark for user provided images.
 */
public class ImageBoard extends BillboardMarker implements Landmark {

	public static final Icon icon = Icons.getImageIcon("billboard.png");

	// Defaults
	public static double defaultSize = 1.0;
	public static boolean defaultLabelVisible = true;
	public static boolean defaultPinned = false;
	public static String defaultImagePath;
	protected static Texture defaultTexture;

	// File path to the image file.
	private String imagePath;

	// Map Element state
	private ImageBoardState state;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public ImageBoard(ImageBoardState state) {
		super(state.name, state.position, state.size, state.color, state.labelVisible, state.pinned);
		this.state = state;
		imagePath = state.imagePath;

		// load image into the texture
		Texture texture = null;
		if (imagePath == null) {
			texture = getDefaultTexture();
			this.imagePath = defaultImagePath;
		} else {
			texture = ImageUtil.createTexture(imagePath, true);
			texture.setApply(ApplyMode.Modulate);
		}
		setTexture(texture, texture);
		state.setMapElement(this);
		setVisible(state.visible);
	}

	/**
	 * Get the MapElement state
	 */
	@Override
	public MapElementState getState() {
		return (state);
	}

	/**
	 * Get the image file path
	 * 
	 * @return
	 */
	public String getImagePath() {
		return (imagePath);
	}

	/**
	 * Set the image file path
	 * 
	 * @param imagePath
	 */
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
		Texture texture = ImageUtil.createTexture(imagePath, true);
		texture.setApply(ApplyMode.Modulate);
		setTexture(texture, texture);
		updateGeometricState(0, true);
	}

	/**
	 * Get the point and distance to seek to.
	 */
	@Override
	public double getSeekPointAndDistance(Vector3 point) {
		point.set(getTranslation());
		return (getRadius() * 1.5);
	}

	/**
	 * Get the map element type
	 */
	@Override
	public Type getType() {
		return (Type.Billboard);
	}

	@Override
	public Icon getIcon() {
		return (icon);
	}

	protected static Texture getDefaultTexture() {
		if (defaultTexture == null) {
			defaultTexture = ImageUtil.createTexture(defaultImagePath, true);
			defaultTexture.setApply(ApplyMode.Modulate);
		}
		return (defaultTexture);
	}

	/**
	 * Set the defaults
	 * 
	 * @param properties
	 */
	public static void setDefaultsFromProperties(Properties properties) {
		defaultSize = (float) StringUtil.getDoubleValue(properties, "MapElement.ImageBoard.defaultSize", true,
			defaultSize, false);
		defaultLabelVisible = StringUtil.getBooleanValue(properties, "MapElement.ImageBoard.defaultLabelVisible",
			defaultLabelVisible, false);
		defaultPinned = StringUtil.getBooleanValue(properties, "MapElement.ImageBoard.defaultPinned", defaultPinned,
			false);
	}

	/**
	 * Save the defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		properties.setProperty("MapElement.ImageBoard.defaultSize", Double.toString(defaultSize));
		properties.setProperty("MapElement.ImageBoard.defaultLabelVisible", Boolean.toString(defaultLabelVisible));
		properties.setProperty("MapElement.ImageBoard.defaultPinned", Boolean.toString(defaultPinned));
	}

}
