package gov.nasa.arc.dert.scene.tool;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.QuadTree;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.scenegraph.RasterText;
import gov.nasa.arc.dert.scenegraph.Text.AlignType;
import gov.nasa.arc.dert.state.GridState;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.State.StateType;
import gov.nasa.arc.dert.util.SpatialUtil;
import gov.nasa.arc.dert.viewpoint.BasicCamera;
import gov.nasa.arc.dert.viewpoint.ViewDependent;

import java.awt.Color;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.MaterialState.MaterialFace;
import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.PickingHint;

/**
 * Provides a base class for the grid map element classes.
 */
public abstract class Grid extends Movable implements Tool, ViewDependent {

	protected static float AMBIENT_FACTOR = 0.75f;

	// Defaults
	public static double defaultCellSize = 1;

	// Grid parts
	protected Line lattice;
	protected Mesh body;

	// Label
	protected Node text;
	protected boolean labelVisible;
	protected boolean actualCoords;

	// Immovable
	protected boolean pinned;

	// Dimensions and location
	protected Vector3 origin;
	protected double cellSize;
	protected Vector3 offset, location;

	// Color
	protected Color color = Color.white;
	protected ColorRGBA colorRGBA;

	// scale factor for viewpoint resizing
	protected double scale = 1, oldScale = 1;

	// Map Element state
	protected StateType type;
	protected GridState state;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public Grid(GridState state) {
		super(state.name);
		this.state = state;
		this.cellSize = state.size;
		this.color = state.color;
		this.pinned = state.pinned;
		offset = new Vector3();
		location = new Vector3();
		setTranslation(state.location);
		colorRGBA = new ColorRGBA(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f,
			color.getAlpha() / 255f);

		MaterialState ms = new MaterialState();
		ms.setDiffuse(MaterialState.MaterialFace.FrontAndBack, ColorRGBA.BLACK);
		ms.setAmbient(MaterialState.MaterialFace.FrontAndBack, ColorRGBA.BLACK);
		ms.setEmissive(MaterialState.MaterialFace.FrontAndBack, colorRGBA);
		lattice = new Line("_lattice");
		lattice.setRenderState(ms);
		lattice.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		lattice.setModelBound(new BoundingBox());

		ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Emissive);
		ms.setColorMaterialFace(MaterialState.MaterialFace.FrontAndBack);
		ms.setEnabled(true);
		text = new Node("_text");
		text.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		text.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		text.setRenderState(ms);
		setLabelVisible(state.labelVisible);

		// invisible body used for selection
		body = new Mesh("_body");
		SpatialUtil.setPickHost(body, this);
		MaterialState bms = new MaterialState();
		bms.setDiffuse(MaterialState.MaterialFace.FrontAndBack, ColorRGBA.BLACK_NO_ALPHA);
		bms.setEnabled(true);
		BlendState bs = new BlendState();
		bs.setBlendEnabled(true);
		body.setRenderState(bms);
		body.setRenderState(bs);
		// body.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		body.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
		body.setModelBound(new BoundingBox());

		attachChild(body);
		attachChild(lattice);
		attachChild(text);

