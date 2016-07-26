package gov.nasa.arc.dert.scene.tool;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scenegraph.FigureMarker;
import gov.nasa.arc.dert.scenegraph.Shape;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.state.ScaleBarState;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.SpatialUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.world.MoveEdit;
import gov.nasa.arc.dert.viewpoint.BasicCamera;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.Icon;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.image.Image;
import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.image.PixelDataType;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.util.TextureKey;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Provides a class for a 3D map scale.
 */
public class ScaleBar extends FigureMarker implements Tool {

	public static final Icon icon = Icons.getImageIcon("scale.png");

	// Defaults
	public static Color defaultColor = Color.white;
	public static int defaultCellCount = 4;
	public static boolean defaultLabelVisible = true;
	public static boolean defaultAutoLabel = true;
	public static double defaultCellSize = 1;
	public static double defaultRadius = 1;
	public static double defaultAzimuth = 0;
	public static double defaultTilt = 0;

	// Label
	private boolean autoLabel;

	// Map Element state
	private ScaleBarState state;

	// Dimensions
	private int cellCount;
	private double radius;
	
	private Vector3 offset;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public ScaleBar(ScaleBarState state) {
		super(state.name, state.location, state.size, state.color, state.labelVisible, false, state.pinned);
		this.state = state;
		cellCount = state.cellCount;
		radius = state.radius;
		autoLabel = state.autoLabel;
		setAzimuth(state.azimuth);
		setTilt(state.tilt);
		setVisible(state.visible);
		setStrictZ(state.strictZ);
		this.state = state;
		
		offset = new Vector3();
		contents.detachChild(surfaceNormalArrow);
		surfaceNormalArrow = null;
		contents.setScale(1);

		buildRod();
		
		state.setMapElement(this);
	}
	
	public String getLabel() {
		return(label.getText());
	}
	

	@Override
	protected void scaleShape(double scale) {
		// do nothing;
	}

	
	private void buildRod() {
		if (shape != null)
			contents.detachChild(shape);
		shape = Shape.createShape("_geometry", ShapeType.rod, cellCount, (float)radius, (float)size*cellCount);
		SpatialUtil.setPickHost(shape, this);
		shape.getSceneHints().setCastsShadows(false);
		contents.attachChild(shape);
		label.setTranslation(new Vector3(0, 2.5*radius, 0));
		if (autoLabel)
			label.setText(getName()+" = "+String.format(Landscape.stringFormat, (size*cellCount)).trim());
		
		TextureState tState = new TextureState();
		tState.setTexture(getTexture());
		tState.setEnabled(true);
		shape.getGeometry().setRenderState(tState);

		updateGeometricState(0, true);
		updateWorldTransform(true);
		updateWorldBound(true);
	}

	@Override
	public void setInMotion(boolean inMotion, ReadOnlyVector3 pickPosition) {
		super.setInMotion(inMotion, pickPosition);
		if (inMotion) {
			pickPosition.subtract(getWorldTranslation(), offset);
			offset.setZ(0);
		} else {
			offset.set(Vector3.ZERO);
		}
	}

	@Override
	public void setTranslation(double x, double y, double z) {
		super.setTranslation(x - offset.getX(), y - offset.getY(), z - offset.getZ());
	}

	@Override
	public void setTranslation(ReadOnlyVector3 loc) {
		super.setTranslation(loc.subtract(offset, null));
	}

	/**
	 * Set the location
	 * 
	 * @param i
	 * @param p
	 */
	@Override
	public void setLocation(double x, double y, double z, boolean doEdit, boolean zOnly) {
		if (doEdit)
			Dert.getMainWindow().getUndoHandler().addEdit(new MoveEdit(this, new Vector3(getTranslation()), strictZ));
		if (zOnly)
			super.setTranslation(x, y, z);
		else
			setTranslation(x, y, z);
		updateListeners();
	}
	
	public int getCellCount() {
		return(cellCount);
	}
	
	public void setCellCount(int cells) {
		if (cellCount == cells)
			return;
		cellCount = cells;
		buildRod();
	}
	
	public void setAutoLabel(boolean autoLabel) {
		this.autoLabel = autoLabel;
	}
	
	public boolean isAutoLabel() {
		return(autoLabel);
	}

	/**
	 * Get the MapElement state
	 */
	public MapElementState getState() {
		return (state);
	}

