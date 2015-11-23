package gov.nasa.arc.dert.scenegraph;

import gov.nasa.arc.dert.landscape.QuadTree;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scenegraph.Text.AlignType;
import gov.nasa.arc.dert.viewpoint.BasicCamera;
import gov.nasa.arc.dert.viewpoint.ViewDependent;

import java.awt.Color;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;

/**
 * Base class for marker objects such as Landmarks and Tool actuators. Carries a
 * label.
 */
public abstract class Marker extends Movable implements ViewDependent {

	// marker size
	protected double size = 1;
	// scale factor for viewpoint resizing
	protected double scale = 1, oldScale = 1;

	// marker color
	protected Color color;
	// Ardor3D version of color
	protected ColorRGBA colorRGBA;
	// label color
	protected ReadOnlyColorRGBA labelColorRGBA = ColorRGBA.WHITE;
	// material state
	protected MaterialState materialState;

	// marker label
	protected RasterText label;
	// label contents
	protected String labelStr = "";

	protected Vector3 location;

	// contains label and spatials specific to marker type
	protected Node contents;

	/**
	 * Constructor
	 */
	public Marker(String name, ReadOnlyVector3 point, double size, Color color, boolean labelVisible, boolean pinned) {
		super(name);
		this.labelStr = name;
		this.size = size;
		setPinned(pinned);
		location = new Vector3();
		colorRGBA = new ColorRGBA(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f,
			color.getAlpha() / 255f);
		if (point != null) {
			super.setTranslation(point);
		}

		// default states
		// for transparency
		BlendState bs = new BlendState();
		bs.setBlendEnabled(true);
		setRenderState(bs);
		// turn off textures by default to block inherited textures
		TextureState textureState = new TextureState();
		textureState.setEnabled(false);
		setRenderState(textureState);
		// set the color and material state
		setColor(color);

		contents = new Node("_contents");
		contents.setScale(size);
		attachChild(contents);

		// create the label object
		createLabel(labelVisible);

		getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
	}

	/**
	 * Set the name and label
	 */
	@Override
	public void setName(String name) {
		super.setName(name);
		if (label != null) {
			label.setText(name);
			label.markDirty(DirtyType.RenderState);
		}
		markDirty(DirtyType.RenderState);
	}

	/**
	 * Get the size
	 * 
	 * @return
	 */
	public double getSize() {
		return (size);
	}

	/**
	 * Set the size
	 * 
	 * @param size
	 */
	public void setSize(double size) {
		if (this.size == size) {
			return;
		}
		this.size = size;
		scaleShape(scale);
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

	/**
	 * Get the color
	 * 
	 * @return
	 */
	public Color getColor() {
		return (color);
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

	protected abstract void setMaterialState();

	protected void createLabel(boolean labelVisible) {
		label = new RasterText("_label", labelStr, AlignType.Center);
		label.setScaleFactor((float) (0.75 * size));
		label.setColor(labelColorRGBA);
		label.setTranslation(0, 0, 1.5);
		label.setVisible(labelVisible);
		contents.attachChild(label);
	}

	/**
	 * Set label visibility
	 * 
	 * @param labelVisible
	 */
	public void setLabelVisible(boolean labelVisible) {
		label.setVisible(labelVisible);
	}

	/**
	 * Set label text
	 * 
	 * @param str
	 */
	public void setLabel(String str) {
		if (str == null) {
			str = "";
		}
		if (labelStr.equals(str)) {
			return;
		}
		labelStr = str;
		label.setText(labelStr);
		updateWorldBound(true);
	}

	/**
	 * Get label text
	 * 
	 * @return
	 */
	public String getLabel() {
		return (labelStr);
	}

	/**
	 * Determine label visibility
	 * 
	 * @return
	 */
	public boolean isLabelVisible() {
		return (label.isVisible());
	}

	/**
	 * Update size depending on camera location.
	 */
	@Override
	public void update(BasicCamera camera) {
		scale = camera.getPixelSizeAt(getWorldTranslation(), true) * PIXEL_SIZE;
		if (Math.abs(scale - oldScale) > 0.0000001) {
			oldScale = scale;
			scaleShape(scale);
		}
	}

	protected void scaleShape(double scale) {
		contents.setScale(size * scale);
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
	 * Update the Z coordinate when elevation changes
	 * 
	 * @param quadTree
	 * @return
	 */
	public boolean updateElevation(QuadTree quadTree) {
		ReadOnlyVector3 t = getWorldTranslation();
		if (quadTree.contains(t.getX(), t.getY())) {
			double z = World.getInstance().getLandscape().getZ(t.getX(), t.getY(), quadTree);
			if (!Double.isNaN(z)) {
				setTranslation(t.getX(), t.getY(), z);
				return (true);
			}
		}
		return (false);
	}

	@Override
	public String toString() {
		return (getName());
	}

	/**
	 * Get the location in planetary coordinates
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getLocation() {
		location.set(getWorldTranslation());
		World.getInstance().getLandscape().localToWorldCoordinate(location);
		return (location);
	}

}
