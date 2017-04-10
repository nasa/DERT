package gov.nasa.arc.dert.landscape;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

import com.ardor3d.image.Texture;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.util.TextureKey;
import com.ardor3d.util.TextureManager;

/**
 * A mesh that represents a QuadTree in the display.
 *
 */
public class QuadTreeMesh extends Mesh {

	// Mesh dimensions
	private int tileWidth, tileLength;
	private int tWidth, tLength;

	// Pixel dimensions
	private double pixelWidth, pixelLength;

	// This mesh is empty
	protected boolean empty;

	// Copy of edge vertices for stitching
	private float[][] edge;

	// Copy of edge normals for stitching
	private Vector3[][] nrml;

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param tileWidth
	 * @param tileLength
	 * @param pixelWidth
	 * @param pixelLength
	 */
	public QuadTreeMesh(String name, int tileWidth, int tileLength, double pixelWidth, double pixelLength) {
		super(name);
		this.tileWidth = tileWidth;
		this.tileLength = tileLength;
		this.pixelWidth = pixelWidth;
		this.pixelLength = pixelLength;
		tWidth = tileWidth + 1;
		tLength = tileLength + 1;
	}

	public int getTileWidth() {
		return (tileWidth);
	}

	public int getTileLength() {
		return (tileLength);
	}

	public double getPixelWidth() {
		return (pixelWidth);
	}

	public double getPixelLength() {
		return (pixelLength);
	}

	public boolean isEmpty() {
		return (empty);
	}

	/**
	 * Given a column and row in the mesh, return the vertex
	 * 
	 * @param c
	 * @param r
	 * @param vertex
	 * @return
	 */
	public Vector3 getVertex(int c, int r, Vector3 vertex) {
		FloatBuffer vertexBuffer = getMeshData().getVertexBuffer();
		if (empty) {
			double width = pixelWidth * tileWidth;
			double length = pixelLength * tileLength;
			return (new Vector3(-width / 2 + c * pixelWidth, length / 2 - r * pixelLength, vertexBuffer.get(2)));
		}
		int i = r * tWidth + c;
		if ((i * 3 + 2) >= vertexBuffer.limit()) {
			throw new IllegalArgumentException("Column = " + c + ", Row = " + r + ", Tile width = " + tWidth
				+ ", Index = " + (i * 3 + 2) + ", Limit = " + vertexBuffer.limit());
		}
		if (vertex == null) {
			vertex = new Vector3(vertexBuffer.get(i * 3), vertexBuffer.get(i * 3 + 1), vertexBuffer.get(i * 3 + 2));
		} else {
			vertex.set(vertexBuffer.get(i * 3), vertexBuffer.get(i * 3 + 1), vertexBuffer.get(i * 3 + 2));
		}
		return (vertex);
	}

	/**
	 * Get the elevation (Z coordinate) at a given column and row in the mesh
	 * 
	 * @param c
	 * @param r
	 * @return
	 */
	public float getElevation(int c, int r) {
		FloatBuffer vertexBuffer = getMeshData().getVertexBuffer();
		if (vertexBuffer == null)
			throw new IllegalStateException(getName()+" Column = " + c + ", Row = " + r
					+ ", vertex buffer is null");
		if (empty) {
			return (vertexBuffer.get(2));
		}
		int i = r * tWidth + c;
		// System.err.println("QuadTreeMesh.getElevation "+c+" "+r+" "+i+" "+tWidth+" "+vertexBuffer.limit());
		if ((i * 3 + 2) >= vertexBuffer.limit()) {
			throw new IllegalArgumentException(getName()+" Column = " + c + ", Row = " + r + ", Tile width = " + tWidth
				+ ", Index = " + (i * 3 + 2) + ", Limit = " + vertexBuffer.limit());
		}
		return (vertexBuffer.get(i * 3 + 2));
	}

