package gov.nasa.arc.dert.landscape.quadtree;

import gov.nasa.arc.dert.util.MathUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.IntBufferData;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Extension of Ardor3D MeshData class that allows setting all of the buffers in
 * the constructor.
 *
 */
public class TileMeshData extends MeshData {
	
	/**
	 * Constructor to create empty mesh.
	 * @param vertices
	 * @param tileWidth
	 * @param tileLength
	 * @param missingColor
	 */
	public TileMeshData(FloatBuffer vertices, int columns, int rows, float missingFillValue, float[] missingColor) {
		super();
		int tileWidth = columns-1;
		int tileLength = rows-1;
		// vertices
		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(4 * 3);
		vertexBuffer.put(vertices.get(0)).put(vertices.get(1)).put(missingFillValue);
		int i = tileLength * columns * 3;
		vertexBuffer.put(vertices.get(i)).put(vertices.get(i + 1)).put(missingFillValue);
		i += tileWidth * 3;
		vertexBuffer.put(vertices.get(i)).put(vertices.get(i + 1)).put(missingFillValue);
		i = tileWidth * 3;
		vertexBuffer.put(vertices.get(i)).put(vertices.get(i + 1)).put(missingFillValue);
		vertexBuffer.flip();
		_vertexCoords = new FloatBufferData(vertexBuffer, 3);
		_vertexCount = _vertexCoords.getTupleCount();

		// normals
		FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(4 * 3);
		for (int j = 0; j < 4; ++j) {
			normalBuffer.put(0).put(0).put(1);
		}
		normalBuffer.flip();
		_normalCoords = new FloatBufferData(normalBuffer, 3);

		// colors
		FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(4 * 4);
		for (int j = 0; j < 4; ++j) {
			colorBuffer.put(missingColor[0]).put(missingColor[1]).put(missingColor[2]).put(missingColor[3]);
		}
		colorBuffer.flip();
		_colorCoords = new FloatBufferData(colorBuffer, 4);

		// indices
		IntBuffer indexBuffer = BufferUtils.createIntBuffer(4);
		indexBuffer.put(0).put(1).put(3).put(2);
		int[] indexLengths = { 4 };
		_indexModes[0] = IndexMode.TriangleStrip;
		_indexBuffer = new IntBufferData(indexBuffer);
		_indexLengths = indexLengths;

		// texture coordinates
		FloatBuffer texCoordBuffer = BufferUtils.createFloatBuffer(4 * 2);
		texCoordBuffer.put(0).put(1);
		texCoordBuffer.put(0).put(0);
		texCoordBuffer.put(1).put(0);
		texCoordBuffer.put(1).put(1);
		texCoordBuffer.flip();
		FloatBufferData tcb = new FloatBufferData(texCoordBuffer, 2);
		_textureCoords.add(0, tcb);
				
		updatePrimitiveCounts();
		refreshInterleaved();
	}
	
