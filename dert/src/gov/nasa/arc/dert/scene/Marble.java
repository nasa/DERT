package gov.nasa.arc.dert.scene;

import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.scenegraph.DirectionArrow;
import gov.nasa.arc.dert.scenegraph.FigureMarker;
import gov.nasa.arc.dert.scenegraph.Shape.ShapeType;
import gov.nasa.arc.dert.state.MapElementState;
import gov.nasa.arc.dert.state.MapElementState.Type;
import gov.nasa.arc.dert.state.MarbleState;
import gov.nasa.arc.dert.terrain.QuadTree;
import gov.nasa.arc.dert.util.SpatialUtil;
import gov.nasa.arc.dert.viewpoint.BasicCamera;

import javax.swing.Icon;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;

/**
 * A green sphere that marks the location where the user last picked. It also
 * displays the surface normal at the point and the direction to the light.
 *
 */
public class Marble extends FigureMarker implements MapElement {

	// the direction arrow to the light
	private DirectionArrow solarDirectionArrow;

	// the MapElement state
	private MarbleState state;

	/**
	 * Constructor
	 * 
	 * @param state
	 */
	public Marble(MarbleState state) {
		super("Marble", null, state.size, 0, state.color, state.labelVisible, true, false);
		setShape(ShapeType.sphere);
		surfaceNormalArrow.getSceneHints().setCullHint(CullHint.Never);
		getSceneHints().setAllPickingHints(false);
		solarDirectionArrow = new DirectionArrow("Direction to Sol", (float)(state.size*2), ColorRGBA.YELLOW);
		contents.attachChild(solarDirectionArrow);
		contents.detachChild(billboard);
		billboard = null;
		setVisible(state.visible);
		this.state = state;
		state.setMapElement(this);
	}

	/**
	 * Get the map element state
	 */
	@Override
	public MapElementState getState() {
		return (state);
	}

	/**
	 * Get the surface normal
	 */
	@Override
	public ReadOnlyVector3 getNormal() {
		return (surfaceNormal);
	}

	@Override
	public void setNormal(ReadOnlyVector3 normal) {
		if (normal != null) {
			super.setNormal(normal);
			state.updateText();
		}
	}

	/**
	 * Set the direction to the light
	 * 
	 * @param direction
	 */
	public void setSolarDirection(ReadOnlyVector3 direction) {
		solarDirectionArrow.setDirection(direction);
		state.updateText();
	}

	/**
	 * Move the marble
	 * 
	 * @param pos
	 * @param normal
	 * @param camera
	 */
	public void update(ReadOnlyVector3 pos, ReadOnlyVector3 normal, BasicCamera camera) {
		if (normal == null) {
			Vector3 store = new Vector3();
			Landscape.getInstance().getNormal(pos.getX(), pos.getY(), store);
			setNormal(store);
		} else {
			setNormal(normal);
		}
		setTranslation(pos);
		location.set(pos);
		if (camera != null) {
			update(camera);
		}
		state.updateText();
		updateGeometricState(0);
	}
	
	public void landscapeChanged(QuadTree quadTree) {
		updateElevation(quadTree);
	}

	/**
	 * Get the point and distance to seek to
	 */
	@Override
	public double getSeekPointAndDistance(Vector3 point) {
		point.set(getTranslation());
		double distance = Math.max(2*getSize(), 50*Landscape.getInstance().getPixelWidth());
		return (distance);
	}

	/**
	 * Get Map Element type
	 */
	@Override
	public Type getType() {
		return (Type.Marble);
	}

	/**
	 * Set the surface normal
	 * 
	 * @param show
	 */
	public void setSurfaceNormalVisible(boolean show) {
		surfaceNormalArrow.getSceneHints().setCullHint(show ? CullHint.Inherit : CullHint.Always);
		markDirty(DirtyType.RenderState);
	}

	/**
	 * Find out if surface normal is visible
	 * 
	 * @return
	 */
	public boolean isSurfaceNormalVisible() {
		return (SpatialUtil.isDisplayed(surfaceNormalArrow));
	}
	
	public Icon getIcon() {
		return(null);
	}

}
