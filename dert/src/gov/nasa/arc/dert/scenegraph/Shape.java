package gov.nasa.arc.dert.scenegraph;

import gov.nasa.arc.dert.util.SpatialUtil;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Arrow;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.scenegraph.shape.Cone;
import com.ardor3d.scenegraph.shape.Cylinder;
import com.ardor3d.scenegraph.shape.Disk;
import com.ardor3d.scenegraph.shape.Dome;
import com.ardor3d.scenegraph.shape.Pyramid;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.scenegraph.shape.Sphere;
import com.ardor3d.scenegraph.shape.Teapot;
import com.ardor3d.scenegraph.shape.Torus;

/**
 * Provides a 3D object of one or more Ardor3D meshes.
 *
 */
public class Shape extends Node {

	public static enum ShapeType {
		none, arrow, ball, box, cone, cylinder, dart, disk, dome, flag, pyramid, quad, rod, sphere, teapot, torus
	}

	public static Vector3[] SHAPE_TEXT_OFFSET = { new Vector3(0, 0, 0), new Vector3(0, 0.3, 0), new Vector3(0, 1.2, 0),
		new Vector3(0, 1.2, 0), new Vector3(0, 1.2, 0), new Vector3(0, 1.2, 0), new Vector3(0, 1.2, 0),
		new Vector3(0, 0.3, 0), new Vector3(0, 0.6, 0), new Vector3(0, 2.1, 0), new Vector3(0, 1.2, 0),
		new Vector3(0, 0.5, 0), new Vector3(0, 0.5, 0), new Vector3(0, 0.6, 0), new Vector3(0, 1, 0), new Vector3(0, 0.5, 0) };

	protected Spatial geometry;
	
	/**
	 * Create an instance of shape containing a geometry specified by type. Arguments
	 * are treated according to geometry type.
	 * 
	 * none: No geometry is created. Arguments are ignored.
	 * arrow: arg1=length, arg2=width
	 * ball: arg1=samples, arg2=radius
	 * box: arg1=xExtent, arg2=yExtent, arg3=zExtent
	 * cone: arg1=samples, arg2=radius, arg3=height
	 * cylinder: arg1=samples, arg2=radius, arg3=height
	 * dart: arg1=samples, arg2=radius, arg3=height
	 * disk: arg1=samples, arg2=radius
	 * dome: arg1=samples, arg2=radius
	 * flag: arg1=height
	 * pyramid: arg1=width, arg2=height
	 * quad: arg1=size
	 * rod: arg1=cellCount, arg2=radius, arg3=length 
	 * sphere: arg1=samples, arg2=radius
	 * teapot: arg1=size
	 * torus: arg1=samples, arg2=tubeRadius, arg3=centerRadius
	 * 
	 * @param name
	 * @param shapeType
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return
	 */
	public static Shape createShape(String name, ShapeType shapeType, float arg1, float arg2, float arg3) {
		Spatial geometry = null;
		switch (shapeType) {
		case none:
			break;
		case arrow:
			geometry = new Arrow("_arrow", arg1, arg2);
			break;
		case ball:
			geometry = new Sphere("_sphere", (int)arg1, (int)arg1, arg2);
			geometry.setTranslation(0, 0, arg1 * 0.5f);
			break;
		case box:
			geometry = new Box("_box", new Vector3(), 0.5f * arg1, 0.5f * arg2, 0.5f * arg3);
			geometry.setTranslation(0, 0, 0.5f * arg3);
			break;
		case cone:
			geometry = new Cone("_cone", (int)arg1, (int)arg1, arg2, arg3, true);
			geometry.setRotation(new Matrix3().fromAngles(Math.PI, 0, 0));
			geometry.setTranslation(0, 0, 0.5f * arg3);
			break;
		case cylinder:
			geometry = new Cylinder("_cylinder", (int)arg1, (int)arg1, arg2, arg3, true);
			geometry.setTranslation(0, 0, 0.5f * arg3);
			break;
		case dart:
			geometry = new Cone("_dart", (int)arg1, (int)arg1, arg2, arg3, true);
			geometry.setTranslation(0, 0, 0.5f * arg3);
			break;
		case disk:
			geometry = new Disk("_disk", (int)arg1, (int)arg1, arg2);
			break;
		case dome:
			geometry = new Dome("_dome", (int)arg1/2, (int)arg1, arg2);
			geometry.setRotation(new Matrix3().fromAngles(Math.PI / 2, 0, 0));
			break;
		case flag:
			geometry = new Flag("_flag", arg1);
			geometry.setTranslation(0, 0, 0.5f*arg1);
			break;
		case pyramid:
			geometry = new Pyramid("_pyramid", arg1, arg2);
			geometry.setRotation(new Matrix3().fromAngles(Math.PI / 2, 0, 0));
			geometry.setTranslation(0, 0, arg1 * 0.5f);
			break;
		case quad:
			geometry = new Quad("_quad", arg1, arg1);
			break;
		case rod:
			geometry = new Rod("_rod", (int)arg1, 30, arg2, arg3);
			geometry.setTranslation(0, 0, arg2);
			break;
		case sphere:
			geometry = new Sphere("_sphere", (int)arg1, (int)arg1, arg2);
			break;
		case teapot:
			geometry = new Teapot("_teapot");
			geometry.setRotation(new Matrix3().fromAngles(Math.PI / 2, 0, 0));
			geometry.setScale(new Vector3(arg1, arg1, arg1));
			break;
		case torus:
			geometry = new Torus("_torus", (int)arg1, (int)arg1, arg2, arg3);
			break;
		}
		if (geometry == null) {
			return(new Shape(name, ShapeType.none, null));
		}
		return(new Shape(name, shapeType, geometry));
	}
	
