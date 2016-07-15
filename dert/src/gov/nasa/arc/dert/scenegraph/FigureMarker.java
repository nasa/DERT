package gov.nasa.arc.dert.scenegraph;

import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.util.SpatialUtil;
import gov.nasa.arc.dert.viewpoint.BasicCamera;

import java.awt.Color;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.MaterialState.MaterialFace;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;

/**
 * Provides a 3D marker object.
 */
public class FigureMarker extends Marker {

	protected static float AMBIENT_FACTOR = 0.75f;

	// Rotation
	protected double azimuth, tilt;
	protected Matrix3 rotMat;

	// 3D shape
	protected Shape shape;
	protected ShapeType shapeType;

	// Arrow to show surface normal
	protected DirectionArrow surfaceNormalArrow;
	protected Vector3 surfaceNormal = new Vector3();

	// flag to maintain the original size as viewpoint changes. If true, resize
	// as viewpoint changes.
	protected boolean autoScale;

	/**
	 * Constructor
	 */
	public FigureMarker(String name, ReadOnlyVector3 point, double size, Color color, boolean labelVisible,
		boolean autoScale, boolean pinned) {
		super(name, point, (float) size, color, labelVisible, pinned);
		this.autoScale = autoScale;
		surfaceNormalArrow = new DirectionArrow("Surface Normal", 2, ColorRGBA.RED);
		surfaceNormalArrow.getSceneHints().setCullHint(CullHint.Always);
		contents.attachChild(surfaceNormalArrow);
	}

	/**
	 * Set the type of 3D shape
	 * 
	 * @param type
	 */
	public void setShape(ShapeType type) {
		if (shapeType == type) {
			return;
		}
		if (shape != null) {
			contents.detachChild(shape);
		}
		shapeType = type;
		shape = new Shape("_geometry", shapeType);
		shape.updateWorldBound(true);
		SpatialUtil.setPickHost(shape, this);
		contents.attachChild(shape);
		label.setTranslation(Shape.SHAPE_TEXT_OFFSET[shapeType.ordinal()]);
		updateWorldTransform(true);
		updateWorldBound(true);
		scaleShape(scale);
	}

	@Override
	protected void setMaterialState() {
		materialState.setAmbient(MaterialFace.FrontAndBack, new ColorRGBA(colorRGBA.getRed() * AMBIENT_FACTOR,
			colorRGBA.getGreen() * AMBIENT_FACTOR, colorRGBA.getBlue() * AMBIENT_FACTOR, colorRGBA.getAlpha()));
		materialState.setDiffuse(MaterialFace.FrontAndBack, colorRGBA);
		materialState.setEmissive(MaterialFace.FrontAndBack, ColorRGBA.BLACK);
	}

	@Override
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
	 * Determine if fixed size
	 * 
	 * @return
	 */
	public boolean isAutoScale() {
		return (autoScale);
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
		if (shape != null) {
			rotMat = new Matrix3();
			shape.setRotation(rotMat.fromAngles(-Math.toRadians(tilt), 0.0, -Math.toRadians(azimuth)));
		}
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
		if (shape != null) {
			rotMat = new Matrix3();
			shape.setRotation(rotMat.fromAngles(-Math.toRadians(tilt), 0.0, -Math.toRadians(azimuth)));
		}
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
	 * Set the surface normal
	 * 
	 * @param normal
	 */
	public void setNormal(ReadOnlyVector3 normal) {
		if (normal != null) {
			surfaceNormal.set(normal);
			surfaceNormalArrow.setDirection(normal);
		}
	}

	/**
	 * Get the surface normal
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getNormal() {
		return (surfaceNormal);
	}

	/**
	 * Get the type of shape
	 * 
	 * @return
	 */
	public ShapeType getShapeType() {
		return (shapeType);
	}

	/**
	 * Set fixed size
	 * 
	 * @param fixed
	 */
	public void setAutoScale(boolean auto) {
		if (autoScale == auto) {
			return;
		}
		autoScale = auto;
		if (!autoScale) {
			oldScale = scale;
			scale = 1;
		} else {
			scale = oldScale;
		}
		scaleShape(scale);
		markDirty(DirtyType.Transform);
	}

	/**
	 * Update size based on camera location
	 */
	@Override
	public void update(BasicCamera camera) {
		scale = camera.getPixelSizeAt(getWorldTranslation(), true) * PIXEL_SIZE;
		if (Math.abs(scale - oldScale) > 0.0000001) {
			oldScale = scale;
			if (autoScale) {
				scaleShape(scale);
			}
		}
	}

}
