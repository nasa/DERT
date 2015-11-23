package gov.nasa.arc.dert.scene;

import gov.nasa.arc.dert.landscape.QuadTree;
import gov.nasa.arc.dert.scenegraph.GroupNode;
import gov.nasa.arc.dert.state.MapElementState;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Provides a group of MapElement objects.
 *
 */
public class MapElements extends GroupNode {

	// List of MapElement states
	private ArrayList<MapElementState> mapElementStateList;

	// Thread service for updates
	private ExecutorService executor;

	/**
	 * Constructor
	 * 
	 * @param stateList
	 */
	public MapElements(ArrayList<MapElementState> stateList) {
		super("Features");
		this.mapElementStateList = stateList;
		executor = Executors.newFixedThreadPool(5);
	}

	/**
	 * Initialize this MapElements object
	 */
	public void initialize() {
		// Turn off texturing
		TextureState ts = new TextureState();
		ts.setEnabled(false);
		setRenderState(ts);

		// add all map elements to scene graph
		for (int i = 0; i < mapElementStateList.size(); ++i) {
			MapElementState state = mapElementStateList.get(i);
			attachChild((Spatial) state.getMapElement());
		}
	}

	/**
	 * The landscape has changed, update Z coordinates
	 * 
	 * @param quadTree
	 */
	public void landscapeChanged(final QuadTree quadTree) {
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			final Spatial child = getChild(i);
			if (child instanceof MapElement) {
				if (child instanceof LineSet) {
					Runnable runnable = new Runnable() {
						@Override
						public void run() {
							Thread.yield();
							boolean modified = ((MapElement) child).updateElevation(quadTree);
							if (modified) {
								EventQueue.invokeLater(new Runnable() {
									@Override
									public void run() {
										child.markDirty(DirtyType.Bounding);
									}
								});
							}
						}
					};
					executor.execute(runnable);
				} else {
					((MapElement) child).updateElevation(quadTree);
				}
			}
		}
	}

}
