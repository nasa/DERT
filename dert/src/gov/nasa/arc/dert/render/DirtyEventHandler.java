package gov.nasa.arc.dert.render;

import gov.nasa.arc.dert.scenegraph.GroupNode;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.terrain.QuadTreeMesh;

import java.util.concurrent.atomic.AtomicBoolean;

import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyEventListener;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.extension.BillboardNode;

/**
 * Provides an Ardor3D DirtyEventListener for the World.
 *
 */
public class DirtyEventHandler implements DirtyEventListener {

	// Node to listen on
	private GroupNode rootNode;

	// Changed flag
	public final AtomicBoolean changed;
	public final AtomicBoolean terrainChanged;

	/**
	 * Constructor
	 * 
	 * @param rootNode
	 */
	public DirtyEventHandler(GroupNode rootNode) {
		this.rootNode = rootNode;
		changed = new AtomicBoolean();
		terrainChanged = new AtomicBoolean();
	}

	/**
	 * A child of the root node changed
	 */
	@Override
	public boolean spatialDirty(Spatial spatial, DirtyType type) {
//		System.err.println("DirtyEventHandler.spatialDirty "+type+" "+spatial);
		if (spatial == null) {
			spatial = rootNode;
		}
		switch (type) {
		case Attached:
//			rootNode.updateGeometricState(0, true);
			changed.set(true);
			terrainChanged.set((spatial instanceof QuadTreeMesh) || terrainChanged.get());
			break;
		case Detached:
//			rootNode.updateGeometricState(0, true);
			changed.set(true);
			terrainChanged.set((spatial instanceof QuadTreeMesh) || terrainChanged.get());
			break;
		case Bounding:
//			rootNode.updateGeometricState(0, true);
			break;
		case RenderState:
//			rootNode.updateGeometricState(0, true);
			changed.set(!(spatial instanceof BillboardNode) || changed.get());
			break;
		case Transform:
//			rootNode.updateGeometricState(0, true);
			if (spatial instanceof Movable) {
				((Movable) spatial).notifyListeners();
			}
			changed.set(true);
			terrainChanged.set((spatial instanceof QuadTreeMesh) || terrainChanged.get());
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

}
