package gov.nasa.arc.dert.render;

import gov.nasa.arc.dert.scene.Marble;
import gov.nasa.arc.dert.scenegraph.BillboardMarker;
import gov.nasa.arc.dert.scenegraph.GroupNode;
import gov.nasa.arc.dert.scenegraph.Movable;

import java.util.concurrent.atomic.AtomicBoolean;

import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyEventListener;
import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Provides an Ardor3D DirtyEventListener for the World.
 *
 */
public class DirtyEventHandler implements DirtyEventListener {

	// Node to listen on
	private GroupNode rootNode;

	// Changed flag
	private AtomicBoolean changed;

	/**
	 * Constructor
	 * 
	 * @param rootNode
	 */
	public DirtyEventHandler(GroupNode rootNode) {
		this.rootNode = rootNode;
		changed = new AtomicBoolean();
	}

	/**
	 * A child of the root node changed
	 */
	@Override
	public boolean spatialDirty(Spatial spatial, DirtyType type) {
		if (spatial == null) {
			spatial = rootNode;
		}
		changed.set(false);
		switch (type) {
		case Attached:
			rootNode.updateGeometricState(0, true);
			changed.set(!(spatial instanceof BillboardMarker));
			break;
		case Detached:
			rootNode.updateGeometricState(0, true);
			changed.set(!(spatial instanceof BillboardMarker));
			break;
		case Bounding:
			rootNode.updateGeometricState(0, true);
			break;
		case RenderState:
			rootNode.updateGeometricState(0, true);
			changed.set(!(spatial instanceof BillboardMarker));
			break;
		case Transform:
			spatial.updateWorldTransform(true);
			if (spatial instanceof Movable) {
				((Movable) spatial).notifyListeners();
			}
			changed.set(!(spatial instanceof BillboardMarker) && !(spatial instanceof Marble));
			break;
		case Destroyed:
			break;
		}
		return (false);
	}

	@Override
	public boolean spatialClean(Spatial spatial, DirtyType type) {
		return (false);
	}

	public final boolean getChanged() {
		return (changed.get());
	}

	public final void setChanged(boolean value) {
		changed.set(value);
	}

}
