package gov.nasa.arc.dert.scene.tool.fieldcamera;

import gov.nasa.arc.dert.util.MathUtil;

import java.nio.FloatBuffer;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.NormalsMode;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.util.geom.BufferUtils;

/**
 * A pyramid that defines the field of view (FOV) of a camera. The top of the
 * pyramid is located at the camera and pyramid itself is defined by a width at
 * the base and a height. The pyramid will be axis aligned with the default
 * OpenGL camera. That is, the peak is located at (0,0,0) and the base is
 * parallel to the XY plane at some point on the -Z axis.
 * 
 * Adapted from Ardor3D example
 */
public class FrustumPyramid extends Node {

	// Dimensions
	private double height, width, length;

	// Color
	private ColorRGBA color;

	// Vertices
	private Vector3 peak, vert0, vert1, vert2, vert3;

	// Components
	private Mesh sides;

	/**
	 * Constructor
	 */
	public FrustumPyramid() {
		// nothing here
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 *            the name of the scene element.
	 * @param width
	 *            the base width of the pyramid.
	 * @param height
	 *            the height of the pyramid from the base to the peak.
	 */
	public FrustumPyramid(final String name, final double width, final double height, final double length,
		ReadOnlyColorRGBA color) {
		super(name);
		this.width = width;
		this.height = height;
		this.length = length;
		this.color = new ColorRGBA(color);
		sides = new Mesh("_sides");

		setVertexData();
		setNormalData();
		getSceneHints().setNormalsMode(NormalsMode.NormalizeIfScaled);
		setColor(this.color);
		getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
		sides.setModelBound(new BoundingBox());
		sides.updateModelBound();
		sides.getSceneHints().setAllPickingHints(false);
		attachChild(sides);
	}

	/**
	 * Set the color
	 * 
	 * @param color
	 */
	public void setColor(ReadOnlyColorRGBA color) {
		this.color.set(color);
		MaterialState ms = new MaterialState();
		this.color.setAlpha(0.3f);
		ms.setDiffuse(MaterialState.MaterialFace.FrontAndBack, this.color);
		ms.setAmbient(MaterialState.MaterialFace.FrontAndBack, ColorRGBA.BLACK);
		ms.setEmissive(MaterialState.MaterialFace.FrontAndBack, this.color);
		ms.setEnabled(true);
		setRenderState(ms);
	}

	/**
	 * Get visibility
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
			getSceneHints().setCullHint(CullHint.Dynamic);
		} else {
			getSceneHints().setCullHint(CullHint.Always);
		}
		getSceneHints().setPickingHint(PickingHint.Pickable, visible);
	}

	/**
	 * Sets the vertices that make the pyramid. Where the center of the box is
	 * the origin and the base and height are set during construction.
	 */
	protected void setVertexData() {
		peak = new Vector3(0, 0, 0);
		vert0 = new Vector3(-width / 2, -height / 2, -length);
		vert1 = new Vector3(width / 2, -height / 2, -length);
		vert2 = new Vector3(width / 2, height / 2, -length);
		vert3 = new Vector3(-width / 2, height / 2, -length);

		FloatBuffer verts = BufferUtils.createVector3Buffer(12);

		// side 1
		verts.put((float) vert0.getX()).put((float) vert0.getY()).put((float) vert0.getZ());
		verts.put((float) vert1.getX()).put((float) vert1.getY()).put((float) vert1.getZ());
		verts.put((float) peak.getX()).put((float) peak.getY()).put((float) peak.getZ());

		// side 2
		verts.put((float) vert1.getX()).put((float) vert1.getY()).put((float) vert1.getZ());
		verts.put((float) vert2.getX()).put((float) vert2.getY()).put((float) vert2.getZ());
		verts.put((float) peak.getX()).put((float) peak.getY()).put((float) peak.getZ());

		// side 3
		verts.put((float) vert2.getX()).put((float) vert2.getY()).put((float) vert2.getZ());
		verts.put((float) vert3.getX()).put((float) vert3.getY()).put((float) vert3.getZ());
		verts.put((float) peak.getX()).put((float) peak.getY()).put((float) peak.getZ());

		// side 4
		verts.put((float) vert3.getX()).put((float) vert3.getY()).put((float) vert3.getZ());
		verts.put((float) vert0.getX()).put((float) vert0.getY()).put((float) vert0.getZ());
		verts.put((float) peak.getX()).put((float) peak.getY()).put((float) peak.getZ());

		verts.rewind();
		sides.getMeshData().setVertexBuffer(verts);
	}

	/**
	 * Defines the normals of each face of the pyramid.
	 */
	protected void setNormalData() {

		Vector3 normal = new Vector3();
		Vector3 work = new Vector3();

		FloatBuffer norms = BufferUtils.createVector3Buffer(12);

		// side 1
		MathUtil.createNormal(normal, vert0, vert1, peak, work);
		norms.put((float) normal.getX()).put((float) normal.getY()).put((float) normal.getZ());
		norms.put((float) normal.getX()).put((float) normal.getY()).put((float) normal.getZ());
		norms.put((float) normal.getX()).put((float) normal.getY()).put((float) normal.getZ());

		// side 2
		MathUtil.createNormal(normal, vert1, vert2, peak, work);
		norms.put((float) normal.getX()).put((float) normal.getY()).put((float) normal.getZ());
		norms.put((float) normal.getX()).put((float) normal.getY()).put((float) normal.getZ());
		norms.put((float) normal.getX()).put((float) normal.getY()).put((float) normal.getZ());

		// side 3
		MathUtil.createNormal(normal, vert2, vert3, peak, work);
		norms.put((float) normal.getX()).put((float) normal.getY()).put((float) normal.getZ());
		norms.put((float) normal.getX()).put((float) normal.getY()).put((float) normal.getZ());
		norms.put((float) normal.getX()).put((float) normal.getY()).put((float) normal.getZ());

		// side 4
		MathUtil.createNormal(normal, vert3, vert0, peak, work);
		norms.put((float) normal.getX()).put((float) normal.getY()).put((float) normal.getZ());
		norms.put((float) normal.getX()).put((float) normal.getY()).put((float) normal.getZ());
		norms.put((float) normal.getX()).put((float) normal.getY()).put((float) normal.getZ());

		norms.rewind();
		sides.getMeshData().setNormalBuffer(norms);
	}
}