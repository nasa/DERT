package gov.nasa.arc.dert.scenegraph;

import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Provides a horizontal Cylinder with closed ends defined by length and radius.
 * The center is the origin. Adapted from the Ardor3D Cylinder.
 *
 */
public class Rod
	extends Mesh {

    private int _axisSamples;

    private int _radialSamples;

    private double _radius;

    private double _length;

    public Rod() {}

    /**
     * Creates a new Rod. Center is the origin. Usually, a higher sample number creates a better
     * looking rod, but at the cost of more vertex information.
     * 
     * @param name
     *            The name of this Rod.
     * @param axisSamples
     *            Number of triangle samples along the axis.
     * @param radialSamples
     *            Number of triangle samples along the radial.
     * @param radius
     *            The radius of the cylinder.
     * @param length
     *            The rod's length.
     */
    public Rod(final String name, final int axisSamples, final int radialSamples, final double radius,
            final double length) {

        super(name);

        _axisSamples = axisSamples;
        _radialSamples = radialSamples;
        _length = length;
        _radius = radius;
        allocateVertices();
    }

    /**
     * @return Returns the length.
     */
    public double getLength() {
        return _length;
    }

    /**
     * @param length
     *            The length to set.
     */
    public void setLength(final double length) {
        _length = length;
        allocateVertices();
    }

    /**
     * @return Returns the radius.
     */
    public double getRadius() {
        return _radius;
    }

    /**
     * Change the radius of this cylinder.
     * 
     * @param radius
     *            The radius to set.
     */
    public void setRadius(final double radius) {
        _radius = radius;
        allocateVertices();
    }

    /**
     * @return the number of samples along the cylinder axis
     */
    public int getAxisSamples() {
        return _axisSamples;
    }

    /**
     * @return number of samples around cylinder
     */
    public int getRadialSamples() {
        return _radialSamples;
    }

    private void allocateVertices() {
        // allocate vertices
        final int verts = (_axisSamples+1) * (_radialSamples + 1) + 2;
        _meshData.setVertexBuffer(BufferUtils.createVector3Buffer(_meshData.getVertexBuffer(), verts));

        // allocate normals if requested
        _meshData.setNormalBuffer(BufferUtils.createVector3Buffer(_meshData.getNormalBuffer(), verts));

        // allocate texture coordinates
        _meshData.setTextureBuffer(BufferUtils.createVector2Buffer(verts), 0);

//        final int count = (2 + 2 * _axisSamples) * _radialSamples;
        final int count = (_axisSamples+1) * _radialSamples*6;

        if (_meshData.getIndices() == null || _meshData.getIndices().getBufferLimit() != 3 * count) {
            _meshData.setIndices(BufferUtils.createIndexBufferData(3 * count, verts - 1));
        }

        setGeometryData();
        setIndexData();
    }

    private void setGeometryData() {
        // generate geometry
        final double inverseRadial = 1.0 / _radialSamples;
        final double inverseAxis = 1.0 / _axisSamples;
        final double halfLength = 0.5 * _length;

        // Generate points on the unit circle to be used in computing the mesh
        // points on a rod slice.
        final double[] sin = new double[_radialSamples + 1];
        final double[] cos = new double[_radialSamples + 1];

        for (int radialCount = 0; radialCount < _radialSamples; radialCount++) {
            final double angle = MathUtils.TWO_PI * inverseRadial * radialCount;
            cos[radialCount] = MathUtils.cos(angle);
            sin[radialCount] = MathUtils.sin(angle);
        }
        sin[_radialSamples] = sin[0];
        cos[_radialSamples] = cos[0];

        // generate the rod itself
        final Vector3 tempNormal = new Vector3();
        int i = 0;
        for (int axisCount = 0; axisCount < (_axisSamples+1); axisCount++) {
            double axisFraction = axisCount * inverseAxis;
            double y = -halfLength + _length * axisFraction;

            // compute center of slice
            final Vector3 sliceCenter = new Vector3(0, y, 0);

            // compute slice vertices with duplication at end point
            final int save = i;
            for (int radialCount = 0; radialCount < _radialSamples; radialCount++) {
                final double radialFraction = radialCount * inverseRadial; // in [0,1)
                tempNormal.set(cos[radialCount], 0, sin[radialCount]);
                _meshData.getNormalBuffer().put(tempNormal.getXf()).put(tempNormal.getYf()).put(tempNormal.getZf());

                tempNormal.multiplyLocal(_radius).addLocal(sliceCenter);
                _meshData.getVertexBuffer().put(tempNormal.getXf()).put(tempNormal.getYf()).put(tempNormal.getZf());

                _meshData.getTextureCoords(0).getBuffer().put((float) axisFraction).put((float) radialFraction);
                i++;
            }

            BufferUtils.copyInternalVector3(_meshData.getVertexBuffer(), save, i);
            BufferUtils.copyInternalVector3(_meshData.getNormalBuffer(), save, i);

            _meshData.getTextureCoords(0).getBuffer().put((float) axisFraction).put(1.0f);

            i++;
        }

        _meshData.getVertexBuffer().put(0).put((float) -halfLength).put(0); // bottom center
        _meshData.getNormalBuffer().put(0).put(-1).put(0);
        _meshData.getTextureCoords(0).getBuffer().put(-(float)inverseAxis).put(0.5f);
        _meshData.getVertexBuffer().put(0).put((float) halfLength).put(0); // top center
        _meshData.getNormalBuffer().put(0).put(1).put(0);
        _meshData.getTextureCoords(0).getBuffer().put(1+(float)inverseAxis).put(0.5f);
        
        _meshData.getVertexBuffer().rewind();
        _meshData.getNormalBuffer().rewind();
        _meshData.getTextureBuffer(0).rewind();
    }

    private void setIndexData() {
        _meshData.getIndices().rewind();

        // generate connectivity
        
        int k = 0;
        for (int i = 0; i < _radialSamples; i++) {
            _meshData.getIndices().put(k ++);
            _meshData.getIndices().put(_meshData.getVertexCount() - 2);
            _meshData.getIndices().put(k);
        }
        k = 0;
        int l = _radialSamples+1;
        for (int j = 0; j<_axisSamples; j++) {
            for (int i = 0; i < (_radialSamples+1); i++) {
                _meshData.getIndices().put(k ++);
                _meshData.getIndices().put(k);
                _meshData.getIndices().put(l ++);
                _meshData.getIndices().put(k);
                _meshData.getIndices().put(l);
                _meshData.getIndices().put(l-1);
            }
        }
        k = _axisSamples*(_radialSamples+1);
        for (int i = 0; i < _radialSamples; i++) {
            _meshData.getIndices().put(k ++);
            _meshData.getIndices().put(k);
            _meshData.getIndices().put(_meshData.getVertexCount() - 1);
        }
    }

}
