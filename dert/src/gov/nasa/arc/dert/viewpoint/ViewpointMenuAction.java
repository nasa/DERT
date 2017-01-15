package gov.nasa.arc.dert.viewpoint;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.MenuItemAction;
import gov.nasa.arc.dert.action.PopupMenuAction;
import gov.nasa.arc.dert.icon.Icons;
import gov.nasa.arc.dert.scene.World;
import gov.nasa.arc.dert.viewpoint.ViewpointNode.ViewpointMode;

import java.awt.PopupMenu;
import java.awt.Toolkit;

import javax.swing.ImageIcon;

/**
 * Activates hike mode.
 *
 */
public class ViewpointMenuAction extends PopupMenuAction {
	
	protected static ViewpointMenuAction INSTANCE;
	
	public static ImageIcon vpIcon, vpHikeIcon, vpMapIcon, bootIcon, mapIcon;
	
	static {
		vpIcon = Icons.getImageIcon("viewpoint.png");
		vpHikeIcon = Icons.getImageIcon("viewpointonfoot.png");
		vpMapIcon = Icons.getImageIcon("viewpointmap.png");
		bootIcon = Icons.getImageIcon("boot.png");
		mapIcon = Icons.getImageIcon("map.png");
	}

	protected boolean oldOnTop;
	
	public static ViewpointMenuAction getInstance() {
		if (INSTANCE == null)
			INSTANCE = new ViewpointMenuAction();
		return(INSTANCE);
	}

	/**
	 * Constructor
	 */
	protected ViewpointMenuAction() {
		super("set viewpoint mode", null, vpIcon);

	}

	@Override
	protected void fillMenu(PopupMenu menu) {
		MenuItemAction item = new MenuItemAction("Nominal") {			
			@Override
			protected void run() {
				setMode(ViewpointMode.Nominal);
			}
		};
		menu.add(item);
		item = new MenuItemAction("Map") {			
			@Override
			protected void run() {
				setMode(ViewpointMode.Map);
			}
		};
		menu.add(item);
		item = new MenuItemAction("Hike") {			
			@Override
			protected void run() {
				setMode(ViewpointMode.Hike);
			}
		};
		menu.add(item);
	}
	
	protected void setMode(ViewpointMode mode) {
		ViewpointController controller = Dert.getWorldView().getScenePanel().getViewpointController();
		ViewpointMode currentMode = controller.getViewpointNode().getMode();
		if (currentMode == mode)
			return;
		if (!Dert.getWorldView().getViewpointNode().setMode(mode)) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		switch (mode) {
		case Nominal:
			World.getInstance().setMapElementsOnTop(oldOnTop);
			break;
		case Hike:
			World.getInstance().setMapElementsOnTop(oldOnTop);
			controller.updateLookAt();
			break;
		case Map:
			oldOnTop = World.getInstance().isMapElementsOnTop();
			World.getInstance().setMapElementsOnTop(true);
			break;
		}
		setModeIcon(mode);
	}
	
	public void setModeIcon(ViewpointMode mode) {
		switch (mode) {
		case Nominal:
			setIcon(vpIcon);
			break;
		case Hike:
			setIcon(vpHikeIcon);
			break;
		case Map:
			setIcon(vpMapIcon);
			break;
		}
	}

}
