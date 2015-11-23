package gov.nasa.arc.dert.util;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.util.geom.BufferUtils;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUtessellator;
import com.jogamp.opengl.glu.GLUtessellatorCallback;
import com.jogamp.opengl.glu.GLUtessellatorCallbackAdapter;

/**
 * Tessellator provides methods to tessellate a polygon that can be convex,
 * concave, and have holes.
 *
 */
public class Tessellator {

	protected List<ReadOnlyVector3> outerVertex;
	protected List<List<ReadOnlyVector3>> innerVertex;
	protected ArrayList<Vector3> tessellatedPolygon = new ArrayList<Vector3>();
	protected FloatBuffer vertexBuffer;
	protected Vector3 referencePoint;

	public Tessellator() {
		// nothing here
	}

	protected void tessellateInterior(GLUtessellatorCallback callback) {
		GLU glu = new GLU();
		GLUtessellator tess = GLU.gluNewTess();
		this.beginTessellation(tess, callback);

		try {
			this.doTessellate(glu, tess, callback);
		} finally {
			this.endTessellation(tess);
			GLU.gluDeleteTess(tess);
		}

		vertexBuffer = BufferUtils.createFloatBuffer(tessellatedPolygon.size() * 3);
		for (int i = 0; i < tessellatedPolygon.size(); ++i) {
			Vector3 vec = tessellatedPolygon.get(i);
			vertexBuffer.put(vec.getXf());
			vertexBuffer.put(vec.getYf());
			vertexBuffer.put(vec.getZf());
		}
		vertexBuffer.flip();
	}

	protected void beginTessellation(GLUtessellator tess, GLUtessellatorCallback callback) {
		GLU.gluTessNormal(tess, 0.0, 0.0, 1.0);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_BEGIN, callback);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_VERTEX, callback);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_END, callback);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_COMBINE, callback);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_EDGE_FLAG, callback);
	}

	protected void endTessellation(GLUtessellator tess) {
		GLU.gluTessCallback(tess, GLU.GLU_TESS_BEGIN, null);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_VERTEX, null);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_END, null);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_COMBINE, null);
		GLU.gluTessCallback(tess, GLU.GLU_TESS_EDGE_FLAG, null);
	}

	protected void doTessellate(GLU glu, GLUtessellator tess, GLUtessellatorCallback callback) {
		// Determine the winding order of the shape vertices, and setup the GLU
		// winding rule which corresponds to
		// the shapes winding order.
		int windingRule = (MathUtil.isCounterClockwise(outerVertex)) ? GLU.GLU_TESS_WINDING_POSITIVE
			: GLU.GLU_TESS_WINDING_NEGATIVE;
		// System.err.println("Tessellator.doTessellate "+windingRule+" "+GLU.GLU_TESS_WINDING_POSITIVE+" "+GLU.GLU_TESS_WINDING_NEGATIVE);

		GLU.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, windingRule);
		GLU.gluTessBeginPolygon(tess, null);
		GLU.gluTessBeginContour(tess);

		for (ReadOnlyVector3 pos : outerVertex) {
			double[] compArray = new double[3];
			compArray[0] = pos.getX();
			compArray[1] = pos.getY();
			compArray[2] = pos.getZ();
			GLU.gluTessVertex(tess, compArray, 0, compArray);
		}
		GLU.gluTessEndContour(tess);

		if (innerVertex != null) {
			for (int i = 0; i < innerVertex.size(); ++i) {
				GLU.gluTessBeginContour(tess);
				for (ReadOnlyVector3 ll : innerVertex.get(i)) {
					ReadOnlyVector3 pos = ll;
					double[] compArray = new double[3];
					compArray[0] = pos.getX();
					compArray[1] = pos.getY();
					compArray[2] = pos.getZ();
					GLU.gluTessVertex(tess, compArray, 0, compArray);
				}
				GLU.gluTessEndContour(tess);
			}
		}
		GLU.gluTessEndPolygon(tess);
	}

	/**
	 * Callback class for tessellating polygon interior.
	 *
	 */
	protected class TessellatorCallback extends GLUtessellatorCallbackAdapter {

		public TessellatorCallback() {
			// nothing here
		}

		@Override
		public void begin(int type) {
			// nothing here
		}

		@Override
		public void vertex(Object vertexData) {
			Vector3 pos = new Vector3(((double[]) vertexData)[0], ((double[]) vertexData)[1],
				((double[]) vertexData)[2]);
			// System.err.println("Tessellator.vertex "+pos);
			pos.addLocal(referencePoint);
			tessellatedPolygon.add(pos);
		}

		@Override
		public void end() {
			// nothing here
		}

		@Override
		public void combine(double[] coords, Object[] data, float[] weight, Object[] outData) {
			outData[0] = coords;
		}

		@Override
		public void edgeFlag(boolean boundaryEdge) {
			// nothing here
		}
	}

	/**
	 * Uses the first coordinate as a reference point that is subtracted from
	 * all others to reduce the size of the number. The polygon is also
	 * tessellated and the vertex buffer is set for rendering.
	 * 
	 * @param inPositions
	 *            new coordinates
	 * @param dc
	 *            DrawContext
	 */
	public FloatBuffer tessellate(ArrayList<ReadOnlyVector3> outerVertex, List<List<ReadOnlyVector3>> innerVertex) {
		if ((outerVertex == null) || (outerVertex.size() == 0)) {
			return (null);
		}
		ArrayList<ReadOnlyVector3> positions = new ArrayList<ReadOnlyVector3>();
		referencePoint = new Vector3(outerVertex.get(0));
		// System.err.println("Tessellator.tessellate "+referencePoint);
		for (ReadOnlyVector3 vert : outerVertex) {
			Vector3 pos = vert.subtract(referencePoint, null);
			positions.add(pos);
		}
		this.outerVertex = positions;

		if (innerVertex != null) {
			this.innerVertex = new ArrayList<List<ReadOnlyVector3>>();
			for (int i = 0; i < innerVertex.size(); ++i) {
				List<ReadOnlyVector3> innerVert = innerVertex.get(i);
				positions = new ArrayList<ReadOnlyVector3>();
				for (ReadOnlyVector3 vert : innerVert) {
					Vector3 pos = vert.subtract(referencePoint, null);
					positions.add(pos);
				}
				this.innerVertex.add(positions);
			}
		}

		tessellatedPolygon.clear();
		this.tessellateInterior(new TessellatorCallback());
		return (vertexBuffer);
	}
}
