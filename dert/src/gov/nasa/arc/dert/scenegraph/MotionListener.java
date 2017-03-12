package gov.nasa.arc.dert.scenegraph;

import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides interface for listeners to Movable object motion.
 *
 */
public interface MotionListener {
	public void move(Movable mo, ReadOnlyVector3 position);
	public void pin(Movable mo, boolean value);
}
