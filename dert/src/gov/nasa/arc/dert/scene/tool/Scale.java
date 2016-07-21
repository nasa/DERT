package gov.nasa.arc.dert.scene.tool;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.QuadTree;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.scenegraph.RasterText;
import gov.nasa.arc.dert.scenegraph.Rod;
import gov.nasa.arc.dert.scenegraph.Text.AlignType;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.state.ScaleState;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.viewpoint.BasicCamera;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.swing.Icon;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.image.Image;
import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.image.PixelDataType;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Quaternion;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.MaterialState.MaterialFace;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.util.TextureKey;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Provides a class for a 3D map scale.
 */
public class Scale extends Movable implements Tool {

	public static final Icon icon = Icons.getImageIcon("scale.png");

	protected static float AMBIENT_FACTOR = 0.75f;

	// Defaults
	public static Color defaultColor = Color.white;
	public static int defaultCellCount = 4;
	public static boolean defaultLabelVisible = true;
	public static boolean defaultAutoLabel = true;
	public static double defaultCellSize = 1;
	public static double defaultRadius = 1;

	// Scale parts
	protected Mesh rod;
	protected RasterText label;

	// Label
	protected boolean autoLabel;

	// Map Element state
	protected ScaleState state;

	// Dimensions
	private int cellCount;
	private double radius, cellSize;

	// Rotation
	protected double azimuth, tilt;
	protected Matrix3 rotMat;
	
	// material state
	protected MaterialState materialState;
	protected Color color;
	protected boolean labelVisible;
	// Ardor3D version of color
	protected ColorRGBA colorRGBA;
	
	protected Vector3 offset, location;

    protected static final Quaternion rotator = new Quaternion().applyRotationX(MathUtils.HALF_PI);

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public Scale(ScaleState state) {
		super(state.name);

		this.state = state;
		cellCount = state.cellCount;
		cellSize = state.size;
		radius = state.radius;
		autoLabel = state.autoLabel;
		labelVisible = state.labelVisible;
		
		offset = new Vector3();
		location = new Vector3();
		setTranslation(state.location);
		setColor(state.color);
		setPinned(state.pinned);		

		label = new RasterText("_label", state.name, AlignType.Center);
		label.setScaleFactor((float) (0.75 * cellSize));
		label.setColor(ColorRGBA.WHITE);
		label.setVisible(labelVisible);
		label.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		label.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Emissive);
		ms.setColorMaterialFace(MaterialState.MaterialFace.FrontAndBack);
		ms.setEnabled(true);
		label.setRenderState(ms);
		attachChild(label);

		buildRod();
		
