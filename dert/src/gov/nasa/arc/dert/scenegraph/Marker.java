package gov.nasa.arc.dert.scenegraph;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.QuadTree;
import gov.nasa.arc.dert.scenegraph.text.RasterText;
import gov.nasa.arc.dert.scenegraph.text.Text.AlignType;
import gov.nasa.arc.dert.util.UIUtil;
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
import com.ardor3d.scenegraph.extension.BillboardNode;
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
	protected BillboardNode billboard;

	protected Vector3 worldLoc;

	// contains label and spatials specific to marker type
	protected Node contents;
	
	protected boolean labelVisible;

	/**
	 * Constructor
	 */
	public Marker(String name, ReadOnlyVector3 point, double size, double zOff, Color color, boolean labelVisible, boolean pinned) {
		super(name);
		billboard = new BillboardNode("_billboard");
		this.labelStr = name;
		this.labelVisible = labelVisible;
		this.size = size;
		this.zOff = zOff;
		setPinned(pinned);
		worldLoc = new Vector3();
		if (point != null)
			setLocation(point, false);

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
		createLabel();
		billboard.attachChild(label);
		contents.attachChild(billboard);

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
		color = newColor;
		colorRGBA = UIUtil.colorToColorRGBA(color);
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

	protected void createLabel() {
		label = new RasterText("_label", labelStr, AlignType.Center, true);
		label.setScaleFactor((float) (0.75 * size));
		label.setColor(labelColorRGBA);
		label.setTranslation(0, 2, 0);
		label.setVisible(labelVisible);
	}

	/**
	 * Set label visibility
	 * 
	 * @param labelVisible
	 */
	public void setLabelVisible(boolean labelVisible) {
		this.labelVisible = labelVisible;
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
		return (labelVisible);
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
	
	public double getShapeScale() {
		return(scale);
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
		ReadOnlyVector3 t = getLocation();
		if (quadTree.contains(t.getX(), t.getY())) {
			double z = Landscape.getInstance().getZ(t.getX(), t.getY(), quadTree);
			if (!Double.isNaN(z)) {
				setLocation(t.getX(), t.getY(), z, false);
				return (true);
			}
		}
		return (false);
	}

	@Override
	public String toString() {
		return (getName());
	}

}
