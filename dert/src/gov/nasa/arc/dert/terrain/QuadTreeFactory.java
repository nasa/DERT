package gov.nasa.arc.dert.terrain;

import gov.nasa.arc.dert.io.TileSource;
import gov.nasa.arc.dert.landscape.DerivativeLayer;
import gov.nasa.arc.dert.landscape.FieldCameraLayer;
import gov.nasa.arc.dert.landscape.FieldLayer;
import gov.nasa.arc.dert.raster.ProjectionInfo;
import gov.nasa.arc.dert.render.SharedTexture2D;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.UIUtil;

import java.awt.Color;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.image.Image;
import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.image.PixelDataType;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.image.util.ImageUtils;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.hint.NormalsMode;
import com.ardor3d.util.TextureKey;
import com.ardor3d.util.geom.BufferUtils;

/**
 * Helper class for building QuadTree tiles.
 *
 */
public class QuadTreeFactory {

	// Fields used for missing vertices
	private ColorRGBA missingColor = new ColorRGBA(0, 0, 0, 0);
	private float missingFillValue;

	// List of layers
	private Layer[] layerList;

	// The base layer
	private RasterLayer baseLayer;

	// Threading service
	private ExecutorService executor;

	// The surface color
	private float[] rgba;

	// Layers are showing
	private boolean layersEnabled = true;

	// Pixel scale factor for millimeter scale terrains
	private double pixelScale;

	// Dimensions
	private int tileWidth, tileLength;

	// The source of tile data
	private TileSource source;

	// A texture for empty tiles
	private Texture emptyTexture;

	// The dimensions of the entire terrain
	private double terrainWidth, terrainLength;
	
	private int bytesPerTile;
	
	private String label;

	/**
	 * Constructor
	 * 
	 * @param label
	 * @param source
	 * @param baseLayer
	 * @param layerList
	 * @param pixelScale
	 */
	public QuadTreeFactory(String label, TileSource source, RasterLayer baseLayer, Layer[] layerList, double pixelScale) {
		this.label = label;
		this.source = source;
		this.layerList = layerList;
		this.baseLayer = baseLayer;
		this.pixelScale = pixelScale;
		this.tileWidth = baseLayer.getTileWidth();
		this.tileLength = baseLayer.getTileLength();
		ProjectionInfo projInfo = baseLayer.getProjectionInfo();
		terrainWidth = baseLayer.getRasterWidth() * projInfo.scale[0] * pixelScale;
		terrainLength = baseLayer.getRasterLength() * projInfo.scale[1] * pixelScale;
		missingFillValue = baseLayer.getFillValue();

		bytesPerTile = (tileWidth*tileLength*14+2*tileWidth+2*tileLength)*4;
		for (int i = 0; i < layerList.length; ++i) {
			if (layerList[i] != null) {
				bytesPerTile += layerList[i].getBytesPerTile();
			}
		}

		executor = Executors.newFixedThreadPool(5);
	}

	/**
	 * Dispose of resources
	 */
	public void dispose() {
		executor.shutdown();
		QuadTreeCache.getInstance().clear(label);
	}

	/**
	 * Get a QuadTree
	 * 
	 * @param key
	 * @param p	QuadTree will be translated to this point
	 * @param pixelWidth
	 * @param pixelLength
	 * @param wait	wait for tile source to load the data
	 * @return
	 */
	public QuadTree getQuadTree(QuadKey key, ReadOnlyVector3 p, double pixelWidth, double pixelLength, boolean wait) {
		QuadTree quadTree = QuadTreeCache.getInstance().getQuadTree(label+key);
		if (quadTree == null)
			quadTree = createQuadTree(key, p, pixelWidth, pixelLength, wait);
		if (quadTree.getMesh() != null)
			return (quadTree);
		return(null);
	}

