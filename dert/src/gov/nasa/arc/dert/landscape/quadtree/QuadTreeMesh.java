package gov.nasa.arc.dert.landscape.quadtree;

import gov.nasa.arc.dert.landscape.quadtree.QuadTree.Side;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

import com.ardor3d.image.Texture;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
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
	private int columns, rows;

	// Pixel dimensions
	private double pixelWidth, pixelLength;

	// This mesh is empty
	protected boolean empty;

	// Copy of edge vertices for stitching
	private float[][] edge;

	// Copy of edge normals for stitching
	private Vector3[][] nrml;

	// Fields used for interpolation of edge normals.
	private Vector3 n0, n1, n2;

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param tileWidth
	 * @param tileLength
	 * @param pixelWidth
	 * @param pixelLength
	 */
	public QuadTreeMesh(String name, int columns, int rows, double pixelWidth, double pixelLength) {
		super(name);
		this.columns = columns;
		this.rows = rows;
		this.tileWidth = columns-1;
		this.tileLength = rows-1;
		this.pixelWidth = pixelWidth;
		this.pixelLength = pixelLength;
		n0 = new Vector3();
		n1 = new Vector3();
		n2 = new Vector3();
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
		int i = r * columns + c;
		if ((i * 3 + 2) >= vertexBuffer.limit()) {
			throw new IllegalArgumentException("Column = " + c + ", Row = " + r + ", Num Columns = " + columns
				+ ", Num Rows = "+rows+", Index = " + (i * 3 + 2) + ", Limit = " + vertexBuffer.limit());
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
		int i = r * columns + c;
		// System.err.println("QuadTreeMesh.getElevation "+c+" "+r+" "+i+" "+tWidth+" "+vertexBuffer.limit());
		if ((i * 3 + 2) >= vertexBuffer.limit()) {
			throw new IllegalArgumentException(getName()+" Column = " + c + ", Row = " + r + ", Num Columns = " + columns
				+", Num Rows = "+rows+ ", Index = " + (i * 3 + 2) + ", Limit = " + vertexBuffer.limit());
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
		int i = r * columns + c;
		if ((i * 3 + 2) >= vertexBuffer.limit()) {
			throw new IllegalArgumentException("Column = " + c + ", Row = " + r + ", Num Columns = " + columns
				+ ", Num Rows = "+rows+", Index = " + (i * 3 + 2) + ", Limit = " + vertexBuffer.limit());
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
		int i = r * columns + c;
		if ((i * 3 + 2) >= normalBuffer.limit()) {
			throw new IllegalArgumentException("Column = " + c + ", Row = " + r + ", Num Columns = " + columns
				+ ", Num Rows = "+rows+", Index = " + (i * 3 + 2) + ", Limit = " + normalBuffer.limit());
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
		int i = r * columns + c;
		if ((i * 3 + 2) >= normalBuffer.limit()) {
			throw new IllegalArgumentException("Column = " + c + ", Row = " + r + ", Num Columns = " + columns
				+ ", Num Rows = "+rows+", Index = " + (i * 3 + 2) + ", Limit = " + normalBuffer.limit());
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
		for (int r = 0; r < rows; ++r) {
			int i = r * columns + c;
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
		for (int r = 0; r < rows; ++r) {
			int i = 3 * (r * columns + c);
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
		for (int c = 0; c < columns; ++c) {
			int i = r * columns + c;
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
		for (int c = 0; c < columns; ++c) {
			int i = 3 * (r * columns + c);
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
	
	private void cacheEdges() {
		edge = new float[4][];
		// cache edge elevations
		// left
		edge[0] = new float[rows];
		for (int i = 0; i < rows; ++i) {
			edge[0][i] = getElevation(0, i);
		}
		// right
		edge[1] = new float[rows];
		for (int i = 0; i < rows; ++i) {
			edge[1][i] = getElevation(tileWidth, i);
		}
		// bottom
		edge[2] = new float[columns];
		for (int i = 0; i < columns; ++i) {
			edge[2][i] = getElevation(i, tileLength);
		}
		// top
		edge[3] = new float[columns];
		for (int i = 0; i < columns; ++i) {
			edge[3][i] = getElevation(i, 0);
		}

		// cache edge normals
		FloatBuffer normalBuffer = getMeshData().getNormalBuffer();
		nrml = new Vector3[4][];
		// left
		nrml[0] = new Vector3[rows];
		for (int i = 0; i < rows; ++i) {
			int ii = i * columns * 3;
			nrml[0][i] = new Vector3(normalBuffer.get(ii), normalBuffer.get(ii + 1), normalBuffer.get(ii + 2));
		}
		// right
		nrml[1] = new Vector3[rows];
		int w = columns-1;
		for (int i = 0; i < rows; ++i) {
			int ii = (i*columns+w) * 3;
			nrml[1][i] = new Vector3(normalBuffer.get(ii), normalBuffer.get(ii + 1), normalBuffer.get(ii + 2));
		}
		//bottom
		nrml[2] = new Vector3[columns];
		w = columns*(rows-1);
		for (int i = 0; i < columns; ++i) {
			int ii = (w+i) * 3;
			nrml[2][i] = new Vector3(normalBuffer.get(ii), normalBuffer.get(ii + 1), normalBuffer.get(ii + 2));
		}
		// top
		nrml[3] = new Vector3[columns];
		for (int i = 0; i < columns; ++i) {
			int ii = i * 3;
			nrml[3][i] = new Vector3(normalBuffer.get(ii), normalBuffer.get(ii + 1), normalBuffer.get(ii + 2));
		}
	}
	
	public synchronized final float[] getEdge(Side side) {
		return(edge[side.ordinal()]);
	}
	
	public synchronized final Vector3[] getNrml(Side side) {
		return(nrml[side.ordinal()]);
	}

    /**
     * Sets the mesh data object for this mesh.
     * 
     * @param meshData
     *            the mesh data object
     */
	@Override
    public synchronized void setMeshData(final MeshData meshData) {
		super.setMeshData(meshData);
		if (!empty)
			cacheEdges();
    }

	public void fillEdge(Side side, int[] e, QuadTreeMesh that) {
		int ib = e[0];
		int ie = e[1] - ib;
		if (ie <= 0) {
//			System.err.println("QuadTreeMesh.fillEdge fill count <= 0 between " + getName()+" "+that.getName());
			return;
		}
		int nw = tileWidth / ie;
		int nh = tileLength / ie;
		int i = ib;
		double ww = 1.0 / nw;
		double wh = 1.0 / nh;
		float[] adjacentEdge;
		Vector3[] adjacentNormal;
		switch (side) {
		case Left:
			// get other tile edge data
			adjacentEdge = that.getEdge(Side.Right);
			adjacentNormal = that.getNrml(Side.Right);
			
			for (int j = 0; j < rows; j += nh) {
				// fill every nth pixel of this tile with vertex from the other tile
				setElevation(0, j, adjacentEdge[i]);
				that.setElevation(tileWidth, i, adjacentEdge[i]);
				// fill every nth pixel of this tile with normal from the other tile
				setNormal(0, j, adjacentNormal[i]);
				that.setNormal(tileWidth, i, adjacentNormal[i]);
				i++;
			}
			// interpolate new values in this tile
			for (int j = 0; j < tileLength; j += nh) {
				double el0 = getElevation(0, j);
				getNormal(0, j, n0);
				double el1 = getElevation(0, j+nh);
				getNormal(0, j + nh, n1);
				for (int k = 1; k < nh; ++k) {
					double el = el0 + k * wh * (el1-el0);
					interpolateNormal(n0, n1, n2, k * wh);
					setElevation(0, j + k, (float)el);
					setNormal(0, j + k, n2);
				}
			}
			break;
		case Right:
			// get original data from other tile
			adjacentEdge = that.getEdge(Side.Left);
			adjacentNormal = that.getNrml(Side.Left);
			
			for (int j = 0; j < rows; j += nh) {
				// fill every nth pixel of this tile with vertex from the other tile
				setElevation(tileWidth, j, adjacentEdge[i]);
				that.setElevation(0, i, adjacentEdge[i]);
				// fill every nth pixel of this tile with normal from other tile
				setNormal(tileWidth, j, adjacentNormal[i]);
				that.setNormal(0, i, adjacentNormal[i]);
				i++;
			}
			for (int j = 0; j < tileLength; j += nh) {
				double el0 = getElevation(tileWidth, j);
				getNormal(tileWidth, j, n0);
				double el1 = getElevation(tileWidth, j + nh) - el0;
				getNormal(tileWidth, j + nh, n1);
				for (int k = 1; k < nh; ++k) {
					double el = el0 + k * wh * el1;
					interpolateNormal(n0, n1, n2, k * wh);
					setElevation(tileWidth, j + k, (float)el);
					setNormal(tileWidth, j + k, n2);
				}
			}
			break;
		case Bottom:
			adjacentEdge = that.getEdge(Side.Top);
			adjacentNormal = that.getNrml(Side.Top);
			for (int j = 0; j < columns; j += nw) {
				// fill every nth pixel of this tile with vertex from the other tile
				setElevation(j, tileLength, adjacentEdge[i]);
				that.setElevation(i, 0, adjacentEdge[i]);
				// fill every nth pixel of this tile with normal from other tile
				setNormal(j, tileLength, adjacentNormal[i]);
				that.setNormal(i, 0, adjacentNormal[i]);
				i++;
			}
			for (int j = 0; j < tileWidth; j += nw) {
				double el0 = getElevation(j, tileLength);
				getNormal(j, tileLength, n0);
				double el1 = getElevation(j + nw, tileLength) - el0;
				getNormal(j + nw, tileLength, n1);
				for (int k = 1; k < nw; ++k) {
					double el = el0 + k * ww * el1;
					interpolateNormal(n0, n1, n2, k * ww);
					setElevation(j + k, tileLength, (float)el);
					setNormal(j + k, tileLength, n2);
				}
			}
			break;
		case Top:
			adjacentEdge = that.getEdge(Side.Bottom);
			adjacentNormal = that.getNrml(Side.Bottom);
			
			for (int j = 0; j < columns; j += nw) {
				// fill every nth pixel of this tile with vertex from the other tile
				setElevation(j, 0, adjacentEdge[i]);
				that.setElevation(i, tileLength, adjacentEdge[i]);
				// fill every nth pixel of this tile with normal from other tile
				setNormal(j, 0, adjacentNormal[i]);
				that.setNormal(i, tileLength, adjacentNormal[i]);
				i++;
			}
			for (int j = 0; j < tileWidth; j += nw) {
				double el0 = getElevation(j, 0);
				getNormal(j, 0, n0);
				double el1 = getElevation(j + nw, 0) - el0;
				getNormal(j + nw, 0, n1);
				for (int k = 1; k < nw; ++k) {
					double el = el0 + k * ww * el1;
					interpolateNormal(n0, n1, n2, k * ww);
					setElevation(j + k, 0, (float)el);
					setNormal(j + k, 0, n2);
				}
			}
			break;
		}
	}

	private void interpolateNormal(Vector3 n0, Vector3 n1, Vector3 result, double weight) {
		result.set(n0);
		result.subtractLocal(n1);
		result.multiplyLocal(weight);
		result.addLocal(n1);
	}
	
//	@Override
//	public void render(final Renderer renderer, final MeshData meshData) {
//		System.err.println("QuadTreeMesh.render "+getName());
//		super.render(renderer, meshData);
//	}

}
