/**

DERT is a viewer for digital terrain models created from data collected during NASA missions.

DERT is Released in under the NASA Open Source Agreement (NOSA) found in the “LICENSE” folder where you
downloaded DERT.

DERT includes 3rd Party software. The complete copyright notice listing for DERT is:

Copyright © 2015 United States Government as represented by the Administrator of the National Aeronautics and
Space Administration.  No copyright is claimed in the United States under Title 17, U.S.Code. All Other Rights
Reserved.

Desktop Exploration of Remote Terrain (DERT) could not have been written without the aid of a number of free,
open source libraries. These libraries and their notices are listed below. Find the complete third party license
listings in the separate “DERT Third Party Licenses” pdf document found where you downloaded DERT in the
LICENSE folder.
 
JogAmp Ardor3D Continuation
Copyright © 2008-2012 Ardor Labs, Inc.
 
JogAmp
Copyright 2010 JogAmp Community. All rights reserved.
 
JOGL Portions Sun Microsystems
Copyright © 2003-2009 Sun Microsystems, Inc. All Rights Reserved.
 
JOGL Portions Silicon Graphics
Copyright © 1991-2000 Silicon Graphics, Inc.
 
Light Weight Java Gaming Library Project (LWJGL)
Copyright © 2002-2004 LWJGL Project All rights reserved.
 
Tile Rendering Library - Brain Paul 
Copyright © 1997-2005 Brian Paul. All Rights Reserved.
 
OpenKODE, EGL, OpenGL , OpenGL ES1 & ES2
Copyright © 2007-2010 The Khronos Group Inc.
 
Cg
Copyright © 2002, NVIDIA Corporation
 
Typecast - David Schweinsberg 
Copyright © 1999-2003 The Apache Software Foundation. All rights reserved.
 
PNGJ - Herman J. Gonzalez and Shawn Hartsock
Copyright © 2004 The Apache Software Foundation. All rights reserved.
 
Apache Harmony - Open Source Java SE
Copyright © 2006, 2010 The Apache Software Foundation.
 
Guava
Copyright © 2010 The Guava Authors
 
GlueGen Portions
Copyright © 2010 JogAmp Community. All rights reserved.
 
GlueGen Portions - Sun Microsystems
Copyright © 2003-2005 Sun Microsystems, Inc. All Rights Reserved.
 
SPICE
Copyright © 2003, California Institute of Technology.
U.S. Government sponsorship acknowledged.
 
LibTIFF
Copyright © 1988-1997 Sam Leffler
Copyright © 1991-1997 Silicon Graphics, Inc.
 
PROJ.4
Copyright © 2000, Frank Warmerdam

LibJPEG - Independent JPEG Group
Copyright © 1991-2018, Thomas G. Lane, Guido Vollbeding
 

Disclaimers

No Warranty: THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY KIND,
EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
THAT THE SUBJECT SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY
WARRANTY THAT THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE. THIS AGREEMENT
DOES NOT, IN ANY MANNER, CONSTITUTE AN ENDORSEMENT BY GOVERNMENT AGENCY OR ANY
PRIOR RECIPIENT OF ANY RESULTS, RESULTING DESIGNS, HARDWARE, SOFTWARE PRODUCTS OR
ANY OTHER APPLICATIONS RESULTING FROM USE OF THE SUBJECT SOFTWARE.  FURTHER,
GOVERNMENT AGENCY DISCLAIMS ALL WARRANTIES AND LIABILITIES REGARDING THIRD-PARTY
SOFTWARE, IF PRESENT IN THE ORIGINAL SOFTWARE, AND DISTRIBUTES IT "AS IS."

Waiver and Indemnity:  RECIPIENT AGREES TO WAIVE ANY AND ALL CLAIMS AGAINST THE UNITED
STATES GOVERNMENT, ITS CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR
RECIPIENT.  IF RECIPIENT'S USE OF THE SUBJECT SOFTWARE RESULTS IN ANY LIABILITIES,
DEMANDS, DAMAGES, EXPENSES OR LOSSES ARISING FROM SUCH USE, INCLUDING ANY DAMAGES
FROM PRODUCTS BASED ON, OR RESULTING FROM, RECIPIENT'S USE OF THE SUBJECT SOFTWARE,
RECIPIENT SHALL INDEMNIFY AND HOLD HARMLESS THE UNITED STATES GOVERNMENT, ITS
CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR RECIPIENT, TO THE EXTENT
PERMITTED BY LAW.  RECIPIENT'S SOLE REMEDY FOR ANY SUCH MATTER SHALL BE THE IMMEDIATE,
UNILATERAL TERMINATION OF THIS AGREEMENT.

**/

