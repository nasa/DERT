package gov.nasa.arc.dert.view.world;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.action.mapelement.AddBillboardAction;
import gov.nasa.arc.dert.action.mapelement.AddCameraAction;
import gov.nasa.arc.dert.action.mapelement.AddCartesianGridAction;
import gov.nasa.arc.dert.action.mapelement.AddFigureAction;
import gov.nasa.arc.dert.action.mapelement.AddPathAction;
import gov.nasa.arc.dert.action.mapelement.AddPlacemarkAction;
import gov.nasa.arc.dert.action.mapelement.AddPlaneAction;
import gov.nasa.arc.dert.action.mapelement.AddProfileAction;
import gov.nasa.arc.dert.action.mapelement.AddRadialGridAction;
import gov.nasa.arc.dert.action.mapelement.AddScaleAction;
import gov.nasa.arc.dert.action.mapelement.DeleteMapElementAction;
import gov.nasa.arc.dert.action.mapelement.EditAction;
import gov.nasa.arc.dert.action.mapelement.HideMapElementAction;
import gov.nasa.arc.dert.action.mapelement.OnGroundAction;
import gov.nasa.arc.dert.action.mapelement.OpenAnnotationAction;
import gov.nasa.arc.dert.action.mapelement.OpenBillboardAction;
import gov.nasa.arc.dert.action.mapelement.PinMapElementAction;
import gov.nasa.arc.dert.action.mapelement.PlaceHereAction;
import gov.nasa.arc.dert.action.mapelement.RenameAction;
import gov.nasa.arc.dert.render.SceneCanvasPanel;
import gov.nasa.arc.dert.scene.LineSet;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.Marble;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.landmark.ImageBoard;
import gov.nasa.arc.dert.scene.landmark.Landmark;
import gov.nasa.arc.dert.scene.tapemeasure.TapeMeasure;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scene.tool.Tool;
import gov.nasa.arc.dert.scene.tool.Waypoint;
import gov.nasa.arc.dert.scenegraph.BillboardMarker;
import gov.nasa.arc.dert.scenegraph.Movable;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.util.SpatialUtil;
import gov.nasa.arc.dert.view.Console;
import gov.nasa.arc.dert.view.InputHandler;
import gov.nasa.arc.dert.view.mapelement.MapElementsView;
import gov.nasa.arc.dert.viewpoint.CoRAction;
import gov.nasa.arc.dert.viewpoint.ViewDependent;
import gov.nasa.arc.dert.viewpoint.ViewpointController;

import java.awt.Cursor;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;

/**
 * Provides an InputHandler for the WorldView.
 *
 */
public class WorldInputHandler implements InputHandler {

	// Spatial being dragged
	private Movable movable;

	// Mouse position
	private int mouseX, mouseY, mouseButton;

	// Last spatial selected
	private Spatial lastSelection;

	// Helpers
	private Vector3 lastPosition;
	private Vector3 pickPosition = new Vector3(), pickNormal = new Vector3();
//	private boolean lastStrictZ;

	// Viewpoint
	private ViewpointController controller;

	// Panel
	private SceneCanvasPanel canvasPanel;

	// Fly through path
	private Path path;

	// Tape measure
	private TapeMeasure tape;

	/**
	 * Constructor
	 * 
	 * @param controller
	 * @param canvasPanel
	 */
	public WorldInputHandler(ViewpointController controller, SceneCanvasPanel canvasPanel) {
		this.controller = controller;
		this.canvasPanel = canvasPanel;
	}

	/**
	 * Get the location where a pick occurred
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getPickLocation() {
		return (pickPosition);
	}

	/**
	 * Get the surface normal at the pick location
	 * 
	 * @return
	 */
	public ReadOnlyVector3 getPickNormal() {
		return (pickNormal);
	}

	/**
	 * Set the path for fly through.
	 * 
	 * @param path
	 */
	public void setPath(Path path) {
		if (this.path != null)
			this.path.complete();
		this.path = path;
		if ((path == null) && (tape == null)) {
			canvasPanel.getCanvas().setCursor(null);
		} else if (path != null) {
			Console.getInstance().println("Single-click to add a point to "+path.getName()+". Select \"Path Complete\" from context menu when finished.");
			canvasPanel.getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}
	}

	/**
	 * Set the tape measure
	 * 
	 * @param tape
	 */
	public void setTapeMeasure(TapeMeasure tape) {
		this.tape = tape;
		if ((path == null) && (tape == null)) {
			canvasPanel.getCanvas().setCursor(null);
		} else {
			canvasPanel.getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}
	}

