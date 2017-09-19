package gov.nasa.arc.dert.io;

import gov.nasa.arc.dert.landscape.QuadTreeTile;
import gov.nasa.arc.dert.raster.RasterFile.DataType;

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
	 * Given the tile id, return if it exists.
	 * 
	 * @param id
	 * @return
	 */
	public boolean tileExists(String id);

	/**
	 * Get a tile.
	 * 
	 * @param layerName
	 * @param id
	 * @param dataType
	 * @return the tile
	 */
	public QuadTreeTile getTile(String layerName, String id, DataType dataType);

	// public byte[] getTileBytes(String layerName, String id, byte[] bytes);

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
	public String getKey(double x, double y, double worldWidth, double worldLength);

	/**
	 * Given a coordinate and level, get the id of the tile at that level
	 * 
	 * @param x
	 *            , y the coordinate
	 * @param worldWidth
	 *            , worldLength the physical dimensions of the raster
	 * @return
	 */
	public String getKey(double x, double y, double worldWidth, double worldLength, int lvl);

	// public String getMaxLevel(String id);

	/**
	 * Get the landscape path
	 * 
	 * @return
	 */
	public String getPath();

}