package gov.nasa.arc.dert.view.world;

import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.action.UndoHandler;
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
import gov.nasa.arc.dert.action.mapelement.LockMapElementAction;
import gov.nasa.arc.dert.action.mapelement.OnGroundAction;
import gov.nasa.arc.dert.action.mapelement.OpenAnnotationAction;
import gov.nasa.arc.dert.action.mapelement.OpenBillboardAction;
import gov.nasa.arc.dert.action.mapelement.PlaceHereAction;
import gov.nasa.arc.dert.action.mapelement.RenameAction;
import gov.nasa.arc.dert.landscape.quadtree.QuadTreeMesh;
import gov.nasa.arc.dert.render.SceneCanvas;
import gov.nasa.arc.dert.render.SceneCanvasPanel;
import gov.nasa.arc.dert.scene.MapElement;
import gov.nasa.arc.dert.scene.Marble;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.scene.featureset.Feature;
import gov.nasa.arc.dert.scene.featureset.FeatureSet;
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
import gov.nasa.arc.dert.view.InputManager;
import gov.nasa.arc.dert.view.mapelement.MapElementsView;
import gov.nasa.arc.dert.viewpoint.CoRAction;
import gov.nasa.arc.dert.viewpoint.ViewDependent;
import gov.nasa.arc.dert.viewpoint.ViewpointController;

import java.awt.Cursor;
import java.awt.PopupMenu;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;

/**
 * Provides an InputHandler for the WorldView.
 *
 */
