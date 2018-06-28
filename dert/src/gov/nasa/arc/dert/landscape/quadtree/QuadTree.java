package gov.nasa.arc.dert.landscape.quadtree;

import gov.nasa.arc.dert.camera.BasicCamera;
import gov.nasa.arc.dert.scene.World;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Node;

/**
 * An instance of QuadTree represents a tile in the landscape. It provides
 * a node in a hierarchical quad-tree data structure. This object
 * contains a mesh that is displayed in the landscape, and a pointer to 4
 * child QuadTrees. When the QuadTree is split, the mesh is detached and the
 * children are attached. When the QuadTree is merged, the children are detached
 * and the mesh is re-attached. Each time a change occurs the meshes are stitched
 * together with their neighbors using a set of stored edge vertices and normals.
 * A set of corner points is maintained to aid in splitting and merging.
 *
 */
public class QuadTree
	extends Node {

	// The minimum number of screen pixels for a mesh cell
	public static int CELL_SIZE = 4;

	// Side of a quad tree
	public static enum Side {
		Left, Right, Bottom, Top
	}
	
	private static int[] adjQuadHort = {2, 1, 4, 3};
	private static int[] adjQuadVert = {3, 4, 1, 2};

	// This quad tree is in use
	public boolean inUse;

	// Last time this quad tree was pulled from the cache
	protected long timestamp;

	// The mesh that will be rendered
	protected QuadTreeMesh mesh;

	// This quad tree is at the highest resolution
	protected boolean highestLevel;

	// The next level of quad trees
	protected QuadTree[] child;

	// Sides of the quad tree that need stitching
	private boolean[] dirty = new boolean[Side.values().length];

	// The quad tree's neighbors
	private QuadTree left, top, right, bottom;

	// Fields used for determining if quad tree should be merged or split
	private Vector3 camLoc, lookAt, closest;

	// Points in the quad tree used to determine if it should be merged or split
	// Coordinates are relative to the landscape center
	private Vector3[] cornerPoint;
	private Vector3 centerPoint;

	// Pixel dimensions
	protected double pixelWidth, pixelLength;
	
	// Size of this quad tree object in bytes
	protected int sizeInBytes;
	
	// Unique identifier for this QuadTree. Contains path in file system.
	protected QuadKey quadKey;
	
	// List to hold neighbors during stitching
	protected ArrayList<QuadTree> neighborList = new ArrayList<QuadTree>();
	

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param p
	 *            Translation point relative to the parent quad tree
	 * @param pixelWidth
	 * @param pixelLength
	 * @param level
	 * @param quadrant
	 */
	public QuadTree(QuadKey qKey, ReadOnlyVector3 p, double pixelWidth, double pixelLength, int sizeInBytes) {
		super(qKey.toString());
		camLoc = new Vector3();
		lookAt = new Vector3();
		closest = new Vector3();

		this.quadKey = qKey;
		this.pixelWidth = pixelWidth;
		this.pixelLength = pixelLength;
		this.sizeInBytes = sizeInBytes;
		setTranslation(p);
	}
	
	public QuadKey getKey() {
		return(quadKey);
	}

	/**
	 * Create the test points for this quad tree. They will include the 4
	 * corners and p. The coordinates are relative to the center of the
	 * landscape.
	 * 
	 * @param p
	 */
	public void createCornerPoints(ReadOnlyVector3 p, int tileWidth, int tileLength) {
		double width = tileWidth * pixelWidth / 2;
		double length = tileLength * pixelLength / 2;
		// create corner points
		cornerPoint = new Vector3[4];
		cornerPoint[0] = new Vector3(p.getX() - width, p.getY() - length, p.getZ());
		cornerPoint[1] = new Vector3(p.getX() + width, p.getY() - length, p.getZ());
		cornerPoint[2] = new Vector3(p.getX() + width, p.getY() + length, p.getZ());
		cornerPoint[3] = new Vector3(p.getX() - width, p.getY() + length, p.getZ());
		centerPoint = new Vector3(p);
	}

	/**
	 * Set the mesh for this QuadTree
	 * 
	 * @param mesh
	 */
	public synchronized void setMesh(QuadTreeMesh mesh, double minZ) {
		attachChild(mesh);
		updateGeometricState(0);
		int tileWidth = mesh.getTileWidth();
		int tileLength = mesh.getTileLength();
		int tWidth = tileWidth + 1;

		// update test point Z values
		FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		if (mesh.isEmpty()) {
			for (int i = 0; i < cornerPoint.length; ++i)
				cornerPoint[i].setZ(vertexBuffer.get(2));
			centerPoint.setZ(vertexBuffer.get(2));
		} else {
			// lower left
			cornerPoint[0].setZ(vertexBuffer.get(tileLength * tWidth * 3 + 2) - minZ);
			// lower right
			cornerPoint[1].setZ(vertexBuffer.get((tileLength * tWidth + tileWidth) * 3 + 2) - minZ);
			// upper right
			cornerPoint[2].setZ(vertexBuffer.get(tileWidth * 3 + 2) - minZ);
			// upper left
			cornerPoint[3].setZ(vertexBuffer.get(2) - minZ);
			// center
			centerPoint.setZ(vertexBuffer.get((tWidth * tileLength / 2 + tileWidth / 2) * 3 + 2) - minZ);
		}
		this.mesh = mesh;
	}

	/**
	 * Get the mesh
	 * 
	 * @return
	 */
	public synchronized QuadTreeMesh getMesh() {
		return (mesh);
	}
	
	public int getSize() {
		return(sizeInBytes);
	}

	/**
	 * Does this quad tree contain the coordinate X,Y?
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public final boolean contains(double x, double y) {
		if (cornerPoint == null) {
			return (false);
		}
		if (x < cornerPoint[0].getX()) {
			return (false);
		}
		if (x > cornerPoint[2].getX()) {
			return (false);
		}
		if (y < cornerPoint[0].getY()) {
			return (false);
		}
		if (y > cornerPoint[2].getY()) {
			return (false);
		}
		return (true);
	}

	/**
	 * Set the Quad Tree neighbors
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	public void setNeighbors(QuadTree left, QuadTree right, QuadTree bottom, QuadTree top) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		setDirty(Side.Left, (left != null));
		setDirty(Side.Right, (right != null));
		setDirty(Side.Bottom, (bottom != null));
		setDirty(Side.Top, (top != null));
	}
	
	private void stitch(Side side, QuadTree neighbor) {
		if (getMesh().isEmpty()) {
			Arrays.fill(dirty, false);
			return;
		}
		if (neighbor.getMesh().isEmpty()) {
			Arrays.fill(neighbor.dirty, false);
			setDirty(side, false);
			return;
		}
		neighborList.clear();
		getNeighbors(side, neighbor, quadKey, neighborList);
		for (int i=0; i<neighborList.size(); ++i) {
			neighbor = neighborList.get(i);
			if (neighbor.getMesh().isEmpty())
				Arrays.fill(neighbor.dirty, false);
			else
				doStitch(side, neighbor);
		}
		setDirty(side, false);
	}

	/**
	 * Stitch a quadtree to the given side.
	 * 
	 * @param side
	 *            the side where the quadtree is located
	 * @param that
	 *            the quadtree
	 */
	private void doStitch(Side side, QuadTree that) {
		boolean sameLevel = (this.quadKey.getLevel() == that.quadKey.getLevel());
		// this and that are on the same level
		if (sameLevel) {
			int tileWidth = getMesh().getTileWidth();
			int tileLength = getMesh().getTileLength();
			switch (side) {
			case Left:
				getMesh().setElevationColumn(0, getMesh().getEdge(Side.Left));
				getMesh().setNormalsColumn(0, getMesh().getNrml(Side.Left));
				that.getMesh().setElevationColumn(tileWidth, that.getMesh().getEdge(Side.Right));
				that.getMesh().setNormalsColumn(tileWidth, getMesh().getNrml(Side.Left));
				setDirty(Side.Left, false);
				that.setDirty(Side.Right, false);
				break;
			case Right:
				getMesh().setElevationColumn(tileWidth, getMesh().getEdge(Side.Right));
				getMesh().setNormalsColumn(tileWidth, getMesh().getNrml(Side.Right));
				that.getMesh().setElevationColumn(0, that.getMesh().getEdge(Side.Left));
				that.getMesh().setNormalsColumn(0, getMesh().getNrml(Side.Right));
				setDirty(Side.Right, false);
				that.setDirty(Side.Left, false);
				break;
			case Bottom:
				getMesh().setElevationRow(tileLength, getMesh().getEdge(Side.Bottom));
				getMesh().setNormalsRow(tileLength, getMesh().getNrml(Side.Bottom));
				that.getMesh().setElevationRow(0, that.getMesh().getEdge(Side.Top));
				that.getMesh().setNormalsRow(0, getMesh().getNrml(Side.Bottom));
				setDirty(Side.Bottom, false);
				that.setDirty(Side.Top, false);
				break;
			case Top:
				getMesh().setElevationRow(0, getMesh().getEdge(Side.Top));
				getMesh().setNormalsRow(0, getMesh().getNrml(Side.Top));
				that.getMesh().setElevationRow(tileLength, that.getMesh().getEdge(Side.Bottom));
				that.getMesh().setNormalsRow(tileLength, getMesh().getNrml(Side.Top));
				setDirty(Side.Top, false);
				that.setDirty(Side.Bottom, false);
				break;
			}
		}
		// this is higher resolution than that
		// find the ends and do the stitching
		else if (this.quadKey.getLevel() > that.quadKey.getLevel()) {
			int[] e = findStitchEnds(side, that);
			getMesh().fillEdge(side, e, that.getMesh());
			setDirty(side, false);
		}
		// this is lower resolution than that
		// use that instead of this, find the ends and do the stitching
		else if (this.quadKey.getLevel() < that.quadKey.getLevel()) {
			side = switchSides(side);
			int[] e = that.findStitchEnds(side, this);
			that.getMesh().fillEdge(side, e, this.getMesh());
			that.setDirty(side, false);
		}
	}
	
	/**
	 * Print out if this QuadTree still has dirty sides. Used for debugging.
	 * @return if any sides are dirty
	 */
	public boolean isDirty() {
		if (child == null) {
			if (isDirty(Side.Left))
				System.err.println("QuadTree.isDirty LEFT "+getName());
			if (isDirty(Side.Right))
				System.err.println("QuadTree.isDirty RIGHT "+getName());
			if (isDirty(Side.Bottom))
				System.err.println("QuadTree.isDirty BOTTOM "+getName());
			if (isDirty(Side.Top))
				System.err.println("QuadTree.isDirty TOP "+getName());
			return(isDirty(Side.Left) || isDirty(Side.Right) || isDirty(Side.Bottom) || isDirty(Side.Top));
		}
		else
			return(child[0].isDirty() || child[1].isDirty() || child[2].isDirty() || child[3].isDirty());
	}
	
	private Side switchSides(Side side) {
		switch (side) {
		case Left:
			return(Side.Right);
		case Right:
			return(Side.Left);
		case Bottom:
			return(Side.Top);
		case Top:
			return(Side.Bottom);
		}
		return(null);
	}
	
	private void setDirty(Side side, boolean val) {
		dirty[side.ordinal()] = val;
	}
	
	private boolean isDirty(Side side) {
		return(dirty[side.ordinal()]);
	}

	/**
	 * Splitting method.  If not at the highest level already, get the children of this QuadTree
	 * from the cache. They are only returned if they are ready. If they are set them. 
	 * @return true if children are set
	 */
	private boolean split() {
		if (!highestLevel) {
			QuadTreeFactory factory = QuadTreeFactory.getInstance();
			highestLevel = !factory.childrenExist(quadKey);
			if (!highestLevel) {
				QuadTree[] child = factory.getQuadTreeChildren(quadKey, this, false);
				if (child != null) {
					setChildren(child);
					return(true);
				}
			}
		}
		return(false);
	}

	/**
	 * Detach the parent mesh and attach the children.
	 * @param child
	 */
	private void setChildren(QuadTree[] child) {
		child[0].setNeighbors(left, child[1], child[2], top);
		child[1].setNeighbors(child[0], right, child[3], top);
		child[2].setNeighbors(left, child[3], bottom, child[0]);
		child[3].setNeighbors(child[2], right, bottom, child[1]);
		detachChild(mesh);
		for (int i = 0; i < child.length; ++i) {
			attachChild(child[i]);
			child[i].inUse = true;
			World.getInstance().getMarble().landscapeChanged(child[i]);
			World.getInstance().getLandmarks().landscapeChanged(child[i]);
			World.getInstance().getFeatureSets().landscapeChanged(child[i]);
		}
		Arrays.fill(dirty, false);
		this.child = child;
	}

	/**
	 * Remove all children from this QuadTree and re-attach its mesh.
	 * return true if children are detached
	 */
	private boolean clearChildren() {
		if (child == null) {
			return(false);
		}
		
		for (int i = 0; i < child.length; ++i) {
			child[i].clearChildren();
			child[i].setNeighbors(null, null, null, null);
			detachChild(child[i]);
			child[i].inUse = false;
			Arrays.fill(child[i].dirty, false);
			child[i] = null;
		}
		child = null;
		attachChild(mesh);
		setDirty(Side.Left, (left != null));
		setDirty(Side.Right, (right != null));
		setDirty(Side.Bottom, (bottom != null));
		setDirty(Side.Top, (top != null));
		return(true);
	}

	/**
	 * Traverse the quad tree recursively to stitch all dirty sides.
	 */
	public void stitch() {
		if (child != null)
			for (int i=0; i<child.length; ++i)
				child[i].stitch();
		else {
			if (isDirty(Side.Left) && (left != null))
				stitch(Side.Left, left);
			if (isDirty(Side.Right) && (right != null))
				stitch(Side.Right, right);
			if (isDirty(Side.Bottom) && (bottom != null))
				stitch(Side.Bottom, bottom);
			if (isDirty(Side.Top) && (top != null))
				stitch(Side.Top, top);
		}
	}

	/**
	 * Merge 4 quad trees by removing the children from a parent and restoring its mesh.
	 * @return success
	 */
	private boolean merge() {
		boolean success = clearChildren();
		if (success) {
			World.getInstance().getMarble().landscapeChanged(this);
			World.getInstance().getLandmarks().landscapeChanged(this);
			World.getInstance().getFeatureSets().landscapeChanged(this);
		}
		return(success);
	}

	/**
	 * Determine if this QuadTree needs to be merged or split
	 * 
	 * @param camera
	 * @param return true if changed
	 */
	public boolean update(final BasicCamera camera) {
		if (!inUse) {
			return(false);
		}
		
		boolean isCulled = camera.isCulled(this);

//		Vector3[] tPoint = getCornerPoints();

		if (cornerPoint == null) {
			return(false);
		}

		// find test point or camera lookAt point that is closest to the camera
		double minDist = Double.MAX_VALUE;
		camLoc.set(camera.getLocation());
		lookAt.set(camera.getLookAt());
		if (contains(lookAt.getX(), lookAt.getY())) {
			minDist = camLoc.distance(lookAt);
			closest.set(lookAt);
		}
		if (camLoc.distance(centerPoint) < minDist)
			closest.set(centerPoint);
		for (int i=0; i<cornerPoint.length; ++i) {
			double d = camLoc.distance(cornerPoint[i]);
			if (d < minDist) {
				minDist = d;
				closest.set(cornerPoint[i]);
			}
		}

		// get the pixel size at the closest point
		double pixSize = camera.getPixelSizeAt(minDist, true);
		if (pixSize <= 0)
			return(false);

		// Mesh cells should be larger than a single pixel.
		pixSize *= CELL_SIZE;
		
		boolean changed = false;

		// greater than the pixel size of this tile, we can go to a coarser
		// resolution by merging
		if ((pixSize >= pixelWidth) || isCulled) {
			// only merge if we have split
			if (child != null) {
				changed = merge();
//				System.err.println("QuadTree.update merge "+getName()+" "+changed);
			}
		}

		// less than the pixel size of this tile, we need to go to a higher
		// resolution by splitting
		else if (pixSize <= pixelWidth / 2) {
			// only split if we haven't already
			if (child == null) {
				changed = split();
//				System.err.println("QuadTree.update split "+getName()+" "+changed);
			} else {
				for (int i = 0; i < child.length; ++i) {
					changed |= child[i].update(camera);
				}
			}
		}

		// let children update
		else if (child != null) {
			for (int i = 0; i < child.length; ++i) {
				changed |= child[i].update(camera);
			}
		}
		return(changed);
	}
	
	/**
	 * Get all of the neighbors that are adjacent on the given side.
	 * @param side
	 * @param neighbor
	 * @param qKey
	 * @param neighborList
	 */
	private void getNeighbors(Side side, QuadTree neighbor, QuadKey qKey, ArrayList<QuadTree> neighborList) {
		
		// At the bottom of the tree, add the neighbor to the list
		if (neighbor.child == null) {
			neighborList.add(neighbor);
		}
		// The neighbor is a higher or same resolution than this.
		// Search its children.
		else if (neighbor.quadKey.getLevel() >= qKey.getLevel()) {
			switch (side) {
			case Left:
				getNeighbors(side, neighbor.child[1], qKey, neighborList);
				getNeighbors(side, neighbor.child[3], qKey, neighborList);
				break;
			case Right:
				getNeighbors(side, neighbor.child[0], qKey, neighborList);
				getNeighbors(side, neighbor.child[2], qKey, neighborList);
				break;
			case Bottom:
				getNeighbors(side, neighbor.child[0], qKey, neighborList);
				getNeighbors(side, neighbor.child[1], qKey, neighborList);
				break;
			case Top:
				getNeighbors(side, neighbor.child[2], qKey, neighborList);
				getNeighbors(side, neighbor.child[3], qKey, neighborList);
				break;
			}
		}
		// The neighbor has a lower resolution than this. Determine which child we are next to.
		// Search that child.
		else {
			int q = qKey.getPath(neighbor.quadKey.getLevel());
			switch (side) {
			case Left:
			case Right:
				q = adjQuadHort[q-1];
				getNeighbors(side, neighbor.child[q-1], qKey, neighborList);
				break;
			case Bottom:
			case Top:
				q = adjQuadVert[q-1];
				getNeighbors(side, neighbor.child[q-1], qKey, neighborList);
				break;
			}
		}
	}

	/**
	 * Dispose of any resources
	 */
	public void dispose() {
		inUse = false;
		if (mesh != null)
			mesh.dispose();
		mesh = null;
	}

	/**
	 * Get the center of the QuadTree
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getCenter() {
		return (getWorldTranslation());
	}

	/**
	 * Get the test points
	 * 
	 * @return
	 */
	public Vector3[] getCornerPoints() {
		return (cornerPoint);
	}

	/**
	 * Get the elevation at the given coordinates using bilinear interpolation.
	 * 
	 * @param x
	 * @param y
	 * @return NaN if outside this QuadTree
	 */
	public float getElevation(double x, double y) {
		if (child != null) {
			for (int i = 0; i < child.length; ++i) {
				if (child[i].contains(x, y)) {
					return (child[i].getElevation(x, y));
				}
			}
		} else {
			return (getMesh().getElevationBilinear(x - cornerPoint[0].getX(), y - cornerPoint[0].getY()));
		}
		return (Float.NaN);
	}

	/**
	 * Get the elevation using nearest neighbor interpolation.
	 * 
	 * @param x
	 * @param y
	 * @return NaN, if outside this QuadTree
	 */
	public float getElevationNearestNeighbor(double x, double y) {
		if (child != null) {
			for (int i = 0; i < child.length; ++i) {
				if (child[i].contains(x, y)) {
					return (child[i].getElevationNearestNeighbor(x, y));
				}
			}
		} else {
			return (mesh.getElevationNearestNeighbor(x - cornerPoint[0].getX(), y - cornerPoint[0].getY()));
		}
		return (Float.NaN);
	}

	/**
	 * Get the normal at the given X,Y coordinate
	 * 
	 * @param x
	 * @param y
	 * @param store
	 * @return
	 */
	public boolean getNormal(double x, double y, Vector3 store) {
		if (child != null) {
			for (int i = 0; i < child.length; ++i) {
				if (child[i].contains(x, y)) {
					return (child[i].getNormal(x, y, store));
				}
			}
		} else {
			return (getMesh().getNormal((int) Math.floor((x - cornerPoint[0].getX()) / pixelWidth),
					getMesh().getTileLength()-(int)Math.floor((y - cornerPoint[0].getY()) / pixelLength), store));
		}
		return (false);
	}

	/**
	 * Find the ends of the pixels that must be stitched between two adjacent QuadTrees.
	 * @param side
	 * @param that
	 * @return
	 */
	private int[] findStitchEnds(Side side, QuadTree that) {
		int tileWidth = mesh.getTileWidth();
		int tileLength = mesh.getTileLength();

		// start point
		int j = 0;
		// end point
		int k = 0;
		switch (side) {
		case Left:
		case Right:
			j = quadKey.findYAtLevel(0, that.quadKey.getLevel(), tileLength);
			k = quadKey.findYAtLevel(tileLength, that.quadKey.getLevel(), tileLength);
			break;
		case Top:
		case Bottom:
			j = quadKey.findXAtLevel(0, that.quadKey.getLevel(), tileWidth);
			k = quadKey.findXAtLevel(tileWidth, that.quadKey.getLevel(), tileWidth);
			break;
		}
		return (new int[] { j, k });
	}	
	
	@Override
	public String toString() {
		return(getName());
	}

}
