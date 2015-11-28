package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scenegraph.MotionListener;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.ui.TextDialog;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.View;

import java.awt.Color;
import java.awt.Window;

import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;

/**
 * Base class for map element state objects.
 *
 */
public abstract class MapElementState extends State {

	// Types of map elements
	public static enum Type {
		Placemark, Figure, Billboard, LineSet, Path, Plane, CartesianGrid, RadialGrid, Profile, FieldCamera, Waypoint, Marble
	}
	
	protected static final int X_OFFSET = 20, Y_OFFSET = 20;

	// User defined note
	protected String annotation;

	// Options
	public boolean visible;
	public boolean pinned, labelVisible;
	public double size;
	public Color color;

	// Map element type
	public Type mapElementType;

	// Index id for groups
	public long id;

	// The MapElement associated with this state
	protected transient MapElement mapElement;
	// Dialog for viewing the annotation
	protected transient TextDialog annotationDialog;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param mapElementType
	 * @param prefix
	 */
	public MapElementState(long id, Type mapElementType, String prefix) {
		this(id, mapElementType, prefix, 1, Color.white, true, false);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param mapElementType
	 * @param prefix
	 * @param size
	 * @param color
	 * @param labelVisible
	 * @param pinned
	 */
	public MapElementState(long id, Type mapElementType, String prefix, double size, Color color, boolean labelVisible,
		boolean pinned) {
		super(prefix + id, StateType.MapElement, null);
		this.id = id;
		this.size = size;
		this.color = color;
		this.labelVisible = labelVisible;
		this.pinned = pinned;
		visible = true;
		this.mapElementType = mapElementType;
		annotation = "";
	}

	/**
	 * Get the MapElement
	 * 
	 * @return
	 */
	public MapElement getMapElement() {
		return (mapElement);
	}

	/**
	 * Set the MapElement
	 * 
	 * @param me
	 */
	public void setMapElement(MapElement me) {
		this.mapElement = me;
		if (mapElement instanceof Movable) {
			Movable movable = (Movable) mapElement;
			movable.addMotionListener(new MotionListener() {
				@Override
				public void move(Movable mo, ReadOnlyVector3 pos) {
					if (annotationDialog != null) {
						annotationDialog.setMessage(StringUtil.format(mapElement.getLocation()));
					}
				}
			});
		}
	}

	@Override
	public void save() {
		super.save();
		if (mapElement != null) {
			name = mapElement.getName();
			size = mapElement.getSize();
			color = mapElement.getColor();
			pinned = mapElement.isPinned();
			visible = mapElement.isVisible();
			labelVisible = mapElement.isLabelVisible();
		}
	}

	/**
	 * Open the annotation
	 */
	public void openAnnotation() {
		if (annotationDialog == null) {
			annotationDialog = new TextDialog(null, name, 400, 200, true, false);
		}
		if (mapElement != null) {
			annotationDialog.setMessage(StringUtil.format(mapElement.getLocation()));
		}
		annotationDialog.setText(annotation);
		annotationDialog.open();
	}

	/**
	 * Set the annotation
	 * 
	 * @param note
	 */
	public void setAnnotation(String note) {
		if (note != null) {
			annotation = note;
		}
		if (annotationDialog != null) {
			annotationDialog.setText(note);
		}
	}

	/**
	 * Get the annotation
	 * 
	 * @return
	 */
	public String getAnnotation() {
		return (annotation);
	}

	@Override
	public void dispose() {
		if (mapElement != null) {
			Spatial spatial = (Spatial) mapElement;
			Node parent = spatial.getParent();
			if (parent != null) {
				parent.detachChild(spatial);
			}
			mapElement = null;
		}
		if (annotationDialog != null) {
			annotationDialog.close();
		}
		annotationDialog = null;
	}

	/**
	 * Open a view associated with the MapElement
	 * 
	 * @return
	 */
	public View open() {
		// This state element has no view
		if (viewData == null) {
			return (null);
		}
		
		// The view is not visible
		if (!viewData.isVisible())
			return(null);
		
		// This state element has a view
		Window window = viewData.getViewWindow();
		if (window != null) {
			window.setVisible(true);
			return (viewData.view);
		}
		
		// No view, create one
		createView();
		viewData.viewWindow.setVisible(true);
		return (viewData.view);
	}
	
	protected void createView() {
		// nothing here
	}

}
