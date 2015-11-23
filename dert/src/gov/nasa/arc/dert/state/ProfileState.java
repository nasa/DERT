package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.Grid;
import gov.nasa.arc.dert.scene.tool.Profile;
import gov.nasa.arc.dert.view.View;
import gov.nasa.arc.dert.view.graph.Graph;
import gov.nasa.arc.dert.view.graph.GraphView;

import java.awt.Color;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides a state object for the Profile tool.
 *
 */
public class ProfileState extends ToolState {

	// Profile end points
	public Vector3 p0, p1;

	// Graph
	protected transient Graph graph;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public ProfileState(ReadOnlyVector3 position) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration()
			.incrementMapElementCount(MapElementState.Type.Profile), MapElementState.Type.Profile, "Profile",
			Profile.defaultSize, Profile.defaultColor, Profile.defaultLabelVisible, Profile.defaultPinned, position);
		viewData = new ViewData(-1, -1, -1, 300, false);
		viewData.setVisible(true);
		p0 = new Vector3(this.position);
		p1 = new Vector3(World.getInstance().getLandscape().getCenter());
		p1.subtractLocal(p0);
		p1.normalizeLocal();
		p1.multiplyLocal(Grid.defaultCellSize / 2);
		p1.addLocal(p0);
		p1.setZ(World.getInstance().getLandscape().getZ(p1.getX(), p1.getY()));
	}

	@Override
	public void save() {
		super.save();
		if (mapElement != null) {
			Profile profile = (Profile) mapElement;
			p0 = new Vector3(profile.getEndpointA());
			p1 = new Vector3(profile.getEndpointB());
		}
	}

	/**
	 * Set the graph data
	 * 
	 * @param vertex
	 * @param vertexCount
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 */
	public void setData(float[] vertex, int vertexCount, float xMin, float xMax, float yMin, float yMax) {
		if (graph != null) {
			((GraphView)viewData.view).setData(vertex, vertexCount, xMin, xMax, yMin, yMax);
		}
	}

	/**
	 * Set the graph color
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		if (graph != null) {
			((GraphView)viewData.view).setColor(color);
		}
	}

	/**
	 * Save data as CSV formatted file
	 * 
	 * @param filename
	 */
	public void saveAsCSV(String filename) {
		if (graph != null) {
			graph.saveAsCsv(filename, new String[] { "Distance", "Elevation" });
		}
	}

	/**
	 * Get the graph
	 * 
	 * @return
	 */
	public Graph getGraph() {
		if (graph != null) {
			return (graph);
		}
		try {
			color = getMapElement().getColor();
			graph = new Graph(100000, color);
			return (graph);
		} catch (Exception e) {
			System.out.println("Unable to load graph, see log.");
			e.printStackTrace();
			return (null);
		}
	}

	@Override
	public void setView(View view) {
		super.setView(view);
		((Profile) mapElement).updateGraph();
	}
	
	protected void createView() {
		setView(new GraphView(this));
		viewData.createWindow(Dert.getMainWindow(), name + " Graph", X_OFFSET, Y_OFFSET);
	}

}
