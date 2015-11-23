package gov.nasa.arc.dert.landscape;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.ardor3d.renderer.IndexMode;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.IntBufferData;
import com.ardor3d.scenegraph.MeshData;

/**
 * Extension of Ardor3D MeshData class that allows setting all of the buffers in
 * the constructor.
 *
 */
public class TileMeshData extends MeshData {

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

}
