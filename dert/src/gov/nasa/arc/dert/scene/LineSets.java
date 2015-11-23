package gov.nasa.arc.dert.scene;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.landscape.QuadTree;
import gov.nasa.arc.dert.scenegraph.GroupNode;
import gov.nasa.arc.dert.state.LineSetState;
import gov.nasa.arc.dert.view.Console;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;

import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Provides a set of LineSet map elements.
 *
 */
public class LineSets extends GroupNode {

	// List of LineSets
	private ArrayList<LineSetState> lineSetList;

	// Thread service for updating
	private ExecutorService executor;

	/**
	 * Constructor
	 * 
	 * @param lineSetList
	 */
	public LineSets(ArrayList<LineSetState> lineSetList) {
		super("LineSets");
		this.lineSetList = lineSetList;
		executor = Executors.newFixedThreadPool(5);
	}

	/**
	 * Initialize this LineSets object
	 */
	public void initialize() {
		TextureState ts = new TextureState();
		ts.setEnabled(false);
		setRenderState(ts);
		for (int i = 0; i < lineSetList.size(); ++i) {
			LineSetState state = lineSetList.get(i);
			addLineSet(state, false);
		}
	}

	/**
	 * The landscape has changed, update the elevation of all the LineSets
	 * 
	 * @param quadTree
	 */
	public void landscapeChanged(final QuadTree quadTree) {
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			final Spatial child = getChild(i);
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					Thread.yield();
					boolean modified = ((LineSet) child).updateElevation(quadTree);
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
		}
	}

	/**
	 * Add a LineSet to the list
	 * 
	 * @param state
	 * @param update
	 * @return
	 */
	public LineSet addLineSet(LineSetState state, boolean update) {
		try {
			LineSet lineSet = new LineSet(state);
			Spatial spatial = lineSet;
			attachChild(spatial);
			if (update) {
				spatial.updateGeometricState(0, true);
			}
			return (lineSet);
		} catch (Exception e) {
			Console.getInstance().println(e.getMessage());
			JOptionPane.showMessageDialog(Dert.getMainWindow(), e.getMessage());
			return (null);
		}
	}

	/**
	 * Show all LineSets
	 * 
	 * @param visible
	 */
	public void setAllVisible(boolean visible) {
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			((LineSet) getChild(i)).setVisible(visible);
		}
	}

	/**
	 * Save defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		LineSet.saveDefaultsToProperties(properties);
	}

}
