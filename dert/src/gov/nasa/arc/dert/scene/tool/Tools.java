package gov.nasa.arc.dert.scene.tool;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.landscape.QuadTree;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.scenegraph.GroupNode;
import gov.nasa.arc.dert.state.FieldCameraState;
import gov.nasa.arc.dert.state.GridState;
import gov.nasa.arc.dert.state.PathState;
import gov.nasa.arc.dert.state.PlaneState;
import gov.nasa.arc.dert.state.ProfileState;
import gov.nasa.arc.dert.state.ToolState;

import java.util.ArrayList;
import java.util.Properties;

import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyType;

/**
 * Provides a group of Tool map elements
 *
 */
public class Tools extends GroupNode {

	// List of tool states
	private ArrayList<ToolState> toolList;

	/**
	 * Constructor
	 * 
	 * @param toolList
	 */
	public Tools(ArrayList<ToolState> toolList) {
		super("Tools");
		this.toolList = toolList;
	}

	/**
	 * Create and add the tools to the scene graph
	 */
	public void initialize() {
		TextureState ts = new TextureState();
		ts.setEnabled(false);
		setRenderState(ts);
		for (int i = 0; i < toolList.size(); ++i) {
			ToolState state = toolList.get(i);
			addTool(state, false);
		}
	}

	/**
	 * The landscape changed, update tool Z coordinates
	 * 
	 * @param quadTree
	 */
	public void landscapeChanged(final QuadTree quadTree) {
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			final Spatial child = getChild(i);
			if (child instanceof Tool) {
				((Tool) child).updateElevation(quadTree);
			}
		}
	}

	/**
	 * Get all tools of type FieldCamera
	 * 
	 * @return
	 */
	public ArrayList<FieldCamera> getFieldCameras() {
		ArrayList<FieldCamera> list = new ArrayList<FieldCamera>();
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			Spatial child = getChild(i);
			if (child instanceof FieldCamera) {
				list.add((FieldCamera) child);
			}
		}
		return (list);
	}

	/**
	 * Add a tool
	 * 
	 * @param state
	 * @param update
	 * @return
	 */
	public Tool addTool(ToolState state, boolean update) {
		Tool tool = null;
		switch (state.mapElementType) {
		case Figure:
		case Placemark:
		case Billboard:
			break;
		case Path:
			tool = new Path((PathState) state);
			break;
		case Plane:
			tool = new Plane((PlaneState) state);
			break;
		case Profile:
			tool = new Profile((ProfileState) state);
			break;
		case FieldCamera:
			tool = new FieldCamera((FieldCameraState) state);
			break;
		case CartesianGrid:
			tool = new CartesianGrid((GridState) state);
			break;
		case RadialGrid:
			tool = new RadialGrid((GridState) state);
			break;
		case Waypoint:
		case Marble:
		case LineSet:
			break;
		}

		if (tool != null) {
			Spatial spatial = (Spatial) tool;
			attachChild(spatial);
			if (update) {
				spatial.updateGeometricState(0, true);
				tool.update(Dert.getWorldView().getViewpointNode().getBasicCamera());
			}
			if ((tool instanceof FieldCamera) || (tool instanceof Profile)) {
				state.open();
				spatial.markDirty(DirtyType.Transform);
			}
		}
		return (tool);
	}

	/**
	 * Show all tools
	 * 
	 * @param visible
	 */
	public void setAllVisible(boolean visible) {
		for (int i = 0; i < getNumberOfChildren(); ++i) {
			((Tool) getChild(i)).setVisible(visible);
		}
	}

	/**
	 * Get a list of tools that are Paths
	 * 
	 * @return
	 */
	public ArrayList<Path> getFlyablePaths() {
		int n = getNumberOfChildren();
		ArrayList<Path> list = new ArrayList<Path>();
		for (int i = 0; i < n; ++i) {
			Spatial child = getChild(i);
			if (child instanceof Path) {
				Path path = (Path)child;
				if (path.getNumberOfPoints() > 1)
					list.add(path);
			}
		}
		return (list);
	}

	/**
	 * Save defaults
	 * 
	 * @param properties
	 */
	public static void saveDefaultsToProperties(Properties properties) {
		FieldCamera.saveDefaultsToProperties(properties);
		Path.saveDefaultsToProperties(properties);
		Plane.saveDefaultsToProperties(properties);
		CartesianGrid.saveDefaultsToProperties(properties);
		RadialGrid.saveDefaultsToProperties(properties);
		Profile.saveDefaultsToProperties(properties);
	}

}
