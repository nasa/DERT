package gov.nasa.arc.dert.scenegraph;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.TextureCombineMode;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Provides a single line segment object.
 */
public class LineSegment extends Line {

	/**
	 * Constructor
	 * 
	 * @param p0
	 *            first end point
	 * @param p1
	 *            second end point
	 */
	public LineSegment(String name, ReadOnlyVector3 p0, ReadOnlyVector3 p1) {
		super(name);
		getSceneHints().setTextureCombineMode(TextureCombineMode.Off);
		float[] vertex = new float[6];
		vertex[0] = (float) p0.getX();
		vertex[1] = (float) p0.getY();
		vertex[2] = (float) p0.getZ();
		vertex[3] = (float) p1.getX();
		vertex[4] = (float) p1.getY();
		vertex[5] = (float) p1.getZ();
		_meshData.setVertexBuffer(BufferUtils.createFloatBuffer(vertex));
		_meshData.setNormalBuffer(null);
		_meshData.setColorBuffer(null);
		_meshData.setTextureCoords(null, 0);
		_meshData.setIndices(null);
		setModelBound(new BoundingBox());
		updateModelBound();
	}

	/**
	 * Set the endpoints of the line segment
	 * 
	 * @param p0
	 * @param p1
	 */
	public void setPoints(ReadOnlyVector3 p0, ReadOnlyVector3 p1) {
		float[] vertex = new float[6];
		vertex[0] = (float) p0.getX();
		vertex[1] = (float) p0.getY();
		vertex[2] = (float) p0.getZ();
		vertex[3] = (float) p1.getX();
		vertex[4] = (float) p1.getY();
		vertex[5] = (float) p1.getZ();
		_meshData.setVertexBuffer(BufferUtils.createFloatBuffer(vertex));
		markDirty(DirtyType.Bounding);
	}
	
	public void setColor(ReadOnlyColorRGBA color) {
		MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.None);
		ms.setDiffuse(ColorRGBA.BLACK);
		ms.setAmbient(ColorRGBA.BLACK);
		ms.setEmissive(MaterialState.MaterialFace.FrontAndBack, color);
		setRenderState(ms);
	}
}