	/**
	 * Get the map element type
	 */
	public Type getType() {
		return (Type.Scale);
	}

	@Override
	public Icon getIcon() {
		return (icon);
	}
	
	public double getCellRadius() {
		return(radius);
	}
	
	public void setCellRadius(double radius) {
		if (this.radius == radius)
			return;
		this.radius = radius;
		buildRod();
	}

	/**
	 * Get the color map texture
	 * 
	 * @return
	 */
	private Texture2D getTexture() {
		Texture2D texture = null;
		texture = new Texture2D();
		texture.setWrap(Texture.WrapMode.Clamp);
		texture.setTextureStoreFormat(TextureStoreFormat.RGBA8);
		texture.setMinificationFilter(Texture.MinificationFilter.NearestNeighborNoMipMaps);
		texture.setMagnificationFilter(Texture.MagnificationFilter.NearestNeighbor);
		texture.setTextureKey(TextureKey.getRTTKey(Texture.MinificationFilter.NearestNeighborNoMipMaps));
		texture.setApply(Texture2D.ApplyMode.Modulate);
		
		ByteBuffer buffer = BufferUtils.createByteBuffer(cellCount*4*4*4);
		int[] col = new int[2];
		col[0] = MathUtil.bytes2Int((byte)color.getAlpha(), (byte)color.getBlue(),
			(byte)color.getGreen(), (byte)color.getRed());
		Color bcolor = Color.BLACK;
		col[1] = MathUtil.bytes2Int((byte)bcolor.getAlpha(), (byte)bcolor.getBlue(),
			(byte)bcolor.getGreen(), (byte)bcolor.getRed());
		for (int j=0; j<4; ++j)
			for (int i = 0; i < cellCount; ++i)
				for (int k=0; k<4; ++k)
					buffer.putInt(col[i%2]);		
		buffer.limit(cellCount*4*4*4);
		buffer.rewind();
		
		ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>(1);
		list.add(buffer);
		Image image = new Image(ImageDataFormat.RGBA, PixelDataType.UnsignedByte, cellCount*4, 4, list, null);
		texture.setImage(image);
		return (texture);
	}

	/**
	 * Get the point and distance to seek to
	 */
	public double getSeekPointAndDistance(Vector3 point) {
		BoundingVolume bv = getWorldBound();
		point.set(bv.getCenter());
		return (getRadius() * 1.5);
	}

	@Override
	public String toString() {
		return (getName());
	}
	
	public void setHiddenDashed(boolean hiddenDashed) {
		// nothing here
	}

	/**
	 * Update size depending on camera location.
	 */
	@Override
	public void update(BasicCamera camera) {
		// nothing here
	}

	/**
	 * Set the defaults
	 * 
	 * @param properties
	 */
	public static void setDefaultsFromProperties(Properties properties) {
		defaultColor = StringUtil.getColorValue(properties, "MapElement.Scale.defaultColor", defaultColor, false);
		defaultLabelVisible = StringUtil.getBooleanValue(properties, "MapElement.Scale.defaultLabelVisible",
			defaultLabelVisible, false);
		defaultAutoLabel = StringUtil.getBooleanValue(properties, "MapElement.Scale.defaultAutoLabel", defaultAutoLabel, false);
		defaultCellCount = StringUtil.getIntegerValue(properties, "MapElement.Scale.defaultCellCount", true, defaultCellCount, false);
		defaultAzimuth = StringUtil.getDoubleValue(properties, "MapElement.Scale.defaultAzimuth", false,
			defaultAzimuth, false);
		defaultTilt = StringUtil.getDoubleValue(properties, "MapElement.Scale.defaultTilt", false, defaultTilt, false);
	}

	/**
	 * Save the defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		properties.setProperty("MapElement.Scale.defaultColor", StringUtil.colorToString(defaultColor));
		properties.setProperty("MapElement.Scale.defaultLabelVisible", Boolean.toString(defaultLabelVisible));
		properties.setProperty("MapElement.Scale.defaultAutoLabel", Boolean.toString(defaultAutoLabel));
		properties.setProperty("MapElement.Scale.defaultCellCount", Integer.toString(defaultCellCount));
		properties.setProperty("MapElement.Scale.defaultAzimuth", Double.toString(defaultAzimuth));
		properties.setProperty("MapElement.Scale.defaultTilt", Double.toString(defaultTilt));
	}
	
}
