package gov.nasa.arc.dert.landscape;

import java.util.HashMap;

public class QuadTreeCache {

	// The maximum amount of memory for the cache (in bytes)
	public static long MAX_CACHE_MEMORY = 400000000l;
	public static int MAX_CLEANUP_COUNT = 100;

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
//		System.err.println("QuadTreeCache.getQuadTree ."+key+".");
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
//		System.err.println("QuadTreeCache.putQuadTree ."+key+".");
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
		Object oldestKey = null;
		QuadTree oldestItem = null;
		int k = 1;
		for (int i=0; i<key.length; ++i) {
			oldestKey = key[i];
			oldestItem = quadTreeMap.get(oldestKey);
			if (!oldestItem.inUse) {
				k = i+1;
				break;
			}
		}
		if (oldestItem == null)
			throw new IllegalStateException("Unable to clean up quad tree cache.  All tiles are in use. Increase maximum cache size.");
		for (int i = k; i < key.length; ++i) {
			QuadTree item = quadTreeMap.get(key[i]);
			if (!item.inUse) {
				if (item.timestamp < oldestItem.timestamp) {
					oldestKey = key[i];
					oldestItem = item;
				}
			}
		}
		if (oldestItem.inUse)
			throw new IllegalStateException("Unable to clean up quad tree cache.  All tiles are in use. Increase maximum cache size.");
		// remove the oldest item if not in use
		QuadTree qt = quadTreeMap.remove(oldestKey);
		qt.dispose();
		qt = null;
		cleanupCount++;
		if (cleanupCount == MAX_CLEANUP_COUNT) {
			System.gc();
			cleanupCount = 0;
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
