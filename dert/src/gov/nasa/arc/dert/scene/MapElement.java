package gov.nasa.arc.dert.scene;

import gov.nasa.arc.dert.landscape.QuadTree;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;

import java.awt.Color;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Interface for map elements. These include objects that are not part of the
 * landscape itself. Those are landmarks (placemarks, figures, and billboards),
 * tools (path, plane, camera, grids, profile), and line sets.
 *
 */
public interface MapElement {

	public String getName();

	public MapElementState getState();

	public void setName(String name);

	public boolean isVisible();

	public void setVisible(boolean visible);

	public Type getType();

	public boolean isPinned();

	public void setPinned(boolean pinned);

	public boolean updateElevation(QuadTree quadTree);

	public void setVerticalExaggeration(double vertExag, double oldVertExag, double minZ);

	public double getSeekPointAndDistance(Vector3 point);

	public double getSize();

	public Color getColor();

	public boolean isLabelVisible();

	public void setLabelVisible(boolean visible);

	public ReadOnlyVector3 getLocation();

}
