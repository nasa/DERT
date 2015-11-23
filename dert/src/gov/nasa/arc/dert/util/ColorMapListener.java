package gov.nasa.arc.dert.util;

/**
 * Provides listener methods for color map changes.
 *
 */
public interface ColorMapListener {

	public void mapChanged(ColorMap colorMap);

	public void rangeChanged(ColorMap colorMap);

}