	/**
	 * Given the key, get a QuadTree
	 * 
	 * @param key
	 * @return
	 */
	public QuadTree getQuadTree(QuadKey key) {
		QuadTree quadTree = QuadTreeCache.getInstance().getQuadTree(label+key);
		if (quadTree == null) {
			Vector3 p = keyToTileCenter(key);
			double s = Math.pow(2, key.getLevel());
			double pixelWidth = (terrainWidth / tileWidth) / s;
			double pixelLength = (terrainLength / tileLength) / s;
			createQuadTree(key, p, pixelWidth, pixelLength, true);
		}
		else if (quadTree.getMesh() != null)
			return(quadTree);
		return (null);
	}

	/**
	 * Given a key, get the offset from the parent center
	 * 
	 * @param key
	 * @return
	 */
	private Vector3 keyToTileCenter(QuadKey key) {
		Vector3 p = new Vector3();
		byte[] path = key.getPath();
		if (path.length < 1) {
			return (p);
		}
		double n = Math.pow(2, path.length);
		double w = terrainWidth / n;
		double l = terrainLength / n;
		int q = key.getQuadrant();
		switch (q) {
		case 1:
			p.set(p.getX() - w, p.getY() + l, 0);
			break;
		case 2:
			p.set(p.getX() + w, p.getY() + l, 0);
			break;
		case 3:
			p.set(p.getX() - w, p.getY() - l, 0);
			break;
		case 4:
			p.set(p.getX() + w, p.getY() - l, 0);
			break;
		}
		return (p);
	}

	/**
	 * Given a key, find the OpenGl coordinates relative to the center of the
	 * terrain
	 * 
	 * @param key
	 * @return
	 */
	private Vector3 keyToTestPointCenter(QuadKey qp) {
		Vector3 p = new Vector3();
		byte[] path = qp.getPath();
		if (path.length <= 1) {
			return (p);
		}
		double w = terrainWidth/2;
		double l = terrainLength/2;
		for (int i = 0; i < path.length; ++i) {
			w /= 2;
			l /= 2;
			switch (path[i]) {
			case 1:
				p.set(p.getX() - w, p.getY() + l, 0);
				break;
			case 2:
				p.set(p.getX() + w, p.getY() + l, 0);
				break;
			case 3:
				p.set(p.getX() - w, p.getY() - l, 0);
				break;
			case 4:
				p.set(p.getX() + w, p.getY() - l, 0);
				break;
			}
		}
		return (p);
	}
	
	/**
	 * Indicate if a QuadTree with the give key has children.
	 * 
	 * @param quadKey
	 * @return have children
	 */
	public boolean childrenExist(QuadKey quadKey) {
		return(source.tileExists(quadKey+File.separator+"1"));
	}

	/**
	 * Get the 4 child QuadTrees of the given parent out of the cache.
	 * If not all 4 children are present in the cache, return null.
	 * 
	 * @param qp
	 * @param parent
	 * @param wait
	 * @return the QuadTrees or null if not all are present in the cache
	 */
	public QuadTree[] getQuadTreeChildren(QuadKey qp, QuadTree parent, boolean wait) {

		// load the quadtrees
		double pixelWidth = parent.pixelWidth / 2;
		double pixelLength = parent.pixelLength / 2;
		double xCenter = parent.pixelWidth * tileWidth / 4;
		double yCenter = parent.pixelLength * tileLength / 4;
		int count = 0;
		QuadTree[] qt = new QuadTree[4];
		qt[0] = getQuadTree(qp.createChild(1), new Vector3(-xCenter, yCenter, 0), pixelWidth, pixelLength, wait);
		if (qt[0] != null)
			count ++;
		qt[1] = getQuadTree(qp.createChild(2), new Vector3(xCenter, yCenter, 0), pixelWidth, pixelLength, wait);
		if (qt[1] != null)
			count ++;
		qt[2] = getQuadTree(qp.createChild(3), new Vector3(-xCenter, -yCenter, 0), pixelWidth, pixelLength, wait);
		if (qt[2] != null)
			count ++;
		qt[3] = getQuadTree(qp.createChild(4), new Vector3(xCenter, -yCenter, 0), pixelWidth, pixelLength, wait);
		if (qt[3] != null)
			count ++;
		if (count == 4)
			return(qt);
		else
			return(null);
	}

