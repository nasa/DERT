package gov.nasa.arc.dert.landscape;

import gov.nasa.arc.dert.io.TileSource;
import gov.nasa.arc.dert.util.StringUtil;

import java.awt.Color;

/**
 * Provides a class for handling a Range Map.
 *
 */
public class RangeMap extends Terrain {

	/**
	 * Constructor
	 * 
	 * @param source
	 *            source of tiles
	 * @param layerManager
	 *            manager of layers
	 * @param surfaceColor
	 *            surface color
	 */
	public RangeMap(TileSource source, LayerManager layerManager, Color surfaceColor) {
		super(StringUtil.getLabelFromFilePath(source.getPath()), source, layerManager, surfaceColor);
		if (layerManager.getGridCellSize() == 0)
			layerManager.setGridCellSize(Landscape.defaultCellSize);
	}
}
