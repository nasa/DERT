package gov.nasa.arc.dert.scenegraph;

import java.io.IOException;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Ellipsoid represents a 3D ellipsoid object. Adapted from Ardor3D Sphere.
 */
public class Ellipsoid extends Mesh {

    protected int _zSamples;

    protected int _radialSamples;
    
    protected double _theta;

    /** the distance from the center point each point falls on */
    public double _radiusA, _radiusB, _radiusC;
    /** the center of the sphere */
    public final Vector3 _center = new Vector3();

    public Ellipsoid() {}

    /**
     * Constructs a sphere. By default the Sphere has not geometry data or center.
     * 
     * @param name
     *            The name of the sphere.
     */
    public Ellipsoid(final String name) {
        super(name);
    }

    /**
     * Constructs a sphere with center at the origin. For details, see the other constructor.
     * 
     * @param name
     *            Name of sphere.
     * @param zSamples
     *            The samples along the Z.
     * @param radialSamples
     *            The samples along the radial.
     * @param radius
     *            Radius of the sphere.
     * @see #Sphere(java.lang.String, com.ardor3d.math.Vector3, int, int, double)
     */
    public Ellipsoid(final String name, final double theta, final double radiusA, final double radiusB, final double radiusC) {
        this(name, new Vector3(0, 0, 0), theta, radiusA, radiusB, radiusC);
    }

    /**
     * Constructs a sphere. All geometry data buffers are updated automatically. Both zSamples and radialSamples
     * increase the quality of the generated sphere.
     * 
     * @param name
     *            Name of the sphere.
     * @param center
     *            Center of the sphere.
     * @param zSamples
     *            The number of samples along the Z.
     * @param radialSamples
     *            The number of samples along the radial.
     * @param radius
     *            The radius of the sphere.
     */
    public Ellipsoid(final String name, final ReadOnlyVector3 center, final double theta,
            final double radiusA, final double radiusB, final double radiusC) {
        super(name);
        setData(center, theta, radiusA, radiusB, radiusC);
    }

    /**
     * Changes the information of the sphere into the given values.
     * 
     * @param center
     *            The new center of the sphere.
     * @param zSamples
     *            The new number of zSamples of the sphere.
     * @param radialSamples
     *            The new number of radial samples of the sphere.
     * @param radius
     *            The new radius of the sphere.
     */
    public void setData(final ReadOnlyVector3 center, final double theta, final double radiusA, final double radiusB, final double radiusC) {
        _center.set(center);
        _zSamples = (int)(180/theta+0.5)+1;
        _radialSamples = (int)(360/theta+0.5)+1;
        _radiusA = radiusA;
        _radiusB = radiusB;
        _radiusC = radiusC;
        _theta = theta;

        setGeometryData();
        setIndexData();
    }

    /**
     * builds the vertices based on the radius, center and radial and zSamples.
     */
    private void setGeometryData() {
        // allocate vertices
        final int verts = (_zSamples - 2) * _radialSamples + 2;
        final FloatBufferData vertsData = _meshData.getVertexCoords();
        if (vertsData == null) {
            _meshData.setVertexBuffer(BufferUtils.createVector3Buffer(verts));
        } else {
            vertsData.setBuffer(BufferUtils.createVector3Buffer(vertsData.getBuffer(), verts));
        }

        // allocate normals if requested
        final FloatBufferData normsData = _meshData.getNormalCoords();
        if (normsData == null) {
            _meshData.setNormalBuffer(BufferUtils.createVector3Buffer(verts));
        } else {
            normsData.setBuffer(BufferUtils.createVector3Buffer(normsData.getBuffer(), verts));
        }

        // allocate texture coordinates
        final FloatBufferData texData = _meshData.getTextureCoords(0);
        if (texData == null) {
            _meshData.setTextureBuffer(BufferUtils.createVector2Buffer(verts), 0);
        } else {
            texData.setBuffer(BufferUtils.createVector2Buffer(texData.getBuffer(), verts));
        }
        
        final double _thetaRad = Math.toRadians(_theta);

        // generate geometry

        // generate the ellipsoid itself
        final Vector3 tempVa = Vector3.fetchTempInstance();
        for (int iZ = 1; iZ < (_zSamples - 1); iZ++) {
        	double aV = -Math.PI*0.5+iZ*_thetaRad;

            // compute slice vertices with duplication at end point
            Vector3 kNormal;
            for (int iR = 0; iR < _radialSamples; iR++) {
            	double aH = -Math.PI+iR*_thetaRad;
            	double x = _center.getX()+_radiusA*Math.cos(aV)*Math.cos(aH);
            	double y = _center.getY()+_radiusB*Math.cos(aV)*Math.sin(aH);
            	double z = _center.getZ()+_radiusC*Math.sin(aV);
                _meshData.getVertexBuffer().put((float)x).put((float)y).put((float)z);
                tempVa.set(x, y, z);
                kNormal = tempVa.subtractLocal(_center);
                kNormal.normalizeLocal();
                _meshData.getNormalBuffer().put(kNormal.getXf()).put(kNormal.getYf()).put(kNormal.getZf());

                _meshData.getTextureCoords(0).getBuffer().put((float)iR/(float)_radialSamples).put((float)iZ/(float)_zSamples);
            }
        }

        // south pole
        _meshData.getVertexBuffer().put(_center.getXf()).put(_center.getYf()).put((float) (_center.getZ() - _radiusC));
        _meshData.getNormalBuffer().put(0).put(0).put(-1);
        _meshData.getTextureCoords(0).getBuffer().put(0.5f).put(0.0f);

        // north pole
        _meshData.getVertexBuffer().put(_center.getXf()).put(_center.getYf()).put((float) (_center.getZ() + _radiusC));
        _meshData.getNormalBuffer().put(0).put(0).put(1);
        _meshData.getTextureCoords(0).getBuffer().put(0.5f).put(1.0f);
        
        Vector3.releaseTempInstance(tempVa);
    }

