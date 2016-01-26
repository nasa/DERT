package gov.nasa.arc.dert.landscape;

import java.util.HashMap;

public class QuadTreeCache {

	// The maximum amount of memory for the cache (in bytes)
	public static long MAX_CACHE_MEMORY = 400000000l;

	// A hash map to keep track of QuadTree tiles
	protected HashMap<String, QuadTree> quadTreeMap;

	// The number of cache cleanups since the last garbage collection
	protected int cleanupCount;

	// The maximum cache size (in quad trees)
	protected long maxCacheSize;

	/**
	 * Constructor
	 * 
	 * @param bytesPerTile
	 */
	public QuadTreeCache(int bytesPerTile) {
		quadTreeMap = new HashMap<String, QuadTree>();
		maxCacheSize = MAX_CACHE_MEMORY / bytesPerTile;
	}

	/**
	 * Given a key, return the associated QuadTree Update the timestamp.
	 * 
	 * @param key
	 * @return
	 */
	public synchronized QuadTree getQuadTree(String key) {
		QuadTree quadTree = quadTreeMap.get(key);
		if (quadTree != null) {
			quadTree.timestamp = System.currentTimeMillis();
		}
		return (quadTree);
	}

	/**
	 * Place a QuadTree in the cache Update the timestamp
	 * 
	 * @param key
	 * @param quadTree
	 */
	public synchronized void putQuadTree(String key, QuadTree quadTree) {
		quadTree.timestamp = System.currentTimeMillis();
		quadTreeMap.put(key, quadTree);
		cleanUpCache();
	}

	protected void cleanUpCache() {
		// not full yet
		if (quadTreeMap.size() < maxCacheSize) {
			return;
		}
		// get the oldest QuadTree not in use
		Object[] key = new Object[quadTreeMap.size()];
		key = quadTreeMap.keySet().toArray(key);
		Object oldestKey = key[0];
		QuadTree oldestItem = quadTreeMap.get(oldestKey);
		for (int i = 1; i < key.length; ++i) {
			QuadTree item = quadTreeMap.get(key[i]);
			if (oldestItem.enabled == item.enabled) {
				if (item.timestamp < oldestItem.timestamp) {
					oldestKey = key[i];
					oldestItem = item;
				}
			} else if (oldestItem.enabled && !item.enabled) {
				oldestItem = item;
				oldestKey = key[i];
			}
		}
		// remove the oldest item if not in use
		if (!oldestItem.enabled) {
			QuadTree qt = quadTreeMap.remove(oldestKey);
			qt.dispose();
			cleanupCount++;
			if (cleanupCount == 100) {
				System.gc();
				cleanupCount = 0;
			}
		}
	}

	/**
	 * Empty the cache
	 */
	public synchronized void dispose() {
		quadTreeMap.clear();
	}

	/**
	 * Update the surface color for all elements in the cache
	 * 
	 * @param rgba
	 */
	public synchronized void updateSurfaceColor(float[] rgba) {
		Object[] key = new Object[quadTreeMap.size()];
		key = quadTreeMap.keySet().toArray(key);
		for (int i = 0; i < key.length; ++i) {
			QuadTree item = quadTreeMap.get(key[i]);
			item.getMesh().updateSurfaceColor(rgba);
		}
	}
}