		state.setMapElement(this);
	}
	
	public String getLabel() {
		return(label.getText());
	}
	
	protected void buildRod() {
		if (rod != null)
			detachChild(rod);
		rod = new Rod("_rod", cellCount, 16, radius, cellSize*cellCount);
        rod.getSceneHints().setCastsShadows(false);
        attachChild(rod);
        rod.updateModelBound();
		label.setTranslation(new Vector3(0, 0, 1.2*radius));
		if (autoLabel)
			label.setText(getName()+" = "+String.format(Landscape.stringFormat, (cellSize*cellCount)).trim());
		
		TextureState tState = new TextureState();
		tState.setTexture(getTexture());
		tState.setEnabled(true);
		rod.setRenderState(tState);

		updateGeometricState(0, true);
		updateWorldTransform(true);
		updateWorldBound(true);
	}

	@Override
	public void setInMotion(boolean inMotion, ReadOnlyVector3 pickPosition) {
		super.setInMotion(inMotion, pickPosition);
		if (inMotion) {
			pickPosition.subtract(getWorldTranslation(), offset);
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
    
    @Override
    public double getSize() {
    	return(cellSize);
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
	 * Show the label
	 */
	@Override
	public void setLabelVisible(boolean visible) {
		labelVisible = visible;
		label.setVisible(visible);
		label.markDirty(DirtyType.RenderState);
	}

	/**
	 * Find out if the label is visible
	 */
	@Override
	public boolean isLabelVisible() {
		return (labelVisible);
	}

	/**
	 * Set the color
	 * 
	 * @param newColor
	 */
	public void setColor(Color newColor) {
		if ((color != null) && color.equals(newColor)) {
			return;
		}
		color = newColor;
		colorRGBA = new ColorRGBA(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f,
			color.getAlpha() / 255f);
		if (materialState == null) {
			// add a material state
			materialState = new MaterialState();
			materialState.setColorMaterial(ColorMaterial.None);
			materialState.setEnabled(true);
			setRenderState(materialState);
		}
		setMaterialState();
		markDirty(DirtyType.RenderState);
	}

	protected void setMaterialState() {
		materialState.setAmbient(MaterialFace.FrontAndBack, new ColorRGBA(colorRGBA.getRed() * AMBIENT_FACTOR,
			colorRGBA.getGreen() * AMBIENT_FACTOR, colorRGBA.getBlue() * AMBIENT_FACTOR, colorRGBA.getAlpha()));
		materialState.setDiffuse(MaterialFace.FrontAndBack, colorRGBA);
		materialState.setEmissive(MaterialFace.FrontAndBack, ColorRGBA.BLACK);
	}

	protected void enableHighlight(boolean enable) {
		if (enable) {
			materialState.setAmbient(MaterialFace.FrontAndBack, new ColorRGBA(colorRGBA.getRed() * AMBIENT_FACTOR,
				colorRGBA.getGreen() * AMBIENT_FACTOR, colorRGBA.getBlue() * AMBIENT_FACTOR, colorRGBA.getAlpha()));
			materialState.setDiffuse(MaterialFace.FrontAndBack, colorRGBA);
			materialState.setEmissive(MaterialFace.FrontAndBack, colorRGBA);
		} else {
			setMaterialState();
		}
	}

	/**
	 * Set the azimuth
	 * 
	 * @param azimuth
	 */
	public void setAzimuth(double azimuth) {
		if (this.azimuth == azimuth) {
			return;
		}
		this.azimuth = azimuth;
		rotMat = new Matrix3();
		rod.setRotation(rotMat.fromAngles(-Math.toRadians(tilt), 0.0, -Math.toRadians(azimuth)));
	}

	/**
	 * Get the azimuth
	 * 
	 * @return
	 */
	public double getAzimuth() {
		return (azimuth);
	}

	/**
	 * Set the tilt
	 * 
	 * @param tilt
	 */
	public void setTilt(double tilt) {
		if (this.tilt == tilt) {
			return;
		}
		this.tilt = tilt;
		rotMat = new Matrix3();
		rod.setRotation(rotMat.fromAngles(-Math.toRadians(tilt), 0.0, -Math.toRadians(azimuth)));
	}

	/**
	 * Get the tilt
	 * 
	 * @return
	 */
	public double getTilt() {
		return (tilt);
	}

	/**
	 * Get the MapElement state
	 */
	@Override
	public MapElementState getState() {
		return (state);
	}

	/**
	 * Get the map element type
	 */
	@Override
	public Type getType() {
		return (Type.Scale);
	}

	@Override
	public Icon getIcon() {
		return (icon);
	}

	/**
	 * Set the grid cell size
	 * 
	 * @param cellSize
	 */
	public void setSize(double size) {
		if (cellSize == size)
			return;
		cellSize = size;
		
		buildRod();
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
	public Texture2D getTexture() {
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
	@Override
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
	 * Update the Z coordinate when elevation changes
	 * 
	 * @param quadTree
	 * @return
	 */
	public boolean updateElevation(QuadTree quadTree) {
		ReadOnlyVector3 t = getWorldTranslation();
		if (quadTree.contains(t.getX(), t.getY())) {
			double z = Landscape.getInstance().getZ(t.getX(), t.getY(), quadTree);
			if (!Double.isNaN(z)) {
				setTranslation(t.getX(), t.getY(), z);
				return (true);
			}
		}
		return (false);
	}

	/**
	 * Get the location in planetary coordinates
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getLocation() {
		location.set(getWorldTranslation());
		Landscape.getInstance().localToWorldCoordinate(location);
		return (location);
	}

	/**
	 * Get the color
	 * 
	 * @return
	 */
	public Color getColor() {
		return (color);
	}

	/**
	 * Set the vertical exaggeration
	 * 
	 * @param vertExag
	 * @param oldVertExag
	 * @param minZ
	 */
	public void setVerticalExaggeration(double vertExag, double oldVertExag, double minZ) {
		ReadOnlyVector3 wTrans = getWorldTranslation();
		Vector3 tmp = new Vector3(wTrans.getX(), wTrans.getY(), wTrans.getZ() * vertExag / oldVertExag);
		getParent().worldToLocal(tmp, tmp);
		setTranslation(tmp);
	}

	/**
	 * Update size depending on camera location.
	 */
	@Override
	public void update(BasicCamera camera) {
		// nothing here
	}

	/**
	 * Determine visibility
	 * 
	 * @return
	 */
	public boolean isVisible() {
		return (getSceneHints().getCullHint() != CullHint.Always);
	}

	/**
	 * Set visibility
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible) {
		if (visible) {
			getSceneHints().setCullHint(CullHint.Inherit);
		} else {
			getSceneHints().setCullHint(CullHint.Always);
		}
		markDirty(DirtyType.RenderState);
	}
	
}
