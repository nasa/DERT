package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.tool.Grid;
import gov.nasa.arc.dert.scene.tool.Plane;
import gov.nasa.arc.dert.util.ColorMap;
import gov.nasa.arc.dert.view.View;
import gov.nasa.arc.dert.view.contour.ContourScenePanel;
import gov.nasa.arc.dert.view.contour.ContourView;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides a state object for the Plane tool.
 *
 */
public class PlaneState extends ToolState {

	// Show the triangle
	public boolean triangleVisible;

	// Three points of the triangle
	public Vector3 p0, p1, p2;

	// Dimensional scales
	public double lengthScale, widthScale;

	// Color map
	public String colorMapName = Plane.defaultColorMap;
	public boolean gradient;
	public double minimum = 0;
	public double maximum = 100;

	// The display panel for the elevation difference map
	protected transient ContourScenePanel panel;

	/**
	 * Constructor
	 * 
	 * @param position
	 */
	public PlaneState(ReadOnlyVector3 position) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration()
			.incrementMapElementCount(MapElementState.Type.Plane), MapElementState.Type.Plane, "Plane",
			Plane.defaultSize, Plane.defaultColor, Plane.defaultLabelVisible, Plane.defaultPinned, position);
		p0 = new Vector3(position);
		p1 = new Vector3(World.getInstance().getLandscape().getCenter());
		p1.subtractLocal(p0);
		p1.normalizeLocal();
		p2 = new Vector3(p1);
		p1.multiplyLocal(Grid.defaultCellSize / 2);
		p1.addLocal(p0);
		p1.setZ(World.getInstance().getLandscape().getZ(p1.getX(), p1.getY()));
		p2.multiplyLocal(Grid.defaultCellSize / 4);
		p2.addLocal(p0);
		p2.setZ(World.getInstance().getLandscape().getZ(p2.getX(), p2.getY()));

		triangleVisible = Plane.defaultTriangleVisible;
		lengthScale = Plane.defaultSize;
		widthScale = Plane.defaultSize;
		viewData = new ViewData(-1, -1, 550, 400, false);
	}

	@Override
	public void save() {
		super.save();
		if (mapElement != null) {
			Plane plane = (Plane) mapElement;
			p0 = new Vector3(plane.getPoint(0));
			p1 = new Vector3(plane.getPoint(1));
			p2 = new Vector3(plane.getPoint(2));
			triangleVisible = plane.isTriangleVisible();
			lengthScale = plane.getLengthScale();
			widthScale = plane.getWidthScale();
		}
		if (panel != null) {
			ColorMap colorMap = panel.getColorMap();
			colorMapName = colorMap.getName();
			gradient = colorMap.isGradient();
			minimum = colorMap.getMinimum();
			maximum = colorMap.getMaximum();
		}
	}

	@Override
	public void setView(View view) {
		super.setView(view);
		panel = ((ContourView) view).getContourScenePanel();
		panel.setDraw(true);
	}
	
	@Override
	public void createView() {
		setView(new ContourView(this));
		viewData.createWindow(Dert.getMainWindow(), name + " Elevation Difference Map", X_OFFSET, Y_OFFSET);
	}
}
