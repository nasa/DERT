package gov.nasa.arc.dert.scene.tool.fieldcamera;

import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.SpatialUtil;
import gov.nasa.arc.dert.util.UIUtil;
import gov.nasa.arc.dert.viewpoint.BasicCamera;

import java.awt.Color;
import java.nio.FloatBuffer;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
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
	private double height, width;

	// Color
	private ColorRGBA color;

	// Vertices
	private Vector3 peak = new Vector3(), vert0 = new Vector3(), vert1 = new Vector3(), vert2 = new Vector3(), vert3 = new Vector3();

	// Components
	private Mesh sides;
	
	// Camera
	private Vector3 normal = new Vector3();
	
	// Helper
	private Vector3 work = new Vector3();

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
	 * @param camera
	 *            the camera for which the frustum will be constructed.
	 * @param color
	 *            the color of the frustum.
	 */
	public FrustumPyramid(final String name, BasicCamera camera) {
		super(name);
		sides = new Mesh("_sides");
		sides.setModelBound(new BoundingBox());
//		sides.getSceneHints().setAllPickingHints(false);
		
		setCamera(camera);

		getSceneHints().setNormalsMode(NormalsMode.NormalizeIfScaled);
		getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
		attachChild(sides);
	}
	
	public void setCamera(BasicCamera camera) {
        height = Math.tan(Math.toRadians(camera.getFovY()) * 0.5);
        width = height * camera.getAspect(); 
		
		setVertexData(camera.getFrustumFar());
		setNormalData();
	}
	
	/**
	 * Change the length of the frustum.
	 * 
	 * @param length
	 */
	public void setLength(double length) {
		setVertexData(length);
	}

	/**
	 * Set the color
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = UIUtil.colorToColorRGBA(color);
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
		return (SpatialUtil.isDisplayed(this));
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
	protected void setVertexData(double length) {
        
        Vector3 left = new Vector3(Vector3.NEG_UNIT_X);
        left.multiplyLocal(width*length);
        
        Vector3 top = new Vector3(Vector3.UNIT_Y);
        top.multiplyLocal(height*length);
        
        Vector3 center = new Vector3(Vector3.NEG_UNIT_Z);
        center.multiplyLocal(length);
        
        vert0.set(center);
        vert0.addLocal(left);
        vert0.subtractLocal(top);
        
        vert1.set(center);
        vert1.subtractLocal(left);
        vert1.subtractLocal(top);
        
        vert2.set(center);
        vert2.subtractLocal(left);
        vert2.addLocal(top);
        
        vert3.set(center);
        vert3.addLocal(left);
        vert3.addLocal(top);

		FloatBuffer verts = sides.getMeshData().getVertexBuffer();
		if (verts == null)
			verts = BufferUtils.createVector3Buffer(12);
		verts.rewind();

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

		verts.flip();
		sides.getMeshData().setVertexBuffer(verts);
		sides.updateModelBound();
	}

	/**
	 * Defines the normals of each face of the pyramid.
	 */
	protected void setNormalData() {

		FloatBuffer norms = sides.getMeshData().getNormalBuffer();
		
		if (norms == null)
			norms = BufferUtils.createVector3Buffer(12);

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

		norms.flip();
		sides.getMeshData().setNormalBuffer(norms);
	}
}