	/**
	 * Get the elevation using nearest neighbor interpolation at a coordinate in
	 * the mesh.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public float getElevationNearestNeighbor(double x, double y) {
		int c = (int) Math.round(x / pixelWidth);
		c = Math.min(c, tileWidth);
		int r = (int) Math.round(y / pixelLength);
		r = Math.min(r, tileLength);
		r = tileLength - r;
		return (getElevation(c, r));
	}

	/**
	 * Get the elevation using bilinear interpolation at a coordinate in the
	 * mesh.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public float getElevationBilinear(double x, double y) {
		// binary interpolation
		// get 4 corner posts
		int c0 = (int) Math.floor(x / pixelWidth);
		int c1 = (int) Math.min(Math.ceil(x / pixelWidth), tileWidth);
		int r0 = (int) Math.floor(y / pixelLength);
		int r1 = (int) Math.min(Math.ceil(y / pixelLength), tileLength);
		// get 4 corner coordinates
		double x1 = c0;
		double x2 = c1;
		double y1 = r0;
		double y2 = r1;
		x /= pixelWidth;
		y /= pixelLength;
		// flip rows (this is like an image)
		r0 = tileLength - r0;
		r1 = tileLength - r1;
		// linear interpolation in X direction
		double xIn1 = 0;
		double xIn2 = 0;
		if (c0 == c1) {
			xIn1 = getElevation(c0, r1);
			xIn2 = getElevation(c0, r0);
		} else {
			xIn1 = (x2 - x) / (x2 - x1) * getElevation(c0, r0) + (x - x1) / (x2 - x1) * getElevation(c1, r0);
			xIn2 = (x2 - x) / (x2 - x1) * getElevation(c0, r1) + (x - x1) / (x2 - x1) * getElevation(c1, r1);
		}
		// linear interpolation in Y direction
		double elev = 0;
		if (r0 == r1) {
			elev = xIn1;
		} else {
			elev = (y2 - y) / (y2 - y1) * xIn1 + (y - y1) / (y2 - y1) * xIn2;
		}
		return ((float) elev);
	}

	/**
	 * Set the elevation at a given column and row in the mesh
	 * 
	 * @param c
	 * @param r
	 * @param el
	 */
	public final void setElevation(int c, int r, float el) {
		if (empty) {
			return;
		}
		FloatBuffer vertexBuffer = getMeshData().getVertexBuffer();
		int i = r * tWidth + c;
		if ((i * 3 + 2) >= vertexBuffer.limit()) {
			throw new IllegalArgumentException("Column = " + c + ", Row = " + r + ", Tile width = " + tWidth
				+ ", Index = " + (i * 3 + 2) + ", Limit = " + vertexBuffer.limit());
		}
		vertexBuffer.put(i * 3 + 2, el);
	}

	/**
	 * Get the normal at a given column and row in the mesh
	 * 
	 * @param c
	 * @param r
	 * @param store
	 * @return
	 */
	public boolean getNormal(int c, int r, Vector3 store) {
		if (empty) {
			store.set(0, 0, 0);
			return (false);
		}
		FloatBuffer normalBuffer = getMeshData().getNormalBuffer();
		int i = r * tWidth + c;
		if ((i * 3 + 2) >= normalBuffer.limit()) {
			throw new IllegalArgumentException("Column = " + c + ", Row = " + r + ", Tile width = " + tWidth
				+ ", Index = " + (i * 3 + 2) + ", Limit = " + normalBuffer.limit());
		}
		store.set(normalBuffer.get(i * 3), normalBuffer.get(i * 3 + 1), normalBuffer.get(i * 3 + 2));
		return (true);
	}

	/**
	 * Fill the vertex at the given column and row with normal vector.
	 * 
	 * @param c
	 *            column index
	 * @param r
	 *            row index
	 * @param el
	 *            elevation data
	 */
	public final void setNormal(int c, int r, Vector3 nrml) {
		if (empty) {
			return;
		}
		FloatBuffer normalBuffer = getMeshData().getNormalBuffer();
		int i = r * tWidth + c;
		if ((i * 3 + 2) >= normalBuffer.limit()) {
			throw new IllegalArgumentException("Column = " + c + ", Row = " + r + ", Tile width = " + tWidth
				+ ", Index = " + (i * 3 + 2) + ", Limit = " + normalBuffer.limit());
		}
		normalBuffer.put(i * 3, nrml.getXf());
		normalBuffer.put(i * 3 + 1, nrml.getYf());
		normalBuffer.put(i * 3 + 2, nrml.getZf());
	}

	/**
	 * Fill the given column with elevation data.
	 * 
	 * @param c
	 *            the column index
	 * @param el
	 *            the elevation data
	 */
	public final void setElevationColumn(int c, float[] el) {
		if (empty) {
			return;
		}
		FloatBuffer vertexBuffer = getMeshData().getVertexBuffer();
		for (int r = 0; r < tLength; ++r) {
			int i = r * tWidth + c;
			vertexBuffer.put(i * 3 + 2, el[r]);
		}
	}

	/**
	 * Fill the given column with elevation data.
	 * 
	 * @param c
	 *            the column index
	 * @param el
	 *            the elevation data
	 */
	public final void setNormalsColumn(int c, Vector3[] nl) {
		if (empty) {
			return;
		}
		FloatBuffer normalBuffer = getMeshData().getNormalBuffer();
		for (int r = 0; r < tLength; ++r) {
			int i = 3 * (r * tWidth + c);
			normalBuffer.put(i, nl[r].getXf());
			normalBuffer.put(i + 1, nl[r].getYf());
			normalBuffer.put(i + 2, nl[r].getZf());
		}
	}

