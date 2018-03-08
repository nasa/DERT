package gov.nasa.arc.dert.landscape.io;

import gov.nasa.arc.dert.landscape.io.QuadTreeTile.DataType;
import gov.nasa.arc.dert.landscape.quadtree.QuadKey;

import java.util.Properties;

/**
 * Interface to source of landscape tiles.
 *
 */
public interface TileSource {

	/**
	 * Connect to this source with a username and password.
	 * 
	 * @param location
	 * @param userName
	 * @param password
	 * @return true if successful
	 */
	public boolean connect(String location, String userName, String password);

	/**
	 * Get the properties from a layer.properties file.
	 * 
	 * @param layerName
	 *            the layer name
	 * @return the properties
	 */
	public Properties getProperties(String layerName);

	/**
	 * Given the tile key, return if it exists.
	 * 
	 * @param id
	 * @return
	 */
	public boolean tileExists(String key);

	/**
	 * Get a tile.
	 * 
	 * @param layerName
	 * @param id
	 * @param dataType
	 * @return the tile
	 */
	public QuadTreeTile getTile(String layerName, QuadKey id, DataType dataType);

	/**
	 * Get information about all the layers in the landscape
	 * 
	 * @return
	 */
	public String[][] getLayerInfo();

	/**
	 * Given a coordinate, get the id of the tile at the highest level
	 * 
	 * @param x
	 *            , y the coordinate
	 * @param worldWidth
	 *            , worldLength the physical dimensions of the raster
	 * @return
	 */
	public QuadKey getKey(double x, double y, double worldWidth, double worldLength);

	/**
	 * Given a coordinate and level, get the id of the tile at that level
	 * 
	 * @param x
	 *            , y the coordinate
	 * @param worldWidth
	 *            , worldLength the physical dimensions of the raster
	 * @return
	 */
	public QuadKey getKey(double x, double y, double worldWidth, double worldLength, int lvl);

	/**
	 * Get the landscape path
	 * 
	 * @return
	 */
	public String getLandscapePath();

}
