package gov.nasa.arc.dert.scene.tool;

import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scenegraph.BillboardMarker;
import gov.nasa.arc.dert.scenegraph.FigureMarker;
import gov.nasa.arc.dert.scenegraph.Shape;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.state.WaypointState;
import gov.nasa.arc.dert.util.ImageUtil;
import gov.nasa.arc.dert.util.SpatialUtil;

import javax.swing.Icon;

import com.ardor3d.image.Texture;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.MaterialState.MaterialFace;
import com.ardor3d.scenegraph.Node;

/**
 * Provides a map element that serves as a waypoint in a path
 *
 */
public class Waypoint extends BillboardMarker implements MapElement {

	public static final Icon icon = Icons.getImageIcon("waypoint_24.png");
	public static String defaultIconName = "waypoint.png";

	// Waypoint texture
	protected static Texture texture;

	// Map element state
	protected WaypointState state;
	
	// Waypoint is represented as sphere on the surface
	protected boolean useSphere;
	protected Shape shape;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public Waypoint(WaypointState state) {
		super(state.name, state.location, state.size, state.zOff, state.color, state.labelVisible, state.pinned);
		if (texture == null) {
			texture = ImageUtil.createTexture(Icons.getIconURL(defaultIconName), true);
		}
		setTexture(texture, texture);
		setVisible(state.visible);
		this.state = state;
		state.setMapElement(this);
	}

	/**
	 * Get the map element state
	 */
	@Override
	public MapElementState getState() {
		return (state);
	}

	/**
	 * Get the point and distance to seek to
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
		return (Type.Waypoint);
	}

	/**
	 * Get the name of the parent path
	 * 
	 * @return
	 */
	public String getPathName() {
		String str = getName();
		int indx = str.lastIndexOf(".");
		str = str.substring(0, indx);
		return (str);
	}

	/**
	 * Get the parent path
	 * 
	 * @return
	 */
	public Path getPath() {
		Node parent = getParent();
		if (parent == null) {
			return (null);
		}
		return ((Path) parent.getParent());
	}

	public void showAsSphere(boolean useSphere) {
		if (this.useSphere == useSphere)
			return;
		this.useSphere = useSphere;
		if (useSphere) {
			contents.detachChild(billboard);
			shape = Shape.createShape("_geometry", ShapeType.sphere, (float)(size*0.5));
			shape.updateWorldBound(true);
			SpatialUtil.setPickHost(shape, this);
			contents.attachChild(shape);
			updateWorldBound(true);
			scaleShape(scale);
		}
		else {
			contents.detachChild(shape);
			shape = null;
			contents.attachChild(billboard);
			updateWorldBound(true);
			scaleShape(scale);
		}
		setMaterialState();
	}

	@Override
	protected void setMaterialState() {
		if (useSphere) {
			materialState.setAmbient(MaterialFace.FrontAndBack, new ColorRGBA(colorRGBA.getRed() * FigureMarker.AMBIENT_FACTOR,
					colorRGBA.getGreen() * FigureMarker.AMBIENT_FACTOR, colorRGBA.getBlue() * FigureMarker.AMBIENT_FACTOR, colorRGBA.getAlpha()));
			materialState.setDiffuse(MaterialFace.FrontAndBack, colorRGBA);
			materialState.setEmissive(MaterialFace.FrontAndBack, ColorRGBA.BLACK);
		}
		else
			super.setMaterialState();
	}

	@Override
	protected void enableHighlight(boolean enable) {
		if (useSphere) {
			if (enable) {
				materialState.setAmbient(MaterialFace.FrontAndBack, new ColorRGBA(colorRGBA.getRed() * FigureMarker.AMBIENT_FACTOR,
					colorRGBA.getGreen() * FigureMarker.AMBIENT_FACTOR, colorRGBA.getBlue() * FigureMarker.AMBIENT_FACTOR, colorRGBA.getAlpha()));
				materialState.setDiffuse(MaterialFace.FrontAndBack, colorRGBA);
				materialState.setEmissive(MaterialFace.FrontAndBack, colorRGBA);
			} else {
				setMaterialState();
			}
		}
		else {
			if (enable) {
				billboard.setTexture(highlightTexture);
				materialState.setEmissive(MaterialFace.FrontAndBack, highlightColorRGBA);
			} else {
				billboard.setTexture(nominalTexture);
				materialState.setEmissive(MaterialFace.FrontAndBack, colorRGBA);
			}
		}
	}

//	@Override
//	protected void createLabel(boolean labelVisible) {
//		if (useSphere)
//			super.createLabel(labelVisible);
//		else {
//			label = new RasterText("_label", labelStr, AlignType.Center, true);
//			label.setScaleFactor((float) (0.75 * size));
//			label.setColor(labelColorRGBA);
//			label.setTranslation(0, 1.5, 0);
//			label.setVisible(labelVisible);
//		}
//	}

}
