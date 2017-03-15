package gov.nasa.arc.dert.scenegraph;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.view.world.GroundEdit;
import gov.nasa.arc.dert.view.world.MoveEdit;

import java.util.ArrayList;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Node;

/**
 * Abstract base class for objects that can be moved along the terrain.
 *
 */
public abstract class Movable extends Node {

	private boolean locked, inMotion;
	protected double zOff;
	private ArrayList<MotionListener> listeners;
	protected Vector3 location, workVec;

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public Movable(String name) {
		super(name);
		location = new Vector3();
		workVec = new Vector3();
		listeners = new ArrayList<MotionListener>();
	}

	/**
	 * Determine mobility
	 * 
	 * @return
	 */
	public boolean isLocked() {
		return (locked);
	}

	/**
	 * Set mobility
	 * 
	 * @param pinned
	 */
	public void setLocked(boolean locked) {
		this.locked = locked;
		if (locked) {
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
			updateListeners();
		}
	}

	/**
	 * Notify listeners
	 */
	public void updateListeners() {
		ReadOnlyVector3 position = getTranslation();
		for (int i = 0; i < listeners.size(); ++i) {
			listeners.get(i).move(this, position);
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

	/**
	 * Set the location
	 * 
	 * @param i
	 * @param p
	 */
	public void setLocation(double x, double y, double z, boolean doEdit) {
		if (doEdit)
			Dert.getMainWindow().getUndoHandler().addEdit(new MoveEdit(this, new Vector3(location)));
		location.set(x, y, z);
		setTranslation(x, y, z+zOff);
		updateListeners();
	}
	
	public void setLocation(ReadOnlyVector3 loc, boolean doEdit) {
		setLocation(loc.getX(), loc.getY(), loc.getZ(), doEdit);
	}
	
	public ReadOnlyVector3 getLocation() {
		return(location);
	}
	
	public void setZOffset(double z, boolean doTrans) {
		zOff = z;
		if (doTrans) {
			setTranslation(location.getX(), location.getY(), location.getZ()+zOff);
			updateListeners();
		}
	}
	
	public double getZOffset() {
		return(zOff);
	}
	
	public GroundEdit ground() {
		GroundEdit ge = new GroundEdit(this, zOff);
		setZOffset(0, true);
		return(ge);
	}

	/**
	 * Get the location in planetary coordinates
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getLocationInWorld() {
		updateWorldTransform(false);
		workVec.set(getWorldTranslation());
		Landscape.getInstance().localToWorldCoordinate(workVec);
		return (workVec);
	}
}