	/**
	 * Move an object
	 * 
	 * @param pos
	 * @param normal
	 */
	public void move(Vector3 pos, Vector3 normal) {
		if (movable instanceof ViewDependent)
			((ViewDependent) movable).update(controller.getViewpointNode().getCamera());
		movable.setLocation(pos.getX(), pos.getY(), pos.getZ(), false);
	}

	/**
	 * Grab an object
	 * 
	 * @param spat
	 * @param pos
	 * @return
	 */
	public boolean grab(Spatial spat, Vector3 pos) {
		if (mouseButton != 1) {
			return (false);
		}
		movable = findMovable(spat);
		
		spat = movable;
		while (!(spat instanceof MapElement) && (spat != null)) {
			spat = spat.getParent();
		}
		MapElementsView view = ConfigurationManager.getInstance().getCurrentConfiguration().getMapElementsView();
		if (view != null)
			view.selectMapElement((MapElement)spat);
		return (hasMouse());
	}

	protected Movable findMovable(Spatial spat) {
		while (spat != null) {
			if (spat instanceof Movable) {
				if (!((Movable) spat).isPinned() && !(spat instanceof Marble)) {
					return ((Movable) spat);
				} else {
					return (null);
				}
			}
			spat = spat.getParent();
		}
		return (null);
	}

	/**
	 * Determine if this InputHandler is dragging an object
	 * 
	 * @return
	 */
	public boolean hasMouse() {
		return ((movable != null));
	}

	/**
	 * Mouse was scrolled
	 */
	@Override
	public void mouseScroll(int delta) {
		mouseButton = 4;
		controller.mouseScroll(delta);
	}