	/**
	 * Create a shape with unit size.
	 * @param name
	 * @param shapeType
	 * @param size
	 * @return
	 */
	public static Shape createShape(String name, ShapeType shapeType, float size) {
		Spatial geometry = null;
		switch (shapeType) {
		case none:
			break;
		case arrow:
			geometry = new Arrow("_arrow", size, size * 0.1f);
			break;
		case ball:
			geometry = new Sphere("_sphere", 50, 50, size * 0.5f);
			geometry.setTranslation(0, 0, size * 0.5f);
			break;
		case box:
			geometry = new Box("_box", new Vector3(), 0.5f * size, 0.5f * size, 0.5f * size);
			geometry.setTranslation(0, 0, 0.5f * size);
			break;
		case cone:
			geometry = new Cone("_cone", 50, 50, 0.5f * size, size, true);
			geometry.setRotation(new Matrix3().fromAngles(Math.PI, 0, 0));
			geometry.setTranslation(0, 0, 0.5f * size);
			break;
		case cylinder:
			geometry = new Cylinder("_cylinder", 50, 50, 0.5f * size, size, true);
			geometry.setTranslation(0, 0, 0.5f * size);
			break;
		case dart:
			geometry = new Cone("_dart", 50, 50, 0.5f * size, size, true);
			geometry.setTranslation(0, 0, 0.5f * size);
			break;
		case disk:
			geometry = new Disk("_disk", 50, 50, 0.5*size);
			break;
		case dome:
			geometry = new Dome("_dome", 25, 50, 0.5f*size);
			geometry.setRotation(new Matrix3().fromAngles(Math.PI / 2, 0, 0));
			break;
		case flag:
			geometry = new Flag("_flag", 2 * size);
			geometry.setTranslation(0, 0, size);
			break;
		case pyramid:
			geometry = new Pyramid("_pyramid", size, size);
			geometry.setRotation(new Matrix3().fromAngles(Math.PI / 2, 0, 0));
			geometry.setTranslation(0, 0, size * 0.5f);
			break;
		case quad:
			geometry = new Quad("_quad", size, size);
			break;
		case rod:
			geometry = new Rod("_rod", 20, 20, 0.1f * size, size);
			geometry.setTranslation(0, 0, 0.1f * size);
			break;
		case sphere:
			int n = 50;
			if (size < 1)
				n = 25;
			geometry = new Sphere("_sphere", n, n, size * 0.5f);
			break;
		case teapot:
			geometry = new Teapot("_teapot");
			geometry.setRotation(new Matrix3().fromAngles(Math.PI / 2, 0, 0));
			geometry.setScale(new Vector3(0.25f * size, 0.25f * size, 0.25f * size));
			break;
		case torus:
			geometry = new Torus("_torus", 50, 50, 0.1f*size, 0.5f*size);
			break;
		}
		if (geometry == null) {
			return(new Shape(name, ShapeType.none, null));
		}
		return(new Shape(name, shapeType, geometry));
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param shapeType
	 * @param xSize
	 * @param ySize
	 * @param zSize
	 */
	protected Shape(String name, ShapeType shapeType, Spatial geometry) {
		super(name);
		this.geometry = geometry;
		if (geometry != null) {
			if (geometry instanceof Mesh) {
				Mesh mesh = (Mesh) geometry;
				mesh.setModelBound(new BoundingBox());
				mesh.updateModelBound();
			}
			TextureState textureState = new TextureState();
			textureState.setEnabled(false);
			geometry.setRenderState(textureState);
			geometry.markDirty(DirtyType.RenderState);
			geometry.getSceneHints().setCullHint(CullHint.Dynamic);
			SpatialUtil.setPickHost(geometry, this);
			attachChild(geometry);
		}
	}
	
	public Spatial getGeometry() {
		return(geometry);
	}

}