	/**
	 * Fill the given row with elevation data.
	 * 
	 * @param r
	 *            the row index
	 * @param el
	 *            the elevation data
	 */
	public final void setElevationRow(int r, float[] el) {
		if (empty) {
			return;
		}
		FloatBuffer vertexBuffer = getMeshData().getVertexBuffer();
		for (int c = 0; c < tWidth; ++c) {
			int i = r * tWidth + c;
			vertexBuffer.put(i * 3 + 2, el[c]);
		}
	}

	/**
	 * Fill the given row with elevation data.
	 * 
	 * @param r
	 *            the row index
	 * @param el
	 *            the elevation data
	 */
	public final void setNormalsRow(int r, Vector3[] nl) {
		if (empty) {
			return;
		}
		FloatBuffer normalBuffer = getMeshData().getNormalBuffer();
		for (int c = 0; c < tWidth; ++c) {
			int i = 3 * (r * tWidth + c);
			normalBuffer.put(i, nl[c].getXf());
			normalBuffer.put(i + 1, nl[c].getYf());
			normalBuffer.put(i + 2, nl[c].getZf());
		}
	}

	/**
	 * Dispose of resources for this mesh
	 */
	public void dispose() {
		TextureState ts = (TextureState) getLocalRenderState(StateType.Texture);
		if (ts == null)
			return;
		int maxUnit = ts.getMaxTextureIndexUsed();
		for (int i = 0; i < maxUnit; ++i) {
			Texture texture = ts.getTexture(i);
			if (texture != null) {
				TextureKey tKey = texture.getTextureKey();
				if (tKey != null) {
					TextureManager.removeFromCache(tKey);
					// System.err.println("QuadTreeFactory.removeTexture "+tKey);
				}
				List<ByteBuffer> data = texture.getImage().getData();
				data.clear();
			}
		}
		TileMeshData tmd = (TileMeshData)getMeshData();
		tmd.dispose();
	}

	/**
	 * Enable the layers on this mesh.
	 * 
	 * @param enable
	 */
	public void enableLayers(boolean enable) {
		TextureState textureState = (TextureState) getLocalRenderState(RenderState.StateType.Texture);
		textureState.setEnabled(enable);
		markDirty(DirtyType.RenderState);
	}

	/**
	 * Set the surface color for this mesh
	 * 
	 * @param rgba
	 */
	public void updateSurfaceColor(float[] rgba) {
		FloatBuffer colors = getMeshData().getColorBuffer();
		int dataSize = colors.limit();
		for (int i = 0; i < dataSize; i += 4) {
			if (colors.get(i + 3) != 0) {
				colors.put(i, rgba[0]);
				colors.put(i + 1, rgba[1]);
				colors.put(i + 2, rgba[2]);
				colors.put(i + 3, rgba[3]);
			}
		}
		getMeshData().setColorBuffer(colors);
		markDirty(DirtyType.RenderState);
	}
	
	public void cacheEdges() {
		edge = new float[4][];
		// cache edge elevations
		// left
		edge[0] = new float[tLength];
		for (int i = 0; i < tLength; ++i) {
			edge[0][i] = getElevation(0, i);
		}
		// top
		edge[1] = new float[tWidth];
		for (int i = 0; i < tWidth; ++i) {
			edge[1][i] = getElevation(i, 0);
		}
		// right
		edge[2] = new float[tLength];
		for (int i = 0; i < tLength; ++i) {
			edge[2][i] = getElevation(tileWidth, i);
		}
		// bottom
		edge[3] = new float[tWidth];
		for (int i = 0; i < tWidth; ++i) {
			edge[3][i] = getElevation(i, tileLength);
		}

		// cache edge normals
		FloatBuffer normalBuffer = getMeshData().getNormalBuffer();
		nrml = new Vector3[2][];
		// left
		nrml[0] = new Vector3[tLength];
		for (int i = 0; i < tLength; ++i) {
			int ii = i * tWidth * 3;
			nrml[0][i] = new Vector3(normalBuffer.get(ii), normalBuffer.get(ii + 1), normalBuffer.get(ii + 2));
		}
		// top
		nrml[1] = new Vector3[tWidth];
		for (int i = 0; i < tWidth; ++i) {
			int ii = i * 3;
			nrml[1][i] = new Vector3(normalBuffer.get(ii), normalBuffer.get(ii + 1), normalBuffer.get(ii + 2));
		}

	}
	
	public final float[] getEdge(int index) {
		return(edge[index]);
	}
	
	public final Vector3[] getNrml(int index) {
		return(nrml[index]);
	}

}
