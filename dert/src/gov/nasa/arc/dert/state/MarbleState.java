package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.scene.Marble;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.util.MathUtil;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.TextView;
import gov.nasa.arc.dert.view.View;

import java.awt.Color;

import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Spatial;

/**
 * A state object for the green Marble.
 *
 */
public class MarbleState extends MapElementState {

	public Vector3 position;

	private transient Vector2 work;

	/**
	 * Constructor
	 */
	public MarbleState() {
		super(0, MapElementState.Type.Marble, "Marble");
		position = new Vector3();
		name = "Marble";
		viewData = new ViewData(-1, -1, 400, 200, true);
		labelVisible = false;
		color = Color.green;
		size = 0.5;
	}

	@Override
	public void save() {
		super.save();
		if (mapElement != null) {
			position = new Vector3(((Spatial) mapElement).getTranslation());
		}
	}

	@Override
	public void createView() {
		setView(new TextView(this, false));
		viewData.createWindow(Dert.getMainWindow(), "Marble Info", X_OFFSET, Y_OFFSET);
		updateText();
	}

	/**
	 * Update the data displayed in the Marble view
	 */
	public void updateText() {
		Dert.getMainWindow().updateMarbleLocationField();

		View view = viewData.getView();
		if (view == null) {
			return;
		}

		if (work == null) {
			work = new Vector2();
		}

		Marble marble = (Marble) mapElement;
		Vector3 loc = new Vector3(marble.getWorldTranslation());
		World world = World.getInstance();
		world.getLandscape().localToWorldCoordinate(loc);
		String str = "Location (meters): " + StringUtil.format(loc) + "\n";
		world.getLandscape().worldToSphericalCoordinate(loc);
		str += "Longitude: " + StringUtil.format(loc.getX()) + StringUtil.DEGREE + "\n";
		str += "Latitude: " + StringUtil.format(loc.getY()) + StringUtil.DEGREE + "\n";
		ReadOnlyVector3 normal = marble.getNormal();
		str += "Surface Normal Vector: " + StringUtil.format(normal) + "\n";
		ReadOnlyVector3 dir = marble.getSolarDirection();
		str += "Solar Direction Vector: " + StringUtil.format(dir) + "\n";
		str += "Elevation (meters): " + StringUtil.format(loc.getZ()) + "\n";
		str += "Slope: " + StringUtil.format(MathUtil.getSlopeFromNormal(normal)) + StringUtil.DEGREE + "\n";
		str += "Aspect: " + StringUtil.format(MathUtil.getAspectFromNormal(normal, work)) + StringUtil.DEGREE + "\n";
		((TextView) view).setText(str);
	}

}
