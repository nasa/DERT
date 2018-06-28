package gov.nasa.arc.dert.scene.tool;

import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.landscape.quadtree.QuadTree;
import gov.nasa.arc.dert.scenegraph.HiddenLine;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.scenegraph.text.BitmapText;
import gov.nasa.arc.dert.scenegraph.text.Text.AlignType;
import gov.nasa.arc.dert.state.GridState;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.util.SpatialUtil;
import gov.nasa.arc.dert.viewpoint.ViewDependent;

import java.awt.Color;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.PickingHint;

/**
 * Provides a base class for the grid map element classes.
 */
public abstract class Grid extends Movable implements Tool, ViewDependent {

	// Grid parts
	protected HiddenLine lattice;

	// Label
	protected Node text;
	protected boolean labelVisible;
	protected boolean actualCoords;

	// Dimensions and location
	protected Vector3 origin;
	protected double cellSize;
	protected Vector3 offset;

	// Color
	protected Color color = Color.white;
	protected float lineWidth;

	// scale factor for viewpoint resizing
	protected double scale = 1, oldScale = 1;

	// Map Element state
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
		this.lineWidth = state.lineWidth;
		this.zOff = state.zOff;
		offset = new Vector3();
		setLocation(state.location, false);

		lattice = new HiddenLine("_lattice", IndexMode.Lines);
		SpatialUtil.setPickHost(lattice, this);
		lattice.setColor(color);
		lattice.setModelBound(new BoundingBox());

		MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Emissive);
		ms.setColorMaterialFace(MaterialState.MaterialFace.FrontAndBack);
		ms.setEnabled(true);
		text = new Node("_text");
		text.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		text.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		text.setRenderState(ms);
		setLabelVisible(state.labelVisible);

		attachChild(lattice);
		attachChild(text);

		setVisible(state.visible);
		setLocked(state.locked);
		
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
		this.color = color;
		lattice.setColor(color);
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
		lattice.highlight(enable, color);
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
		buildText();
		updateGeometricState(0, true);
		updateWorldTransform(true);
		updateWorldBound(true);
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
	
	protected abstract void buildText();

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

	protected BitmapText createColumnText(String name, double val, double x, double y, ReadOnlyColorRGBA color) {
		if (actualCoords) {
			val += origin.getXf();
		}
		return (createText(name, val, x, y, color));
	}

	protected BitmapText createRowText(String name, double val, double x, double y, ReadOnlyColorRGBA color) {
		if (actualCoords) {
			val += origin.getYf();
		}
		return (createText(name, val, x, y, color));
	}

	protected BitmapText createText(String name, double val, double x, double y, ReadOnlyColorRGBA color) {
		String textVal = String.format("%10.2f", val).trim();
		BitmapText vdt = new BitmapText(name, BitmapText.DEFAULT_FONT, textVal, AlignType.Center, true);
		vdt.setScaleFactor(1);
		vdt.getSceneHints().setCullHint(CullHint.Inherit);
		vdt.setColor(color);
		vdt.setTranslation(x, y, 0);
		return (vdt);
	}

	protected BitmapText createText(String name, String textVal, double x, double y, ReadOnlyColorRGBA color) {
		BitmapText vdt = new BitmapText(name, BitmapText.DEFAULT_FONT, textVal, AlignType.Center, true);
		vdt.setScaleFactor(1);
		vdt.getSceneHints().setCullHint(CullHint.Inherit);
		vdt.setColor(color);
		vdt.setTranslation(x, y, 0);
		return (vdt);
	}

	/**
	 * Set the line width.
	 * 
	 * @param width
	 */
	public void setLineWidth(float width) {
		lineWidth = width;
		lattice.setLineWidth(width);
		markDirty(DirtyType.RenderState);
	}
	
	public float getLineWidth() {
		return(lineWidth);
	}
	
	public void setHiddenDashed(boolean hiddenDashed) {
		lattice.enableDash(hiddenDashed);
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
	 * Find out if this grid is visible
	 */
	@Override
	public boolean isVisible() {
		return (SpatialUtil.isDisplayed(this));
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
	 * Update view dependent size
	 */
	@Override
	public void update(BasicCamera camera) {
		// nothing here
	}

	@Override
	public void setInMotion(boolean inMotion, ReadOnlyVector3 pickPosition) {
		super.setInMotion(inMotion, pickPosition);
		if (inMotion) {
			pickPosition.subtract(getWorldTranslation(), offset);
		} else {
			if (actualCoords) {
				origin = new Vector3(getWorldTranslation());
				buildText();
				updateGeometricState(0, true);
				updateWorldTransform(true);
				updateWorldBound(true);
			}
			offset.set(Vector3.ZERO);
		}
	}

	@Override
	public void setLocation(double x, double y, double z, boolean doEdit) {
		super.setLocation(x - offset.getX(), y - offset.getY(), z - offset.getZ(), doEdit);
	}

	@Override
	public String toString() {
		return (getName());
	}
}