    /**
     * sets the indices for rendering the sphere.
     */
    private void setIndexData() {
        // allocate connectivity
        final int verts = (_zSamples - 2) * _radialSamples + 2;
        final int tris = 2 * (_zSamples - 2) * (_radialSamples-1);
        _meshData.setIndices(BufferUtils.createIndexBufferData(3 * tris, verts - 1));

        // generate connectivity
        for (int iZ = 0, iZStart = 0; iZ < (_zSamples - 3); iZ++) {
            int i0 = iZStart;
            int i1 = i0 + 1;
            iZStart += _radialSamples;
            int i2 = iZStart;
            int i3 = i2 + 1;
            for (int i = 0; i < _radialSamples-1; i++) {
                _meshData.getIndices().put(i0++);
                _meshData.getIndices().put(i1);
                _meshData.getIndices().put(i2);
                _meshData.getIndices().put(i1++);
                _meshData.getIndices().put(i3++);
                _meshData.getIndices().put(i2++);
            }
        }

        // south pole triangles
        for (int i = 0; i < _radialSamples-1; i++) {
           _meshData.getIndices().put(i);
           _meshData.getIndices().put(_meshData.getVertexCount() - 2);
           _meshData.getIndices().put(i + 1);
        }

        // north pole triangles
        final int iOffset = (_zSamples - 3) * _radialSamples;
        for (int i = 0; i < _radialSamples-1; i++) {
           _meshData.getIndices().put(i + iOffset);
           _meshData.getIndices().put(i + 1 + iOffset);
           _meshData.getIndices().put(_meshData.getVertexCount() - 1);
        }
    }

    /**
     * Returns the center of this sphere.
     * 
     * @return The sphere's center.
     */
    public Vector3 getCenter() {
        return _center;
    }

    public double getRadiusA() {
        return _radiusA;
    }

    public double getRadiusB() {
        return _radiusB;
    }

    public double getRadiusC() {
        return _radiusC;
    }

    @Override
    public void write(final OutputCapsule capsule) throws IOException {
        super.write(capsule);
        capsule.write(_zSamples, "zSamples", 0);
        capsule.write(_radialSamples, "radialSamples", 0);
        capsule.write(_radiusA, "radiusA", 0);
        capsule.write(_radiusB, "radiusB", 0);
        capsule.write(_radiusC, "radiusC", 0);
        capsule.write(_center, "center", new Vector3(Vector3.ZERO));
    }

    @Override
    public void read(final InputCapsule capsule) throws IOException {
        super.read(capsule);
        _zSamples = capsule.readInt("zSamples", 0);
        _radialSamples = capsule.readInt("radialSamples", 0);
        _radiusA = capsule.readDouble("radiusA", 0);
        _radiusB = capsule.readDouble("radiusB", 0);
        _radiusC = capsule.readDouble("radiusC", 0);
        _center.set((Vector3) capsule.readSavable("center", new Vector3(Vector3.ZERO)));
    }
}