	/**
	 * Constructor for mesh from vertex buffer.
	 * @param vertexBuffer
	 * @param pixelScale
	 * @param tileWidth
	 * @param tileLength
	 * @param missingFillValue
	 * @param rgba
	 */
	public TileMeshData(FloatBuffer vertexBuffer, double pixelScale, int columns, int rows, float missingFillValue, float[] surfaceColor, float[] missingColor) {
		super();
		
		int tileLength = rows-1;
		int dataSize = columns * rows;

		// create color buffer
		FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(dataSize * 4);

		int k = 2;
		for (int r = 0; r < rows; ++r) {
			for (int c = 0; c < columns; ++c) {
				float z = vertexBuffer.get(k);
				// fill missing value vertices with missing value color
				if (Float.isNaN(z)) {
					vertexBuffer.put(k, (float) (missingFillValue * pixelScale));
					colorBuffer.put(missingColor[0]).put(missingColor[1]).put(missingColor[2]).put(missingColor[3]);
				} else {
					colorBuffer.put(surfaceColor[0]).put(surfaceColor[1]).put(surfaceColor[2]).put(surfaceColor[3]);
				}
				k += 3;
			}
		}
		vertexBuffer.rewind();
		colorBuffer.flip();

		// get normals
		FloatBuffer normalBuffer = createNormals(vertexBuffer, columns, rows, dataSize);

		FloatBuffer texCoordBuffer = getTexCoords(columns, rows);
		FloatBufferData tcb = new FloatBufferData(texCoordBuffer, 2);
		
		// vertex indices
		int[] indexLengths = new int[tileLength];
		IntBuffer indexBuffer = BufferUtils.createIntBuffer(tileLength * columns * 2);
		int i = 0;
		// System.err.println("RasterTile.createIndexes "+tSize+" "+tSize1+" "+newLevel+" "+vertices.limit());
		for (int r = 0; r < tileLength; ++r) {
			for (int c = 0; c < columns; ++c) {
				indexBuffer.put(i);
				indexBuffer.put(i + columns);
				i++;
			}
			indexLengths[r] = columns * 2;
		}
		indexBuffer.flip();
		
		_vertexCoords = new FloatBufferData(vertexBuffer, 3);
		_vertexCount = _vertexCoords.getTupleCount();
		_normalCoords = new FloatBufferData(normalBuffer, 3);
		_colorCoords = new FloatBufferData(colorBuffer, 4);
		_textureCoords.add(0, tcb);
		_indexModes[0] = IndexMode.TriangleStrip;
		_indexBuffer = new IntBufferData(indexBuffer);
		_indexLengths = indexLengths;
		
		updatePrimitiveCounts();
		refreshInterleaved();
	}

	/**
	 * Constructor
	 * 
	 * @param vertexBuffer
	 * @param texCoordBuffer
	 * @param colorBuffer
	 * @param indices
	 * @param indexLengths
	 * @param normalBuffer
	 * @param indexMode
	 */
	public TileMeshData(FloatBuffer vertexBuffer, FloatBuffer texCoordBuffer, FloatBuffer colorBuffer,
		IntBuffer indices, int[] indexLengths, FloatBuffer normalBuffer, IndexMode indexMode) {
		super();
		_vertexCoords = new FloatBufferData(vertexBuffer, 3);
		_vertexCount = _vertexCoords.getTupleCount();
		_colorCoords = new FloatBufferData(colorBuffer, 4);
		if (texCoordBuffer != null) {
			FloatBufferData tcb = new FloatBufferData(texCoordBuffer, 2);
			_textureCoords.add(0, tcb);
		}
		if (normalBuffer == null) {
			_normalCoords = null;
		} else {
			_normalCoords = new FloatBufferData(normalBuffer, 3);
		}
		_indexModes[0] = indexMode;
		_indexBuffer = new IntBufferData(indices);
		_indexLengths = indexLengths;
		updatePrimitiveCounts();
		refreshInterleaved();
	}

	/**
	 * Update primitive counts.
	 */
	private void updatePrimitiveCounts() {
		final int maxIndex = _indexBuffer != null ? _indexBuffer.getBufferLimit() : _vertexCount;
		final int maxSection = _indexLengths != null ? _indexLengths.length : 1;
		if (_primitiveCounts.length != maxSection) {
			_primitiveCounts = new int[maxSection];
		}
		for (int i = 0; i < maxSection; i++) {
			final int size = _indexLengths != null ? _indexLengths[i] : maxIndex;
			final int count = IndexMode.getPrimitiveCount(getIndexMode(i), size);
			_primitiveCounts[i] = Math.max(0, count);
		}

	}

	private void refreshInterleaved() {
		if (_interleaved != null) {
			_interleaved.setNeedsRefresh(true);
		}
	}
	
	public void dispose() {
		if (_vertexCoords != null)
			_vertexCoords.setBuffer(null);
		if (_normalCoords != null)
			_normalCoords.setBuffer(null);
		if (_colorCoords != null)
			_colorCoords.setBuffer(null);
		if (_indexBuffer != null)
			_indexBuffer.setBuffer(null);
		if (_textureCoords != null)
			for (int i=0; i<_textureCoords.size(); ++i)
				_textureCoords.get(i).setBuffer(null);
	}	

