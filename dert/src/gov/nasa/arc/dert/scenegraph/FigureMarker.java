package gov.nasa.arc.dert.scenegraph;

import gov.nasa.arc.dert.landscape.Landscape;
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

	public static float AMBIENT_FACTOR = 0.75f;

	// Rotation
	protected double azimuth, tilt;
	protected Matrix3 rotMat;

	// 3D shape
	protected Shape shape;
	protected ShapeType shapeType = ShapeType.none;

	// Arrow to show surface normal
	protected DirectionArrow surfaceNormalArrow;
	protected Vector3 surfaceNormal = new Vector3();

	// flag to maintain the original size as viewpoint changes. If true, resize
	// as viewpoint changes.
	protected boolean autoScale;
	protected boolean autoShowLabel = false;

	/**
	 * Constructor
	 */
	public FigureMarker(String name, ReadOnlyVector3 point, double size, double zOff, Color color, boolean labelVisible,
		boolean autoScale, boolean pinned) {
		super(name, point, (float) size, zOff, color, labelVisible, pinned);
		this.autoScale = autoScale;
		surfaceNormalArrow = new DirectionArrow("Surface Normal", (float)(size*2), ColorRGBA.RED);
		surfaceNormalArrow.getSceneHints().setCullHint(CullHint.Always);
		contents.attachChild(surfaceNormalArrow);
	}

	/**
	 * Set the type of 3D shape
	 * 
	 * @param type
	 */
	public void setShape(ShapeType type) {
		if (shape != null) {
			contents.detachChild(shape);
		}
		shapeType = type;
		shape = Shape.createShape("_geometry", shapeType, (float)size);
		shape.updateWorldBound(true);
		SpatialUtil.setPickHost(shape, this);
		contents.attachChild(shape);
		Vector3 offset = Shape.SHAPE_TEXT_OFFSET[shapeType.ordinal()];
		label.setTranslation(offset.getX(), offset.getY()*size, offset.getZ());
		updateWorldTransform(true);
		updateWorldBound(true);
		scaleShape(scale);
	}
	
	public void setAutoShowLabel(boolean show) {
		autoShowLabel = show;
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
		markDirty(DirtyType.RenderState);
	}

	@Override
	protected void scaleShape(double scale) {
		if (autoScale)
			contents.setScale(scale);
		else
			contents.setScale(1);
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
			shape.setRotation(rotMat.fromAngles(Math.toRadians(tilt), 0.0, -Math.toRadians(azimuth)));
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
			shape.setRotation(rotMat.fromAngles(Math.toRadians(tilt), 0.0, -Math.toRadians(azimuth)));
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
			if (surfaceNormalArrow != null)
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
	 * Set the size
	 * 
	 * @param size
	 */
	@Override
	public void setSize(double size) {
		if (this.size == size) {
			return;
		}
		this.size = size;
		if (surfaceNormalArrow != null)
			surfaceNormalArrow.setLength(size*1.5);
		setShape(shapeType);
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
		if (!auto) {
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
		if (labelVisible) {
			if (autoShowLabel) {
				if (scale <= PIXEL_SIZE) {
					if (!label.isVisible())
						label.setVisible(true);
				}
				else if (label.isVisible())
					label.setVisible(false);
			}
		}
	}
	
	@Override
	public void setInMotion(boolean inMotion, ReadOnlyVector3 pickPosition) {
		if (isInMotion() && !inMotion) {
			if (surfaceNormalArrow != null) {
				if (SpatialUtil.isDisplayed(surfaceNormalArrow)) {
					Landscape.getInstance().getNormal(getLocation().getX(), getLocation().getY(), surfaceNormal);
					surfaceNormalArrow.setDirection(surfaceNormal);
				}
			}
		}
		super.setInMotion(inMotion, pickPosition);		
	}

}