	private QuadTree createQuadTree(QuadKey key, ReadOnlyVector3 p, double pixelWidth, double pixelLength, boolean wait) {

		// create the quad tree tile and put it in the cache as a place holder
		// while we load the contents
		// this keeps us from starting another load operation for this tile
		final QuadTree qt = new QuadTree(key, p, pixelWidth, pixelLength, bytesPerTile);
		qt.createCornerPoints(keyToTestPointCenter(key), tileWidth, tileLength);
		QuadTreeCache.getInstance().putQuadTree(label+key, qt);

		// load the quad tree mesh contents
		if (wait) {
			loadQuadTreeContents(qt);
		} else {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					Thread.yield();
					loadQuadTreeContents(qt);
				}
			};
			executor.execute(runnable);
		}
		return(qt);
	}

	private void loadQuadTreeContents(QuadTree qt) {
		// load the mesh
		QuadTreeMesh mesh = getMesh(qt.getKey(), qt.pixelWidth, qt.pixelLength);
		if (mesh == null) {
			return;
		}

		// load the image layers as textures
		TextureState textureState = new TextureState();
		for (int i = 0; i < layerList.length; ++i) {
			Texture texture = null;
			if (layerList[i] != null) {
				if (mesh.isEmpty()) {
					// this is an empty quad tree tile (just for padding)
					texture = getEmptyTexture();
				} else if (layerList[i] instanceof DerivativeLayer) {
					texture = ((DerivativeLayer) layerList[i]).getTexture(qt.getKey(), null);
					((DerivativeLayer) layerList[i]).createColorMapTextureCoords(mesh, i);
				} else if (layerList[i] instanceof FieldLayer) {
					texture = ((FieldLayer) layerList[i]).getTexture(qt.getKey(), null);
					((FieldLayer) layerList[i]).createColorMapTextureCoords(qt.getKey(), mesh, i);
				} else if (!(layerList[i] instanceof FieldCameraLayer)) {
					// load the texture
					texture = getTexture(qt.getKey(), i, null);
					if (texture == null) {
						texture = getEmptyTexture();
					}
				}
			}
			else if (i == 0)
				texture = getEmptyTexture();
			textureState.setTexture(texture, i);
		}
		textureState.setEnabled(layersEnabled);
		mesh.setRenderState(textureState);
		qt.setMesh(mesh);
	}

	private Texture getEmptyTexture() {
		if (emptyTexture == null) {
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(16);
			for (int i = 0; i < 16; ++i) {
				byteBuffer.put((byte) 0);
			}
			byteBuffer.flip();
			ImageDataFormat format = ImageDataFormat.Luminance;
			PixelDataType type = PixelDataType.UnsignedByte;
			ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>(1);
			list.add(byteBuffer);
			Image image = new Image(format, type, 4, 4, list, null);
			emptyTexture = new SharedTexture2D();
			TextureKey tKey = TextureKey.getKey(null, false, TextureStoreFormat.GuessNoCompressedFormat,
				"DertEmptyTexture", Texture.MinificationFilter.BilinearNoMipMaps);
			emptyTexture.setTextureKey(tKey);
			emptyTexture.setImage(image);
			emptyTexture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
			emptyTexture.setTextureStoreFormat(ImageUtils.getTextureStoreFormat(tKey.getFormat(), image));
			emptyTexture.setApply(Texture2D.ApplyMode.Modulate);
			emptyTexture.setHasBorder(false);
			emptyTexture.setWrap(Texture.WrapMode.EdgeClamp);
			emptyTexture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
		}
		return (emptyTexture);
	}

	private Texture getTexture(QuadKey key, int tUnit, Texture texture) {
		if (layerList[tUnit] == null) {
			return (null);
		}
		texture = layerList[tUnit].getTexture(key, texture);
		if (texture == null) {
			return (null);
		}

		texture.setApply(Texture2D.ApplyMode.Modulate);
		texture.setHasBorder(false);
		texture.setWrap(Texture.WrapMode.EdgeClamp);
		texture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
		return (texture);
	}

	private QuadTreeMesh getEmptyMesh(QuadKey key, FloatBuffer vertices, double pixelWidth, double pixelLength) {
		// vertices
		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(4 * 3);
		vertexBuffer.put(vertices.get(0)).put(vertices.get(1)).put(vertices.get(2));
		int i = tileLength * (tileWidth + 1) * 3;
		vertexBuffer.put(vertices.get(i)).put(vertices.get(i + 1)).put(vertices.get(i + 2));
		i += tileWidth * 3;
		vertexBuffer.put(vertices.get(i)).put(vertices.get(i + 1)).put(vertices.get(i + 2));
		i = tileWidth * 3;
		vertexBuffer.put(vertices.get(i)).put(vertices.get(i + 1)).put(vertices.get(i + 2));
		vertexBuffer.flip();

		// normals
		FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(4 * 3);
		for (int j = 0; j < 4; ++j) {
			normalBuffer.put(0).put(0).put(1);
		}
		normalBuffer.flip();

		// colors
		FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(4 * 4);
		for (int j = 0; j < 4; ++j) {
			colorBuffer.put(missingColor.getRed()).put(missingColor.getGreen()).put(missingColor.getBlue())
				.put(missingColor.getAlpha());
		}
		colorBuffer.flip();

		// indices
		IntBuffer indexBuffer = BufferUtils.createIntBuffer(4);
		indexBuffer.put(0).put(1).put(3).put(2);
		int[] indexLengths = { 4 };

		// texture coordinates
		FloatBuffer texCoordBuffer = BufferUtils.createFloatBuffer(4 * 2);
		texCoordBuffer.put(0).put(1);
		texCoordBuffer.put(0).put(0);
		texCoordBuffer.put(1).put(0);
		texCoordBuffer.put(1).put(1);
		texCoordBuffer.flip();

		QuadTreeMesh mesh = new QuadTreeMesh("_mesh_"+key, tileWidth, tileLength, pixelWidth, pixelLength);
		mesh.empty = true;
		mesh.setMeshData(createTileMeshData(vertexBuffer, texCoordBuffer, colorBuffer, indexBuffer, indexLengths,
			normalBuffer, IndexMode.TriangleStrip));

		return (mesh);
	}

	private QuadTreeMesh getMesh(QuadKey key, double pixelWidth, double pixelLength) {
		// vertices, normals, and colors
		Object[] result = getVertices(key, pixelWidth * tileWidth, pixelWidth, pixelLength * tileLength, pixelLength);
		if (result == null) {
			return (null);
		}
		FloatBuffer vertexBuffer = (FloatBuffer) result[0];
		FloatBuffer colorBuffer = (FloatBuffer) result[1];
		FloatBuffer normalBuffer = (FloatBuffer) result[2];
		boolean empty = (Boolean) result[3];
		
		QuadTreeMesh mesh = null;

		// all NaNs
		if (empty) {
			mesh = getEmptyMesh(key, vertexBuffer, pixelWidth, pixelLength);
		}
		else {

			// vertex indices
			result = getIndices(vertexBuffer);
			IntBuffer indexBuffer = (IntBuffer) result[0];
			int[] indexLengths = (int[]) result[1];
			FloatBuffer texCoordBuffer = getTexCoords();

			mesh = new QuadTreeMesh("_mesh_"+key, tileWidth, tileLength, pixelWidth, pixelLength);
			mesh.setMeshData(createTileMeshData(vertexBuffer, texCoordBuffer, colorBuffer, indexBuffer, indexLengths,
					normalBuffer, IndexMode.TriangleStrip));
		}

		mesh.getSceneHints().setNormalsMode(NormalsMode.NormalizeIfScaled);
		CullState cullState = new CullState();
		cullState.setCullFace(CullState.Face.Back);
		cullState.setEnabled(true);
		mesh.setRenderState(cullState);
		mesh.setModelBound(new BoundingBox());
		mesh.updateModelBound();
		return (mesh);
	}
	
	private synchronized TileMeshData createTileMeshData(FloatBuffer vertexBuffer, FloatBuffer texCoordBuffer, FloatBuffer colorBuffer, IntBuffer indexBuffer, int[] indexLengths,
			FloatBuffer normalBuffer, IndexMode indexMode) {
		return(new TileMeshData(vertexBuffer, texCoordBuffer, colorBuffer, indexBuffer, indexLengths,
				normalBuffer, indexMode));
	}

	private FloatBuffer getTexCoords() {
		int tWidth = tileWidth + 1;
		int tLength = tileLength + 1;
		int size = tWidth * tLength * 2;
		FloatBuffer texCoords = BufferUtils.createFloatBuffer(size);
		int i = 0;
		for (int r = 0; r < tLength; ++r) {
			for (int c = 0; c < tWidth; ++c) {
				texCoords.put(i * 2, ((float) c) / tileWidth);
				texCoords.put(i * 2 + 1, ((float) r) / tileLength);
				i++;
			}
		}
		texCoords.limit(size);
		texCoords.rewind();
		return (texCoords);
	}

	private Object[] getIndices(FloatBuffer vertices) {
		int tWidth = tileWidth + 1;
		int[] indexLengths = new int[tileLength];
		IntBuffer indices = BufferUtils.createIntBuffer(tileLength * tWidth * 2);
		int i = 0;
		// System.err.println("RasterTile.createIndexes "+tSize+" "+tSize1+" "+newLevel+" "+vertices.limit());
		for (int r = 0; r < tileLength; ++r) {
			for (int c = 0; c < tWidth; ++c) {
				indices.put(i);
				indices.put(i + tWidth);
				i++;
			}
			indexLengths[r] = tWidth * 2;
		}
		indices.flip();
		Object[] result = new Object[2];
		result[0] = indices;
		result[1] = indexLengths;
		return (result);
	}

	private Object[] getVertices(QuadKey key, double width, double pixelWidth, double height, double pixelLength) {

		// Get the base layer tile data
		QuadTreeTile tile = baseLayer.getTile(key);
		if (tile == null) {
			return (null);
		}
		int dataSize = tile.width * tile.length;
		FloatBuffer data = tile.raster.asFloatBuffer();

		// create vertex and color buffers
		FloatBuffer vertex = BufferUtils.createFloatBuffer(dataSize * 3);
		FloatBuffer colors = BufferUtils.createFloatBuffer(dataSize * 4);

		// fill buffers
		int cb = 0;
		int ce = tile.width - 1;
		int rb = 0;
		int re = tile.length - 1;

		int k = 0;
		boolean empty = true;

		float y = (float) height / 2;
		for (int r = rb; r <= re; ++r) {
			float x = -(float) width / 2;
			for (int c = cb; c <= ce; ++c) {
				float z = data.get(k);
				// fill missing value vertices with missing value color
				if (Float.isNaN(z)) {
					z = missingFillValue;
					colors.put(0).put(0).put(0).put(0);
				} else {
					colors.put(rgba[0]).put(rgba[1]).put(rgba[2]).put(rgba[3]);
					empty = false;
				}
				vertex.put(x).put(y).put((float) (z * pixelScale));
				k++;
				x += pixelWidth;
			}
			y -= pixelLength;
		}
		vertex.flip();
		colors.flip();

		// get normals
		FloatBuffer normals = createNormals(vertex, tile.length, tile.width, dataSize);

		// return results
		Object[] result = new Object[4];
		result[0] = vertex;
		result[1] = colors;
		result[2] = normals;
		result[3] = new Boolean(empty);
		return (result);
	}

	private FloatBuffer createNormals(FloatBuffer vertex, int rows, int cols, int dataSize) {
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

	/**
	 * Set the surface color for all QuadTrees
	 * 
	 * @param surfaceColor
	 */
	public void setSurfaceColor(Color surfaceColor) {
		rgba = UIUtil.colorToFloatArray(surfaceColor);
		QuadTreeCache.getInstance().updateSurfaceColor(label, rgba);
	}
}
