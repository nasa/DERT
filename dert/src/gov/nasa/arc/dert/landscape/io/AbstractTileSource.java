package gov.nasa.arc.dert.landscape.io;

import gov.nasa.arc.dert.landscape.quadtree.QuadKey;

import java.util.ArrayList;

/**
 * Interface to source of landscape tiles.
 *
 */
public abstract class AbstractTileSource
	implements TileSource {

	// Quad tree structure of existing tile keys
	protected DepthTree depthTree;

	/**
	 * Given an X,Y coordinate, find the highest level tile key that contains
	 * that coordinate.
	 * 
	 * @param x
	 *            , y the coordinate
	 * @param worldWidth
	 *            , worldHeight the physical dimensions of the source
	 * @return the key string
	 */
	public synchronized QuadKey getKey(double x, double y, double worldWidth, double worldLength) {
		return (getKey(depthTree, x, y, new ArrayList<Byte>(), worldWidth / 2, worldLength / 2, -1));
	}

	/**
	 * Given an X,Y coordinate and level, find the key of the tile that contains
	 * that coordinate. Returns next best level if doesn't reach requested level.
	 * 
	 * @param x
	 *            , y the coordinate
	 * @param worldWidth
	 *            , worldHeight the physical dimensions of the source
	 * @return the key string
	 */
	public synchronized QuadKey getKey(double x, double y, double worldWidth, double worldLength, int lvl) {
		return (getKey(depthTree, x, y, new ArrayList<Byte>(), worldWidth / 2, worldLength / 2, lvl));
	}

	private QuadKey getKey(DepthTree depthTree, double x, double y, ArrayList<Byte> qList, double width, double length, int lvl) {
		if (depthTree.child == null)
			return (new QuadKey(qList));
		if (lvl == 0)
			return(new QuadKey(qList));	
		double w = width / 2;
		double l = length / 2;
		if (x < 0) {
			if (y >= 0) {
				depthTree = depthTree.child[0];
				qList.add(new Byte((byte)1));
				return (getKey(depthTree, x + w, y - l, qList, w, l, lvl-1));
			} else {
				depthTree = depthTree.child[2];
				qList.add(new Byte((byte)3));
				return (getKey(depthTree, x + w, y + l, qList, w, l, lvl-1));
			}
		} else {
			if (y >= 0) {
				depthTree = depthTree.child[1];
				qList.add(new Byte((byte)2));
				return (getKey(depthTree, x - w, y - l, qList, w, l, lvl-1));
			} else {
				depthTree = depthTree.child[3];
				qList.add(new Byte((byte)4));
				return (getKey(depthTree, x - w, y + l, qList, w, l, lvl-1));
			}
		}
	}
	
	protected abstract DepthTree getDepthTree();

}
