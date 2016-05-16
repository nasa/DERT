package gov.nasa.arc.dert.state;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.util.StateUtil;
import gov.nasa.arc.dert.view.ColorBarView;
import gov.nasa.arc.dert.view.ConsoleView;
import gov.nasa.arc.dert.view.HelpView;
import gov.nasa.arc.dert.view.View;
import gov.nasa.arc.dert.view.lighting.LightPositionView;
import gov.nasa.arc.dert.view.lighting.LightingView;
import gov.nasa.arc.dert.view.mapelement.MapElementsView;
import gov.nasa.arc.dert.view.surfaceandlayers.SurfaceAndLayersView;
import gov.nasa.arc.dert.view.viewpoint.ViewpointView;

import java.awt.Window;
import java.util.HashMap;

/**
 * Base class for state objects based on AWT or Swing panels.
 *
 */
public class PanelState extends State {

	// Types of views
	public static enum PanelType {
		Console, Help, ColorBars, SurfaceAndLayers, Lighting, LightPosition, MapElements, Viewpoint
	}

	public PanelType type;
	public String title;

	/**
	 * Constructor
	 * 
	 * @param type
	 * @param title
	 * @param viewData
	 */
	public PanelState(PanelType type, String title, ViewData viewData) {
		super(type.toString(), StateType.Panel, viewData);
		this.type = type;
		this.title = title;
	}
	
	/**
	 * Constructor from hash map.
	 */
	public PanelState(HashMap<String,Object> map) {
		super(map);
		String str = StateUtil.getString(map, "PanelType", null);
		if (str == null)
			throw new NullPointerException("Panel has no type.");
		type = PanelType.valueOf(str);
		title = StateUtil.getString(map, "PanelTitle", "");
	}
	
	@Override
	public boolean isEqualTo(State state) {
		if ((state == null) || !(state instanceof PanelState)) 
			return(false);
		PanelState that = (PanelState)state;
		if (!super.isEqualTo(that)) 
			return(false);
		if (!this.title.equals(that.title)) 
			return(false);
		if (this.type != that.type) 
			return(false);
		return(true);
	}

	/**
	 * Save contents
	 */
	@Override
	public HashMap<String,Object> save() {
		HashMap<String,Object> map = super.save();
		map.put("PanelType", type.toString());
		map.put("PanelTitle", title);
		return(map);
	}

	/**
	 * Open the view
	 * 
	 * @return
	 */
	public View open() {
		Window window = viewData.getViewWindow();
		View view = viewData.getView();
		if (window != null) {
			window.setVisible(true);
			return (view);
		}
		int xOffset = 20;
		int yOffset = 20;
		if (view == null) {
			switch (type) {
			case Help:
				view = new HelpView(this);
				break;
			case ColorBars:
				view = new ColorBarView(this);
				break;
			case Console:
				view = new ConsoleView(this);
				window = Dert.getConsoleWindow();
				xOffset = 0;
				yOffset = 600;
				break;
			case MapElements:
				view = new MapElementsView((MapElementsState) this);
				break;
			case Lighting:
				view = new LightingView(this);
				break;
			case LightPosition:
				view = new LightPositionView(this);
				break;
			case SurfaceAndLayers:
				view = new SurfaceAndLayersView(this);
				break;
			case Viewpoint:
				view = new ViewpointView((ViewpointState) this);
				break;
			default:
				throw new IllegalArgumentException("Unknown panel type " + type);
			}
			setView(view);
		}
		if (window == null) {
			window = viewData.createWindow(Dert.getMainWindow(), title, xOffset, yOffset);
		} else {
			viewData.setViewWindow(window, true, xOffset, yOffset);
		}
		window.setVisible(true);
		return (view);
	}

}
