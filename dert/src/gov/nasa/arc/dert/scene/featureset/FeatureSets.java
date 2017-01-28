package gov.nasa.arc.dert.scene.featureset;

import gov.nasa.arc.dert.landscape.QuadTree;
import gov.nasa.arc.dert.scenegraph.GroupNode;
import gov.nasa.arc.dert.state.FeatureSetState;
import gov.nasa.arc.dert.view.Console;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JTextField;

import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;

/**
 * Provides a set of FeatureSet map elements.
 *
 */
public class FeatureSets extends GroupNode {

	// List of FeatureSets
	private ArrayList<FeatureSetState> featureSetList;

	// Thread service for updating
	private ExecutorService executor;
	
	private ZBufferState zBufferState;

	/**
	 * Constructor
	 * 
	 * @param featureSetList
	 */
	public FeatureSets(ArrayList<FeatureSetState> featureSetList) {
		super("FeatureSets");
		this.featureSetList = featureSetList;
		executor = Executors.newFixedThreadPool(5);
	}

	/**
	 * Initialize this FeatureSets object
	 */
	public void initialize() {
		TextureState ts = new TextureState();
		ts.setEnabled(false);
		setRenderState(ts);
		getSceneHints().setCullHint(CullHint.Dynamic);
		
		for (int i = 0; i < featureSetList.size(); ++i) {
			FeatureSetState state = featureSetList.get(i);
			addFeatureSet(state, false, null);
		}

		zBufferState = new ZBufferState();
		zBufferState.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
		zBufferState.setEnabled(true);
		setRenderState(zBufferState);
	}

	/**
	 * The landscape has changed, update the elevation of all the FeatureSets
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
					boolean modified = ((FeatureSet) child).updateElevation(quadTree);
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
	 * Add a FeatureSet to the list
	 * 
	 * @param state
	 * @param update
	 * @return
	 */
	public FeatureSet addFeatureSet(FeatureSetState state, boolean update, JTextField msgField) {
		try {
			FeatureSet featureSet = new FeatureSet(state);
			attachChild(featureSet);
			markDirty(DirtyType.RenderState);
			if (update) {
				featureSet.updateGeometricState(0, true);
			}
			return (featureSet);
		} catch (Exception e) {
			Console.println(e.getMessage());
			if (msgField != null)
				msgField.setText(e.getMessage());
			e.printStackTrace();
			return (null);
		}
	}

	/**
	 * Show all FeatureSets
	 * 
	 * @param visible
	 */
	public void setAllVisible(boolean visible) {
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			((FeatureSet) getChild(i)).setVisible(visible);
		}
	}
	
	public void setOnTop(boolean onTop) {
		zBufferState.setEnabled(!onTop);
	}
	
	public boolean isOnTop() {
		return(!zBufferState.isEnabled());
	}

	/**
	 * Save defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		FeatureSet.saveDefaultsToProperties(properties);
	}

}
