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
		none, arrow, ball, box, cone, cylinder, dart, disk, dome, flag, pyramid, quad, sphere, teapot, torus
	}

	public static Vector3[] SHAPE_TEXT_OFFSET = { new Vector3(0, 0, 0), new Vector3(0, 0, 0.3), new Vector3(0, 0, 1.2),
		new Vector3(0, 0, 1.2), new Vector3(0, 0, 1.2), new Vector3(0, 0, 1.2), new Vector3(0, 0, 1.2),
		new Vector3(0, 0, 0.3), new Vector3(0, 0, 1.2), new Vector3(0, 0, 2), new Vector3(0, 0, 1.2),
		new Vector3(0, 0, 0.5), new Vector3(0, 0, 0.6), new Vector3(0, 0, 2), new Vector3(0, 0, 0.5) };

	private Spatial geometry;

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param shapeType
	 * @param xSize
	 * @param ySize
	 * @param zSize
	 */
	public Shape(String name, ShapeType shapeType) {
		super(name);
		createGeometry(shapeType, 1, 1, 1);
	}

	private void createGeometry(ShapeType shapeType, float xSize, float ySize, float zSize) {
		switch (shapeType) {
		case none:
			break;
		case arrow:
			geometry = new Arrow("_arrow", xSize, xSize * 0.1f);
			break;
		case ball:
			geometry = new Sphere("_sphere", 50, 50, xSize * 0.5f);
			geometry.setTranslation(0, 0, xSize * 0.5f);
			break;
		case box:
			geometry = new Box("_box", new Vector3(), 0.5f * xSize, 0.5f * ySize, 0.5f * zSize);
			geometry.setTranslation(0, 0, 0.5f * zSize);
			break;
		case cone:
			geometry = new Cone("_cone", 50, 50, 0.5f * xSize, xSize, true);
			geometry.setRotation(new Matrix3().fromAngles(Math.PI, 0, 0));
			geometry.setTranslation(0, 0, 0.5f * xSize);
			break;
		case cylinder:
			geometry = new Cylinder("_cylinder", 50, 50, 0.5f * xSize, xSize, true);
			geometry.setTranslation(0, 0, 0.5f * xSize);
			break;
		case dart:
			geometry = new Cone("_dart", 50, 50, 0.5f * xSize, xSize, true);
			geometry.setTranslation(0, 0, 0.5f * xSize);
			break;
		case disk:
			geometry = new Disk("_disk", 50, 50, xSize);
			break;
		case dome:
			geometry = new Dome("_dome", 25, 50, xSize);
			geometry.setRotation(new Matrix3().fromAngles(Math.PI / 2, 0, 0));
			break;
		case flag:
			geometry = new Flag("_flag", 2 * xSize);
			geometry.setTranslation(0, 0, xSize);
			break;
		case pyramid:
			geometry = new Pyramid("_pyramid", xSize, xSize);
			geometry.setRotation(new Matrix3().fromAngles(Math.PI / 2, 0, 0));
			geometry.setTranslation(0, 0, xSize * 0.5f);
			break;
		case quad:
			geometry = new Quad("_quad", xSize, xSize);
			break;
		case sphere:
			geometry = new Sphere("_sphere", 50, 50, xSize * 0.5f);
			break;
		case teapot:
			geometry = new Teapot("_teapot");
			geometry.setRotation(new Matrix3().fromAngles(Math.PI / 2, 0, 0));
			geometry.setScale(new Vector3(0.5f * xSize, 0.5f * xSize, 0.5f * xSize));
			break;
		case torus:
			geometry = new Torus("_torus", 50, 50, 0.2f * xSize, xSize);
			break;
		}
		if (geometry == null) {
			return;
		}
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
