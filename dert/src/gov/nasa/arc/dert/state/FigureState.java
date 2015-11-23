package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.landmark.Figure;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * Provides a state object for the Figure.
 *
 */
public class FigureState extends LandmarkState {

	// Surface normal
	public Vector3 normal;

	// Orientation
	public double azimuth, tilt;

	// Type of shape
	public ShapeType shape;

	// Options
	public boolean showNormal, fixedSize;

	/**
	 * Constructor
	 * 
	 * @param position
	 * @param normal
	 */
	public FigureState(ReadOnlyVector3 position, ReadOnlyVector3 normal) {
		super(ConfigurationManager.getInstance().getCurrentConfiguration()
			.incrementMapElementCount(MapElementState.Type.Figure), MapElementState.Type.Figure, "Figure",
			Figure.defaultSize, Figure.defaultColor, Figure.defaultLabelVisible, Figure.defaultPinned, position);
		this.normal = new Vector3(normal);
		azimuth = Figure.defaultAzimuth;
		tilt = Figure.defaultTilt;
		shape = Figure.defaultShapeType;
		showNormal = Figure.defaultSurfaceNormalVisible;
		fixedSize = Figure.defaultFixedSize;
	}

	@Override
	public void save() {
		super.save();
		if (mapElement != null) {
			Figure figure = (Figure) mapElement;
			normal = new Vector3(figure.getNormal());
			azimuth = figure.getAzimuth();
			tilt = figure.getTilt();
			shape = figure.getShapeType();
			showNormal = figure.isSurfaceNormalVisible();
			fixedSize = figure.isFixedSize();
		}
	}
}