public class WorldInputHandler
	extends InputManager {

	// Spatial being dragged
	private Movable movable;

	// Mouse
	private int mouseButton;

	// Last spatial selected
	private Spatial lastSelection;

	// Helpers
	private Vector3 lastPosition;
	private Vector3 pickPosition = new Vector3(), pickNormal = new Vector3();

	// Viewpoint
	private ViewpointController controller;

	// Panel
	private SceneCanvasPanel canvasPanel;

	// Fly through path
	private Path path;

	// Tape measure
	private TapeMeasure tape;
	
	private PopupMenu contextMenu;
	
	// Canvas scale
	private double xCanvasScale = 1, yCanvasScale = 1;

	/**
	 * Constructor
	 * 
	 * @param controller
	 * @param canvasPanel
	 */
	public WorldInputHandler(SceneCanvas canvas, ViewpointController controller, SceneCanvasPanel canvasPanel) {
		super(canvas);
		this.controller = controller;
		this.canvasPanel = canvasPanel;
		contextMenu = new PopupMenu("");
		canvasPanel.add(contextMenu);
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
			canvasPanel.setCursor(null);
		} else if (path != null) {
			Console.println("Single-click to add a point to "+path.getName()+". Select \"Path Complete\" from context menu when finished.");
			canvasPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
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
			canvasPanel.setCursor(null);
		} else {
			canvasPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
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
			((ViewDependent) movable).update(controller.getViewpoint().getCamera());
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
		
		// We picked the landscape
		if (spat instanceof QuadTreeMesh)
			return(false);
		
		// Is spat a movable?
		movable = findMovable(spat);		
//		if (movable != null)
//			spat = movable;
//		
//		while (!(spat instanceof MapElement) && (spat != null)) {
//			spat = spat.getParent();
//		}
//		if (spat != null) {
//			MapElementsView view = ConfigurationManager.getInstance().getCurrentConfiguration().getMapElementsView();
//			if (view != null)
//				view.selectMapElement((MapElement)spat);
//		}
		return (hasMouse());
	}

	protected Movable findMovable(Spatial spat) {
		while (spat != null) {
			if (spat instanceof Movable) {
				if (!((Movable) spat).isLocked() && !(spat instanceof Marble)) {
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
		this.mouseButton = mouseButton;
		controller.mousePress(x, y, mouseButton);
		Spatial spatial = controller.doPick(x*xCanvasScale, y*yCanvasScale, pickPosition, pickNormal, false);
		if (spatial != null) {
			if (mouseButton == 1) {
				if ((tape == null) && (path == null)) {
					if (grab(spatial, pickPosition)) {
						lastPosition = new Vector3(movable.getTranslation());
						((Spatial) movable).getSceneHints().setAllPickingHints(false);
						movable.setInMotion(true, pickPosition);
						canvasPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					}
				}
				else
					// Workaround for cursor changing to default after clicking on tape dialog when it is
					// entirely inside the main window. Cursor will be changed back to crosshair on
					// release.
					canvasPanel.setCursor(null);
			}
			lastSelection = SpatialUtil.getPickHost(spatial);
//			System.err.println("WorldInputHandler.mousePress "+spatial);
		}
	}

	/**
	 * Mouse button was released
	 */
	@Override
	public void mouseRelease(int x, int y, int mouseButton) {
		this.mouseButton = mouseButton;
		if (mouseButton == 1) {
			if ((tape == null) && (path == null)) {
				if (hasMouse()) {
					((Spatial) movable).getSceneHints().setAllPickingHints(true);
					movable.setInMotion(false, null);
					UndoHandler.getInstance().addEdit(new MoveEdit(movable, lastPosition));
					movable = null;
					canvasPanel.setCursor(null);
				}
			}
			else
				// Rest of workaround for cursor changing to default after clicking on tape dialog when it is
				// entirely inside the main window.
				canvasPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));				
		}
		controller.mouseRelease(x, y, mouseButton);
	}

	/**
	 * Mouse was moved
	 */
	@Override
	public void mouseMove(int x, int y, int dx, int dy, int mouseButton, boolean cntlDown, boolean shiftDown) {
		this.mouseButton = mouseButton;
		if (mouseButton > 1) {
			controller.mouseMove(x, y, dx, dy, mouseButton);
		} else if (!hasMouse()) {
			controller.mouseMove(x, y, dx, dy, mouseButton);
		}
		if (tape != null) {
			controller.doPick(x*xCanvasScale, y*yCanvasScale, pickPosition, pickNormal, false);
			tape.move(pickPosition);
		}
		if (hasMouse()) {
			if (shiftDown) {
//				controller.getViewpointNode().coordInScreenPlane(dx, dy, tmpVec, pickPosition);
				double s = controller.getViewpoint().getCamera().getPixelSizeAt(pickPosition, false);
				movable.setZOffset(movable.getZOffset()+dy*xCanvasScale*s, true);
			} else {
				controller.doPick(x*xCanvasScale, y*yCanvasScale, pickPosition, pickNormal, true);
				move(pickPosition, pickNormal);
			}
		}
	}

	/**
	 * Mouse button was single-clicked
	 */
	@Override
	public void mouseClick(int x, int y, int mouseButton) {
		this.mouseButton = mouseButton;
		if (mouseButton == 1) {
			// tape measure overrides path way point input
			// Place tape measure end point
			if (tape != null) {
				tape.click(pickPosition);
			} else if (path != null) {
				path.click(pickPosition);
			} else if (lastSelection instanceof World) {
				World.getInstance().getMarble().update(pickPosition, getPickNormal(), controller.getViewpoint().getCamera());
			} else if (lastSelection instanceof MapElement) {
				MapElementsView view = ConfigurationManager.getInstance().getCurrentConfiguration().getMapElementsView();
				if (view != null)
					view.selectMapElement((MapElement)lastSelection);
			}

		} else if (mouseButton == 2) {
			// set the new center of rotation
			setCenterOfRotation();
		} else if (mouseButton == 3) {
			doContextMenu(x, y);
		}
	}
	
	private void doContextMenu(int x, int y) {
		if (path != null) {
			contextMenu.removeAll();
			contextMenu.add(new MenuItemAction("Path Complete") {
				@Override
				protected void run() {
					setPath(null);
				}					
			});
			contextMenu.show(canvasPanel, x, height - y);
		}
		else if (!hasMouse()) {
			if (lastSelection != null) {
				contextMenu.removeAll();
				// display context menu
				contextMenu.add(new CoRAction(this));
				// clicked in terrain
				if (lastSelection instanceof World) {
					PopupMenu submenu = new PopupMenu("Add");
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
					contextMenu.add(submenu);
					contextMenu.add(new PlaceHereAction(pickPosition));
				}
				// clicked on map element
				else if (lastSelection instanceof MapElement) {
					MapElement mapElement = (MapElement) lastSelection;
					if (mapElement instanceof Waypoint) {
						contextMenu.add(new HideMapElementAction(((Waypoint) mapElement).getPath()));
						contextMenu.add(new DeleteMapElementAction(((Waypoint) mapElement).getPath()));
						contextMenu.add(new RenameAction(((Waypoint) mapElement).getPath()));
						contextMenu.add(new EditAction((Waypoint) mapElement, ((Waypoint) mapElement).getPath().getName()));
						contextMenu.add(new LockMapElementAction(((Waypoint) mapElement).getPath()));
						contextMenu.add(new OnGroundAction((Waypoint) mapElement));
						contextMenu.add(new OpenAnnotationAction(((Waypoint)mapElement).getPath()));
						contextMenu.add(new OpenAnnotationAction(mapElement));
					}
					else if (mapElement instanceof FeatureSet) {
						contextMenu.add(new HideMapElementAction(mapElement));
						contextMenu.add(new DeleteMapElementAction(mapElement));
						contextMenu.add(new RenameAction(mapElement));
						contextMenu.add(new EditAction(mapElement));
						contextMenu.add(new OpenAnnotationAction(mapElement));
					} else if (mapElement instanceof Feature) {
						contextMenu.add(new HideMapElementAction(mapElement));
					} else {
						contextMenu.add(new HideMapElementAction(mapElement));
						contextMenu.add(new DeleteMapElementAction(mapElement));
						contextMenu.add(new RenameAction(mapElement));
						contextMenu.add(new EditAction(mapElement));
						contextMenu.add(new LockMapElementAction(mapElement));
						contextMenu.add(new OnGroundAction(mapElement));
						contextMenu.add(new OpenAnnotationAction(mapElement));
						if (mapElement instanceof ImageBoard) {
							contextMenu.add(new OpenBillboardAction((ImageBoard) mapElement));
						}
					}
				} else if (lastSelection instanceof BillboardMarker) {
					Node parent = ((BillboardMarker) lastSelection).getParent();
					if (parent instanceof MapElement) {
						contextMenu.add(new HideMapElementAction((MapElement) parent));
						contextMenu.add(new DeleteMapElementAction((MapElement) parent));
						contextMenu.add(new RenameAction((MapElement) parent));
						contextMenu.add(new EditAction((MapElement) parent));
						contextMenu.add(new LockMapElementAction((MapElement) parent));
						contextMenu.add(new OnGroundAction((MapElement) parent));
						contextMenu.add(new OpenAnnotationAction((MapElement) parent));
					}
				}
				contextMenu.show(canvasPanel, x, canvasPanel.getHeight() - y);
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
		controller.getViewpoint().setCenterOfRotation(getPickLocation(), true);
	}
	
	public void setCanvasScale(double xScale, double yScale) {
		xCanvasScale = xScale;
		yCanvasScale = yScale;
	}

}