	private FloatBuffer createNormals(FloatBuffer vertex, int cols, int rows, int dataSize) {
		// compute normal for each face
		float[] nrml = new float[dataSize * 3];
		byte[] cnt = new byte[dataSize];
		Vector3 norm = new Vector3();
		Vector3 v0 = new Vector3();
		Vector3 v1 = new Vector3();
		Vector3 v2 = new Vector3();
		Vector3 work = new Vector3();
		int i = 0;
		int n = cols * 3;
		int k = 0;
		for (int r = 0; r < rows - 1; ++r) {
			for (int c = 0; c < cols - 1; ++c) {
				k = r * cols + c;
				i = k * 3;
				v0.set(vertex.get(i), vertex.get(i + 1), vertex.get(i + 2));
				v1.set(vertex.get(i + n), vertex.get(i + n + 1), vertex.get(i + n + 2));
				v2.set(vertex.get(i + 3), vertex.get(i + 4), vertex.get(i + 5));
				MathUtil.createNormal(norm, v0, v1, v2, work);
				addFace(k, nrml, norm, cnt);
			}
			for (int c = 1; c < cols; ++c) {
				k = r * cols + c;
				i = k * 3;
				v0.set(vertex.get(i-3), vertex.get(i-2), vertex.get(i-1));
				v1.set(vertex.get(i+n), vertex.get(i+n+1), vertex.get(i+n+2));
				v2.set(vertex.get(i), vertex.get(i + 1), vertex.get(i + 2));
				MathUtil.createNormal(norm, v0, v1, v2, work);
				addFace(k, nrml, norm, cnt);
			}
		}
		for (int r = 1; r < rows; ++r) {
			for (int c = 0; c < cols - 1; ++c) {
				k = r * cols + c;
				i = k * 3;
				v0.set(vertex.get(i - n), vertex.get(i - n + 1), vertex.get(i - n + 2));
				v1.set(vertex.get(i), vertex.get(i + 1), vertex.get(i + 2));
				v2.set(vertex.get(i + 3), vertex.get(i + 4), vertex.get(i + 5));
				MathUtil.createNormal(norm, v0, v1, v2, work);
				addFace(k, nrml, norm, cnt);
			}
			for (int c = 1; c < cols; ++c) {
				k = r * cols + c;
				i = k * 3;
				v0.set(vertex.get(i), vertex.get(i + 1), vertex.get(i + 2));
				v1.set(vertex.get(i-n), vertex.get(i-n+1), vertex.get(i-n+2));
				v2.set(vertex.get(i-3), vertex.get(i-2), vertex.get(i-1));
				MathUtil.createNormal(norm, v0, v1, v2, work);
				addFace(k, nrml, norm, cnt);
			}
		}
		// normal for vertex is average of surrounding faces
		for (int r = 0; r < rows; ++r) {
			for (int c = 0; c < cols; ++c) {
				k = r * cols + c;
				i = k * 3;
				for (int j = 0; j < 3; ++j) {
					nrml[i + j] /= cnt[k];
				}
			}
		}
		FloatBuffer normal = BufferUtils.createFloatBuffer(nrml);
		return (normal);
	}

	private void addFace(int index, float[] nrml, Vector3 norm, byte[] cnt) {
		int i = index * 3;
		nrml[i] += norm.getXf();
		nrml[i + 1] += norm.getYf();
		nrml[i + 2] += norm.getZf();
		cnt[index]++;
	}


	private FloatBuffer getTexCoords(int columns, int rows) {
		int tileWidth = columns-1;
		int tileLength = rows-1;
		int size = columns * rows * 2;
		FloatBuffer texCoords = BufferUtils.createFloatBuffer(size);
		int i = 0;
		for (int r = 0; r < rows; ++r) {
			for (int c = 0; c < columns; ++c) {
				texCoords.put(i * 2, ((float) c) / tileWidth);
				texCoords.put(i * 2 + 1, ((float) r) / tileLength);
				i++;
			}
		}
		texCoords.limit(size);
		texCoords.rewind();
		return (texCoords);
	}

}
