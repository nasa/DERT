package gov.nasa.arc.dert.scenegraph;

import java.util.ArrayList;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Node;

/**
 * Abstract base class for objects that can be moved along the terrain.
 *
 */
public abstract class Movable extends Node {

	private boolean pinned, inMotion;
	private ArrayList<MotionListener> listeners;

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public Movable(String name) {
		super(name);
		listeners = new ArrayList<MotionListener>();
	}

	/**
	 * Determine mobility
	 * 
	 * @return
	 */
	public boolean isPinned() {
		return (pinned);
	}

	/**
	 * Set mobility
	 * 
	 * @param pinned
	 */
	public void setPinned(boolean pinned) {
		this.pinned = pinned;
		if (pinned) {
			inMotion = false;
		}
	}

	/**
	 * Determine if moving
	 * 
	 * @return
	 */
	public boolean isInMotion() {
		return (inMotion);
	}

	/**
	 * Set moving
	 * 
	 * @param inMotion
	 * @param pickPosition
	 */
	public void setInMotion(boolean inMotion, ReadOnlyVector3 pickPosition) {
		this.inMotion = inMotion;
		enableHighlight(inMotion);
		return;
	}

	protected abstract void enableHighlight(boolean enable);

	/**
	 * Add listener
	 * 
	 * @param mol
	 */
	public void addMotionListener(MotionListener mol) {
		listeners.add(mol);
	}

	/**
	 * Remove listener
	 * 
	 * @param mol
	 */
	public void removeMotionListener(MotionListener mol) {
		listeners.remove(mol);
	}

	/**
	 * Notify listeners
	 */
	public void notifyListeners() {
		if (inMotion) {
			ReadOnlyVector3 position = getTranslation();
			for (int i = 0; i < listeners.size(); ++i) {
				listeners.get(i).move(this, position);
			}
		}
	}

	/**
	 * Get the radius of this movable.
	 * 
	 * @return
	 */
	public double getRadius() {
		updateWorldTransform(true);
		updateWorldBound(true);
		BoundingVolume wb = getWorldBound();
		if (wb == null) {
			return (1);
		}
		ReadOnlyVector3 s = getWorldScale();
		return (wb.getRadius() / s.getX());
	}
}