	/**
	 * Mouse button was pressed
	 */
	@Override
	public void mousePress(int x, int y, int mouseButton, boolean cntlDown, boolean shiftDown) {
		mouseX = x;
		mouseY = y;
		this.mouseButton = mouseButton;
		controller.mousePress(x, y, mouseButton);
		Spatial spatial = controller.doPick(mouseX, mouseY, pickPosition, pickNormal, false);
		if (spatial != null) {
			if (mouseButton == 1) {
				if ((tape == null) && (path == null)) {
					if (grab(spatial, pickPosition)) {
						lastPosition = new Vector3(movable.getTranslation());
						((Spatial) movable).getSceneHints().setAllPickingHints(false);
						movable.setInMotion(true, pickPosition);
						canvasPanel.getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));	
					}
				}
			}
			lastSelection = SpatialUtil.getPickHost(spatial);
		}
	}

	/**
	 * Mouse button was released
	 */
	@Override
	public void mouseRelease(int x, int y, int mouseButton) {
		mouseX = x;
		mouseY = y;
		this.mouseButton = mouseButton;
		if (mouseButton == 1) {
			if ((tape == null) && (path == null)) {
				if (hasMouse()) {
					((Spatial) movable).getSceneHints().setAllPickingHints(true);
					movable.setInMotion(false, null);
					Dert.getMainWindow().getUndoHandler().addEdit(new MoveEdit(movable, lastPosition));
					movable = null;
					canvasPanel.getCanvas().setCursor(null);
				}
			}
		}
		controller.mouseRelease(x, y, mouseButton);
	}

	/**
	 * Mouse was moved
	 */
	@Override
	public void mouseMove(int x, int y, int dx, int dy, int mouseButton, boolean cntlDown, boolean shiftDown) {
		mouseX = x;
		mouseY = y;
		this.mouseButton = mouseButton;
		if (mouseButton > 1) {
			controller.mouseMove(x, y, dx, dy, mouseButton);
		} else if (!hasMouse()) {
			controller.mouseMove(x, y, dx, dy, mouseButton);
		}
		if (tape != null) {
			controller.doPick(mouseX, mouseY, pickPosition, pickNormal, false);
			tape.move(pickPosition);
		}
		if (hasMouse()) {
			if (shiftDown && (movable instanceof MapElement)) {
//				controller.getViewpointNode().coordInScreenPlane(dx, dy, tmpVec, pickPosition);
				double s = controller.getViewpointNode().getCamera().getPixelSizeAt(pickPosition, false);
				movable.setZOffset(movable.getZOffset()+dy*s, true);
			} else {
				controller.doPick(mouseX, mouseY, pickPosition, pickNormal, true);
				move(pickPosition, pickNormal);
			}
		}
	}

	/**
	 * Mouse button was single-clicked
	 */
	@Override
	public void mouseClick(int x, int y, int mouseButton) {
		mouseX = x;
		mouseY = y;
		this.mouseButton = mouseButton;
		if (mouseButton == 1) {
			// tape measure overrides path way point input
			// Place tape measure end point
			if (tape != null) {
				tape.click(pickPosition);
			} else if (path != null) {
				path.click(pickPosition);
			} else if (lastSelection instanceof World) {
				World.getInstance().getMarble().update(pickPosition, getPickNormal(), controller.getViewpointNode().getCamera());
			}
		} else if (mouseButton == 2) {
			// set the new center of rotation
			setCenterOfRotation();
		} else if (mouseButton == 3) {
			if (path != null) {
				JPopupMenu menu = new JPopupMenu(path.getName());
				menu.add(new MenuItemAction("Path Complete") {
					@Override
					protected void run() {
						setPath(null);
					}					
				});
				menu.show(canvasPanel, x, canvasPanel.getHeight() - y);
			}
			else if (!hasMouse()) {
				if (lastSelection != null) {
					// display context menu
					JPopupMenu menu = new JPopupMenu(lastSelection.getName());
					menu.add(new CoRAction(this));
					// clicked in terrain
					if (lastSelection instanceof World) {
						JMenu submenu = new JMenu("Add");
						submenu.add(new AddPlacemarkAction(pickPosition));
						submenu.add(new AddFigureAction(pickPosition, pickNormal));
						submenu.add(new AddBillboardAction(pickPosition));
						submenu.add(new AddScaleAction(pickPosition));
						submenu.add(new AddRadialGridAction(pickPosition));
						submenu.add(new AddCartesianGridAction(pickPosition));
						submenu.add(new AddPathAction(pickPosition));
						submenu.add(new AddPlaneAction(pickPosition));
						submenu.add(new AddProfileAction(pickPosition));
						submenu.add(new AddCameraAction(pickPosition));
						menu.add(submenu);
						menu.add(new PlaceHereAction(pickPosition));
					}
					// clicked on map element
					else if (lastSelection instanceof MapElement) {
						MapElement mapElement = (MapElement) lastSelection;
						if (mapElement instanceof Waypoint) {
							menu.add(new HideMapElementAction(((Waypoint) mapElement).getPath()));
							menu.add(new DeleteMapElementAction(((Waypoint) mapElement).getPath()));
							menu.add(new RenameAction(((Waypoint) mapElement).getPath()));
							menu.add(new EditAction(((Waypoint) mapElement).getPath()));
							menu.add(new PinMapElementAction(((Waypoint) mapElement).getPath()));
							menu.add(new OnGroundAction((Waypoint) mapElement));
							menu.add(new OpenAnnotationAction(mapElement));
						} else if (!(mapElement instanceof LineSet)) {
							menu.add(new HideMapElementAction(mapElement));
							menu.add(new DeleteMapElementAction(mapElement));
							menu.add(new RenameAction(mapElement));
							menu.add(new EditAction(mapElement));
							menu.add(new PinMapElementAction(mapElement));
							menu.add(new OnGroundAction(mapElement));
							menu.add(new OpenAnnotationAction(mapElement));
							if (mapElement instanceof ImageBoard) {
								menu.add(new OpenBillboardAction((ImageBoard) mapElement));
							}
						}
					} else if (lastSelection instanceof BillboardMarker) {
						Node parent = ((BillboardMarker) lastSelection).getParent();
						if (parent instanceof MapElement) {
							menu.add(new HideMapElementAction((MapElement) parent));
							menu.add(new DeleteMapElementAction((MapElement) parent));
							menu.add(new RenameAction((MapElement) parent));
							menu.add(new EditAction((MapElement) parent));
							menu.add(new PinMapElementAction((MapElement) parent));
							menu.add(new OpenAnnotationAction((MapElement) parent));
						}
					}
					menu.show(canvasPanel, x, canvasPanel.getHeight() - y);
				}
			}
		}
	}

	/**
	 * Mouse button was double-clicked
	 */
	@Override
	public void mouseDoubleClick(int x, int y, int mouseButton) {
		if (mouseButton != 1) {
			return;
		}
		if ((tape != null) || (path != null)) {
			return;
		}
		if ((lastSelection != null)
			&& ((lastSelection instanceof Landmark) || (lastSelection instanceof Tool) || (lastSelection instanceof Waypoint))) {
			MapElement mElement = (MapElement) lastSelection;
			mElement.getState().openAnnotation();
		}
	}

	@Override
	public void stepUp(boolean shiftDown) {
		controller.stepUp(shiftDown);
	}

	@Override
	public void stepDown(boolean shiftDown) {
		controller.stepDown(shiftDown);
	}

	@Override
	public void stepRight(boolean shiftDown) {
		controller.stepRight(shiftDown);
	}

	@Override
	public void stepLeft(boolean shiftDown) {
		controller.stepLeft(shiftDown);
	}

	public void setCenterOfRotation() {
		controller.getViewpointNode().setCenterOfRotation(getPickLocation());
	}

	public void setLookAt(ReadOnlyVector3 pos) {
		controller.getViewpointNode().setLookAt(pos);
	}

}