		setVisible(state.visible);
		state.setMapElement(this);
	}

	/**
	 * Get the MapElement state
	 */
	@Override
	public MapElementState getState() {
		return (state);
	}

	/**
	 * Set the grid cell size
	 * 
	 * @param cellSize
	 */
	public void setSize(double cellSize) {
		if (this.cellSize == cellSize) {
			return;
		}
		this.cellSize = cellSize;
		if (this.cellSize <= 0) {
			this.cellSize = 1;
		}
		buildGrid();
	}

	/**
	 * Get the grid cell size
	 */
	@Override
	public double getSize() {
		return (cellSize);
	}

	/**
	 * Set the color
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		if (!this.color.equals(color)) {
			this.color = color;
			colorRGBA = new ColorRGBA(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f,
				color.getAlpha() / 255f);
			MaterialState ms = new MaterialState();
			ms.setDiffuse(MaterialState.MaterialFace.FrontAndBack, ColorRGBA.BLACK);
			ms.setAmbient(MaterialState.MaterialFace.FrontAndBack, ColorRGBA.BLACK);
			ms.setEmissive(MaterialState.MaterialFace.FrontAndBack, colorRGBA);
			lattice.setRenderState(ms);
		}
	}

	/**
	 * Get the color
	 */
	@Override
	public Color getColor() {
		return (color);
	}

	@Override
	protected void enableHighlight(boolean enable) {
		MaterialState materialState = (MaterialState) lattice.getLocalRenderState(RenderState.StateType.Material);
		if (enable) {
			materialState.setAmbient(MaterialFace.FrontAndBack, new ColorRGBA(colorRGBA.getRed() * AMBIENT_FACTOR,
				colorRGBA.getGreen() * AMBIENT_FACTOR, colorRGBA.getBlue() * AMBIENT_FACTOR, colorRGBA.getAlpha()));
			materialState.setDiffuse(MaterialFace.FrontAndBack, colorRGBA);
			materialState.setEmissive(MaterialFace.FrontAndBack, colorRGBA);
			lattice.setRenderState(materialState);
		} else {
			materialState.setAmbient(MaterialFace.FrontAndBack, new ColorRGBA(colorRGBA.getRed() * AMBIENT_FACTOR,
				colorRGBA.getGreen() * AMBIENT_FACTOR, colorRGBA.getBlue() * AMBIENT_FACTOR, colorRGBA.getAlpha()));
			materialState.setDiffuse(MaterialFace.FrontAndBack, colorRGBA);
			materialState.setEmissive(MaterialFace.FrontAndBack, ColorRGBA.BLACK);
		}
	}

	/**
	 * Show the label
	 */
	@Override
	public void setLabelVisible(boolean visible) {
		labelVisible = visible;
		if (text != null) {
			text.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
			text.markDirty(DirtyType.RenderState);
		}
	}

	/**
	 * Find out if the label is visible
	 */
	@Override
	public boolean isLabelVisible() {
		return (labelVisible);
	}

	/**
	 * Show actual landscape coordinate, default is relative to grid origin
	 * 
	 * @param enable
	 */
	public void setActualCoordinates(boolean enable) {
		actualCoords = enable;
		origin = new Vector3(getWorldTranslation());
		buildGrid();
	}

	/**
	 * Find out if showing actual coordinates
	 * 
	 * @return
	 */
	public boolean isActualCoordinates() {
		return (actualCoords);
	}

	protected abstract void buildGrid();

	/**
	 * Set the vertical exaggeration
	 */
	@Override
	public void setVerticalExaggeration(double vertExag, double oldVertExag, double minZ) {
		ReadOnlyVector3 wTrans = getWorldTranslation();
		Vector3 tmp = new Vector3(wTrans.getX(), wTrans.getY(), (wTrans.getZ() - minZ) * vertExag / oldVertExag + minZ);
		getParent().worldToLocal(tmp, tmp);
		setTranslation(tmp);
	}

	protected RasterText createColumnText(String name, double val, double x, double y, ReadOnlyColorRGBA color) {
		if (actualCoords) {
			val += origin.getXf();
		}
		return (createText(name, val, x, y, color));
	}

	protected RasterText createRowText(String name, double val, double x, double y, ReadOnlyColorRGBA color) {
		if (actualCoords) {
			val += origin.getYf();
		}
		return (createText(name, val, x, y, color));
	}

	protected RasterText createText(String name, double val, double x, double y, ReadOnlyColorRGBA color) {
		String textVal = String.format("%10.2f", val).trim();
		RasterText vdt = new RasterText(name, textVal, AlignType.Center);
		vdt.setScaleFactor(1);
		vdt.getSceneHints().setCullHint(CullHint.Inherit);
		vdt.setColor(color);
		vdt.setTranslation(x, y, 0);
		return (vdt);
	}

	protected RasterText createText(String name, String textVal, double x, double y, ReadOnlyColorRGBA color) {
		RasterText vdt = new RasterText(name, textVal, AlignType.Center);
		vdt.setScaleFactor(1);
		vdt.getSceneHints().setCullHint(CullHint.Inherit);
		vdt.setColor(color);
		vdt.setTranslation(x, y, 0);
		return (vdt);
	}

	/**
	 * Update the elevation (Z coordinate)
	 */
	@Override
	public boolean updateElevation(QuadTree quadTree) {
		return (false);
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

	/**
	 * Find out if the grid is immobile
	 */
	@Override
	public boolean isPinned() {
		return (pinned);
	}

	/**
	 * Find out if this grid is visible
	 */
	@Override
	public boolean isVisible() {
		return (getSceneHints().getCullHint() != CullHint.Always);
	}

	/**
	 * Set visibility
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			getSceneHints().setCullHint(CullHint.Inherit);
		} else {
			getSceneHints().setCullHint(CullHint.Always);
		}
		markDirty(DirtyType.RenderState);
	}

	/**
	 * Set mobility
	 */
	@Override
	public void setPinned(boolean pinned) {
		this.pinned = pinned;
	}

	/**
	 * Update view dependent size
	 */
	@Override
	public void update(BasicCamera camera) {
		// nothing here
	}

	protected void scaleShape(double scale) {
		text.setScale(scale);
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
	public String toString() {
		return (getName());
	}

	/**
	 * Get the location in world coordinates
	 */
	@Override
	public ReadOnlyVector3 getLocation() {
		location.set(getWorldTranslation());
		Landscape.getInstance().localToWorldCoordinate(location);
		return (location);
	}
}
